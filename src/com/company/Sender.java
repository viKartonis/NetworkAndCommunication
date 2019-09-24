package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Sender implements Runnable
{
    private MulticastSocket udpSender;
    private InetAddress group;
    private String message = InetAddress.getLocalHost().toString();
    private int port = 8080;

    public Sender() throws IOException
    {
        group = InetAddress.getByName("224.0.0.1");
        udpSender = new MulticastSocket();
    }

    public Sender(String address) throws IOException
    {
        group = InetAddress.getByName(address);
        udpSender = new MulticastSocket();
    }

    public Sender(String address, int port) throws IOException
    {
        this.port = port;
        group = InetAddress.getByName(address);
        udpSender = new MulticastSocket(port);
    }

    @Override
    public void run()
    {
        try
        {
            DatagramPacket sendMessage = new DatagramPacket(message.getBytes(), message.length(), group, port);

            udpSender.joinGroup(group);
            while (true)
            {
                udpSender.send(sendMessage);
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException interrruptedException)
                {
                    System.err.println("The thread has been interrupted");
                }
            }
        }
        catch (IOException ioException)
        {
            System.out.println("Cannot get local host");
        }
    }

    public String getMessage()
    {
        return message;
    }
}
