package net.eureka.couchcast.gui;

import java.io.File;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.eureka.couchcast.foundation.file.manager.DirectoryFactory;
import net.eureka.couchcast.gui.control.WindowControl;
import net.eureka.couchcast.gui.control.handlers.DoubleClickSizeHandler;
import net.eureka.couchcast.gui.control.handlers.MoveHandler;
import net.eureka.couchcast.gui.playlist.PlaylistViewer;

public final class MainMenu extends Scene
{
	private PlaylistViewer viewer = null;
	
	private final EventHandler<ActionEvent> LOAD_LISTENER = new EventHandler<ActionEvent>()
	{
		@Override
		public final void handle(ActionEvent e) 
		{
			DirectoryChooser monitored_chooser = new DirectoryChooser();
			monitored_chooser.setTitle("Choose directory to load");
			File choosen_dir = monitored_chooser.showDialog(null);
			if(choosen_dir != null)
				DirectoryFactory.addNewScanner(choosen_dir.getAbsolutePath());
			//new DirectoryScanner(choosen_dir.getAbsolutePath(), true);
		}
		
		
	};
	
	private final EventHandler<ActionEvent> SHUTDOWN_LISTENER = new EventHandler<ActionEvent>()
	{
		@Override
		public final void handle(ActionEvent e) 
		{
			System.exit(0);
		}
	};
	
	private final EventHandler<ActionEvent> SETTINGS_LISTENER = new EventHandler<ActionEvent>()
	{
		@Override
		public final void handle(ActionEvent e) 
		{
			parent.switchToSettings();
		}
	};
	
	private StackPane root = null;
	private BorderPane background = new BorderPane();
	
	private Menu parent = null;
	
	//private final Image image = new Image(getClass().getResourceAsStream("image/logo_text_huge.png"));
	
	private final Button settingsButton = new Button("Settings"),
								helpButton = new Button("Help"),
								loadButton = new Button("Load"),
								shutdownButton = new Button("Shutdown");
	
	private final Button heading = new Button();
	
	private final FlowPane buttonContainer = new FlowPane(loadButton, settingsButton, helpButton, shutdownButton), headingContainer = new FlowPane(Orientation.VERTICAL, heading, buttonContainer);
	
	MainMenu(Stage parent_stage, Menu parent_container)
	{
		super(new StackPane());
		this.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		this.parent = parent_container;
		this.root = (StackPane) this.getRoot();
		this.root.getChildren().add(this.background);
		
		// Define window controls
		new MoveHandler(parent_stage, this.background);
		new DoubleClickSizeHandler(parent_stage, this.background);
		new WindowControl(parent_stage, this.root);
		
		
		setUpComponents();
	}
	
	private void setUpComponents()
	{
		background.setId("container");
		background.setTop(headingContainer);
		viewer = new PlaylistViewer(background);
		setUpButtons();
		setUpTooltips();
	}
	
	private void setUpButtons()
	{
		heading.setId("heading");
		heading.setMinSize(360, 38);
		settingsButton.setPrefSize(125, 40);
		settingsButton.setId("default_button");
		settingsButton.setOnAction(SETTINGS_LISTENER);
		helpButton.setId("default_button");
		helpButton.setPrefSize(125, 40);
		loadButton.setId("default_button");
		loadButton.setPrefSize(125, 40);
		loadButton.setOnAction(LOAD_LISTENER);
		shutdownButton.setId("default_button");
		shutdownButton.setPrefSize(125, 40);
		shutdownButton.setOnAction(SHUTDOWN_LISTENER);
		setUpButtonContainer();
	}
	
	private void setUpButtonContainer()
	{
		headingContainer.setPrefSize(600, 120);
		headingContainer.setId("heading_container");
		headingContainer.setVgap(10);
		buttonContainer.setPrefSize(600, 45);
		buttonContainer.setHgap(5);
		buttonContainer.setVgap(5);
	}
	
	private void setUpTooltips()
	{
		Tooltip loading_tip = new Tooltip("Load a directory of media files.");
		loading_tip.setId("tool_tip");
		Tooltip.install(loadButton, loading_tip);
		Tooltip settings_tip = new Tooltip("Server settings. Such as server name, password and monitored media directory.");
		settings_tip.setId("tool_tip");
		Tooltip.install(settingsButton, settings_tip);
		Tooltip help_tip = new Tooltip("Opens the help page for further information on how to use the media server.");
		help_tip.setId("tool_tip");
		Tooltip.install(helpButton, help_tip);
		Tooltip shutdown_tip = new Tooltip("Shutdown the media server.");
		shutdown_tip.setId("tool_tip");
		Tooltip.install(shutdownButton, shutdown_tip);
	}
	
	public void updatePlaylist()
	{
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() 
			{
				if(viewer != null)
					viewer.setUpData();
			}
		});
	}
}
