package com.company;

import java.io.*;
import java.net.InetAddress;

public class IPEndPoint implements Serializable
{
    private InetAddress address;
    private int port;

    public IPEndPoint(InetAddress address, int port)
    {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress()
    {
        return address;
    }

    public int getPort()
    {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!(o instanceof IPEndPoint)) return false;
        IPEndPoint another = (IPEndPoint) o;
        if (!address.equals(another.address)) return false;
        if (port != another.port) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return address.hashCode() + port;
    }
}

