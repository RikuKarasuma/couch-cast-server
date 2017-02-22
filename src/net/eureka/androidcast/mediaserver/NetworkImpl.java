package net.eureka.androidcast.mediaserver;

public interface NetworkImpl extends Runnable 
{
	public void run();
	public void read();
	public boolean verify();
	public void write();
	public void close();
}
