package leaf.exec.simulation;

import java.util.ArrayList;
import java.util.HashMap;

import leaf.context.ContextData;
import leaf.context.LiveSituation;
import leaf.tools.LeafLog;

/**
 * This class aims to feed a live situation with randomly generated data.
 * It generates a context for some task and assert if the task will fail 
 * or not in this situation (expert knowledge)
 * 
 * @author Nathan Ramoly
 *
 */
public class LiveSituationRandomFactory {
	
	/**
	 * Rate of successful situation
	 * It can be 0 for experiments
	 */
	private static final double RATESUCCSIT = 0.20;
	
	/**
	 * Randomly generates data for the scenario:
	 * The robot is asked to give an object to the user.
	 * There is only one user. 3 rooms: kitchen <-> living room <-> bedroom
	 * Context data includes:
	 * User position, user activity, temperature, object relative pos
	 * object containance
	 * It randomly determines if the task failed or not
	 * Failure cases:
	 * obstacle on the way (chair in front of bed)
	 * User sleeping (room kitchen or bedroom)
	 * User working (only in bedroom)
	 * 
	 * 
	 * TODO id failure cause
	 * @param ls
	 * @return
	 */
	public static Boolean genScenarGive(LiveSituation ls)
	{
		LeafLog.m("LSFact", "Starting context generation...");
		
		//First, clear current situation
		ls.reset();
		
		//Determine if we generate a failure or a success
		Boolean success = Math.random() < RATESUCCSIT;
		
		//Determine the causes of failure
		Boolean failAct = false;
		Boolean failObs = false;
		Double randCause = Math.random();
		if(!success)
		{
			if(randCause < 0.3)
			{failObs = true;}
			else if(randCause < 0.9)
			{failAct = true;}
			else
			{failObs = true; failAct = true; }
		}
		
		//Start generating data...
		
		//Temperature
		ls.addData("Livingroom", "hasTemperature", Double.toString(Math.random()*10+15) );
		ls.addData("Bedroom", "hasTemperature", Double.toString(Math.random()*10+15) );
		ls.addData("Kitchen", "hasTemperature", Double.toString(Math.random()*10+15) );
		
		//Activity
		//Sleeping, working, cooking, reading, eating
		double randAct = Math.random();
		double randRoom = Math.random();
		
		if(failAct)
		{
			//Sleeping (always fail)
			if(randAct < 0.66)
			{
				ls.addData("Katleen", "isDoing", "Sleeping");
				//Bedroom
				if(randRoom < 0.5)
				{ ls.addData("Katleen", "isLocatedIn", "Bedroom"); }
				//livingroom
				else
				{ ls.addData("Katleen", "isLocatedIn", "Livingroom"); }
			}
			else
			{
				ls.addData("Katleen", "isDoing", "Working");
				ls.addData("Katleen", "isLocatedIn", "Bedroom");
			}
		}
		else
		{
			//Working
			 if(randAct < 0.25)
			{
				ls.addData("Katleen", "isDoing", "Working");
				//The robot giving is ok if user work in livingroom
				ls.addData("Katleen", "isLocatedIn", "Livingroom"); 
			}
			//cooking
			else if(randAct < 0.5)
			{
				ls.addData("Katleen", "isDoing", "Cooking");
				ls.addData("Katleen", "isLocatedIn", "Kitchen");
			}
			//eating
			else if(randAct < 0.75)
			{
				ls.addData("Katleen", "isDoing", "Eating");
				//Bedroom
				if(randRoom < 0.33)
				{ ls.addData("Katleen", "isLocatedIn", "Bedroom"); }
				//Kitchen
				else if(randRoom < 0.33*2)
				{ ls.addData("Katleen", "isLocatedIn", "Kitchen"); }
				//livingroom
				else
				{ ls.addData("Katleen", "isLocatedIn", "Livingroom"); }
			}
			//Default: reading
			else
			{
				ls.addData("Katleen", "isDoing", "Reading");
				//Bedroom
				if(randRoom < 0.33)
				{ ls.addData("Katleen", "isLocatedIn", "Bedroom"); }
				//Kitchen
				else if(randRoom < 0.33*2)
				{ ls.addData("Katleen", "isLocatedIn", "Kitchen"); }
				//livingroom
				else
				{ ls.addData("Katleen", "isLocatedIn", "Livingroom"); }
			}		
		}
		
		//Object position
		//Relative position is in cm (x y, y is depth and x is left/right)
		// if two object have x close and the other has y < -> in front
		// if x and y close -> close to
		//Todo multiple object ?
		
		//Add an obstacle
		if(failObs)
		{			
			ls.addData("bed", "isLocatedIn", "Bedroom");
			ls.addRawData("bedPos", "x", "150");
			ls.addRawData("bedPos", "y", "200");
			
			ls.addData("chair1", "isLocatedIn", "Bedroom");
			//ls.addData("chair1", "hasPosition", "posChair1");
			ls.addRawData("posChair1", "x", Integer.toString((int)(Math.random()*50+125)) );
			ls.addRawData("posChair1", "y", Integer.toString((int)(Math.random()*180)) );
		}
		//Add object, but no obstacle
		else
		{
			ls.addData("bed", "isLocatedIn", "Bedroom");
			//ls.addData("bed", "hasPosition", "bedPos");
			ls.addRawData("bedPos", "x", "150");
			ls.addRawData("bedPos", "y", "200");
			
			ls.addData("chair1", "isLocatedIn", "Bedroom"); 
			//ls.addData("chair1", "hasPosition", "chairPos");
			ls.addRawData("chairPos", "x", Integer.toString((int)(Math.random()*150+200)) ); //Chair not in front of bed
			ls.addRawData("chairPos", "y", Integer.toString((int)(Math.random()*300)) );
		}
		
		//Other stuff
		ls.addData("BottleWater1", "isLocatedIn", "Livingroom");
		ls.addData("BottleFanta", "isLocatedIn", "Livingroom");
		ls.addData("table1", "isLocatedIn", "Livingroom");
		ls.addData("table1", "contains", "BottleWater1");
		ls.addData("table1", "contains", "BottleFanta");
		
		//ls.addData("BottleWater1", "hasPosition", "bot1pos");
		ls.addRawData("bot1pos", "x", Integer.toString((int)(Math.random()*200))); //random position on table (2*2)
		ls.addRawData("bot1pos", "y", Integer.toString((int)(Math.random()*200)));
		
		//ls.addData("BottleFanta", "hasPosition", "bot2pos");
		ls.addRawData("bot2pos", "x", Integer.toString((int)(Math.random()*200))); 
		ls.addRawData("bot2pos", "y", Integer.toString((int)(Math.random()*200)));
		
		return success;
	}
	
	/**
	 * Answer to questions:
	 * TODO better reward
	 */
	public static HashMap<ContextData, Double> getFeedbacks(ArrayList<ContextData> toCheck)
	{
		
		System.out.println("Asking about causes: "+toCheck);
		
		HashMap<ContextData, Double> reward = new HashMap<ContextData, Double>();
		
		if(toCheck == null)
		{ return reward; }
		
		//Causes
		//1
		ContextData sleeping = new ContextData("katleen", "isDoing", "sleeping");
		//2
		ContextData workingAct = new ContextData("katleen", "isDoing", "working");
		ContextData workingLoc = new ContextData("katleen", "isLocatedIn", "bed1");
		//3
		ContextData obstacle = new ContextData("chair1", "inFrontOf", "bed"); 
		
		for(ContextData causeToCheck: toCheck)
		{
			if(causeToCheck.equals(sleeping))
			{
				reward.put(causeToCheck, 1.0);
			}
			else if(causeToCheck.equals(workingAct) || causeToCheck.equals(workingLoc))
			{
				reward.put(causeToCheck, 0.5);
			}
			else if(causeToCheck.equals(obstacle))
			{
				reward.put(causeToCheck, 1.0);
			}
			else
			{
				reward.put(causeToCheck, 0.0);
			}
		}
		
		return reward;
	}
}
