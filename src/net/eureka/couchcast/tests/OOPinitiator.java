package net.eureka.couchcast.tests;

import net.eureka.couchcast.foundation.file.media.MediaFile;
import net.eureka.couchcast.mediaserver.player.Receiver;



/**
 * Tests the {@link Receiver} contract of starting and stopping the OOP. Does this by repeating the
 * process constantly. Does not test the initial network protocol.  
 * @author Garak
 *
 */
public final class OOPinitiator extends TestBase
{
	
	public OOPinitiator()
	{
		// Set failed to true so we know it failed if it hasn't gotten to the end.
		setFailed(true);
		
		// Initialise test file.
		final MediaFile test_file = new MediaFile("Test Name".getBytes(), "E:\\Media\\Unsorted\\Dark Materia - Frame of Mind.mp4".getBytes(), "0".getBytes());
		// Start flag.
		final boolean start_playing = true,
				// Stop flag.
				stop_playing = false;
		
		
		// Repeat twenty times...
		for(int i = 0; i < 20; i++ )
		{
			// Send start signal.
			new Receiver(start_playing, test_file);
			
			// Wait for three seconds before repeating.
			synchronized (Thread.currentThread())
			{
				try {
					Thread.currentThread().wait(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Send stop signal every loop...
			new Receiver(stop_playing, null);
		}
		
		setFailed(false);
	}
	
	public static void main(String[] args)
	{
		new OOPinitiator();
	}
}
