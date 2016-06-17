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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.veil.game.GameConstant;

public class RangeProfile {
	
	public class DistanceLog implements Serializable {
		private static final long serialVersionUID = 8048529594171440656L;
		
		private int speed;
		private List<Integer> list;
		private List<Boolean> miss;
		
		public DistanceLog(int speed){
			this.speed = speed;
			list = new ArrayList<Integer>();
			miss = new ArrayList<Boolean>();
		}
		
		public void add(int distance, boolean playerDamaged){
			list.add(distance);
			miss.add(playerDamaged);
		}
		
		public int sum(){
			int sum = 0;
			for(int i=0; i<list.size(); i++){
				if(!miss.get(i))
					sum += list.get(i);
			}
			return sum;
		}
		
		public int sampleSize(){
			int validSize = 0;
			for(int i=0; i<list.size(); i++){
				if(!miss.get(i))
					validSize++;
			}
			return validSize;
		}
		
		public float average(){
			if(list.size() == 0) return 0;
			int size = sampleSize();
			if(size == 0) return 0;
			return 1f*sum()/size;
		}
		
		@Override
		public String toString(){
			StringBuilder strb = new StringBuilder();
			strb.append(speed).append(" :");
			for(int i=0; i<list.size(); i++){
				strb.append(" ");
				if(miss.get(i)){
					strb.append("-");
				}
				strb.append(list.get(i));
			}
			return strb.toString();
		}
		
		private void writeObject(java.io.ObjectOutputStream out) throws IOException {
			out.writeInt(speed);
			out.writeInt(list.size());
			for(int data : list){
				out.writeInt(data);
			}
			out.writeInt(miss.size());
			for(boolean data : miss){
				out.writeBoolean(data);
			}
		}
		
		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
			speed = in.readInt();
			int listSize = in.readInt();
			list = new ArrayList<Integer>(listSize);
			for(int i=0; i<listSize; i++){
				list.add(in.readInt());
			}
			listSize = in.readInt();
			miss = new ArrayList<Boolean>(listSize);
			for(int i=0; i<listSize; i++){
				miss.add(in.readBoolean());
			}
		}

		@SuppressWarnings("unused")
		private void readObjectNoData() throws ObjectStreamException {
			this.speed = -1;
			list = new LinkedList<Integer>();
		}
	}
	
	public static RangeProfile instance = new RangeProfile(GameConstant.profileDir.child("Screenshot_range"));
	
	private HashMap<Integer, DistanceLog> logs = new HashMap<Integer, DistanceLog>();
	private boolean sessionLogged = false;
	private int pendingSpeed, pendingDistance;
	private boolean playerDamaged;
	
	private FileHandle ssDir;
	private int ssFilename;
	
	public RangeProfile(){
		this(null);
	}
	
	public RangeProfile(FileHandle fh){
		reset(null);
		ssDir = fh;
		if(ssDir != null){
			ssFilename = ssDir.list().length;
		}
	}
	
	/**
	 * Save previous profile (if available) to target directory and reset
	 */
	public void reset(FileHandle fh){
		if(fh != null && logs.size() > 0){
			save(fh.child("range_profile.txt"));
		}
		pendingSpeed = -1;
		pendingDistance = -1;
		playerDamaged = false;
		sessionLogged = false;
		logs.clear();
	}
	
	public void preUpdate(LevelSnapshot snapshot){
		if(snapshot.playerState.jumping && !sessionLogged){
			String iden = snapshot.enemy.identifier;
			pendingSpeed = Integer.parseInt(iden.substring(iden.lastIndexOf("_")+1));
			if(snapshot.tempRect.size() > 0){
				Vector2 firstAttackPos = new Vector2();
				snapshot.tempRect.values().iterator().next().getCenter(firstAttackPos);
				Vector2 playerPos = new Vector2();
				snapshot.playerRect.getCenter(playerPos);
				pendingDistance = (int)firstAttackPos.dst(playerPos);
			}
			if(ssDir != null){
				ScreenshotUtility.takeScreenshot(ssDir, ""+ssFilename);
				ssFilename++;
			}
			sessionLogged = true;
		}
		if(snapshot.player.getBaseHP() < snapshot.player.maxhp){
			playerDamaged = true;
		}
	}
	
	/**
	 * Call when current range profiling session should end. Return true if the player is damaged during the session
	 */
	public boolean onSessionEnd(){
		boolean isPlayerDamaged = playerDamaged;
		if(sessionLogged && pendingSpeed > -1){
			if(!logs.containsKey(pendingSpeed)){
				logs.put(pendingSpeed, new DistanceLog(pendingSpeed));
			}
			logs.get(pendingSpeed).add(pendingDistance, playerDamaged);
			//System.out.println(logs.get(pendingSpeed));
		}
		pendingSpeed = -1;
		pendingDistance = -1;
		playerDamaged = false;
		sessionLogged = false;
		return isPlayerDamaged;
	}
	
	public void print(){
		for(DistanceLog log : logs.values()){
			System.out.println(log);
		}
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>");
	}
	
	public void save(FileHandle fh){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeInt(logs.size());
			for(DistanceLog log : logs.values()){
				out.writeObject(log);
			}
			byte[] bytes = bos.toByteArray();
			fh.writeBytes(bytes, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Calculate relevant range from all range profiles in specified directory
	 */
	public static int calculateRelevantRange(FileHandle dir){
		float[] out = new float[1];
		int sampleSize = calculateTotalDistance(dir, out);
		out[0] /= sampleSize;
		return (int)out[0];
	}
	
	private static int calculateTotalDistance(FileHandle dir, float[] out){
		int sampleSize = 0;
		for(FileHandle fh : dir.list()){
			if(!fh.isDirectory()){
				RangeProfile profile = new RangeProfile();
				profile.load(fh);
				profile.print();
				int totalDistance = 0;
				int counter = 0;
				for(DistanceLog log : profile.logs.values()){
					totalDistance += log.sum();
					counter += log.sampleSize();
				}
				out[0] += totalDistance;
				sampleSize += counter;
			}else{
				sampleSize += calculateTotalDistance(fh, out);
			}
		}
		return sampleSize;
	}
	
	public void load(FileHandle fh){
		byte[] data = fh.readBytes();
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			int remaining = in.readInt();
			logs.clear();
			while(remaining > 0){
				logs.put(remaining, (DistanceLog)in.readObject());
				remaining--;
			}
		} catch (Exception e) {
			System.err.println("Error loading range profile "+fh.nameWithoutExtension());
		}
	}
}
