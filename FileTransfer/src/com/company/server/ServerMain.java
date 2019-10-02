package com.company.server;

public class ServerMain
{
    public static void main(String[] args)
    {
        Server server;
        if (args.length == 0)
        {
            server = new Server(8080);
        }
        else
        {
            server = new Server(Integer.parseInt(args[0]));
        }
        server.start();
    }
}