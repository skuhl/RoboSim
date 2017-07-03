package ui;
import java.util.ArrayList;

public class DisplayLine {
	ArrayList<String> fields;
	private int itemIdx;
	private int xAlign;

	public DisplayLine() {
		fields = new ArrayList<>();
		itemIdx = -1;
		xAlign = 0;
	}

	public DisplayLine(int idx) {
		fields = new ArrayList<>();
		itemIdx = idx;
		xAlign = 0;
	}
	
	public DisplayLine(int idx, int align) {
		fields = new ArrayList<>();
		itemIdx = idx;
		xAlign = align;
	}

	public DisplayLine(int idx, int align, String... strings) {
		fields = new ArrayList<>();
		for(String col : strings) {
			fields.add(col);
		}
		
		itemIdx = idx;
		xAlign = align;
	}
	
	public DisplayLine(int idx, String... strings) {
		fields = new ArrayList<>();
		for(String col : strings) {
			fields.add(col);
		}
		
		itemIdx = idx;
		xAlign = 0;
	}

	public void add(int i, String s) {
		fields.add(i, s);
	}

	public boolean add(String s) {
		return fields.add(s);
	}

	public DisplayLine clone() {
		DisplayLine clone = new DisplayLine(itemIdx, xAlign);
		for(String s: fields) {
			clone.add(s);
		}
		
		return clone;
	}

	public String get(int idx) {
		return fields.get(idx);
	}

	public int getItemIdx() {
		return itemIdx;
	}
	


	public int getxAlign() {
		return xAlign;
	}

	public String remove(int i) {
		return fields.remove(i);
	}

	public String set(int i, String s) {
		return fields.set(i, s);
	}

	public void setItemIdx(int idx) {
		itemIdx = idx;
	}

	public void setxAlign(int offset) {
		xAlign = offset;
	}

	public int size() {
		return fields.size();
	}
	
	@Override
	public String toString() {
		return fields.toString();
	}
}