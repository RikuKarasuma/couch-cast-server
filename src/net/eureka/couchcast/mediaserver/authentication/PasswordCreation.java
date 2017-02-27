package net.eureka.couchcast.mediaserver.authentication;

import java.io.File;

import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.security.sha.Sha3;

/**
 * Called when {@link ConnectionValidation} is first created. This class checks if the password file is in the Couch Cast
 * user directory. If not a new password file is created with the default string value of "password", then stored as
 * a SHA384 encrypted key.
 * 
 * @author Owen McMonagle.
 * 
 * @see ConnectionValidation
 */
public final class PasswordCreation 
{
	/**
	 * Path to the password file.
	 */
	private static final String PASSWORD_PATH = ApplicationGlobals.getApplicationDirectory().toString()+"cJwf1-sA4kt53sf";
			
	/**
	 * Default password if no password has been set.
	 */
	private static final String DEFAULT_PASSWORD = new String("password");
	
	/**
	 * Flag for signaling whether the password file is there or not. 
	 */
	private static boolean isPasswordThere = false;
	
	/**
	 * Static block called when {@link PasswordCreation} is first Initialized.
	 */
	static
	{
		// Retrieve password file as File instance from default Password Path.
		File password_file = new File(PASSWORD_PATH);
		// Verify if file is located at the path.
		isPasswordThere = password_file.isFile();
		// Print out if password file has been found.
		System.out.println("Found password:\t"+isPasswordThere);
	}
	
	/**
	 * Only Constructor, checks if password has been located.
	 */
	PasswordCreation()
	{
		// Call method to check password existence.
		checkPassword();
	}
	
	/**
	 * Checks if isPasswordThere equals false. If so a new SHA384 key is created from the string "password".
	 * Then it is saved on the password path.
	 */
	private static void checkPassword()
	{
		// Check if password has been located...
		if(!isPasswordThere)
			// if not generate new default password.
			Sha3.generateSha3(new StringBuffer(DEFAULT_PASSWORD)).save(new StringBuffer(PASSWORD_PATH));
	}
}
