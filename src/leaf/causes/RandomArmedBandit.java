package leaf.causes;

import java.util.ArrayList;
import java.util.HashMap;

import leaf.context.ContextData;

/**
 * Randomly select a cause, without checking for exploration or exploitation
 * @author nathan
 *
 */
public class RandomArmedBandit {
	
	public static ArrayList<ContextData> selection( Double risk, ArrayList<ContextData> oldCauses, ArrayList<ContextData> newCauses, HashMap<ContextData, Integer> scoreNewCauses, Integer N, Integer T)
	{
		ArrayList<ContextData> ret = new ArrayList<ContextData>();
		
		ArrayList<ContextData> allCauses = new	ArrayList<ContextData>();
		
		allCauses.addAll(newCauses);
		allCauses.addAll(oldCauses);
		
		for(int i=0; i<N; i++)
		{
			int randVal = (int)( (Math.random())*allCauses.size() );
			
			ret.add( allCauses.get(i) );
			allCauses.remove(i);
		}
		
		return ret;
	}
}
