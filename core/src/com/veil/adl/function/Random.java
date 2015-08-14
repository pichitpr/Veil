package com.veil.adl.function;

import java.util.List;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.badlogic.gdx.math.MathUtils;

public class Random implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		@SuppressWarnings("rawtypes")
		List list = (List)param[0];
		return list.get(MathUtils.random(list.size()-1));
	}

}
