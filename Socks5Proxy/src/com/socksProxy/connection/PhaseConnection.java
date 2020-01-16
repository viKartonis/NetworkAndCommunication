package com.socksProxy.connection;

import com.socksProxy.ConnectionSelector;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public abstract class PhaseConnection extends Connection
{
	protected SocketChannel channel;
	protected ArrayList<Byte> buffer = new ArrayList<>();

	public PhaseConnection(ConnectionSelector connectionSelector, SocketChannel _channel)
	{
		super(connectionSelector);
		channel = _channel;
	}
}
