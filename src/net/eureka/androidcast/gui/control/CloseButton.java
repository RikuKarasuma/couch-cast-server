package net.eureka.androidcast.gui.control;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.eureka.androidcast.gui.control.handlers.CloseHandler;

public final class CloseButton extends CssButton 
{
	private static final String CSS_ID = "close_button";
	
	public CloseButton(Stage primary_stage, HBox container)
	{
		super(CSS_ID, CssButton.DEFAULT_SIZE, CssButton.DEFAULT_SIZE);
		Button close_button = this.getButton();
		new CloseHandler(primary_stage, close_button);
		container.getChildren().add(close_button);
	}
}
