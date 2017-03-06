package leaf.ontology;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import leaf.context.ContextData;
import leaf.tools.LeafLog;


/**
 * The Ontology classe allows to manage one single knowledge instance stored as an ontology.
 * This classe asbtracts the use of Jena library for ontology management.
 * It loads and updates a knowledge from a given file.
 * @author Nathan Ramoly
 *
 */
public class Ontology {

	/** Attribute **/
	
	/**
	 * Path to default structural ontology
	 */
	public static final String SOURCE = "res/onto/base.owl";
	
	/**
	 * Path to rule file
	 */
	public static final String RULES = "res/rules/rule.txt";
	
	/**
	 * Prefix for query
	 */
	private static final String PREFIX = "prefix rdf: <" + RDF.getURI() + ">\n" +
            "prefix owl: <" + OWL.getURI() + ">\n"+
            "prefix xsd: <"+ XSD.getURI() +"> \n"+
            "prefix rdfs: <" + RDFS.getURI() + ">\n" +
            "prefix leaf:<" + "file:/C:/Users/ramol_na/Documents/workspace/LEAF/res/onto/res/onto.owl#" + ">\n" ;
	
	/**
	 * Path to the ontology's file
	 * If null, no file selected, can't be saved.
	 */
	private String ontoFile;
	
	/**
	 * Main model that is updated and corrected.
	 */
	private OntModel model;
	
	/**
	 * Model computed after rules application.
	 */
	private InfModel infModel;
	
	
	/** Methods **/
	
	/**
	 * Default constructor that only load the default structural ontology.
	 * It can't save it until a file is provided.
	 */
	public Ontology()
	{
		ontoFile = null;
				
		try {
			model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
			model.read(new FileInputStream(SOURCE),null,"TTL");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//FileManager.get().readModel( model, SOURCE );
	}
	
	/**
	 * Load an ontology from a file
	 */
	public Ontology(String path)
	{
		try {
			model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
			model.read(new FileInputStream(path),null,"TTL");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Export the ontology to the current file (overwrite it)
	 * @throws FileNotFoundException 
	 */
	public void export() throws FileNotFoundException
	{
		if(ontoFile != null)
		{
			export(ontoFile);
		}
		else
		{
			throw new FileNotFoundException("ONTOLOGY | The ontology is not linked to any file !");
		}
	}
	
	/**
	 * Export the ontology to the given file.
	 * The path is not stored in ontoFile.
	 * It is save as turtle.
	 */
	public void export(String path)
	{
		FileWriter out = null;
    	try {   		
			out = new FileWriter( path );
			if(infModel == null)
			{  
				model.write( out, "Turtle" );
			}
			else
			{
				infModel.write( out, "Turtle" );
			}     	 
    	 
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	finally {
    	  if (out != null) {
    	    try {out.close();} catch (IOException ignore) {}
    	  }
    	}
	}
	
    /**
     * Apply rules for activity recognition
     */
    public void applyRules()
    {
    	Reasoner reasoner = new GenericRuleReasoner( Rule.rulesFromURL( RULES ) );
    	
    	infModel = ModelFactory.createInfModel( reasoner, model );
    }
    
    /**
     * Add a triple into the ontology
     */
    public void update( String subject, String predicate, String object)
    {
    	LeafLog.m("Ontology", "Updating context ontology..."); 
    	
    	String query = PREFIX + "insert data {"+subject+" "+predicate+" "+object+"}";
    	
    	LeafLog.d("Ontology", query); 
    	
    	UpdateAction.parseExecute(query, model);
    }
    
    /**
     * Add a triple concerning an entity having the name given in parameter with a property (preucate) and a value
     */
    public void updateEntity(String name, String property, String object)
    {
    	LeafLog.i("Ontology", "Updating context ontology..."); 
        	
       	String query = PREFIX + "insert {?subj leaf:"+property+" ?obj } ";
    	query += "where{ ?subj leaf:hasName '"+name+"' . ?obj leaf:hasName '"+object+"'}";
    	
    	LeafLog.d("Ontology", query); 
    	
    	UpdateAction.parseExecute(query, model);
    	
    }
    
    /**
     * Get all the context info carried by the ontology as a set of triple
     * It returns only triple whose subject has a hasName property: it returns only the actual context data
     */
    public ArrayList<ContextData> getContextData()
    {
    	LeafLog.m(this.getClass().toString(), "Extracting all context data of the ontology");
    	
    	ArrayList<ContextData> ret = new ArrayList<ContextData>();
    	
       	String queryStr = PREFIX + "select distinct ?subj ?pred ?obj ";
    	queryStr += "where{ ?subj leaf:hasName ?a .  ?subj ?pred ?obj ."
    			+ " filter not exists { ?subj rdf:type ?obj } ."
    			+ " filter not exists { ?subj leaf:hasName ?obj }  }"; //Remove this filter if name is to be taken into account
    	
    	 Query query = QueryFactory.create( queryStr );
         QueryExecution qexec = QueryExecutionFactory.create( query, model );
         try {
             ResultSet results = qexec.execSelect();              
             while (results.hasNext()) {
            	 QuerySolution solution = results.next(); 
            	 
            	 //ret.add( new Triple(solution.get("subj").asNode(), solution.get("pred").asNode(), solution.get("obj").asNode() ) );
            	
            	 String subj = solution.get("subj").asResource().getLocalName();
            	 String pred = solution.get("pred").asResource().getLocalName();
            	 String obj;
            	 if(solution.get("obj").isLiteral())
            	 { obj = solution.get("obj").asLiteral().getString(); }
            	 else
            	 { obj = solution.get("obj").asResource().getLocalName();}
            	 
            	 ret.add(new ContextData( subj, pred,  obj) );
             }
         }
         finally {
             qexec.close();
         }
    	
    	return ret;
    }
    
}
