package screen;

import enums.ScreenMode;
import robot.RobotRun;

public abstract class Screen {
	public final ScreenMode mode;
	protected final RobotRun robotRun;
	protected MenuScroll contents;
	protected MenuScroll options;
	protected String header;
	protected String[] labels;
	
	public Screen(ScreenMode m, RobotRun r) {
		mode = m;
		robotRun = r;
		contents = new MenuScroll("cont", 8, 10, 20);
		options = new MenuScroll("opt", 3, 10, 180);
		labels = new String[5];
	}
	
	public void loadText() {
		loadHeader();
		loadContents();
		loadOptions();
		loadLabels();
	}
	
	public String getHeader() { return header; }
	public MenuScroll getContents() { return contents; }
	public MenuScroll getOptions() { return options; }
	public String[] getLabels() { return labels; }
	
	abstract void loadHeader();
	abstract void loadContents();
	abstract void loadOptions();
	abstract void loadLabels();
	
	public abstract void loadVars();
	public abstract void loadPrev();
	
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
