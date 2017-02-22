package net.eureka.androidcast.mediaserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import net.eureka.androidcast.foundation.init.NetworkGlobals;

public abstract class NetworkWorker implements NetworkImpl
{
	private Socket connection = null;
	private boolean executed = false, scheduled = false;
	
	public NetworkWorker(final Socket connection) 
	{
		this.connection = connection;
	}
	
	public abstract void run(); 

	public abstract void read();

	public abstract boolean verify(); 

	public abstract void write(); 
	
	public final InetAddress getAddress()
	{
		return connection.getInetAddress();
	}
	
	/**
	 * Attempts to validate the client address at {@link NetworkGlobals}. If validated then a reply
	 * can be sent with the media file list.
	 * 
	 * @throws IOException - In/Out error will be thrown if the client address cannot be retrieved.
	 */
	public final boolean validateAddress() throws IOException
	{
		// Retrieve client address.
		final InetAddress client_address = connection.getInetAddress();
		// Attempt to validate client address.
		return NetworkGlobals.compareAddress(client_address);
	}

	@Override
	public final void close()
	{
		try 
		{
			connection.close();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public final OutputStream getOutput() 
	{
		try
		{
			return connection.getOutputStream();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public final InputStream getInput()
	{
		try
		{
			return connection.getInputStream();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public final void setSocketTimeout(int timeout)
	{
		try 
		{
			this.connection.setSoTimeout(timeout);
		} 
		catch (SocketException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isExecuted() 
	{
		return executed;
	}

	public void setExecuted(boolean executed) 
	{
		this.executed = executed;
	}

	public boolean isScheduled() 
	{
		return scheduled;
	}

	public void setScheduled(boolean scheduled) 
	{
		this.scheduled = scheduled;
	}

}
