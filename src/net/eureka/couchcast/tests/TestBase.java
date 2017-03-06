package net.eureka.couchcast.tests;

/**
 * A class to provide a test base for any test object. Managed by the {@link TestManager}.
 * 
 * @author Owen McMonagle.
 *
 * @see TestManager
 * @see OOPinitiator
 * @see DirectoryFactoryMonitor
 *
 *
 * @version 0.1
 */
public abstract class TestBase
{
	boolean failed = false;
	
	
	public void setFailed(boolean has_failed)
	{
		failed = has_failed;
	}
	
	public boolean hasFailed()
	{
		return failed;
	}
}
