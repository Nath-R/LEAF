package leaf.main;


import java.util.HashMap;

import leaf.causes.Extraction;
import leaf.context.ContextData;
import leaf.context.LiveSituation;

/**
 * Test/Example class to check how things work and and if they do work
 * @author Nathan Ramoly
 *
 */
public class testMain {

	public static void main(String[] args) {
		
		//Comment according to exec
		
		createSituations();
		
		testExtraction();
		
		testFeedback();
		
		testRiskEval();
	
	}
	
	private static void createSituations()
	{
		LiveSituation ls = new LiveSituation();
		
		ls.addData("Katleen", "isDoing", "Cooking");
		ls.addData("Katleen", "isLocatedIn", "Salon");
		//ls.addData("BottleWater1", "inFrontOf", "BottleFanta");		
		ls.save("doStuff");		
		ls.lastTaskFailed();
		
		ls.reset();
		
		ls.addData("Katleen", "isDoing", "Cooking");
		ls.addData("Katleen", "isLocatedIn", "Salon");
		ls.addData("BottleWater1", "inFrontOf", "BottleFanta");		
		ls.save("doStuff");		
		ls.lastTaskFailed();
		
		ls.reset();
		
		ls.addData("Katleen", "isDoing", "Cooking");
		ls.addData("Katleen", "isLocatedIn", "Salon");
		//ls.addData("BottleWater1", "inFrontOf", "BottleFanta");		
		ls.save("doStuff");		
		ls.lastTaskFailed();
		
	}
	
	private static void testExtraction()
	{
		Extraction.extractDataToAsk("doStuff");
	}
	
	private static void testFeedback()
	{
		ContextData cda = new ContextData("katleen", "isDoing", "cooking");
		ContextData cdb = new ContextData("katleen", "isLocatedIn", "lr1");
		HashMap<ContextData, Double> rewards = new HashMap<ContextData, Double>();
		rewards.put(cda, 1.0);
		rewards.put(cdb, 0.0);
		Extraction.getUserFeedback("doStuff", rewards);
	}
	
	private static void testRiskEval()
	{
		LiveSituation ls = new LiveSituation();
		
		ls.addData("Katleen", "isDoing", "Cooking");
		ls.addData("Katleen", "isLocatedIn", "Salon");
		ls.addData("BottleWater1", "inFrontOf", "BottleFanta");
		
		ls.getTaskCurrentRisk("doStuff");
	}
	
}
