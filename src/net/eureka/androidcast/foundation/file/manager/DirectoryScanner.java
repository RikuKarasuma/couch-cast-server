package net.eureka.androidcast.foundation.file.manager;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.DirectoryStream.Filter;
import java.util.ArrayList;
import java.util.List;

import net.eureka.androidcast.Static;
import net.eureka.androidcast.foundation.file.media.MediaFile;
import net.eureka.androidcast.foundation.file.media.MediaVerifier;
import net.eureka.androidcast.foundation.init.ApplicationGlobals;
import net.eureka.androidcast.foundation.logging.Logger;
import net.eureka.androidcast.gui.lang.LanguageDelegator;
import net.eureka.androidcast.gui.lang.Languages;

public final class DirectoryScanner implements Runnable
{
	private static final DirectoryStream.Filter<Path> DIRECTORY_FILTER = new Filter<Path>()
    {
		private final String ENCODING = ApplicationGlobals.getEncoding();
		
		@Override
		public boolean accept(Path entry) throws IOException 
		{
			// Get file to verify.
			File entry_file = entry.toFile();
			// If the file is not a directory...
			if(!entry_file.isDirectory())
				// If the file is not a duplicate...
				if(!FileFactory.isDuplicate(entry_file.getAbsolutePath().getBytes(ENCODING)))
				{
					// Get file name.
					final String file_name = entry.getFileName().toString();
					// Return true or false depending if the Media extension is valid.
					return MediaVerifier.isFileValid(file_name);
				}
				// If the file is a duplicate...
				else
					// Return false to ignore file.
					return false;
			// If the file is a directory...
			else
				// Return true to retrieve file directory.
				return true;
		}
	};
	
	
	private static final String VALID_EXTENSION_LIST = MediaVerifier.getExtensionList();
	private static int globalSerial = 0;
	
	private int serial = 0;
	
	private ArrayList<byte[]> validationPaths = new ArrayList<byte[]>();
	private ArrayList<byte[]> fileLengths = new ArrayList<byte[]>();
	private ArrayList<MediaFile> foundMediaFiles = new ArrayList<MediaFile>();
	private List<File> passedFiles = null;
	private boolean scanning = false, finished = false, scheduled = false, isDeepSearchDir = false;
	private String directory = "";
	
	
	public DirectoryScanner(String dir_to_scan) 
	{
		this.directory = dir_to_scan;
		globalSerial++;
		this.serial = globalSerial;
	}
	
	public DirectoryScanner(boolean is_deep_search_dir, String dir_to_scan) 
	{
		this.isDeepSearchDir = is_deep_search_dir;
		this.directory = dir_to_scan;
		globalSerial++;
		this.serial = globalSerial;
	}
	
	public DirectoryScanner(String dir_to_scan, List<File> passed_files_to_scan) 
	{
		this.directory = dir_to_scan;
		this.passedFiles = passed_files_to_scan;
		globalSerial++;
		this.serial = globalSerial;
	}
	
	public DirectoryScanner(String dir_to_scan, List<File> passed_files_to_scan, boolean is_deep_search_dir)
	{
		this.isDeepSearchDir = is_deep_search_dir;
		this.directory = dir_to_scan;
		this.passedFiles = passed_files_to_scan;
		globalSerial++;
		this.serial = globalSerial;
	}
	
	public DirectoryScanner(String dir_to_scan, boolean start) 
	{
		this.directory = dir_to_scan;
		globalSerial++;
		this.serial = globalSerial;
		if(start)
			new Thread(this).start();
	}
	
	public void run() 
	{
		// DEBUG System.out.println("Scanner No. " + serial + " running."+"\n"+this.directory);
		boolean deep_search = ApplicationGlobals.isDeepSearch();
		if(deep_search || (!deep_search && !this.isDeepSearchDir))
		{
			
			this.scanning = true;
			if(this.passedFiles != null)
				scanFiles();
			else if(doesFileExist(directory.getBytes()))
				scanDir();
			else
				this.clean();
			this.scanning = false;
			
			if(!foundMediaFiles.isEmpty())
			{
				for(int i = 0; i < foundMediaFiles.size(); i++)
					FileFactory.addMediaFile(foundMediaFiles.get(i).getName(), foundMediaFiles.get(i).getLocation(), foundMediaFiles.get(i).getSize(), fileLengths.get(i), this.isDeepSearchDir);
				
				foundMediaFiles.clear();
			}
		}
		else
			this.clean();
		
		this.scheduled = false;
	}
	
	private void scanDir()
	{
		this.load();
		this.finished = verifyExistanceOfFiles(validationPaths);
	}
	
	private void scanFiles()
	{
		this.iterateThroughDir(passedFiles);
		this.finished = verifyExistanceOfFiles(validationPaths);
		this.passedFiles = null;
	}
	
	private void clean()
	{
		if(validationPaths.size() > 0)
		{
			DirectoryFactory.addPathsForCleanUp(validationPaths);
			validationPaths.clear();
		}
		finished = true;
	}

	public boolean isScanning()
	{
		return this.scanning;
	}
	
	public int getSerial()
	{
		return this.serial;
	}
	
	public String getDir()
	{
		return this.directory;
	}
	
	public boolean isFinished()
	{
		return finished;
	}
	
	public void setScheduled(boolean is_scheduled)
	{
		this.scheduled = is_scheduled;
	}
	
	public boolean isScheduled()
	{
		return this.scheduled;
	}


//	/**
//	 * Attempts to load the default download directory specified within ApplicationGlobals.
//	 */
//	private static void loadDefaultDirectory()
//	{
//		final ArrayList<String> monitored_list = ApplicationGlobals.getMonitoredList();
//		// Load with default directory.
//		for(int i = 0; i < monitored_list.size(); i++)
//		{
//			load(monitored_list.get(i));
//			delay(true);
//		}
//	}

	/**
	 * Attempts to load all files within the path directory specified as a parameter. Once all
	 * files have been gathered, each is iterated through and verified with the conditions that 
	 * the file is completely written, not a directory and is a valid media file that VLC can 
	 * play. Once those conditions are met, the file is then checked against the list for duplicates.
	 * Finally, if the file is not a duplicate; it will then be added to the media list.
	 * 
	 * Logs each file loaded.
	 * 
	 * Attempts to update MediaViewer (Settings play-list) after each load.
	 * 
	 * @param String directory - Directory to load the media files from.
	 */
	//private static void load(String directory)
	private void load()
	{
		// Gather each file within the specified directory.
		ArrayList<File> potential_media_files = new ArrayList<File>();
		try
		{
			// Store potential_media_files within list.
			potential_media_files.addAll(retrieveFilesAsList(this.directory, VALID_EXTENSION_LIST));
			iterateThroughDir(potential_media_files);
		}
		// If files can not be loaded..
		catch(NullPointerException e)
		{
			// Note in log that no files can be found.
			Logger.append(new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.NO_MEDIA_FILES_LOCATED)));
		}
	}
	
	private void iterateThroughDir(List<File> potential_media_files)
	{
		// Create file instance.
		File potential_media_file = null;
		// Create list limit.
		int potential_media_files_limit = potential_media_files.size(), scan_count = 0;
		// Flag to indicate whether or not files were added to FileFactory.
		boolean files_added = false;
		// Iterate through each file gathered...
		for(int i = 0; i < potential_media_files_limit; i++)
		{
			//Delay
			delay();
			// Get a potential media file.
			potential_media_file = potential_media_files.get(i);
			// Perform media file checks.
			files_added = handlePotentialMediaFile(potential_media_file, scan_count);
			// Check if the number of files scanned equal to 25.
			handleScanCount(scan_count);
		}
		// If files were added...
		if(files_added)
			// Display that a change has been made to the playlist.
			DirectoryFactory.signalPlaylistChanged();
		
		//System.out.println("List size: "+FileFactory.getListSize());
	}
	
	private boolean handlePotentialMediaFile(File potential_media_file, int scan_count)
	{
		// If the file is not a directory, a file vlc can play and not a duplicate....
		// && MediaVerifier.isFileValid(potential_media_file)
		if(!potential_media_file.isDirectory()) // //&& isCompletelyWritten(file)
		{
			// Get file path and add to FileFactory list.
			byte[] path = handleNewFile(potential_media_file, isDeepSearchDir);
			if(path != null)
			{
				// Add new path for validation checking.
				validationPaths.add(path);
				// Increase scan count.
				scan_count = scan_count + 1;
				// Flag to signal that the playlist needs updated.
				return true;
			}
		}
		// If the media file is a directory...
		else if(ApplicationGlobals.isDeepSearch())
			handleNewDirectory(potential_media_file);
		
		return false;
	}
	
	private byte[] handleNewFile(File potential_media_file, boolean deep_search)
	{
		final String encoding = ApplicationGlobals.getEncoding();
		byte[] path = null;
		try
		{
			path = potential_media_file.getAbsolutePath().getBytes(encoding);
			//System.out.println(new String(path));
			// Create a new media file and add to list.
			foundMediaFiles.add(new MediaFile(potential_media_file.getName().getBytes(encoding), path, Static.byteCalculator(potential_media_file.length()).getBytes(encoding)));
			fileLengths.add(Static.longToBytes(potential_media_file.length()));
			//FileFactory.addMediaFile(potential_media_file.getName().getBytes(encoding), path, Static.byteCalculator(potential_media_file.length()).getBytes(encoding), deep_search);
		} 
		catch (UnsupportedEncodingException e) 
		{
			// Note in log that no files can be found.
			Logger.append(new StringBuffer("UnsupportedEncodingException exception"));
		}
		return path;
	}
	
	private static void handleNewDirectory(File directory)
	{ 
		String dir_path = directory.getAbsolutePath();
		List<File> passed_files = retrieveFilesAsList(dir_path, VALID_EXTENSION_LIST); 
		int size = passed_files.size();
		if(size > 0)
			// Add new Directory scanner if new directory.
			DirectoryFactory.addNewScanner(dir_path, passed_files);
	}
	
	private static void handleScanCount(int scan_count)
	{
		if(scan_count == 25)
		{
			DirectoryFactory.signalPlaylistChanged();
			scan_count = 0;
		}
	}
	
	private static List<File> retrieveFilesAsList(final String location,final String extension_list_filter)
	{
		List<File> list = new ArrayList<File>();
		Path dir = FileSystems.getDefault().getPath( location );
		
	    DirectoryStream<Path> stream = null;
		try 
		{
			stream = Files.newDirectoryStream(dir, DIRECTORY_FILTER);
			for (Path path : stream) 
				list.add(path.toFile());
		}
		catch (FileSystemException e)
		{
			// Ignore monitored directory not found.
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    finally 
	    {
	    	try 
	    	{
				stream.close();
			} 
	    	catch (IOException e) 
	    	{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		return list;
	}
	
	private static void delay()
	{
		final int search_delay = ApplicationGlobals.getSearchDelay();
		if(search_delay > 0)
			synchronized (Thread.currentThread())
			{
				try 
				{
					Thread.currentThread().wait(search_delay);
				}
				catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	
	/**
	 * Responsible for removing files that no longer exist from the play-list.
	 */
	private static boolean verifyExistanceOfFiles(ArrayList<byte[]> paths)
	{
		final int playlist_size = paths.size();
		// If the media play-list has files...
		if(playlist_size > 0)
		{
			// Create a list to store the references that need to be removed.
			//ArrayList<Integer> files_that_no_longer_exist = new ArrayList<Integer>();
			ArrayList<byte[]> file_paths_that_no_longer_exist = new ArrayList<byte[]>();
			// Iterate through each media file within the play-list...
			for(int index = 0; index < playlist_size; index++ )
			{
				byte[] media_file_path = paths.get(index);
				// If file doesn't not exist anymore...
				if(!doesFileExist(media_file_path))
					// Add media file to list for removal.
					file_paths_that_no_longer_exist.add(media_file_path);
			}
			int number_of_files_removed = file_paths_that_no_longer_exist.size();
			if(number_of_files_removed > 0)
			{
				// Remove all files that no longer exist.
				DirectoryFactory.addPathsForCleanUp(file_paths_that_no_longer_exist);
				paths.removeAll(file_paths_that_no_longer_exist);
				
				if(paths.size() == file_paths_that_no_longer_exist.size())
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Verifies if the file exists.
	 * @param File file - File path to verify.
	 * @return Boolean - True if exists, false otherwise.
	 */
	public static boolean doesFileExist(byte[] file)
	{
		try 
		{
			return new File(new String(file, "UTF-8")).exists();
		}
		catch (UnsupportedEncodingException e) 
		{
			return false;
		}
	}
}
