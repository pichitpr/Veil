package com.veil.game.collider;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Collider {

	private class Line{
		public Vector2 start,end;
		
		public Line(Vector2 start,Vector2 end){
			this.start = start;
			this.end = end;
		}
	}
	
	public enum ColliderType{CIRCLE,SQUARE,LINE};
	
	private ColliderType type;
	private Object gdxShape;
	
	public boolean intersect(Collider c){
		if(this.type == ColliderType.LINE || c.type == ColliderType.LINE){
			
		}else{
			if(this.type == c.type){
				if(this.type == ColliderType.CIRCLE){
					return Intersector.overlaps((Circle)gdxShape, (Circle)c.gdxShape);
				}else{
					return Intersector.overlaps((Rectangle)c.gdxShape, (Rectangle)gdxShape);
				}
			}else{
				if(this.type == ColliderType.CIRCLE){
					return Intersector.overlaps((Circle)gdxShape, (Rectangle)c.gdxShape);
				}else{
					return Intersector.overlaps((Circle)c.gdxShape, (Rectangle)gdxShape);
				}
			}
		}
		return false;
	}
}
