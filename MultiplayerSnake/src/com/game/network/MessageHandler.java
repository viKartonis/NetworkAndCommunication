package com.game.network;

import me.ippolitov.fit.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;

import java.util.Date;

public class MessageHandler
{
    private SnakesProto.GameMessage message;
    private UDPUnicast udpUnicast;
    private UDPManager udpManager;
    private IPEndPoint master;


    public MessageHandler(UDPUnicast udpUnicast)
    {
        this.udpUnicast = udpUnicast;
        message = udpUnicast.receiveMessage();
        master = udpUnicast.getMaster();
        udpManager = new UDPManager(udpUnicast.getDatagramSocket(), udpUnicast.getQueueMessage(),
                udpUnicast.getChild(), udpUnicast.getSeqAddress(), master);
    }

    public void handle()
    {
//        switch (message.getTypeCase())
//        {
//        case JOIN:
//        {
////            try
////            {
////                udpManager.sendConfirmationMessage(message.getMsgSeq(), master);
////            }
////            catch (IOException e)
////            {
////                e.printStackTrace();
////            }
////            tracker.put(masterIp, new Date());
////            if (!child.contains(masterIp))
////            {
////                child.add(masterIp);
////            }
////
////            if (ipEndPointParent != null)
////            {
//////                                    Message<IPEndPoint> reserveNode = new Message<>(MessageType.ReserveNode, name,
//////                                            ipEndPointParent);
////                SnakesProto.GameMessage.RoleChangeMsg deputyMessage = SnakesProto.GameMessage
////                        .RoleChangeMsg.newBuilder()
////                        .setSenderRole(SnakesProto.NodeRole.MASTER)
////                        .setReceiverRole(SnakesProto.NodeRole.DEPUTY).build();
////
////                SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
////                        .setRoleChange(deputyMessage).build();
////
////                socket.send(new DatagramPacket(gameMessage.toByteArray(),
////                        gameMessage.toByteArray().length, receiveMessage.getAddress(),
////                        receiveMessage.getPort()));                       //отправить резервную ноду
////                queueMessage.put(gameMessage.getMsgSeq(), gameMessage);
////                udpManager.createOrInsert(masterIp, gameMessage.getMsgSeq());
////
////                /////отправляю состаяние поля
////
////
////                SnakesProto.GameState gameStateMessage = SnakesProto.GameState
////                        .newBuilder()
////                        .setStateOrder(state)
////                        .addSnakes()
////                        .build();
////
////
////                SnakesProto.GameMessage.StateMsg stateMessage = SnakesProto.GameMessage
////                        .StateMsg.newBuilder()
////                        .setState().build();
////
////                SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
////                        .setState(stateMessage).build();
//
//            }
//
//            break;
//        }
//        case ACK:
//        {
//            if (tracker.isEmpty())
//            {
//                tracker.put(masterIp, new Date());
//            }
//            else
//            {
//                tracker.replace(masterIp, new Date());
//            }
//            //Message<UUID> uuidMessage = (Message<UUID>) message;
//
//            if (seqAddress.get(masterIp) != null)
//                seqAddress.get(masterIp).remove(message.getMsgSeq());
//            break;
//        }
//        case ROLE_CHANGE:
//        {
//            System.out.println("ROLE CHANGE " + receiveMessage.getAddress().toString()
//                    + receiveMessage.getPort());
//            tracker.replace(masterIp, new Date());
//            //Message<IPEndPoint> ipEndPointMessage = (Message<IPEndPoint>) message;
//
//            //if (ipEndPointMessage.getContent() != null)
//            //{
//            ipEndPointReserveNode = new IPEndPoint(receiveMessage.getAddress(),
//                    receiveMessage.getPort());
//            //}
//
//
//            udpManager.sendConfirmationMessage(message.getMsgSeq(), masterIp);
//            break;
//        }
//        case STATE:
//        {
//            System.out.println("STATE " + receiveMessage.getAddress().toString() +
//                    receiveMessage.getPort());
//            tracker.replace(masterIp, new Date());
//            //Message<String> dataMessage = (Message<String>) message;
//
//            if (!dataUid.contains(message.getMsgSeq()))
//            {
//                dataUid.add(message.getMsgSeq());
//            }
//            udpManager.sendConfirmationMessage(message.getMsgSeq(),
//                    new IPEndPoint(receiveMessage.getAddress(),
//                            receiveMessage.getPort()));
//            udpManager.sendDataMessageExceptOne(message, masterIp);
//            break;
//        }
//        case PING:
//        {
//            tracker.replace(masterIp, new Date());
//            udpManager.sendConfirmationMessage(message.getMsgSeq(), masterIp);
//        }
//        case ERROR:
//        {
//            udpManager.sendConfirmationMessage(message.getMsgSeq(), masterIp);
//        }
//        case STEER:
//        {
//            ///изменяем модель
//            SnakesProto.Direction direction = message.getSteer().getDirection();
//            ///отправляем сообщение о поле всем кроме одного
//            //отрисовываем
//        }
//    }
//}
    }
}
