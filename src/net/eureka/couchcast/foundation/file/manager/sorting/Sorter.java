package net.eureka.couchcast.foundation.file.manager.sorting;

import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;

/**
 * Abstract class that provides a protocol for {@link SortImpl} scheduling. The protocol is
 * to provide 'executed' and 'finished' booleans in order for an observer({@link DirectoryFactory})
 * to be able to tell if the sorting algorithm is finished or not.
 * <br>
 * <br>
 * The abstract 'sort()' method is now called from 'run()', so that any thread working on this 
 * will use this pattern appropriately.
 * 
 * @author Owen McMonagle.
 * @see SortImpl
 * @see AlphaSort
 * @see SizeSort
 * @see DirectoryFactory
 */
public abstract class Sorter implements SortImpl 
{
	/**
	 * Used to tell if the sorter has been run yet.
	 */
	private boolean executed = false;
	
	/**
	 * Used to tell if the sorter has finished yet.
	 */
	private boolean finished = false;
	
	/**
	 * To be ran from a thread or thread pool. Changes 'executed' flag to true,
	 * then calls the 'sort()' method, after sorting has been completed, the 
	 * 'finished' flag is set to true.
	 */
	@Override
	public final void run() 
	{	
		this.executed = true;
		this.sort();
		this.finished = true;
	}
	
	/**
	 * Abstract sort method. Supposed to be implemented by a child class in order to
	 * define the type of sorting. 
	 */
	public abstract void sort();
	
	/**
	 * Returns if the sorter has been executed or not yet.
	 * @return boolean - Returns whether or not the sorter has been run yet.
	 */
	public boolean isExecuted()
	{
		return this.executed;
	}
	
	/**
	 * Returns if the sorter has finished ordering yet. 
	 * @return boolean - Returns whether or not hte sorter has finished yet.
	 */
	public boolean isFinished()
	{
		return this.finished;
	}

}
