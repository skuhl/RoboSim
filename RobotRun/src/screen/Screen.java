package screen;

import core.RobotRun;
import enums.ScreenMode;
import ui.MenuScroll;

public abstract class Screen {
	public final ScreenMode mode;
	protected final RobotRun robotRun;
	protected final String header;
	protected final MenuScroll contents;
	protected final MenuScroll options;
	protected final String[] labels;
	
	public static Screen getScreen(ScreenMode m, RobotRun r) {
		switch(m) {
		case DEFAULT: return new ScreenDefault(r);
		case NAV_MAIN_MENU: return new ScreenMainMenu(r);
		case NAV_PROGRAMS: return new ScreenProgs(r);
		case NAV_PROG_INSTR: return new ScreenProgInstructions(r);
		default: return null;
		}
	}
	
	public Screen(ScreenMode m, RobotRun r) {
		mode = m;
		robotRun = r;
		header = loadHeader();
		contents = new MenuScroll("cont", 8, 10, 20);
		options = new MenuScroll("opt", 3, 10, 180);
		labels = loadLabels();
		
		loadContents();
		loadOptions();
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
	
	//Sets text for each screen
	abstract String loadHeader();
	abstract MenuScroll loadContents();
	abstract MenuScroll loadOptions();
	abstract String[] loadLabels();
	
	//Used when switching between screens
	public abstract void loadVars();
	public abstract void loadPrev();
	
	//Button actions
	public abstract void actionUp();
	public abstract void actionDn();
	public abstract void actionLt();
	public abstract void actionRt();
	public abstract void actionEntr();
	public abstract void actionF1();
	public abstract void actionF2();
	public abstract void actionF3();
	public abstract void actionF4();
	public abstract void actionF5();
}
