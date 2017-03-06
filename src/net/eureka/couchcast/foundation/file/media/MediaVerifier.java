package net.eureka.couchcast.foundation.file.media;

import net.eureka.couchcast.foundation.file.manager.DirectoryScanner;

/**
 * Used by {@link DirectoryScanner} to verify a media files extension. Uses a
 * list of valid VLC extensions to determine whether a file can be played or not. 
 * 
 * @author Owen McMonagle.
 * 
 * @see DirectoryScanner
 * 
 * @version 0.1
 */
public final class MediaVerifier
{
	/**
	 * Valid VLC extensions. Taken from this URL: http://www.openwith.org/programs/vlc-media-player
	 */
	private static final String[] VALID_MEDIA_EXTENSIONS = new String[]
	{
				/**
		 * Digital Theater Systems Audio File
		 */
		new String(".DTS"),
		
		/**
		 * General eXchange Format
		 */
		new String(".GXF"),
		
		/**
		 * MPEG-2 Video
		 */
		new String(".M2V"),
		
		/**
		 * MP3 Uniform Resource Locator
		 */
		new String(".M3U"),
		
		/**
		 * MPEG-4 Video File
		 */
		new String(".M4V"),
		
		/**
		 * MPEG-1 Video
		 */
		new String(".MPEG1"),
		
		/**
		 * MPEG-2 Video 
		 */
		new String(".MPEG2"),
		
		/**
		 * AVCHD Video File
		 */
		new String(".MTS"),
		
		/**
		 * Material eXchange Format
		 */
		new String(".MXF"),
		
		/**
		 * Ogg Multimedia Container File
		 */
		new String(".OGM"),
		
		/**
		 * Dolby Digital AC-3 Compressed Audio File
		 */
		new String(".A52"),
		
		/**
		 * Advanced Audio Coding Compressed Audio File
		 */
		new String(".AAC"),
		
		/**
		 * DivX Movie
		 */
		new String(".DIVX"),
		
		/**
		 * Digital Video
		 */
		new String(".DV"),
		
		/**
		 * Flash Video
		 */
		new String(".FLV"),
		
		/**
		 * MPEG-1 Video
		 */
		new String(".M1V"),
		
		/**
		 * MPEG-2 Transport Stream Videos
		 */
		new String(".M2TS"),
		
		/**
		 * Matroska Video Stream 
		 */
		new String(".MKV"),
		
		/**
		 * Apple QuickTime Movie
		 */
		new String(".MOV"),
		
		/**
		 * MPEG-4 Video
		 */
		new String(".MPEG4"),
		
		/**
		 * OpenMG Audio File
		 */
		new String(".OMA"),
		
		/**
		 * Ogg Speex Audio File
		 */
		new String(".SPX"),
		
		/**
		 * DVD Video
		 */
		new String(".TS"),
		
		/**
		 * DVD Video Object
		 */
		new String(".VOB"),
		
		/**
		 * VCD Video
		 */
		new String(".DAT"),
		
		/**
		 * Binary DVD Video
		 */
		new String(".BIN"),
		
		
		/**
		 * 3G Mobile Phone Video
		 */
		new String(".3G2"),
		
		/**
		 * Audio Video Interleave
		 */
		new String(".AVI"),
		
		/**
		 * MPEG Video
		 */
		new String(".MPEG"),
		
		/**
		 * MPEG Video
		 */
		new String(".MPG"),
		
		/**
		 * Free Lossless Audio Codec Compressed Audio File
		 */
		new String(".FLAC"),
		
		/**
		 * MPEG-4 Audio File
		 */
		new String(".M4A"),
		
		/**
		 * MPEG-1 Audio Layer I
		 */
		new String(".MP1"),
		
		/**
		 * Ogg Multimedia Container File
		 */
		new String(".OGG"),
		
		/**
		 * Waveform Audio
		 */
		new String(".WAV"),
		
		/**
		 * Extended Module Audio File
		 */
		new String(".XM"),
		
		/**
		 * 3G Mobile Phone Video
		 */
		new String(".3GP"),
		
		/**
		 * Windows Media Video
		 */
		new String(".WMV"),
		
		/**
		 * Dolby Digital AC-3 Compressed Audio File
		 */
		new String(".AC3"),
		
		/**
		 * Advanced Systems Format Video
		 */
		new String(".ASF"),
		
		/**
		 * MOD Audio File
		 */
		new String(".MOD"),
		
		/**
		 * MPEG-1 Audio Layer II
		 */
		new String(".MP2"),
		
		/**
		 * MPEG-1 Audio Layer 3
		 */
		new String(".MP3"),
		
		/**
		 * MPEG-4 Part 14 Multimedia Container
		 */
		new String(".MP4"),
		
		/**
		 * Windows Media Audio File
		 */
		new String(".WMA"),
		
		/**
		 * Matroska Audio Stream
		 */
		new String(".MKA"),
		
		/**
		 * Protected AAC File
		 */
		new String(".M4P"),
	};
	
	public static String getExtensionList()
	{
		final int offset = 1;
		String extensions = "*.{";
		for(int i = 0; i < VALID_MEDIA_EXTENSIONS.length; i++)
			if(i < VALID_MEDIA_EXTENSIONS.length-offset)
				extensions = extensions + VALID_MEDIA_EXTENSIONS[i].substring(offset).toLowerCase() +",";
			else
				extensions = extensions + VALID_MEDIA_EXTENSIONS[i].substring(offset).toLowerCase();
		extensions = extensions + "}";
		return extensions;
	}
	
	public static void main(String[] args)
	{
		System.out.println(getExtensionList());
	}
	
	/**
	 * Takes a file as a parameter. Uses file to verify if it can be
	 * played by this VLC's implementation. Enabled formats are above.
	 * 
	 * @param File file_to_verify - Media file to verify.
	 * @return Boolean - True if file can be played, false otherwise.
	 */
	public static boolean isFileValid(String file_name)
	{
		// String to store file extension.
		String extension = "";
		
		// Integer to retrieve the last index of the character '.'.
		int i = file_name.lastIndexOf('.');
		// If index is greater than zero and not equal minus one..
		if (i > 0 || i != -1)
			// Split string with a beginning index of i ('.' before extension).
		    extension = file_name.substring(i);
	
		// Use extracted extension to validate file.
		return isExtensionValid(extension);
	}
	
	/**
	 * Takes a string file extension as parameter. If the passed string 
	 * equals any of the above enabled formats. Then the extension is valid.
	 * 
	 * @param String extension - Extension to be verified.
	 * @return Boolean - True if extension is valid, False otherwise.
	 */
	private static boolean isExtensionValid(String extension)
	{
		// Iterate through valid extensions...
		for(String valid : VALID_MEDIA_EXTENSIONS)
			// If an extension equals a valid extension...
			if(extension.toUpperCase().equals(valid))
				// Return true for a valid file.
				return true;
		// Return false for a invalid file.
		return false;
	}
	
	
}
