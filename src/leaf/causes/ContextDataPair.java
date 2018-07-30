package leaf.causes;

import leaf.context.ContextData;

/**
 * Pair of context data used for counting the number of occurrence of pair in the history.
 * It's use for generating probability tree.
 * @author nathan
 *
 */
public class ContextDataPair {

	//** Attributes **//
	
	public ContextData A;
	public ContextData B;
	
	/**
	 * If reflexive (A,B) = (B,A)
	 * If not (A,B) != (B,A)
	 * Useful for directed graph
	 */
	private boolean reflexive;
	
	
	//** Methods **//
	
	public ContextDataPair(ContextData A, ContextData B)
	{
		this.A = A;
		this.B = B;
		this.reflexive = true;
	}
	
	public ContextDataPair(ContextData A, ContextData B, boolean reflexive)
	{
		this.A = A;
		this.B = B;
		this.reflexive = reflexive;
	}


	@Override
	public int hashCode() {
		final int prime = 42;
		int result = 1;
		result = prime * result + ((A == null) ? 0 : A.hashCode());
		result = prime * result + ((B == null) ? 0 : B.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContextDataPair other = (ContextDataPair) obj;
		
		if( A == null || B == null || other.A == null || other.B == null) //not supposed to be null
			return false;
		
		if(reflexive)
		{
			if(!A.equals(other.A) && !A.equals(other.B))
				return false;
			if(!B.equals(other.A) && !B.equals(other.B))
				return false;
		}
		else
		{
			if( !A.equals(other.A) || !B.equals(other.B) )
				return false;
		}
		
		return true;		
	}
	
	
	
}
