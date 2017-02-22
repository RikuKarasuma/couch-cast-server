package net.eureka.androidcast.foundation.init;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.eureka.utils.SystemDetect;
import net.eureka.utils.Systems;

/**
 * Abstract class so it can't be Instantiated. Contains global application variables such as the operating system, application directory (\<USER>\PCCast\),
 * log encoding and the JVM process ID.
 * 
 * @author Owen McMonagle.
 *
 */
public final class ApplicationGlobals 
{
	/**
	 * Detects the operating system.
	 */
	private static final Systems OPERATING_SYSTEM = SystemDetect.detect();
	
	/**
	 * Name of the program.
	 */
	private static final byte[] name = "Couch Cast".getBytes();
	
	private static final String INSTALL_DIRECTORY =  File.separator + new String(name) + File.separator;
	
	private static final String INSTALL_DIRECTORY_PATH = // (Static.is64BitArch()) ? System.getenv("PROGRAMFILES") + 
			System.getenv("SYSTEMDRIVE") + File.separator + "Program Files" + INSTALL_DIRECTORY;
	
	/**
	 * Version of the program.
	 */
	private static final byte[] version = "ver_1.2".getBytes(); 
	
	/**
	 * Application directory. (\<Users>\Android Cast\). This stores the configuration file, server log, SHA key and the default download directory.
	 */
	private static byte[] applicationDirectory = new byte[]{};
			
	/**
	 * Default Monitored directory. Can be replaced by the configuration file or user.
	 */
	private static ArrayList<String> monitoredDirectories = new ArrayList<String>();
	
	/**
	 * Log encoding, must be UTF to support multiple languages.
	 */
	private static final String LOG_ENCODING = new String("UTF-8");
	
	/**
	 * Process ID, this is used to help shutdown the server by being printed to configuration file on start up.
	 */
	private static String processId = ManagementFactory.getRuntimeMXBean().getName().toString().split("@")[0];
	
	private static int searchDelay = 50, updateDelay = 2000;
	
	/**
	 * Used to determine whether or not to search the folders within the monitored directory used by the {@link DirectoryFactory} class. 
	 */
	private static boolean deepSearch = false;
	
	/**
	 * Used by the {@link MediaPlayer} to decide whether or not to minimize all other desktop windows when the play button is pressed. 
	 */
	private static boolean minimizeWindows = true;
	
	private static boolean musicMode = false;
	
	/**
	 * Retrieves the Monitored directory.
	 * @return String - Monitored directory.
	 */
	public static String getMonitoredDirectory(int index) 
	{
		try
		{
			return monitoredDirectories.get(index);
		}
		catch(IndexOutOfBoundsException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	
	public static void setMonitoredList(String combined)
	{
		if(combined.contains("=~="))
		{
			String[] split = combined.split("=~=");
				if(split.length > 1)
					for(int i = 0; i < split.length; i++)
						if(!isDirectoryAdded(split[i]))
							monitoredDirectories.add(split[i]);
		}
		else if(combined.length() > 0)
			monitoredDirectories.add(combined);
	}
	
	public static ArrayList<String> getMonitoredList()
	{
		return monitoredDirectories;
	}
	
	public static int getMonitoredSize()
	{
		return monitoredDirectories.size();
	}
	
	public static String getCombinedDirectoryList()
	{
		String combined = "";
		try
		{
			combined = monitoredDirectories.get(0);
			for(int i = 1; i < monitoredDirectories.size(); i++)
				combined = combined + "=~=" + monitoredDirectories.get(i);
			
		}
		catch(IndexOutOfBoundsException e)
		{
			
		}
		
		return combined;
	}
	
	public static String getLastAddedDirectory()
	{
		try
		{
			return monitoredDirectories.get(monitoredDirectories.size()-1);
		}
		catch(IndexOutOfBoundsException e)
		{
			e.printStackTrace();
		}
		return "N/A";
	}
	
	private static boolean isDirectoryAdded(String dir)
	{
		return monitoredDirectories.contains(dir);
	}

	/**
	 * Sets the new default Monitored directory.
	 * @param String Monitored - New default Monitored directory. 
	 */
	public static void addMonitoredDirectory(String monitored_directory) 
	{
		// If StringBuffer is not null...
		if(monitored_directory != null)
			// set the new download directory.
			monitoredDirectories.add(monitored_directory);
	}
	
	/**
	 * Sets the default application directory. By default  (\<USER>\PCCast\).
	 * @param String application_directory - Application directory which contains the configuration/log/sha files.
	 */
	public static void setApplicationDirectory(String application_directory)
	{
		// If StringBuffer is not null...
		if(application_directory != null)
			// set application directory.
			ApplicationGlobals.applicationDirectory = application_directory.getBytes();
	}
	
	/**
	 * Retrieve the application directory path.
	 * @return String - Application directory path.
	 */
	public static String getApplicationDirectory()
	{
		return new String(applicationDirectory);
	}

	/**
	 * Retrieves the operating system type.
	 * @return Systems - Operating system type. 
	 */
	public static Systems getOperatingSystem() 
	{
		return OPERATING_SYSTEM;
	}

	/**
	 * Encoding for the logger. UTF-8.
	 * @return String - UTF-8 log encoding.
	 */
	public static String getEncoding()
	{
		return LOG_ENCODING;
	}
	
	/**
	 * JVM process ID. Used for the termination of the server via the PC-Cast Process Killer.jar in the event of an update,
	 * or un-install.
	 * @return String - Process ID of the JVM running the server.
	 */
	public static String getProcessID()
	{
		return processId;
	}

	public static String getName()
	{
		return new String(name);
	}
	
	public static String getVersion() 
	{
		return new String(version);
	}
	
	public static String getNameAndVersion()
	{
		return new String(name)+" "+new String(version);
	}
	
	public synchronized static boolean isDeepSearch() 
	{
		return deepSearch;
	}

	public synchronized static void setDeepSearch(boolean deepSearch) 
	{
		ApplicationGlobals.deepSearch = deepSearch;
	}

	public static boolean isMinimizeWindows() 
	{
		return minimizeWindows;
	}

	public static void setMinimizeWindows(boolean minimizeWindows) 
	{
		ApplicationGlobals.minimizeWindows = minimizeWindows;
	}
	
	public static String getInstallPath()
	{
		return INSTALL_DIRECTORY_PATH;
	}

	public static boolean isMusicMode() 
	{
		return musicMode;
	}

	public static void setMusicMode(boolean musicMode)
	{
		ApplicationGlobals.musicMode = musicMode;
	}
	
	public static byte[][][] getFolderVectors()
	{
		// Retrieve the play-list size.
		final int size = monitoredDirectories.size();
		// Create columns and rows.
		byte[][][] rows = new byte[size][][];
		// Create column data reference.
		byte[][] column_data = null;
		// Create media file reference for a single row.
		File single_row = null;
		// If media files exist...
		if(size != 0)
			// Iterate through each play-list file...
			for(int i = 0; i < size; i++)
			{
				// Retrieve a media file for a single row. 
				single_row = new File(monitoredDirectories.get(i));
				// Retrieve column data associate with that media file.
				column_data = parseMediaFolderToList(single_row, i);
				// Add row to table.
				rows[i] = column_data;
			}
		
		// Return 2D table of the play-list files.
		return rows;
	}
	
	private static byte[][] parseMediaFolderToList(final File folder, int index)
	{
		
		final int int_size = 4;
		ByteBuffer buffer = ByteBuffer.allocate(int_size).putInt(index);
		// Return table row with populated data.
		return new byte[][] { folder.getName().getBytes(), folder.getPath().getBytes(), buffer.array()};
	}

	public static int getSearchDelay()
	{
		return searchDelay;
	}

	public static void setSearchDelay(float searchDelay) 
	{
		ApplicationGlobals.searchDelay = (int) searchDelay;
	}
	
	public static void setSearchDelay(int searchDelay) 
	{
		ApplicationGlobals.searchDelay = searchDelay;
	}

	public static int getUpdateDelay()
	{
		return updateDelay;
	}

	public static void setUpdateDelay(float updateDelay) 
	{
		ApplicationGlobals.updateDelay = (int) updateDelay;
	}
}
