package net.eureka.couchcast.mediaserver.player;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import net.eureka.couchcast.Static;
import net.eureka.couchcast.foundation.config.Configuration;
import net.eureka.couchcast.foundation.file.manager.FileFactory;
import net.eureka.couchcast.foundation.file.media.MediaFile;
import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.mediaserver.NetworkInfo;
import net.eureka.couchcast.mediaserver.NetworkWorker;

public final class Receiver extends NetworkWorker
{
	/**
	 *	Sets the OOP video frame to invisible.
	 */
	private static final byte[] HIDE_FRAME_SEQUENCE = new byte[]{ 80, 10};

	/**
	 * 	Sets the OOP video frame to visible.
	 */
	private static final byte[] SHOW_FRAME_SEQUENCE = new byte[]{ 80, 20};
	
	/**
	 *	 Returned to client when a command has been successful.
	 */
	private static final byte[] PLAYER_SUCCESS_SEQUENCE = new byte[]{ 120, 45};

	/**
	 * 	Returned to client when a command has failed.
	 */
	private static final byte[] PLAYER_FAILURE_SEQUENCE = new byte[]{ 120, 80};
	
	/**
	 * 	Play media file.
	 */
	private static final byte[] PLAY_FILE_SEQUENCE = new byte[] { 110, 40};
	
	/**
	 * 	Play media file.
	 */
	private static final byte[] PLAY_TUBE_SEQUENCE = new byte[] { 120, 40};
	
	/**
	 * 	Plays or Pauses media
	 */
	private static final byte[] PLAY_PAUSE_SEQUENCE = new byte[]{ 40, 21};
	
	/**
	 * 	Stops current media.
	 */
	private static final byte[] STOP_FILE_SEQUENCE = new byte[]{ 25, 55};
	
	/**
	 * 	Enable/Disable Fast forward.
	 */
	private static final byte[] FAST_FORWARD_SEQUENCE = new byte[]{ 58, 35};
	
	/**
	 * 	Rewind back 5 seconds.
	 */
	private static final byte[] FAST_REWIND_SEQUENCE = new byte[]{ 57, 34};
	
	/**
	 * 	Buffer size of each media command.
	 */
	private static final int BUFFER_SIZE = 2;
	
	private static long timeSincePlayed = 0; 
	
	//private static int playCount = 0;
	
	private static byte volume = 25;
	
	
	private static final Timer PLAY_TIMER = new Timer();
	
	private static TimerTask timedPlayTask = null;
	
	/**
	 * Wrapper for each bridge connection.
	 */
	private static Socket bridgeConnection = null;
	
	/**
	 * Object Output stream that must be initialized before object input stream.
	 */
	private static ObjectOutputStream bridgeOutput = null;
	
	/**
	 * Object Input stream that must be initialized after object output stream.
	 */
	private static ObjectInputStream bridgeInput = null;
	
	private static NetworkInfo info = null;
	
	/**
	 * Wrapper for the received media file to play.
	 */
	private static MediaFile file = null;
	
	private static boolean startingOOP = false, startedOOP = false;
	
	private static boolean isTimerRunning = false;
	
	/**
	 * Used to contain the passed YouTube MRL.
	 */
	private String mrl = "";
	
	private boolean valid = false;
	
	/**
	 * Buffer of 2 bytes for reading in each command.
	 */
	private byte[] read = new byte[BUFFER_SIZE];
	
	/**
	 * Object Output stream that must be initialised before object input stream.
	 */
	private ObjectOutputStream clientOutput = null;
	
	/**
	 * Object Input stream that must be initialised after object output stream.
	 */
	private ObjectInputStream clientInput = null;
	
	public Receiver(Socket connection)
	{
		super(connection);
	}
	
	public Receiver(boolean start, MediaFile media_file)
	{
		super(null);
		if(start)
		{
			file = media_file;
			read = PLAY_FILE_SEQUENCE;
			validateCommand();
		}
		else
			sendStopSig();
	}

	@Override
	public void run()
	{
		try
		{
			clientOutput = new ObjectOutputStream(getOutput());
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Read and validate number of bytes that were taken in.
		read();
		// If number of bytes valid...
		if(valid)
			// Validate connection and the passed command.
			verify();
		// Write response based on validation
		write();
		// Close connection.
		close();
	}

	@Override
	public void read() 
	{
		// For checking number of bytes read.
		int bytes_read = 0;

		try 
		{
			clientInput = new ObjectInputStream(getInput());
			// Read the first two bytes into array.
			bytes_read = clientInput.read(read, 0, BUFFER_SIZE);
			//System.out.println("Bytes read:"+ read[0] +", " + read[1]);
			// Check for file associated with command. NOTE: Would only happen if command was PLAY_FILE_SEQUENCE.
			checkForFile();
			// Check for string associated with command. NOTE: Would only happen if command was PLAY_TUBE_SEQUENCE.
			checkForMRL();
			checkForMusicMode();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			if(bytes_read == 2)
				valid = true;
		}
	}
	
	/**
	 * Checks if the read byte array is equal to the PLAY_FILE_SEQUENCE, if so. This means that a media file wrapper object
	 * will be sent through the stream straight after the command. The file will be read and kept for use in running the command.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void checkForFile() throws ClassNotFoundException, IOException
	{
		// Check if read equals PLAY_FILE_SEQUENCE, if so...
		if(Static.compareBytes(read, PLAY_FILE_SEQUENCE))
			// Read in MediaFile object from client.
			file = (MediaFile) clientInput.readObject();
	}
	
	/**
	 * Checks if the read byte array is equal to the PLAY_TUBE_SEQUENCE, if so. This means that a MRL string object
	 * will be sent through the stream straight after the command. The string will be read and kept for use in running the command.
	 * @throws IOException
	 */
	private void checkForMRL() throws IOException
	{
		// Check if read equals PLAY_FILE_SEQUENCE, if so...
		if(Static.compareBytes(read, PLAY_TUBE_SEQUENCE))
			// Read in string from client.
			mrl =  clientInput.readUTF();
	}
	
	private void checkForMusicMode()
	{
		if(Static.compareBytes(read, SHOW_FRAME_SEQUENCE))
		{
			ApplicationGlobals.setMusicMode(false);
			new Configuration(true);
		}
		else if(Static.compareBytes(read, HIDE_FRAME_SEQUENCE))
		{
			ApplicationGlobals.setMusicMode(true);
			new Configuration(true);
		}
	}

	@Override
	public boolean verify() 
	{
		// Attempt to validate client address.
		boolean valid = false;
		try 
		{
			valid = validateAddress();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		// If client address is valid...
		if(!valid)
			return false;
		else
		{
			//System.out.println("Valid connection.");
			// Attempt to validate command.
			validateCommand();
		}
		
		
		return valid;
	}
	
	/**
	 * Goes through each command sequence trying to match what was read so it will know which command
	 * to run through the media player.
	 */
	private void validateCommand()
	{
		if(file != null)
			// Check if bytes read equal PLAY_FILE_SEQUENCE...
			if(Static.compareBytes(read, PLAY_FILE_SEQUENCE))
				// Play file read in.
				play();
			else if( !startedOOP && !startingOOP && (Static.compareBytes(read, PLAY_PAUSE_SEQUENCE)))
			{
				read = PLAY_FILE_SEQUENCE;
				play();
			}
			// Check if bytes read equal PLAY_PAUSE_SEQUENCE...
			else if((Static.compareBytes(read, PLAY_PAUSE_SEQUENCE) || Static.compareBytes(read, STOP_FILE_SEQUENCE) || Static.compareBytes(read, STOP_FILE_SEQUENCE)
					|| Static.compareBytes(read, FAST_FORWARD_SEQUENCE) || Static.compareBytes(read, FAST_FORWARD_SEQUENCE) || Static.compareBytes(read, FAST_REWIND_SEQUENCE)
					|| read[0] == 98 || Static.compareBytes(read, HIDE_FRAME_SEQUENCE) || Static.compareBytes(read, SHOW_FRAME_SEQUENCE)) && startedOOP)
				//System.out.println("Sending to bridge.");
				sendToBridge();
			else if(read[0] == 99 && startedOOP)
			{
				volume = read[1];
				sendToBridge();
			}
			else if(Static.compareBytes(read, PLAY_TUBE_SEQUENCE))
				playMRL();
	}
	
	/**
	 * Plays the received media file.
	 */
	private void playMRL()
	{
		// If command is enabled to run..
		if(mrl != null && !mrl.isEmpty())
		{
			// If media player not null....
			if(bridgeConnection != null)
			{
				read = STOP_FILE_SEQUENCE;
				sendToBridge();
				bridgeConnection = null;
				read = PLAY_TUBE_SEQUENCE;
			}
			file = null;
			sendMediaToOOP();
		}
	}
	
	/**
	 * Plays the received media file.
	 */
	private void play()
	{
		final int LIMIT = 3000; 
		long time = System.currentTimeMillis() - timeSincePlayed;
		if(((time > LIMIT) || (time == 0)) && !isTimerRunning && !startingOOP)
			sendPlaySig();
		else 
			waitAndPlay();
	}
	
	private void waitAndPlay()
	{
		if(isTimerRunning) timedPlayTask.cancel();
		
		isTimerRunning = true;
		timedPlayTask = new TimerTask()
		{
			@Override
			public void run()
			{
				sendPlaySig();
				isTimerRunning = false;
			}
		};
		PLAY_TIMER.schedule(timedPlayTask, 3000);
		
	}
	
	private void sendPlaySig()
	{
		// If media player not null....
		if(bridgeConnection != null)
			sendStopSig();
		read = PLAY_FILE_SEQUENCE;
		sendMediaToOOP();
		timeSincePlayed = System.currentTimeMillis();
	}
	
	private void sendStopSig()
	{
		read = STOP_FILE_SEQUENCE;
		sendToBridge();
		bridgeConnection = null;
	}
	
	private void sendToBridge()
	{
		//System.out.println("Attempting to send to bridge...");
		if(bridgeConnection != null)
			synchronized (bridgeOutput) 
			{
				try 
				{
					//System.out.println("Valid.");
					bridgeOutput.write(read, 0, BUFFER_SIZE);
					bridgeOutput.flush();
					//System.out.println("Written.");
				} 
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	
	private void sendMediaToOOP()
	{
		// Initialize media player.
		startBridge();
		writeMediaToOOP();
	}
	
	private void writeMediaToOOP()
	{
		try 
		{
			sendToBridge();
			synchronized (bridgeOutput) 
			{
				if(file != null)
				{
					final int file_index = FileFactory.compareMediaFilesForIndex(file.getLocation());
					final String media_path = new String(file.getLocation());
					bridgeOutput.writeUTF(media_path);
					bridgeOutput.writeInt(file_index);
				}
				else
					bridgeOutput.writeUTF(mrl);
				bridgeOutput.flush();
			}
			read = new byte[]{ 99, volume};
			sendToBridge();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void writeMediaToOOP(String path_to_media) throws IOException
	{
		synchronized (bridgeOutput) 
		{
			bridgeOutput.writeUTF(path_to_media);
		}
	}
	

	@Override
	public void write()
	{
		// Byte array for response.
		byte[] response = null;
		try
		{
			// If validConnection and runCommand are true, then response equals PLAYER_SUCCESS_SEQUENCE. If not PLAYER_FAILURE_SEQUENCE.
			response = (valid) ? PLAYER_SUCCESS_SEQUENCE : PLAYER_FAILURE_SEQUENCE ;
			// Write response to stream.
			clientOutput.write(response, 0, BUFFER_SIZE);
			// Flush stream to client.
			clientOutput.flush();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void checkIfBridgeInitialization()
	{
		if(Static.compareBytes(read, PLAY_FILE_SEQUENCE) && !startedOOP)
			while(!startedOOP)
				synchronized (this)
				{
					try
					{
						this.wait(50);
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
				}
	}
	
	private static void startBridge()
	{
		boolean connected = false, error = false;
		KeepWindowFocusHack focus_hack = null;
		byte connection_counter = 0;
		while(!connected && !error)
			try 
			{
				if(ApplicationGlobals.isMusicMode())
					focus_hack = new KeepWindowFocusHack();
				
				startOOP();
				startOOPConection();
				startReading();
				connected = true;
			}
			catch (Exception e)
			{
				connection_counter += 1;
				//e.printStackTrace();
				System.err.println("Failed to connect to OOP " + connection_counter + " times.");
				
				if(connection_counter >= 10)
					error = true;
			}
			finally
			{
				if(focus_hack != null)
					focus_hack.running = false;
			}
	}
	
	private static void startOOP()
	{
		String bridge_path = ApplicationGlobals.getInstallPath() + new String(ApplicationGlobals.getName())+" OOP.exe";
		try
		{
			startingOOP = true;
			Runtime.getRuntime().exec(bridge_path);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void startOOPConection() throws UnknownHostException, IOException
	{
		final InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 63053);
		final int timeout = 5000;
		bridgeConnection = new Socket();
		bridgeConnection.connect(address, timeout);
        bridgeOutput = new ObjectOutputStream(bridgeConnection.getOutputStream());
        bridgeInput = new ObjectInputStream(bridgeConnection.getInputStream());
	}
	
	private static void startReading()
	{
		new Thread(new Runnable()
        {
			@Override
			public void run() 
			{
				boolean connected = true;
				NetworkInfo received_info = null;
				while(connected)
				{
					try
					{
						received_info = (NetworkInfo) bridgeInput.readObject();
						waitForOppositeWrite();
						if(received_info != null)
						{
							info = received_info;
							startingOOP = false;
							startedOOP = true;
						}
						
					}
					catch (Exception e)
					{
						System.err.println("Disconnected from OOP.");
						info.setTime(info.getLength());
						info.setPlaying(false);
						info.setForward(false);
						connected = false;
						startedOOP = false;
					}
				}
				resetBridge();
			}
			
			private void waitForOppositeWrite() throws InterruptedException
			{
				synchronized (this)
				{
					this.wait(900);
				}
			}
		}).start();
	}
	
	private static void resetBridge()
	{
		try 
		{
			bridgeConnection.close();
		}
		catch (NullPointerException e)
		{
			System.out.println("Bridge already disconnected.");
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			bridgeConnection = null;
			//info = null;
		}
	}
	
	public static NetworkInfo getMediaInfo()
	{
		return info;
	}
	
	
	/**
	 * A hack used to keep video games focused in full screen.
	 * 
	 * It simply keeps the app focused by sending input events
	 * just before the Out Of Process Player starts. By doing
	 * this, windows acknowledges the current app should keep
	 * focus because input events seem to be of a higher 
	 * precedence than background window events.
	 * 
	 * The key being pressed is NUM_LOCK.
	 * 
	 * Requires game to be top level container which already 
	 * has focus in order to work.
	 * 
	 * @author Garak
	 *
	 */
	private final static class KeepWindowFocusHack implements Runnable
	{
		private boolean running = true;
		
		private KeepWindowFocusHack() 
		{
			new Thread(this).start();
		}
		
		@Override
		public void run() 
		{
			try 
			{
				long time = System.currentTimeMillis();
				java.awt.Robot key_robot = new java.awt.Robot();
				while(running)
				{
					key_robot.keyPress(KeyEvent.VK_NUM_LOCK);
					key_robot.keyRelease(KeyEvent.VK_NUM_LOCK);
					key_robot.delay(150);
					
					time = System.currentTimeMillis() - time;
					if(time > 10000)
						running = false;
				}
			} 
			catch (AWTException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
