package leaf.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.h2.jdbcx.JdbcDataSource;

import leaf.context.ContextData;

/**
 * The DataBase Manager (DBM) is in charge of interacting with an embedded H2 database.
 * It uses the singleton paradigm.
 * @author Nathan Ramoly
 *
 */
public class DataBaseManager {

	
	/** Attributes **/
	
	/**
	 * Access to database
	 */
	private Connection db;
	
	/**
	 * Path to db file
	 */
	private static final String DBPATH = "~/Documents/workspace/LEAF/res/db/db";
	
	
	
	
	/** Constructor and singleton design pattern **/
	
	/**
	 * Private constructor, shall be called only once.
	 */
	private DataBaseManager()
	{
		//Loading/Creating database
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:"+DBPATH);
		ds.setUser("sa");
		ds.setPassword("sa");
		try {
			db = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0); //no db, aborting program
		}
		
		//Table creation
		
		String queryTableSituation = "CREATE TABLE IF NOT EXISTS History(id INT PRIMARY KEY AUTO_INCREMENT, storeDate TIMESTAMP, task VARCHAR(255), success BOOLEAN, path VARCHAR(255) )";
		String queryTableCauses = "CREATE TABLE IF NOT EXISTS Cause(id INT PRIMARY KEY AUTO_INCREMENT, subject VARCHAR(255), predicate VARCHAR(255), object VARCHAR(255), failBelief DOUBLE, numberFeedback INT )";
		String queryTableLink = "CREATE TABLE IF NOT EXISTS Belonging(idCause INT, idSituation INT, PRIMARY KEY(idCause, idSituation) )";
		
		try {
			Statement stmt = db.createStatement();
			
			stmt.execute(queryTableSituation);
			stmt.execute(queryTableCauses);
			stmt.execute(queryTableLink);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	} 
	
	/**
	 * The instance
	 */
	private static DataBaseManager INSTANCE = null;
 
	/**
	 * Creation or getting of the instance.
	 */
	public static synchronized DataBaseManager getInstance()
	{			
		if (INSTANCE == null)
		{ INSTANCE = new DataBaseManager(); }
		return INSTANCE;
	}
	

	/**
	 * Create a new entry for a situation in the history.
	 * An entry is created at the same time as the situation's ontology is saved.
	 * Date is set as the current one and the success is defulatly set to true.
	 */
	public void addSituationToHistory(String task, String pathToOnto)
	{
		try {
			Statement stmt = db.createStatement();
			
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			String query = "INSERT INTO History(storeDate, task, success, path) VALUES('"+timestamp+"', '"+task+"', 'true', '"+pathToOnto+"' )";
			
			stmt.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Set the last stored situation history success value to false.
	 */
	public void updateFailLastAction()
	{
		try {
			Statement stmt = db.createStatement();
			
			String query = "UPDATE History SET success=false WHERE storeDate = (SELECT MAX(storeDate) FROM History)";
			
			stmt.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * Run a query
	 * Just to prevent light code redundancy
	 * @throws SQLException 
	 */
	private ResultSet executeQuery(String query) throws SQLException
	{
		Statement stmt = db.createStatement();
		
		return stmt.executeQuery(query);
	}
	
	/**
	 * Get the number of failing situations for one given task
	 */
	public int getNbrFailSituation(String task)
	{
		int ret = 0;
		
		try {
			ResultSet res = executeQuery("SELECT COUNT(*) AS count FROM History WHERE task LIKE '"+task+"' AND success='False'");
			
			if(res.next())
			{ ret = Integer.parseInt( res.getString("count") ); }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * Get the number of failing situation over the last N situation
	 */
	public int getNbrFailLastNSituation(String task, Integer N)
	{
		int ret = 0;
		
		try {
			ResultSet res = executeQuery("SELECT COUNT(*) AS count "
					+ "FROM History h1, (SELECT id as h2id FROM History h2 WHERE h2.task LIKE '"+task+"' ORDER BY h2.storeDate DESC LIMIT "+N+")"
					+ "WHERE h1.id = h2id AND h1.success='False'"
					);
			
			if(res.next())
			{ ret = Integer.parseInt( res.getString("count") ); }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	
	/**
	 * Get all context data (causes) of one task
	 */
	public ArrayList<ContextData> getCD(String task)
	{
		ArrayList<ContextData> ret = new ArrayList<ContextData>();
		
		try {
			
			String query = "SELECT DISTINCT subject, object, predicate, failBelief, numberFeedback FROM Cause, Belonging, History "
					+ "WHERE Cause.id = Belonging.idCause AND History.id = Belonging.idSituation "
					+ "AND History.task LIKE '"+task+"' ";
			
			LeafLog.d("DBM", "Running: "+query);
			ResultSet res = executeQuery( query );
			
			while(res.next())
			{
				ret.add( new ContextData(res.getString("subject"), res.getString("predicate"), res.getString("object"), res.getDouble("failBelief"), res.getInt("numberFeedback")) );
			}			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return ret;
	}
	
	/**
	 * Get the last failing situation path for one given task
	 */
	public String getLastFailSitOntoPath(String task)
	{
		String ret = "";
		
		String query = "SELECT path FROM History "
				+ "WHERE task LIKE '"+task+"' AND success='False' "
				+ "AND storeDate = (SELECT MAX(storeDate) FROM History WHERE task LIKE '"+task+"' AND success='False' )";
		
		try {
			ResultSet res = executeQuery( query );
			
			if(res.next())
			{ret = res.getString("path");}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * Get path to all ontology that succeeded or failed for a give task
	 */
	public ArrayList<String> getOntoPaths(String task, Boolean success)
	{
		ArrayList<String> paths = new ArrayList<String>();		
		
		try {
			
			String query = "SELECT path FROM History "
			+ "WHERE task LIKE '"+task+"' AND success='"+success+"' ";	
			
			ResultSet res = executeQuery( query );
			
			while(res.next())
			{
				paths.add( res.getString("path") );
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return paths;
	}
	
	/**
	 * Insert a new context data
	 * Belief and nbrFeedback must be set !
	 */
	public void insertContextData(String task, ContextData cd)
	{
		try {
			Statement stmt = db.createStatement();
			
			String query = "INSERT INTO Cause(subject, predicate, object, failBelief, numberFeedback) "
					+ "VALUES('"+cd.getEntity()+"', '"+cd.getProperty()+"', '"+cd.getValue()+"', '"+cd.getBelief()+"', '"+cd.getNbrFeedback()+"' )";
			
			stmt.execute(query);
			
			//Adding relation to last failing situations
			createBelonging(task, cd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update the context data with the same subject, predicate and object
	 */
	public void updateContextData(String task, ContextData cd)
	{
		try {
			Statement stmt = db.createStatement();
			
			String query = "UPDATE Cause SET failBelief='"+cd.getBelief()+"', numberFeedback='"+cd.getNbrFeedback()+"' "
					+ "WHERE subject LIKE '"+cd.getEntity()+"' AND predicate LIKE '"+cd.getProperty()+"' AND object LIKE '"+cd.getValue()+"' ";
			
			stmt.execute(query);
			
			//Adding relation to last failing situations
			createBelonging(task, cd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a belonging relation between the given context data (identified as cause) and the last failing situation
	 * Called when adding or updating a cause
	 */
	private void createBelonging(String task, ContextData cd)
	{
		//Query to get the last situation
		String querySit = "SELECT id FROM History "
				+ "WHERE task LIKE '"+task+"' AND success='False' "
				+ "AND storeDate = (SELECT MAX(storeDate) FROM History WHERE task LIKE '"+task+"' AND success='False' )";
		
		//Query to get the context data
		String queryCause = "SELECT id FROM Cause "
				+ "WHERE subject LIKE '"+cd.getEntity()+"' AND predicate LIKE '"+cd.getProperty()+"' AND object LIKE '"+cd.getValue()+"' ";
		
		try {
			ResultSet resSit = executeQuery(querySit);
			ResultSet resCau = executeQuery(queryCause);
			
			if(resSit.next() && resCau.next())
			{
				int idSit = resSit.getInt("id");
				int idCau = resCau.getInt("id");
				
				String query = "INSERT INTO Belonging(idCause, idSituation) "
						+ "VALUES('"+idCau+"', '"+idSit+"')";
			
				Statement stmt = db.createStatement();
				
				stmt.execute(query);				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
