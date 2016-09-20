package com.veil.ai;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.veil.platforminterface.PlatformUtility;

public class ProfileEvaluator {

	private static FileHandle rangeProfileTmp = PlatformUtility.fileOperator.getStorageRoot().child("rangeprofile_tmp");
	private static FileHandle battleProfileTmp = PlatformUtility.fileOperator.getStorageRoot().child("battleprofile_tmp");
	
	private List<FileHandle> rangeProfiles = new LinkedList<FileHandle>();
	private List<FileHandle> battleProfiles = new LinkedList<FileHandle>();
	
	public float errorMargin = 0.1f;
	
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
		HashSet<String> exclusionList = createDurationExclusionSet(targetBattleProfile);
		
		float[] evaluationParameter = BattleProfile.calculateEvaluationParameter(battleProfileTmp, relevantRange, -1, 
				exclusionList, null, exclusionList);
		System.out.println("========== Baseline parameters ==========");
		System.out.println("range "+relevantRange+" px");
		System.out.println("time "+evaluationParameter[0]+" frames");
		System.out.println("miss rate "+evaluationParameter[1]);
		System.out.println("hp percentage "+evaluationParameter[2]);
		
		if(targetBattleProfile == null) return;
		
		float[] targetParameter = BattleProfile.calculateEvaluationParameter(targetBattleProfile, relevantRange, 
				(int)evaluationParameter[0], exclusionList, null, exclusionList);
		System.out.println("========== Evaluating parameters ==========");
		System.out.println("time "+targetParameter[0]+" frames");
		System.out.println("miss rate "+targetParameter[1]);
		System.out.println("hp percentage "+targetParameter[2]);
		
		System.out.println("===================================");
		//boolean durationPass = Math.abs(evaluationParameter[0] - targetParameter[0])/evaluationParameter[0] <= 0.1f;
		boolean missRatePass = Math.abs(evaluationParameter[1] - targetParameter[1])/evaluationParameter[1] <= errorMargin;
		boolean hpPass = targetParameter[2] <= errorMargin;
		//System.out.println("Duration:"+durationPass+"  Miss rate:"+missRatePass+"  HP:"+hpPass);
		System.out.println("Miss rate:"+missRatePass+"  HP:"+hpPass);
	}
	
	public void end(){
		if(rangeProfileTmp.exists()){
			rangeProfileTmp.deleteDirectory();
		}
		if(battleProfileTmp.exists()){
			battleProfileTmp.deleteDirectory();
		}
	}
	
	private HashSet<String> createDurationExclusionSet(FileHandle aiDirectory){
		//Find all AI player name 
		ProfileTable aiEncounterTable = new ProfileTable();
		markEncounterTable(aiDirectory, aiEncounterTable);
		HashSet<String> aiPlayerName = new HashSet<String>();
		for(String playerName : aiEncounterTable.getColHeaderIterable()){
			aiPlayerName.add(playerName);
		}
		
		ProfileTable encounterTable = new ProfileTable();
		markEncounterTable(battleProfileTmp, encounterTable);
		markEncounterTable(aiDirectory, encounterTable);
		
		HashSet<String> set = new HashSet<String>();
		for(String enemyName : encounterTable.getRowHeaderIterable()){
			//Enemy is valid only if:
			//- No invalid data exists in any encounter
			//- All AI encounter the enemy
			//- At least 1 player encounter the enemy
			boolean invalidEnemy = false;
			boolean humanPlayerEncounterThisEnemy = false;
			for(String playerName : encounterTable.getColHeaderIterable()){
				String value = encounterTable.getCell(enemyName, playerName);
				if(aiPlayerName.contains(playerName)){
					//AI player's record
					if(value == null || value.equals("i")){
						invalidEnemy = true;
						break;
					}
				}else{
					//Human player's record
					if(value != null){
						humanPlayerEncounterThisEnemy = true;
						if(value.equals("i")){
							invalidEnemy = true;
							break;
						}
					}
				}
			}
			if(invalidEnemy || !humanPlayerEncounterThisEnemy){
				set.add(enemyName);
			}
		}
		return set;
	}
	
	private void markEncounterTable(FileHandle fh, ProfileTable encounterTable){
		//row=enemyFileName col=playerName val={null: no encounter, ""=fought, "i"=invalidForBattleDuration }
		if(!fh.isDirectory()){
			BattleProfile battleProfile = new BattleProfile();
			battleProfile.load(fh);
			encounterTable.setCell(fh, battleProfile.isValidForBattleDuration() ? "" : "i");
		}else{
			for(FileHandle f : fh.list()){
				markEncounterTable(f, encounterTable);
			}
		}
	}
}
