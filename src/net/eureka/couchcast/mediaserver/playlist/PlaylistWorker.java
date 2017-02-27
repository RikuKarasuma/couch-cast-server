package net.eureka.couchcast.mediaserver.playlist;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import net.eureka.couchcast.Static;
import net.eureka.couchcast.foundation.file.manager.FileFactory;
import net.eureka.couchcast.foundation.file.media.MediaFile;
import net.eureka.couchcast.mediaserver.NetworkHandler;
import net.eureka.couchcast.mediaserver.NetworkImpl;
import net.eureka.couchcast.mediaserver.NetworkWorker;


/**
 * Adheres to the {@link NetworkWorker} and {@link NetworkImpl} protocols. Acts as a file server endpoint.
 * Sends playlist updates to a Android Client. Runs as a persistant connection in its own thread.
 * 
 * @author Owen McMonagle.
 * 
 * @see NetworkImpl
 * @see NetworkWorker
 * @see NetworkHandler
 * 
 *
 */
public final class PlaylistWorker extends NetworkWorker
{
	/**
	 * Receiving buffer size, for receiving the file discovery sequence.
	 */
	private static final int BUFFER_SIZE = 2;
	
	private static int workerCount = 0;
	
	/**
	 * The File Discovery Sequence, this is sent by the Android Client as a form of validation.
	 */
	private static final byte[] FILE_DISCOVERY_SEQUENCE = new byte[]{ 85, 56 };
	
	
	private ObjectOutputStream output = null;
	private ObjectInputStream input = null;
	private byte[] read = new byte[2];
	private boolean valid = false, updated = true;
	
	public PlaylistWorker(Socket connection) 
	{
		super(connection);
		this.setSocketTimeout(4000);
		workerCount++;
		Thread thread = new Thread(this);
		thread.setName("Playlist Worker: "+workerCount);
		thread.start();
	}

	@Override
	public void run() 
	{
		initialiseObjectStreams();
		System.out.println("Starting file server..");
		do
		{
			//System.out.println(Thread.currentThread().getName() +" working... \n");
			read();
			valid = verify();
			write();
			this.waitForThread();
		}
		while(valid);
		close();
	}
	
	private void initialiseObjectStreams()
	{
		try
		{
			output = new ObjectOutputStream(getOutput());
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void waitForThread()
	{
		synchronized (this) 
		{
			try
			{
				this.wait(2000);
			} 
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void read() 
	{
		try 
		{
			if(input == null)
				input = new ObjectInputStream(getInput());
			input.read(read, 0, BUFFER_SIZE);
		} 
		catch (IOException e)
		{
			valid = false;
			System.err.println("File Server disconnected.\nReason: "+e.getLocalizedMessage());
			//e.printStackTrace();
		}
	}

	@Override
	public boolean verify() 
	{
		try 
		{
			return (validateAddress() && compareDiscoverySequence());
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Compares the read in sequence to the File Discovery Sequence, if matched true is returned.
	 *  
	 * @return Boolean - True if read in matches the File Discovery Sequence, False otherwise.
	 */
	private boolean compareDiscoverySequence()
	{
		// Compares byte arrays against each other.
		return Static.compareBytes(read, FILE_DISCOVERY_SEQUENCE);
	}
	
	@Override
	public void write() 
	{
		try
		{
			// Declare file list response.
			ArrayList<MediaFile> media_file_list = null;
			// Declare alpha list response.
			ArrayList<Integer> sorted_alpha_list = null;
			// Declare size list response.
			ArrayList<Integer> sorted_size_list = null;
			
			// Declare byte response
			byte[] byte_response = null;
			// If the connection is valid, retrieve and store the media file list. If not create an empty file list.
			media_file_list = (valid && updated) ? FileFactory.getMediaPlaylist() : new ArrayList<MediaFile>();
			
			sorted_alpha_list = (valid && updated) ? FileFactory.getAlphaList() : new ArrayList<Integer>();
			
			sorted_size_list = (valid && updated) ? FileFactory.getSortedSizeList() : new ArrayList<Integer>();
			
			// If the connection is valid, respond with the File Discovery Sequence. If not create a blank byte array.
			byte_response = (valid) ? FILE_DISCOVERY_SEQUENCE : new byte[]{ 0, 0 };
			// Write byte response to client.
			output.write(byte_response, 0, byte_response.length);
			// Write object response to client.
			output.writeObject(media_file_list);
			// Flush response code & media list down stream.
			output.flush();
			// Write alpha list response to client.
			output.writeObject(sorted_alpha_list);
			// Flush alpha list down stream.
			output.flush();
			// Write size list response to client.
			output.writeObject(sorted_size_list);
			// Flush size list down stream.
			output.flush();
			// If the play-list was just updated...
			if(updated)
				// Signal the update was complete.
				updated = false;
		}
		catch(IOException e)
		{
			valid = false;
			System.err.println(Thread.currentThread().getName() + " disconnected.");
		}

	}
	
	/**
	 * Indicates to the handler that a new play-list should be sent on the next information pass.
	 */
	public synchronized void setPlaylistUpdated() 
	{
		updated = true;
	}
	
	public synchronized boolean isValid()
	{
		return valid;
	}

}
