package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class UDPMulticast
{
    private MulticastSocket multicastSocket;
    private InetAddress group;
    private Set<String> addressSet = new LinkedHashSet<>();

    public UDPMulticast() throws IOException
    {
        group = InetAddress.getByName("224.0.0.1");
        multicastSocket = new MulticastSocket(8080);
    }

    public UDPMulticast(String address) throws IOException
    {
        group = InetAddress.getByName(address);
        multicastSocket = new MulticastSocket(8080);
    }

    public UDPMulticast(String address, int port) throws IOException
    {
        group = InetAddress.getByName(address);
        multicastSocket = new MulticastSocket(port);
    }
    public void sendMessage() throws IOException
    {
        String message = InetAddress.getLocalHost().toString();
        byte[] buf = new byte[message.length()];
        DatagramPacket sendMessage = new DatagramPacket(message.getBytes(), message.length(), group, 8080);
        DatagramPacket receiveMessage = new DatagramPacket(buf, buf.length);

        multicastSocket.joinGroup(group);
        multicastSocket.setSoTimeout(1000);

        while (true)
        {
            multicastSocket.send(sendMessage);

            try
            {
                Date start = new Date(),
                     end;
                do
                {
                    multicastSocket.receive(receiveMessage);
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
            }
            addressSet.clear();
            }
        }
    }
