package com.company.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Date;

public class UserThread implements Runnable
{
    private Socket socket;

    public UserThread(Socket socket)
    {
        this.socket = socket;
    }

    public void run()
    {
        try
        {
            byte[] name = new byte[4];
            InputStream in = socket.getInputStream();
            socket.setSoTimeout(3000);
            byte[] data = new byte[8];

            in.read(name);
            int nameSize = ByteBuffer.wrap(name).getInt();
            name = new byte[nameSize];

            in.read(name, 0, nameSize);

            String nameFile = new String(name);
            File file = new File(nameFile);
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            in.read(data);
            long dataSize = ByteBuffer.wrap(data).getLong();
            data = new byte[2048];

            int writeByte;
            Date start = new Date();
            Date end;
            int tmp = 0;

            while ((writeByte = in.read(data)) > 0)
            {
                tmp += writeByte;
                end = new Date();
                if (end.getTime() - start.getTime() > 3000)
                {
                    System.out.println("instant speed " + writeByte/(end.getTime() - start.getTime()));
                    System.out.println("Average speed " + tmp/(end.getTime() - start.getTime()));
                }
                fileOutputStream.write(data, 0, writeByte);
            }
            if (tmp == dataSize)
                System.out.println("All file was received");
        }
        catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }
}

