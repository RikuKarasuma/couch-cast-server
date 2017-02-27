package net.eureka.couchcast.mediaserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;
import net.eureka.couchcast.foundation.init.NetworkGlobals;
import net.eureka.couchcast.mediaserver.authentication.ConnectionValidation;
import net.eureka.couchcast.mediaserver.player.Broadcaster;
import net.eureka.couchcast.mediaserver.player.Receiver;
import net.eureka.couchcast.mediaserver.playlist.PlaylistWorker;

/**
 * Acts as a gateway which constantly listens for incoming TCP/IP connections at port 63050. Specific byte codes
 * determine the path that the connections will take with respect to the actions they wish to achieve, this is called
 * the action stage. Once a path has been decided, the connection is then passed to the thread pool for completion, 
 * or added to a persistant connections list for constant monitoring.
 * <br>
 * <br>
 * {@link DirectoryFactory} signals playlist updates to each persistant {@link NetworkWorker} through a method called
 * signalPlaylistUpdate().
 * 
 * @author Owen McMonagle
 * @see NetworkImpl
 * @see NetworkWorker
 * @see PlaylistWorker
 * @see Broadcaster
 * @see Receiver
 * @see ConnectionValidation
 * @see DirectoryFactory
 */
public final class NetworkHandler implements Runnable
{
	private static final int PORT = 63050, //0xf64a
								CONNECTION_BACKLOG_LIMIT = 100; // 0x64
	
	/**
	 * Byte code actions. Each byte code represents a path that a TCP connection will take.
	 */
	private static final byte AUTHENTICATION_CODE = -127, //0x81
								FILE_CODE = -126, //0x82 
								INFO_CODE = -125, //0x83
								RECEIVER_CODE = -124; // 0x84
	
	
	/**
	 * Thread pool of workers that expand and contract as needed. These handle all
	 * the incoming connections that ain't persistant.
	 */
	private static final ExecutorService WORKERS = Executors.newCachedThreadPool();
	
	/**
	 * Networking flag, continues the while loop that looks for more traffic. 
	 */
	private static boolean networking = true;
	
	/**
	 * Server socket which listens for incoming TCP connections.
	 */
	private static ServerSocket serverSocket = null;
	/**
	 * List of each ConnectionHandler serving out files.
	 */
	private static final List<NetworkWorker> persistantConnections = Collections.synchronizedList(new ArrayList<NetworkWorker>());
	
	/**
	 * Dedicated thread to listen for incoming TCP connections.
	 */
	private static Thread handlerThread = null;
	
	/**
	 * Initialises the server socket to listen for incoming traffic, then proceeds to start
	 * a dedicated thread to handle said incoming traffic.
	 */
	public NetworkHandler() 
	{
		//this.setName("Network Handler");
		this.initialise();
		handlerThread = new Thread(this);
		handlerThread.setName("Network Handler");
		handlerThread.start();
	}
	
	/**
	 * Creates a new server socket on port 63050. Sets a socket timeout of five seconds.
	 */
	private void initialise()
	{
		try 
		{
			serverSocket = new ServerSocket(PORT, CONNECTION_BACKLOG_LIMIT, NetworkGlobals.getDhcpNetwork());
			serverSocket.setSoTimeout(5000);
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Searches for incoming traffic continuously. Once found passes connections off 
	 * to a verification method.
	 */
	@Override
	public void run() 
	{
		// Keep searching for new traffic...
		while(networking)
			handleIncoming();
	}
	
	/**
	 * Retrieves a new connection from the server socket (Blocks until found). Then
	 * passes connection to a verification method called verifyConnection(). 
	 */
	private static void handleIncoming()
	{
		// Connection is closed at either verifyConnection or within a NetworkWorker.
		// Get new connection.
		Socket incoming_connection = getNewConnection();
		// Verify connection not null.
		if(incoming_connection != null)
			// Pass incoming connections for verification.
			verifyConnection(incoming_connection);
	}
	
	/**
	 * Retrieves a new connection from server socket. Timeout occurs every five seconds.
	 * Once a timeout occurs, the persistant connections is verified. 
	 * @return {@link Socket} - Incoming tcp connection.
	 */
	private static Socket getNewConnection()
	{
		try 
		{
			// Attempt to accept socket. Timeout = 2 seconds.
			return serverSocket.accept();
		} 
		// Catch and ignore timeout. Attempt to handle persistant
		// connection each time instead.
		catch (SocketTimeoutException e)
		{
			handlePersistantConnections();
		}
		// Catch and display IO errors.
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Attempts to verify the incoming tcp connection via socket. A byte code is read 
	 * in order to determine the action that the connection wishes to take. If the byte
	 * is verified, the server responds with byte value 1 to indicate success. If not
	 * the value 0 is sent and the connection is closed. 
	 * <br>
	 * <br>
	 * If successful, the verified connection will be passed on as a persistant connection
	 * in its own thread, or as a job to be run on the "WORKERS" thread pool. This occurs
	 * during the verifyCode() stage.
	 * @param incoming_connection - Incoming {@link Socket} you wish to verify. 
	 */
	private static void verifyConnection(Socket incoming_connection)
	{
		try
		{
			// Get Streams....
			OutputStream  output = incoming_connection.getOutputStream();
			InputStream input = incoming_connection.getInputStream();
			
			// Read in function code.
			// Represented by a byte value.
			byte read = (byte) input.read();
			
			//Verify read in byte.
			//If successful, will schedule task. 
			boolean valid = verifyCode(read, incoming_connection);
			
			// Generate response byte.
			// 1 = Valid, 0 = Invalid.
			byte response = (byte)(valid ? 1 :  0);
			// Write response to client.
			output.write(response);
			// Flush bytes downstream.
			output.flush();
			
			// If not valid...
			if(!valid)
				// Close incoming socket.
				incoming_connection.close();
		}
		catch(SocketException e)
		{
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Attempts to verify the incoming {@link Socket} by checking if the byte code received
	 * during the last stage verifyConnection() matches any corresponding action we have. If
	 * so, the connection is passed off to a thread pool named "WORKERS" (For a short lived
	 * action) or to its own thread (For persistant actions) and added to the persistant 
	 * connections list.
	 * 
	 * @param code - Byte code received at the verifyConnection method.
	 * @param incoming_connection - TCP connection associated with the byte code to verify.
	 * @return Returns true if the action has been verified and cleared to run. False otherwise.
	 */
	private static boolean verifyCode(byte code, Socket incoming_connection)
	{
		switch(code)
		{
			case AUTHENTICATION_CODE:
				//System.out.println("Authentication code.");
				WORKERS.execute(new ConnectionValidation(incoming_connection));
				break;
			case FILE_CODE:
				//System.out.println("Playlist code.");
				PlaylistWorker playlist_worker = new PlaylistWorker(incoming_connection);
				persistantConnections.add(playlist_worker);
				break;	
			case INFO_CODE:
				//System.out.println("Info code.");
				WORKERS.execute(new Broadcaster(incoming_connection));
				break;
			case RECEIVER_CODE:
				//System.out.println("Receiver code.");
				WORKERS.execute(new Receiver(incoming_connection));
				break;
			default:
				
				return false;
		}
		return true;
	}
	
	/**
	 * Goes through each persistant connection and checks its validity.
	 */
	private synchronized static void handlePersistantConnections()
	{
		if(persistantConnections.size() > 0)
			// Handle persistant connections.
			handlePersistance();
	}
	
	/**
	 * Iterates through each persistant connection and checks for any invalid connections.
	 * Invalid connections are connections which may have timed out or disconnected.
	 * These are then removed from the persistant connections list. 
	 */
	private synchronized static void handlePersistance()
	{
		ArrayList<NetworkWorker> invalid_connections = new ArrayList<NetworkWorker>();
		for(NetworkWorker worker : persistantConnections)
			if(worker instanceof PlaylistWorker && !((PlaylistWorker)worker).isValid())
				invalid_connections.add(worker);
		
		if(invalid_connections.size() > 0)
			persistantConnections.remove(invalid_connections);
	}
	
	/**
	 * Called by the {@link DirectoryFactory} whenever a updated play-list needs to be sent. Informs each
	 * {@link PlaylistWorker} currently active, that a new play-list should be sent.
	 */
	public synchronized static void signalPlaylistUpdate()
	{
		// If there is any connections...
		if(persistantConnections.size() > 0)
			// Iterate through connections....
			for(NetworkWorker client_playlist_connection : persistantConnections)
				// Signal to the handler that a new play-list should be sent.
				if(client_playlist_connection instanceof PlaylistWorker)
					((PlaylistWorker)client_playlist_connection).setPlaylistUpdated();
	}
	
}
