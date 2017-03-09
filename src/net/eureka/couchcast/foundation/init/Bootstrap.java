package net.eureka.couchcast.foundation.init;

import java.io.File;
import java.net.InetAddress;

import net.eureka.couchcast.Static;
import net.eureka.couchcast.foundation.config.Configuration;
import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;
import net.eureka.couchcast.foundation.logging.Logger;
import net.eureka.couchcast.gui.lang.LanguageDelegator;
import net.eureka.couchcast.gui.lang.Languages;
import net.eureka.utils.IO;


/**
 * This class bootstraps the foundation of the media server. Responsiblities are as follows:
 * <pre>
 * 	Creates and sets application directory ("USERNAME\Couch Cast"). {@link ApplicationGlobals}
 * 	Creates the default monitored directory ("USERNAME\Couch Cast\Monitored").
 * 	Instantiates the {@link Logger}.
 * 	Reads the {@link Configuration} file.
 * 	Checks if the DHCP activated interface is null. This is used by {@link Start} to determine if we have a network.
 * 	Prints starting information(O.S to log.
 * 	Checks if any directories have been read from the config file. If none are found, adds the default.
 * 	Instantiates the {@link DirectoryFactory}.
 * </pre>
 * 
 * <br>
 * Must be called before networking and gui creation for proper initialization. 
 * 
 * @author Owen McMonagle.
 *
 * @see ApplicationGlobals
 * @see InitialiseNetwork
 * @see Logger
 * @see Configuration
 * @see FileServer
 * @see DirectoryFactory  
 * @see Start
 * 
 * @version 0.2
 */
public final class Bootstrap 
{

	/**
	 * Used to determine if we have a DHCP activated interface on start up. This is determined by the
	 * {@link Configuration} file reading the interface name and correlating it with the list of 
	 * interfaces found in {@link Static}. The {@link InetAddress} to that interface is then set within 
	 * {@link NetworkGlobals}.
	 */
	private boolean noDhcpNetwork = false;
	
	
	/**
	 * Responsibilities: 
	 *<pre>
	 * 	Creates and sets application directory ("USERNAME\Couch Cast"). {@link ApplicationGlobals}
	 * 	Creates the default monitored directory ("USERNAME\Couch Cast\Monitored").
	 * 	Instantiates the {@link Logger}.
	 * 	Reads the {@link Configuration} file.
	 * 	Checks if the DHCP activated interface is null. This is used by {@link Start} to determine if we have a network.
	 * 	Prints starting information(O.S to log.
	 * 	Checks if any directories have been read from the config file. If none are found, adds the default.
	 * 	Instantiates the {@link DirectoryFactory}.
	 * </pre>
	 */
	public Bootstrap() 
	{
		// Retrieve the system drive directory.
		String system_drive = System.getenv("SYSTEMDRIVE"),
			   // Retrieve the default home path.
			   home_path = System.getenv("HOME");
		// If home path is null, try the updated renamed home path.
		home_path = ((home_path == null) ? System.getenv("HOMEPATH") : home_path);
		
		// Create default home/download directory path. 
		final StringBuffer directory_path = new StringBuffer(system_drive+home_path+File.separator+ApplicationGlobals.getName()),
						   monitored_path = new StringBuffer(directory_path+File.separator+"Monitored");
		
		System.out.println("Application dir:"+directory_path+"\nDownload dir:"+monitored_path);
		// Create default application directory.
		final boolean directory_created = IO.createDirectory(directory_path);
		// Create default download directory.
		final boolean monitored_directory_created = IO.createDirectory(monitored_path);
		// Set application directory path as global in ApplicationGlobals.
		ApplicationGlobals.setApplicationDirectory(directory_path.toString()+File.separator);
		// Create instance of logger to set it up.
		new Logger();
		// Append lines to signify new instance session.
		Logger.append(new StringBuffer("--------------------------------"));
		// Create instance of Configuration to set up configuration file.
		new Configuration(false);
		// Check if a DHCP Network has been chosen...
		if(NetworkGlobals.getDhcpNetwork() == null)
			noDhcpNetwork = true;
		// Print initial log results.
		Logger.append(new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.LOG_SYSTEM_DETECTION) + ApplicationGlobals.getOperatingSystem().toString()), new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.LOG_APPLICATION_DIRECTORY)+Boolean.toString(directory_created)), new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.LOG_DOWNLOAD_DIRECTORY)+Boolean.toString(monitored_directory_created)), new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.LANGUAGE)+Static.getSystemLocale()));
		if(ApplicationGlobals.getMonitoredSize() == 0)
			// Set monitored directory as global in ApplicationGlobals.
			ApplicationGlobals.addMonitoredDirectory(monitored_path.toString());
		// Initialise file fetcher.
		new DirectoryFactory();
		
	}
	
	/**
	 * Used to determine if we have a DHCP activated interface on startup.
	 * @return Boolean - True if we have no DHCP interface, false otherwise.
	 */
	public boolean hasNoDHCPNetwork()
	{
		return noDhcpNetwork;
	}
}
