import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


public class Bacon {
	//class to create a dbconn and execute bfs to find degree of separation between two actors
	private String dbms;
	private String password;
	private String userName;
	private String serverName;
	private int portNumber;
	private String dbName;
	
	
	private Connection con;
	
	public Bacon(String dbms, String password, String userName,
			String serverName, int portNumber, String dbName) {
		super();
		this.dbms = dbms;
		this.password = password;
		this.userName = userName;
		this.serverName = serverName;
		this.portNumber = portNumber;
		this.dbName = dbName;
		
		try {
			con = getConnection();
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}



	/**
	 * @param args
	 */
	
	public Connection getConnection() throws SQLException {
		//taken from Oracle javadocs

	    Connection conn = null;
	    Properties connectionProps = new Properties();
	    connectionProps.put("user", this.userName);
	    connectionProps.put("password", this.password);

	    if (this.dbms.equals("mysql")) {
	        conn = DriverManager.getConnection(
	                   "jdbc:" + this.dbms + "://" +
	                   this.serverName +
	                   ":" + this.portNumber + "/",
	                   connectionProps);
	    } else if (this.dbms.equals("derby")) {
	        conn = DriverManager.getConnection(
	                   "jdbc:" + this.dbms + ":" +
	                   this.dbName +
	                   ";create=true",
	                   connectionProps);
	    }
	    System.out.println("Connected to database");
	    return conn;
	}
	
	
	//returns a set of undiscovered actors ie doens't exist in Set actors
	public Set<Integer> query(int origActor, Set<Integer> actors){
		Set<Integer> newActors = new HashSet<Integer>();
		try{
			PreparedStatement traverse;//prep statement to discover a nodes neighbors
			
			String traverseStatement = "SELECT actorID FROM seenIn WHERE movieID IN " +
					"(SELECT movieID FROM seenIn WHERE actorID = ?";
			traverse = con.prepareStatement(traverseStatement);
			
			traverse.setInt(1, origActor); // bind variables to prep statement
			ResultSet results = traverse.executeQuery();
			
			while(results.next()){
				//for each result add that result to new actors
				int friendID = results.getInt(1);
				newActors.add(friendID);
			}
			//take set difference between actors discovered in query and previously seen actors
			newActors.removeAll(actors);
			//new actors now contains unseen actors discovered in traversal;
			
			
		}
		catch(SQLException s){
			s.printStackTrace();
			
		}
		return newActors;
	}
	int execute(int origActor, int targetActor, int maxDepth){
		//keep traversing graph in bfs style until target actor is found
		
		Set<Integer> prevActors = new HashSet<Integer>(); //discovered actors
		Set<Integer> result = new HashSet<Integer>(); //new actors discovered in previous depth
			
		boolean progress = true;//indicating new nodes are still being discovered
		int depth = 0; //keeps track of depth
		result.add(origActor); //initialize result to origActor
		while(progress&&!prevActors.contains(targetActor)&&depth<maxDepth){
			//when this while breaks depth will be returned
			Set<Integer> newActors = new HashSet<Integer>(); //new actors discovered this round
			for(int actor : result){
				//for each new actor discovered in the previous round
				newActors.addAll(query(actor, prevActors));
				//find all of that actors undiscovered friends
			}
			progress = !newActors.isEmpty(); //check to ensure we found new actors (progress)
			depth++; //increment depth
			prevActors.addAll(result); //update the list of seen actors
			result.clear(); //empty results
			result.addAll(newActors); //add the actors discovered in this degree
			
		}
		return depth;
		
	}
	public static void main(String[] args) {
		//six degrees method
		Bacon kevin = new Bacon("dbms", "pwd", "uname", "servnName", 1, "dbname");//construct new graph searching obj
		int seperation = kevin.execute(1, 2, 1000); //do a fake search for id1 =1 and id2 = 2 and set maxDepth to 1000;
		System.out.println(seperation);
		//should add ability to execute with two actor names.  Would be an easy implementation but out of time
		
		

	}

}
