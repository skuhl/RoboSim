package ui;

import java.util.HashMap;
import java.util.Map;

import controlP5.ControlP5;
import global.Fields;
import processing.core.PConstants;
import processing.event.KeyEvent;

/**
 * Defines a variation of a dropdown list that allows the user to type text in
 * order to find an element in the list based off what the user types.
 * 
 * @author Joshua Hooker
 */
public class DropdownSearch extends MyDropdownList {
	
	/**
	 * The string used to filter the items in the dropdown list by there name.
	 */
	protected StringBuilder searchBuffer;
	
	public DropdownSearch(ControlP5 theControlP5, String name, int inputType) {
		super(theControlP5, name, inputType);
		searchBuffer = new StringBuilder("");
	}
	
	@Override
	public DropdownSearch clear() {
		super.clear();
		searchBuffer = new StringBuilder("");
		return this;
	}
	
	@Override
	public void keyEvent(KeyEvent e) {
		
		if (e.getKeyCode() == ControlP5.UP || e.getKeyCode() == ControlP5.DOWN
				|| e.getKeyCode() == ControlP5.LEFT ||
				e.getKeyCode() == ControlP5.RIGHT) {
			// Allow arrow key navigation of the list
			super.keyEvent(e);
			
		} else if (isInside && e.getAction() == KeyEvent.PRESS) {
			
			if (e.getKey() >= 32 && e.getKey() <= 126) {
				if (searchBuffer.length() < 16) {
					// Append a character to the end of the search buffer
					searchBuffer.append(e.getKey());
					reorderItems();
					open();
				}
				
			} else if (e.getKeyCode() == PConstants.BACKSPACE) {
				int lastIdx = searchBuffer.length() - 1;
				if (lastIdx >= 0) {
					// Delete the last character in the search buffer
					searchBuffer.deleteCharAt(lastIdx);
					reorderItems();
					open();
				}
				
			} else if (e.getKeyCode() == ControlP5.ENTER) {
				// Set the item at index 0 as active
				if (items.size() > 0) {
					Fields.debug("%s\n", items.get(0).get("name"));
					setValue(0);
					close();
				}
			}
		}
	}
	
	@Override
	public DropdownSearch setValue(float newValue) {
		super.setValue(newValue);
		// Reset the search buffer
		searchBuffer = new StringBuilder("");
		return this;
	}
	
	/**
	 * Orders the items in the dropdown list based on the edit distance between
	 * the search buffer and the names of each item.
	 */
	protected void reorderItems() {
		// Hold all elements in a temporary list
		HashMap<String, Integer> nameToED = new HashMap<>();
		
		/* Compute the edit distances between each item, with respect to its
		 * name, and the search buffer */
		for (int idx = 0; idx < items.size(); ++idx) {
			Map<String, Object> item = items.get(idx);
			String name = (String)item.get("name");
			Integer editDist = compareToSearch(name);
			
			nameToED.put(name, editDist);
		}
		
		// Sort items based off their edit distances
		for (int cdx = 1; cdx < items.size(); ++cdx) {
			Map<String, Object> curItem = items.get(cdx);
			int curED = nameToED.get(curItem.get("name"));
			
			int insertIdx = cdx;
			
			for (int idx = insertIdx - 1; idx >= 0; --idx) {
				Map<String, Object> compareItem = items.get(idx);
				int insertED = nameToED.get(compareItem.get("name"));
				
				if (insertED < curED) {
					break;
				}
				
				insertIdx = idx;
			}
			
			Map<String, Object> insertItem = items.get(insertIdx);
			
			if (insertIdx != cdx) {
				// Swap the items
				items.set(cdx, insertItem);
				items.set(insertIdx, curItem);
			}
		}
		
		// Update the dropdown label
		getCaptionLabel().setText(searchBuffer.toString());
	}
	
	/**
	 * Compares the given name to the dropdown's search buffer and returns an
	 * integer weight describing the difference between the search buffer and
	 * the given name. The lower the value is the closer of a match the given
	 * name is to the current search buffer.
	 * 
	 * @param name	The name of an item in this dropdown list
	 * @return		The integer weight computed after comparing the given name
	 * 				to the search buffer
	 */
	private int compareToSearch(String name) {
		if (searchBuffer.length() == 0) {
			// buffer is undefined
			return 0;
		}
		
		String normName = name.toLowerCase();
		String normBuf = searchBuffer.toString().toLowerCase();
		
		boolean isPrefix = normName.startsWith(normBuf);
		boolean isSubString;
		
		if (!isPrefix) {
			isSubString = normName.contains(normBuf);
			
		} else {
			isSubString = isPrefix;
		}
		
		/* Compute the edit distance between the search buffer and the given
		 * string without regard to letter case */
		int ED = Fields.editDistance(normName, normBuf);
		
		if (isPrefix) {
			return ED;
			
		} else {
			int diff = name.length() - searchBuffer.length();
			
			if (isSubString) {
				/* Add more to name, where the search buffer is non-prefix
				 * substring to give names, in order to give names, where the
				 * search buffer is a prefix, a higher priority. */
				return ED + diff;
				
			} else {
				/* Add even more to a name, where the search buffer is not a
				 * substring, in order to give names, where the search buffer
				 * is a substring a higher priority. */
				return (ED + diff) * (ED + diff);
			}
		}
	}
}
