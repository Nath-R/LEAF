package leaf.context;

/**
 * A context data is a triple about an observation of the environment.
 * It carries a triple as: name of entity, property, value (or name of target entity)
 * @author Nathan Ramoly
 *
 */
public class ContextData {

	//** Attributes **//
	
	String entity;
	
	String property;
	
	String value;
	
	Double belief;
	
	Integer nbrFeedback;
	
	
	//** Methods **//
	
	public ContextData(String entity, String property, String value)
	{
		this.entity = entity;
		this.property = property;
		this.value = value;
		this.belief = null;
		this.nbrFeedback = null;
	}
	
	public Integer getNbrFeedback() {
		return nbrFeedback;
	}

	public void setNbrFeedback(Integer nbrFeedback) {
		this.nbrFeedback = nbrFeedback;
	}

	public ContextData(String entity, String property, String value, Double belief, Integer nbrFeedback)
	{
		this.entity = entity;
		this.property = property;
		this.value = value;
		this.belief = belief;
		this.nbrFeedback = nbrFeedback;
	}

	public Double getBelief() {
		return belief;
	}

	public void setBelief(Double belief) {
		this.belief = belief;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString()
	{
		if(belief != null)
		{ return "("+entity+" "+property+" "+value+" "+belief+")"; }
		else
		{ return "("+entity+" "+property+" "+value+" 0)"; }	
	}
	
	@Override
	public boolean equals(Object ob)
	{
		if(ob instanceof ContextData)
		{
			ContextData cdB = (ContextData)ob;
			return entity.equals(cdB.getEntity()) && property.equals(cdB.getProperty()) && value.equals(cdB.getValue());
		}
		else
		{ return false; }
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		return result;
	}
	
}
