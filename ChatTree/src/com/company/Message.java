package com.company;

import java.io.*;
import java.util.UUID;

public class Message<T extends Serializable> implements Serializable
{
    private MessageType type;
    private String name;
    private UUID personalUid;
    private T content;

    public Message(MessageType type, String name, T content)
    {
        this.type = type;
        this.name = name;
        this.content = content;
        personalUid = UUID.randomUUID();
    }

    public Message(MessageType type, String name)
    {
        this(type, name, null);
    }

    public MessageType getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public UUID getPersonalUid()
    {
        return personalUid;
    }

    public T getContent()
    {
        return content;
    }

    public byte[] toByteArray() throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static Message deserializeMessage(byte[] serializedMessage, int length) throws IOException, ClassNotFoundException
    {
        ObjectInputStream objectInputStream = new ObjectInputStream(
                new ByteArrayInputStream(serializedMessage, 0, length));
        Message restoredFromByte =  (Message) objectInputStream.readObject();
        objectInputStream.close();
        return restoredFromByte;
    }

}
