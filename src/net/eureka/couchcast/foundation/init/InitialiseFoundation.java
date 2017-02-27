package net.eureka.couchcast.foundation.init;

import java.io.File;

import net.eureka.couchcast.Static;
import net.eureka.couchcast.foundation.config.Configuration;
import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;
import net.eureka.couchcast.foundation.logging.Logger;
import net.eureka.couchcast.foundation.service.Initialiser;
import net.eureka.couchcast.gui.Menu;
import net.eureka.couchcast.gui.lang.LanguageDelegator;
import net.eureka.couchcast.gui.lang.Languages;
import net.eureka.utils.IO;


/**
 * Handles the creation of the File Manager, File server, Home folder path(In \USERNAME\PC-Cast\), Logger, Start up script creation and configuration file creation.
 * This is merely a launching pad for the lower foundation level of the server and should only need to be created once per server instance.
 * 
 * MUST BE CALLED BEFORE PEER RECEIVER BUT AFTER TRAY. This class bootstraps most of foundation server so that each other independent Thread can operate around it.
 *  
 * @author Owen McMonagle.
 *
 * @see ApplicationGlobals
 * @see InitialiseNetwork
 * @see Logger
 * @see Initialiser
 * @see Configuration
 * @see FileServer
 * @see DirectoryFactory  
 */
// IGNORE UNUSED WARNING.
@SuppressWarnings("unused")
public final class InitialiseFoundation 
{

	private boolean noDhcpNetwork = false;
	
	
	/**
	 * Server foundation constructor. Handles the set up of the Application directory, Download directory, ApplicationGlobals, Logger, Initialiser(For creating start up script on login),
	 * configuration file, file server and file fetcher. 
	 * 
	 * MUST BE CALLED BEFORE PEER RECEIVER BUT AFTER TRAY. This class bootstraps most of foundation server so that each other independent Thread can operate around it.
	 * 
	 */
	public InitialiseFoundation() 
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
		final boolean download_directory_created = IO.createDirectory(monitored_path);
		// Set application directory path as global in ApplicationGlobals.
		ApplicationGlobals.setApplicationDirectory(directory_path.toString()+File.separator);
		// Create instance of logger to set it up.
		new Logger();
		// Append lines to signify new instance session.
		Logger.append(new StringBuffer("--------------------------------"));
		// Create instance of Initialiser to set up the start up service file.
		new Initialiser();
		// Create instance of Configuration to set up configuration file.
		new Configuration(false);
		// Check if a DHCP Network has been chosen...
		if(NetworkGlobals.getDhcpNetwork() == null)
			noDhcpNetwork = true;
		// Print initial log results.
		Logger.append(new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.LOG_SYSTEM_DETECTION) + ApplicationGlobals.getOperatingSystem().toString()), new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.LOG_APPLICATION_DIRECTORY)+Boolean.toString(directory_created)), new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.LOG_DOWNLOAD_DIRECTORY)+Boolean.toString(download_directory_created)), new StringBuffer(LanguageDelegator.getLanguageOfComponent(Languages.LANGUAGE)+Static.getSystemLocale()));
		if(ApplicationGlobals.getMonitoredSize() == 0)
			// Set monitored directory as global in ApplicationGlobals.
			ApplicationGlobals.addMonitoredDirectory(monitored_path.toString());
		// Initialise file fetcher.
		new DirectoryFactory();
		
	}
	
	public boolean hasNoDHCPNetwork()
	{
		return noDhcpNetwork;
	}
}
