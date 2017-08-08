package com.veil.ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

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
		HashSet<String> missRateExclusionList = createMissRateExclusionList(targetBattleProfile, relevantRange, true);
		
		float[] evaluationParameter = BattleProfile.calculateEvaluationParameter(battleProfileTmp, relevantRange, -1, 
				exclusionList, missRateExclusionList, exclusionList);
		System.out.println("========== Baseline parameters ==========");
		System.out.println("range "+relevantRange+" px");
		System.out.println("time "+evaluationParameter[0]+" frames");
		System.out.println("miss rate "+evaluationParameter[1]);
		System.out.println("hp percentage "+evaluationParameter[2]);
		
		if(targetBattleProfile == null) return;
		
		float[] targetParameter = BattleProfile.calculateEvaluationParameter(targetBattleProfile, relevantRange, 
				(int)evaluationParameter[0], exclusionList, missRateExclusionList, exclusionList);
		System.out.println("========== Evaluating parameters ==========");
		System.out.println("time "+targetParameter[0]+" frames");
		System.out.println("miss rate "+targetParameter[1]);
		System.out.println("hp percentage "+targetParameter[2]);
		
		System.out.println("===================================");
		//boolean durationPass = Math.abs(evaluationParameter[0] - targetParameter[0])/evaluationParameter[0] <= 0.1f;
		boolean missRatePass = Math.abs(evaluationParameter[1] - targetParameter[1]) <= errorMargin;
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
	
	private HashSet<String> createMissRateExclusionList(FileHandle aiDirectory, int relevantRange, boolean alsoExcludeNoCloseUp){
		//Find all AI player name 
		ProfileTable aiEncounterTable = new ProfileTable();
		markEncounterTable(aiDirectory, aiEncounterTable);
		HashSet<String> aiPlayerName = new HashSet<String>();
		for(String playerName : aiEncounterTable.getColHeaderIterable()){
			aiPlayerName.add(playerName);
		}

		ProfileTable encounterTable = new ProfileTable();
		markMissrateTable(battleProfileTmp, encounterTable, relevantRange, -1);
		markMissrateTable(aiDirectory, encounterTable, relevantRange, -1);

		HashSet<String> set = new HashSet<String>();
		for(String enemyName : encounterTable.getRowHeaderIterable()){
			//Enemy is valid only if:
			//- All AI encounter the enemy
			//- At least 1 player encounter the enemy
			//- Has close up combat (depends on flag)
			boolean invalidEnemy = false;
			boolean humanPlayerEncounterThisEnemy = false;
			for(String playerName : encounterTable.getColHeaderIterable()){
				String value = encounterTable.getCell(enemyName, playerName);
				if(aiPlayerName.contains(playerName)){
					//AI player's record
					if(value == null || (alsoExcludeNoCloseUp && value == "n")){
						invalidEnemy = true;
						break;
					}
				}else{
					//Human player's record
					if(value != null){
						humanPlayerEncounterThisEnemy = true;
						if(alsoExcludeNoCloseUp && value == "n"){
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
	
	private void markMissrateTable(FileHandle fh, ProfileTable encounterTable, int relevantRange, int durationCap){
		//row=enemyFileName col=playerName val={null: no encounter, ""=fought, "n"=noCloseUp }
		if(!fh.isDirectory()){
			BattleProfile battleProfile = new BattleProfile();
			battleProfile.load(fh);
			encounterTable.setCell(fh, battleProfile.everCloseUp(relevantRange, durationCap) ? "" : "n");
		}else{
			for(FileHandle f : fh.list()){
				markEncounterTable(f, encounterTable);
			}
		}
	}
	
	//=================================================
	
	/**
	 * Arrange profile data into cluster using k-mean. K-mean is applied multiple times using single attribute 
	 * (duration, missrate or hp) at a time. Each result cluster from K-mean is clustered again with K-mean using different attribute.
	 * This will result in durationClusterSize*missrateClusterSize*hpClusterSize clusters. The attribute order in each K-mean step
	 * is determined by priority. First attribute specified in priority[] is used first. Attribute is indicated by integer
	 * (0:duration, 1:missrate, 2:hp). Result is saved to resultTable which can be saved to file
	 */
	public void evaluatePrioritizedCluster(int durationClusterSize, int missrateClusterSize, int hpClusterSize, int[] priority,
			FileHandle file){
		int relevantRange = RangeProfile.calculateRelevantRange(rangeProfileTmp);
		HashMap<String,List<ClusteringProfile>> profileTable = new HashMap<String, List<ClusteringProfile>>();
		//Currently, hp percent is not used. Therefore, battle duration cap (to be used with hp percent) is set to -1 (don't care)
		constructProfileTable(battleProfileTmp, profileTable, relevantRange, -1);
		Dataset dataset = new DefaultDataset();
		for(Entry<String,List<ClusteringProfile>> entry : profileTable.entrySet()){
			if(entry.getValue().size() == 0) continue;
			float durationAvg = 0;
			float missrateAvg = 0;
			float hpPercentAvg = 0;
			for(ClusteringProfile profile : entry.getValue()){
				durationAvg += profile.battleDuration;
				missrateAvg += profile.missrate;
				hpPercentAvg += profile.remainingHpPercent;
			}
			durationAvg /= entry.getValue().size();
			missrateAvg /= entry.getValue().size();
			hpPercentAvg /= entry.getValue().size();
			dataset.add(new DenseInstance(new double[]{ 0 }, 
					new ClusteringProfile(entry.getKey(), durationAvg, missrateAvg, hpPercentAvg, 0)));
		}
		
		int[] clusterSize = new int[priority.length];
		for(int i=0; i<priority.length; i++){
			int attribute = priority[i];
			clusterSize[i] = (attribute == 0 ? durationClusterSize : (attribute == 1 ? missrateClusterSize : hpClusterSize));
		}
		
		List<Dataset> result = prioritizedKmean(dataset, 0, priority, clusterSize);
		
		String csv = "x,duration,missrate,hpPercent\n";
		for(Dataset resultCluster : result){
			if(resultCluster != null){
				if(resultCluster instanceof DatasetMarker){
					csv += "^^MARKER^^,,,^^^^^^^^^^^^^^^^^^^^^"+((DatasetMarker)resultCluster).toString()+"\n";
				}else{
					Iterator<Instance> it = resultCluster.iterator();
					while(it.hasNext()){
						ClusteringProfile profile = (ClusteringProfile)it.next().classValue();
						csv += profile.name+","+profile.battleDuration+","+profile.missrate+","+profile.remainingHpPercent+"\n";
					}
				}
			}else{
				csv += "^^NULL^^,,,^^^^^^^^^^^^^^^^^^^^^NULL\n";
			}
		}
		file.writeString(csv, false);
	}
	
	public void evaluateBulletDensity(int clustersize, int mode, FileHandle file){
		HashMap<String,List<ClusteringProfile>> profileTable = new HashMap<String, List<ClusteringProfile>>();
		constructProfileTable(battleProfileTmp, profileTable, 1, -1);
		Dataset dataset = new DefaultDataset();
		for(Entry<String,List<ClusteringProfile>> entry : profileTable.entrySet()){
			if(entry.getValue().size() == 0) continue;
			float bulletCountAvg = 0;
			float bulletRateAvg = 0;
			for(ClusteringProfile profile : entry.getValue()){
				bulletCountAvg += profile.bulletCount;
				bulletRateAvg += profile.bulletCount/profile.battleDuration;
			}
			bulletCountAvg /= entry.getValue().size();
			bulletRateAvg /= entry.getValue().size();
			dataset.add(new DenseInstance(new double[]{ 0 }, 
					new ClusteringProfile(entry.getKey(), bulletCountAvg, bulletRateAvg, 0, 0)));
		}
		
		switchData(dataset, mode);
		Clusterer kmean = new KMeans(clustersize);
		Dataset[] result = kmean.cluster(dataset);
		
		String csv = "x,bulletCount,bulletRate\n";
		for(Dataset resultCluster : result){
			if(resultCluster != null){
				Iterator<Instance> it = resultCluster.iterator();
				while(it.hasNext()){
					ClusteringProfile profile = (ClusteringProfile)it.next().classValue();
					csv += profile.name+","+profile.battleDuration+","+profile.missrate+"\n";
				}
				csv += "^^MARKER^^,,,^^^^^^^^^^^^^^^^^^^^^MARKER\n";
			}else{
				csv += "^^NULL^^,,,^^^^^^^^^^^^^^^^^^^^^NULL\n";
			}
		}
		file.writeString(csv, false);
	}
	
	private class ClusteringProfile implements Comparable<ClusteringProfile> {
		public String name;
		public float battleDuration;
		public float missrate;
		public float remainingHpPercent;
		public int bulletCount;
		
		public ClusteringProfile(String name, float battleDuration, float missrate, float remainingHpPercent, int bulletCount){
			this.name = name;
			this.battleDuration = battleDuration;
			this.missrate = missrate;
			this.remainingHpPercent = remainingHpPercent;
			this.bulletCount = bulletCount;
		}

		@Override
		public int compareTo(ClusteringProfile o) {
			return name.compareTo(o.name);
		}
		
		@Override
		public String toString(){
			return name+"--"+battleDuration+"--"+missrate+"--"+remainingHpPercent+"--"+bulletCount;
		}
	}
	
	private void constructProfileTable(FileHandle source, HashMap<String, List<ClusteringProfile>> table, int relevantRange,
			int capDuration){
		constructProfileTable(source, table, relevantRange, capDuration, false);
	}
	
	private void constructProfileTable(FileHandle source, HashMap<String, List<ClusteringProfile>> table, int relevantRange,
			int capDuration, boolean skipInvalidMissrate){
		if(!source.isDirectory()){
			System.out.println(source.path());
			BattleProfile battleProfile = new BattleProfile();
			battleProfile.load(source);
			String enemyName = battleProfile.getName();
			if(!table.containsKey(enemyName)){
				table.put(enemyName, new LinkedList<ClusteringProfile>());
			}
			List<ClusteringProfile> list = table.get(enemyName);
			float missrate = battleProfile.getMissRate(relevantRange, -1);
			if(missrate < 0){
				if(skipInvalidMissrate)
					return;
				missrate = 0;
			}
			String[] split = source.pathWithoutExtension().split("/");
			String playerName = split[split.length-2];
			/*
			list.add(new ClusteringProfile(
					playerName, battleProfile.getBattleDuration(), missrate, 
					battleProfile.getRemainingHPPercent(capDuration <= 0 ? (int)(battleProfile.getBattleDuration())+1 : capDuration),
					battleProfile.getBulletCount()
					));
					*/
			list.add(new ClusteringProfile(
					playerName, Integer.MAX_VALUE, missrate, 
					battleProfile.getRemainingHPPercent(capDuration <= 0 ? Integer.MAX_VALUE : capDuration),
					battleProfile.getBulletCount()
					));
		}else{
			for(FileHandle fh : source.list()){
				constructProfileTable(fh, table, relevantRange, capDuration);
			}
		}
	}
	
	/**
	 * Switch data in each instance to that of specified mode of corresponding ClusteringProfile .
	 * 0:Duration, 1:Missrate, 2:HP
	 */
	private void switchData(Dataset dataset, int datamode){
		Iterator<Instance> it = dataset.iterator();
		while(it.hasNext()){
			Instance i = it.next();
			ClusteringProfile profile = (ClusteringProfile)i.classValue();
			double newValue = (datamode == 0 ? profile.battleDuration : (datamode == 1 ? profile.missrate : profile.remainingHpPercent));
			i.put(0, newValue);
		}
	}
	
	private List<Dataset> prioritizedKmean(Dataset dataset, int priorityLevel, int[] priority, int[] clusterSize){
		//System.out.println("KMean:"+priorityLevel);
		
		//Setup dataset
		int attribute = priority[priorityLevel];
		switchData(dataset, attribute);
		
		//Invalid dataset (not enough unique data) -- return the dataset appended with null until the number satisfied
		//the expected cluster number
		if(!validForClustering(dataset, clusterSize[priorityLevel])){
			List<Dataset> resultList = new LinkedList<Dataset>();
			resultList.add(dataset);
			int expectedClusterSize = 1;
			for(int i=priorityLevel; i<priority.length; i++){
				expectedClusterSize *= clusterSize[i];
			}
			for(int i=1; i<=expectedClusterSize-1; i++){
				resultList.add(null);
			}
			resultList.add(new DatasetMarker(attribute,true));
			return resultList;
		}
		
		//Apply k-mean recursively
		/*
		Iterator<Instance> it = dataset.iterator();
		while(it.hasNext()){
			Instance ins = it.next();
			System.out.println(ins.value(0)+"   "+ins.classValue());
		}
		*/
		Clusterer kmean = new KMeans(clusterSize[priorityLevel]);
		Dataset[] result = kmean.cluster(dataset);
		List<Dataset> resultList = new LinkedList<Dataset>();
		if(priorityLevel == priority.length-1){
			for(Dataset resultCluster : result){
				resultList.add(resultCluster);
				resultList.add(new DatasetMarker(attribute,false));
			}
			return resultList;
		}else{
			for(Dataset resultCluster : result){
				resultList.addAll( prioritizedKmean(resultCluster, priorityLevel+1, priority, clusterSize) );
				resultList.add(new DatasetMarker(attribute,false));
			}
			return resultList;
		}
	}
	
	private boolean validForClustering(Dataset dataset, int clusterSize){
		if(dataset.size() < clusterSize) return false;
		HashSet<Instance> uniqueInstance = new HashSet<Instance>();
		Iterator<Instance> it = dataset.iterator();
		while(it.hasNext()){
			uniqueInstance.add(it.next());
			if(uniqueInstance.size() >= clusterSize)
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("serial")
	private class DatasetMarker extends DefaultDataset{
		private int mode;
		private boolean invalid;
		public DatasetMarker(int mode, boolean invalid){
			super();
			this.mode = mode;
			this.invalid = invalid;
		}
		
		@Override
		public String toString(){
			return (invalid ? "--" : "")+mode;
		}
	}
	
	//=================================================
	
	/**
	 * Evaluate each profile individually against provided baseline using set error margin. Profile of the same enemy generated
	 * by different player (categorized by directory) is averaged before evaluation. Result is saved to csv file. Also, 3 lists are returned
	 * <br/> 
	 * 0) A list of enemies that are evaluated. <br/>
	 * 1) A list of enemies that does not pass the evaluation. (Miss rate lower than threshold) <br/>
	 * 2) Same as 1) but for Miss rate higher than threshold case
	 * Profile marker ("-" and "_") is trimmed so both lists can be used to compare against script name.
	 */
	public List<String>[] evaluateIndividually(float capDuration, float missrateBaseline,
			float hpPercentBaseline, FileHandle resultFile){
		int relevantRange = RangeProfile.calculateRelevantRange(rangeProfileTmp);
		HashMap<String,List<ClusteringProfile>> profileTable = new HashMap<String, List<ClusteringProfile>>();
		List<String> evaluatedEnemies = new LinkedList<String>();
		List<String> lowMissrateEnemies = new LinkedList<String>();
		List<String> highMissrateEnemies = new LinkedList<String>();
		constructProfileTable(battleProfileTmp, profileTable, relevantRange, (int)capDuration);
		String csv = "x,MissrateAvg,HpAvg,MissrateResult,HpResult,Pass\n";
		for(Entry<String,List<ClusteringProfile>> entry : profileTable.entrySet()){
			if(entry.getValue().size() == 0) continue;
			float missrateAvg = 0;
			float hpPercentAvg = 0;
			for(ClusteringProfile profile : entry.getValue()){
				missrateAvg += profile.missrate;
				hpPercentAvg += profile.remainingHpPercent;
			}
			missrateAvg /= entry.getValue().size();
			hpPercentAvg /= entry.getValue().size();
			boolean missrateResult = false;
			boolean hpResult = false;
			if(missrateBaseline >= 0){
				missrateResult = Math.abs(missrateAvg - missrateBaseline) <= errorMargin;
			}
			if(hpPercentBaseline >= 0){
				hpResult = Math.abs(hpPercentAvg - hpPercentBaseline) <= 0.00001f; //The float is epsilon
			}
			csv += entry.getKey()+","+missrateAvg+","+hpPercentAvg+","+missrateResult+","+hpResult+","+
					(missrateResult && hpResult)+"\n";
			
			//Trim profile marker so that we can get the actual script name
			String profileName = entry.getKey();
			if(profileName.startsWith("-")){
				profileName = profileName.substring(1);
			}
			if(profileName.startsWith("_")){
				profileName = profileName.substring(1);
			}
			evaluatedEnemies.add(profileName);
			if(!missrateResult || !hpResult){
				if(missrateAvg < missrateBaseline){
					lowMissrateEnemies.add(profileName);
				}else{
					highMissrateEnemies.add(profileName);
				}
			}
		}
		resultFile.writeString(csv, false);
		return new List[]{evaluatedEnemies, lowMissrateEnemies, highMissrateEnemies};
	}
	
	//=================================================
	
	public void dumpProfile(float capDuration, FileHandle resultFile){
		int relevantRange = RangeProfile.calculateRelevantRange(rangeProfileTmp);
		HashMap<String,List<ClusteringProfile>> profileTable = new HashMap<String, List<ClusteringProfile>>();
		constructProfileTable(battleProfileTmp, profileTable, relevantRange, (int)capDuration, true);
		//String csv = "x,Dur,MissrateAvg,HpAvg(cap="+capDuration+")\n";
		String csv = "";
		for(Entry<String,List<ClusteringProfile>> entry : profileTable.entrySet()){
			if(entry.getValue().size() == 0){
				csv += entry.getKey()+",---------\n";
				continue;
			}
			csv += entry.getKey()+"";
			float durationAvg = 0;
			float missrateAvg = 0;
			float hpPercentAvg = 0;
			for(ClusteringProfile profile : entry.getValue()){
				durationAvg += profile.battleDuration;
				missrateAvg += profile.missrate;
				hpPercentAvg += profile.remainingHpPercent;
				//csv += ","+profile.remainingHpPercent;
			}
			//csv += "\n";
			durationAvg /= entry.getValue().size();
			missrateAvg /= entry.getValue().size();
			hpPercentAvg /= entry.getValue().size();
			csv += entry.getKey()+","+durationAvg+","+missrateAvg+","+hpPercentAvg+"\n";
		}
		resultFile.writeString(csv, false);
	}
}
