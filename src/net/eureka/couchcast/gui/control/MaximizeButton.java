package net.eureka.couchcast.gui.control;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.eureka.couchcast.gui.control.handlers.MaximizeHandler;

public final class MaximizeButton extends CssButton
{
	private static final String CSS_ID = "maximize_button";
	
	public MaximizeButton(Stage primary_stage, HBox container)
	{
		super(CSS_ID, CssButton.DEFAULT_SIZE, CssButton.DEFAULT_SIZE);
		Button maximise_button = this.getButton();
		new MaximizeHandler(primary_stage, maximise_button);
		container.getChildren().add(maximise_button);
	}
}
