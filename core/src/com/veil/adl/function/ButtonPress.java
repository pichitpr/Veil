package com.veil.adl.function;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.veil.ai.Controller;

public class ButtonPress implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		String key = param[0].toString();
		boolean result = false;
		if(key.equalsIgnoreCase("up")){
			result = Controller.instance.up;
			//result = Gdx.input.isKeyPressed(Input.Keys.UP);
		}else if(key.equalsIgnoreCase("down")){
			result = Controller.instance.down;
			//result = Gdx.input.isKeyPressed(Input.Keys.DOWN);
		}else if(key.equalsIgnoreCase("left")){
			result = Controller.instance.left;
			//result = Gdx.input.isKeyPressed(Input.Keys.LEFT);
		}else if(key.equalsIgnoreCase("right")){
			result = Controller.instance.right;
			//result = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
		}else if(key.equalsIgnoreCase("attack")){
			result = Controller.instance.shoot;
			//result = Gdx.input.isKeyPressed(Input.Keys.X);
		}else if(key.equalsIgnoreCase("jump")){
			result = Controller.instance.jump;
			//result = Gdx.input.isKeyPressed(Input.Keys.Z);
		}
		return result;
	}

}
