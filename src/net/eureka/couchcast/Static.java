package net.eureka.couchcast;

import java.awt.Desktop;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Contains useful methods that could be common to the whole media server.
 * 
 * @author Owen McMonagle
 *
 *
 * @version 0.2
 */
public final class Static 
{
	
	/**
	 * Viable interface names found during the interface search at program start up. if 
	 * getInetAddresses has not been called, this list will be empty.
	 */
	private static final ArrayList<String> INTERFACES = new ArrayList<String>();
	
	
	/**
	 * Metric Megabyte unit of measurement.
	 */
	private static final int METRIC_MB_UNIT = 1000000;
			
	/**
	 * Metric Gigabyte unit of measurement.
	 */
	private static final int METRIC_GB_UNIT = 1000000000;
	
	/**
	 * Helps in retrieval of the last four digits in the MAC address. Extra one digit is for the
	 * included dash. e.g "##-##"   
	 */
	private static final int MAC_OFFSET = 5;
	
	/**
	 * Identifier for signalling the type of address wanted from the network interface. In this case
	 * the I.PV4 assigned to the current DHCP activated network interface. This is used in the method below 
	 * called getAddress(String address_type). 
	 */
	private static final String IP_ADDRESS_IDENTIFIER = new String("ip");
			
	/**
	 * Identifier for signalling the type of address wanted from the network interface. In this case
	 * the MAC address assigned to the current DHCP activated network interface. This is used in the method below 
	 * called getAddress(String address_type). 
	 */
	private static final String MAC_ADDRESS_IDENTIFIER = new String("mac"); 
	
	/**
	 * Used as a delimiter to retrieve the last four digits of the MAC address; So it can be assigned to the default
	 * server name.
	 */
	private static final String ADDRESS_DELIMITER = new String("-");
	
	/**
	 * Used to compare two byte sequences against each other that each have the length of 2.
	 * 
	 * @param byte[] data - First two bytes.  
	 * @param byte[] against - Second two bytes.
	 * @return Boolean - True if each sequence matches, false otherwise.
	 */
	public static boolean compareBytes(final byte[] data, final byte[] against)
	{
		return(data[0] == against[0] && data[1] == against[1]);
	}
	
	/**
	 * Used to determine if the computer O.S is 64 or 32 bit. 
	 * @return Boolean - True if 64bit. False otherwise.
	 */
	public static boolean is64BitArch()
	{
		boolean is64bit = false;
		// Checks if O.S is windows...
		if (System.getProperty("os.name").contains("Windows"))
			// if so check for 32bit program files which will confirm architecture.
		    is64bit = (System.getenv("ProgramFiles(x86)") != null);
		else
			// if the String 64 doesn't exist in property os.arch then 32bit is confirmed. Otherwise the arch is 64 bit.
		    is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
		
		// Return arch calculation.
		return is64bit;
	}
	
	/**
	 * Used to determine the byte order of the System Architecture.
	 * @return Boolean - True if Architecture is Big Endian, false otherwise. 
	 */
	public static boolean isEndianessBig()
	{
		return ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);
	}
	
	/**
	 * Retrieves the user language from the O.S.
	 * @return String - Language locale code for O.S.
	 */
	public static String getSystemLocale()
	{
		return System.getProperty("user.language");
	}
	
	/**
	 * Retrieves the MAC address associated with current DHCP activated network interface. Then parses it into
	 * a format that can be used for the end of the default server name.
	 * 
	 * WARNING: Having Multiple DHCP interfaces (such as tunnelling software) may interfere with this and return
	 * a false positive resulting in the wrong address. In this case the network adapter associated with that 
	 * particular DHCP address must be disabled.
	 *  
	 * @return StringBuffer - Last four digits of the DHCP MAC address.
	 */
	public static StringBuffer getModifiedMac()
	{
		// Retrieve MAC address from interface.
		final String mac_address = getAddress(MAC_ADDRESS_IDENTIFIER);
		// Initialize beginning parse position.
		final int begin_position = (mac_address.length()-MAC_OFFSET);
		// Retrieve the modified MAC address, from the last five index positions.
		String modified_address = mac_address.substring(begin_position);
		// Split the modified MAC address using the dash in between the last four digits. 
		final String[] split_modified_address = modified_address.split(ADDRESS_DELIMITER);
		// Concatenate the last four digits together.
		modified_address = new String(split_modified_address[0]+split_modified_address[1]);
		// Return MAC as StringBuffer.
		return new StringBuffer(modified_address);
	}
	
	/**
	 * Retrieves either the current DHCP MAC/IP address from the network interface. Used to get
	 * the MAC address for the server name.
	 * 
	 * Using the identifiers (IP_ADDRESS_IDENTIFIER and MAC_ADDRESS_IDENTIFIER above as parameters,
	 * either MAC/IP can be retrieved.
	 * 
	 * WARNING: Having Multiple DHCP interfaces (such as tunneling software) may interfere with this and return
	 * a false positive resulting in the wrong address. In this case the network adapter associated with that 
	 * particular DHCP address must be disabled.
	 * 
	 * @param address_type - IP_ADDRESS_IDENTIFIER or MAC_ADDRESS_IDENTIFIER. 
	 * @return String - IP or MAC of current DHCP activated interface, null if no DHCP interface available.
	 */
	private static String getAddress(String address_type)
	{
		// String for address storage.
	    String address = "";
	    // Attempts to retrieve DHCP address.
	    InetAddress lan_ip = getInetAddresses().get(0);
	    // If no DHCP address is available then return with no address.
        if(lan_ip == null) return null;
        // If identifier equals I.P retrieval... 
        if(address_type.equals(IP_ADDRESS_IDENTIFIER))
        	// Parse I.P address and store.
        	address = lan_ip.toString().replaceAll("^/+", "");
        // Else if the identifier is MAC retrieval... 
        else if(address_type.equals(MAC_ADDRESS_IDENTIFIER))
        	// Retrieve MAC address using InetAddress of the Interface.
        	address = getMacAddress(lan_ip);
        // Else throw exception stating the option is not valid.
		else
			try
        	{
				throw new Exception("Specify \"ip\" or \"mac\"");
			} 
        	catch (Exception e) 
        	{
				e.printStackTrace();
			}

        // Return IP or MAC.
		return address;
	}
	
	/**
	 * Retrieves particular network interface address by the passed parameter name.
	 * @param name - Name of network interface.
	 * @return {@link InetAddress} - Found network address, null if none was found.
	 */
	public static InetAddress getInetAddressFromName(String name)
	{
		ArrayList<InetAddress> valid_addresses = getInetAddresses();
		
		for(int i = 0; i < valid_addresses.size(); i ++)
			if(INTERFACES.get(i).equals(name))
				return valid_addresses.get(i);
		
		return null;
	}
	
	/**
	 * Searches through available network interfaces and creates a list of bindable address to return.
	 * @return {@link ArrayList} - List of network interface addresses of type {@link InetAddress}.
	 */
	public static ArrayList<InetAddress> getInetAddresses()
	{
		ArrayList<InetAddress> valid_addresses = new ArrayList<>();
		
		try 
		{
			ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			
			for (NetworkInterface net_interface : interfaces) 
				if(!INTERFACES.contains(net_interface.toString()) && net_interface.isUp() && !net_interface.isVirtual() && !net_interface.isLoopback() && net_interface.supportsMulticast())
				{
					// iterate over the addresses associated with the interface
					ArrayList<InetAddress> addresses = Collections.list(net_interface.getInetAddresses());
					for (int i = 0; i < addresses.size(); i ++)
					{
						InetAddress address = addresses.get(i);
						
						// look only for ipv4 addresses
						if (!(address instanceof Inet6Address) && address.isReachable(3000))
						{
							String name = net_interface.toString();
							System.out.format("["+i+"] ni: %s\n", name);
							valid_addresses.add(address);
							// Add name to interface name list
							INTERFACES.add(name.split(":")[1]);
							// Make i the size limit to exit loop.
							i = addresses.size();
						}
					}
				}
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valid_addresses;
	}
	
	/** **Deprecated**
	 * Retrieves the InetAddress of the current DHCP activated network interface. It does
	 * this by checking through each available interface, if the InetAddress of that interface
	 * equals a Inet4Address(or IPV4) and also is a Local address then that interface is chosen.
	 * 
	 * WARNING: Having Multiple DHCP interfaces (such as tunnelling software) may interfere with this and return
	 * a false positive resulting in the wrong address. In this case the network adapter associated with that 
	 * particular DHCP address must be disabled or removed.
	 * 
	 * @return InetAddress - Address of the current activated DHCP interface.
	 */
	@SuppressWarnings("unused")
	@Deprecated()
	private static InetAddress getBackUpInetAddress()
	{
		// Used for storing a located DHCP interface address.
		InetAddress lan_ip = null;
		// Used for locating a InetAddress within a Interface.
		String ip_address = null;
		// Enumeration for containing the various network interfaces available.
		Enumeration<NetworkInterface> net = null;
		try
		{
			// Retrieves Network Interfaces available.
			net = NetworkInterface.getNetworkInterfaces();
			// Iterates through each interface...
			while(net.hasMoreElements())
			{
				// Retrieve interface.
				NetworkInterface element = net.nextElement();
				if(element.isUp())
				{
					// Retrieves addresses associated with that interface.
					Enumeration<InetAddress> addresses = element.getInetAddresses();
					// Iterate through each address received...
					while (addresses.hasMoreElements())
					{
						// Get I.P from address.
						InetAddress ip = addresses.nextElement();
						// If InetAddress is an instance of the object Inet4Address (IPV4) and is a local address...
						if (ip instanceof Inet4Address && ip.isSiteLocalAddress())
						{
							// Store IP address for location.
							ip_address = ip.getHostAddress();
							// Retrieve the InetAddress associated with that verified address.
							lan_ip = InetAddress.getByName(ip_address);
						}
					}
				}
			}
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch(SocketException e)
		{
			e.printStackTrace();
		}
		
		// Return InetAddress.
		return lan_ip;
	}
	
	

	/**
	 * Retrieves MAC address of an Interface from a given InetAddress.
	 * @param InetAddress ip - Interface address from which the MAC will be taken from. 
	 * @return String - MAC address associated with the passed InetAddress Interface.
	 */
	private static String getMacAddress(InetAddress ip)
	{
		// Address for storage.
		String address = null;
		try
		{
			// Retrieve network interface from IP.
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			// Retrieve MAC in byte form.
			byte[] mac = network.getHardwareAddress();
			// StringBuilder for delicate parsing.
			StringBuilder sb = new StringBuilder();
			// Format MAC address into a more readable form.
			for (int i = 0; i < mac.length; i++)
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			
			// Cast builder to string for return.
			address = sb.toString();
		}
		catch (SocketException e) 
		{
			e.printStackTrace();
		}
		// Return MAC address.
		return address;
	}
	
	
	/**
	 * Calculates a passed long byte value into either Imperial -Megabytes or Gigabyte-Megabyte formats. 
	 * @param Long file_size - Amount of bytes to be translated.
	 * @return String - The amount of Megabytes or Gigabytes the file_size translates to.
	 */
	public static String byteCalculator(long file_size)
	{
		// Buffer for calculation.
		StringBuffer calculated_size = new StringBuffer();
		// If total file size is less than a Imperial Gigabyte Unit...
		if(file_size < METRIC_GB_UNIT)
			// Then calculate in just Megabytes.
			calculated_size.append(file_size/METRIC_MB_UNIT + "MB");
		// If total file size is greater than a Imperial Gigabyte Unit...
		else if(file_size > METRIC_GB_UNIT)
		{
			// Retrieve the amount of gigabytes available.
			int gigabytes = (int) (file_size/METRIC_GB_UNIT),
				// Retrieve the remaining megabytes available.
				remaining_size = (int) (file_size - (gigabytes*METRIC_GB_UNIT)),
				// Calculate remaining megabytes to the Imperial Megabyte Unit.
				megabytes = (int) (remaining_size/METRIC_MB_UNIT);
			// Append Calculation to buffer.
			calculated_size.append( gigabytes+"."+Integer.toString(megabytes).toCharArray()[0]+"GB");
		}
		// Return calculated result as String.
		return calculated_size.toString();
	}
	
	/**
	 * Converts long to byte array.
	 * @param x - Long to convert.
	 * @return byte[] - Byte array of the passed long
	 */
	public static byte[] longToBytes(long x) 
	{
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	/**
	 * Converts byte array to long.
	 * @param byte[] - Byte array to convert. 
	 * @return long - Long of the passed byte array.
	 */
	public static long bytesToLong(byte[] bytes)
	{
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}
	
	/**
	 * Opens a webpage with the default system browser.
	 * @param url - String URL of page to open.
	 */
	public static void openWebpage(String url)
	{
		try
		{
			openWebpage(new URL(url).toURI());
		}
		catch (MalformedURLException | URISyntaxException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens a webpage with the default system browser.
	 * @param {@link URL} - URL of page to open.
	 */
	public static void openWebpage(URL url) 
	{
	    try 
	    {
	        openWebpage(url.toURI());
	    }
	    catch (URISyntaxException e) 
	    {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * Opens a webpage with the default system browser.
	 * @param {@link URI} - URI of page to open.
	 */
	public static void openWebpage(URI uri) 
	{
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) 
	        try 
	    	{
	            desktop.browse(uri);
	        } 
	    	catch (Exception e) 
	    	{
	            e.printStackTrace();
	        }
	    
	}
	
	/**
	 * Returns interface names found during the search with the method, getInetAddresses.
	 * If that method has not been run at least once, this list will be returned empty.
	 * @return {@link ArrayList} - List of type {@link String}, contains viable interface names.
	 */
	public static ArrayList<String> getInterfaceNames()
	{
		return INTERFACES;
	}
	

}
