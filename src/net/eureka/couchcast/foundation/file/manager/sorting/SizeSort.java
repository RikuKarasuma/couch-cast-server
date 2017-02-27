package net.eureka.couchcast.foundation.file.manager.sorting;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import net.eureka.couchcast.Static;
import net.eureka.couchcast.foundation.file.manager.FileFactory;
import net.eureka.couchcast.mediaserver.NetworkHandler;

public class SizeSort extends Sorter 
{

	@Override
	public void sort()
	{
		ArrayList<byte[]> media_sizes = FileFactory.getMediaFileSizeList();
		
		final int length = media_sizes.size();
		
		ArrayList<Long> casted_sizes = new ArrayList<>(length);
		ArrayList<Long> sorted_size_list = new ArrayList<>(length);
		
		final ArrayList<byte[]> index_list = new ArrayList<>(length);
		
		for(int i = 0; i < length; i ++)
		{
			long casted_size = Static.bytesToLong(media_sizes.get(i));
			casted_sizes.add(casted_size);
			sorted_size_list.add(casted_size);
		}
		
		Collections.sort(sorted_size_list, Collections.reverseOrder());
		
		// Iterate through the sorted name list...
        for( int i = 0; i < length; i++)
        {
            Long sorted_size = sorted_size_list.get(i);
            int index = casted_sizes.indexOf(sorted_size);
            byte[] casted_index = ByteBuffer.allocate(4).putInt(index).array();
            index_list.add(casted_index);
        }
        
        FileFactory.setSizeList(index_list);
        NetworkHandler.signalPlaylistUpdate();
		
        //System.err.println("Size Sorting complete. "+index_list.size());
	}
	

	
}