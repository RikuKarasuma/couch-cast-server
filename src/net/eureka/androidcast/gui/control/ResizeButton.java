package net.eureka.androidcast.gui.control;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.eureka.androidcast.gui.control.handlers.ResizeHandler;

public final class ResizeButton extends CssButton
{
	private static final String CSS_ID = "resize_button";
	private static final float SIZE = 12.5f;
	
	public ResizeButton(Stage primary_stage, StackPane overlay)
	{
		super(CSS_ID, SIZE, SIZE);
		Button resize_button = this.getButton();
		new ResizeHandler(primary_stage, resize_button);
		overlay.getChildren().add(resize_button);
		StackPane.setAlignment(resize_button, Pos.BOTTOM_RIGHT);
	}
}
