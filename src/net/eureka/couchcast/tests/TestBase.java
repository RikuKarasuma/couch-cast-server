package net.eureka.couchcast.tests;

public class TestBase
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
