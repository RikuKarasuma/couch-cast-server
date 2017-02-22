package net.eureka.androidcast.gui.control.handlers;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public final class ResizeHandler
{
	
	public ResizeHandler(final Stage primary_stage, Button root)
	{
		root.setOnMouseDragged(new EventHandler<MouseEvent>()
		{
	        public void handle(MouseEvent event) 
	        {
	        	double newX = event.getScreenX() - primary_stage.getX() + 13;
	            double newY = event.getScreenY() - primary_stage.getY() + 10; 
	            if (newX % 5 == 0 || newY % 5 == 0)
	            {
	            	final float min_width = (float) primary_stage.getMinWidth(),
	            				min_height = (float) primary_stage.getMinHeight(); 
	                if (newX > min_width) 
	                	primary_stage.setWidth(newX);
	                else
	                	primary_stage.setWidth(min_width);

	                if (newY > min_height)
	                	primary_stage.setHeight(newY);
	                else 
	                	primary_stage.setHeight(min_height);
	            }
	        }
	    });
	}
	
}
