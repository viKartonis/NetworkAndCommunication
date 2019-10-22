package com.company;

import java.io.IOException;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args)
    {
        TreeNode node;
        try
        {
            if (args.length == 3)
            {
                node = new TreeNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            }
            else
            {
                node = new TreeNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                        InetAddress.getByName(args[3]), Integer.parseInt(args[4]));
            }
            node.run();

        }
        catch (IOException ioException)
        {
            System.err.println("Cannot send and receive message to group");
        }
    }
}
