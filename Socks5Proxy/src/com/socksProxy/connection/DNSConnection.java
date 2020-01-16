package com.socksProxy.connection;

import com.socksProxy.ConnectionSelector;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DNSConnection extends Connection
{
	private DatagramChannel channel;

	private ByteBuffer buffer = ByteBuffer.allocate(4096);

	private Map<Integer, SecondPhaseConnection> needAck = new HashMap<>();

	private Map<byte[], InetAddress> cachedHosts = new HashMap<>();
	private Map<byte[], Long> delays = new HashMap<>();

	private Map<Integer, Message> messageSendMap = new HashMap<>();
	private Map<Integer, Message> messageWaitMap = new HashMap<>();
	private Map<Integer, byte[]> addressesIds = new HashMap<>();

	private SocketAddress dnsRequestAddress;

	private static DNSConnection instance;

	private int counter = 0;

	public static void createInstance(ConnectionSelector _connectionSelector)
	{
		if (instance == null)
		{
			instance = new DNSConnection(_connectionSelector);
		}
	}

	public static DNSConnection getInstance()
	{
		if (instance == null)
		{
			throw new NullPointerException("DNSConnection is null");
		}
		return instance;
	}

	private DNSConnection(ConnectionSelector _connectionSelector)
	{
		super(_connectionSelector);

		try
		{
			channel = DatagramChannel.open();
			channel.configureBlocking(false);

			ResolverConfig resolverConfig = new ResolverConfig();

			dnsRequestAddress = new InetSocketAddress(InetAddress.getByName("8.8.8.8"), 53);
			channel.connect(dnsRequestAddress);

			//System.out.println("Finish initialization");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private void reregister()
	{
		int opts = 0;

		if (messageSendMap.size() > 0) opts |= SelectionKey.OP_WRITE;
		if (messageWaitMap.size() > 0) opts |= SelectionKey.OP_READ;

		//System.out.println("DNS OPTS CHANGES: " + opts);

		connectionSelector.registerConnection(channel, this, opts);
	}

	public void resolveAddress(byte[] address, SecondPhaseConnection connection) throws org.xbill.DNS.TextParseException, ClosedChannelException
	{
		System.out.println("RESOLVE ADDRESS");
		if (cachedHosts.containsKey(address))
		{
			System.out.println("Cached has address: " + cachedHosts.get(address));
			connection.setIpResolved(true, cachedHosts.get(address));
			return;
		}
		needAck.put(counter, connection);

		System.out.println("Resolving " + new String(address));

		Message message = new Message();
		Header header = message.getHeader();
		header.setOpcode(Opcode.QUERY);
		header.setID(counter);
		header.setRcode(Rcode.NOERROR);
		header.setFlag(Flags.RD);

		String hostAddress = new String(address);
		String newHostAddress = hostAddress + ".";
		//System.out.println("HOST ADDRESS: " + newHostAddress);
		Name name = new Name(newHostAddress);
		Record record;
		try
		{
			record = Record.newRecord(name, Type.A, DClass.IN);
		}
		catch (RelativeNameException e)
		{
			//System.out.println(e.getMessage());
			needAck.remove(counter);
			System.out.println("Cannot create record");
			connection.setIpResolved(false, null);
			return;
		}

		message.addRecord(record, Section.QUESTION);
		System.out.println(message.getHeader().getFlag(Flags.RD));
		messageSendMap.put(counter, message);
		addressesIds.put(counter, address);
		++counter;

		reregister();
	}

	@Override
	public void perform(SelectionKey key) throws IOException
	{

		if (key.isValid() && key.isWritable())
		{
			System.out.println("WRITABLE DNS");
//			System.out.println(messageSendMap.size());
			Map.Entry<Integer, Message> record = messageSendMap.entrySet().iterator().next();
			int send = channel.send(ByteBuffer.wrap(record.getValue().toWire()), dnsRequestAddress);

			if (send > 0)
			{
				System.out.println("SOMETHING HAPPENED");
				messageWaitMap.put(record.getKey(), record.getValue());
				messageSendMap.remove(record.getKey());
				reregister();
			}
		}

		if (key.isValid() && key.isReadable())
		{
			System.out.println("READABLE DNS");
			int read = channel.read(buffer);

			if(read == -1 || read == 0)
			{
				return;
			}
			byte[] resp = new byte[buffer.position()];

			System.arraycopy(buffer.array(), 0, resp, 0, resp.length);
			buffer.clear();
			Message response = new Message(resp);

			int id = response.getHeader().getID();
			if (!needAck.containsKey(id)) return;

			SecondPhaseConnection connection = needAck.get(id);

			//System.out.println("DNS READ FOR CONNECTION: " + (connection == null));
			needAck.remove(id);
			messageSendMap.remove(id);

			Record[] records = response.getSectionArray(Section.ANSWER);

			InetAddress resolverIp = null;

			for (Record record : records)
			{
				if (record.getType() == Type.A)
				{
					try
					{
						resolverIp = InetAddress.getByAddress(record.rdataToWireCanonical());
					}
					catch (UnknownHostException ignored)
					{
						continue;
					}
					break;
				}
			}

			if (resolverIp != null)
			{

				cachedHosts.put(addressesIds.remove(id), resolverIp);

				System.out.println("IP RESOLVED: " + resolverIp);
				connection.setIpResolved(true, resolverIp);
			} else
			{
				System.out.println("IP NOT RESOLVED");
				connection.setIpResolved(false, null);
			}
		}

		reregister();

	}

	@Override
	public void terminate(SelectionKey key)
	{
		super.terminate(key);
		connectionSelector.shutdown();
	}

	@Override
	public void terminate()
	{
		connectionSelector.deleteConnection(channel);
		try
		{
			channel.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}