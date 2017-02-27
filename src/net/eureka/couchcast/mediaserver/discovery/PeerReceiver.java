package net.eureka.couchcast.mediaserver.discovery;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import net.eureka.couchcast.Start;
import net.eureka.couchcast.foundation.init.NetworkGlobals;
import net.eureka.couchcast.foundation.logging.Logger;
import net.eureka.couchcast.gui.lang.LanguageDelegator;
import net.eureka.couchcast.gui.lang.Languages;

/**
 * This is the Peer Discovery class. It is called PeerReceiver because it is more client than server. In this case
 * of UPnP UDP, the multicast socket binds to the UPnP port(1900) then joins the multicast group address(239.255.255.250).
 * From here, the socket listens for specific discovery packets that are sent from the Android client to the modem then to each
 * computer. Once a discovery packet has been received and verified, the same discovery packet is filled with the server name 
 * in UTF-8 byte form and sent to the android client which is listening at port(1901).
 * Here is a simple diagram of the process.
 * <br>
 * <pre>
 * 	
 *F************************************************************************|/-----[DISCOVERY PACKET]-------->(MEDIA SERVER 1900)----| 
 *F************************************************************************|/*******************************************************|
 *F*(ANDROID CLIENT)----[DISCOVERY PACKET]----->(MODEM 239.255.255.250:1900)------[DISCOVERY PACKET]-------->(MEDIA SERVER 1900)----| 
 *F********|***************************************************************|\*******************************************************|
 *F********|***************************************************************|\-----[DISCOVERY PACKET]-------->(MEDIA SERVER 1900)----|
 *F********|************************************************************************************************************************|
 *F********|************************************************************************************************************************|
 *F********=-------[1901]-------------------[DISCOVERY PACKET WITH SERVER NAME]-----------------------------------------------------=
 * 
 * </pre>
 * 
 * If the connection to the modem interface has been lost, the thread will wait in a loop until the interface is up again. Once it is 
 * up, the PeerReceiver calls InitialiseNetwork.restartPeerDiscovery so that the socket will be rejoined properly. 
 * 
 * @author Owen McMonagle

 * @see NetworkGlobals
 */
public final class PeerReceiver extends Thread
{
	/**
	 * UPnP UDP discovery port.
	 */
	private static final int PEER_DISCOVERY_PORT = 1900;
	
	/**
	 * UDP port from which the client will be listening on for a response.
	 */
	private static final int PEER_RECEIVER_PORT = 1901;
			
	/**
	 * Time in milliseconds until read timeout. 
	 */
	private static final int BLOCK_TIMEOUT = 5000;
	
	/**
	 * List of verified packets to respond to. Emptied after each mass packet response.
	 */
	private static ArrayList<DatagramPacket> packets = new ArrayList<DatagramPacket>();
	
	/**
	 * Registered Multicast socket. 
	 */
	private static MulticastSocket socket = null;
	
	/**
	 *	Used to indicate when to append to log file at server initialisation.
	 */
	private static boolean firstTime = false;
	
	/**
	 * Used to know when to disconnect socket.
	 */
	private static boolean disconnect = false;
	
	/**
	 * 	Multicast Broadcast Address. Used to join to multicast group on the modem interface.
	 */
	private static InetAddress multicastAddress = null;
	
	/**
	 * The Only Constructor. Resets disconnect flag, then proceeds to set thread name to "Discovery" and Initialises the multicast address.
	 * After that the multicast socket is Initialised and binded to port(1900), the network interface is set to LocalHost so that the DHCP
	 * address will be chosen. Finally the socket joins the multicast broadcast address(239.255.255.250), sets the read timeout to five seconds
	 * and starts the thread.
	 */
	public PeerReceiver()
	{
		try
		{
			// Reset disconnect flag.
			disconnect = false;
			// Set thread name.
			this.setName("Discovery");
			// Initialise broadcast address.
			multicastAddress = InetAddress.getByName("239.255.255.250");
			// Initialise multicast socket and bind to port(1900).
			socket = new MulticastSocket(PEER_DISCOVERY_PORT);
			// Set network interface to Local Host.
			socket.setNetworkInterface(NetworkInterface.getByInetAddress(NetworkGlobals.getDhcpNetwork()));
			// Join multicast broadcast address.
			socket.joinGroup(multicastAddress);
			// Set read timeout to 5000 milliseconds.
			socket.setSoTimeout(BLOCK_TIMEOUT);
			// Start Peer Discovery thread.
			this.start();
		}
		catch (IOException e)
		{
			String error_message = "ERROR: Setting up Peer Discovery.\n"+e.getLocalizedMessage(); 
			System.err.println(error_message);
			Logger.append(new StringBuffer(error_message));
		}
	}
	
	/**
	 * Thread starts here. Attempts to write to log if first time initialised. A loop is in place
	 * until disconnect equals True or the thread is interrupted. This loop waits for UPnP packets,
	 * once received and verified the packets are sent back with the server name attached.
	 * 
	 * If the network interface goes down, then it will trigger networkFailureRestartProcedure which 
	 * will wait until the Interface is back up then will restart the PeerReceiver by calling Initialise
	 * Network.restartPeerDiscovery.
	 */
	public void run() 
	{
		// Attempt to write to log.
		writeLogMessage();
		// Loop while disconnect and isInterrupted equals false.
		while(!disconnect && !this.isInterrupted())
		{
			// If socket if not closed, network is up and disconnect equal false then...
			if(!socket.isClosed() && isNetworkUp() && !disconnect)
			{
				// receive a UPnP UDP packet.
				receive();
				// respond to any valid packets.
				respond();
			}
			// If the network interface is not up...
			else if(!isNetworkUp())
				// enter network restart procedure.
				networkFailureRestartProcedure();
		}
	}
	
	/**
	 * Called when the network interface goes down. Keeps waiting until the network is enabled again.
	 * Once enabled the socket is closed, thread interrupted and InitialiseNetwork.restartPeerDiscovery
	 * is called so that Peer Discovery is set up again.
	 */
	private void networkFailureRestartProcedure()
	{
		// Print waiting to console.
		System.out.println("Waiting");
		// Loops for one second until the network is up and potentially reachable.
		while(!isNetworkUp())
			waitOneSecond();

		// Wait for ten seconds for interface to come back online.
		System.out.print("Interface restarting in ten seconds.");
		for( int i = 0; i < 10; i++)
		{
				System.out.print(".");
				waitOneSecond();
		}
		
		// Checks if the socket is open..
		if(!socket.isClosed())
			// if so, closes the socket.
			socket.close();
		// Set disconnect to true 
		disconnect = true;
		// Signal for Peer Discovery restart.
		Start.restartPeerDiscovery();
		// Interrupt this thread.
		try 
		{
			this.interrupt();
		} 
		catch (Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Instructs the thread to wait for one second.
	 */
	private void waitOneSecond()
	{
		synchronized (this)
		{
			try 
			{
				this.wait(1000);
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks if the sockets network interface is up.
	 * @return Boolean - True if network interface is up, false otherwise.
	 */
	private static boolean isNetworkUp()
	{
		try 
		{
			return socket.getNetworkInterface().isUp();
		}
		catch (SocketException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Attempts to receive packet from socket. If packet has been received, it is then sent to be verified.
	 *  
	 * Reading is blocked for five seconds before proceeding. (This does not apply if the packet has been received).
	 */
	private static void receive()
	{
		// Initialize packet buffer.
		byte[] buffer = new byte[210];
		// Create container packet for receiving content.
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try 
		{
			// Attempt to receive packet from socket.
			socket.receive(packet);
			// Attempt to verify received packet.
			verifyReceivedPacket(packet);
		} 
		catch (SocketTimeoutException e)
		{
			// SAFE TO IGNORE exception as timeout happens every five seconds.
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Validates the passed parameter(packet) by checking if it isn't null and if the data within the received packet is
	 * valid. If these are true, the packet is added to a list waiting to be responded to.
	 * @param packet - Received packet to validate.
	 */
	private static void verifyReceivedPacket(DatagramPacket packet)
	{
		// Check if packet is not equal to null and if packet contents are verified... 
		if(packet != null && verifyParsedReceivedData(new String(packet.getData())))
			// if so add to response list.
			packets.add(packet);
	}
	
	/**
	 * Takes received packet data sent by client and parses it. Once parsed, the data is sent to be verified. Verification
	 * depends upon the SSDP M-Search, Search Target (ST) equalling "|media-server|" and Host (HOST) equalling "|media-client|".
	 * <pre>
	 *    VALID SSDP M-SEARCH DATA STRUCTURE
	 *    
	 * 		"M-SEARCH * HTTP/1.1 \r\n" +
	 * 		"ST: |media-server| \r\n" +
	 * 		"MX: 1\r\n" +
	 * 		"MAN: ssdp:discovery \r\n" +
	 * 		"HOST: |media-client| \r\n\r\n";
	 * </pre>
	 * 
	 * @param String built_data_array - Received packet data from modem that could be a media client. 
	 * @return Boolean - True if packet data is valid, false otherwise.
	 */
	private static boolean verifyParsedReceivedData(String built_data_array)
	{
		// Attempt to parse received packet data.
		String[] split = built_data_array.split("\\|");
		try
		{
			// Validate parsed packet data.
			return isSplitDataValid(split);
		} 
		catch (Exception e) 
		{
			
		}
		// If exception occurs, return false.
		return false;
	}
	
	/**
	 * Takes received parsed data and verifies it. Listed beside each M-Search query below are indexes ([#]),
	 * from which the data was split.
	 * The data is verified by taking index [1] "media-server" and index [3] "media-client" and making
	 * sure they equal those values.
	 * 
	 * <pre>
	 *    VALID SSDP M-SEARCH DATA STRUCTURE
	 *    
	 * 		[0]"M-SEARCH * HTTP/1.1 \r\n" +
	 * 		"ST: |[1]media-server|[2] \r\n" +
	 * 		"MX: 1\r\n" +
	 * 		"MAN: ssdp:discovery \r\n" +
	 * 		"HOST: |[3]media-client|[4] \r\n\r\n";
	 * </pre>
	 * 
	 * 
	 * @param String split_data - Received split packet data to be validated.
	 * @return Boolean - True if index 1 and index 3 both equals "media-server" and "media-client" respectively.
	 * @throws Exception - Most likely array index exception, indicating that data is not valid.
	 */
	private static boolean isSplitDataValid(String[] split_data) throws Exception
	{
		// Define constant strings for the split data to be checked against.
		final String client_check = new String("media-client"), server_check = new String("media-server"); 
		
		// Verify server check (index 1) and client check (index 3) both equals "media-server" and "media-client"
		// respectively.
		if(server_check.equals(split_data[1]) && client_check.equals(split_data[3]))
			// Returns true if valid.
			return true;
		// Returns false if invalid.
		return false;
	}
	
	/**
	 * Responds to all validated packets added to the packets ArrayList above. This allows the media client to find
	 * the server for TCP/IP connections as well as the server name.
	 */
	private static void respond()
	{
		// Iterate through each validated packet...
		for(DatagramPacket packet : packets)
			// if packet not equal null...
			if(packet != null)
				// send packet to client.
				send(packet);
		
		// If any packets were received...
		if(packets.size() > 0)
			// Remove all packets.
			packets.clear();
	}
	
	/**
	 * Sends a validated packet back to where it came from. The address is located on the passed parameter packet.
	 * The server name is retrieved from NetworkGlobals so it can be placed on the packet being sent to the client.
	 * This enables the client to find the server for further TCP/IP communication and to find out the server name before
	 * authentication. 
	 * @param DatagramPacket to_send - Validated packet to send back to where it came from with server name attached. 
	 */
	private static void send(DatagramPacket to_send)
	{
		// Retrieve the server name.
		final String name_of_server = NetworkGlobals.getServerName().toString().trim();
		try 
		{
			// Sends datagram packet back to client.
			send(name_of_server.getBytes("UTF-8"), to_send.getAddress());
		} 
		catch (UnsupportedEncodingException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	/**
	 * Creates a new Datagram Packet to be sent with the passed byte array and InetAddress. Uses the Peer receiver port (1901).
	 * @param byte[] data - Data to be placed in packet.
	 * @param InetAddress address - Address of the client to send to. 
	 */
	private static void send(byte[] data, InetAddress address)
	{
		// Construct new datagram packet with passed data, address and peer receiver port.
		DatagramPacket name_packet = new DatagramPacket(data, data.length,address, PEER_RECEIVER_PORT);
		try
		{
			// Send packet through Datagram Socket to client.
			socket.send(name_packet);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes first time initialization to log. Only writes to log on server start up.
	 */
	private static void writeLogMessage()
	{
		// If first time start up...
		if(!firstTime)
		{
			// Write start up to log with specific language and port number.
			Logger.append(new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.PORT_NUMBER)+Integer.toString(PEER_DISCOVERY_PORT)));
			// Set first time written to True.
			firstTime = true;
		}
	}
}
