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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
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
	private boolean unbeatable;
	private HashMap<Integer,EnemyLog> logs = new HashMap<Integer,EnemyLog>();
	private int frameCounter = 0;
	
	public BattleProfile(){
		reset(null,false,null);
	}
	
	/**
	 * Save previous profile (if available) to target directory and reset with new profile name ready to be updated
	 */
	public void reset(String newProfileName, boolean resetFromUnbeatable, FileHandle dir){
		if(name != null && dir != null){
			unbeatable = resetFromUnbeatable;
			//Should save profile before resetting
			save(dir.child((resetFromUnbeatable ? "_" : "")+name+".txt"));
		}
		startProfile = false;
		name = newProfileName;
		unbeatable = false;
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
	 * [0] = (int) Battle timelimit
	 * [1] = (float) Good evasion rate (AI must perform at equal or higher rate)
	 * [2] = (float) Good remaining HP percentage (AI must perform at equal or lower percentage)
	 */
	public static float[] calculateEvaluationParameter(FileHandle dir, int relevantRange){
		float[] params = new float[3];
		int sampleSize = calculateTotalTimelimit(dir, params);
		params[0] /= sampleSize;
		sampleSize = calculateTotalRate(dir, params, relevantRange, (int)params[0]);
		params[1] /= sampleSize;
		params[2] /= sampleSize;
		return params;
	}
	
	private static int calculateTotalTimelimit(FileHandle dir, float[] out){
		int sampleSize = 0;
		for(FileHandle fh : dir.list()){
			if(!fh.isDirectory()){
				BattleProfile profile = new BattleProfile();
				profile.load(fh);
				out[0] += profile.getTimelimit();
				sampleSize++;
			}else{
				sampleSize += calculateTotalTimelimit(fh, out);
			}
		}
		return sampleSize;
	}
	
	private static int calculateTotalRate(FileHandle dir, float[] out, int relevantRange, int averageTimelimit){
		int sampleSize = 0;
		for(FileHandle fh : dir.list()){
			if(!fh.isDirectory()){
				BattleProfile profile = new BattleProfile();
				profile.load(fh);
				out[1] += profile.getEvasionRate(relevantRange);
				out[2] += profile.getRemainingHPPercent(averageTimelimit);
				sampleSize++;
			}else{
				sampleSize += calculateTotalRate(fh, out, relevantRange, averageTimelimit);
			}
		}
		return sampleSize;
	}
	
	/**
	 * Load battle profile. Not everything is saved, so it is expected that only methods below are invoked on the loaded version
	 * of profile  
	 */
	private void load(FileHandle fh){
		byte[] data = fh.readBytes();
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = null;
		try {
			name = fh.nameWithoutExtension();
			in = new ObjectInputStream(bis);
			unbeatable = in.readBoolean();
			int remaining = in.readInt();
			logs.clear();
			while(remaining > 0){
				logs.put(remaining, (EnemyLog)in.readObject());
				remaining--;
			}
		} catch (Exception e) {
			System.err.println("Error loading battle profile "+fh.nameWithoutExtension());
		}
	}
	
	private int getTimelimit(){
		int timelimit = 0;
		for(EnemyLog log : logs.values()){
			int afterLastFrame = log.startFrame+log.lifetime;
			if(afterLastFrame > timelimit){
				timelimit = afterLastFrame;
			}
		}
		return timelimit;
	}
	
	private float getEvasionRate(int relevantRange){
		HashSet<EnemyLog> set = new HashSet<EnemyLog>();
		set.addAll(logs.values());
		set.removeIf(log -> !log.everCloseToPlayer(relevantRange));
		int hitCount = 0;
		for(EnemyLog log : set){
			for(int frame : log.hitPlayerFrame){
				if(log.distSq.get(frame) <= relevantRange){
					hitCount++;
				}
			}
		}
		return Math.min(1, hitCount*1f/set.size());
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
				return remainingHp*1f/log.maxHP;
			}
		}
		return 1; //Impossible case
	}
}
