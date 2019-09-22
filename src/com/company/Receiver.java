package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class Receiver implements Runnable
{
    private MulticastSocket multicastSocket;
    private InetAddress group;
    private Set<String> addressSet = new LinkedHashSet<>();
    private String message;

    public Receiver(String message) throws IOException
    {
        this.message = message;
        group = InetAddress.getByName("224.0.0.1");
        multicastSocket = new MulticastSocket(8080);
    }

    public Receiver(String address, String message) throws IOException
    {
        this.message = message;
        group = InetAddress.getByName(address);
        multicastSocket = new MulticastSocket(8080);
    }

    public Receiver(String address, int port, String message) throws IOException
    {
        this.message = message;
        group = InetAddress.getByName(address);
        multicastSocket = new MulticastSocket(port);
    }

    public void run()
    {
        byte[] buf = new byte[message.length()];
        DatagramPacket receiveMessage = new DatagramPacket(buf, buf.length);

        try
        {
            multicastSocket.joinGroup(group);
            multicastSocket.setSoTimeout(1000);

            while (true)
            {
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
                    } while (end.getTime() - start.getTime() < 1000);
                }
                catch (SocketTimeoutException ignore)
                {
                }
                addressSet.clear();
            }
        }
        catch (IOException ioException)
        {
            System.err.println("Address is not multicast or join error");
        }
    }
}

