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
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;

public class RangeProfile {

	public class DistanceLog implements Serializable {
		private static final long serialVersionUID = 8048529594171440656L;
		
		private int speed;
		private List<Integer> list;
		
		public DistanceLog(int speed){
			this.speed = speed;
			list = new LinkedList<Integer>();
		}
		
		public void add(int distance){
			list.add(distance);
		}
		
		public int sum(){
			int sum = 0;
			for(int d : list) sum += d;
			return sum;
		}
		
		public int sampleSize(){
			return list.size();
		}
		
		public float average(){
			if(list.size() == 0) return 0;
			return 1f*sum()/sampleSize();
		}
		
		@Override
		public String toString(){
			StringBuilder strb = new StringBuilder();
			strb.append(speed).append(" :");
			for(int i : list){
				strb.append(" ").append(i);
			}
			return strb.toString();
		}
		
		private void writeObject(java.io.ObjectOutputStream out) throws IOException {
			out.writeInt(speed);
			out.writeInt(list.size());
			for(int data : list){
				out.writeInt(data);
			}
		}
		
		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
			speed = in.readInt();
			list = new LinkedList<Integer>();
			int listSize = in.readInt();
			for(int i=0; i<listSize; i++){
				list.add(in.readInt());
			}
		}

		@SuppressWarnings("unused")
		private void readObjectNoData() throws ObjectStreamException {
			this.speed = -1;
			list = new LinkedList<Integer>();
		}
	}
	
	public static RangeProfile instance = new RangeProfile();
	
	private HashMap<Integer, DistanceLog> logs = new HashMap<Integer, DistanceLog>();
	private boolean sessionLogged = false;
	private int pendingSpeed, pendingDistance;
	
	public RangeProfile(){
		reset(null);
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
			sessionLogged = true;
		}
	}
	
	public void onSessionEnd(){
		if(sessionLogged && pendingSpeed > -1){
			if(!logs.containsKey(pendingSpeed)){
				logs.put(pendingSpeed, new DistanceLog(pendingSpeed));
			}
			logs.get(pendingSpeed).add(pendingDistance);
		}
		pendingSpeed = -1;
		pendingDistance = -1;
		sessionLogged = false;
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
	
	private void load(FileHandle fh){
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
