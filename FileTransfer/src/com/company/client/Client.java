package com.company;

import java.io.IOException;
import java.net.Socket;

public class Client
{
    private String path;
    private String hostAddress;
    private int port;

    public Client(String path, String hostAddress, int port)
    {
        this.path = path;
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public void sendFile()
    {
        try(Socket socket = new Socket(hostAddress, port))
        {

        }
        catch (IOException ioException)
        {

        }
    }
}
