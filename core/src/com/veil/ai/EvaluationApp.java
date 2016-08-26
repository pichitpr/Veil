package com.veil.ai;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.files.FileHandle;
import com.veil.game.GameConstant;
import com.veil.platforminterface.PlatformUtility;

public class EvaluationApp extends Game {

	public static ProfileTable durationTable;
	public static ProfileTable missCountTable;
	public static FileHandle fhTemp;
	
	private String[] typeDir = {"Enemy","Elite","Miniboss","Boss"};
	
	@Override
	public void create() {
		FileHandle rangeProfile = new FileHandle("C:\\Users\\angelix\\Documents\\CU DOC\\Profile\\timing_all\\Range");
		
		for(String type : typeDir){
			System.out.println("######$$$######  "+type+"  ######$$$######");
			durationTable = new ProfileTable();
			missCountTable = new ProfileTable();
			FileHandle baseline = new FileHandle("C:\\Users\\angelix\\Documents\\CU DOC\\Profile\\timing_all\\Battle\\"+type);
			FileHandle target = GameConstant.profileDir.child(type);
			ProfileEvaluator eva = new ProfileEvaluator();
	        eva.addBattleProfilePath(baseline);
	        eva.addRangeProfilePath(rangeProfile);
	        eva.begin();
	        eva.evaluate(target);
	        eva.end();
	        //durationTable.saveToFile(PlatformUtility.fileOperator.getStorageRoot().child("battle_duration_"+type+".csv"));
	        missCountTable.saveToFile(PlatformUtility.fileOperator.getStorageRoot().child("miss_count_"+type+".csv"));
		}
	}

}
