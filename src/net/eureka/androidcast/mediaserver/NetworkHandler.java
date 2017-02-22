package net.eureka.androidcast.mediaserver;

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

import net.eureka.androidcast.foundation.file.manager.DirectoryFactory;
import net.eureka.androidcast.mediaserver.authentication.ConnectionValidation;
import net.eureka.androidcast.mediaserver.player.Broadcaster;
import net.eureka.androidcast.mediaserver.player.Receiver;
import net.eureka.androidcast.mediaserver.playlist.PlaylistWorker;

public final class NetworkHandler implements Runnable
{
	private static final int PORT = 63050; //0xf64a
	private static final byte AUTHENTICATION_CODE = -127, //0x81
								FILE_CODE = -126, //0x82 
								INFO_CODE = -125, //0x83
								RECEIVER_CODE = -124; // 0x84
	
	private static final ExecutorService WORKERS = Executors.newCachedThreadPool();
	private static boolean networking = true;
	private static ServerSocket serverSocket = null;
	/**
	 * List of each ConnectionHandler serving out files.
	 */
	private static final List<NetworkWorker> persistantConnections = Collections.synchronizedList(new ArrayList<NetworkWorker>());
	
	public NetworkHandler() 
	{
		//this.setName("Network Handler");
		this.initialise();
		//this.start();
		WORKERS.execute(this);
	}
	
	private void initialise()
	{
		try 
		{
			serverSocket = new ServerSocket(PORT);
			serverSocket.setSoTimeout(5000);
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() 
	{
		// Keep searching for new traffic...
		while(networking)
		{
			// Wait for incoming connections.
			//System.out.println("Waiting...");
			handleIncoming();
		}
	}
	
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
	
	private static Socket getNewConnection()
	{
		try 
		{
			// Attempt to accept socket. Timeout = 2 seconds.
			return serverSocket.accept();
		} 
		// Catch and ignore timeout.
		catch (SocketTimeoutException e)
		{
			//System.out.println("Timeout...");
			handlePersistantConnections();
		}
		// Catch and display IO errors.
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
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
	
	private synchronized static void handlePersistantConnections()
	{
		if(persistantConnections.size() > 0)
			// Handle persistant connections.
			handlePersistance();
	}
	
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
	 * {@link FileHandler} currently active, that a new play-list should be sent.
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
