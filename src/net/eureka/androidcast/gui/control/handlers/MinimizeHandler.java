package net.eureka.androidcast.gui.control.handlers;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public final class MinimizeHandler 
{
	
	public MinimizeHandler(final Stage primary_stage, Button root_movable)
	{
		root_movable.setOnMouseReleased(new EventHandler<MouseEvent>() 
		{
            @Override
            public void handle(MouseEvent event) 
            {
            	primary_stage.setIconified(true);
            }
        });
	}
}
