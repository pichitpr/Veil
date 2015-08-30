package com.veil.adl;

import java.util.HashMap;

import adl_2daa.AgentModel;
import adl_2daa.Registry;
import adl_2daa.ast.structure.Agent;
import adl_2daa.ast.structure.Root;
import adl_2daa.tool.ADLCompiler;
import adl_2daa.tool.Parser;

import com.badlogic.gdx.files.FileHandle;
import com.veil.adl.action.AddExtraVelocityToPlayer;
import com.veil.adl.action.ChangeDirectionToPlayerByStep;
import com.veil.adl.action.Debug;
import com.veil.adl.action.Despawn;
import com.veil.adl.action.FlipDirection;
import com.veil.adl.action.FloorStun;
import com.veil.adl.action.Goto;
import com.veil.adl.action.Jump;
import com.veil.adl.action.Notify;
import com.veil.adl.action.RunCircling;
import com.veil.adl.action.RunHarmonic;
import com.veil.adl.action.RunStraight;
import com.veil.adl.action.RunTo;
import com.veil.adl.action.Set;
import com.veil.adl.action.Spawn;
import com.veil.adl.action.Var;
import com.veil.adl.action.VarDec;
import com.veil.adl.action.VarInc;
import com.veil.adl.action.VarSet;
import com.veil.adl.action.Wait;
import com.veil.adl.function.Abs;
import com.veil.adl.function.Anchor;
import com.veil.adl.function.Attack;
import com.veil.adl.function.Attacked;
import com.veil.adl.function.ButtonPress;
import com.veil.adl.function.CollideWithDynamic;
import com.veil.adl.function.Damage;
import com.veil.adl.function.Damaged;
import com.veil.adl.function.DecimalSet;
import com.veil.adl.function.DecimalSetSymmetry;
import com.veil.adl.function.DirectionComponent;
import com.veil.adl.function.DirectionSet;
import com.veil.adl.function.DirectionSetDivide;
import com.veil.adl.function.DirectionSetRange;
import com.veil.adl.function.DistanceTo;
import com.veil.adl.function.DistanceToPlayer;
import com.veil.adl.function.DynamicCount;
import com.veil.adl.function.DynamicFilter;
import com.veil.adl.function.Get;
import com.veil.adl.function.InTheAir;
import com.veil.adl.function.Notified;
import com.veil.adl.function.Peak;
import com.veil.adl.function.Perpendicular;
import com.veil.adl.function.Random;
import com.veil.adl.function.RandomPositionInRadius;
import com.veil.adl.function.RangeCap;
import com.veil.adl.function.RangeCapCircular;
import com.veil.adl.function.Rel;
import com.veil.adl.function.RelDirection;
import com.veil.adl.function.RelPlayer;
import com.veil.adl.function.SurfaceInDir;
import com.veil.adl.function.TimePass;
import com.veil.adl.function.TravelDistance;
import com.veil.adl.function.TurnTo;
import com.veil.adl.function.TurnToPlayer;
import com.veil.adl.function.VarGet;
import com.veil.platforminterface.PlatformUtility;

public class AgentDatabase {

	public static boolean setup(){
		registerAction();
		registerFunction();
		modelMap = new HashMap<String, AgentModel>();
		return parseScriptInDir(PlatformUtility.fileOperator.getStorageRoot().child("Script"));
	}
	
	private static HashMap<String,AgentModel> modelMap;
	
	public static AgentModel getAgentModelFor(String identifier){
		return modelMap.get(identifier);
	}
	
	private static void registerAction(){
		Registry.registerAction("AddExtraVelocityToPlayer", new AddExtraVelocityToPlayer());
		Registry.registerAction("ChangeDirectionToPlayerByStep", new ChangeDirectionToPlayerByStep());
		Registry.registerAction("Debug", new Debug());
		Registry.registerAction("Despawn", new Despawn());
		Registry.registerAction("FlipDirection", new FlipDirection());
		Registry.registerAction("FloorStun", new FloorStun());
		Registry.registerAction("Goto", new Goto());
		Registry.registerAction("Jump", new Jump());
		Registry.registerAction("Notify", new Notify());
		Registry.registerAction("RunCircling", new RunCircling());
		Registry.registerAction("RunHarmonic", new RunHarmonic());
		Registry.registerAction("RunStraight", new RunStraight());
		Registry.registerAction("RunTo", new RunTo());
		Registry.registerAction("Set", new Set());
		Registry.registerAction("Spawn", new Spawn());
		Registry.registerAction("VarDec", new VarDec());
		Registry.registerAction("VarInc", new VarInc());
		Registry.registerAction("Var", new Var());
		Registry.registerAction("VarSet", new VarSet());
		Registry.registerAction("Wait", new Wait());
	}
	
	private static void registerFunction(){
		Registry.registerFunction("Abs",new Abs());
		Registry.registerFunction("Anchor",new Anchor());
		Registry.registerFunction("Attack",new Attack());
		Registry.registerFunction("Attacked",new Attacked());
		Registry.registerFunction("ButtonPress",new ButtonPress());
		Registry.registerFunction("CollideWithDynamic", new CollideWithDynamic());
		Registry.registerFunction("Damage",new Damage());
		Registry.registerFunction("Damaged",new Damaged());
		Registry.registerFunction("DecimalSet", new DecimalSet());
		Registry.registerFunction("DecimalSetSymmetry", new DecimalSetSymmetry());
		Registry.registerFunction("DirectionComponent", new DirectionComponent());
		Registry.registerFunction("DirectionSetDivide", new DirectionSetDivide());
		Registry.registerFunction("DirectionSet", new DirectionSet());
		Registry.registerFunction("DirectionSetRange", new DirectionSetRange());
		Registry.registerFunction("DistanceTo", new DistanceTo());
		Registry.registerFunction("DistanceToPlayer", new DistanceToPlayer());
		Registry.registerFunction("DynamicCount", new DynamicCount());
		Registry.registerFunction("DynamicFilter", new DynamicFilter());
		Registry.registerFunction("Get", new Get());
		Registry.registerFunction("InTheAir", new InTheAir());
		Registry.registerFunction("Notified", new Notified());
		Registry.registerFunction("Peak", new Peak());
		Registry.registerFunction("Perpendicular", new Perpendicular());
		Registry.registerFunction("Random", new Random());
		Registry.registerFunction("RandomPositionInRadius", new RandomPositionInRadius());
		Registry.registerFunction("RangeCapCircular", new RangeCapCircular());
		Registry.registerFunction("RangeCap", new RangeCap());
		Registry.registerFunction("Rel", new Rel());
		Registry.registerFunction("RelPlayer", new RelPlayer());
		Registry.registerFunction("SurfaceInDir", new SurfaceInDir());
		Registry.registerFunction("TimePass", new TimePass());
		Registry.registerFunction("TravelDistance", new TravelDistance());
		Registry.registerFunction("TurnTo", new TurnTo());
		Registry.registerFunction("TurnToPlayer", new TurnToPlayer());
		Registry.registerFunction("VarGet", new VarGet());
		
		//=========== v4
		Registry.registerFunction("RelDirection", new RelDirection());
	}
	
	private static boolean parseScriptInDir(FileHandle dir){
		String script;//,line;
		Parser parser;
		Root astRoot;
		AgentModel agentModel;
		for(FileHandle f : dir.list()){
			if(f.isDirectory()){ 
				if(!parseScriptInDir(f))
					return false;
			}else{
				System.out.println("Parsing & compiling file : "+f.name());
				try {
					script = f.readString();
					parser = new Parser();
					astRoot = parser.parse(script);
					for(Agent astAgent : astRoot.getRelatedAgents()){
						System.out.println("Compiling agent : "+astAgent.getIdentifier());
						agentModel = ADLCompiler.compile(astAgent);
						modelMap.put(astAgent.getIdentifier(), agentModel);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}
}
