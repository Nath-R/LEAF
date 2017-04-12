package leaf.exec.simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import leaf.causes.Extraction;
import leaf.context.ContextData;
import leaf.context.LiveSituation;

/**
 * Simulation process.
 * Generates N number of labeled situation and provide them to the system.
 * It records the results of each methods
 * @author Nathan Ramoly
 *
 */
public class Simulation {

	/**
	 * Number of situation for testing
	 */
	public static final int NBRSIT = 100;
	
	/**
	 * Threshold defining when a task is too risky to execute
	 */
	public static final double THRESHRISK = 0.4;
	
	
	public static void main(String[] args) {
		
		System.out.println("Starting simulation");
		
		//Live context
		LiveSituation ls = new LiveSituation();
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<Double> confs = new ArrayList<Double>();
		
		for(int i=0; i<NBRSIT; i++)
		{
			//Generate situation
			Boolean taskGiveOk = LiveSituationRandomFactory.genScenarGive(ls);
			
			//Select task with system
			Double confFailTaskGive = ls.getTaskCurrentRisk("DeliverObject");
			//Double confFailTaskGive = ls.getCurrentTaskRiskStat("DeliverObject");
			
			confs.add(confFailTaskGive);
			
			//Compare results and record
			
			
			//Giving object task would have failed, but risk detected (True Positive)
			if(!taskGiveOk && confFailTaskGive >= THRESHRISK)
			{
				ls.save("putObject");
				results.add("TP");
			}
			//Selected task giving object... but fails (False Negative)
			//The executed task fails
			else if (!taskGiveOk && confFailTaskGive < THRESHRISK)
			{
				ls.save("DeliverObject");
				ls.lastTaskFailed();
				results.add("FN");
				
				//Learning...
				ArrayList<ContextData> toCheck = Extraction.extractDataToAsk("DeliverObject");
				HashMap<ContextData, Double> rewards = LiveSituationRandomFactory.getFeedbacks(toCheck); //expert knowledge
				Extraction.getUserFeedback("DeliverObject", rewards);
			}
			//Task is fine, but prevented anyway (False Positive)
			//The second task succeed
			else if(taskGiveOk && confFailTaskGive >= THRESHRISK)
			{
				ls.save("DeliverObject"); //Save deliver object as it was suppose to be runnable
				results.add("FP");
			}
			//No problem and no correction (True negative)
			//Task went fine 
			else if(taskGiveOk && confFailTaskGive < THRESHRISK)
			{
				ls.save("DeliverObject");
				results.add("TN");
			}
			
			
		}
		
		System.out.println("\n\n\n");
		System.out.println(results);
		
		//Compute results
		//Compute correctness for each test instance
		ArrayList<Double> correctness = new ArrayList<Double>();
		int sizeWin = 20; //20 elements to compute
		for(int i=0; i<results.size(); i++)
		{
			double cntTot = 0;
			double cntTrue = 0;
			
			for(int j=Math.max(i-sizeWin, 0); j<=i; j++)
			{
				cntTot++;
				
				if(results.get(j).equals("TN") || results.get(j).equals("TP"))
				{ cntTrue++; }
			}
			
			correctness.add(cntTrue/cntTot);
		}
		System.out.println(confs);
		System.out.println(correctness);
		
		//Write result to file:
		try {
			FileWriter fw = new FileWriter(new File("result.csv"), true );	
			BufferedWriter bw = new BufferedWriter(fw);
			String message = "";
			
			for(Double cor: correctness)
			{
				message+=cor+"; ";
			}		
		
			bw.write(message+"\n");
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
