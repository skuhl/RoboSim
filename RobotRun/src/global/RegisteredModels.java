package global;

import java.io.File;
import java.util.HashMap;

import processing.data.JSONArray;
import processing.data.JSONObject;
import robot.CamSelectArea;
import robot.CamSelectView;
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
				
				JSONArray viewList = area.getJSONArray("views");
				CamSelectView[] views = new CamSelectView[viewList.size()];
				
				for(int k = 0; k < viewList.size(); k += 1) {
					JSONObject view = viewList.getJSONObject(k);
					String align = view.getString("align");
					int x1 = view.getInt("x1");
					int y1 = view.getInt("y1");
					int x2 = view.getInt("x2");
					int y2 = view.getInt("y2");
					views[k] = new CamSelectView(align, x1, y1, x2, y2);
				}
				
				selectAreas[j] = new CamSelectArea(id, views);	
			}
			
			modelAreasOfInterest.put(objID, selectAreas);
		}
	}
}
