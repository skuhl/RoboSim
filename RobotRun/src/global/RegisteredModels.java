package global;

import java.io.File;
import java.util.HashMap;

import core.CamSelectArea;
import core.CamSelectView;
import core.RobotRun;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class RegisteredModels {
	public static final HashMap<String, Integer> modelIDList = new HashMap<String, Integer>();
	public static final HashMap<Integer, Integer> modelFamilyList = new HashMap<Integer, Integer>();
	public static final HashMap<Integer, CamSelectArea[]> modelAreasOfInterest = new HashMap<Integer, CamSelectArea[]>();
		
	public static void loadModelDefs() {
		String path = RobotRun.getInstance().sketchPath();
		JSONObject root = RobotRun.loadJSONObject(new File(path + "/data/defs/models.json"));
		JSONArray objList = root.getJSONArray("objList");
		
		for(int i = 0; i < objList.size(); i += 1) {
			JSONObject obj = objList.getJSONObject(i);
			String fileName = obj.getString("modelFileName");
			int mdlID = obj.getInt("modelID");
			modelIDList.put(fileName, mdlID);
			
			int familyID = obj.getInt("modelFamilyID");
			modelFamilyList.put(mdlID, familyID);
			
			JSONArray selectList = obj.getJSONArray("selectAreas");
			CamSelectArea[] selectAreas = new CamSelectArea[selectList.size()];
						
			for(int j = 0; j < selectList.size(); j += 1) {
				JSONObject area = selectList.getJSONObject(j);
				int id = area.getInt("areaID");
				boolean isDefect = area.getInt("areaType") == 1;
				
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
				
				selectAreas[j] = new CamSelectArea(id, isDefect, views);	
			}
			
			modelAreasOfInterest.put(mdlID, selectAreas);
		}
	}
}
