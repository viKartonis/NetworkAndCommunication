package com.game.network;

import com.game.controller.NetworkController;
import me.ippolitov.fit.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class UDPUnicast
{
    private DatagramSocket datagramSocket;
    private MulticastSocket multicastSocket;

    private int timeOutInSecond;
    private String name;
    private Set<IPEndPoint> child;
    private IPEndPoint ipEndPointReserveNode;
    private Map<IPEndPoint, Date> tracker;
    private Map<IPEndPoint, Set<Long>> seqAddress;
    private Map<Long, SnakesProto.GameMessage> queueMessage;

    private IPEndPoint master;
    private Set<Long> dataUid;
    private UDPManager udpManager;
    private NetworkController networkController;
    private int state = 0;
    private SnakesProto.GameMessage message;
    private boolean deputy = false;
    private int port = 9192;
    private String address = "127.0.0.1";
    private InetAddress group;

    public UDPUnicast() throws SocketException
    {
        seqAddress = new HashMap<>();
        dataUid = new HashSet<>();
        tracker = new HashMap<>();
        timeOutInSecond = 10;
        child = new HashSet<>();
        queueMessage = new HashMap<>();
        networkController = new NetworkController();
        datagramSocket = new DatagramSocket();
        try
        {
            group = InetAddress.getByName("239.192.0.4");
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        try
        {
            multicastSocket = new MulticastSocket(port);
            multicastSocket.joinGroup(group);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public SnakesProto.GameMessage receiveMessage()
    {
        try
        {
            System.out.println("Receive message");
            int capacity = 4096;
            datagramSocket.setSoTimeout(3000);

            Date start = new Date(), end = start;
            while (end.getTime() - start.getTime() < 3000)
            {
                try
                {
                    //ждать сообщения
                    byte[] bufferReceive = new byte[capacity];
                    DatagramPacket receiveMessage = new DatagramPacket(bufferReceive, capacity);

                    datagramSocket.receive(receiveMessage);

                    message = SnakesProto.GameMessage.parseFrom(receiveMessage.getData());

                    udpManager = new UDPManager(datagramSocket, queueMessage, child, seqAddress, master);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            end = new Date();
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        return message;
    }


    public void sendSteer() throws Exception
    {
        SnakesProto.GameMessage gameMessage = networkController.getGameMessage();
        udpManager.send(gameMessage, master);
    }

    public class Pair<T, U> {
        public final T t;
        public final U u;

        public Pair(T t, U u) {
            this.t= t;
            this.u= u;
        }
    }

    public Pair<IPEndPoint, SnakesProto.GameMessage.AnnouncementMsg> receiveAnnouncement()
    {
        byte[] bufferReceive = new byte[4096];
        DatagramPacket receiveMessage = new DatagramPacket(bufferReceive, 4096, group, port);

        try
        {
            multicastSocket.receive(receiveMessage);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        SnakesProto.GameMessage announcementMsg;
        try
        {
            announcementMsg = SnakesProto.GameMessage
                    .parseFrom(ByteBuffer.wrap(receiveMessage.getData(), 0, receiveMessage.getLength()));

        }
        catch (IOException e)
        {
            //e.printStackTrace();
            return null;
        }

        return new Pair<>(new IPEndPoint(receiveMessage.getAddress(), receiveMessage.getPort()),
                announcementMsg.getAnnouncement());
    }


    public Pair receiveJoin()
    {
        byte[] bufferReceive = new byte[4096];
        DatagramPacket receiveMessage = new DatagramPacket(bufferReceive, 4096);

        try
        {
            datagramSocket.receive(receiveMessage);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        SnakesProto.GameMessage joinMsg;
        try
        {
            joinMsg = SnakesProto.GameMessage
                    .parseFrom(ByteBuffer.wrap(receiveMessage.getData(), 0, receiveMessage.getLength()));

        }
        catch (IOException e)
        {
            //e.printStackTrace();
            return null;
        }

        return new Pair<>(new IPEndPoint(receiveMessage.getAddress(),
                receiveMessage.getPort()), joinMsg.getJoin());
    }

    public void sendGameState(SnakesProto.GameMessage gameMessage)
    {
        try
        {
            udpManager.sendDataMessageEveryNode(gameMessage);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void sendAnnouncement(SnakesProto.GameMessage message) throws IOException
    {
        int len = message.toByteArray().length;
        DatagramPacket sendMessage = new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                group, port);

       SnakesProto.GameMessage announcementMsg = SnakesProto.GameMessage
                .parseFrom(ByteBuffer.wrap(sendMessage.getData(), 0, sendMessage.getLength()));

        multicastSocket.send(sendMessage);
    }


    public void sendJoin(IPEndPoint ipEndPoint, SnakesProto.PlayerType playersType, boolean viewer,
                         String playerName)
    {
        try
        {
            udpManager.setConnection(ipEndPoint, playersType, viewer, playerName);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void sendMessage(SnakesProto.GameMessage message)
    {
        //проверить когда наш ресивер последний раз был в сети
        Date time = new Date();
        List<IPEndPoint> deleteList = new LinkedList<>();
        for (Map.Entry<IPEndPoint, Date> current : tracker.entrySet())
        {
            if (time.getTime() - current.getValue().getTime() >= timeOutInSecond * 1000)
            {
                deleteList.add(current.getKey());
                System.out.println("DELETE" + current.getKey().toString());

                if (current.getKey().equals(master))
                {
                    //перестроить дерево
                    master = ipEndPointReserveNode;
                    try
                    {
                        udpManager.setConnection(ipEndPointReserveNode, SnakesProto.PlayerType.HUMAN,
                                false,
                                name);
                        ipEndPointReserveNode = null;

                        if (deputy)
                        {
                            SnakesProto.GameMessage.RoleChangeMsg masterMessage = SnakesProto.GameMessage
                                    .RoleChangeMsg.newBuilder()
                                    .setSenderRole(SnakesProto.NodeRole.MASTER)
                                    .build();

                            SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                                    .setRoleChange(masterMessage).build();

                            udpManager.sendDataMessageExceptOne(gameMessage, master);
                        }

                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    //удалить которые сломались
                    child.remove(current.getKey());
                }
            }
        }
        for (IPEndPoint ipEndPoint : deleteList)
        {
            tracker.remove(ipEndPoint);
        }

        //перепослать сообщения
        for (Map.Entry<IPEndPoint, Set<Long>> current : seqAddress.entrySet())
        {
            for (Long uuid : current.getValue())
            {
                //byte[] message = queueMessage.get(uuid).toByteArray();
                try
                {
                    datagramSocket.send(new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                            current.getKey().getAddress(),
                            current.getKey().getPort()));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        //ping
        SnakesProto.GameMessage.PingMsg pingMessage = SnakesProto.GameMessage
                .PingMsg.newBuilder().build();

        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setPing(pingMessage).build();
        try
        {
            udpManager.sendDataMessageEveryNode(gameMessage);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public DatagramSocket getDatagramSocket()
    {
        return datagramSocket;
    }

    public  Map<Long, SnakesProto.GameMessage> getQueueMessage()
    {
        return queueMessage;
    }

    public Set<IPEndPoint> getChild()
    {
        return child;
    }

    public Map<IPEndPoint, Set<Long>> getSeqAddress()
    {
        return seqAddress;
    }

    public IPEndPoint getMaster()
    {
        return master;
    }


}



