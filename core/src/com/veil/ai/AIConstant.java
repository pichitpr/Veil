package com.veil.ai;

public class AIConstant {

	//History buffer constant for path prediction
	public static int historyBufferSize = 5;
	//Change threshold that will cause frame[x-2] obsolete when calculating change at frame[x]
	public static float distanceChangeThreshold = 15; //unit in pixel
	public static float directionChangeThreshold = 20; //unit in degree
	

	//AI controller constant
	public static int buttonSpamDelay = 10; //A number of frame delay for pressing shoot button (min 2)
	public static int reactionTime = 4; //A number of frame required to re-decide button press
	public static int simulationDepth = 3; //A number of future step required to simulate to find safest button combination
	public static int safeMargin = 5;
	
	
	//AI evaluation constant
	public static int timeframe;
	//Evasion rate: 1 - (unique enemy hit count) / (total unique enemies in range)
	public static int evasionProfileRange; //Range in radius
	public static float goodEvasionRate; //A threshold rate for AI to be considered good at evading
}
