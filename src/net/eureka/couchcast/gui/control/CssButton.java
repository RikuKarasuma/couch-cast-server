package net.eureka.couchcast.gui.control;

import javafx.scene.control.Button;

public abstract class CssButton 
{
	public static final float DEFAULT_SIZE = 25f;
	
	private Button button = new Button();
	
	public CssButton(final String css_id, final float width, final float height)
	{
		this.button.setId(css_id);
		this.button.setPrefSize(width, height);
	}
	
	public final Button getButton()
	{
		return button;
	}
}
