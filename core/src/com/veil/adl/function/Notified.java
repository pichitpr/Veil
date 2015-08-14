package com.veil.adl.function;

import java.util.List;

import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;
import com.veil.game.element.DynamicEntity;

public class Notified implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		DynamicEntity dyn = ((ItpData)itp.getAdditionalData()).thisAgent;
		int expecting = ((Number)param[0]).intValue();
		List<Integer> activeMsg = dyn.flag.getMessageTable();
		boolean result = false;
		for(Integer i : activeMsg){
			if(i == expecting){
				result = true;
				break;
			}
		}
		return result;
	}

}
