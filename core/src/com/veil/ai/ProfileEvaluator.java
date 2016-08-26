package com.veil.ai;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.veil.platforminterface.PlatformUtility;

public class ProfileEvaluator {

	private static FileHandle rangeProfileTmp = PlatformUtility.fileOperator.getStorageRoot().child("rangeprofile_tmp");
	private static FileHandle battleProfileTmp = PlatformUtility.fileOperator.getStorageRoot().child("battleprofile_tmp");
	
	private List<FileHandle> rangeProfiles = new LinkedList<FileHandle>();
	private List<FileHandle> battleProfiles = new LinkedList<FileHandle>();
	
	public void addRangeProfilePath(FileHandle dir){
		rangeProfiles.add(dir);
	}
	
	public void addBattleProfilePath(FileHandle dir){
		battleProfiles.add(dir);
	}
	
	public void begin(){
		if(rangeProfileTmp.exists()){
			rangeProfileTmp.deleteDirectory();
		}
		int i=0;
		for(FileHandle rangeProfileFolder : rangeProfiles){
			rangeProfileFolder.copyTo(rangeProfileTmp.child("range_"+i));
			i++;
		}
		if(battleProfileTmp.exists()){
			battleProfileTmp.deleteDirectory();
		}
		i=0;
		for(FileHandle battleProfileFolder : battleProfiles){
			battleProfileFolder.copyTo(battleProfileTmp.child("battle_"+i));
			i++;
		}
	}
	
	public void evaluate(FileHandle targetBattleProfile){
		int relevantRange = RangeProfile.calculateRelevantRange(rangeProfileTmp);
		float[] evaluationParameter = BattleProfile.calculateEvaluationParameter(battleProfileTmp, relevantRange, -1);
		System.out.println("========== Baseline parameters ==========");
		System.out.println("range "+relevantRange+" px");
		System.out.println("time "+evaluationParameter[0]+" frames");
		System.out.println("miss rate "+evaluationParameter[1]);
		System.out.println("hp percentage "+evaluationParameter[2]);
		
		if(targetBattleProfile == null) return;
		
		float[] targetParameter = BattleProfile.calculateEvaluationParameter(targetBattleProfile, relevantRange, (int)evaluationParameter[0]);
		System.out.println("========== Evaluating parameters ==========");
		System.out.println("time "+targetParameter[0]+" frames");
		System.out.println("miss rate "+targetParameter[1]);
		System.out.println("hp percentage "+targetParameter[2]);
		
		System.out.println("===================================");
		boolean durationPass = Math.abs(evaluationParameter[0] - targetParameter[0])/evaluationParameter[0] <= 0.1f;
		boolean missRatePass = Math.abs(evaluationParameter[1] - targetParameter[1])/evaluationParameter[1] <= 0.1f;
		boolean hpPass = Math.abs(evaluationParameter[2] - targetParameter[2])/evaluationParameter[2] <= 0.1f;
		System.out.println("Duration:"+durationPass+"  Miss rate:"+missRatePass+"  HP:"+hpPass);
	}
	
	public void end(){
		if(rangeProfileTmp.exists()){
			rangeProfileTmp.deleteDirectory();
		}
		if(battleProfileTmp.exists()){
			battleProfileTmp.deleteDirectory();
		}
	}
}
