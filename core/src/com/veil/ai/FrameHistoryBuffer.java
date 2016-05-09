package com.veil.ai;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class FrameHistoryBuffer {

	//Change threshold that will cause frame[x-2] obsolete when calculating change at frame[x]
	public static float distanceChangeThreshold = 15; //unit in pixel
	public static float directionChangeThreshold = 20; //unit in degree
	
	public boolean verbose = false;
	
	private List<Rectangle> buf;
	private int bufSize;
	private boolean updated;
	
	public FrameHistoryBuffer(int bufferSize){
		buf = new LinkedList<Rectangle>();
		bufSize = bufferSize;
	}
	
	public void clearUpdateFlag(){
		updated = false;
	}
	
	public boolean isUpdated(){
		return updated;
	}
	
	public void addFrame(Rectangle rect){
		if(buf.size() == bufSize){
			buf.remove(0);
		}
		buf.add(rect);
		updated = true;
	}
	
	//NOTE:: Prediction algorithm may need to consult research on "path extrapolation in human"
	public Rectangle[] predictNextFrame(int futureFrame){
		if(buf.size() == 0){
			if(verbose) System.out.println("Empty buf");
			return null;
		}else if(buf.size() == 1){
			//Predict as static object since there is not enough data
			Rectangle startingFrame = buf.iterator().next(); 
			Rectangle[] frames = new Rectangle[futureFrame];
			for(int i=0; i<frames.length; i++){
				frames[i] = new Rectangle(startingFrame);
			}
			if(verbose) System.out.println("Single frame buf");
			return frames;
		}else if(buf.size() == 2){
			//Use the same displacement, we have no enough data for direction change
			Iterator<Rectangle> it = buf.iterator();
			Rectangle first = it.next();
			Rectangle second = it.next();
			Vector2 displacement = new Vector2(second.x - first.x, second.y - first.y);
			Rectangle[] frames = new Rectangle[futureFrame];
			frames[0] = new Rectangle(second.x+displacement.x, second.y+displacement.y, second.width, second.height);
			for(int i=1; i<frames.length; i++){
				frames[i] = new Rectangle(frames[i-1].x+displacement.x, frames[i-1].y+displacement.y, 
						frames[i-1].width, frames[i-1].height);
			}
			if(verbose) System.out.println("2 frames buf");
			return frames;
		}
		
		//[0] is not used, [x]=frame[x]-frame[x-1]
		Vector2[] displacement = new Vector2[buf.size()];
		//[0] is not used, [1]=0, [x]=dist(frame[x]-frame[x-1]) - dist(frame[x-1]-frame[x-2])
		float[] distanceChange = new float[buf.size()];
		//[0] is not used, [1]=0, [x]=angdiff(frame[x]-frame[x-1], frame[x-1]-frame[x-2])
		float[] directionChange = new float[buf.size()];
		Rectangle lastFrame = null;
		//Setup displacement & direction change data
		Rectangle prevFrame = null;
		int frame=0;
		int firstValidFrame = 0;
		for(Rectangle rect : buf){
			if(frame > 0){
				displacement[frame] = new Vector2(rect.x - prevFrame.x, rect.y - prevFrame.y);
				if(frame == 1){
					distanceChange[frame] = 0;
					directionChange[frame] = 0;
				}else{
					distanceChange[frame] = displacement[frame].len() - displacement[frame-1].len();
					directionChange[frame] = displacement[frame].angle(displacement[frame-1]);
					if(Math.abs(distanceChange[frame]) > distanceChangeThreshold || 
							Math.abs(directionChangeThreshold) > directionChangeThreshold){
						firstValidFrame = frame-1;
					}
				}
				if(frame == buf.size()-1){
					lastFrame = rect;
				}
			}
			prevFrame = rect;
			frame++;
		}
		
		//Predict based on data
		/*
		float predictedDistance = 0;
		float predictedDirection = 0;
		*/
		Rectangle[] result = new Rectangle[futureFrame];
		if(firstValidFrame+1 == displacement.length-1){
			//2 valid frames available for prediction
			Vector2 displacementVec = displacement[displacement.length-1];
			//Rectangle lastRect = buf.get(buf.size()-1);
			result[0] = new Rectangle(lastFrame.x+displacementVec.x, lastFrame.y+displacementVec.y, lastFrame.width, lastFrame.height);
			for(int i=1; i<result.length; i++){
				result[i] = new Rectangle(result[i-1].x+displacementVec.x, result[i-1].y+displacementVec.y, 
						result[i-1].width, result[i-1].height);
			}
			if(verbose) System.out.println("2 valid frames buf");
			/*
			predictedDistance = displacement[displacement.length-1].len();
			predictedDirection = 0;
			*/
		}else{
			//at least 3 frames for prediction
			int displacementVarCount = displacement.length-(firstValidFrame+1);
			double[] displacementPredictor = new double[displacementVarCount];
			double[] displacementVar = new double[displacementVarCount];
			double[] directionchangePredictor = new double[displacementVarCount-1];
			double[] directionChangeVar = new double[displacementVarCount-1];
			for(int i=0; i<displacementVar.length; i++){
				displacementPredictor[i] = i;
				displacementVar[i] = displacement[firstValidFrame+1+i].len();
				if(i > 0){
					directionchangePredictor[i-1] = i-1;
					directionChangeVar[i-1] = directionChange[firstValidFrame+1+i];
				}
			}
			//Rectangle lastRect = null;
			Vector2 vec = null;
			for(int i=0; i<futureFrame; i++){
				regression.PolynomialRegression regression = 
						new regression.PolynomialRegression(displacementPredictor, displacementVar, 1);
				float predictedDistance = (float)regression.predict(displacementPredictor.length+i);
				regression = new regression.PolynomialRegression(directionchangePredictor, directionChangeVar, 1);
				float predictedDirection = (float)regression.predict(directionchangePredictor.length+i);
				if(i == 0){
					vec = new Vector2(displacement[displacement.length-1]);
				}else{
					vec = new Vector2(result[i-1].x - lastFrame.x, result[i-1].y - lastFrame.y);
					lastFrame = result[i-1];
				}
				vec.rotate(predictedDirection);
				vec.setLength(predictedDistance);
				result[i] = new Rectangle(lastFrame.x+vec.x, lastFrame.y+vec.y, lastFrame.width, lastFrame.height);
			}
			if(verbose) System.out.println("Regression based prediction");
			/*
			boolean passFirstFrame = false;
			for(int i=firstValidFrame+1; i<displacement.length; i++){
				if(passFirstFrame){
					predictedDirection += directionChange[i];
				}
				predictedDistance += displacement[i].len();
				passFirstFrame = true;
			}
			int frameCount = displacement.length-(firstValidFrame+1);
			predictedDistance /= frameCount;
			frameCount--;
			predictedDirection /= frameCount;
			*/
		}
		/*
		Vector2 vec = new Vector2(displacement[displacement.length-1]);
		vec.rotate(predictedDirection);
		vec.setLength(predictedDistance);
		Rectangle result = new Rectangle(lastFrame.x+vec.x, lastFrame.y+vec.y, lastFrame.width, lastFrame.height);
		*/
		
		//Clean up obsolete frame
		while(firstValidFrame > 0){
			buf.remove(0);
			firstValidFrame--;
		}
		return result;
	}
}
