package net.eureka.androidcast.gui.control.handlers;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public final class MoveHandler 
{
	
	private float xOffset = 0, yOffset = 0;
	
	public MoveHandler(final Stage primary_stage, Pane root_movable)
	{
		root_movable.setOnMousePressed(new EventHandler<MouseEvent>() 
		{
            @Override
            public void handle(MouseEvent event) 
            {
            	xOffset = (float) event.getSceneX();
            	yOffset = (float) event.getSceneY();
            }
        });
		root_movable.setOnMouseDragged(new EventHandler<MouseEvent>() 
		{
            @Override
            public void handle(MouseEvent event)
            {
            	primary_stage.setX(event.getScreenX() - xOffset);
            	primary_stage.setY(event.getScreenY() - yOffset);

            }
        });
	}
}
