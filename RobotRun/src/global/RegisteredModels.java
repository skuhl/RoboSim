package global;

import java.io.File;
import java.util.HashMap;

import geom.CamSelectArea;
import processing.data.JSONArray;
import processing.data.JSONObject;
import robot.RobotRun;

public class RegisteredModels {
	public static final HashMap<String, Integer> modelIDList = new HashMap<String, Integer>();
	public static final HashMap<Integer, CamSelectArea[]> modelAreasOfInterest = new HashMap<Integer, CamSelectArea[]>();
		
	public static void loadModelDefs() {
		JSONObject root = RobotRun.loadJSONObject(new File("data/defs/models.json"));
		JSONArray objList = root.getJSONArray("objList");
		
		for(int i = 0; i < objList.size(); i += 1) {
			JSONObject obj = objList.getJSONObject(i);
			String fileName = obj.getString("objectFileName");
			int objID = obj.getInt("objectID");
			
			modelIDList.put(fileName, objID);
			
			JSONArray selectList = obj.getJSONArray("selectAreas");
			CamSelectArea[] selectAreas = new CamSelectArea[selectList.size()];
						
			for(int j = 0; j < selectList.size(); j += 1) {
				JSONObject area = selectList.getJSONObject(j);
				int id = area.getInt("areaID");
				float x1 = area.getInt("x1");
				float y1 = area.getInt("y1");
				float x2 = area.getInt("x2");
				float y2 = area.getInt("y2");
				selectAreas[j] = new CamSelectArea(id, x1, y1, x2, y2);		
			}
			
			modelAreasOfInterest.put(objID, selectAreas);
		}
	}
}
