package net.eureka.couchcast.foundation.file.manager;

import java.util.BitSet;

/**
 * Handles cataloging of media files with respect to deep search directories. This is so we can tell
 * if a media file is within a deep search directory or not when we need to disable that feature in
 * settings. A {@link BitSet} is used to efficiently store each boolean as a bit instead of a primitive
 * or boxed primitive boolean.  
 * 
 * @author Owen McMonagle.
 * 
 * @see FileFactory
 * 
 * @version 0.1
 */
public class DeepSearchIndex
{
	private final static BitSet INDEXES = new BitSet();
	
	/**
	 * Inserts a deep search flag into the specific index of the {@link BitSet}.
	 * @param index - Index you wish to insert the deep search boolean.
	 * @param deep_searched - Flag value to be inserted at the specified index.
	 */
	public synchronized static void add(final int index, final boolean deep_searched)
	{
		INDEXES.set(index, deep_searched);
	}
	
	/**
	 * Returns whether or not a media file is deep searched or not.
	 * @param index - Index of file you wish to retrieve. 
	 * @return boolean - True if file is within deep search directory, false otherwise. 
	 */
	public synchronized static boolean get(final int index)
	{
		return INDEXES.get(index);
	}
}
