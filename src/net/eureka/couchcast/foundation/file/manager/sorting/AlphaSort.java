package net.eureka.couchcast.foundation.file.manager.sorting;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;
import net.eureka.couchcast.foundation.file.manager.FileFactory;
import net.eureka.couchcast.mediaserver.NetworkHandler;

/**
 * Sorts media files alphabetically. Creates a sorted list by organizing the indexes of the original list. 
 * The list is then set in the {@link FileFactory} and the {@link NetworkHandler} is used to update the 
 * clients with the new list.
 * 
 * @author Owen McMonagle.
 * 
 * @see Sorter
 * @see SortImpl
 * @see FileFactory
 * @see NetworkHandler
 * @see DirectoryFactory
 * 
 * @version 0.1
 */
public final class AlphaSort extends Sorter 
{
	
	/**
	 * Sorts media files alphabetically. Creates a sorted list by organizing the indexes of the original list. 
	 * The list is then set in the {@link FileFactory} and the {@link NetworkHandler} is used to update the 
	 * clients with the new list.
	 */
	@Override
	public void sort()
	{
		ArrayList<byte[]> names = FileFactory.getNameList();
		ArrayList<String> casted_names = new ArrayList<>(names.size());
		// Create integer index list.
		final ArrayList<byte[]> index_list = new ArrayList<>();
		// Retrieve and store the list lengths.
		final int length = names.size();
		// Create name list.
		ArrayList<String> sorted_name_list = new ArrayList<>(names.size());
		
		// Retrieve each media file name...
		for( int i = 0; i < length; i++)
		{
			String casted_name = new String(names.get(i));
			// Then store the name in the list.
			sorted_name_list.add(casted_name);
			casted_names.add(i, casted_name);
		}
		// Sort name list alphabetically.
		Collections.sort(sorted_name_list);
		// Iterate through the sorted name list...
		for( int i = 0, index = 0; i < length; i++)
		{
			String sorted_name = sorted_name_list.get(i);
			index = casted_names.indexOf(sorted_name);
			byte[] casted_integer = ByteBuffer.allocate(4).putInt(index).array();
			index_list.add(casted_integer);
		}
		
		FileFactory.setAlphaList(index_list);
		NetworkHandler.signalPlaylistUpdate();
	}
}
