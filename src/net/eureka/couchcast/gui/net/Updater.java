package net.eureka.couchcast.gui.net;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import net.eureka.couchcast.Start;
import net.eureka.couchcast.Static;
import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.gui.AppStage;

/**
 * Informs the user if the media server is out of date or if contact can't be made. If so, points the user to the 
 * sourceforge download URL. The constructor requires the main set up thread which will be halted until an action
 * has selected by the user.
 * 
 * @author Owen McMonagle.
 * 
 * @see ApplicationGlobals
 * @see AppStage
 * @see Start
 * 
 * @version 0.5
 */
public final class Updater 
{
	
	private static final String 
			VERSION_CONTROL_CENTER = "http://couchcastcenter-rikukarasuma.rhcloud.com/number",
			URL = "http://sourceforge.net/projects/couch-cast-media-server/files/", 
			MESSAGE = "<html>A new update is available, please download and install in order to be "
					+ "compatible with the latest Android client version.</html>",
			WARNING = "<html>Could not contact download servers, we are unable to verify if your "
					+ "version is out of date.</html>";
	
	/**
	 * Opens the users browser and attempts to download the latest update.
	 */
	private ActionListener downloadListener = new ActionListener() 
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			String encoded = (URL + newVersion + "/" + getDownloadName(ApplicationGlobals.is32Bit()) + "/download").replace(" ", "%20");
			Static.openWebpage(encoded);
			System.exit(0);
		}
	};
	
	/**
	 * Opens the users browser and attempts to download the latest update.
	 */
	private ActionListener okayListener = new ActionListener() 
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			((JFrame)((JButton)e.getSource()).getParent().getParent().getParent().getParent()).dispose();
			signalThreadContinue();
		}
	};
	
	/**
	 * Listens for the window to be closed, once close. It signals the main set up thread to continue.
	 */
	private WindowAdapter windowListener = new WindowAdapter()
	{
		
		@Override
        public void windowClosing(WindowEvent we) 
		{
			signalThreadContinue();
        }
	};

	/**
	 * Main set up thread, passed through by the constructor. Halted when GUI is created.
	 */
	private Thread main = null;
	
	private String newVersion = "";
	
	/**
	 * Takes in main thread as a parameter, if thread is null the JVM is shut down. Otherwise the main thread is 
	 * signalled to wait and a new thread is started in order to check if a new version exits, if so, the a link
	 * to the newest version is provided for download.
	 * 
	 * @param main_setup_thread - Main set up thread.
	 */
	public Updater(Thread main_setup_thread)
	{
		if(main_setup_thread != null)
		{
			main = main_setup_thread;
			startUpdateThread();
			signalThreadWait();
		}
		else
			System.exit(1);
	}
	
	/**
	 * Starts a thread to verify if a new update is available and if so, creates a GUI to provide the user with
	 * a download link. 
	 */
	private void startUpdateThread()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				final boolean new_version = isNewVersion(ApplicationGlobals.getVersion());
				System.out.println("New version:"+new_version);
				if(new_version)
					setUpDownload();
				else
					signalThreadContinue();
			}
		}).start();;
	}
	
	/**
	 * Signals the main set up thread to wait.
	 */
	private void signalThreadWait()
	{
		synchronized (main)
		{
			try 
			{
				main.wait();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Signals the main set up thread to continue.
	 */
	private void signalThreadContinue()
	{
		synchronized (main)
		{
			main.notify();
		}
	}
	
	/**
	 * Used to determine if there is a new version available online to download. If the internet 
	 * can't be accessed, then a window informs the user that they may not be up to date.
	 * @param version - Current app version.
	 * @return boolean - True if new version, false otherwise.
	 */
	private boolean isNewVersion(String version)
	{
		try 
		{
			HttpURLConnection connection = (HttpURLConnection) new java.net.URL(VERSION_CONTROL_CENTER).openConnection();
			final int response_code = connection.getResponseCode();
			System.out.println("Response code:"+connection.getResponseCode());
			if(response_code == 200)
			{
				byte[] version_num = new byte[3];
				connection.getInputStream().read(version_num);
				connection.getInputStream().close();
				newVersion = new String(version_num);
				System.out.println("Latest version number:" + newVersion);
				return !version.equals(new String(version_num));
			}
			else
				setUpWarning();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Sets up the information window about the new update available. Provides a download link.
	 */
	private void setUpDownload()
	{
		Image icon = getIconImage();
		JFrame frame = new JFrame(ApplicationGlobals.getName() + " Update");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(windowListener);
		if(icon != null)
			frame.setIconImage(icon);
		Container container = frame.getContentPane();
		JLabel update_message = new JLabel(MESSAGE);
		update_message.setBorder(new EmptyBorder(10, 10, 10, 10));
		update_message.setHorizontalAlignment(SwingConstants.CENTER);
		JButton download_button = new JButton("Download");
		download_button.addActionListener(downloadListener);
		download_button.setSize(75, 25);
		container.add(update_message, BorderLayout.NORTH);
		container.add(download_button, BorderLayout.CENTER);
		frame.setSize(300, 125);
		frame.setVisible(true);
		frame.setResizable(false);
		centreWindow(frame);
	}
	
	/**
	 * Sets up a warning window stating that the server could not contact the version server.
	 */
	private void setUpWarning()
	{
		Image icon = getIconImage();
		JFrame frame = new JFrame(ApplicationGlobals.getName() + " Update");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(windowListener);
		if(icon != null)
			frame.setIconImage(icon);
		Container container = frame.getContentPane();
		JLabel update_message = new JLabel(WARNING);
		update_message.setBorder(new EmptyBorder(10, 10, 10, 10));
		update_message.setHorizontalAlignment(SwingConstants.CENTER);
		JButton ok_button = new JButton("Okay");
		ok_button.addActionListener(okayListener);
		ok_button.setSize(75, 25);
		container.add(update_message, BorderLayout.NORTH);
		container.add(ok_button, BorderLayout.CENTER);
		frame.setSize(300, 125);
		frame.setVisible(true);
		frame.setResizable(false);
		centreWindow(frame);
	}
	
	/**
	 * Creates the name of the executable to download. Name is retrieved from {@link ApplicationGlobals}.
	 * @param is_32_bit - Used to determine the architecture.
	 * @return Download executable name.
	 */
	private String getDownloadName(boolean is_32_bit)
	{
		return ApplicationGlobals.getName() + " " + ((is_32_bit) ? "32-bit Setup.exe" : "64-bit Setup.exe"); 
	}
	
	/**
	 * Here for the future when perhaps we can host files on our own platform. 
	 * @return 
	 */
	@SuppressWarnings("unused")
	private JProgressBar setUpGUI()
	{
		Image icon = getIconImage();
		JFrame frame = new JFrame(ApplicationGlobals.getName() + " Updater");
		if(icon != null)
			frame.setIconImage(icon);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container container = frame.getContentPane();
		JProgressBar progress_bar = new JProgressBar();
		progress_bar.setValue(25);
		progress_bar.setStringPainted(true);
		Border border = BorderFactory.createTitledBorder("Downloading...");
		progress_bar.setBorder(border);
		container.add(progress_bar, BorderLayout.NORTH);
		frame.setSize(300, 75);
		frame.setVisible(true);
		frame.setResizable(false);
		return progress_bar;
	}
	
	/**
	 * Retrieves the icon for displaying on the GUI.
	 * @return {@link Image} - PNG Image icon within install dir.
	 */
	private Image getIconImage()
	{
		try 
		{
			return new ImageIcon(ImageIO.read(new File(ApplicationGlobals.getInstallPath()+"logo.png"))).getImage();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Centers any window that is passed through.
	 * @param frame - Window to be centered.
	 */
	public static void centreWindow(Window frame) 
	{
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}
	
}
