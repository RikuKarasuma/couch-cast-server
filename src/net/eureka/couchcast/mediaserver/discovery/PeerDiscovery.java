package net.eureka.couchcast.mediaserver.discovery;



import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import net.eureka.network.udp.PacketIdentifier;

/** MODIFIED FOR SERVER USE.
 * 
 * Sends discovery UPnP packets to the modem and waits two seconds for any responses. Any responses are
 * collected and used to populate the {@link ConnectionDelegate}, which is responsible for most of the
 * client/server network interactions. For example the packets collected here are used to create the
 * {@link net.eureka.androidcast.design.connection.list.ConnectionController.ConnectionList} located within
 * the {@link net.eureka.androidcast.design.connection.list.ConnectionController}.
 *
 * @author Owen McMonagle.
 */
public final class PeerDiscovery extends Thread
{
	public static void main(String[] args)
	{
		PeerDiscovery discoverer = new PeerDiscovery();
		for(String name : discoverer.getNames())
			System.out.println(name);
	}
	 
	/**
	 * Read block timeout.
	 */
	private static final int BLOCK_TIMEOUT = 100;

	/**
	 * Search timeout.
	 */
	private static final int REFRESH_TIMEOUT = 10000;

	/**
	 * Port to wait for response packets on.
	 */
	private static final int DISCOVERY_PORT = 1901;

	/**
	 * Search payload data. Required for the server to recognise the media client.
	 */
	private final static String DISCOVER_MESSAGE_ROOTDEVICE =
																"M-SEARCH * HTTP/1.1 \r\n" +
																"ST: |media-server| \r\n" +
																"MX: 1\r\n" +
																"MAN: ssdp:discovery \r\n" +
																"HOST: |media-client| \r\n\r\n";

	/**
	 * List to contain all the received packets server.
	 */
	private ArrayList<DatagramPacket> packets = new ArrayList<DatagramPacket>();

	/**
	 * List to contain all received server name.
	 */
	private ArrayList<String> names = new ArrayList<>();

	/**
	 * UDP multicast socket.
	 */
	private MulticastSocket socket = null;

	/**
	 * Used to indicate if the search has timed out.
	 */
	private boolean isTimeout = false;

	/**
	 * Timer used to stop the search, once time has run out.
	 */
	private Timer DISCOVERY_TIMER = new Timer("Peer Discovery Timer.");

	/**
	 * Creates a new multicast socket to listen on. Then joins the UPnP
	 * broadcast group at (239.255.255.250). Finally the search thread is
	 * started.
	 */
	public PeerDiscovery()
	{
		isTimeout = false;
		try
		{
			socket = new MulticastSocket(DISCOVERY_PORT);
			socket.joinGroup(InetAddress.getByName("239.255.255.250"));
			socket.setSoTimeout(BLOCK_TIMEOUT);
			//socket.setBroadcast(true);
			this.start();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends discovery packet, initializes the search timeout, listens for packets then
	 * attempts to add those packets and address names.
	 */
	public void run()
	{
		this.sendDiscoveryPacket();
		this.initialiseTimeout();
		this.performSearch();
		getAddresses();
	}

	/**
	 * Schedules the search timeout.
	 */
	private void initialiseTimeout()
	{
		if(DISCOVERY_TIMER == null)
			DISCOVERY_TIMER = new Timer();
		DISCOVERY_TIMER.schedule(getTimeoutTask(), REFRESH_TIMEOUT);
	}

	/**
	 * Sends a discovery packet to the modem at the broadcast address on port 1900.
	 */
	private void sendDiscoveryPacket()
	{
		byte[] encoded_buffer = DISCOVER_MESSAGE_ROOTDEVICE.getBytes();
		DatagramPacket initialising_packet = null;
		try
		{
			initialising_packet = new DatagramPacket(encoded_buffer, encoded_buffer.length, InetAddress.getByName("239.255.255.250"), DISCOVERY_PORT-1);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		send(initialising_packet);
	}

	/**
	 * Waits for response packets. If any are received, the name and packet are stored.
	 */
	private void receive()
	{
		byte[] buffer = new byte[40];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try
		{
			socket.receive(packet);
			packets.add(packet);
			names.add(new String(packet.getData(), "UTF-8"));
		}
		catch (SocketTimeoutException e)
		{
			System.out.println("No data yet.");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends a packet.
	 * @param to_send - packet to send.
	 */
	private void send(DatagramPacket to_send)
	{
		try
		{
			socket.send(to_send);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to receive UPnP packets until the search is over.
	 */
	private void performSearch()
	{
		while(!isTimeout)
			receive();
	}

	/**
	 * Retrieves the task that will stop the search once time has run out.
	 * @return
	 */
	private TimerTask getTimeoutTask()
	{
		return new TimerTask()
		{
			@Override
			public final void run()
			{
				isTimeout = true;
				DISCOVERY_TIMER.purge();
			}
		};
	}

	/**
	 * Retrieves the received addresses as an Array of packet identifiers.
	 * @return Array of packet identifiers.
	 */
	private PacketIdentifier[] getAddresses()
	{
		PacketIdentifier[] address_identifiers = new PacketIdentifier[packets.size()];
		for (int i = 0; i < packets.size(); i++)
		{
			DatagramPacket packet = packets.get(i);
			PacketIdentifier identifier = new PacketIdentifier(packet.getAddress(), packet.getPort(), packet.getLength());
			address_identifiers[i] = identifier;
		}
		return address_identifiers;
	}
	
	public ArrayList<String> getNames()
	{
		return names;
	}
}
