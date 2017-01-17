package screen;

import window.MenuScroll;

public class ScreenDefault extends Screen {
	public ScreenDefault(MenuScroll menu) {
		super(ScreenMode.DEFAULT, menu);
	}

	@Override
	public void loadText() {
		this.contents.getContents().clear();
	}

	@Override
	public void loadVars() {}

	@Override
	public void loadPrev() {}

	@Override
	public void actionUp() {}

	@Override
	public void actionDn() {}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}
}
