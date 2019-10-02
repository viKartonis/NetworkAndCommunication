package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.*;

public class Receiver implements Runnable
{
    private InetAddress group;
    private int port = 8080;

    public Receiver() throws IOException
    {
        group = InetAddress.getByName("224.0.0.1");
    }

    public Receiver(String address) throws IOException
    {
        group = InetAddress.getByName(address);
    }

    public Receiver(String address, int port) throws IOException
    {
        this.port = port;
        group = InetAddress.getByName(address);
    }

    public void run()
    {
        long timeToLive = 5000;
        byte[] buf = new byte[128];
        DatagramPacket receiveMessage = new DatagramPacket(buf, buf.length);
        Map<InetAddress, Long> knownCopies = new HashMap<>();
        List<InetAddress> keysForDelete = new LinkedList<>();

        Map<InetAddress, Integer> copies = new HashMap<>();
        try (MulticastSocket multicastSocket = new MulticastSocket(port))
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
                        end = new Date();
                        knownCopies.put(receiveMessage.getAddress(), end.getTime());
                        System.out.println("Receive message from " + new String(receiveMessage.getData()).trim());
                    } while (end.getTime() - start.getTime() < 1000);
                }
                catch (SocketTimeoutException ignore) {}
                Date currentTime = new Date();
                System.out.println("++++++++++++++++++++++++++");
                knownCopies.forEach((ia, l) -> {
                    long delta = currentTime.getTime() - l;
                    System.out.println(ia + " " + delta / 1000L + " seconds ago");
                    if (delta > timeToLive)
                        keysForDelete.add(ia);
                });
                System.out.println("--------------------------");
                keysForDelete.forEach(knownCopies::remove);
                keysForDelete.clear();
            }
        }
        catch (IOException ioException)
        {
            System.err.println("Address is not multicast or join error");
        }
    }
    public int getPort(){return port;}
}

