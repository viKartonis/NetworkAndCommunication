package com.company.client;

import java.util.Optional;

public class ClientMain
{
    public static void main(String[] args)
    {
        Client client;
        if (args.length < 2)
        {
            System.err.println("Not enough params");
            return;
        }
        else if (args.length == 2)
        {
            client = new Client(args[0], args[1], 8080);
        }
        else
        {
            client = new Client(args[0], args[1], Integer.parseInt(args[2]));
        }
        client.sendFile();
    }
}
