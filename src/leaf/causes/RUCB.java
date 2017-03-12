package leaf.causes;

import java.util.ArrayList;
import java.util.HashMap;

import leaf.context.ContextData;
import leaf.tools.LeafLog;

/**
 * Risk oriented UCB algorithm for solving the multi-armed bandit issue.
 * In this case, the risk represent the number of fails: 
 * if we fail a lot, we were unable to learn properly the last times, thus we have to confirm the previously detected causes
 * (like: are you SURE this is the cause ?!)
 * The algorithm select a set of context data (possible cause) to ask the user about it causaility relation toward the failure.
 * 
 * It should not be risk oriented UCB but efficiency oriented UCB
 * @author Nathan Ramoly
 *
 */
public class RUCB {

	/**
	 * Maximum exploration rate: when everything is 'fine' (low failure), explore a lot.
	 */
	public static final double EMAX = 0.7;
	
	/**
	 * Minimum exploration: if not sure, ask user about reliable causes, as they may not be actually valid
	 */
	public static final double EMIN = 0.4;
	
	/**
	 * Risk threshold
	 */
	public static final double THR = 0.7;
	
	/**
	 * 
	 * @param risk
	 * @param oldCauses
	 * @param newCauses
	 * @param scoreNewCauses
	 * @param N Number of context data to return
	 * @param T Number of total fail situation for this task (use for selecting best context data)
	 * @return
	 */
	public static ArrayList<ContextData> selection( Double risk, ArrayList<ContextData> oldCauses, ArrayList<ContextData> newCauses, HashMap<ContextData, Integer> scoreNewCauses, Integer N, Integer T)
	{
		ArrayList<ContextData> ret = new ArrayList<ContextData>();
		
		//Compute the current E
		Double E = EMAX - risk*(EMAX-EMIN);
		
		if(risk < THR)
		{
			ret = EUCB(E, oldCauses, newCauses, scoreNewCauses, N, T); 
		}
		else if (risk >= THR)
		{
			ret = EUCB(EMIN, oldCauses, newCauses, scoreNewCauses, N, T);
		}			
		
		return ret;
	}
	
	private static ArrayList<ContextData> EUCB( Double E, ArrayList<ContextData> oldCauses, ArrayList<ContextData> newCauses, HashMap<ContextData, Integer> scoreNewCauses, Integer N, Integer T)
	{
		ArrayList<ContextData> ret = new ArrayList<ContextData>();
		
		//Selecting  N data
		for(int i=0; i<N; i++)
		{
			Double q = Math.random();
			boolean noExploitAvailable = false;
			
			if(q>E) //Exploit
			{
				LeafLog.m("RUCB","Exploit for iteration "+i);
				//Go through each old causes and select the with the highest d value
				ContextData curSelcd = null;
				Double curD = 0.0;
				for(ContextData oc: oldCauses)
				{
					Double D = oc.getBelief()*Math.sqrt( Math.log(T)/oc.getNbrFeedback() );
					
					//If max and not already selected
					if(D>=curD && !ret.contains(oc))
					{
						curD = D;
						curSelcd = oc;
					}
				}			
				
				//Adding it to return list
				if(curSelcd != null)
				{ ret.add(curSelcd); }
				//Go to explore and pick a context data here
				else
				{ noExploitAvailable = true; }
			}
			
			if(q<=E || noExploitAvailable) //Explore
			{
				LeafLog.m("RUCB","Explore for iteration "+i);
				
				
				//Select the newly observed data with the highest score
//				ContextData curSelcd = null;
//				Integer maxScore = 0;
//				
//				for(ContextData nc: newCauses)
//				{
//					//If higher score and not already selected...
//					if(scoreNewCauses.get(nc) > maxScore && !ret.contains(nc))
//					{
//						curSelcd = nc;
//						maxScore = scoreNewCauses.get(nc);
//					}
//				}	
//				
//				//Adding to return list
//				if(curSelcd != null)
//				{ ret.add(curSelcd); }
//				else
//				{ LeafLog.i("RUCB","No further context data to be checked in exploration");	}
				
				//Select a random value
				//Create list with score > 0 and not in list
				ArrayList<ContextData> filteredCauses = new ArrayList<ContextData>();
				
				for(ContextData nc: newCauses)
				{
					if(scoreNewCauses.get(nc) > 0.0  && !ret.contains(nc))
					{
						filteredCauses.add(nc);
					}
				}
				
				if(filteredCauses.size() <= 0)
				{LeafLog.i("RUCB","No further context data to be checked in exploration");	}
				else
				{
					int randVal = (int)( (Math.random())*filteredCauses.size());
					if(randVal == filteredCauses.size())
					{randVal--;}
					ret.add(filteredCauses.get(randVal));
				}

			}
		}
		
		LeafLog.m("RUCB","Selected "+ret.size()+" context data to check out of "+N+" requested");
		
		return ret;
	}
}
