package net.eureka.couchcast.gui.lang;

import javax.swing.JFileChooser;

/**
 * Contains the language specific strings used throughout the media server. The {@link LanguageDelegator} handles
 * which language is retrieved.
 * 
 * @author Owen McMonagle.
 * 
 * @see LanguageDelegator
 * @see Detect
 * 
 * @version 0.1
 */
public enum Languages 
{
	/**
	 * GUI Submit button.
	 */
	SUBMIT_BUTTON
	{

		@Override
		public String getEnglish()
		{
			return "Submit";
		}
	},
	
	/**
	 * GUI Browse button.
	 */
	BROWSE_BUTTON
	{

		@Override
		public String getEnglish()
		{
			return "Add";
		}
		
	},
	
	/**
	 * GUI Shutdown button.
	 */
	SHUTDOWN_BUTTON
	{

		@Override
		public String getEnglish()
		{
			return "Shutdown Server";
		}
		
	},
	
	/**
	 * GUI Server name configuration label.
	 */
	SERVER_NAME_LABEL
	{

		@Override
		public String getEnglish() 
		{
			return "Server name:";
		}
		
	},
	
	/**
	 * GUI Server password configuration label.
	 */
	SERVER_PASSWORD_LABEL
	{

		@Override
		public String getEnglish() 
		{
			return "Server password:";
		}
		
	},
	
	/**
	 * GUI Server monitored directory configuration label.
	 */
	MONITORED_DIRECTORY_LABEL
	{

		@Override
		public String getEnglish()
		{
			return "Monitored directories:";
		}
		
	},
	
	/**
	 * GUI monitor configuration {@link JFileChooser} title.
	 */
	MONITORED_DIALOG_TITLE
	{

		@Override
		public String getEnglish()
		{
			return "Select a new monitored directory.";
		}

		
	},
	
	/**
	 * GUI {@link JFileChooser} cancel button text.
	 */
	DIALOG_CANCEL_BUTTON
	{

		@Override
		public String getEnglish()
		{
			return "Cancel";
		}

		
	},
	
	/**
	 * GUI {@link JFileChooser}, file type.
	 */
	DIALOG_FILE_TYPE
	{

		@Override
		public String getEnglish()
		{
			return "Folders only";
		}

		
	},
	
	/**
	 * Tray, authentication success message.
	 */
	TRAY_CLIENT_AUTHENICATION_SUCCESS
	{

		@Override
		public String getEnglish()
		{
			return "Client authenticated!";
		}
		
	},
	
	/**
	 * Tray, authentication address message.
	 */
	TRAY_CLIENT_AUTHENICATION_MESSAGE
	{

		@Override
		public String getEnglish()
		{
			return "Address:";
		}

	},
	
	/**
	 * Tray, name changed success message.
	 */
	TRAY_NAME_CHANGE_SUCCESS
	{

		@Override
		public String getEnglish()
		{
			return "Name change!";
		}

		
	},
	
	/**
	 * Tray, new name message.
	 */
	TRAY_NAME_CHANGE_MESSAGE
	{

		@Override
		public String getEnglish()
		{
			return "New server name:  ";
		}

		
	},
	
	/**
	 * Tray, download directory changed success message.
	 */
	TRAY_DOWNLOAD_CHANGE_SUCCESS
	{

		@Override
		public String getEnglish()
		{
			return "Download directory changed";
		}

		
	},
	
	/**
	 * Tray, new download directory message. 
	 */
	TRAY_DOWNLOAD_CHANGE_MESSAGE
	{

		@Override
		public String getEnglish()
		{
			return "New directory name:  ";
		}

	},
	
	/**
	 * Tray, password change success message.
	 */
	TRAY_PASSWORD_CHANGE_SUCCESS
	{

		@Override
		public String getEnglish()
		{
			return "Authentication";
		}

	},
	
	/**
	 * Tray, new password message.
	 */
	TRAY_PASSWORD_CHANGE_MESSAGE
	{

		@Override
		public String getEnglish()
		{
			return "The server password has been changed.";
		}
		
	},
	
	
	/**
	 * Log, notifies if start up server script was created.
	 */
	LOG_SERVICE_LAUNCHER_CREATED
	{

		@Override
		public String getEnglish()
		{
			return "Start up launcher created.";
		}

		
	},
	
	/**
	 * Log, notifies if start up server script already exists.
	 */
	LOG_SERVICE_LAUNCHER_EXISTS
	{

		@Override
		public String getEnglish()
		{
			return "Start up launcher exists.";
		}

		
	},
	
	/**
	 * Log, for system detection.
	 */
	LOG_SYSTEM_DETECTION
	{

		@Override
		public String getEnglish()
		{
			return "Detected system: ";
		}

	},
	
	/**
	 * Log, application directory creation.
	 */
	LOG_APPLICATION_DIRECTORY
	{

		@Override
		public String getEnglish()
		{
			return "Application directory created: ";
		}

	},
	
	/**
	 * Log, download directory creation.
	 */
	LOG_DOWNLOAD_DIRECTORY
	{

		@Override
		public String getEnglish()
		{
			return "Download directory created: ";
		}

		
	},
	
	/**
	 * File server, number of files served message.
	 */
	SERVER_FILES_SERVED
	{

		@Override
		public String getEnglish()
		{
			return "Number of files served: ";
		}

		
	},

	/**
	 * GUI, play-list heading.
	 */
	PLAYLIST_VIEWER_LABEL
	{

		@Override
		public String getEnglish()
		{
			return "Playlist viewer";
		}

		
	},
	
	/**
	 * GUI, invalid name.
	 */
	INVALID_NAME_LENGTH
	{

		@Override
		public String getEnglish()
		{
			return "Name cannot be blank.";
		}

		
	},
	
	/**
	 * Generic error message.
	 */
	ERROR
	{

		@Override
		public String getEnglish()
		{
			return "Error";
		}

		
	},
	
	/**
	 * GUI, invalid password length.
	 */
	INVALID_PASSWORD_LENGTH
	{

		@Override
		public String getEnglish()
		{
			return "The new password cannot be less than four characters.";
		}

		
	},
	
	/**
	 * Log, imported media file path.
	 */
	IMPORTED_MEDIA_FILE_LOCATION
	{
		@Override
		public String getEnglish() 
		{
			return "Imported media file location:";
		}
		
	},
	
	/**
	 * Log, no media files found.
	 */
	NO_MEDIA_FILES_LOCATED
	{
		@Override
		public String getEnglish() 
		{
			return "No media files found in directory";
		}
		
	},
	
	/**
	 * Log, port number.
	 */
	PORT_NUMBER
	{
		@Override
		public String getEnglish() 
		{
			return "Port Number:";
		}
		
	},
	
	/**
	 * GUI, two instances of media server running error.
	 */
	TWO_INSTANCES_ERROR
	{
		@Override
		public String getEnglish() 
		{
			return "Android Cast is already running.";
		}
		
	},
	
	/**
	 * Name.
	 */
	NAME
	{
		@Override
		public String getEnglish() 
		{
			return "Name";
		}
		
	},
	
	/**
	 * Size.
	 */
	SIZE
	{
		@Override
		public String getEnglish() 
		{
			return "Size";
		}
		
	},
	
	/**
	 * Number.
	 */
	NUMBER
	{
		@Override
		public String getEnglish() 
		{
			return "Index";
		}
	},
	
	/**
	 * Location/Path.
	 */
	LOCATION
	{

		@Override
		public String getEnglish()
		{
			return "Location";
		}

		
	},
	
	/**
	 * Yes/Affirmative/Okay.
	 */
	OKAY
	{

		@Override
		public String getEnglish()
		{
			return "Okay";
		}
		
	}, 
	
	/**
	 * Language.
	 */
	LANGUAGE
	{
		@Override
		public String getEnglish() 
		{
			return "Language:";
		}
	};
	
	public abstract String getEnglish();
}
