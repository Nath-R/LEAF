package leaf.causes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import leaf.tools.LeafLog;
import leaf.tools.ProbabiliNode;
import leaf.context.ContextData;
import leaf.ontology.Ontology;
import leaf.tools.DataBaseManager;

/**
 * Representation of a causality graph for one task.
 * It is constructed by analyzing the history based on probability trees.
 * (It uses the current situation to intervene on the probability trees)
 * It represents the causality relation between context data.
 * It is used for identifying the failing causes for the one task. 
 * 
 * @author nathan
 *
 */
public class CausalGraph {

	
	//** Attributes **//
	
	/**
	 * Task related to this causal graph
	 */
	String task;
	
	/**
	 * The graph itself as a set of relation between vertices associated to the weight of the 
	 */
	HashMap<ContextDataPair, Double> graph;
	
	/**
	 * Number of situation to consider in H
	 */
	static final int NSIT = 10;
	
	/**
	 * Oriented grapg mode or not
	 * TODO as parameter
	 */
	static final boolean ORIENTED = true;
	
	//** Methods **//
	
	public CausalGraph(String task)
	{
		this.task = task;
		this.graph = new HashMap<ContextDataPair, Double>();
	}
	
	/**
	 * Construct the causal graph from the history for the given task
	 */
	public void construct()
	{
		//Go through history and compute each pair occurrence
		
		//Count for each context data
		HashMap<ContextData, Integer> cdCount = new HashMap<ContextData, Integer>();
		
		//Count for pair
		HashMap<ContextDataPair, Integer> pairCount = new HashMap<ContextDataPair, Integer>();
		
		DataBaseManager dbm = DataBaseManager.getInstance();
		ArrayList<String> succOntPath = dbm.getOntoPaths(task, true);
		ArrayList<String> failOntPath = dbm.getOntoPaths(task, false);
		ArrayList<String> allPath = new ArrayList<String>();
		
		allPath.addAll(succOntPath);
		allPath.addAll(failOntPath);
		
		allPath = new ArrayList<String>(allPath.subList( Math.max(0, allPath.size() -1 -NSIT), allPath.size() - 1));
		
		Integer totNumInst = allPath.size();
		
		for(String path: allPath)
		{
			//Extract context data
			Ontology curOnt = new Ontology(path);
			
			ArrayList<ContextData> curCds= curOnt.getContextData();		
			
			//Adding the result of the task as a context data
			boolean succ = succOntPath.contains(path);
			ContextData res;
			if(succ)
			{ res = new ContextData(task, "outcome", "success"); }
			else
			{ res = new ContextData(task, "outcome", "failure"); }
			
			curCds.add(res);
			
			//Copy of list for n² loop
			//ArrayList<ContextData> curCdsB = new ArrayList<ContextData>(curCds);
			
			
			//Go through each context data and update pair
			for(int i=0; i<curCds.size(); i++)
			{
				ContextData cd = curCds.get(i);
				
				if(cdCount.containsKey(cd))
				{ cdCount.put(cd, cdCount.get(cd)+1); }
				else
				{ cdCount.put(cd, 1);}
				
				//Generating the pair
				for(int j=i; j<curCds.size(); j++)
				{
					ContextData cdb = curCds.get(j);
					
					if(cd != cdb)
					{
						ContextDataPair cdp = new ContextDataPair(cd, cdb);
						
						if(pairCount.containsKey(cdp))
						{ pairCount.put(cdp, pairCount.get(cdp)+1); }
						else
						{ pairCount.put(cdp, 1); }
					}
				}
			}			
		}
		
		//Generate the trees
		//Go through all pair of context data
		//For all pair, assess the direction of causality
		for(Entry<ContextDataPair, Integer> pair: pairCount.entrySet())
		{
			ContextData A = pair.getKey().A;
			ContextData B = pair.getKey().B;
			Integer nbrOccAB = pair.getValue();
			Integer nbrA = cdCount.get(A);
			Integer nbrB = cdCount.get(B);
			
			//Interventions
			boolean intA = false;
			boolean intB = false;
			
			//Intervening on currently observed data
			String lastPath = dbm.getLastFailSitOntoPath(task);
			Ontology lastFailOnto = new Ontology(lastPath);	
			ArrayList<ContextData> lastCD = lastFailOnto.getContextData();
			
			//Selecting observed data in last failure situation for intervention
			//Note that by this way, task failure is never intervened on
			if(lastCD.contains(A))
			{ intA = true; }
			else if(lastCD.contains(B))
			{ intB = true; }
			
			//LeafLog.m("CausalGraph", "nbr: tot="+totNumInst+" AB="+nbrOccAB+" A="+nbrA+" B="+nbrB);
			
			//Building the tree...
			ProbabiliNode treeRoot = new ProbabiliNode();
			
			//A->B hypothesis
			ProbabiliNode hl = new ProbabiliNode("H", "h", 0.5);
			
			//P(A) && P(nA)
			double pa = ((double)nbrA)/((double)totNumInst);
			if(intA)
			{ pa = 1.0; }
			ProbabiliNode al = new ProbabiliNode(A.getEntity()+" "+A.getProperty(), A.getValue(), pa );
			ProbabiliNode ar = new ProbabiliNode(A.getEntity()+" "+A.getProperty(), "non "+A.getValue(), 1.0 - pa );
			
			//P(B|A) && P(nB|A)
			double pba = 0.5; //default
			if(nbrA != 0)
			{ pba = ((double)nbrOccAB)/((double)nbrA); }
			if(intB)
			{ pba = 1.0; }
			
			al.addChild(B.getEntity()+" "+B.getProperty(), B.getValue(),  pba );
			al.addChild(B.getEntity()+" "+B.getProperty(), "non "+B.getValue(), 1.0 - pba );
			
			//P(B|nA) && P(nB|nA)
			double pbna = 0.5; //default
			if(totNumInst - nbrA != 0)
			{ pbna = ((double)nbrB - nbrOccAB)/((double)totNumInst - nbrA); }
			if(intB)
			{ pbna = 1.0; }
			
			ar.addChild(B.getEntity()+" "+B.getProperty(), B.getValue(), pbna );
			ar.addChild(B.getEntity()+" "+B.getProperty(), "non "+B.getValue(), 1.0 - pbna );
			
			hl.addChild(al);
			hl.addChild(ar);
			
			
			//B->A hypothesis
			ProbabiliNode hr = new ProbabiliNode("H", "nh", 0.5);
			
			//P(B) && P(nB)
			double pb = ((double)nbrB)/((double)totNumInst);
			if(intB)
			{ pb = 1.0; }
			ProbabiliNode bl = new ProbabiliNode(B.getEntity()+" "+B.getProperty(), B.getValue(),  pb);
			ProbabiliNode br = new ProbabiliNode(B.getEntity()+" "+B.getProperty(), "non "+B.getValue(), 1.0 - pb);
			
			//P(A|B) && P(nA|B)
			double pab = 0.5; //default
			if(nbrB != 0)
			{ pab = ((double)nbrOccAB)/((double)nbrB); }
			if(intA)
			{ pab = 1.0; }
			bl.addChild(A.getEntity()+" "+A.getProperty(), A.getValue(), pab );
			bl.addChild(A.getEntity()+" "+A.getProperty(), "non "+A.getValue(), 1.0 - pab );
			
			//P(A|nB) && P(nA|nB)
			double panb = 0.5; //default
			if(totNumInst - nbrB != 0)
			{ panb = ((double)nbrA - nbrOccAB)/((double)totNumInst - nbrB); }
			if(intA)
			{ pab = 1.0; }
			br.addChild(A.getEntity()+" "+A.getProperty(), A.getValue(), panb );
			br.addChild(A.getEntity()+" "+A.getProperty(), "non "+A.getValue(), 1.0 - panb );
			
			hr.addChild(bl);
			hr.addChild(br);
			
			
			treeRoot.addChild(hl);
			treeRoot.addChild(hr);
			
			//LeafLog.m("CausalGraph", treeRoot.toString());
			
			//Evaluate tree
			if(treeRoot.isConsistent(true))
			{
				//Computing P(H|A,B)
				//P(B|A,H)
				HashMap<String, String> querPbah = new HashMap<String, String>();
				querPbah.put("H", "h");
				querPbah.put(A.getEntity()+" "+A.getProperty(), A.getValue());
				querPbah.put(B.getEntity()+" "+B.getProperty(), B.getValue());				
				double pbah = treeRoot.evaluate(querPbah);
				//P(A|H)
				HashMap<String, String> querPah = new HashMap<String, String>();
				querPah.put("H", "h");
				querPah.put(A.getEntity()+" "+A.getProperty(), A.getValue());
				double pah = treeRoot.evaluate(querPah);
				//P(H)
				HashMap<String, String> querPh = new HashMap<String, String>();
				querPh.put("H", "h");
				double ph = treeRoot.evaluate(querPh);
				
				//P(A|nH,B)
				HashMap<String, String> querPbanh = new HashMap<String, String>();
				querPbanh.put("H", "nh");
				querPbanh.put(B.getEntity()+" "+B.getProperty(), B.getValue());	
				querPbanh.put(A.getEntity()+" "+A.getProperty(), A.getValue());			
				double pbanh = treeRoot.evaluate(querPbanh);
				//P(B|nH)
				HashMap<String, String> querPbnh = new HashMap<String, String>();
				querPbnh.put("H", "nh");
				querPbnh.put(B.getEntity()+" "+B.getProperty(), B.getValue());			
				double pbnh = treeRoot.evaluate(querPbnh);
				//P(nH)
				HashMap<String, String> querPnh = new HashMap<String, String>();
				querPnh.put("H", "nh");
				double pnh = treeRoot.evaluate(querPnh);
				
				double phab = (pbah*pah*ph) / ((pbah*pah*ph)+(pbanh*pbnh*pnh));
				LeafLog.i("CausalGraph", "Tree unabalancing for "+A+" and "+B+" = "+phab);
				
				//No cause...
				if(phab == ph)
				{}
				//Balanced toward H: A->B
				else if(!ORIENTED)
				{
					graph.put(new ContextDataPair(A, B, true), Math.max(phab, 1.0-phab) );	
				}
				else if(phab > ph)
				{
					graph.put(new ContextDataPair(A, B, false), phab);					
				}
				//Balanced toward nH: B->A
				else if(phab < ph)
				{
					graph.put(new ContextDataPair(B, A, false), 1.0-phab);
				}
			}
			else
			{
				LeafLog.e("CausalGraph", "Probability Tree not properly balanced ! Cannot compute causal relation betwen "+A+" and "+B);
			}
		}
		
	}
	
	/**
	 * Provide a list of causes for a given context data ordered as:
	 * 1- The closer comes first
	 * 2- The highest weight comes first
	 */
	public ArrayList<ContextData> listCauses (ContextData caused)
	{
		// The list itself that will be filled
		ArrayList<ContextData> ordCauseList = new ArrayList<ContextData>();
		//Parallel list of weight for ordering
		ArrayList<Double> ordWeightList = new ArrayList<Double>();
		
		//Fifo containing the next nodes to check (element add at the end)
		LinkedList<ContextData> fifoCause = new LinkedList<ContextData>();		
		//Parallel fifo containing the value of edgeS (value of path) going toward the node carried in previous fifo
		LinkedList<Double> fifoWeight = new LinkedList<Double>();	
		
		fifoCause.add(caused);
		fifoWeight.add(1.0);
		
		//Main loop, get the element out the fifo and get its causes and add them to the list and the fifo for further exploration
		
		while(!fifoCause.isEmpty())
		{
			ContextData target = fifoCause.removeFirst();
			Double tarWeight = fifoWeight.removeFirst();
			
			for(Entry<ContextDataPair, Double> node: graph.entrySet())
			{
				ContextDataPair vertices = node.getKey();
				Double w = node.getValue();
				
				//If B is target, then A cause target, add A to list and fifo (A not already found and not the caused)
				if( vertices.B.equals(target) && !ordCauseList.contains(vertices.A) && !caused.equals(vertices.A)  )
				{
					int insertPos = 0;
					//Add orderly, highest weight first
					for(int i=0; i<ordCauseList.size(); i++)
					{
						if(w*tarWeight > ordWeightList.get(i))
						{ insertPos=i; break;}
					}
					ordCauseList.add(insertPos, vertices.A);
					ordWeightList.add(insertPos, w*tarWeight);
					
					fifoCause.add(vertices.A);
					fifoWeight.add(w*tarWeight);
				}
				else if ( !ORIENTED &&  vertices.A.equals(target) && !ordCauseList.contains(vertices.B) && !caused.equals(vertices.B) )
				{
					int insertPos = 0;
					//Add orderly, highest weight first
					for(int i=0; i<ordCauseList.size(); i++)
					{
						if(w*tarWeight > ordWeightList.get(i))
						{ insertPos=i; break;}
					}
					ordCauseList.add(insertPos, vertices.B);
					ordWeightList.add(insertPos, w*tarWeight);
					
					fifoCause.add(vertices.B);
					fifoWeight.add(w*tarWeight);
				}	
				
			}
		}
		
		return ordCauseList;
	}
	
	
}
