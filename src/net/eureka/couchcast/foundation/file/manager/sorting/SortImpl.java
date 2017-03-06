package net.eureka.couchcast.foundation.file.manager.sorting;

import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;

/**
 * 
 * Interface to handle a sorting pattern. Implements {@link Runnable} so it can be added to a {@link Thread}.
 * See {@link Sorter} for guidelines.
 * 
 * @author Owen McMonagle.
 *
 * @see Sorter
 * @see AlphaSort
 * @see SizeSort
 * @see DirectoryFactory
 * 
 * @version 0.1
 */
public interface SortImpl extends Runnable
{
	
	/**
	 * Used to make the sorter thread compatible.
	 */
	@Override
	public void run();
}
