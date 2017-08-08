package com.veil.ai;

import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.veil.game.GameConstant;
import com.veil.platforminterface.PlatformUtility;

public class EvalAgentApp extends Game {

	private String[] typeDir = {"Enemy","Elite","Miniboss","Boss"};
	private float[] avgCapDuration = {152.9167f, 329.8f, 596.333f, 1337.5263f}; //Based on clustering result
	private float[] baselineMissrate = {0.0652f, 0.0667f, 0.1246f, 0.1669f}; //Based on AI result against dataset (only collected from valid one)
	private float[] baselineHpPercent = {0.1667f, 0.04722f, 0, 0.03997f}; //Based on AI result against selected cluster, duration is capped with capDuration
	
	@Override
	public void create() {
		FileHandle rangeProfile = new FileHandle("C:\\Users\\angelix\\Documents\\CU DOC\\Profile\\timing_all\\Range");
		//Create a copy of current DB as "result_script_pass" in root
		FileHandle passScript = PlatformUtility.fileOperator.getStorageRoot().child("result_script_pass");
		passScript.deleteDirectory();
        GameConstant.agentDatabaseDir.child("Rush").copyTo(passScript);
        //Create empty folder "result_script_fail_low","result_script_fail_high" in root
        FileHandle failLowScript = PlatformUtility.fileOperator.getStorageRoot().child("result_script_fail_low");
        failLowScript.deleteDirectory();
        failLowScript.mkdirs();
        FileHandle failHighScript = PlatformUtility.fileOperator.getStorageRoot().child("result_script_fail_high");
        failHighScript.deleteDirectory();
        failHighScript.mkdirs();
		
		for(int i=0; i<4; i++){
			String type = typeDir[i];
			float baseMissrate = baselineMissrate[i];
			System.out.println("######$$$######  "+type+"  ######$$$######");
			FileHandle target = GameConstant.profileDir.child(type); 
			if(!target.exists()){
				target.mkdirs();
			}
			ProfileEvaluator eva = new ProfileEvaluator();
	        eva.addBattleProfilePath(target);
	        eva.addRangeProfilePath(rangeProfile);
	        eva.begin();
	        List<String>[] enemiesList = eva.evaluateIndividually(-1, baseMissrate, 0, 
	        		PlatformUtility.fileOperator.getStorageRoot().child("gen_eval_"+type+".csv")
	        		);
	        eva.end();
	        
	        //Remove all scripts that are not evaluated in "result_script_pass" type subdir 
	        //and move fail enemy script into "result_script_fail_low"/"result_script_fail_high" type subdir
	        FileHandle passSubdir = passScript.child(type);
	        FileHandle failLowSubdir = failLowScript.child(type);
	        failLowSubdir.mkdirs();
	        FileHandle failHighSubdir = failHighScript.child(type);
	        failHighSubdir.mkdirs();
	        List<String> evaluated = enemiesList[0];
	        List<String> lowMiss = enemiesList[1];
	        List<String> highMiss = enemiesList[2];
	        for(FileHandle src : passSubdir.list()){
	        	if(src.isDirectory())
	        		continue;
	        	//This script is not evaluated, remove it
	        	if(!evaluated.contains(src.nameWithoutExtension())){
	        		src.delete();
	        	}
	        	//This is a script of the enemy that fail the evaluation
	        	if(lowMiss.contains(src.nameWithoutExtension())){
	        		src.moveTo(failLowSubdir.child(src.name()));
	        	}else if(highMiss.contains(src.nameWithoutExtension())){
	        		src.moveTo(failHighSubdir.child(src.name()));
	        	}
	        }
		}
		
		Gdx.app.exit();
	}
}
