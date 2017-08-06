package ui;

import controlP5.Button;
import controlP5.ControlP5;
import controlP5.ControllerGroup;

/**
 * An extension to the ControlP5 Button class that allows drag clicks.
 * 
 * @author Joshua Hooker
 */
public class MyButton extends Button {
	
	/**
	 * The text to render on the button, if it is active.
	 */
	private String actTxtLbl;
	
	/**
	 * The text to render on the button, if it is inactive.
	 */
	private String inActTxtLbl;
	
	public MyButton(ControlP5 theControlP5, String theName, String inActTxtLbl,
			String actTxtLbl) {
		
		super(theControlP5, theName);
		this.inActTxtLbl = inActTxtLbl;
		this.actTxtLbl = actTxtLbl;
		
		if (inActTxtLbl != null) {
			_myCaptionLabel.setText(inActTxtLbl);
		}
	}

	protected MyButton(ControlP5 theControlP5, ControllerGroup<?> theParent,
			String theName, float theDefaultValue, int theX, int theY, int
			theWidth, int theHeight, String inActTxtLbl, String actTxtLbl) {
		
		super(theControlP5, theParent, theName, theDefaultValue, theX, theY, 
				theWidth, theHeight);
		
		this.inActTxtLbl = inActTxtLbl;
		this.actTxtLbl = actTxtLbl;
		
		if (inActTxtLbl != null) {
			_myCaptionLabel.setText(inActTxtLbl);
		}
	}
	
	/**
	 * The text that is display on the button, when it is active.
	 * 
	 * @return	Active button label
	 */
	public String getActTxtLbl() {
		return actTxtLbl;
	}
	
	/**
	 * The text to display on the button, when it is inactive.
	 * 
	 * @return	Inactive button label
	 */
	public String getInActTxtLbl() {
		return inActTxtLbl;
	}
	
	@Override
	protected void activate() {
		super.activate();
		// Update the button's text based on its state
		if (actTxtLbl != null) {
			
			if (isOn) {
				getCaptionLabel().setText(actTxtLbl);
				
			} else if (inActTxtLbl != null) {
				getCaptionLabel().setText(inActTxtLbl);
			}
		}
	}
	
	@Override
	protected void onEndDrag() {
		// Allow drag clicks
		onClick();
	}
}
