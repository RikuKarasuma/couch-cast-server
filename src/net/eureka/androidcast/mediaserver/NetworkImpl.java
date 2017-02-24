package net.eureka.androidcast.mediaserver;

/**
 * Defines the network protocol which every TCP connection must adhere to when connecting through the
 * {@link NetworkHandler}. These protocols are run once the connection has passed the action stage. The
 * action stage determines what kind of action the connection wants to take. Then the connection is passed
 * off to a thread pool for a short lived connection or a single thread if the connection is persistant.
 * 
 * 
 * @author Owen McMonagle
 *
 */
public interface NetworkImpl extends Runnable 
{
	public void run();
	public void read();
	public boolean verify();
	public void write();
	public void close();
}
