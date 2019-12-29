//package com.game.network;
//
//import com.game.model.GameField;
//import me.ippolitov.fit.snakes.SnakesProto;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.InetAddress;
//import java.net.MulticastSocket;
//
//public class UDPMulticast
//{
//    private MulticastSocket multicastSocket;
//    private InetAddress group;
//    private UDPUnicast udpUnicast;
//    private GameField field;
//    private int port;
//
//    public UDPMulticast() throws IOException
//    {
//        group = InetAddress.getByName("239.192.0.4");
//        multicastSocket = new MulticastSocket(9192);
//        udpUnicast = new UDPUnicast(group.toString(), 9192);
//    }
//
//    public UDPMulticast(String address) throws IOException
//    {
//        group = InetAddress.getByName(address);
//        multicastSocket = new MulticastSocket(9192);
//        udpUnicast = new UDPUnicast(address, 9192);
//    }
//
//    public UDPMulticast(String address, int port) throws IOException
//    {
//        this.port = port;
//        group = InetAddress.getByName(address);
//        multicastSocket = new MulticastSocket(port);
//        udpUnicast = new UDPUnicast(address, port);
//    }
//
//
//    public byte[] getMessage()
//    {
//        ////ack в ресиве
//        ///ping в main
//        ////steer в логике
//        ///state в контроллере(переделать данную функцию на 2 send-receive)
//        ///acconnect in main
//        ////JoinMsg in controller
//        ///ErrorMsg in controller
//        ///RoleChangeMsg in udp
//
//
////        SnakesProto.GameMessage.Builder gameMessage = SnakesProto.GameMessage.newBuilder();
////        SnakesProto.GameMessage.AckMsg ackMsg =
////        gameMessage.setAck();
//        return null;
//    }
//
//
//    public void sendMessage() throws IOException
//    {
//        SnakesProto.GameMessage.AnnouncementMsg announcementMessage = SnakesProto.GameMessage
//                .AnnouncementMsg.newBuilder().setPlayers().build();
//
//        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
//                .setRoleChange(deputyMessage).build();
//
//        DatagramPacket sendMessage = new DatagramPacket(message, message.length(), group, );
//
//            multicastSocket.send(sendMessage);
//    }
//}
