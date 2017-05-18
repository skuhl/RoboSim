package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A data class, which keys track of information regarding key events,
 * specifically whether specific keys are currently held down or not
 * and the last key held down.
 * 
 * NOTE:	This class needs to be updated with the keyPressed and keyReleased
 * 			functions of the PApplet!!
 * 
 * @author Joshua Hooker
 */
public class KeyCodeMap {
	/**
	 * Contains data for key codes such as the character representing the key
	 * code and whether it is held down or not.
	 */
	private final HashMap<Integer, KeyData> keyDownMap;
	
	/**
	 * The code of the last key pressed and not released.
	 */
	private int codeOfLast;
	
	/**
	 * When the last key pressed was first pressed.
	 */
	private long startTimeOfLast;
	
	/**
	 * How long has the last key pressed been held down.
	 */
	private long downTimeOfLast;
	
	
	/**
	 * Initializes instance data.
	 */
	public KeyCodeMap() {
		keyDownMap = new HashMap<>();
		codeOfLast = 0;
		startTimeOfLast = -1L;
		downTimeOfLast = 0L;
	}
	
	/**
	 * @return	For how long has the last key pressed been held down
	 */
	public long getTimeOfLastKey() {
		return downTimeOfLast;
	}
	
	/**
	 * Returns if the key with the given code is currently being held down.
	 * 
	 * @param code	The code pertaining to a key
	 * @return		Whether the key with the given code is being held down
	 */
	public boolean isKeyDown(int code) {
		KeyData data = keyDownMap.get(code);
		return data != null && data.isDown;
	}
	
	/**
	 * Update the state of the key with the given keyCode and character.
	 * 
	 * @param code	The code of the key
	 * @param key	The character representing the key
	 */
	public void keyPressed(int code, char key) {
		KeyData data = keyDownMap.get(code);
		
		if (code != codeOfLast) {
			codeOfLast = code;
			startTimeOfLast = System.currentTimeMillis();
			downTimeOfLast = 0L;
		}
		
		if (data == null) {
			// Add key data
			data = new KeyData(key, true);
			keyDownMap.put(code, data);
			
		} else {
			// Update existing key data
			data.isDown = true;
		}
	}

	/**
	 * Update the state of the key with the given keyCode and character.
	 * 
	 * @param code	The code of the key
	 * @param key	The character representing the key
	 */
	public void keyReleased(int code, char key) {
		KeyData data = keyDownMap.get(code);
		
		if (code == codeOfLast) {
			codeOfLast = 0;
			startTimeOfLast = -1L;
			downTimeOfLast = 0L;
		}
		
		if (data == null) {
			// Add key data
			data = new KeyData(key, false);
			keyDownMap.put(code, data);
			
		} else {
			// Update existing key data
			data.isDown = false;
		}
	}
	
	/**
	 * Returns a set of all key codes of keys, whose state is stored in this
	 * class (pressed or released).
	 * 
	 * @return	A set of all key codes stored in this class
	 */
	public Set<Integer> keySet() {
		return keyDownMap.keySet();
	}
	
	/**
	 * A list of characters pertaining to all keys, which are currently being
	 * held down.
	 * 
	 * @return	A list of characters held down
	 */
	public List<Character> keysDown() {
		Set<Integer> keyCodes = keyDownMap.keySet();
		List<Character> keysDown = new ArrayList<>();
		
		for (int code : keyCodes) {
			if (isKeyDown(code)) {
				keysDown.add(keyDownMap.get(code).key);
			}
		}
		
		return keysDown;
	}
	
	/**
	 * The character of the last key to be held down and not released.
	 * 
	 * @return	The last character to be pressed and not released
	 */
	public Character lastKeyPressed() {
		KeyData data = keyDownMap.get(codeOfLast);
		
		if (data != null && data.key >= 32 && data.key <= 126) {
			// only return keys with valid text characters
			return data.key;
		}
		
		return null;
	}
	
	/**
	 * Updates the time for which the last key was held down.
	 */
	public void update() {
		if (startTimeOfLast >= 0) {
			downTimeOfLast = System.currentTimeMillis() - startTimeOfLast;
		}
	}
	
	/**
	 * Data class for keeping track of key characters and the state of the
	 * keys.
	 * 
	 * @author Joshua Hooker
	 */
	private class KeyData {
		public final char key;
		public boolean isDown;
		
		public KeyData(char k, boolean down) {
			key = k;
			isDown = down;
		}
		
	}
}
