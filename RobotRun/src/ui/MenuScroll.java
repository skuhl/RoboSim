package ui;

import java.util.ArrayList;

import global.RMath;

public class MenuScroll {
	private final String name;
	private int maxDisp;
	private int xPos;
	private int yPos;
	
	private ArrayList<DisplayLine> lines;
	private boolean[] lineSelect;
	
	private int lineIdx;
	private int columnIdx;
	private int renderStart;
	
	public MenuScroll(String n, int max, int x, int y) {
		name = n;
		
		maxDisp = max;
		xPos = x;
		yPos = y;
		
		lines = new ArrayList<>();
		
		lineIdx = 0;
		columnIdx = 0;
	}
	
	public DisplayLine addLine(DisplayLine d) {
		lines.add(d);
		return d;
	}
	
	public DisplayLine addLine(int idx, String... lineTxt) {
		DisplayLine newLine = newLine(idx, lineTxt);
		lines.add(newLine);
		return newLine;
	}
	
	public DisplayLine addLine(String... lineTxt) {
		lines.add(newLine(lines.size(), lineTxt));
		return lines.get(lines.size() - 1);
	}
	
	public void clear() {
		lines.clear();
	}
	
	/**
	 * @return	A copy of the current contents of the display line
	 */
	public ArrayList<DisplayLine> copyContents() {
		ArrayList<DisplayLine> copy = new ArrayList<DisplayLine>();
		for(DisplayLine d: lines) {
			copy.add(d.clone());
		}
		
		return copy;
	}
	
	public DisplayLine get(int i) {
		return lines.get(i);
	}
	
	public int getColumnIdx() {
		return columnIdx;
	}
	
	/**
	 * TODO comment
	 * 
	 * @return
	 */
	public DisplayLine getCurrentItem() {
		if (lineIdx >= 0 && lineIdx < lines.size()) {
			return lines.get(lineIdx);
		}
		
		return null;
	}
	
	/**
	 * TODO comment
	 * 
	 * @return
	 */
	public int getCurrentItemIdx() {
		DisplayLine active = getCurrentItem();
		
		if (active != null) {
			return active.getItemIdx();
		}
		
		return -1;
	}
	
	public int getItemColumnIdx() {
		int idx = columnIdx;
		for(int i = lineIdx - 1; i >= 0; i -= 1) {
			if(lines.get(i).getItemIdx() != lines.get(i + 1).getItemIdx()) break;
			idx += lines.get(i).size();
		}

		return idx;
	}
	
	public int getItemIdx() {
		if(lineIdx < lines.size() && lineIdx >= 0)
			return lines.get(lineIdx).getItemIdx();
		else
			return -1;
	}
	
	public int getItemLineIdx() {
		int row = 0;
		DisplayLine currRow = getCurrentItem();
		while (lineIdx - row >= 0 && currRow.getItemIdx() == lines.get(lineIdx - row).getItemIdx()) {
			row += 1;
		}

		return row - 1;
	}
	
	public int getLineIdx() {
		return lineIdx;
	}
	
	public int getMaxDisplay() {
		return maxDisp;
	}
	
	public String getName() {
		return name;
	}
	
	public int getRenderStart() {
		return renderStart;
	}
	
	public boolean[] getSelection() {
		return lineSelect;
	}
	
	public int getXPos() {
		return xPos;
	}
	
	public int getYPos() {
		return yPos;
	}

	public boolean isSelected(int idx) {
		return lineSelect != null && idx >= 0 && idx < lineSelect.length
				&& lineSelect[idx];
	}

	public int moveDown(boolean page) {
		int size = lines.size();  

		if (page) {
			// Move display frame down an entire screen's display length
			int prevIdx = getItemIdx();			
			lineIdx = Math.min(size - 1, lineIdx + (maxDisp - 1));
			renderStart = (Math.max(0, Math.min(size - maxDisp, renderStart + (maxDisp - 1))));
			for(; prevIdx <= getItemIdx() && lineSelect != null; prevIdx += 1) {
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
		
		return getItemColumnIdx();
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
		
		return getItemColumnIdx();
	}
	
	public int moveUp(boolean page) {
		if (page) {
			// Move display frame up an entire screen's display length
			int prevIdx = getItemIdx();
			lineIdx = (Math.max(0, lineIdx - (maxDisp - 1)));
			renderStart = (Math.max(0, renderStart - (maxDisp - 1)));
			for(; prevIdx >= getItemIdx() && lineSelect != null; prevIdx -= 1) {
				toggleSelect(prevIdx);
			}
		} 
		else {
			// Move up a single row
			lineIdx = (Math.max(0, lineIdx - 1));
		}

		return getItemIdx();
	}
	
	public void reset() {
		lines.clear();
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
	
	public void setRenderStart(int renStart) {
		renderStart = renStart;
	}

	public void setSelectedColumnIdx(int i) {
		columnIdx = i;
	}
	
	public void setSelectedLineIdx(int i) {
		lineIdx = i;
	}
	
	public int size() {
		return lines.size();
	}
	
	public boolean toggleSelect(int idx) {
		if (lineSelect != null && idx >= 0 && idx < lineSelect.length) {
			return lineSelect[idx] = !lineSelect[idx];
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		String out = new String();
		
		for (DisplayLine l : lines) {
			
			out += l.toString();
			
		}
		
		
		return out;
	}
	
	public void updateRenderIndices() {
		if(lines.size() > 0) {
			lineIdx = RMath.clamp(lineIdx, -1, lines.size() - 1);
			int limboLnIdx = (lineIdx == -1) ? 0 : lineIdx;
			columnIdx = RMath.clamp(columnIdx, -1, lines.get(limboLnIdx).size() - 1);
			renderStart = RMath.clamp(renderStart, limboLnIdx - (maxDisp - 1), limboLnIdx);
			
		} else {
			lineIdx = -1;
			columnIdx = -1;
			renderStart = 0;
		}	
	}
	
	private DisplayLine newLine(int itemIdx, String... columns) {
		DisplayLine line =  new DisplayLine(itemIdx);

		for(String col : columns) {
			line.add(col);
		}

		return line;
	}
}
