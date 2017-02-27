package net.eureka.couchcast.tests;

import java.util.ArrayList;

import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;
import net.eureka.couchcast.foundation.file.manager.FileFactory;

/**
 * Tests the {@link DirectoryFactory} by passing a directory to it for scanning and waits for 15 seconds
 * until displaying the found files.
 * @author Garak
 *
 */
public final class DirectoryFactoryMonitor extends TestBase
{
	public DirectoryFactoryMonitor(boolean exit_after) 
	{
		// Create ArrayList for directories.
		final ArrayList<String> test_directories = new ArrayList<String>();
		// Create directory.
		final String test_directory = "E:\\Media\\Unsorted";
		// Add directory to list.
		test_directories.add(test_directory);
		
		
		// Create factory.
		final DirectoryFactory factory = new DirectoryFactory(test_directories);
		
		// Main thread for waiting.
		final Thread main = Thread.currentThread();
		
		synchronized (main)
		{
			// Time to wait until displaying found files.
			final int time_to_wait = 15000;
			try 
			{
				// Wait for a while.
				main.wait(time_to_wait);
			}
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Stop the factory.
		factory.cancel();
		
		// Get list size.
		final int size = FileFactory.getListSize();
		
		// Print number of files found.
		System.out.println("# of discovered files: " + size);
		// Iterate over the name list of all found files....
		for(byte[] name:  FileFactory.getNameList())
			// Print each discovery media name.
			System.out.println(new String(name));
		
		// If size greater than zero set test not failed.
		setFailed(!(size > 0));
		
		// Check if instructed to exit after...
		if(exit_after)
			// Exit JVM.
			System.exit(0);
	}
	
	
	public static void main(String[] args)
	{
		new DirectoryFactoryMonitor(true);
	}
}
