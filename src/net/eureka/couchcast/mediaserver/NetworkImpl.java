package net.eureka.couchcast.mediaserver;

/**
 * Defines the network protocol which every TCP connection must adhere to when connecting through the
 * {@link NetworkHandler}. These protocols are run once the connection has passed the action stage. The
 * action stage determines what kind of action the connection wants to take. Then the connection is passed
 * off to a thread pool for a short lived connection or a single thread if the connection is persistant.
 * 
 * <br>
 * <br>
 * Some of these abstract methods are defined more clearly within {@link NetworkWorker}.
 * 
 * @author Owen McMonagle
 * @see NetworkHandler
 * @see NetworkWorker
 *
 */
public interface NetworkImpl extends Runnable 
{
	/**
	 * Controls the order in read, verify, write and close occurs.
	 */
	public void run();
	
	/**
	 * Abstract method for reading.
	 */
	public void read();
	
	/**
	 * Abstract method for verification.
	 * @return True if verified, false otherwise.
	 */
	public boolean verify();
	
	/**
	 * Abstract method for writing.
	 */
	public void write();
	
	/**
	 * Abstract method for closing.
	 */
	public void close();
}
