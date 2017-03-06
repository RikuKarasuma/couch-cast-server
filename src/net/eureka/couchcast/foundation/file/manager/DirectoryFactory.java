package net.eureka.couchcast.foundation.file.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.eureka.couchcast.foundation.file.manager.sorting.AlphaSort;
import net.eureka.couchcast.foundation.file.manager.sorting.SizeSort;
import net.eureka.couchcast.foundation.file.manager.sorting.SortImpl;
import net.eureka.couchcast.foundation.file.manager.sorting.Sorter;
import net.eureka.couchcast.foundation.file.media.MediaFile;
import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.gui.tray.Tray;
import net.eureka.couchcast.mediaserver.NetworkHandler;

/**
 * Handles the monitoring of each saved directory. Does this by creating a fixed thread pool based upon the number
 * of available processors minus one, the factory itself used a {@link Thread} too. Each monitored directory is 
 * managed by a {@link DirectoryScanner}, these scanners are worked upon by the thread pool. This allows for 
 * a multithreaded search pattern using a Observer pattern along with a Producer/Consumer pattern. 
 * <br>
 * <br>
 * A while loop constantly manages the observer pattern and assigns any directory not being scanned, as a job 
 * to be run in the future.
 * <br>
 * <br>
 * Sorting of the media files is also a responsibility of the thread pool and uses another observer pattern.
 * <br>
 * <br>
 * Shaving and refactoring needs to occur for the next directory factory version as SRP is getting trampled
 * with the number of responsiblities that this has too manage, cost to maintain is becoming too high. 
 * 
 * 
 * @author Owen McMonagle.
 * 
 * @see DirectoryScanner
 * @see FileFactory
 * @see NetworkHandler
 * @see Tray
 * 
 * @version 0.2
 */
public final class DirectoryFactory extends Thread
{
	private static final int WORKER_POOL_SIZE = Runtime.getRuntime().availableProcessors()-1, SCHEDULED_WORKER_SIZE = 1;
	
	
	/**
	 *  Flag for indicating change to the play-list.
	 */
	private static boolean playlistChanged = false;
	
	private static final ExecutorService WORKERS = Executors.newFixedThreadPool(WORKER_POOL_SIZE);
	private static final ScheduledExecutorService SCHEDULED_WORKER = Executors.newScheduledThreadPool(SCHEDULED_WORKER_SIZE);
	
	private static final List<DirectoryScanner> SCANNERS = Collections.synchronizedList(new ArrayList<DirectoryScanner>());
	private static final List<SortImpl> SORTERS = Collections.synchronizedList(new ArrayList<SortImpl>());
	private static final List<byte[]> PATHS_TO_REMOVE = Collections.synchronizedList(new ArrayList<byte[]>());
	
	private static ArrayList<String> monitoredDirectories = ApplicationGlobals.getMonitoredList();
	
	private boolean discovery = true;
	
	public DirectoryFactory()
	{
		this.setName("File Fetcher V2");
		SCHEDULED_WORKER.scheduleWithFixedDelay(new FileFactoryVerifier(), 20, 20, TimeUnit.SECONDS);
		initialise();
		this.start();
	}
	
	public DirectoryFactory(ArrayList<String> monitored_directories)
	{
		this.setName("File Fetcher V2");
		SCHEDULED_WORKER.scheduleWithFixedDelay(new FileFactoryVerifier(), 20, 20, TimeUnit.SECONDS);
		monitoredDirectories = monitored_directories;
		initialise();
		this.start();
	}
	
	@Override
	public void run() 
	{
		this.waitFor(true);
		while(discovery)
		{
			handleCleanUp();
			handleScanners();
			handleSorters();
			attemptGuiUpdate();
			this.waitFor(false);
		}
	}
	
	
	private void waitFor(boolean start_up)
	{
		final int delay = (start_up) ? 4000 : ApplicationGlobals.getUpdateDelay();
		if(delay > 0)
			synchronized (this)
			{	
				try
				{
					this.wait(delay);
				} 
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
	}
	
	public void cancel()
	{
		discovery = false;
	}
	
	public static void initialise()
	{
		for(String directory : monitoredDirectories)
			if(!isScannerDuplicate(directory))
				SCANNERS.add(new DirectoryScanner(directory));
	}
	
	
	private static void handleCleanUp()
	{
		int clean_up_size = PATHS_TO_REMOVE.size();
		if(clean_up_size > 0)
		{
			FileFactory.removeByPaths(new ArrayList<byte[]>(PATHS_TO_REMOVE));
			PATHS_TO_REMOVE.clear();
			playlistChanged = true;
		}
		
		if(FileFactory.validateInternalFileStructure(ApplicationGlobals.isDeepSearch()))
			playlistChanged = true;
	}
	
	private static void handleScanners()
	{
		ArrayList<DirectoryScanner> finished_scanners = new ArrayList<DirectoryScanner>();
		synchronized (SCANNERS) 
		{
			for(int i = 0; i < SCANNERS.size(); i++)
			{
				DirectoryScanner scanner = SCANNERS.get(i);
				handleScanner(scanner, finished_scanners);
			}
		}
		
		if(finished_scanners.size() > 0)
		{
			SCANNERS.removeAll(finished_scanners);
			finished_scanners.clear();
		}
	}
	
	private static void handleSorters()
	{
		ArrayList<SortImpl> finished_sorters = new ArrayList<SortImpl>();
		synchronized (SORTERS) 
		{
			for(int i = 0; i < SORTERS.size(); i++)
			{
				Sorter sorter = (Sorter) SORTERS.get(i);
				if(!sorter.isFinished() && !sorter.isExecuted())
				{
					if(sorter instanceof AlphaSort)
						WORKERS.execute(sorter);
					else if(sorter instanceof SizeSort)
						WORKERS.execute(sorter);
				}
				else if(sorter.isFinished())
					finished_sorters.add(sorter);
			}
			
			if(finished_sorters.size() > 0)
			{
				SORTERS.removeAll(finished_sorters);
				finished_sorters.clear();
			}
		}
		
	}
	
	private static void handleScanner(DirectoryScanner scanner, ArrayList<DirectoryScanner> finished_scanners)
	{
		if(!scanner.isScanning() && !scanner.isScheduled() && !scanner.isFinished())
		{
			WORKERS.execute(scanner);
			scanner.setScheduled(true);
		}
		if(scanner.isFinished())
			finished_scanners.add(scanner);
	}
	
	/**
	 * Attempts to update the GUI, once a play-list change has been detected.
	 */
	private static void attemptGuiUpdate()
	{
		// If the play-list has been changed...
		if(playlistChanged)
		{
			// Attempt to update the advanced Play-list. 
			Tray.updatePlaylist();
			// Set play-list changed to false after update.
			playlistChanged = false;
			// Handle sorting.
			handleSortScheduling();
			// Signal to the FileServer that each FileHandler needs an update.
			NetworkHandler.signalPlaylistUpdate();
		}
	}
	
	public static void handleSortScheduling()
	{
		//System.out.println("Is sorting:"+isAlphaSorting() + "\nIs synced:" + FileFactory.isAlphaSynced());
		//System.out.println("Path size:"+ FileFactory.getNameList().size() + "\nSort size" + FileFactory.getAlphaList().size());
		if(!isAlphaSorting() && !FileFactory.isAlphaSynced())
			// Schedule new sort.
			SORTERS.add(new AlphaSort());
		if(!isSizeSorting() && !FileFactory.isSizeSynced())
			SORTERS.add(new SizeSort());
	}
	
	private static boolean isAlphaSorting()
	{
		boolean has_alpha = false;
		synchronized (SORTERS) 
		{
			for(int i = 0; i < SORTERS.size(); i ++)
				if(SORTERS.get(i) instanceof AlphaSort)
					has_alpha = true;
		}
		
		return has_alpha;
	}
	
	private static boolean isSizeSorting()
	{
		boolean has_size = false;
		synchronized (SORTERS) 
		{
			for(int i = 0; i < SORTERS.size(); i ++)
				if(SORTERS.get(i) instanceof SizeSort)
					has_size = true;
		}
		
		return has_size;
	}

	/**
	 * Retrieves the File a MediaFile is associated with, using the path.
	 * @param MediaFile media_file_info - Media file representation of the file wanted.
	 * @return File - Returns File if a media file is associated, null otherwise.
	 */
	public static File getMediaFile(final MediaFile media_file_info)
	{
		// Attempt to retrieve file associated with the media file object.
		final File media_file = new File(media_file_info.getLocation().toString());
		// Returns the file if it is not a folder and not null. Null otherwise.
		return (media_file != null && media_file.isFile()) ? media_file : null ;
	}
	
	public synchronized static void signalPlaylistChanged()
	{
		playlistChanged = true;
	}
	
	private synchronized static boolean isScannerDuplicate(String dir)
	{
		for(int i = 0; i < SCANNERS.size(); i++)
			if(dir.equals(SCANNERS.get(i).getDir()))
				return true;
		
		return false;
	}
	
	public synchronized static void addNewScanner(String dir, List<File> passed_dir_files)
	{
		final boolean deep_search_directory = true;
		if(!isScannerDuplicate(dir))
			SCANNERS.add(new DirectoryScanner(dir, passed_dir_files, deep_search_directory));
	}
	
	public synchronized static void addNewScanner(String dir)
	{
		if(!isScannerDuplicate(dir))
			SCANNERS.add(new DirectoryScanner(dir));
	}
	
	public synchronized static void addPathsForCleanUp(List<byte[]> paths)
	{
		for(int i = 0; i < paths.size(); i ++)
		{
			int length = 0;
			byte[] to_copy = paths.get(i), copy_into = new byte[(length = to_copy.length)];
			
			System.arraycopy(to_copy, 0, copy_into, 0, length);
			PATHS_TO_REMOVE.add(copy_into);
		}
	}
	
	public static void resetScanners()
	{
		SCANNERS.clear();
	}
	
	public static void reinitialize()
	{
		SCANNERS.clear();
		initialise();
	}
}
