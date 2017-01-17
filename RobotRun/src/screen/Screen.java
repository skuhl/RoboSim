package screen;

import window.MenuScroll;

public abstract class Screen {
	public final ScreenMode mode;
	protected MenuScroll contents;
	protected MenuScroll options;
	protected String header;
	protected String[] fnLabel;
	
	public Screen(ScreenMode m, MenuScroll menu) {
		mode = m;
		contents = menu;
		fnLabel = new String[5];
	}
	
	public Screen(ScreenMode m, MenuScroll menu1, MenuScroll menu2) {
		mode = m;
		contents = menu1;
		options = menu2;
		fnLabel = new String[5];
	}
	
	public abstract void loadText();
	public abstract void loadVars();
	public abstract void loadPrev();
	public abstract void actionUp();
	public abstract void actionDn();
	public abstract void actionLt();
	public abstract void actionRt();
}
