package com.veil.ai;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.veil.game.element.DynamicEntity;
import com.veil.game.element.Player;

public class BattleProfile {
	
	private class EnemyLog implements Serializable {
		private static final long serialVersionUID = -8664985101334101023L;
		
		private Player player;
		private DynamicEntity entity;
		private boolean isMainAgent;
		private int maxHP, lastHP;
		private int startFrame, lifetime; //The first frame of profiling is 0
		private List<Integer> distSq;
		private List<Integer> hitPlayerFrame;
		private List<Integer> damagedFrame;
		private boolean shouldNotUpdate, updated;
		
		public EnemyLog(Player player, DynamicEntity entity, boolean isMainAgent, int startFrame){
			this.player = player;
			this.entity = entity;
			this.isMainAgent = isMainAgent;
			maxHP = entity.getBaseHP();
			lastHP = entity.getBaseHP();
			this.startFrame = startFrame;
			this.lifetime = 0;
			distSq = new LinkedList<Integer>();
			hitPlayerFrame = new LinkedList<Integer>();
			damagedFrame = new LinkedList<Integer>();
			shouldNotUpdate = false;
		}
		
		public void onPreupdate(){
			if(shouldNotUpdate) return;
			lifetime++;
			updated = true;
		}
		
		public void postUpdateCheck(){
			if(shouldNotUpdate) return;
			distSq.add(
					(int)player.getCenteredPositionCorrespondToRF().dst2(entity.getCenteredPositionCorrespondToRF())
					);
			if(lastHP > entity.getBaseHP()){
				lastHP = entity.getBaseHP();
				damagedFrame.add(lifetime-1);
			}
			if(!updated){
				shouldNotUpdate = true;
				entity = null;
			}
		}
		
		public void hitPlayer(){
			hitPlayerFrame.add(lifetime-1);
		}
		
		public void clearUpdateFlag(){
			updated = false;
		}
		
		public boolean everCloseToPlayer(int distance){
			distance = distance*distance;
			for(Integer d : distSq){
				if(d <= distance) return true;
			}
			return false;
		}
		
		private void writeObject(java.io.ObjectOutputStream out) throws IOException {
			out.writeBoolean(isMainAgent);
			out.writeInt(startFrame);
			out.writeInt(maxHP);
			out.writeInt(lifetime);
			out.writeInt(distSq.size());
			for(int data : distSq){
				out.writeInt(data);
			}
			out.writeInt(hitPlayerFrame.size());
			for(int data : hitPlayerFrame){
				out.writeInt(data);
			}
			if(isMainAgent){
				out.writeInt(lastHP);
				out.writeInt(damagedFrame.size());
				for(int data : damagedFrame){
					out.writeInt(data);
				}
			}
		}
		
		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
			isMainAgent = in.readBoolean();
			startFrame = in.readInt();
			maxHP = in.readInt();
			lifetime = in.readInt();
			distSq = new LinkedList<Integer>();
			int listSize = in.readInt();
			for(int i=0; i<listSize; i++){
				distSq.add(in.readInt());
			}
			hitPlayerFrame = new LinkedList<Integer>();
			listSize = in.readInt();
			for(int i=0; i<listSize; i++){
				hitPlayerFrame.add(in.readInt());
			}
			if(isMainAgent){
				lastHP = in.readInt();
				damagedFrame = new LinkedList<Integer>();
				listSize = in.readInt();
				for(int i=0; i<listSize; i++){
					damagedFrame.add(in.readInt());
				}
			}
		}

		@SuppressWarnings("unused")
		private void readObjectNoData() throws ObjectStreamException {
			isMainAgent = false;
			startFrame = -1;
			hitPlayerFrame = new LinkedList<Integer>();
		}
		
		@Override
		public String toString(){
			StringBuilder strb = new StringBuilder();
			if(entity != null){
				strb.append(entity.identifier);
				strb.append(" (").append(entity.hashCode()).append(") ");
			}else{
				strb.append("entity ");
			}
			strb.append("start "+startFrame+" lifetime "+lifetime+" ");
			strb.append("[ ");
			for(int i : hitPlayerFrame){
				strb.append(i).append(" ");
			}
			strb.append("]");
			if(isMainAgent){
				strb.append("{ ");
				for(int i : damagedFrame){
					strb.append(i).append(" ");
				}
				strb.append("}");
			}
			return strb.toString();
		}
	}
	
	public static BattleProfile instance = new BattleProfile();
	
	private boolean startProfile;
	private String name;
	private boolean unbeatable, playerDead;
	private int playerRemainingHP = -1; //This field may be inaccurate (if battle is skipped during the frame the player loses HP)
	private HashMap<Integer,EnemyLog> logs = new HashMap<Integer,EnemyLog>();
	private int frameCounter = 0;
	
	public BattleProfile(){
		saveAndReset(null,BattleSessionEndReason.InitialSession,null);
	}
	
	/**
	 * Save previous profile (if available) to target directory and reset with new profile name ready to be updated
	 */
	public void saveAndReset(String newProfileName, BattleSessionEndReason endReason, FileHandle dir){
		if(name != null && dir != null){
			unbeatable = endReason == BattleSessionEndReason.Unbeatable;
			playerDead = endReason == BattleSessionEndReason.PlayerDead;
			//Should save profile before resetting
			String filename = name+".txt";
			if(unbeatable) filename = "_"+filename;
			if(playerDead) filename = "-"+filename;
			save(dir.child(filename));
		}
		startProfile = false;
		name = newProfileName;
		unbeatable = false;
		playerDead = false;
		playerRemainingHP = -1;
		logs.clear();
		frameCounter = 0;
	}
	
	public boolean isStart(){
		return startProfile;
	}
	
	//Update profile with pre-update snapshot
	public void preUpdate(LevelSnapshot snapshot){
		if(!startProfile){
			if(snapshot.playerOnFloor){
				startProfile = true;
			}else{
				return;
			}
		}
		for(EnemyLog log : logs.values()){
			log.clearUpdateFlag();
		}
		if(snapshot.enemy != null){
			if(!logs.containsKey(snapshot.enemy.hashCode())){
				logs.put(snapshot.enemy.hashCode(), new EnemyLog(snapshot.player, snapshot.enemy, true, frameCounter));
			}
			logs.get(snapshot.enemy.hashCode()).onPreupdate();
		}
		for(DynamicEntity dyn : snapshot.tempRect.keySet()){
			if(!logs.containsKey(dyn.hashCode())){
				logs.put(dyn.hashCode(), new EnemyLog(snapshot.player, dyn, false, frameCounter));
			}
			logs.get(dyn.hashCode()).onPreupdate();
		}
		playerRemainingHP = snapshot.player.getBaseHP();
	}
	
	public void hitPlayer(DynamicEntity attacker){
		if(!startProfile) return;
		logs.get(attacker.hashCode()).hitPlayer();
	}
	
	public void postUpdate(){
		if(!startProfile) return;
		for(EnemyLog log : logs.values()){
			log.postUpdateCheck();
		}
		frameCounter++;
	}
	
	public void print(){
		System.out.println(name);
		for(EnemyLog log : logs.values()){
			System.out.println(log);
		}
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>");
	}
	
	public void save(FileHandle fh){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeBoolean(unbeatable);
			out.writeBoolean(playerDead);
			out.writeInt(playerRemainingHP);
			out.writeInt(logs.size());
			for(EnemyLog log : logs.values()){
				out.writeObject(log);
			}
			byte[] bytes = bos.toByteArray();
			fh.writeBytes(bytes, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Calculate necessary parameters required to evaluate battle profile from existing profiles in specified directory. <br/>
	 * Return length-3 array <br/>
	 * [0] = (int) Battle duration (used to set battle timelimit for AI)
	 * [1] = (float) Good miss rate (AI must perform at similar rate)
	 * [2] = (float) Good remaining HP percentage (AI must perform at similar percentage)
	 */
	public static float[] calculateEvaluationParameter(FileHandle dir, int relevantRange){
		float[] params = new float[3];
		int sampleSize = calculateTotalBattleDuration(dir, params);
		params[0] /= sampleSize;
		sampleSize = calculateMissRate(dir, params, relevantRange);
		params[1] /= sampleSize;
		sampleSize = calculateHPPercent(dir, params, (int)params[0]);
		params[2] /= sampleSize;
		return params;
	}
	
	private static int calculateTotalBattleDuration(FileHandle dir, float[] out){
		int sampleSize = 0;
		for(FileHandle fh : dir.list()){
			if(!fh.isDirectory()){
				BattleProfile profile = new BattleProfile();
				profile.load(fh);
				if(profile.isValidForBattleDuration()){
					out[0] += profile.getBattleDuration();
					sampleSize++;
				}
			}else{
				sampleSize += calculateTotalBattleDuration(fh, out);
			}
		}
		return sampleSize;
	}
	
	private static int calculateMissRate(FileHandle dir, float[] out, int relevantRange){
		int sampleSize = 0;
		for(FileHandle fh : dir.list()){
			if(!fh.isDirectory()){
				BattleProfile profile = new BattleProfile();
				profile.load(fh);
				out[1] += profile.getMissRate(relevantRange);
				sampleSize++;
			}else{
				sampleSize += calculateMissRate(fh, out, relevantRange);
			}
		}
		return sampleSize;
	}
	
	private static int calculateHPPercent(FileHandle dir, float[] out, int averageTimelimit){
		int sampleSize = 0;
		for(FileHandle fh : dir.list()){
			if(!fh.isDirectory()){
				BattleProfile profile = new BattleProfile();
				profile.load(fh);
				if(profile.isValidForRemainingHP()){
					out[2] += profile.getRemainingHPPercent(averageTimelimit);
					sampleSize++;
				}
			}else{
				sampleSize += calculateHPPercent(fh, out, averageTimelimit);
			}
		}
		return sampleSize;
	}
	
	/**
	 * Load battle profile. Not everything is saved, so it is expected that only methods below are invoked on the loaded version
	 * of profile  
	 */
	public void load(FileHandle fh){
		byte[] data = fh.readBytes();
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = null;
		try {
			name = fh.nameWithoutExtension();
			in = new ObjectInputStream(bis);
			unbeatable = in.readBoolean();
			playerDead = in.readBoolean();
			playerRemainingHP = in.readInt();
			int remaining = in.readInt();
			logs.clear();
			while(remaining > 0){
				logs.put(remaining, (EnemyLog)in.readObject());
				remaining--;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error loading battle profile "+fh.nameWithoutExtension());
		}
	}
	
	private boolean isValidForBattleDuration(){
		if(unbeatable) return false;
		if(playerDead){
			for(EnemyLog log : logs.values()){
				if(log.isMainAgent){
					return log.lastHP < log.maxHP;
				}
			}
		}
		return true;
	}
	
	private boolean isValidForRemainingHP(){
		return isValidForBattleDuration();
	}
	
	private int getBattleDuration(){
		int timelimit = 0;
		EnemyLog mainAgent = null;
		for(EnemyLog log : logs.values()){
			int afterLastFrame = log.startFrame+log.lifetime;
			if(afterLastFrame > timelimit){
				timelimit = afterLastFrame;
			}
			if(log.isMainAgent){
				mainAgent = log;
			}
		}
		if(playerDead && mainAgent.lastHP > 0){
			//Extrapolate timelimit based on main agent's remaining HP in case player is dead
			int damageDealtToEnemy = mainAgent.maxHP - mainAgent.lastHP;
			timelimit = (int)(timelimit * mainAgent.maxHP * 1f / damageDealtToEnemy);
		}
		//System.out.println("time "+name+" "+timelimit);
		return timelimit;
	}
	
	private float getMissRate(final int relevantRange){
		//Filter only entities that ever gets close to player
		HashSet<EnemyLog> set = new HashSet<EnemyLog>();
		set.addAll(logs.values());
		set.removeIf(log -> !log.everCloseToPlayer(relevantRange));
		if(set.size() == 0){
			return 0; //Would this causes bias??
		}
		
		float sumMissRate = 0;
		for(EnemyLog log : set){
			//Calculate miss rate against individual entity
			int maxHitCount = MathUtils.ceil(log.lifetime / 60f); // ceil(EntityLifetime/InvulFrame)
			int hitCount = 0;
			int lastHitFrame = -1;
			Collections.sort(log.hitPlayerFrame);
			for(int frame : log.hitPlayerFrame){
				if(lastHitFrame < 0 || frame - lastHitFrame >= 60){
					hitCount++;
					lastHitFrame = frame;
				}
			}
			sumMissRate += hitCount*1f/maxHitCount;
		}
		
		//System.out.println("miss "+name+" "+hitCount+" "+set.size());
		return Math.min(1, sumMissRate/set.size());
	}
	
	private float getRemainingHPPercent(int timeLimit){
		for(EnemyLog log : logs.values()){
			if(log.isMainAgent){
				int remainingHp = log.maxHP;
				for(int damagedFrame : log.damagedFrame){
					if(damagedFrame >= timeLimit){
						break;
					}
					remainingHp--;
				}
				if(remainingHp < 0){
					remainingHp = 0;
				}
				//System.out.println("hp "+name+" "+remainingHp+"("+log.lastHP+") /"+log.maxHP);
				return remainingHp*1f/log.maxHP;
			}
		}
		return 1; //Impossible case
	}
}
