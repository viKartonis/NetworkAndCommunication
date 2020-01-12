package com.game.network;

import com.game.model.*;
import me.ippolitov.fit.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class UDPUnicast
{
    private DatagramSocket datagramSocket;
    private MulticastSocket multicastSocket;
    private Set<IPEndPoint> child;
    private Map<IPEndPoint, Set<Long>> seqAddress;
    private Map<Long, SnakesProto.GameMessage> queueMessage;
    private UDPManager udpManager;
    private int state = 0;
    private boolean isMaster = false;
    private boolean isDeputy = false;
    private IPEndPoint deputyIp;
    private int port = 9192;
    private InetAddress group;
    private GameField field;
    private AtomicLong seq;
    private Map<IPEndPoint, GamePlayer> gamePlayers;
    private  Map<IPEndPoint, Date> lastResponse;
    private SnakesProto.GameMessage announcment;
    private int myId;
    private int nextPlayerId = 1;
    private int timeOut = 3000;
    private List<Integer> needDelete = null;

    public UDPUnicast(GameField field) throws SocketException
    {
        myId = 0;
        seq = new AtomicLong(0);
        seqAddress = new HashMap<>();
        child = new HashSet<>();
        queueMessage = new HashMap<>();
        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(250);
        this.field = field;
        gamePlayers = new HashMap<>();
        lastResponse = new HashMap<>();
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
        udpManager = new UDPManager(datagramSocket, queueMessage, child, seqAddress);
    }

    public int getListenPort()
    {
        return datagramSocket.getLocalPort();
    }

    public class Pair<T, U> {
        public final T first;
        public final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }
    }
    public void becomeMaster()
    {
        System.out.println("Becoming master");
        SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg = SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                .setSenderRole(SnakesProto.NodeRole.MASTER).build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder().setMsgSeq(seq.incrementAndGet())
                .setRoleChange(roleChangeMsg).build();
        try
        {
            for(Map.Entry<IPEndPoint, GamePlayer> player : gamePlayers.entrySet())
            {
                 if (player.getValue().getPlayerId() != myId)
                 {
                     child.add(player.getKey());
                 }
            }
            setAnnouncment("",field.getWidth(),field.getHeight(), field.getFoodStatic(), 150, field.getFoodDrop());
            udpManager.sendDataMessageEveryNode(gameMessage);
            isDeputy = false;
            isMaster = true;

            if(child.iterator().hasNext())
            {
                System.out.println("Assigning deputy");

                deputyIp = child.iterator().next();
                SnakesProto.GameMessage.RoleChangeMsg roleChange = SnakesProto.GameMessage.RoleChangeMsg
                        .newBuilder().setReceiverRole(SnakesProto.NodeRole.DEPUTY).build();
                SnakesProto.GameMessage gameMessageRole = SnakesProto.GameMessage.newBuilder()
                        .setMsgSeq(seq.incrementAndGet()).setRoleChange(roleChange).build();
                udpManager.send(gameMessageRole, deputyIp);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void sendAndCheck(SnakesProto.GameMessage gameMessage, IPEndPoint ipEndPoint)
    {
        Date currentDate = new Date();
        Date date = lastResponse.get(ipEndPoint);
        if(null == date)
        {
            return;
        }

        if(currentDate.getTime() - date.getTime() <= timeOut)
        {
            try
            {
                udpManager.send(gameMessage, ipEndPoint);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            child.remove(ipEndPoint);
            seqAddress.remove(ipEndPoint);
            gamePlayers.remove(ipEndPoint);
            lastResponse.remove(ipEndPoint);

            if(isDeputy && udpManager.getIpEndPointParent().equals(ipEndPoint))
            {
                becomeMaster();
            }
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
            return null;
        }

        int masterPort = -1;
        for (SnakesProto.GamePlayer player : announcementMsg.getAnnouncement().getPlayers().getPlayersList())
        {
            try
            {
                gamePlayers.put(new IPEndPoint(receiveMessage.getAddress(), receiveMessage.getPort()),
                        new GamePlayer(player.getIpAddress(), player.getPort(), player.getScore(), player.getId(),
                                player.getRole(), player.getName()));
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            if (player.getRole() == SnakesProto.NodeRole.MASTER)
            {
                masterPort = player.getPort();
            }
        }
        return new Pair<>(new IPEndPoint(receiveMessage.getAddress(), masterPort),
                announcementMsg.getAnnouncement());
    }


    public Pair<IPEndPoint, SnakesProto.GameMessage> receive()
    {
        byte[] bufferReceive = new byte[4096];
        DatagramPacket receiveMessage = new DatagramPacket(bufferReceive, 4096);

        try
        {
            datagramSocket.receive(receiveMessage);
        }
        catch (SocketTimeoutException e)
        {
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        IPEndPoint senderIp = new IPEndPoint(receiveMessage.getAddress(), receiveMessage.getPort());
        lastResponse.put(senderIp, new Date());
        SnakesProto.GameMessage gameMessage;
        try
        {
            gameMessage = SnakesProto.GameMessage
                    .parseFrom(ByteBuffer.wrap(receiveMessage.getData(), 0, receiveMessage.getLength()));
            switch (gameMessage.getTypeCase())
            {
                case JOIN:
                {
                    sendAck(gameMessage.getMsgSeq(), senderIp);
                    SnakesProto.NodeRole role = SnakesProto.NodeRole.NORMAL;

                    if(null == deputyIp)
                    {
                        System.out.println("Set deputy");
                        role = SnakesProto.NodeRole.DEPUTY;
                        deputyIp = senderIp;
                    }

                    int senderId = nextPlayerId;
                    nextPlayerId++;
                    gamePlayers.put(senderIp,
                            new GamePlayer(senderIp, 0, senderId, role, gameMessage.getJoin().getName()));
                    if(field.addSnake(senderId))
                    {
                        child.add(senderIp);
                        try
                        {
                            SnakesProto.GameMessage gameMessageState = makeStateMessage(senderId);

                            SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg = SnakesProto.GameMessage.RoleChangeMsg
                                    .newBuilder().setReceiverRole(role).build();
                            SnakesProto.GameMessage gameMessageRole = SnakesProto.GameMessage.newBuilder()
                                    .setMsgSeq(seq.incrementAndGet()).setRoleChange(roleChangeMsg).build();
                            sendAndCheck(gameMessageState, senderIp);
                            sendAndCheck(gameMessageRole, senderIp);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        SnakesProto.GameMessage.ErrorMsg errorMsg = SnakesProto.GameMessage.ErrorMsg.newBuilder()
                                .setErrorMessage("Cannot to join to this game").build();
                        SnakesProto.GameMessage gameErrorMessage = SnakesProto.GameMessage.newBuilder().setError(errorMsg)
                                .setMsgSeq(seq.incrementAndGet()).build();
                        try
                        {
                            udpManager.send(gameErrorMessage, senderIp);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                case ACK:
                {
                    if (seqAddress.get(senderIp) != null)
                        seqAddress.get(senderIp).remove(gameMessage.getMsgSeq());
                    break;
                }
                case STATE:
                {
                    sendAck(gameMessage.getMsgSeq(), senderIp);

                    ProtobuffConverter.protoToModel(gameMessage.getState().getState(), field, gamePlayers);

                    break;
                }
                case STEER:
                {
                    sendAck(gameMessage.getMsgSeq(), senderIp);
                    field.setDirection(ProtobuffConverter.protoToModel(gameMessage.getSteer().getDirection()),
                            gamePlayers.get(senderIp).getPlayerId());
                    break;
                }
                case ERROR:
                {
                    sendAck(gameMessage.getMsgSeq(), senderIp);
                    break;
                }
                case PING:
                {
                    sendAck(gameMessage.getMsgSeq(), senderIp);
                    break;
                }
                case ROLE_CHANGE:
                {
                    sendAck(gameMessage.getMsgSeq(), senderIp);

                    if(SnakesProto.NodeRole.MASTER == gameMessage.getRoleChange().getSenderRole() && !isMaster)
                    {
                        System.out.println("Master changed");
                        udpManager.setIpEndPointParent(senderIp);
                    }
                    if(SnakesProto.NodeRole.DEPUTY == gameMessage.getRoleChange().getReceiverRole() && !isMaster)
                    {
                        System.out.println("I am deputy");
                        isDeputy = true;
                    }
                    break;
                }
          }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        return new Pair<>(senderIp, gameMessage);
    }

    public void sendAck(long msgSeq, IPEndPoint endPoint)
    {
        SnakesProto.GameMessage.AckMsg ack = SnakesProto.GameMessage.AckMsg.newBuilder().build();
        SnakesProto.GameMessage ackMsg = SnakesProto.GameMessage.newBuilder().setAck(ack)
                .setMsgSeq(msgSeq).build();
        try
        {
            sendAndCheck(ackMsg, endPoint);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void sendAnnouncement() throws IOException
    {
        if (!isMaster)
            return;

        DatagramPacket sendMessage = new DatagramPacket(announcment.toByteArray(), announcment.toByteArray().length,
                group, port);

        multicastSocket.send(sendMessage);
    }


    public void sendJoin(IPEndPoint ipEndPoint, SnakesProto.PlayerType playersType, boolean viewer,
                         String playerName)
    {
        try
        {
            udpManager.setConnection(ipEndPoint, playersType, viewer, playerName, seq.incrementAndGet());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void sendSteer(SnakesProto.Direction direction)
    {
        if (isMaster)
        {
            field.setDirection(ProtobuffConverter.protoToModel(direction), myId);
            return;
        }

        SnakesProto.GameMessage.SteerMsg steerMessage = SnakesProto.GameMessage
                .SteerMsg.newBuilder()
                .setDirection(direction).build();

        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setSteer(steerMessage).setMsgSeq(seq.incrementAndGet()).build();

        try
        {
            sendAndCheck(gameMessage, udpManager.getIpEndPointParent());
        }
        catch (Exception e)
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

    public IPEndPoint getIpEndPointParent()
    {
        return udpManager.getIpEndPointParent();
    }

    public SnakesProto.GameMessage makeStateMessage()
    {
        SnakesProto.GameMessage.StateMsg stateMsg = SnakesProto.GameMessage.StateMsg.newBuilder()
                .setState(ProtobuffConverter.modelToProto(field, gamePlayers))
                .build();

        return SnakesProto.GameMessage.newBuilder()
                .setState(stateMsg).setMsgSeq(seq.incrementAndGet()).build();

    }

    public SnakesProto.GameMessage makeStateMessage(int receiverId)
    {
        SnakesProto.GameMessage.StateMsg stateMsg = SnakesProto.GameMessage.StateMsg.newBuilder()
                .setState(ProtobuffConverter.modelToProto(field, gamePlayers))
                .build();

       return SnakesProto.GameMessage.newBuilder()
                .setState(stateMsg).setMsgSeq(seq.incrementAndGet()).setReceiverId(receiverId).build();

    }

    public void sendState()
    {
        if (!isMaster)
            return;

        try
        {
            SnakesProto.GameMessage gameMessageState = makeStateMessage();

            for (IPEndPoint childIp : child)
            {
                sendAndCheck(gameMessageState, childIp);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void setAnnouncment(String playerName, int width, int height, int foodNum, int timenum, float foodDrop)
    {
        SnakesProto.GamePlayer masterPlayer = SnakesProto.GamePlayer.newBuilder().setId(0)
                .setPort(getListenPort()).setRole(SnakesProto.NodeRole.MASTER)
                .setType(SnakesProto.PlayerType.HUMAN).setIpAddress("127.0.0.1").setScore(0)
                .setName(playerName).build();
        SnakesProto.GamePlayers allPlayers = SnakesProto.GamePlayers.newBuilder().addPlayers(masterPlayer)
                .build();

        SnakesProto.GameConfig gameConfig = SnakesProto.GameConfig.newBuilder().setWidth(width)
                .setHeight(height)
                .setFoodStatic(foodNum)
                .setStateDelayMs(timenum)
                .setDeadFoodProb(foodDrop)
                .build();

        SnakesProto.GameMessage.AnnouncementMsg announcementMsg =
                SnakesProto.GameMessage.AnnouncementMsg.newBuilder().setPlayers(allPlayers).setConfig(gameConfig).build();

        announcment = SnakesProto.GameMessage.newBuilder().setAnnouncement(announcementMsg)
                .setMsgSeq(seq.incrementAndGet())
                .build();
    }

    public void resendMessages()
    {
        for (Map.Entry<IPEndPoint, Set<Long>> current : seqAddress.entrySet())
        {
            for (Long uuid : current.getValue())
            {
                try
                {
                    SnakesProto.GameMessage message = queueMessage.get(uuid);
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
    }

    public void sendPing()
    {
        SnakesProto.GameMessage.PingMsg pingMsg = SnakesProto.GameMessage.PingMsg.newBuilder().build();
        SnakesProto.GameMessage gamePingMessage = SnakesProto.GameMessage.newBuilder().setMsgSeq(seq.incrementAndGet())
                .setPing(pingMsg).build();
        try
        {
            if(isMaster)
            {
                udpManager.sendDataMessageEveryNode(gamePingMessage);
            }
            else if (null != getIpEndPointParent())
            {
                sendAndCheck(gamePingMessage, udpManager.getIpEndPointParent());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean isMaster()
    {
        return isMaster;
    }

    public void setMaster(boolean master)
    {
        isMaster = master;
        if (isMaster)
        {
            gamePlayers.clear();
            try
            {
                IPEndPoint myIp = new IPEndPoint(InetAddress.getByName("127.0.0.1"), datagramSocket.getLocalPort());
                gamePlayers.put(myIp, new GamePlayer(myIp, 0, 0, SnakesProto.NodeRole.MASTER,
                        "MASTER"));
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void setId(int id)
    {
        myId = id;
    }
}



