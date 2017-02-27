package net.eureka.couchcast.tests;

import java.util.ArrayList;

/**
 * Runs each core unit test and prints whether or not the test has passed. 
 * @author Garak
 *
 */
public final class TestManager 
{
	public static void main(String[] args)
	{
		// Collection to contain tests.
		final ArrayList<TestBase> tests = new ArrayList<TestBase>();
		
		// OOP Tester
		tests.add(new OOPinitiator());
		// Directory search tester
		tests.add(new DirectoryFactoryMonitor(false));
		
		// Empty line for neatness.
		System.out.println();
		
		// Iterate through tests run...
		for(TestBase test : tests)
			// if test failed....
			if(test.failed)
				// print test failed.
				System.out.println(test.getClass().getName() + " has failed.");
			// if test succeeded ....
			else
				// print test successful.
				System.out.println(test.getClass().getName() + " has passed.");
		
	}
}
