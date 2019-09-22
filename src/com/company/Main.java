package com.company;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Sender sender;
        Receiver receiver;
        try
        {
            if (args.length == 0)
            {
            sender = new Sender();
            receiver = new Receiver(sender.getMessage());
            }
            else if (args.length == 1)
            {
                sender = new Sender(args[0]);
                receiver = new Receiver(args[0], sender.getMessage());
            }
            else
            {
                sender = new Sender(args[0], Integer.parseInt(args[1]));
                receiver = new Receiver(args[0], Integer.parseInt(args[1]), sender.getMessage());
            }
            Thread send = new Thread(sender);
            send.start();
            Thread receive = new Thread(receiver);
            receive.start();
        }
        catch (IOException ioException)
        {
            System.err.println("Cannot send and receive message to group");
        }
    }
}
