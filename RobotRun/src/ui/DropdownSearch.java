package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import controlP5.ControlP5;
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
	 * The items that do not match the search buffer.
	 */
	protected List<Map<String, Object>> filteredItems;
	
	/**
	 * The string used to filter the items in the dropdown list by there name.
	 */
	protected StringBuilder searchBuffer;
	
	public DropdownSearch(ControlP5 theControlP5, String name) {
		super(theControlP5, name);
		
		filteredItems = new ArrayList<Map<String, Object>>();
		searchBuffer = new StringBuilder("");
	}
	
	@Override
	public DropdownSearch setValue(float newValue) {
		super.setValue(newValue);
		// Reset the search buffer
		searchBuffer = new StringBuilder("");
		updateItemLists();
		return this;
	}
	
	@Override
	public DropdownSearch clear() {
		super.clear();
		// Clear filtered list
		for (int idx = filteredItems.size( ) - 1 ; idx >= 0 ; idx--) {
			filteredItems.remove(idx);
		}
		
		filteredItems.clear();
		searchBuffer = new StringBuilder("");
		return this;
	}
	
	@Override
	public void keyEvent(KeyEvent e) {
		
		if (e.getKeyCode() == ControlP5.UP || e.getKeyCode() == ControlP5.DOWN
				|| e.getKeyCode() == ControlP5.LEFT ||
				e.getKeyCode() == ControlP5.RIGHT) {
			
			super.keyEvent(e);
			
		} else if (isInside && e.getAction() == KeyEvent.PRESS) {
			
			if (e.getKey() >= 32 && e.getKey() <= 126) {
				if (searchBuffer.length() < 16) {
					// Append a character to the end of the search buffer
					System.out.printf("%s + %c\n", searchBuffer, e.getKey());
					searchBuffer.append(e.getKey());
					updateItemLists();
					open();
				}
				
			} else if (e.getKeyCode() == PConstants.BACKSPACE) {
				int lastIdx = searchBuffer.length() - 1;
				if (lastIdx >= 0) {
					// Delete the last character in the search buffer
					System.out.printf("%s - %c\n", searchBuffer, searchBuffer.charAt(lastIdx));
					searchBuffer.deleteCharAt(lastIdx);
					updateItemLists();
					open();
				}
				
			} else if (e.getKeyCode() == 147) {
				// TODO do I really need delete?
				
			} else if (e.getKeyCode() == ControlP5.ENTER) {
				// Set the item at index 0 as active
				if (items.size() > 0) {
					setValue(0);
				}
			}
		}
	}
	
	/**
	 * Updates the filtered and unfiltered item lists for this dropdown.
	 */
	protected void updateItemLists() {
		List<Map<String, Object>> limbo = new ArrayList<Map<String, Object>>();
		int idx = 0;
		
		while (filteredItems != null && idx < filteredItems.size()) {
			Map<String, Object> item = filteredItems.get(idx);
			
			if (item != null) {
				String name = (String)item.get("name");
				
				if (compareToSearch(name) == 0) {
					// Move to limbo list
					filteredItems.remove(idx);
					limbo.add(item);
					continue;
				}		
			}
			
			++idx;
		}
		
		idx = 0;
		while (idx < items.size()) {
			Map<String, Object> item = items.get(idx);
			
			if (item != null) {
				String name = (String)item.get("name");
				
				if (compareToSearch(name) != 0) {
					// Move to filtered list
					items.remove(idx);
					filteredItems.add(item);
					continue;
				}
			}
			
			++idx;
		}
		
		// Re-add items from filtered items
		for (Map<String, Object> item : limbo) {
			items.add(item);
		}
		
		// Update the dropdown label
		getCaptionLabel().setText(searchBuffer.toString());
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param name
	 * @return
	 */
	private int compareToSearch(String name) {
		
		if (searchBuffer.length() == 0) {
			// filter is undefined
			return 0;
		}
		
		// TODO develop a better matching algorithm
		if (name.length() < searchBuffer.length()) {
			// length difference
			return name.length() - searchBuffer.length();
		}
		 
		for (int cdx = 0; cdx < searchBuffer.length(); ++cdx) {
			
			if (name.charAt(cdx) != searchBuffer.charAt(cdx)) {
				// character difference
				return -1;
			}			
		}
			
		return 0;
	}
}
