package net.eureka.androidcast.foundation.file.manager;

public class FileNotFoundError extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 365157154438411098L;
	
	@Override
	public void printStackTrace() 
	{
		this.printError();
		super.printStackTrace();
	}
	
	public void printError()
	{
		System.err.println("File not found within FileFactory.");
	}
}
