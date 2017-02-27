package net.eureka.couchcast.foundation.file.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Runs every 30 seconds from the DirectoryFactory class. It is used to catch any files that may have been added to
 * the FileFactory after they have already been removed. This is a concurrency race issue that unfortunately has not
 * been solved as of yet. This patch alleviates the issue.  
 * 
 * @author Owen McMonagle.
 *
 */
public class FileFactoryVerifier implements Runnable 
{

	private List<byte[]> pathsToVerify = FileFactory.cloneMediaPaths();
	
	@Override
	public void run() 
	{
		//System.out.println("Starting threaded verification process...");
		if(FileFactory.getListSize() != pathsToVerify.size())
		{
			pathsToVerify = FileFactory.cloneMediaPaths();
			//System.out.println("Retrieved new paths...");
		}
		
		List<byte[]> paths_to_remove = new ArrayList<>(pathsToVerify.size());
		File path_file = null;
		//System.out.println("Processing...");
		for(int i = 0; i < pathsToVerify.size(); i++)
		{
			byte[] path_to_verify = pathsToVerify.get(i);
			path_file = new File(new String(path_to_verify));
			if(!path_file.exists())
				paths_to_remove.add(path_to_verify);
		}
		
		if(!paths_to_remove.isEmpty())
			DirectoryFactory.addPathsForCleanUp(paths_to_remove);
		//System.out.println("Completed.");
	}

}
