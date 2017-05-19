package global;

import java.util.HashMap;

public class RegisteredModels {
	public static final HashMap<String, Integer> modelIDList = new HashMap<String, Integer>();
	
	public static void loadModelIDs() {	
		modelIDList.put("circsheet.stl", 0x00);
		modelIDList.put("grinder.stl", 0x01);
		modelIDList.put("inclinedblock.stl", 0x02);
		modelIDList.put("table.stl", 0x03);
	}
}
