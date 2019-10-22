package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.*;

public class TreeNode
{
    private DatagramSocket socket;
    private int port;
    private int timeOutInSecond;
    private String name;
    private int loss;
    private Set<IPEndPoint> child;
    private IPEndPoint ipEndPointReserveNode;
    private Map<IPEndPoint, Date> tracker;
    private Map<IPEndPoint, Set<UUID>> uuidAddress;///premature optimization is the root of evil
    private Map<UUID, Message> queueMessage;

    private IPEndPoint ipEndPointParent;
    private Set<UUID> dataUid;

    public void createOrInsert(IPEndPoint ipEndPoint, UUID uuid)
    {
        if (uuidAddress.containsKey(ipEndPoint))
        {
            uuidAddress.get(ipEndPoint).add(uuid);
        }
        else
        {
            Set<UUID> setUuid = new HashSet<>();
            setUuid.add(uuid);
            uuidAddress.put(ipEndPoint, setUuid);
        }
    }


    public TreeNode(String name, int loss, int port)
    {
        this(name, loss, port, null, 0);
    }

    public TreeNode(String name, int loss, int port, InetAddress address, int portParent)
    {
        uuidAddress = new HashMap<>();
        dataUid = new HashSet<>();
        tracker = new HashMap<>();
        this.port = port;
        timeOutInSecond = 10;
        this.name = name;
        this.loss = loss;
        child = new HashSet<>();
        if (address != null)
        {
            ipEndPointParent = new IPEndPoint(address, portParent);
        }
        queueMessage = new HashMap<>();
    }

    public void sendConfirmationMessage(UUID uuid, IPEndPoint ipEndPoint) throws IOException
    {
        Message<UUID> confirmationMessage = new Message<>(MessageType.Confirmation,
                name, uuid);
        byte[] bufferSend = confirmationMessage.toByteArray();
        DatagramPacket sendMessage = new DatagramPacket(bufferSend, bufferSend.length,
                ipEndPoint.getAddress(), ipEndPoint.getPort());
        socket.send(sendMessage);
    }
    public void sendDataMessageEveryNode(Message message) throws IOException
    {
        queueMessage.put(message.getPersonalUid(), message);
        byte[] analise = message.toByteArray();
        for (IPEndPoint ipEndPoint : child)
        {
        socket.send(new DatagramPacket(analise, analise.length, ipEndPoint.getAddress(), ipEndPoint.getPort()));
        createOrInsert(ipEndPoint, message.getPersonalUid());
        }
        if (ipEndPointParent == null)
        {
            return;
        }
        socket.send(new DatagramPacket(analise, analise.length, ipEndPointParent.getAddress(),
                ipEndPointParent.getPort()));
        createOrInsert(ipEndPointParent, message.getPersonalUid());
    }
    public void sendDataMessageExceptOne(Message message, IPEndPoint sender) throws IOException
    {
        byte[] analise = message.toByteArray();
        queueMessage.put(message.getPersonalUid(), message);

        for (IPEndPoint ipEndPoint : child)
        {
            if(!ipEndPoint.equals(sender))
            {
                socket.send(new DatagramPacket(analise, analise.length, ipEndPoint.getAddress(), ipEndPoint.getPort()));
                createOrInsert(ipEndPoint, message.getPersonalUid());
            }
        }
        if (ipEndPointParent == null)
        {
            return;
        }
        if(!ipEndPointParent.equals(sender))
        {
            socket.send(new DatagramPacket(analise, analise.length, ipEndPointParent.getAddress(),
                    ipEndPointParent.getPort()));
            createOrInsert(ipEndPointParent, message.getPersonalUid());
        }
    }

    public void setConnection(IPEndPoint ipEndPoint) throws IOException
    {
        Message message = new Message(MessageType.ConnectRequest, name);
        queueMessage.put(message.getPersonalUid(), message);
        byte[] analise = message.toByteArray();
        socket.send(new DatagramPacket(analise, analise.length, ipEndPoint.getAddress(),
                ipEndPoint.getPort()));
        createOrInsert(ipEndPoint, message.getPersonalUid());
    }

    public void run()
    {
        Random random = new Random();
        try(BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            DatagramSocket socket = new DatagramSocket(port);)
        {
            this.socket = socket;
            int capacity = 2048;
            socket.setSoTimeout(3000);
            if (ipEndPointParent != null)
                setConnection(ipEndPointParent);

            while (true)
            {
                Date start = new Date(), end = start;
                while(end.getTime() - start.getTime() < 3000)
                {
                    ///////////////////
                    try
                    {
                        //ждать сообщения
                        byte[] bufferReceive = new byte[capacity];
                        DatagramPacket receiveMessage = new DatagramPacket(bufferReceive, capacity);

                        socket.receive(receiveMessage);

                        if (random.nextInt(100) < loss)
                            continue;
                        //обработка в зависимости от типа сообщения

                        Message message = Message.deserializeMessage(receiveMessage.getData(), receiveMessage.getLength());
                        IPEndPoint ipEndPoint = new IPEndPoint(receiveMessage.getAddress(),
                                receiveMessage.getPort());
                        System.out.println(message.getType() + message.getName() + message.getPersonalUid());
                        switch (message.getType())
                        {
                            case ConnectRequest:
                            {
                                //System.out.println(receiveMessage.getAddress().toString() + receiveMessage.getPort());
                                sendConfirmationMessage(message.getPersonalUid(), ipEndPoint);
                                tracker.put(ipEndPoint, new Date());
                                if (!child.contains(ipEndPoint))
                                {
                                    child.add(ipEndPoint);
                                }
                                break;
                            }
                            case Confirmation:
                            {
                            //    System.out.println(receiveMessage.getAddress().toString() + receiveMessage.getPort());
                                tracker.replace(ipEndPoint, new Date());
                                Message<UUID> uuidMessage = (Message<UUID>) message;
                                queueMessage.remove(uuidMessage.getContent());
                                uuidAddress.get(ipEndPoint).remove(uuidMessage.getContent());
                                break;
                            }
                            case ReserveNode:
                            {
                               // System.out.println(receiveMessage.getAddress().toString() + receiveMessage.getPort());
                                tracker.replace(ipEndPoint, new Date());
                                Message<IPEndPoint> ipEndPointMessage = (Message<IPEndPoint>) message;
                                ipEndPointReserveNode = new IPEndPoint(ipEndPointMessage.getContent().getAddress(),
                                        ipEndPointMessage.getContent().getPort());
                                sendConfirmationMessage(message.getPersonalUid(), ipEndPoint);
                                break;
                            }
                            case Data:
                            {
                               // System.out.println(receiveMessage.getAddress().toString() + receiveMessage.getPort());
                                tracker.replace(ipEndPoint, new Date());
                                Message<String> dataMessage = (Message<String>) message;
                                if (!dataUid.contains(dataMessage.getPersonalUid()))
                                {
                                    dataUid.add(dataMessage.getPersonalUid());
                                    System.out.println(dataMessage.getContent());
                                }
                                sendConfirmationMessage(message.getPersonalUid(), new IPEndPoint(receiveMessage.getAddress(),
                                        receiveMessage.getPort()));
                                sendDataMessageExceptOne(dataMessage, ipEndPoint);
                                break;
                            }
                            default:
                            {
                               // System.out.println(receiveMessage.getAddress().toString() + receiveMessage.getPort());
                                tracker.replace(ipEndPoint, new Date());
                                sendConfirmationMessage(message.getPersonalUid(), ipEndPoint);
                            }
                        }
                    }
                    catch (SocketTimeoutException e)
                    {
                    }
                    ///////////////
                    //попытаться отправить
                    while (in.ready())
                    {
                        String data = in.readLine();
                        Message<String> messageData = new Message<>(MessageType.Data, name, data);
                        sendDataMessageEveryNode(messageData);
                    }
                    end = new Date();
                }
                //проверить когда наш ресивер последний раз был в сети
                Date time = new Date();
                List<IPEndPoint> deleteList = new LinkedList<>();
                for (Map.Entry<IPEndPoint, Date> current : tracker.entrySet())
                {
                    if (time.getTime() - current.getValue().getTime() >= timeOutInSecond*1000)
                    {
                        deleteList.add(current.getKey());
                        System.out.println(current.getKey().toString());
                        if(current.getKey().equals(ipEndPointParent))
                        {
                            //перестроить дерево
                            ipEndPointParent = ipEndPointReserveNode;
                            setConnection(ipEndPointReserveNode);
                        }
                        else
                        {
                            //удалить которые сломались
                            child.remove(current.getKey());
                        }
                    }
                }
                for(IPEndPoint ipEndPoint : deleteList)
                {
                    tracker.remove(ipEndPoint);
                }

                //перепослать сообщения
                for (Map.Entry<IPEndPoint, Set<UUID>> current : uuidAddress.entrySet())
                {
                    for(UUID uuid : current.getValue())
                    {
                        byte[] message = queueMessage.get(uuid).toByteArray();
                        socket.send(new DatagramPacket(message, message.length, current.getKey().getAddress(),
                                current.getKey().getPort()));
                    }
                }
                //check
                sendDataMessageEveryNode(new Message(MessageType.Check, name));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }
}
