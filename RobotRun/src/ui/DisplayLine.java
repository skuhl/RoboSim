package ui;
import java.util.ArrayList;

public class DisplayLine {
	ArrayList<String> fields;
	private int itemIdx;
	private int xAlign;

	public DisplayLine() {
		fields = new ArrayList<>();
		setItemIdx(-1);
		setxAlign(0);
	}

	public DisplayLine(int idx) {
		fields = new ArrayList<>();
		setItemIdx(idx);
		setxAlign(0);
	}
	
	public DisplayLine(int idx, int align) {
		fields = new ArrayList<>();
		setItemIdx(idx);
		setxAlign(align);
	}

	public DisplayLine(int idx, int align, String... strings) {
		fields = new ArrayList<>();
		for(String col : strings) {
			fields.add(col);
		}
		
		setItemIdx(idx);
		setxAlign(align);
	}
	
	public DisplayLine(int idx, String... strings) {
		fields = new ArrayList<>();
		for(String col : strings) {
			fields.add(col);
		}
		
		setItemIdx(idx);
		setxAlign(0);
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

	public void setItemIdx(int itemIdx) {
		this.itemIdx = itemIdx;
	}

	public void setxAlign(int xAlign) {
		this.xAlign = xAlign;
	}

	public int size() {
		return fields.size();
	}
	
	@Override
	public String toString() {
		return fields.toString();
	}
}