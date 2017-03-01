package window;

import java.util.ArrayList;

import global.Fields;
import robot.RobotRun;
import screen.ScreenMode;
import screen.ScreenType;

public class MenuScroll {
	private final RobotRun robotRun;
	private final String name;
	private int maxDisp;
	private int xPos;
	private int yPos;
	
	private ArrayList<DisplayLine> lines;
	private boolean[] lineSelect;
	
	private int lineIdx;
	private int columnIdx;
	private int renderStart;
	
	public MenuScroll(RobotRun r, String n, int max, int x, int y) {
		robotRun = r;
		name = n;
		
		maxDisp = max;
		xPos = x;
		yPos = y;
		
		lines = new ArrayList<DisplayLine>();
		
		lineIdx = 0;
		columnIdx = 0;
	}
	
	public DisplayLine addLine(String... lineTxt) {
		lines.add(newLine(lines.size(), lineTxt));
		return lines.get(lines.size() - 1);
	}
	
	public DisplayLine addLine(int idx, String... lineTxt) {
		DisplayLine newLine = newLine(idx, lineTxt);
		lines.add(newLine);
		return newLine;
	}
	
	public DisplayLine addLine(DisplayLine d) {
		lines.add(d);
		return d;
	}
	
	public void clear() {
		lines.clear();
	}
	
	/**
	 * @param screen
	 */
	public void drawLines(ScreenMode screen) {
		boolean selectMode = false;
		if(screen.getType() == ScreenType.TYPE_LINE_SELECT) { selectMode = true; } 
		
		if(lines.size() > 0) {
			lineIdx = RobotRun.clamp(lineIdx, 0, lines.size() - 1);
			columnIdx = RobotRun.clamp(columnIdx, 0, lines.get(lineIdx).size() - 1);
			renderStart = RobotRun.clamp(renderStart, lineIdx - (maxDisp - 1), lineIdx);
		} else {
			lineIdx = 0;
			columnIdx = 0;
			renderStart = 0;
		}
				
		int next_px = 0, next_py = 0; 
		int itemNo = 0, lineNo = 0;
		int bg, txt, selectInd = -1;
		
		for(int i = renderStart; i < lines.size() && lineNo < maxDisp; i += 1) {
			//get current line
			DisplayLine temp = lines.get(i);
			next_px = temp.getxAlign();

			if(i == 0 || lines.get(i - 1).getItemIdx() != lines.get(i).getItemIdx()) {
				selectInd = lines.get(i).getItemIdx();
				if(lines.get(lineIdx).getItemIdx() == selectInd) { bg = Fields.UI_DARK;  }
				else												{ bg = Fields.UI_LIGHT; }
				
				//leading row select indicator []
				robotRun.getCp5().addTextarea(name + itemNo)
				.setText("")
				.setPosition(xPos + next_px, yPos + next_py)
				.setSize(10, 20)
				.setColorBackground(bg)
				.hideScrollbar()
				.moveTo(robotRun.g1);
			}

			itemNo += 1;
			next_px += 10;
			
			//draw each element in current line
			for(int j = 0; j < temp.size(); j += 1) {
				if(i == lineIdx) {
					if(j == columnIdx && !selectMode){
						//highlight selected row + column
						txt = Fields.UI_LIGHT;
						bg = Fields.UI_DARK;          
					} 
					else if(selectMode && lineSelect != null && !lineSelect[temp.getItemIdx()]) {
						//highlight selected line
						txt = Fields.UI_LIGHT;
						bg = Fields.UI_DARK;
					}
					else {
						txt = Fields.UI_DARK;
						bg = Fields.UI_LIGHT;
					}
				} else if(selectMode && lineSelect != null && lineSelect[temp.getItemIdx()]) {
					//highlight any currently selected lines
					txt = Fields.UI_LIGHT;
					bg = Fields.UI_DARK;
				} else {
					//display normal row
					txt = Fields.UI_DARK;
					bg = Fields.UI_LIGHT;
				}

				//grey text for comme also this
				if(temp.size() > 0 && temp.get(0).contains("//")) {
					txt = robotRun.color(127);
				}

				robotRun.getCp5().addTextarea(name + itemNo)
				.setText(temp.get(j))
				.setFont(RobotRun.fnt_con14)
				.setPosition(xPos + next_px, yPos + next_py)
				.setSize(temp.get(j).length()*Fields.CHAR_WDTH + Fields.TXT_PAD, 20)
				.setColorValue(txt)
				.setColorBackground(bg)
				.hideScrollbar()
				.moveTo(robotRun.g1);

				itemNo += 1;
				next_px += temp.get(j).length()*Fields.CHAR_WDTH + (Fields.TXT_PAD - 8);
			} //end draw line elements

			//Trailing row select indicator []
			if(i == lines.size() - 1 || lines.get(i).getItemIdx() != lines.get(i + 1).getItemIdx()) {
				if(lines.get(lineIdx).getItemIdx() == selectInd) { txt = Fields.UI_DARK;  }
				else												{ txt = Fields.UI_LIGHT; }
				
				robotRun.getCp5().addTextarea(name + itemNo)
				.setText("")
				.setPosition(xPos + next_px, yPos + next_py)
				.setSize(10, 20)
				.setColorBackground(txt)
				.hideScrollbar()
				.moveTo(robotRun.g1);
			}

			next_px = 0;
			next_py += 20;
			itemNo += 1;
			lineNo += 1;
		}//end display contents
	}
	
	public DisplayLine get(int i) {
		return lines.get(i);
	}
	
	public int getColumnIdx() {
		return columnIdx;
	}
	
	public int getItemIdx() {
		if(lineIdx < lines.size() && lineIdx >= 0)
			return lines.get(lineIdx).getItemIdx();
		else
			return -1;
	}
	
	public int getLineIdx() {
		return lineIdx;
	}
	
	public int getRenderStart() {
		return renderStart;
	}
	
	public int getSelectedIdx() {
		int idx = columnIdx;
		for(int i = lineIdx - 1; i >= 0; i -= 1) {
			if(lines.get(i).getItemIdx() != lines.get(i + 1).getItemIdx()) break;
			idx += lines.get(i).size();
		}

		return idx;
	}
	
	public boolean[] getSelection() {
		return lineSelect;
	}
	
	public boolean isSelected(int idx) {
		return lineSelect[idx];
	}
	
	public int moveDown(boolean page) {
		int size = lines.size();  

		if (page) {
			// Move display frame down an entire screen's display length
			int prevIdx = lineIdx;			
			lineIdx = Math.min(size - 1, lineIdx + (maxDisp - 1));
			renderStart = (Math.max(0, Math.min(size - maxDisp, renderStart + (maxDisp - 1))));
			for(; prevIdx <= lineIdx && lineSelect != null; prevIdx += 1) {
				toggleSelect(prevIdx);
			}
		} else {
			// Move down a single row
			lineIdx = Math.min(size - 1, lineIdx + 1);
		}

		return getItemIdx();
	}

	public int moveLeft() {
		if(lineIdx > 0 && lines.get(lineIdx - 1).getItemIdx() == lines.get(lineIdx).getItemIdx()) {
			columnIdx = (columnIdx - 1);
			if(columnIdx < 0) {
				moveUp(false);
				columnIdx = (lines.get(lineIdx).size() - 1);
			}
		} else {
			columnIdx = (Math.max(0, columnIdx - 1));
		}
		
		return getSelectedIdx();
	}

	public int moveRight() {
		if(lineIdx < lines.size() - 1 && lines.get(lineIdx + 1).getItemIdx() == lines.get(lineIdx).getItemIdx()) {
			columnIdx = (columnIdx + 1);
			if(columnIdx > lines.get(lineIdx).size() - 1) {
				moveDown(false);
				columnIdx = (0);
			}
		} else {
			columnIdx = (Math.min(lines.get(lineIdx).size() - 1, columnIdx + 1));
		}
		
		return getSelectedIdx();
	}

	public int moveUp(boolean page) {
		if (page) {
			// Move display frame up an entire screen's display length
			int prevIdx = lineIdx;
			lineIdx = (Math.max(0, lineIdx - (maxDisp - 1)));
			renderStart = (Math.max(0, renderStart - (maxDisp - 1)));
			for(; prevIdx >= lineIdx && lineSelect != null; prevIdx -= 1) {
				toggleSelect(prevIdx);
			}
		} 
		else {
			// Move up a single row
			lineIdx = (Math.max(0, lineIdx - 1));
		}

		return getItemIdx();
	}
	
	private DisplayLine newLine(int itemIdx, String... columns) {
		DisplayLine line =  new DisplayLine(itemIdx);

		for(String col : columns) {
			line.add(col);
		}

		return line;
	}
	
	/**
	 * @return	A copy of the current contents of the display line
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<DisplayLine> copyContents() {
		return (ArrayList<DisplayLine>)lines.clone();
	}
	
	public void reset() {
		lineIdx = 0;
		columnIdx = 0;
		renderStart = 0;
	}
	
	// clears the array of selected lines
	public boolean[] resetSelection(int n) {
		if(n > 0) lineSelect = new boolean[n];
		else 	  lineSelect = null;
		return lineSelect;
	}
	
	public DisplayLine set(int i, DisplayLine d) {
		return lines.set(i, d);
	}
	
	public DisplayLine set(int i, String... columns) {
		return lines.set(i, newLine(i, columns));
	}
	
	public MenuScroll setLines(ArrayList<DisplayLine> l) {
		lines = l;
		
		return this;
	}
	
	public MenuScroll setLocation(int x, int y) {
		xPos = x;
		yPos = y;
		
		return this;
	}
	
	public MenuScroll setMaxDisplay(int max) {
		maxDisp = max;
		return this;
	}
	
	public MenuScroll setContents(ArrayList<DisplayLine> c) {
		lines = c;
		return this;
	}
	
	public void setColumnIdx(int i) {
		columnIdx = i;
	}

	public void setLineIdx(int i) {
		lineIdx = i;
	}
	
	public int size() {
		return lines.size();
	}
	
	public boolean toggleSelect(int idx) {
		return lineSelect[idx] = !lineSelect[idx];
	}
}
