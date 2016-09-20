package com.veil.ai;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.files.FileHandle;

public class ProfileTable {
	
	private List<List<String>> table; //table[rowIndex][colIndex]
	private HashMap<String,Integer> rowHeader, colHeader;
	
	public ProfileTable(){
		table = new LinkedList<List<String>>();
		rowHeader = new HashMap<String, Integer>();
		colHeader = new HashMap<String, Integer>();
	}
	
	public Iterable<String> getRowHeaderIterable(){
		return rowHeader.keySet();
	}
	
	public Iterable<String> getColHeaderIterable(){
		return colHeader.keySet();
	}
	
	public String getCell(String rowHeader, String colHeader){
		return table.get(this.rowHeader.get(rowHeader)).get(this.colHeader.get(colHeader));
	}
	
	public void setCell(FileHandle fh, String value){
		String[] split = fh.pathWithoutExtension().split("/");
		setCell(split[split.length-2], split[split.length-1], value);
	}
	
	public void setCell(String playerName, String _enemyName, String value){
		String enemyName = convertSpecialName(_enemyName);
		if(enemyName == null){
			return;
		}
		if(!enemyName.equals(_enemyName)){
			value += ".0";
		}
		
		//Setup header -> index map
		if(!rowHeader.containsKey(enemyName)){
			rowHeader.put(enemyName, rowHeader.size());
		}
		if(!colHeader.containsKey(playerName)){
			colHeader.put(playerName, colHeader.size());
		}
		
		//Expand table if necessary
		while(table.size() < rowHeader.size()){
			table.add(new LinkedList<String>());
		}
		for(List<String> row : table){
			while(row.size() < colHeader.size()){
				row.add(null);
			}
		}
		
		//Set value
		int rowIndex = rowHeader.get(enemyName);
		int colIndex = colHeader.get(playerName);
		table.get(rowIndex).set(colIndex, value);
	}
	
	public String toCsvString(){
		String s = "x";
		List<Entry<String,Integer>> list = new LinkedList<Entry<String,Integer>>(colHeader.entrySet());
		Collections.sort(list, new Comparator<Entry<String,Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		for(Entry<String,Integer> colHeaderContent : list){
			s += ","+colHeaderContent.getKey();
		}
		for(String rowHeaderName : rowHeader.keySet()){
			s += "\r\n"+rowHeaderName;
			int rowIndex = rowHeader.get(rowHeaderName);
			for(String value : table.get(rowIndex)){
				s += "," + (value == null ? "" : value);
			}
		}
		return s;
	}
	
	public void saveToFile(FileHandle fh){
		fh.writeString(toCsvString(), true);
	}
	
	//Return proper name for provided enemyName so that .csv is displayed nicely
	//Due to different version of profiling, some enemies in older version have its name changed or removed
	//- Newer version has unbeatable marker
	//- Some enemies are removed
	//- AlienEgg renamed to _AlienEggSpawner (marker added)
	//Also, player dead marker is removed
	private static String[] markedUnbeat = {"_Biree", "_BoulderSpawner", "_DocronHatcher", "_Floater01", "_Floater02",
		"_Garyoby", "_Haehaey", "_RailCannon", "_SkeletonJoe", "_Squeept", "_SniperCrawl", "_Metroid", "_GroundBarrel"};
	private static String[] removed = {"BigSpiky", "Flyer", "WingedSoldier"};
	private String convertSpecialName(String enemyName){
		//Special case
		if(enemyName.equals("_AlienEgg")){
			return "__AlienEggSpawner";
		}
		for(String s : removed){
			if(enemyName.contains(s)){
				return null;
			}
		}
		for(String s : markedUnbeat){
			if(enemyName.equals(s)){
				return "_"+s;
			}
		}
		//Player dead case
		if(enemyName.startsWith("-")){
			return enemyName.substring(1);
		}
		//Manually flag unbeatable
		if(enemyName.startsWith("_") && !enemyName.startsWith("__")){
			return enemyName.substring(1);
		}
		return enemyName;
	}
}
