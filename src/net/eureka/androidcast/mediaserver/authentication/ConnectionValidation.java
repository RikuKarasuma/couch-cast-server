package net.eureka.androidcast.mediaserver.authentication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import net.eureka.androidcast.foundation.init.ApplicationGlobals;
import net.eureka.androidcast.foundation.init.NetworkGlobals;
import net.eureka.androidcast.gui.lang.LanguageDelegator;
import net.eureka.androidcast.gui.lang.Languages;
import net.eureka.androidcast.gui.tray.Tray;
import net.eureka.androidcast.mediaserver.NetworkWorker;
import net.eureka.security.sha.Sha3;

public final class ConnectionValidation extends NetworkWorker
{
	/**
	 * Checks if password has been generated before and creates a default one if necessary.
	 */
	static
	{
		new PasswordCreation();
	}
	
	/**
	 * Returned if password has been successfully verified. 
	 */
	private static final String SUCCESS = new String("success");
			
	/**
	 * Returned if password verification has failed.
	 */
	private static final String FAILURE = new String("failure");

	/**
	 * Password path, for verification purposes.
	 */
	private static final String PASSWORD_PATH = ApplicationGlobals.getApplicationDirectory()+"/cJwf1-sA4kt53sf";
	
	/**
	 * Output stream that must be initialised before input stream.
	 */
	private  BufferedOutputStream outputStream = null;
	
	/**
	 * Input stream that must be initialised after output stream.
	 */
	private  BufferedInputStream inputStream = null;
	
	/**
	 * Used to store the received password attempt.
	 */
	private transient StringBuffer password = new StringBuffer();
	
	private boolean valid = false;
	
	
	public ConnectionValidation(Socket connection) 
	{
		super(connection);
		outputStream = new BufferedOutputStream(getOutput());
		inputStream = new BufferedInputStream(getInput());
	}

	@Override
	public void run() 
	{
		System.out.println("Verifing..");
		read();
		verify();
		write();
		close();
	}

	@Override
	public void read() 
	{
		// Create a 32 byte buffer for receiving.
		byte[] receving_array = new byte[32];
		// String for casting the bytes to UTF-8 format.
		String received = null;
		try 
		{
			// Read password from input stream.
			inputStream.read(receving_array);
			received = new String(receving_array);
			// Create a new UTF-8 string from received password.
			received = received.trim();
			// If received is not equal to null then...
			if(received != null)
			{
				// Append the password string to the password buffer.
				password.append(received);
			}
		} 
		catch (IOException e) 
		{
			System.err.println("ERROR: Receiving password from authentication.\n"+e.getLocalizedMessage());
		}
		
	}

	@Override
	public boolean verify()
	{
		// Retrieve the original stored SHA3 key.
		final Sha3 encrypted_stored_password = new Sha3(PASSWORD_PATH.toString());
		// Check if the received password, matches the server password.
		if(encrypted_stored_password.compare(password))
		{
			// Display connected message via taskbar tray.
			Tray.displayMessage(LanguageDelegator.getLanguageOfComponent(Languages.TRAY_CLIENT_AUTHENICATION_SUCCESS), LanguageDelegator.getLanguageOfComponent(Languages.TRAY_CLIENT_AUTHENICATION_MESSAGE)+getAddress().getHostAddress());
			// Add client address to list for use throughout the program.
			NetworkGlobals.addClientAddress(getAddress());
			valid = true;
		}
		return false;
	}

	@Override
	public void write() 
	{
		// Initialize response based on validation.
		String response = (valid) ? SUCCESS : FAILURE;
		try 
		{
			outputStream.write(response.getBytes());
			// Flush bytes down stream.
			outputStream.flush();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(response);
	}

}
