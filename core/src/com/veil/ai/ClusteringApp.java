package com.veil.ai;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.files.FileHandle;
import com.veil.game.GameConstant;
import com.veil.platforminterface.PlatformUtility;

public class ClusteringApp extends Game {
	
	private String[] typeDir = {"Enemy","Elite","Miniboss","Boss"};
	
	@Override
	public void create() {
		FileHandle rangeProfile = new FileHandle("C:\\Users\\angelix\\Documents\\CU DOC\\Profile\\timing_all\\Range");
		
		for(String type : typeDir){
			System.out.println("######$$$######  "+type+"  ######$$$######");
			FileHandle target = GameConstant.profileDir.child(type);
			ProfileEvaluator eva = new ProfileEvaluator();
	        eva.addBattleProfilePath(target);
	        eva.addRangeProfilePath(rangeProfile);
	        eva.begin();
	        /*
	        eva.evaluatePrioritizedCluster(4, 3, 3, new int[]{1,0}, 
	        		PlatformUtility.fileOperator.getStorageRoot().child("clustering_"+type+".csv")
	        		);
	        		*/
	        eva.evaluateBulletDensity(3, 1, PlatformUtility.fileOperator.getStorageRoot().child("clustering_bullet_"+type+".csv"));
	        eva.end();
		}
	}

}
