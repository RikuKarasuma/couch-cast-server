package net.eureka.couchcast.foundation.file.manager;

import net.eureka.couchcast.foundation.file.media.MediaFile;

/**
 * An exception object used to mark when a {@link MediaFile} cannot be found from within {@link FileFactory}.
 * 
 * @author Owen McMonagle.
 * 
 * @see MediaFile
 * @see FileFactory
 * 
 * @version 0.1
 */
public final class FileNotFoundError extends Exception
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
