package com.phan.game.selectboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CacheData {

	private final static HashMap<String, List<GridData>> categoryMap = new HashMap<>();
	private static List<GridData> gridDataList = null;
	private final static HashMap<String, String> inputArgMap = new HashMap<>();
	
	public static final HashMap<String, List<GridData>> getCategoryMap() {
		return categoryMap;
	}
	
	public static int createCategoryMap(List<GridData> dataList) {

		if (dataList != null) {
			gridDataList = dataList;
		} 
		if (gridDataList == null) {
			return 0;
		}
		for (GridData data : gridDataList) {
			String key = "{\"" + data.getCategory() + "\":" + data.getPoints() + "}";
			if (categoryMap.get(key) == null) {
				List<GridData> catList = new ArrayList<>();
				data.setPlayed(false);
				catList.add(data);
				categoryMap.put(key, catList);
			} else {
				data.setPlayed(false);
				categoryMap.get(key).add(data);
			}
		}
		return categoryMap.keySet().size();
	}
	
	public static final HashMap<String, String> getInputArgMap() {
		return inputArgMap;
	}
	
	public static final void populateInputArgMap(String [] args) {
		
		for (String arg : args) {
//			System.out.println("Tokenizing arg=" + arg);
			String [] keyValue = arg.split("=");
//			System.out.println("Adding key=" + keyValue[0] + " - value=" + keyValue[1]);
			CacheData.getInputArgMap().put(keyValue[0], keyValue[1]);
		}	
		
	}
	
	public static Integer getIntegerValue(String key, Integer defaultVal) {
		Integer retValue = (defaultVal!=null?defaultVal:0);
		String value = CacheData.getInputArgMap().get(key);
		if (key != null) {
			try {
				retValue = Integer.parseInt(value);			
			} catch(Exception ex) {				
			}
		}
		return retValue;
	}
}
