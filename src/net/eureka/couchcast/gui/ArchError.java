package net.eureka.couchcast.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.eureka.couchcast.Static;
import net.eureka.couchcast.foundation.init.ApplicationGlobals;

/**
 * Creates a GUI messsage to inform the user that this is a 64 bit server and the operating system is 32 bit. Provides
 * a link to download the 32 bit version. Created from {@link Menu}.
 * 
 * @author Owen McMonagle.
 * 
 * @see Menu
 * 
 * @version 0.1
 */
public final class ArchError extends Application 
{
	private final BorderPane CONTAINER = new BorderPane();
	
	/**
	 * Test method.
	 */
	public static void main(String[] args)
	{
		launch(ArchError.class);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		Platform.setImplicitExit(true);
		
		primaryStage.setTitle(ApplicationGlobals.getNameAndVersion());
		primaryStage.setWidth(400);
		primaryStage.setHeight(100);
		primaryStage.setResizable(false);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("image/logo.png")));
		
		primaryStage.setScene(new ErrorMenu(CONTAINER));
		primaryStage.show();
	}
	
	
	private final class ErrorMenu extends Scene
	{
		
		private final String ERROR_MESSAGE_STR = "This operating system is 32 bit. Please download the 32 \nbit version below.",
							 DOWNLOAD_LINK_STR = "http://www.couchcastapp.com";
		
		private final Label ERROR_MESSAGE = new Label(ERROR_MESSAGE_STR);
		private final Hyperlink DOWNLOAD_LINK = new Hyperlink(DOWNLOAD_LINK_STR);
		private final FlowPane ERROR_FLOW_CONTAINER = new FlowPane(ERROR_MESSAGE, DOWNLOAD_LINK);
		
		private final EventHandler<ActionEvent> LINK_LISTENER = new EventHandler<ActionEvent>() 
		{
			@Override
			public void handle(ActionEvent event) 
			{
				Static.openWebpage(DOWNLOAD_LINK_STR);
				System.exit(0);
			}
		};
		
		public ErrorMenu(Parent root) 
		{
			super(root);
			this.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			ERROR_MESSAGE.setId("message");
			ERROR_MESSAGE.setTextFill(Color.WHITE);
			DOWNLOAD_LINK.setOnAction(LINK_LISTENER);
			DOWNLOAD_LINK.setTextFill(Color.RED);
			CONTAINER.setId("container");
			CONTAINER.setCenter(ERROR_FLOW_CONTAINER);
		}
	}

}
