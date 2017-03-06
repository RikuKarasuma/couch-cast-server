package net.eureka.couchcast.foundation.file.manager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.eureka.couchcast.foundation.file.media.MediaFile;
import net.eureka.couchcast.gui.playlist.PlaylistViewer;
import net.eureka.couchcast.mediaserver.NetworkHandler;

/**
 * A factory pattern for media files. The media file data is split between lists that go as follows:
 * name, path, size, file size, alphabetically sorted and size descending sorted. Another list called
 * {@link DeepSearchIndex} is managed by another class for efficiency and to help prevent bloating.
 * <br>
 * <br>
 * Here new lists are created and used to be displayed in the play-list via the GUI using {@link PlaylistViewer}
 * or to be sent to each connected client via {@link NetworkHandler}.
 * <br>
 * <br>
 * Each list handles the data as byte[] in order to cut down on Object memory consumption.
 * 
 * @author Owen McMonagle.
 * 
 * @see DirectoryFactory
 * @see DirectoryScanner
 * @see DeepSearchIndex
 * @see PlaylistViewer
 * @see NetworkHandler
 * @see MediaFile
 *
 * @version 0.3
 */
public final class FileFactory 
{
	private static final List<byte[]> MEDIA_NAME_LIST = Collections.synchronizedList(new ArrayList<byte[]>()),
									  MEDIA_PATH_LIST = Collections.synchronizedList(new ArrayList<byte[]>()),
									  MEDIA_SIZE_LIST = Collections.synchronizedList(new ArrayList<byte[]>()),
									  MEDIA_FILE_SIZE_LIST = Collections.synchronizedList(new ArrayList<byte[]>()),
									  MEDIA_ALPHA_SORT_LIST = Collections.synchronizedList(new ArrayList<byte[]>()),
									  MEDIA_SIZE_SORT_LIST = Collections.synchronizedList(new ArrayList<byte[]>());
	
	
	public synchronized static int addMediaFile(byte[] name, byte[] path, byte[] size_str, byte[] file_size, boolean deep_search)
	{	
		// Add file name to name list.
		MEDIA_NAME_LIST.add(name);
		
		// Calculate index for deep search.
		final int index = (MEDIA_NAME_LIST.size()-1);
		// Set deep search boolean flag.
		DeepSearchIndex.add(index, deep_search);
		
		// Add file path to path list.
		MEDIA_PATH_LIST.add(path);
		// Add file size string to size list.
		MEDIA_SIZE_LIST.add(size_str);
		
		// Add file size bytes to file size list for sorting.
		MEDIA_FILE_SIZE_LIST.add(file_size);
		
		// Return global index for file; in case needed.
		return index;
	}
	
	public synchronized static MediaFile getMediaFile(int index) throws FileNotFoundError
	{
		final int size = MEDIA_NAME_LIST.size();
		MediaFile file = null;
		
		if(index > size)
			throw new FileNotFoundError();
		else
			file = new MediaFile(MEDIA_NAME_LIST.get(index), MEDIA_PATH_LIST.get(index), MEDIA_SIZE_LIST.get(index));
		
		return file;
	}
	
	public synchronized static byte[] getMediaName(int index) throws FileNotFoundError
	{
		final int size = MEDIA_NAME_LIST.size();
		if(index > size)
			throw new FileNotFoundError();
		else
			return MEDIA_NAME_LIST.get(index);
	}
	
	public synchronized static byte[] getMediaPath(int index) throws FileNotFoundError
	{
		final int size = MEDIA_NAME_LIST.size();
		if(index > size)
			throw new FileNotFoundError();
		else
			return MEDIA_PATH_LIST.get(index);
	}
	
	public synchronized static byte[] getMediaSize(int index) throws FileNotFoundError
	{
		final int size = MEDIA_NAME_LIST.size();
		if(index > size)
			throw new FileNotFoundError();
		else
			return MEDIA_SIZE_LIST.get(index);
	}
	
	public synchronized static int getListSize()
	{
		return MEDIA_NAME_LIST.size();
	}
	
	public synchronized static int indexOf(byte[] info) throws FileNotFoundError
	{
		if(MEDIA_NAME_LIST.contains(info))
			return MEDIA_NAME_LIST.indexOf(info);
		else if(MEDIA_PATH_LIST.contains(info))
			return MEDIA_PATH_LIST.indexOf(info);
		else if(MEDIA_SIZE_LIST.contains(info))
			return MEDIA_SIZE_LIST.indexOf(info);
		else
			throw new FileNotFoundError();
	}
	
	public synchronized static void removeByPaths(List<byte[]> paths)
	{
		for(byte[] path : paths)
			removeByPath(path);
	}
	
	private synchronized static void removeByPath(byte[] path)
	{
		int to_remove = 0;
		
		if(!MEDIA_PATH_LIST.contains(path))
			for(int i = 0; i < MEDIA_PATH_LIST.size(); i ++ )
			{
				byte[] compare_path = MEDIA_PATH_LIST.get(i);
				if(compare_path.equals(path))
					to_remove = i;
			}
		else
			to_remove = MEDIA_PATH_LIST.indexOf(path);
		
		remove(to_remove);
	}
	
	public synchronized static void removeAll(ArrayList<Integer> files)
	{
		
		int offset = 0;
		for(int index : files)
		{
			index = index - offset;
			remove(index);
			offset = offset + 1;
		}
		
		
	}
	
	private synchronized static void remove(int index)
	{
		if(index < getListSize())
			try
			{
				MEDIA_NAME_LIST.remove(index);
				MEDIA_PATH_LIST.remove(index);
				MEDIA_SIZE_LIST.remove(index);
				
				MEDIA_FILE_SIZE_LIST.remove(index);
			}
			catch(IndexOutOfBoundsException e)
			{
				System.err.println("Cound not remove by index No. "+index);
			}
	}
	
	/**
	 * Verifies that the passed media file parameter is not a duplicate on the
	 * media play-list.
	 * @param byte[] possible_duplicate - Media file path to verify. 
	 * @return Boolean - True if duplicate, false otherwise.
	 */
	public synchronized static boolean isDuplicate(byte[] possible_duplicate)
	{
		// Iterate through each file on the play-list...
		for(byte[] paths : MEDIA_PATH_LIST)
			// Compare media files by using file path....
			if(compareMediaFiles(paths, possible_duplicate))
				// Return true if file is matched and thus a duplicate.
				return true;
				
	
		// Return false if the file was not found on the list.
		return false;
		
		
	}
	
	/**
	 * Attempts to compare two passed media files by using their paths. Returns true if both
	 * files contain the same path.
	 * @param byte[] compare_0 - First media file path to compare.
	 * @param byte[] compare_1 - Second media file path to compare.
	 * @return Boolean - True if paths matched, false otherwise.
	 */
	private synchronized static boolean compareMediaFiles(byte[] compare_0, byte[] compare_1)
	{
		// Compare path strings, true if both files matched.
		return Arrays.equals(compare_0, compare_1);
	}
	
	/**
	 * Used to determine the index of a media file passed, that is in the play-list.
	 * @param byte[] compare - Path to compare for index. 
	 * @return Integer - Index of the passed media file on the play-list, will return -1 if no such file exists.
	 */
	public synchronized static int compareMediaFilesForIndex(byte[] compare)
	{
		// If file is not null...
		if(compare != null)
		{
			// Iterate through play-list media files...
			for(int i = 0; i < MEDIA_NAME_LIST.size(); i ++)
				// Compare media files for a match.... 
				if(compareMediaFiles(compare, MEDIA_PATH_LIST.get(i)))
					// If both files match return index of the matched files on the play-list.
					return i;
		}
		// Return -1 for no files matched.
		return -1;
	}
	
	/**
	 * Creates a 2D Vector table populated with all the media files information for use
	 * within the MediaViewer.
	 * 
	 * @return Vector of a Vector of Strings - 2D table of the media play-lists files and information.
	 */
	public synchronized static byte[][][] getMediaPlaylistVectors()
	{
		// Retrieve the play-list size.s
		final int list_size = MEDIA_NAME_LIST.size();
		// Create columns and rows.
		byte[][][] rows = new byte[list_size][][];
		// Create column data reference.
		byte[][] column_data = null;
		
		// If media files exist...
		if(list_size != 0)
			// Iterate through each play-list file...
			for(int i = 0; i < list_size; i++)
			{
				// Retrieve column data associate with that media file.
				column_data = parseMediaFileToList( i);
				// Add row to table.
				rows[i] = column_data;
			}
		// If media files do not exist...
		//else
			// Add default no files found row.
			//rows.add(getDefaultListVector());
		
		// Return 2D table of the play-list files.
		return rows;
	}
	
	/**
	 * Retrieves the data from a single media file and inserts it into a 1D vector for 
	 * use in a 2D table. 
	 * @param MediaFile single_row - Media file to extract data from. 
	 * @param Integer row_number - Row number to be inserted for use in the table. 
	 * @return Vector of Strings - Data retrieved from the media file.
	 */
	private synchronized static byte[][] parseMediaFileToList(int row_number)
	{
		// Return table row with populated data.
		return new byte[][] { MEDIA_NAME_LIST.get(row_number), MEDIA_PATH_LIST.get(row_number), MEDIA_SIZE_LIST.get(row_number), String.valueOf(row_number).getBytes() };
	}
	
	/**
	 * Retrieves a copy of the media play-list.
	 * @return ArrayList of MediaFiles - Copy of the original play-list.
	 */
	public synchronized static ArrayList<MediaFile> getMediaPlaylist() 
	{
		ArrayList<MediaFile> list = new ArrayList<MediaFile>();
		for(int i = 0; i < MEDIA_NAME_LIST.size(); i++)
			try
			{
				list.add(getMediaFile(i));
			} 
			catch (FileNotFoundError e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return list;
	}
	
	public static ArrayList<byte[]> cloneMediaPaths()
	{
		return new ArrayList<byte[]>(MEDIA_PATH_LIST);
	}
	
	public synchronized static boolean validateInternalFileStructure(boolean is_deep_search)
	{
		boolean needs_update = false;
		int arrays_size = MEDIA_NAME_LIST.size();
		if(arrays_size > 0)
		{
			ArrayList<Integer> files_to_purge = new ArrayList<Integer>();
			for(int i = 0; i < arrays_size; i++)
			{
				boolean is_file_deep_search_aquired = DeepSearchIndex.get(i);
				if(!is_deep_search && is_file_deep_search_aquired)
					files_to_purge.add(i);
			}
			
			if(files_to_purge.size() > 0)
			{
				removeAll(files_to_purge);
				needs_update = true;
			}
		}
		
		if(!isAlphaSynced() || !isSizeSynced())
		{
			DirectoryFactory.handleSortScheduling();
			needs_update = true;
		}
			
		return needs_update;
	}
	
	public synchronized static void setAlphaList(ArrayList<byte[]> new_sorted_list)
	{
		MEDIA_ALPHA_SORT_LIST.clear();
		MEDIA_ALPHA_SORT_LIST.addAll(new_sorted_list);
	}
	
	public synchronized static ArrayList<Integer> getAlphaList()
	{
		return convertToIntegerList(MEDIA_ALPHA_SORT_LIST);
	}
	
	public synchronized static boolean isAlphaSynced()
	{
		return (MEDIA_PATH_LIST.size() == MEDIA_ALPHA_SORT_LIST.size());
	}
	
	public synchronized static void setSizeList(ArrayList<byte[]> new_sorted_list)
	{
		MEDIA_SIZE_SORT_LIST.clear();
		MEDIA_SIZE_SORT_LIST.addAll(new_sorted_list);
	}
	
	public synchronized static boolean isSizeSynced()
	{
		return (MEDIA_PATH_LIST.size() == MEDIA_SIZE_SORT_LIST.size());
	}
	
	public synchronized static ArrayList<Integer> getSortedSizeList()
	{ 
		return convertToIntegerList(MEDIA_SIZE_SORT_LIST);
	}
	
	public synchronized static ArrayList<byte[]> getNameList()
	{
		return new ArrayList<byte[]>(MEDIA_NAME_LIST);
	}
	
	public static ArrayList<byte[]> getMediaFileSizeList()
	{
		return new ArrayList<byte[]>(MEDIA_FILE_SIZE_LIST);
	}
	
	private static ArrayList<Integer> convertToIntegerList(List<byte[]> to_convert)
	{
		ArrayList<Integer> converted_list = new ArrayList<>();
		for(int i = 0; i < to_convert.size(); i++)
			converted_list.add(ByteBuffer.wrap(to_convert.get(i)).getInt());
		
		return converted_list;
	}
	
}
