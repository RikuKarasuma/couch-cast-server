package net.eureka.couchcast.foundation.file.media;

import java.io.Serializable;

import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;
import net.eureka.couchcast.mediaserver.playlist.PlaylistWorker;

/**
 * Object representation of a Media File across the entire server and client. Used by mechanisms such as the
 * file {@link DirectoryFactory}, {@link PlaylistWorker}. Contains useful information about a media file such 
 * as name, path, size and whether the file is downloading or not.  
 * 
 * *** CLASS MUST BE CONSISTENT WITH CLIENTS CLASS VERSION *** 
 * 
 * @author Owen McMonagle.
 * @see DirectoryFactory
 * @see PlaylistWorker
 */
public class MediaFile implements Serializable
{
	/**
	 * Universal Serialization ID. Helps to identify between versions.
	 * *** CLASS MUST BE CONSISTENT WITH CLIENTS CLASS VERSION *** 
	 */
	private static final long serialVersionUID = 1402145788757979878L;
	
	/**
	 * Name of the media file.
	 */
	private byte[] name = null;
			
	/**
	 * Path of the media file.
	 */
	private byte[] location = null;
	
	/**
	 * Size of the media file, in MB or GB:MB. Metric.
	 */
	private byte[] size = null;
	
	/**
	 * Only constructor. Takes a media name, path, and file size as parameters.
	 * 
	 * @param byte[] media_name - Name of media file.
	 * @param byte[] media_location - Path of the media file.
	 * @param byte[] media_size - Size of the media file.
	 */
	public MediaFile(final byte[] media_name, final byte[] media_location, final byte[] media_size)
	{
		// Store media name.
		this.name = media_name;
		// Store media path.
		this.location = media_location;
		// Store media size.
		this.size = media_size;
	}

	/**
	 * Getter for media name
	 * @return byte[] - Name of the media file.
	 */
	public byte[] getName()
	{
		// Returns name.
		return name;
	}

	/**
	 * Getter for media path.
	 * @return byte[] - Path of the media file.
	 */
	public byte[] getLocation()
	{
		// Returns path.
		return location;
	}

	/**
	 * Getter for media size. Usually in MB or GB:MB (Imperial).
	 * @return byte[] - Size of the media file (MB|GB:MB(Imperial)).
	 */
	public byte[] getSize()
	{
		// Returns size.
		return size;
	}
	
}
