package net.eureka.couchcast.gui.control;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.eureka.couchcast.gui.control.handlers.MinimizeHandler;

public final class MinimizeButton extends CssButton
{
	private static final String CSS_ID = "minimize_button";
	
	public MinimizeButton(Stage primary_stage, HBox container)
	{
		super(CSS_ID, CssButton.DEFAULT_SIZE, CssButton.DEFAULT_SIZE);
		Button minimize_button = this.getButton();
		new MinimizeHandler(primary_stage, minimize_button);
		container.getChildren().add(minimize_button);
	}
}
