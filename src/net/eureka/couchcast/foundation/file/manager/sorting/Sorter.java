package net.eureka.couchcast.foundation.file.manager.sorting;

public abstract class Sorter implements SortImpl 
{
	private boolean executed = false, finished = false;
	
	@Override
	public final void run() 
	{	
		this.executed = true;
		this.sort();
		this.finished = true;
	}
	
	public abstract void sort();
	
	public boolean isExecuted()
	{
		return this.executed;
	}
	
	public boolean isFinished()
	{
		return this.finished;
	}

}
