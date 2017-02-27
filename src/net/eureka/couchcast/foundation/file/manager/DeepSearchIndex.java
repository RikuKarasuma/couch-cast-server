package net.eureka.couchcast.foundation.file.manager;

import java.util.BitSet;

public class DeepSearchIndex
{
	private final static BitSet INDEXES = new BitSet();
	
	public synchronized static void add(final int index, final boolean deep_searched)
	{
		INDEXES.set(index, deep_searched);
	}
	
	public synchronized static boolean get(final int index)
	{
		return INDEXES.get(index);
	}
}
