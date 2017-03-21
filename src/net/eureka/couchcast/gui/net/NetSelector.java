package net.eureka.couchcast.gui.net;



import java.net.InetAddress;

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
import javafx.stage.Stage;
import net.eureka.couchcast.Start;
import net.eureka.couchcast.foundation.init.NetworkGlobals;
import net.eureka.couchcast.gui.AppStage;
import net.eureka.couchcast.gui.control.WindowControl;
import net.eureka.couchcast.gui.control.handlers.DoubleClickSizeHandler;
import net.eureka.couchcast.gui.control.handlers.MoveHandler;

/**
 * 
 * Small Menu which displays the available network interfaces to select. Only appears on first time start up, or if 
 * the config file has been deleted or the network interface is null.
 * 
 * @author Owen McMonagle.
 * 
 * @see AppStage
 * @see NetViewer
 * @see NetworkGlobals
 * 
 * @version 0.3
 */
public final class NetSelector extends Scene 
{ 
	
	private static boolean noNetwork = false;
	
	private NetViewer viewer = null;
	
	private final EventHandler<ActionEvent> EXIT_LISTENER = new EventHandler<ActionEvent>()
	{
		@Override
		public final void handle(ActionEvent e) 
		{
			System.exit(0);
		}
	};
	
	private final EventHandler<ActionEvent> ACCEPT_LISTENER = new EventHandler<ActionEvent>()
	{
		@Override
		public final void handle(ActionEvent e) 
		{
			InetAddress selected_network_address = viewer.getSelectedNetwork();
			String selected_network_address_name = viewer.getNetworkName();
			
			if(!noNetwork && !selected_network_address_name.isEmpty())
			{
				NetworkGlobals.setDhcpNetwork(selected_network_address, viewer.getNetworkName());
				Start.startNetworking();
				parent.createMainMenu();
			}
		}
	};
	
	private StackPane root = null;
	private BorderPane background = new BorderPane();
	
	private AppStage parent = null;
	
	//private final Image image = new Image(getClass().getResourceAsStream("image/logo_text_huge.png"));
	
	private final Button acceptButton = new Button("Accept"),
								exitButton = new Button("Exit");
	
	private final Button heading = new Button();
	
	private final FlowPane buttonContainer = new FlowPane(acceptButton, exitButton), bottomContainer = new FlowPane(Orientation.VERTICAL, buttonContainer);
	
	public NetSelector(Stage parent_stage, AppStage parent_container)
	{
		super(new StackPane());
		this.getStylesheets().add(AppStage.class.getResource("application.css").toExternalForm());
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
		background.setTop(heading);
		background.setBottom(bottomContainer);
		viewer = new NetViewer(background);
		setUpButtons();
		setUpTooltips();
	}
	
	private void setUpButtons()
	{
		heading.setId("heading");
		heading.setMinSize(360, 38);
		acceptButton.setPrefSize(125, 40);
		acceptButton.setId("default_button");
		acceptButton.setOnAction(ACCEPT_LISTENER);
		exitButton.setId("default_button");
		exitButton.setPrefSize(125, 40);
		exitButton.setOnAction(EXIT_LISTENER);
		setUpButtonContainer();
	}
	
	private void setUpButtonContainer()
	{
		bottomContainer.setPrefSize(600, 120);
		bottomContainer.setId("heading_container");
		bottomContainer.setVgap(10);
		buttonContainer.setPrefSize(600, 45);
		buttonContainer.setHgap(5);
		buttonContainer.setVgap(5);
	}
	
	private void setUpTooltips()
	{
		Tooltip accept_tip = new Tooltip("Accept the selected network interface.");
		accept_tip.setId("tool_tip");
		Tooltip.install(acceptButton, accept_tip);
		Tooltip exit_tip = new Tooltip("Exit.");
		exit_tip.setId("tool_tip");
		Tooltip.install(exitButton, exit_tip);
	}
	
	public void updatePlaylist()
	{
		Platform.runLater(new Runnable() 
		{
			
			@Override
			public void run() 
			{
				if(viewer != null)
					viewer.setUpData();
			}
		});
	}

	public static void setNoNetwork(boolean no_network) 
	{
		noNetwork = no_network;
	}
}
