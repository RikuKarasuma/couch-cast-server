package net.eureka.androidcast.gui.control;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public final class WindowControl 
{
	
	private static final float SPACING = 5f;
	private final HBox container = new HBox(SPACING);
	
	public WindowControl(Stage primary_stage, StackPane overlay)
	{
		new ResizeButton(primary_stage, overlay);
		container.setId("control_container");
		container.setPrefSize(100, 25);
		container.setMaxSize(100, 25);
		container.setPickOnBounds(false);
		new MinimizeButton(primary_stage, container);
		new MaximizeButton(primary_stage, container);
		new CloseButton(primary_stage, container);
		overlay.getChildren().add(container);
		StackPane.setAlignment(container, Pos.TOP_RIGHT);
	}
}
