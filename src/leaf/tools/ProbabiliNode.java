package leaf.tools;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Leaf of a probabilitree.
 * For non root leaf, it carries a variable, a value for this variable, a probability and a set of child.
 * From a model eye, the probability is the one attached to the edge going to this node.
 * The child node must all have the same variable. The consistency must be ensured after the tree was created.
 * @author nathan
 *
 */
public class ProbabiliNode {

	
	/** Attributes **/
	
	String variable;

	String value;
	
	Double probability;
	
	ArrayList<ProbabiliNode> children;
	
	/** Methods **/
	
	public ProbabiliNode(String variable, String value, Double probability) {
		super();
		this.variable = variable;
		this.value = value;
		this.probability = probability;
		this.children = new ArrayList<ProbabiliNode>();
	}
	
	public ProbabiliNode()
	{
		super();
		this.children = new ArrayList<ProbabiliNode>();
		this.probability = 1.0;
	}
	
	/**
	 * Create and add one child from the parameters.
	 * @param variable Variable of the new node, must be the same as its sibling
	 * @param value Value of the variable of the new node
	 * @param probability Probability of the new node
	 */
	public void addChild(String variable, String value, Double probability)
	{
		children.add( new ProbabiliNode(variable, value, probability) );
	}
	
	/**
	 * Add a node to the children list
	 * @param newChild The node to add
	 */
	public void addChild(ProbabiliNode newChild)
	{
		children.add( newChild );
	}
	

	/**
	 * Ensure if the child are consistent:
	 * - sum of children' probabilities must be equal to 1
	 * - all children must tackle the same variable
	 * 
	 * @param recursive If true, return the consistency of the whole subtree
	 */
	public Boolean isConsistent(Boolean recursive)
	{
		Double sum = 0.0;
		String prevVar = "";
		
		for(ProbabiliNode child: children)
		{
			sum += child.getProbability();
			
			//Check if variable is the same as the previously checked child
			if(!prevVar.equals("") && !prevVar.equals(child.getVariable()) )
			{ return false; }
			
			prevVar = child.getVariable();
		}
		
		//If sum of probability is different than 1
		if(sum != 1.0 && children.size() != 0)
		{ return false; }
		
		//If required, check validity of children
		if(recursive)
		{
			for(ProbabiliNode child: children)
			{
				if(!child.isConsistent(recursive))
				{ return false; }
			}
		}
		
		return true;
	}
	
	/**
	 * Return the probability of the current set of variable 
	 * For example for p(Y=y|X=x,Z=z), should be given to the method from the root {(X,x)(Y,y)(Z,z)}
	 * 
	 * @param variables branch to evaluate
	 * @return The probability of the branch containing given variable, return 0 if the variable could not be found
	 */
	public Double evaluate(HashMap<String,String> variables)
	{
		double ret = probability;
		
		for(ProbabiliNode child: children)
		{
			if(  child.getValue().equals( variables.get(child.getVariable()) )  )
			{
				variables.remove( child.getVariable() );
				ret *= child.evaluate(variables);
				break;
			}
		}
		
		return ret;
	}
	
	/**
	 * Intervene by changing the probability of the node if it carries a particular variable
	 * It changes to 1 if it matches the value and 0 otherwise.
	 * It then recursively go through children.
	 * Ex: Intervention on X with value x observed
	 * For each node having X and value x, their probability is set to 1
	 * For each node having X and value different than x, their probability is set to 0
	 */
	public void intervene(String var, String val)
	{
		if(variable == null)
		{}
		else if(variable.equals(var) && value.equals(val))
		{
			probability = 1.0;
		}
		else if(variable.equals(var) && !value.equals(val))
		{
			probability = 0.0;
		}
		
		for(ProbabiliNode child: children)
		{
			child.intervene(var, val);
		}
	}
	
	public String toString()
	{
		return toString(0);
	}
	
	public String toString(Integer d)
	{
		String ret = "";
		if(variable != null)
		{ 
			for(int i = 0; i<d; i++)
			{ret += " ";}
			ret += Character.toString ((char) 8627) + variable+"="+value+" "+probability+"\n"; 			
		}
		else
		{
			ret += "root \n";
		}
		
		for(ProbabiliNode child: children)
		{
			ret += child.toString(d+1);
		}
		
		return ret;
	}
	
	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Double getProbability() {
		return probability;
	}

	public void setProbability(Double probability) {
		this.probability = probability;
	}
}
