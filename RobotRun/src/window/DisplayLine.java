package window;
import java.util.ArrayList;

public class DisplayLine {
	ArrayList<String> contents;
	private int itemIdx;
	private int xAlign;

	public DisplayLine() {
		contents = new ArrayList<String>();
		setItemIdx(-1);
		setxAlign(0);
	}

	public DisplayLine(int idx) {
		contents = new ArrayList<String>();
		setItemIdx(idx);
		setxAlign(0);
	}

	public DisplayLine(int idx, int align) {
		contents = new ArrayList<String>();
		setItemIdx(idx);
		setxAlign(align);
	}

	public DisplayLine(ArrayList<String> c, int idx, int align) {
		contents = c;
		setItemIdx(idx);
		setxAlign(align);
	}

	public int size() {
		return contents.size();
	}

	public String get(int idx) {
		return contents.get(idx);
	}

	public String set(int i, String s) {
		return contents.set(i, s);
	}

	public boolean add(String s) {
		return contents.add(s);
	}

	public void add(int i, String s) {
		contents.add(i, s);
	}

	public String remove(int i) {
		return contents.remove(i);
	}

	public int getItemIdx() {
		return itemIdx;
	}

	public void setItemIdx(int itemIdx) {
		this.itemIdx = itemIdx;
	}

	public int getxAlign() {
		return xAlign;
	}

	public void setxAlign(int xAlign) {
		this.xAlign = xAlign;
	}
}