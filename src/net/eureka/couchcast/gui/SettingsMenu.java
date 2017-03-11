package net.eureka.couchcast.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import net.eureka.couchcast.foundation.config.Configuration;
import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;
import net.eureka.couchcast.foundation.file.manager.DirectoryScanner;
import net.eureka.couchcast.foundation.init.ApplicationGlobals;
import net.eureka.couchcast.foundation.init.NetworkGlobals;
import net.eureka.couchcast.gui.control.WindowControl;
import net.eureka.couchcast.gui.control.handlers.DoubleClickSizeHandler;
import net.eureka.couchcast.gui.control.handlers.MoveHandler;
import net.eureka.couchcast.gui.directory.DirectoryViewer;
import net.eureka.couchcast.gui.lang.LanguageDelegator;
import net.eureka.couchcast.gui.lang.Languages;
import net.eureka.couchcast.gui.tray.Tray;
import net.eureka.couchcast.mediaserver.authentication.PasswordCreation;
import net.eureka.security.sha.Sha3;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


/**
 * Instantiated and managed from {@link AppStage}. This class handles the creation and managing of each Javafx
 * component related to the settings menu. One notable component is the {@link DirectoryViewer}; which allows for
 * the viewing of monitored directories. Buttons are available to add, remove or clear the directory list as well
 * as sliders for managing the file search delay and file update delay. These sliders are important for real time
 * performance tuning in lower end PCs.
 * <br>
 * <br>
 * There are two text boxes, one for the server name which is stored in {@link NetworkGlobals} and the other for
 * the network password which is stored into a file using a SHA-384 key ({@link PasswordCreation}). The submit 
 * button only applies to these two text boxes, everything else is saved after each action.
 * <br>
 * <br>
 * Three checkboxes exist to provide flexibility with different functionalities. These are:
 * <pre>
 * 	Minimize windows: If checked, the OOP player will minimize each window that is on the desktop that can be 
 * 	minimized. This is done through through the {@link Configuration} file and located within 
 * 	{@link ApplicationGlobals}.  
 * 
 * 	Deep search: If checked, each {@link DirectoryScanner} will recursively search folders for media, this is
 * 	an expensive operation and for lower end machines, it might be best to add folders directly. Saved to the
 * 	{@link Configuration} file and can be retrieved via P{@link ApplicationGlobals}.
 * 
 * 	Music mode: If checked, the OOP player will keep the media window hidden so that the audio can play in the
 * 	background. This setting can be modified from the clients. Saved to the {@link Configuration} file and can
 * 	be retrieved via {@link ApplicationGlobals}.
 *	</pre>
 * 
 * @author Owen McMonagle.
 * 
 * @see AppStage
 * @see ApplicationGlobals
 * @see DirectoryViewer
 * @see NetworkGlobals
 * @see PasswordCreation
 * @see Configuration
 * 
 * @version 0.1
 */
public final class SettingsMenu extends Scene
{
	/**
	 * Minimum password length.
	 */
	private static final int MINIMUM_PASSWORD_LENGTH = 4;
	
	/**
	 * String that contains the language specific text of the Download Directory. Uses the {@link LanguageDelegator} to decide 
	 * which text to choose. The {@link Languages} enumeration helps retrieve the appropriate stored text.
	 */
	private static final String CONFIG_INFO_STRING = new String(LanguageDelegator.getLanguageOfComponent(Languages.MONITORED_DIRECTORY_LABEL));
	
	/**
	 * String that contains the language specific text of the Submit button. Uses the {@link LanguageDelegator} to decide which 
	 * text to choose. The {@link Languages} enumeration helps retrieve the appropriate stored text. 
	 */
	private static final String SUBMIT_BUTTON_TEXT = new String(LanguageDelegator.getLanguageOfComponent(Languages.SUBMIT_BUTTON));
	
	/**
	 * String that contains the language specific text of the Browse button. Uses the {@link LanguageDelegator} to decide which
	 * text to choose. The {@link Languages} enumeration helps retrieve the appropriate stored text. 
	 */
	private static final String BROWSE_BUTTON_TEXT = new String(LanguageDelegator.getLanguageOfComponent(Languages.BROWSE_BUTTON));
	
	/**
	 * String that contains the language specific text of the file choosers title. Uses the {@link LanguageDelegator} to decide which
	 * text to choose. The {@link Languages} enumeration helps retrieve the appropriate stored text. 
	 */
	private static final String DOWNLOAD_DIALOG_TITLE = new String(LanguageDelegator.getLanguageOfComponent(Languages.MONITORED_DIALOG_TITLE));
	
	/**
	 * Password configuration info, language specific. Uses the {@link LanguageDelegator} to decide which text to choose.
	 * The {@link Languages} enumeration helps retrieve the appropriate stored text.
	 */
	private static final String PASSWORD_CONFIG_INFO_STRING = new String(LanguageDelegator.getLanguageOfComponent(Languages.SERVER_PASSWORD_LABEL));
	
	/**
	 * Path to the password file. The application directory is retrieved from {@link ApplicationGlobals}, then the name of the password
	 * file is attached after the file separator.
	 */
	private static final String PASSWORD_PATH = ApplicationGlobals.getApplicationDirectory().toString()+File.separator+"cJwf1-sA4kt53sf";
	
	/**
	 * Name configuration info, language specific. Uses the {@link LanguageDelegator} to decide which text to choose. The {@link Languages} 
 	 * enumeration helps retrieve the appropriate stored text.
	 */
	private static final String NAME_CONFIG_INFO_STRING = new String(LanguageDelegator.getLanguageOfComponent(Languages.SERVER_NAME_LABEL));
	
	private static boolean directoryChanged = false;
	
	private static String lastOpenedDirectory = "";
	
	private AppStage parent = null;
	
	private final EventHandler<ActionEvent> BACK_LISTENER = new EventHandler<ActionEvent>()
	{
		
		@Override
		public final void handle(ActionEvent e) 
		{
			parent.switchToMain();
		}
	};
	
	private final EventHandler<ActionEvent> SAVE_LISTENER = new EventHandler<ActionEvent>()
	{
		
		@Override
		public final void handle(ActionEvent e) 
		{
			save();
			parent.switchToMain();
		}
	};
	
	private final EventHandler<ActionEvent> BROWSE_LISTENER = new EventHandler<ActionEvent>()
	{
		
		@Override
		public final void handle(ActionEvent e) 
		{
			DirectoryChooser monitored_chooser = new DirectoryChooser();
			monitored_chooser.setTitle(DOWNLOAD_DIALOG_TITLE);
			this.handleDirectoryOpening(monitored_chooser);
			File choosen_dir = monitored_chooser.showDialog(parentStage);
			this.handleChoosenDirectory(choosen_dir);
		}
		
		private final void handleDirectoryOpening(DirectoryChooser monitored_chooser)
		{
			if(lastOpenedDirectory.length() > 0)
			{
				File file = new File(lastOpenedDirectory), parent = file.getParentFile();
				if(parent.isDirectory())
					monitored_chooser.setInitialDirectory(parent);
			}
		}
		
		private final void handleChoosenDirectory(File choosen_dir)
		{
			if(choosen_dir != null)
			{
				lastOpenedDirectory = choosen_dir.getAbsolutePath();
				ApplicationGlobals.addMonitoredDirectory(lastOpenedDirectory);
				directoryList.load();
				directoryChanged = true;
				new Configuration(true);
				DirectoryFactory.initialise();
			}
		}
	};
	
	private final EventHandler<ActionEvent> DEEP_SEARCH_LISTENER = new EventHandler<ActionEvent>()
	{
		
		@Override
		public final void handle(ActionEvent e) 
		{
			if(ApplicationGlobals.isDeepSearch())
				ApplicationGlobals.setDeepSearch(false);
			else
				ApplicationGlobals.setDeepSearch(true);
			
			new Configuration(true);
		}
	};
	
	private final EventHandler<ActionEvent> MINIMIZE_LISTENER = new EventHandler<ActionEvent>()
	{
		
		@Override
		public final void handle(ActionEvent e) 
		{
			if(ApplicationGlobals.isMinimizeWindows())
				ApplicationGlobals.setMinimizeWindows(false);
			else
				ApplicationGlobals.setMinimizeWindows(true);
			
			new Configuration(true);
		}
	};
	
	private final EventHandler<ActionEvent> MUSIC_MODE_LISTENER = new EventHandler<ActionEvent>()
	{
		
		@Override
		public final void handle(ActionEvent e) 
		{
			if(ApplicationGlobals.isMusicMode())
				ApplicationGlobals.setMusicMode(false);
			else
				ApplicationGlobals.setMusicMode(true);
			
			new Configuration(true);
		}
	};
	
	private final EventHandler<ActionEvent> REMOVE_BUTTON_LISTENER = new EventHandler<ActionEvent>()
	{
		
		@Override
		public final void handle(ActionEvent e) 
		{
			ArrayList<String> removed_monitored_paths = null;
			try
			{
				removed_monitored_paths = new ArrayList<String>(Arrays.asList(directoryList.selectedPaths()));
				ApplicationGlobals.getMonitoredList().removeAll(removed_monitored_paths);
				directoryList.resetSelectedPaths();
				DirectoryFactory.reinitialize();
			}
			catch(NullPointerException e1)
			{
				
			}
		}
	};
	
	private final EventHandler<ActionEvent> REMOVE_ALL_BUTTON_LISTENER = new EventHandler<ActionEvent>()
	{
		
		@Override
		public final void handle(ActionEvent e) 
		{
			ApplicationGlobals.getMonitoredList().clear();
			directoryList.resetAllIndexes();
			DirectoryFactory.resetScanners();
		}
	};
	
	private final ChangeListener<Number> searchSliderListener = new ChangeListener<Number>() 
	{
		public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) 
		{
			ApplicationGlobals.setSearchDelay(new_val.floatValue()*1000);
			new Configuration(true);
		}
	},
	updateSliderListener = new ChangeListener<Number>() 
	{
		public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) 
		{
			ApplicationGlobals.setUpdateDelay(new_val.floatValue()*1000);
			new Configuration(true);
		}
	};

	
	private Label directoryLabel = new Label(CONFIG_INFO_STRING),
				  nameLabel = new Label(NAME_CONFIG_INFO_STRING),
				  passwordLabel = new Label(PASSWORD_CONFIG_INFO_STRING);
	
	private TextField nameTextArea = new TextField(),
					  passwordTextArea = new TextField();
	
	private BorderPane directoryPane = new BorderPane();
	
	private DirectoryViewer directoryList = new DirectoryViewer(directoryPane);
	
	private Button save = new Button(SUBMIT_BUTTON_TEXT), back = new Button("Back"), monitoredBrowse = new Button(BROWSE_BUTTON_TEXT), remove = new Button("Remove"),
			removeAll = new Button("Clear"), title = new Button();
	
	private CheckBox minimize = new CheckBox("Minimize windows"), deepSearch = new CheckBox("Deep search"), musicMode = new CheckBox("Music Mode");
	
	private Slider searchSlider = new Slider( 0.0, 0.3, ApplicationGlobals.getSearchDelay()), updateSlider = new Slider( 1.0, 10.0, ApplicationGlobals.getUpdateDelay());
	
	private HBox 	 headingPane = new HBox(directoryLabel, title),
					 namePane = new HBox(nameLabel, nameTextArea),
					 passwordPane = new HBox(passwordLabel, passwordTextArea),
					 boxPane = new HBox(minimize, deepSearch, musicMode),
					 bottomPane = new HBox(back, save);

	private VBox container = null;
	
	private StackPane root = null;
	
	private Stage parentStage = null;
	
	public SettingsMenu(Stage parent_stage, AppStage settings_parent) 
	{
		super(new StackPane());
		this.parentStage = parent_stage;
		this.parent = settings_parent;
		this.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		this.container = new VBox();
		this.container.setId("settings_container");
		this.container.setSpacing(40);
		this.root = (StackPane) this.getRoot();
		this.root.getChildren().add(this.container);
		
		// Define window controls
		new MoveHandler(this.parentStage, this.container);
		new DoubleClickSizeHandler(this.parentStage, this.container);
		new WindowControl(this.parentStage, this.root);

		this.setUpButtons();
		this.setUpDirectorySettings();
		this.setUpNameSettings();
		this.setUpPasswordSettings();
		this.setUpCheckBoxSettings();
		this.setUpBottomPane();
		this.updateCheckBoxes();
		setUpTooltips();
	}
	
	private void setUpButtons()
	{
		final String css_id = "default_button";
		save.setId(css_id);
		back.setId(css_id);
		title.setId("heading");
		title.setPrefSize(360, 38);
		monitoredBrowse.setId(css_id);
		remove.setId(css_id);
		removeAll.setId(css_id);
		back.setOnAction(BACK_LISTENER);
		monitoredBrowse.setOnAction(BROWSE_LISTENER);
		save.setOnAction(SAVE_LISTENER);
		remove.setOnAction(REMOVE_BUTTON_LISTENER);
		removeAll.setOnAction(REMOVE_ALL_BUTTON_LISTENER);
	}
	
	private void setUpDirectorySettings()
	{
		directoryLabel.setId("label");
		headingPane.setId("heading_container");
		headingPane.setSpacing(10);
		this.setUpSearchSlider();
		this.setUpUpdateSlider();
		directoryPane.setTop(headingPane);
		HBox box = new HBox(monitoredBrowse, remove, removeAll, searchSlider, updateSlider);
		box.setSpacing(20);
		BorderPane.setMargin(box, new Insets(10d));
		directoryPane.setBottom(box);
		BorderPane.setMargin(directoryLabel, new Insets(10d));
		directoryPane.setId("button_container");
		container.getChildren().add(directoryPane);
	}
	
	private void setUpSearchSlider()
	{
		searchSlider.setShowTickMarks(true);
		searchSlider.setShowTickLabels(true);
		searchSlider.setMajorTickUnit(0.1f);
		searchSlider.setBlockIncrement(0.01f);
		searchSlider.setSnapToTicks(true);
		searchSlider.setOrientation(Orientation.HORIZONTAL);
		searchSlider.valueProperty().addListener(searchSliderListener);
	}
	
	private void setUpUpdateSlider()
	{
		updateSlider.setShowTickMarks(true);
		updateSlider.setShowTickLabels(true);
		updateSlider.setMajorTickUnit(1f);
		updateSlider.setBlockIncrement(0.01f);
		updateSlider.setSnapToTicks(true);
		updateSlider.setOrientation(Orientation.HORIZONTAL);
		updateSlider.valueProperty().addListener(updateSliderListener);
	}
	
	private void setUpNameSettings()
	{
		nameLabel.setId("label");
		nameTextArea.setMinSize(325, 25);
		nameTextArea.setText(NetworkGlobals.getServerName());
		namePane.setSpacing(10);
		namePane.setId("button_container");
		container.getChildren().add(namePane);
	}
	
	private void setUpPasswordSettings()
	{
		passwordLabel.setId("label");
		passwordTextArea.setMinSize(325, 25);
		passwordPane.setSpacing(10);
		passwordPane.setId("button_container");
		container.getChildren().add(passwordPane);
	}
	
	private void setUpCheckBoxSettings()
	{
		minimize.setOnAction(MINIMIZE_LISTENER);
		deepSearch.setOnAction(DEEP_SEARCH_LISTENER);
		musicMode.setOnAction(MUSIC_MODE_LISTENER);
		boxPane.setId("button_container");
		boxPane.setSpacing(10);
		container.getChildren().add(boxPane);
	}
	
	private void setUpBottomPane()
	{
		bottomPane.setId("button_container");
		bottomPane.setSpacing(50);
		container.getChildren().add(bottomPane);
	}
	
	private void setUpTooltips()
	{
		Tooltip monitored_tip  = new Tooltip("Set a new directory to monitor. A monitored directory will be searched in real time for media files to add to the server playlist.");
		monitored_tip.setId("tool_tip");
		Tooltip.install(directoryPane, monitored_tip);
		Tooltip name_tip = new Tooltip("Server name that is broadcast to the Android Client.");
		name_tip.setId("tool_tip");
		Tooltip.install(namePane, name_tip);
		Tooltip password_tip = new Tooltip("Password used to connect to the media server. Default is \"password\".");
		password_tip.setId("tool_tip");
		Tooltip.install(passwordPane, password_tip);
		Tooltip minimize_tip = new Tooltip("Enable/Disable minimizing window if the play button is pressed.");
		minimize.setId("tool_tip");
		Tooltip.install(minimize, minimize_tip);
		Tooltip deep_search_tip = new Tooltip("Enable/Disable deep search. If enabled, any directory found within the monitored directory will be searched for media files constantly. Disable deep search if you're experiencing performance issues; It is an expensive operation.");
		deep_search_tip.setId("tool_tip");
		Tooltip.install(deepSearch, deep_search_tip);
		Tooltip music_mode_tip = new Tooltip("Disables video and allows for playing music without the use of the window.");
		music_mode_tip.setId("tool_tip");
		Tooltip.install(musicMode, music_mode_tip);
		Tooltip submit_tip = new Tooltip("Submit server changes. Affects server name and password only.");
		submit_tip.setId("tool_tip");
		Tooltip.install(save, submit_tip);
		Tooltip back_tip= new Tooltip("Return to the main menu.");
		back_tip.setId("tool_tip");
		Tooltip.install(back, back_tip);
		Tooltip search_slider_tip = new Tooltip("Controls the delay between folder/file searches in milliseconds. Which increases/decreases the speed of the media servers search pattern. A lower delay will mean faster results but will take more performance.");
		search_slider_tip.setId("tool_tip");
		Tooltip.install(searchSlider, search_slider_tip);
		Tooltip update_slider_tip = new Tooltip("Controls the delay between folder/file updates in seconds. Which increases/decreases the speed of the media servers update completion. A lower delay will mean faster results but will take more performance.");
		update_slider_tip.setId("tool_tip");
		Tooltip.install(updateSlider, update_slider_tip);
	}
	
	public void updateCheckBoxes()
	{
		if(ApplicationGlobals.isDeepSearch())
			deepSearch.setSelected(true);
		else
			deepSearch.setSelected(false);
		
		if(ApplicationGlobals.isMinimizeWindows())
			minimize.setSelected(true);
		else
			minimize.setSelected(false);
		
		if(ApplicationGlobals.isMusicMode())
			musicMode.setSelected(true);
		else
			musicMode.setSelected(false);
		
		searchSlider.setValue((float) ApplicationGlobals.getSearchDelay() / 1000);
		updateSlider.setValue((float) ApplicationGlobals.getUpdateDelay() / 1000);
	}
	
	private void save()
	{
		// Attempt to update the monitored directory.
		handleDirectoryUpdate();
		// Attempt to update the password.
		handlePasswordUpdate();
		// Attempt to update the server name.
		handleNameUpdate();
		// Update the configuration file.
		new Configuration(true);
	}
	
	private void handleDirectoryUpdate()
	{
		if(directoryChanged)
		{
			// Set new download directory.
			//ApplicationGlobals.addMonitoredDirectory(directoryTextArea.getText());
			// Inform user of directory change.
			Tray.displayDownloadFolderChanged();
			directoryChanged = false;
		}
	}
	
	private void handlePasswordUpdate()
	{
		if(passwordTextArea.getText().length() >= 4)
		{
			setNewServerPassword();
			passwordTextArea.setText("");
		}
		else if(passwordTextArea.getText().length() > 0)
			Tray.displayMessage("Password notice!", "Minimum password length must be four or greater.");
	}
	
	private void handleNameUpdate()
	{
		String new_server_name = nameTextArea.getText();
		if(new_server_name.length() > 0 && !new_server_name.equals(NetworkGlobals.getServerName()))
		{
			setNewServerName();
			nameTextArea.setText(new_server_name);
		}
	}
	
	/**
	 * Handles the updating of the new server password. Once either the submit button or enter key is pressed; A call is made
	 * to this method. This method then retrieves the entered server password and checks if it is valid (i.e length greater 
	 * than minimum password length). If valid, the server password is then generated within the application directory. The user is 
	 * notified via the {@link Tray}.
	 */
	private void setNewServerPassword()
	{
		// Retrieve the entered password.
		final StringBuffer new_server_password = new StringBuffer(passwordTextArea.getText());
		// If the entered password is greater than the minimum password length...
		if(new_server_password.toString().length()>=MINIMUM_PASSWORD_LENGTH)
		{
			// Notify user of the change.
			Tray.displayPasswordChanged();
			// Generate new SHA3 key and save within the application directory.
			Sha3.generateSha3(new StringBuffer(new_server_password)).save(new StringBuffer(PASSWORD_PATH));
			// Reset configuration field text.
			passwordTextArea.setText("");
		}
		// If the entered password is less than the minimum password length.
		else
			JOptionPane.showMessageDialog(null, LanguageDelegator.getLanguageOfComponent(Languages.INVALID_PASSWORD_LENGTH), LanguageDelegator.getLanguageOfComponent(Languages.ERROR) , JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Handles the updating of the new server name. Once either the submit button or enter key is pressed; A call is made
	 * to this method. This method then retrieves the entered server name and checks if it is valid (i.e length greater than 0).
	 * If valid, the server name is then updated through {@link NetworkGlobals}, the user is notified via the {@link Tray} and
	 * finally the {@link Configuration} file is updated.
	 */
	private void setNewServerName()
	{
		// Retrieve the new server name.
		final StringBuffer new_server_name = new StringBuffer(nameTextArea.getText());
		// If the new server names length is greater than zero...
		if(new_server_name.toString().length() > 0)
		{
			// Update the server name.
			NetworkGlobals.setServerName(new_server_name.toString());
			// Set configuration field to have the same text.
			nameTextArea.setText(NetworkGlobals.getServerName());
			// Notify the user of the update.
			Tray.displayNameChanged();
		}
		// If the server name is blank...
		else
			// Pop up an error message.
			JOptionPane.showMessageDialog(null, new StringBuffer("Name cannot be blank."), new String("ERROR: Trying to set new name."), JOptionPane.ERROR_MESSAGE);
	}
}
