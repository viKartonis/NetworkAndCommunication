package com.company.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
    private int port;

    public Server(int port)
    {
        this.port = port;
    }

    public void start()
    {
        try(ServerSocket serverSocket = new ServerSocket(port))
        {
            while (true)
            {
               // System.out.println(InetAddress.getLocalHost());
                Socket socket = serverSocket.accept();
                UserThread userThread = new UserThread(socket);
                Thread thread  = new Thread(userThread);
                thread.start();
            }
        }
        catch (IOException ioException)
        {

        }
    }
}