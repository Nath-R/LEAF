package leaf.exec.main;


import java.util.HashMap;

import leaf.causes.CausalGraph;
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
		
		//createSituations();
		
		//testCausalGraph();
		
		//testExtraction();
		
		//testFeedback();
		
		//testRiskEval();
		
		testLiveSituationServer();
	
	}
	
	private static void testLiveSituationServer()
	{
		LiveSituation ls = new LiveSituation();
		
		ls.run();
	}
	
	private static void createSituations()
	{
		LiveSituation ls = new LiveSituation();
		
		ls.addData("Katleen", "isDoing", "Cooking");
		ls.addData("Katleen", "isLocatedIn", "Livingroom");
		//ls.addData("BottleWater1", "inFrontOf", "BottleFanta");		
		//ls.addRawData("bot1", "hasPosition", "posbot1");
		ls.addData("BottleWater1", "isLocatedIn", "Livingroom");
		ls.addData("BottleFanta", "isLocatedIn", "Livingroom");
		ls.addRawData("bot1pos", "x", "25" );
		ls.addRawData("bot1pos", "y", "200" );
		//ls.addRawData("bot2", "hasPosition", "posbot2");
		ls.addRawData("bot2pos", "x", Integer.toString((int)(Math.random()*50)) );
		ls.addRawData("bot2pos", "y", Integer.toString((int)(Math.random()*150)) );
		
		ls.save("doStuff");		
		ls.lastTaskFailed();
		
		ls.reset();
		
		ls.addData("Katleen", "isDoing", "Cooking");
		ls.addData("Katleen", "isLocatedIn", "Livingroom");
		//ls.addData("BottleWater1", "inFrontOf", "BottleFanta");	
		//ls.addRawData("bot1", "hasPosition", "posbot1");
		ls.addData("BottleWater1", "isLocatedIn", "Livingroom");
		ls.addData("BottleFanta", "isLocatedIn", "Livingroom");
		ls.addRawData("bot1pos", "x", "25" );
		ls.addRawData("bot1pos", "y", "200" );
		//ls.addRawData("bot2", "hasPosition", "posbot2");
		ls.addRawData("bot2pos", "x", Integer.toString((int)(Math.random()*50)) );
		ls.addRawData("bot2pos", "y", Integer.toString((int)(Math.random()*150)) );
		
		ls.save("doStuff");		
		ls.lastTaskFailed();
		
		ls.reset();
		
		ls.addData("Katleen", "isDoing", "Eating");
		ls.addData("Katleen", "isLocatedIn", "Livingroom");
		//ls.addData("BottleWater1", "inFrontOf", "BottleFanta");
		//ls.addRawData("bot1", "hasPosition", "posbot1");
		ls.addData("BottleWater1", "isLocatedIn", "Livingroom");
		ls.addData("BottleFanta", "isLocatedIn", "Livingroom");
		ls.addRawData("bot1pos", "x", "25" );
		ls.addRawData("bot1pos", "y", "200" );
		//ls.addRawData("bot2", "hasPosition", "posbot2");
		ls.addRawData("bot2pos", "x", "25" );
		ls.addRawData("bot2pos", "y", "0");
		
		ls.save("doStuff");		
		//ls.lastTaskFailed();
		
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
		
		//ls.getTaskCurrentRisk("doStuff");
		System.out.println( ls.getCurrentTaskRiskStat("doStuff"));
	}
	
	private static void testCausalGraph()
	{
		CausalGraph cg = new CausalGraph("doStuff");
		
		cg.construct();
		
		System.out.println( cg.listCauses(new ContextData("doStuff", "outcome", "failure")) );
	}
	
}
