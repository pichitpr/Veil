package com.veil.ai;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.files.FileHandle;

public class BaselineFindApp extends Game{

	private String[] typeDir = {"Enemy","Elite","Miniboss","Boss"};
	private float[] avgCapDuration = {152.9167f, 329.8f, 596.333f, 1337.5263f}; //Based on clustering result
	
	@Override
	public void create() {
		FileHandle rangeProfile = new FileHandle("C:\\Users\\angelix\\Documents\\CU DOC\\Profile\\timing_all\\Range");
		FileHandle battleProfile = new FileHandle("C:\\Users\\angelix\\Documents\\CU DOC\\Profile\\Profile-2016-09-26-2\\Profile");
		for(int i=0; i<4; i++){
			String type = typeDir[i];
			float capDuration = avgCapDuration[i];
			System.out.println("######$$$######  "+type+"  ######$$$######");
			FileHandle target = battleProfile.child(type); 
			if(!target.exists()){
				target.mkdirs();
			}
			ProfileEvaluator eva = new ProfileEvaluator();
	        eva.addBattleProfilePath(target);
	        eva.addRangeProfilePath(rangeProfile);
	        eva.begin();
	        eva.dumpProfile(-1, battleProfile.child(type+".csv"));
	        eva.end();
		}
	}
	
}
