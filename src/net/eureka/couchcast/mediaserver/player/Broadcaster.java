package net.eureka.couchcast.mediaserver.player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import net.eureka.couchcast.mediaserver.NetworkInfo;
import net.eureka.couchcast.mediaserver.NetworkWorker;

public final class Broadcaster extends NetworkWorker
{
	public Broadcaster(Socket connection) 
	{
		super(connection);
	}

	@Override
	public void run()
	{
		boolean valid = verify();
		if(valid)
			write();
		
		close();
	}

	@Override
	public boolean verify()
	{
		try 
		{
			return validateAddress();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void write() 
	{
		DataOutputStream output = new DataOutputStream(getOutput());
		NetworkInfo info_object = Receiver.getMediaInfo();
		if(info_object != null)
			try
			{
				convertDataAndSend(output, info_object);
				// Flush bytes down stream.
				output.flush();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
	}
		
	private void convertDataAndSend(DataOutputStream output, NetworkInfo info_object) throws IOException
	{
		if(info_object != null)
			sendInfoData(output, info_object);
		else
			sendNullData(output);
	}
	
	private void sendInfoData(DataOutputStream output, NetworkInfo info_object) throws IOException
	{
		synchronized (info_object) 
		{
			// Write is playing to stream. // 1
			output.writeBoolean(info_object.isPlaying());
			// Write is forward to stream. // 2
			output.writeBoolean(info_object.isForward());
			// Write volume byte to stream. // 3
			output.writeByte(info_object.getVolume());
			// Write playing index to stream. // 7
			output.writeInt(info_object.getIndex());
			// Write total time to stream. // 15
			output.writeLong(info_object.getLength());
			// Write current time to stream. // 23
			output.writeLong(info_object.getTime());
			// Write music mode to stream. // 24
			output.writeBoolean(info_object.isMusic());
		}
	}
	
	private void sendNullData(DataOutputStream output) throws IOException
	{
		// Write is playing to stream. // 1
		output.writeBoolean(false);
		// Write is forward to stream. // 2
		output.writeBoolean(false);
		// Write volume byte to stream. // 3
		output.writeByte(0);
		// Write playing index to stream. // 7
		output.writeInt(-1);
		// Write total time to stream. // 15
		output.writeLong(0L);
		// Write current time to stream. // 23
		output.writeLong(0L);
		// Write music mode to stream. // 24
		output.writeBoolean(false);
	}
	
	
	//Unused.
	@Override
	public void read() {}

}
