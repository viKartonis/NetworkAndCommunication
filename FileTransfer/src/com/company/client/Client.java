package com.company.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;

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
        File file = new File(path);
        try(Socket socket = new Socket(hostAddress, port);
            FileInputStream fileInputStream = new FileInputStream(file))
        {
            byte[] nameFile = file.getName().getBytes();
            int nameSize = nameFile.length;
            OutputStream out = socket.getOutputStream();
            byte[] fileSizeBytes = ByteBuffer.allocate(4).putInt(nameSize).array();
            out.write(fileSizeBytes);
            out.flush();
            out.write(nameFile);
            long dataSize = file.length();
            out.write(ByteBuffer.allocate(8).putLong(dataSize).array());
            byte[] dataBuffer = new byte[2048];
            int readByte;
            while((readByte = fileInputStream.read(dataBuffer)) > 0)
            {
                out.write(dataBuffer, 0, readByte);
            }
        }
        catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
}
