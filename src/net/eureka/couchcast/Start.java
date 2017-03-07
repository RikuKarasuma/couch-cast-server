package net.eureka.couchcast;

import java.sql.Timestamp;

import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.foundation.init.Bootstrap;
import net.eureka.couchcast.foundation.init.NetworkGlobals;
import net.eureka.couchcast.foundation.logging.Logger;
import net.eureka.couchcast.gui.AppStage;
import net.eureka.couchcast.gui.tray.Tray;
import net.eureka.couchcast.mediaserver.NetworkHandler;
import net.eureka.couchcast.mediaserver.discovery.PeerReceiver;

/**
 * This is the starting point of the media server. Here each part is initialized and held as global objects so that 
 * they won't be Garbage Collected. Here is a list of each object that is initialized and their functions. </br>
 * 
 * <pre>
 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |Class|||||||||||||Reference Name||||||Function/s|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |PeerReceiver||||||discoveryServer|||||UPnP network discovery. Handles sending of Server I.P details to client.|||||
 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |NetworkHandler||||clientHandler|||||||TCP/IP media server delegate. Handles receiving and verification of media||||
 * |||||||||||||||||||||||||||||||||||||||server commands.|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |Bootstrap|||||||||bootstrap|||||||||||Initializes logger, directory, configuration creation and the file fetcher.||
 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |AppStage||||||||||gui|||||||||||||||||Handles menus creation as well as manages the desktop tray.||||||||||||||||||
 * ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * </pre>
 * The default server name is retrieved here via a method if it cannot be found within the configuration file on start 
 * up in the {@link Bootstrap} area.
 * 
 * Here would be the proper place to restart various components of the server as references need to be held to contain 
 * the object tree and this is the top most level of the server. 
 * </br>
 * </br>
 * <h1> VM ARGUEMENTS: -Dfile.encoding=utf-8 -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10 </br> Make sure these are
 * run through the start script and login script.</h1>
 * </br>
 * 
 * @author Owen McMonagle.
 * 
 * 
 * @see PeerReceiver
 * @see Tray
 * @see Bootstrap
 * @see NetworkHandler
 * @see AppStage
 * 
 * @version 0.3
 */
 

@SuppressWarnings("unused")
public class Start 
{
	/**
	 * Used for time stamping the log with the new starting time. 
	 */
	private static final StringBuffer INITIALISING_LOG_TEXT = new StringBuffer(ApplicationGlobals.getName()+":"+ new Timestamp(System.currentTimeMillis()).toString());
	
	/**
	 * UPnP network discovery. Handles sending of Server I.P details to client.
	 * * REQUIRES RESTART AFTER NETWORK FAILURE* Handled automatically.
	 * Object restarts itself from the method InitialiseNetwork.restartPeerDiscovery below.
	 */
	private static PeerReceiver discoveryServer = null;
	
	/**
	 * TCP/IP media server delegate. Handles receiving and verification of media server commands/actions.
	 */
	private static NetworkHandler clientHandler = null;
	
	/**
	 * A javafx gui app. Creates the main and options menus. Also creates the {@link Tray} icon which manages the
	 * visibility of the {@link AppStage}. 
	 */
	private static AppStage gui = null;
	
	/**
	 * Initialises logger, directory and configuration creation, file fetcher and the file server.
	 */
	private static Bootstrap bootstrap = null;
	
	/**
	 * Program Starting Constructor. Initialises each object in a specific order because some components interlink at certain times.
	 * For instance, The Logger is created within InitialiseFoundation. Then after it is created the Logger is called to append the
	 * server starting time to file. If the Logger was called before InitialiseFoundation was created then the server would crash 
	 * because the Logger was not set up. Below has each line is explained.    
	 */
	public Start() 
	{
		if(!Static.is64BitArch())
			new AppStage(true, false);
		else
		{		
			// Initialise server name with DHCP interface MAC address. 
			setUpServerName();
			// Initialise Logger, directory and configuration creation, file fetcher and server.
			bootstrap = new Bootstrap();
			// Append to log when the server has started.
			Logger.append(INITIALISING_LOG_TEXT);
			// If dhcp network has been found...
			if(!bootstrap.hasNoDHCPNetwork())
				// start networking.
				startNetworking();
			// Initialise menu.
			gui = new AppStage(false, bootstrap.hasNoDHCPNetwork());
			
		}
	}
	
	/**
	 * Server program start. Initialises starting constructor {@link InitialiseNetwork}. 
	 * @param args UNUSED.
	 */
	public static void main(String[] args)
	{
		new Start();
	}
	
	/**
	 * Initialises server name with DHCP interface MAC address. Then stores it within NetworkGlobals for use throughout the program if necessary.
	 */
	private static void setUpServerName()
	{
		// Stores it within NetworkGlobals.
		NetworkGlobals.setServerName(NetworkGlobals.getDefaultServerName());
	}
	
	/**
	 * Initialises networking components.
	 */
	public static void startNetworking()
	{
		// Initialise UPnP discovery server.
		discoveryServer = new PeerReceiver();
		// Initialise TCP network server.
		clientHandler = new NetworkHandler();
	}
	
	/**
	 * Restarts peer discovery after a network interface failure.
	 */
	public static void restartPeerDiscovery()
	{
		discoveryServer = new PeerReceiver();
	}
}
