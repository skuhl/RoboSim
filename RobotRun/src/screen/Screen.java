package screen;

import core.RobotRun;
import ui.MenuScroll;

public abstract class Screen {
	public final ScreenMode mode;
	protected final RobotRun robotRun;
	
	protected final String header;
	protected MenuScroll contents;
	protected MenuScroll options;
	protected String[] labels;
	
	public Screen(ScreenMode m, String header, RobotRun r) {
		mode = m;
		robotRun = r;
		
		this.header = header;
		contents = new MenuScroll("cont", 8, 10, 20);
		options = new MenuScroll("opt", 3, 10, 180);
		labels = new String[5];
	}
	
	public Screen(ScreenMode m, RobotRun r) {
		mode = m;
		robotRun = r;
		
		header = loadHeader();
		contents = new MenuScroll("cont", 8, 10, 20);
		options = new MenuScroll("opt", 3, 10, 180);
		labels = new String[5];
	}
	
	public void updateScreen() {
		contents.clear();
		options.clear();
		
		loadContents();
		loadOptions();
		loadLabels();
	}
	
	public void updateScreen(ScreenState s) {
		updateScreen();
		loadVars(s);
		
		printScreenInfo();
	}
	
	//Used for displaying screen text
	public String getHeader() { return header; }
	public MenuScroll getContents() { return contents; }
	public MenuScroll getOptions() { return options; }
	public String[] getLabels() { return labels; }
	
	public int getContentIdx() { return contents.getLineIdx(); }
	public int getContentColIdx() { return contents.getColumnIdx(); }
	public int getContentStart() { return contents.getRenderStart(); }

	public int getOptionIdx() { return options.getLineIdx(); }
	public int getOptionStart() { return options.getRenderStart(); }
	
	public void setContentIdx(int i) { contents.setSelectedLineIdx(i); }
	
	public ScreenState getScreenState() {
		ScreenState s = new ScreenState(mode, contents.getLineIdx(), contents.getColumnIdx(),
				contents.getRenderStart(), options.getLineIdx(), options.getRenderStart());
		
		return s;
	}
		
	//Loads given set of screen state variables 
	public void setScreenIndices(int contLine, int col, int contRS, int optLine, int optRS) {
		contents.setSelectedLineIdx(contLine);
		contents.setSelectedColumnIdx(col);
		contents.setRenderStart(contRS);
		
		options.setSelectedLineIdx(optLine);
		options.setRenderStart(optRS);
	}
	
	public void printScreenInfo() {
		System.out.println("Current screen: ");
		System.out.println("\tMode: " + mode.name());
		System.out.println("\tRow: " + contents.getLineIdx() + ", col: " + contents.getColumnIdx() +
				", RS: " + contents.getRenderStart());
		System.out.println("\tOpt row: " + options.getLineIdx() + ", opt RS: " + options.getRenderStart());
	}
	
	//Sets text for each screen
	protected abstract String loadHeader();
	protected abstract void loadContents();
	protected abstract void loadOptions();
	protected abstract void loadLabels();
	protected abstract void loadVars(ScreenState s);
		
	//Button actions
	public abstract void actionKeyPress(char key);
	public abstract void actionUp();
	public abstract void actionDn();
	public abstract void actionLt();
	public abstract void actionRt();
	public abstract void actionEntr();
	public abstract void actionBkspc();
	public abstract void actionF1();
	public abstract void actionF2();
	public abstract void actionF3();
	public abstract void actionF4();
	public abstract void actionF5();
}
