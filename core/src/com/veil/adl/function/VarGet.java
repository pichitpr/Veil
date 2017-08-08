package com.veil.adl.function;

import adl_2daa.AgentModelInterpreter;
import adl_2daa.IFunction;
import adl_2daa.SequenceInterpreter;

import com.veil.adl.ItpData;

public class VarGet implements IFunction{

	@Override
	public Object invoke(SequenceInterpreter itp, Object... param) {
		int var = param[0].toString().hashCode();
		AgentModelInterpreter aItp = ((ItpData)itp.getAdditionalData()).mainItp;
		if(aItp.getVarTable().containsKey(var)){
			return aItp.getVarTable().get(var);
		}else{
			return 0;
		}
	}

}
