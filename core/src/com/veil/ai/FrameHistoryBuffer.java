package com.veil.ai;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class FrameHistoryBuffer {
	
	public boolean verbose = false;
	
	private List<Rectangle> buf;
	private int bufSize;
	private boolean updated;
	
	public FrameHistoryBuffer(){
		buf = new LinkedList<Rectangle>();
		bufSize = AIConstant.historyBufferSize;
	}
	
	public List<Rectangle> getBuffer(){
		return buf;
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
		
		//Setup displacement & direction change data
		Vector2 currentDisplacement = null, prevDisplacement = null;
		Rectangle prevFrame = null;
		int frame=0;
		int firstValidFrame = 0;
		for(Rectangle rect : buf){
			if(frame > 0){
				currentDisplacement = new Vector2(rect.x - prevFrame.x, rect.y - prevFrame.y);
				if(frame > 1){
					if(Math.abs(currentDisplacement.len() - prevDisplacement.len()) > AIConstant.distanceChangeThreshold || 
							Math.abs(prevDisplacement.angle(currentDisplacement)) > AIConstant.directionChangeThreshold){
						firstValidFrame = frame-1;
					}
				}
				prevDisplacement = currentDisplacement;
			}
			prevFrame = rect;
			frame++;
		}
		
		//Predict based on data
		Rectangle[] result = new Rectangle[futureFrame];
		int validFrameCount = buf.size()-firstValidFrame;
		double[] xPredictor = new double[validFrameCount];
		double[] xVar = new double[validFrameCount];
		double[] yPredictor = new double[validFrameCount];
		double[] yVar = new double[validFrameCount];
		int i=0;
		frame = 0;
		float w=0,h=0;
		for(Rectangle rect : buf){
			if(frame >= firstValidFrame){
				xPredictor[i] = i;
				xVar[i] = rect.x;
				yPredictor[i] = i;
				yVar[i] = rect.y;
				w = rect.width;
				h = rect.height;
				i++;
			}
			frame++;
		}
		regression.PolynomialRegression xRegression = 
				new regression.PolynomialRegression(xPredictor, xVar, 1);
		regression.PolynomialRegression yRegression = 
				new regression.PolynomialRegression(yPredictor, yVar, 2);
		for(i=0; i<futureFrame; i++){
			float predictedX = (float)xRegression.predict(validFrameCount+i);
			float predictedY = (float)yRegression.predict(validFrameCount+i);
			result[i] = new Rectangle(predictedX, predictedY, w, h);
		}
		
		if(verbose){ 
			System.out.println("Regression based prediction");
			System.out.println("X model : "+xRegression);
			System.out.print("\tVar: ");
			for(double d : xVar){
				System.out.print((float)d+" ");
			}
			System.out.println("");
			System.out.println("Y model : "+yRegression);
			System.out.print("\tVar: ");
			for(double d : yVar){
				System.out.print((float)d+" ");
			}
			System.out.println("");
			System.out.println("================================== END REGRESSION");
		}
		
		//Clean up obsolete frame
		while(firstValidFrame > 0){
			buf.remove(0);
			firstValidFrame--;
		}
		return result;
	}
}
