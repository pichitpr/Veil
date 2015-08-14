package com.veil.game.collider;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class ColliderFeature {

	public static Vector2 getPositionalPointFromShape(Rectangle rect, PositionalFeature feature){
		switch(feature){
		case CENTER: return new Vector2(rect.x+rect.width/2, rect.y+rect.height/2);
		case HEAD: return new Vector2(rect.x+rect.width/2, rect.y+rect.height);
		case FEET: return new Vector2(rect.x+rect.width/2, rect.y);
		case LEFT: return new Vector2(rect.x, rect.y+rect.height/2);
		case RIGHT: return new Vector2(rect.x+rect.width, rect.y+rect.height/2);
		case TOP_LEFT: return new Vector2(rect.x, rect.y+rect.height);
		case TOP_RIGHT: return new Vector2(rect.x+rect.width, rect.y+rect.height);
		case FEET_LEFT: return new Vector2(rect.x, rect.y);
		case FET_RIGHT: return new Vector2(rect.x+rect.width, rect.y+rect.height);
		}
		return null;
	}
}
