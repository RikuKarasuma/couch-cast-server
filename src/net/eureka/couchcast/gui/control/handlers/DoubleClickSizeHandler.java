package net.eureka.couchcast.gui.control.handlers;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public final class DoubleClickSizeHandler
{
	public DoubleClickSizeHandler(final Stage primary_stage, Pane root_movable) 
	{
		root_movable.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event)
		    {
				if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) 
					primary_stage.setMaximized(!primary_stage.isMaximized());
		    }
		});
	}
}
