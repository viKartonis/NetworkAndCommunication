package com.game.network;

import me.ippolitov.fit.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UDPManager
{
    private DatagramSocket datagramSocket;
    private MulticastSocket multicastSocket;
    private Map<Long, SnakesProto.GameMessage> queueMessage;
    private Set<IPEndPoint> child;
    private Map<IPEndPoint, Set<Long>> seqAddress;
    private IPEndPoint ipEndPointParent;


    public UDPManager(DatagramSocket datagramSocket, Map<Long, SnakesProto.GameMessage> queueMessage,
                      Set<IPEndPoint> child, Map<IPEndPoint, Set<Long>> seqAddress, IPEndPoint ipEndPointParent)
    {
        this.datagramSocket = datagramSocket;
        this.queueMessage = queueMessage;
        this.child = child;
        this.seqAddress = seqAddress;
        this.ipEndPointParent = ipEndPointParent;
    }


    public void createOrInsert(IPEndPoint ipEndPoint, long seq)
    {
        if (seqAddress.containsKey(ipEndPoint))
        {
            seqAddress.get(ipEndPoint).add(seq);
        }
        else
        {
            Set<Long> setSeq = new HashSet<>();
            setSeq.add(seq);
            seqAddress.put(ipEndPoint, setSeq);
        }
    }


    public void sendConfirmationMessage(long uuid, IPEndPoint ipEndPoint) throws IOException
    {
        SnakesProto.GameMessage.AckMsg confirmationMessage = SnakesProto.GameMessage.AckMsg.newBuilder().build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder().setAck(confirmationMessage).build();
        byte[] bufferSend = gameMessage.toByteArray();
        DatagramPacket sendMessage = new DatagramPacket(bufferSend, bufferSend.length,
                ipEndPoint.getAddress(), ipEndPoint.getPort());
        datagramSocket.send(sendMessage);
    }

    public void sendDataMessageEveryNode(SnakesProto.GameMessage message) throws IOException
    {
        queueMessage.put(message.getMsgSeq(), message);
        byte[] analise = message.toByteArray();
        for (IPEndPoint ipEndPoint : child)
        {
            datagramSocket.send(new DatagramPacket(analise, analise.length, ipEndPoint.getAddress(),
                    ipEndPoint.getPort()));
            createOrInsert(ipEndPoint, message.getMsgSeq());
        }
        if (ipEndPointParent == null)
        {
            return;
        }
        datagramSocket.send(new DatagramPacket(analise, analise.length, ipEndPointParent.getAddress(),
                ipEndPointParent.getPort()));
        createOrInsert(ipEndPointParent, message.getMsgSeq());
    }
    public void sendDataMessageExceptOne(SnakesProto.GameMessage message, IPEndPoint sender) throws IOException
    {
        byte[] analise = message.toByteArray();
        queueMessage.put(message.getMsgSeq(), message);
        for (IPEndPoint ipEndPoint : child)
        {
            if(!ipEndPoint.equals(sender))
            {
                datagramSocket.send(new DatagramPacket(analise, analise.length,
                        ipEndPoint.getAddress(), ipEndPoint.getPort()));
                createOrInsert(ipEndPoint, message.getMsgSeq());
            }
        }
        if (ipEndPointParent == null)
        {
            return;
        }
        if(!ipEndPointParent.equals(sender))
        {
            datagramSocket.send(new DatagramPacket(analise, analise.length, ipEndPointParent.getAddress(),
                    ipEndPointParent.getPort()));
            createOrInsert(ipEndPointParent, message.getMsgSeq());
        }
    }

    public void setConnection(IPEndPoint ipEndPoint, SnakesProto.PlayerType playersType, boolean viewer,
                              String playerName)
            throws IOException
    {
        SnakesProto.GameMessage.JoinMsg message = SnakesProto.GameMessage.JoinMsg.newBuilder()
                .setPlayerType(playersType)
                .setOnlyView(viewer)
                .setName(playerName).build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder().setJoin(message).build();
        queueMessage.put(gameMessage.getMsgSeq(), gameMessage);
        byte[] analise = gameMessage.toByteArray();
        datagramSocket.send(new DatagramPacket(analise, analise.length, ipEndPoint.getAddress(),
                ipEndPoint.getPort()));
        createOrInsert(ipEndPoint, gameMessage.getMsgSeq());
    }

    public void send(SnakesProto.GameMessage message, IPEndPoint ipEndPoint) throws Exception
    {
        datagramSocket.send(new DatagramPacket(message.toByteArray(), message.toByteArray().length,
                ipEndPoint.getAddress(), ipEndPoint.getPort()));
    }
}
