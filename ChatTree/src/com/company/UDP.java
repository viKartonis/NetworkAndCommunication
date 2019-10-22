package com.company;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class UDP
{
    private DatagramSocket datagramSocket;
    private InetAddress group;
    private Set<String> addressSet = new LinkedHashSet<>();
    private int port = 8080;

    public UDP() throws IOException
    {
        group = InetAddress.getByName("224.0.0.1");
        datagramSocket = new MulticastSocket(8080);
    }

    public UDP(InetAddress address) throws IOException
    {
        group = address;
        datagramSocket = new MulticastSocket(8080);
    }

    public UDP(InetAddress address, int port) throws IOException
    {
        this.port = port;
        group = address;
        datagramSocket = new MulticastSocket(port);
    }


    public void sendMessage() throws IOException
    {
        String message = InetAddress.getLocalHost().toString();
        byte[] buf = new byte[message.length()];
        DatagramPacket sendMessage = new DatagramPacket(message.getBytes(), message.length(), group, port);
        DatagramPacket receiveMessage = new DatagramPacket(buf, buf.length);


        datagramSocket.setSoTimeout(1000);

        while (true)
        {
            datagramSocket.send(sendMessage);

            try
            {
                Date start = new Date(),
                     end;
                do
                {
                    datagramSocket.receive(receiveMessage);
                    String receivedMessage = new String(receiveMessage.getData());
                    if (!addressSet.contains(receivedMessage))
                    {
                        addressSet.add(receivedMessage);
                        System.out.println("Receive message from " + receivedMessage);
                    }
                    end = new Date();
                } while (end.getTime() - start.getTime() < 3000);
            }
            catch (SocketTimeoutException ignore)
            {
                ignore.printStackTrace();
            }
            addressSet.clear();
        }
    }
}