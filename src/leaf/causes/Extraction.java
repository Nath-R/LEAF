package leaf.causes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import leaf.context.ContextData;
import leaf.ontology.Ontology;
import leaf.tools.DataBaseManager;
import leaf.tools.LeafLog;

/**
 * This class carries the methods and processes in charge of extracting the failing causes from the history.
 * It determines what are the date to check and what to ask to the user using a multi-armed bandit like approach.
 * In this case, a 'resource' is a data, exploiting consist in confirming already tested cause while exploring consist in 
 * asking new possible cause to user.
 * Note that only the data from the last failure can be asked (as we suppose the robot will ask about this one)
 * According to user feedback, the belief of causes are adjusted and a reward value is estimated: if the robot asked
 * data that are actually causes, the score is improved.
 * The objective is to maximize the reward (Getting closer to a proper understanding of the failure, thus easy to explain) 
 * considering a large set of data to explore.
 * Asking user has a cost (you can't ask user everything !) thus determining what data to ask is essential.
 * Data to ask are exclusively taken from the last failing situation (the robot asks about the cause of the last failure)
 * 
 * @author Nathan Ramoly
 * 
 */
public class Extraction {

	
	/** Attributes **/
	
	/**
	 * Minimum number of failing situation required for the extraction to be possible.
	 */
	private static final int MINFAILSIT = 2;
	
	/**
	 * Number of question ask to user
	 */
	public static final int NUMQUESTUSER = 3;
	
	/**
	 * Number of situations to check to assess risk (efficiency)
	 */
	public static final int NUMPREVSITRISK = 10;
	
	
	/** Method **/
	
	
	/**
	 * Extraction and computation of data to ask to user
	 * @param task The task whose failure is being investigated
	 */
	public static void extractDataToAsk(String task)
	{
		LeafLog.m("Extraction", "Starting causes question selection");
		
		//Step 0 - init stuff
		
		DataBaseManager dbm = DataBaseManager.getInstance();
		
		//Ensuring we have enough failing ontology for this task
		if(dbm.getNbrFailSituation(task) < MINFAILSIT)
		{
			//Aborting
			LeafLog.w("Extraction", "Not enough failing situations yet !");
			return;
		}
		//Getting the path to last failing ontology
		String path = dbm.getLastFailSitOntoPath(task);
		LeafLog.i("Extraction", "Selected file: "+path);
		
		//Load ontology
		Ontology lastFailOnto = new Ontology(path);		
		
		
		//Step 1 - extract context data from the last failure of this task.
		
		ArrayList<ContextData> lastCD = lastFailOnto.getContextData();
		
		//Debug
		for(ContextData cd: lastCD)
		{
			//System.out.println(cd);
			LeafLog.d("Extraction", cd.toString());
		}		
		
		//Step 2 - Extract context data with belief and those without
		//Note: without belief = cold start
		ArrayList<ContextData> cdBelief = new ArrayList<ContextData>();
		ArrayList<ContextData> cdNoBelief = new ArrayList<ContextData>();
		
		getCDBelief(task, dbm, lastCD, cdBelief, cdNoBelief);
		
		LeafLog.d("Extraction", "Causes observed again:");
		LeafLog.d("Extraction", cdBelief.toString());
		LeafLog.d("Extraction", "New context data:");
		LeafLog.d("Extraction", cdNoBelief.toString());

		
		
		//Step 3 - For those that are unknown, order them by reccurence and absence in succesful situation
		//Score for each: +1 for failing situation, -1 for succesful. Thus, 0 is neutral. (score != belief)
		//The score only allows to select what are the most probable failing data.
		ArrayList<ContextData> successCdHistory = getObservedCDHistory(task, dbm, true);
		ArrayList<ContextData> failCdHistory = getObservedCDHistory(task, dbm, false);
		
		LeafLog.d("Extraction", "CD in success history:");
		LeafLog.d("Extraction", successCdHistory.toString());
		LeafLog.d("Extraction", "CD in fail history:");
		LeafLog.d("Extraction", failCdHistory.toString());
		
		HashMap<ContextData, Integer> scores =  computeScore(cdNoBelief, successCdHistory, failCdHistory);
		
		LeafLog.d("Extraction", "Scores:");
		LeafLog.d("Extraction", scores.toString());
		
		//Step 4 - Strategy for exploiting/exploring
		//Exploiting: it should return a good feedback from user
		//Exploring: it may return a good feedback from user, we need to know for next time
		
		Integer nbrSFail = dbm.getNbrFailSituation(task);
		Double risk = assessRisk(task, dbm);
		LeafLog.d("Extraction", "nbrSitFail: "+nbrSFail+"   risk: "+risk);
		
		ArrayList<ContextData> dataToAsk = RUCB.selection(risk, cdBelief, cdNoBelief, scores, NUMQUESTUSER, nbrSFail);
		
		LeafLog.i("Extraction", "Selected context data to check:");
		LeafLog.i("Extraction", dataToAsk.toString());
		
		
		//Step 5 - Return the data to ask the user
		//TODO
		
		
		//AFTERMARTH Step 6 - wait for feedback and update belief accordingly.
	}
	

	private static void getCDBelief(String task, DataBaseManager dbm, ArrayList<ContextData> cds, ArrayList<ContextData> retListwb, ArrayList<ContextData> retListwob)
	{
		//Get all stored causes
		ArrayList<ContextData> causes = dbm.getCD(task);
		
		//Go through all causes and current data (possible cause)
		//Return those who are in cds and that were observed previously
		for(ContextData cd: cds)
		{
			boolean prevObs = false;
			
			//Go through observed cause and check if the current context data was observed
			for(ContextData cause: causes)
			{
				if(cause.equals(cd))
				{ 
					prevObs = true; //Found cause
					//Add observed cause (thus with belief) in list of cd with belief
					retListwb.add(cause);
					break;
				}				
			}	
			
			//If not observed, add to list of cd with no belief
			if(!prevObs)
			{ retListwob.add(cd); }
		}		
	}
	
	
	private static ArrayList<ContextData> getObservedCDHistory(String task, DataBaseManager dbm, boolean success)
	{
		ArrayList<ContextData> cdHistorySuccess = new ArrayList<ContextData>();
		
		ArrayList<String> paths = dbm.getOntoPaths(task, success);
		
		LeafLog.i("Extraction", "Reading all context in history for task "+task+". High number of disk IO !");
		
		//Go through all onto and get observed task
		//A same context data can be observed and stored multiple times in the list
		//The occurrence of a given cd will then be counted
		//High amount of disk access !
		for(String path: paths)
		{
			LeafLog.d("Extraction", "Loading ontology: "+path);
			
			//Loading ontology
			Ontology onto = new Ontology(path);
			
			cdHistorySuccess.addAll( onto.getContextData() );
		}
		
		return cdHistorySuccess;
	}
	
	/**
	 * 
	 * @param ucd unknown context data (possible cause)
	 * @param fhcd fail history cd
	 * @param shcd success history cd
	 */
	private static HashMap<ContextData, Integer> computeScore(ArrayList<ContextData> ucd, ArrayList<ContextData> shcd, ArrayList<ContextData> fhcd)
	{
		HashMap<ContextData, Integer> scores = new HashMap<ContextData, Integer>();
		
		//For each currently observed unknown context data...
		for(ContextData cd: ucd)
		{
			Integer score = 0;
			//count occurence in failure and success history
			for(ContextData fcd: fhcd)
			{
				if(cd.equals(fcd))
				{score++;}
			}
			for(ContextData scd: shcd)
			{
				if(cd.equals(scd))
				{score--;}
			}
			
			scores.put(cd, score);
		}
		
		return scores;
	}
	
	
	/**
	 * Evaluate the 'risk'
	 * In that case risk mean the importance of having a good guessing
	 * If we fail a lot (risk is high), it is important to valid the already knowed (and observed in current context)
	 * causes to the user.
	 * We can see the risk as the user being mad or by having 'risky' learnt causes (lot of failure)
	 * The risk is assessed based on the number of previous failure.
	 * @return
	 */
	private static Double assessRisk(String task, DataBaseManager dbm) {
		return ((double)dbm.getNbrFailLastNSituation(task, NUMPREVSITRISK)) / ((double)NUMPREVSITRISK);
	}
	
	
	
	
	/**
	 * Get the feedback (reward) from the user (or operator, or expert, or anything)
	 * It updates the belief of context data in database according to formula:  newB = (olbB*N + reward)/(N+1)   (and N=N+1 after aplying this)
	 * @param rewars Hashmap of context data with reward for each. The reward is between 0 and 1. 1 is a good reward (good cause id) 0 is not (bad cause id)
	 */
	public static void getUserFeedback(String task, HashMap<ContextData, Double> rewards)
	{
		LeafLog.m("Feedback", "Getting user feedback");
		
		DataBaseManager dbm = DataBaseManager.getInstance();
		
		//Load all stored context data for this task
		ArrayList<ContextData> storedCds = dbm.getCD(task);
		LeafLog.d("Feedback", storedCds.toString());
				
		for (Map.Entry<ContextData, Double> cdRew : rewards.entrySet()) 
		{
			ContextData cd = cdRew.getKey();
		    Double reward = cdRew.getValue();
		    LeafLog.d("Feedback", "Checking feedback for "+cd);		    
		    
		    //If known, update cd
		    if(storedCds.contains(cd))
		    {
		    	ContextData oldCd = storedCds.get(storedCds.indexOf(cd));
		    	//Computing new belief:
		    	Double newB = (oldCd.getBelief()*oldCd.getNbrFeedback() + reward)/(oldCd.getNbrFeedback()+1.0);
		    	
		    	cd.setBelief(newB);
		    	cd.setNbrFeedback(oldCd.getNbrFeedback()+1);
		    	
		    	//Update
		    	LeafLog.i("Feedback", "Updating "+cd+" belief in database");
		    	dbm.updateContextData(task, cd);
		    }
		    //Insert new entry
		    else
		    {
		    	//Insert only if entry is a possible failure !
		    	if(reward > 0)
		    	{
			    	cd.setBelief(reward);
			    	cd.setNbrFeedback(1);
			    	LeafLog.i("Feedback", "Adding "+cd+" belief to database");
			    	dbm.insertContextData(task, cd);
		    	}
		    	else
		    	{
		    		LeafLog.i("Feedback", cd + "is not a failure cause (negative feedback from user)");
		    	}
		    } 	
		}
	}
	
}
