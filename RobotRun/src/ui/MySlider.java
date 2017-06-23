package ui;

import controlP5.ControlP5;
import controlP5.Slider;

/**
 * Defines an extension of the ControlP5 Slider class that includes an input
 * type implementation.
 * 
 * @author Joshua Hooker
 */
public class MySlider extends Slider implements UIInputElement {
	
	private int inputType;
	
	public MySlider(ControlP5 theControlP5, String name, int inputType) {
		super(theControlP5, name);
		
		this.inputType = inputType;
	}
	
	@Override
	public void clearInput() {
		setValue(0f);
	}

	@Override
	public int getInputType() {
		return inputType;
	}

}
