package leaf.context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import leaf.ontology.Ontology;
import leaf.tools.DataBaseManager;
import leaf.tools.LeafLog;

/**
 * Current situation (global context).
 * It is constantly updated.
 * It is actually a UDP server that continuously observe the context and save the situation into history when asked to.
 * @author Nathan Ramoly
 *
 */
public class LiveSituation extends Thread {

	/** Attributes **/
	
	/**
	 * Current knowledge.
	 * It is loaded from the default ontology. (Not loaded from a previous state)
	 */
	Ontology situation;
	
	/**
	 * Database manager
	 */
	DataBaseManager dbm;
	
	
	/** Methods **/
	
	/**
	 * Constructor
	 */
	public LiveSituation()
	{
		situation = new Ontology();
		dbm = DataBaseManager.getInstance();
	}
	
	/**
	 * Main UDP program that wait for context data to arrive
	 */
	@Override
	public void run() {

		
	}
	
	/**
	 * Add data
	 */
	public void addData(String entity, String property, String value)
	{
		situation.updateEntity(entity, property, value);
	}
	
	/**
	 * Add a direct triple (with node id)
	 */
	public void addRawData(String entity, String property, String value)
	{
		situation.updateProperty(entity, property, value);
	}
	
	/**
	 * Store the current situation in the history
	 * @param task The current task during whom the situation was observed
	 */
	public void save(String task)
	{		
		//Save the file
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
		Date date = new Date();
		
		String ontoName = "situation_"+dateFormat.format(date)+".ttl";
		String path = "res/history/" + ontoName;
		
		//Apply rules before saving
		situation.applyRules();
		
		situation.export(path);
		
		//Create the entry in database
		dbm.addSituationToHistory(task, path);		
	}
	
	/**
	 * Update the history when informed the previous task failed
	 */
	public void lastTaskFailed()
	{
		dbm.updateFailLastAction();
	}
	
	/**
	 * Return the risk of failure for a given task in the current context
	 * Risk formula atan(0.1*sB)/(pi/2)
	 * Where sB = sum of belief
	 * TODO find a better solution than sB
	 * 
	 */
	public synchronized Double getTaskCurrentRisk(String task)
	{
		LeafLog.m("Risk eval.", "Starting failing risk assesment for task "+task);
		
		//Apply rules
		situation.applyRules();
		
		//Get all current context data:
		ArrayList<ContextData> currentCd = situation.getContextData();
		//Get all the failure causes
		ArrayList<ContextData> causes = dbm.getCD(task);
		//List of currently observed causes (taken from cause list as they carry belief and nbrFeedback)
		ArrayList<ContextData> obsCauses = new ArrayList<ContextData>();
		
		//Go through all causes
		for(ContextData cause: causes)
		{
			//If cause is observed in current context
			if(currentCd.contains(cause))
			{
				obsCauses.add(cause);
			}
		}
		
		//All caused identify, compute the risk
		Double sumB = 0.0;
		for(ContextData obsCause: obsCauses)
		{
			sumB += obsCause.getBelief();
		}
		
		Double factor = 1.0;
		LeafLog.d("Risk eval.", "risk computation info:  sumB="+sumB+" nbCause="+obsCauses.size());
		Double risk = Math.atan( factor*sumB )/(Math.PI/2.0); //Atan between 0.1
		
		LeafLog.i("Risk eval.", "Task "+task+" has a risk of failure of: "+risk);
		
		return risk;
	}
	
	/**
	 * Reload the default ontology
	 */
	public void reset()
	{
		situation = new Ontology();
	}
}
