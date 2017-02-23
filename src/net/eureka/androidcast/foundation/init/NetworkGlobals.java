package net.eureka.androidcast.foundation.init;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

import net.eureka.androidcast.foundation.config.Configuration;

/**
 * Abstract class so it can't be Instantiated. NetworkGlobals is a storage object for all network variables common to the
 * whole server. It contains the default server name as well as the modified one that can be chosen in by the user. A list of
 * verified clients is also located here. That list is used by every major socket component around the server for verification
 * of incoming connections. This is done by using the compareAddress(InetAddress compare_address) method below.
 * 
 * @author Owen McMonagle.
 */
public final class NetworkGlobals 
{
	
	/**
	 * Default server name. Used by InitialiseNetwork at the start of the server to set at name with the modified MAC address.
	 */
	private static final String DEFAULT_SERVER_NAME = ApplicationGlobals.getName().toString();
	
	/**
	 * List of all the verified clients connected to the server. Most components have only been designed for one active connection
	 * at a time. Although a lot of the ground work for multiple client connections has been laid, so an upgrade in the future is more
	 * easily achievable.   
	 */
	private static final ArrayList<InetAddress> CLIENTS = new ArrayList<InetAddress>();
	
	private static InetAddress dhcpNetwork = null;
	
	private static String dhcpNetworkName = "";
	
	/**
	 * Custom server name. The user can change this. It is also used by the default server name if no user has set a name.
	 */
	private static String name = null;
	
	/**
	 * Adds a client to the verified clients list provided the address is not null and has not been added before.
	 * Verification of clients is handled by PasswordReceiver.
	 * 
	 * @param InetAddress client_address - InetAddress of the client to add to the verified clients list.
	 */
	public static void addClientAddress(InetAddress client_address)
	{
		// If client address is not null and address has not already been added...
		if(client_address != null && !compareAddress(client_address))
			// add client address to verified clients list.
			CLIENTS.add(client_address);
	}
	
	/**
	 * Sets the new server name if the passed StringBuffer is not null. Otherwise sets the default server name without MAC.
	 * @param String new_server_name - New server name.
	 */
	public static void setServerName(final String new_server_name)
	{
		// If new server name is not null...
		if(new_server_name != null)
			// name of the server is changed to the new server name.
			name = new_server_name;
		// Else if the server name is null...
		else
			// Add default server name without modified MAC.
			name = DEFAULT_SERVER_NAME;
	}
	
	/**
	 * Retrieves the server name.
	 * 
	 * @return String - Name of the server.
	 */
	public static String getServerName()
	{
		return name;
	}
	
	/**
	 * Retrieves the default server name.
	 * 
	 * @return String - Default name of the server.
	 */
	public static String getDefaultServerName()
	{
		return DEFAULT_SERVER_NAME;
	}
	
	/**
	 * Attempts to compare an InetAddress to the entire verified clients list. Returns true if client has been added
	 * to the address already.
	 * 
	 * Used to verify incoming client connections from most major server sockets.
	 * 
	 * @param InetAddress compare_address - Address of client to check/verify. 
	 * @return Boolean - True if client address is already on verified list, false otherwise.
	 */
	public static boolean compareAddress(InetAddress compare_address)
	{
		// Iterate through verified client addresses...
		for(InetAddress client_address : CLIENTS)
			// If the address to compare matches a client address...
			if(compareTwoAddresses(compare_address.getAddress(), client_address.getAddress()))
				// Return true.
				return true;
		
		// Returns false if client is not on the list.
		return false;
	}
	
	/**
	 * Compares two InetAddress objects by their IP. Returns true if matched.
	 * 
	 * @param byte[] compare_address_0 - Address to compare one.
	 * @param byte[] compare_address_1 - Address to compare two.
	 * @return Boolean - True if addresses match, false otherwise.
	 */
	private static boolean compareTwoAddresses(byte[] compare_address_0, byte[] compare_address_1)
	{	
		// Compare both IPs. True if matched.
		return Arrays.equals(compare_address_0, compare_address_1);
	}

	public static InetAddress getDhcpNetwork() 
	{
		return dhcpNetwork;
	}
	
	public static void setDhcpNetwork(InetAddress dhcp_network, String network_name)
	{
		NetworkGlobals.dhcpNetwork = dhcp_network;
		NetworkGlobals.dhcpNetworkName = network_name;
		new Configuration(true);
	}

	public static void setDhcpNetwork(InetAddress dhcp_network) 
	{
		NetworkGlobals.dhcpNetwork = dhcp_network;
	}

	public static String getDhcpNetworkName()
	{
		return dhcpNetworkName;
	}

	public static void setDhcpNetworkName(String dhcpNetworkName) {
		NetworkGlobals.dhcpNetworkName = dhcpNetworkName;
	}
}
