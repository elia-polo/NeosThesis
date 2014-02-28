package main;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import metrics.Stats;
import utils.Util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.gml.GMLWriter;


/**
 * 
 * The UsersGraph class creates a users graph in which
 * there are two types of node:
 * <ol>
 * <li>user node
 * <li>like node
 * </ol>
 * 
 * The first one has the follow properties (see UserTags class in UserUtility.java)
 * <ul>
 * <li>WHOAMI</li><li>REL_STATUS</li><li>ISDAU</li><li>INTERESTED_IN</li><li>GENDER</li>
 * <li>HOMETOWN</li><li>BIRTHDAY</li><li>LOCATION</li>
 * <li>blueprintsId(the blueprintsId is available with Vertex.getId function) </li>
 * </ul> 
 *    
 * and the following (undirected) edges:
 * <ul>
 * <li>FRIEND</li> 
 * <li>LIKES</li>
 * </ul>
 * The FRIEND edges link a user node with another usernode. On the other hand
 * the LIKES edges link a user node with a like node.
 * 
 * A like node has the follow properties:
 * <ul>
 * <li>WHOAMI</li>
 * <li>NAME</li>
 * <li>CATEGORY</li>
 * <li>CATEGORY_LIST</li>
 * <li>blueprintsId  (the blueprintsId is available with Vertex.getId function)</li>
 * </ul>
 * The CATEGORY_LIST is a comma separated list of category unique identifiers.
 *    
 * */

public class UsersGraph {

	private static final boolean debug = false; 
	
	private Graph graph;                 /* the whole users graph */

	/**************************** debug purposes zone ****************************/
	private static int users_number = 0;        //numero utenti totali
	private static int likes_number = 0;        //numero like totali
	//users_number + likes_number == numero nodi
	
	private static int shared_users = 0;        //numero di utenti (EUser) gia' inseriti nel grafo
	private static int shared_friends = 0;      //sommatoria di amici in comune
 	private static int shared_likes = 0;        //sommatoria di likes in comune
	
 	public Stats statistics = new Stats();

 	
	public int getSharedUsers() { return shared_users; }
	public int getSharedFriends() { return shared_friends; }
	public int getSharedLikes() { return shared_likes; }
	public int getUsersNumber() { return users_number; }
	public int getLikesNumber() { return likes_number; }
	
	public boolean checkUsersNumber(int total)  { 
		return total==users_number+shared_users+shared_friends ? true : false ;
	}
	
	public boolean checkLikesNumber(int total) {
		return total==likes_number+shared_likes ? true : false ;
	}
	/*****************************************************************************/

	public UsersGraph() { graph = new TinkerGraph(); }
	
	public Graph getGraph() { return graph ; }
	
	/**
	 * 
	 * Vertex.setProperty fa�ade
	 * @param v vertex to which set the properties 
	 * @param name properties name
	 * @param o properties value
	 * 
	 * @return true if (o is not null) a valid property was added to the given vertex, false otherwise
	 *  
	 * */
	private boolean setProperty(Vertex v, String name, Object o)
	{
		if (o!=null) {
			v.setProperty(name, o);
			return true;
		}
		else {
			v.setProperty(name, "null");
			return false;
		}
	}
	
	public void fillMissingValue(Vertex v) {
		 
		/* friends list */
		Iterable<Vertex> f_list = v.getVertices(Direction.BOTH, UserUtility.FRIEND);
		
		/* friends number */
		int f_num=0; 
		
		/* the current property (age, location...)*/
		String property;
		
		/******** gender variables ********/
		int male = 0, female = 0;
		
		/******** age variable ********/
		ArrayList<Integer> age_list = new ArrayList<Integer>();
		
		/******** location variable ********/
		HashMap<String, Integer> loc_score = new HashMap<String, Integer>();

		
		/******** hometown variable ********/
		HashMap<String, Integer> home_score = new HashMap<String, Integer>();

		
		/******** interested variable ********/
		HashMap<String, Integer> interested_score = new HashMap<String, Integer>();

		
		/******** relationship variable ********/
		HashMap<String, Integer> relationship_score = new HashMap<String, Integer>();

		
		
		/******** education variable ********/
		int hs_score = 0, c_score = 0, gs_score = 0;
		String edu[];
			
		
		for (Vertex friend : f_list) {
			f_num ++;
			/**** gender ****/
			property = friend.getProperty(UserUtility.GENDER);
						
			if (property.equals("male"))
				male++;
			else if (property.equals("female"))
				female++;
			//else (if gender == "null") do nothing
			
			
			/**** age ****/
			property = friend.getProperty(UserUtility.AGE).toString();
			
			if (property.equals("null") == false) {
				age_list.add(Integer.parseInt(property));
			}

			/**** location ****/
			property = friend.getProperty(UserUtility.LOCATION).toString();
			if (property.equals("null") == false) 
				Util.incValue(loc_score, property + "," + 
						friend.getProperty(UserUtility.LOCATION_NAME).toString());
			
			/**** hometown ****/
			property = friend.getProperty(UserUtility.HOMETOWN).toString();
			if (property.equals("null") == false)
				Util.incValue(home_score, property + "," +
						friend.getProperty(UserUtility.HOMETOWN_NAME).toString());
			
			/**** interested ****/
			property = friend.getProperty(UserUtility.INTERESTED_IN).toString();
			if (property.equals("null") == false)
				Util.incValue(interested_score, property);
			
			/**** relation_ship ****/
			property = friend.getProperty(UserUtility.REL_STATUS).toString();
			if (property.equals("null") == false) 
				Util.incValue(relationship_score, property);
			
			/**** education ****/
			property = friend.getProperty(UserUtility.EDUCATION).toString();
			if ( property.equals("0,0,0") == false ) {
				edu = Util.fromCSV(property);
				if (edu[0].equals("1")) hs_score++;
				if (edu[1].equals("1")) c_score++;
				if (edu[2].equals("1")) gs_score++;
			}

		}//FOR
		
		/* education */
		property = v.getProperty(UserUtility.EDUCATION);
		if (property.equals("0,0,0") == false) {
			edu = Util.fromCSV(property);
			
			String[] res = new String[3];
			res[0] = "0"; res[1] = "0"; res[2] = "0";
			
			if (edu[2].equals("1")) {
					res[0] = "1";
					res[1] = "1";
					res[2] = "1";
			} else if(edu[1].equals("1")) { 
					res[0] = "1";
					res[1] = "1";
			}
			
			if (res[0].equals("0")) 
				res[0] =  (f_num - hs_score) > hs_score  ? "0" : "1";
			
			
			if (res[1].equals("0")) 
				res[1] =  (f_num - c_score) > c_score  ? "0" : "1";
			
						
			if (res[1].equals("0")) 
				res[1] =  (f_num - gs_score) > gs_score  ? "0" : "1";
				
			
			v.setProperty(UserUtility.EDUCATION, Util.toCSV(res));
		}
		/* age */
		property = v.getProperty(UserUtility.AGE).toString();
		if (property.equals("null")) {
			if (age_list.isEmpty() == false)
				v.setProperty(UserUtility.AGE, Util.median(age_list).toString());
			else
				v.setProperty(UserUtility.AGE, "22");			
		}
		
		property = v.getProperty(UserUtility.AGE).toString();

		/* location */
		property = v.getProperty(UserUtility.LOCATION).toString();
		if (property.equals("null"))
		{
			if (loc_score.isEmpty()==false)
			{
				String[] loc = Util.fromCSV(Util.retMax(loc_score));				
				v.setProperty(UserUtility.LOCATION, loc[0]);
				v.setProperty(UserUtility.LOCATION_NAME, loc[1]+","+loc[2]);
			}
			else { 
				v.setProperty(UserUtility.LOCATION, "108581069173026");
				v.setProperty(UserUtility.LOCATION_NAME, "Milan,Italy");
			}
		}
		
		/* hometown */
		property = v.getProperty(UserUtility.HOMETOWN).toString();
		if (property.equals("null")) 
		{
			if (home_score.isEmpty()==false)
			{
				String[] home = Util.fromCSV(Util.retMax(home_score));
				v.setProperty(UserUtility.HOMETOWN, home[0]);
				v.setProperty(UserUtility.HOMETOWN_NAME, home[1]);
			}
			else { 
				v.setProperty(UserUtility.HOMETOWN, "108581069173026");
				v.setProperty(UserUtility.HOMETOWN_NAME, "Milan,Italy");
			}
		}
		
		/* interested */
		property = v.getProperty(UserUtility.INTERESTED_IN).toString();
		if (property.equals("null")) {
			if (interested_score.isEmpty() == false)
				v.setProperty(UserUtility.INTERESTED_IN, Util.retMax(interested_score));
			else
				v.setProperty(UserUtility.INTERESTED_IN, "male");
		}
		
		/* relationship */
		property = v.getProperty(UserUtility.REL_STATUS).toString();
		if (property.equals("null")) {
			if (relationship_score.isEmpty() == false) {
				v.setProperty(UserUtility.REL_STATUS, Util.retMax(relationship_score));
			}
			else {
				v.setProperty(UserUtility.REL_STATUS, "Single");
			}
		}
		/* gender */
		property = v.getProperty(UserUtility.GENDER).toString();
		if (property.equals("null")) 		
			v.setProperty(UserUtility.GENDER, male > female ? "male" : "female" );
		
	}

	/**
	 * <p>Add an EUser (see UserUtility.java) to the usersGraph</p>
	 * NB: The addVertex function throws a IllegalArgumentExeception
	 *     when an already-in vertex has been added.
	 * <br>
	 * The general idea is:
	 * <ol>
	 * <li>get the 'user'</li>
	 * <li>set gender, birthday,isDau,etc. properties</li>
	 * <li>get its friends</li>
	 * <ul><li>create an edge from the 'user' to a new friend node</li></ul>
	 * <li>get its likes</li>
	 * <ul><li>create an edge from the 'user' to a new like node</li></ul>
	 * </ol>
	 * @param user the user to be added
	 * @return the updated graph
	 * 
	 * */
	
	public Graph addUser(EUser user) 
	{
		// Increment always, even if the node was already inserted
		statistics.incrementUserCount();
		Vertex v_user;
		try {
			v_user = graph.addVertex(user.getId());
			statistics.incrementNodeCount();
			users_number++;
		} catch(IllegalArgumentException e) {		
			//System.out.println(user.getId() + ": already in!"); //DEBUG
			v_user = graph.getVertex(user.getId());
			shared_users++;
		}
		
		setProperty(v_user,UserUtility.WHOAMI, "user");
		
		// !!!setProperty(v_user,"ide", user.getId()); not necessary because of addVertex(user.getId())
		
		setProperty(v_user,UserUtility.ISDAU, user.isDAU());
		if(setProperty(v_user,UserUtility.GENDER, user.getGender())) {
			statistics.incrementGender();
		}
		if(setProperty(v_user,UserUtility.BIRTHDAY, user.getBirthday())) {
			statistics.incrementBirthday();
		}
		
		if (user.getBirthday()!=null) 
			setProperty(v_user,UserUtility.AGE, Util.getAge(user.getBirthday()));
		else
			v_user.setProperty(UserUtility.AGE, "null");

		if(setProperty(v_user,UserUtility.REL_STATUS, user.getRelationship_status())) {
			statistics.incrementRelStatus();
		}
		if(setProperty(v_user,UserUtility.INTERESTED_IN, user.getInterested_in())) {
			statistics.incrementInterestedIn();
		}
		

		if (user.getHometown()!=null) {
			if(setProperty(v_user,UserUtility.HOMETOWN, user.getHometown().getId())) {
				statistics.incrementHometown();
			}
			setProperty(v_user, UserUtility.HOMETOWN_NAME, user.getHometown().getName());
			
		} else {
			v_user.setProperty(UserUtility.HOMETOWN, "null");
			v_user.setProperty(UserUtility.HOMETOWN_NAME, "null");			
		}
		
		if (user.getLocation()!=null) {
			if(setProperty(v_user,UserUtility.LOCATION, user.getLocation().getId())) {
				statistics.incrementLocation();
			}
			setProperty(v_user, UserUtility.LOCATION_NAME, user.getLocation().getName().replaceAll(", ", ","));
		} else {
			v_user.setProperty(UserUtility.LOCATION, "null");
			v_user.setProperty(UserUtility.LOCATION_NAME, "null");
		}
		
		if(user.getEdu()!= null) {
			String[] vec = user.getEduVec();
			if(setProperty(v_user,UserUtility.EDUCATION, Util.toXSV(vec, ","))) {
				statistics.incrementEducation();
				if(vec[0].equals("1"))
					statistics.incrementHighSchool();
				if(vec[1].equals("1"))
					statistics.incrementCollege();
				if(vec[2].equals("1"))
					statistics.incrementGraduateSchool();
			}
		} else setProperty(v_user, UserUtility.EDUCATION, "null");
		
		/* friends list */
		if (user.getFriends()!=null) {
			statistics.incrementFriends();
			Vertex v_friend;
			String edge_id;
			for (String f_id : user.getFriends()) {
				try {
					v_friend = graph.addVertex(f_id);
					if(debug) {
						v_friend.setProperty(UserUtility.WHOAMI, "user");
					}
					users_number++;
					statistics.incrementNodeCount();
				} catch (IllegalArgumentException e) { 
					v_friend = graph.getVertex(f_id);
					shared_friends++;
				}
					
				/* create an unique id for the edge between a user and its friend */
				if (v_user.getId().toString().compareTo(v_friend.getId().toString()) > 0) 
					edge_id = v_user.getId().toString()+ "-" + v_friend.getId().toString();
				else
					edge_id = v_friend.getId().toString() + "-" + v_user.getId().toString() ;
				
				try {
					graph.addEdge(edge_id, v_user, v_friend, UserUtility.FRIEND);
					statistics.incrementEdgeCount();
				} catch (IllegalArgumentException e) { /* do nothing */ }

			}
		}
			
		/* likes list */
		if (user.getLikes()!=null) {
			statistics.incrementLikes();
			Vertex v_like;
			for (ELike like : user.getLikes()) {
				try {
					//!!!only id is used
					v_like = graph.addVertex(like.getId());
					likes_number++;
					statistics.incrementLikeCount();
					//setProperty(v_like, "ide", like.getId());
					
					setProperty(v_like, UserUtility.NAME, like.getName());
					setProperty(v_like, UserUtility.CATEGORY, like.getCategory());
					
					try {
						String cat_list  = new String();
						//!!!only id is used
						for (CoupleNameId cni : like.getCategory_list())
							cat_list += cni.getId() + ",";
						cat_list = cat_list.substring(0, cat_list.length()-1);	
						setProperty(v_like,UserUtility.CATEGORY_LIST, cat_list);
					} catch(NullPointerException e) { }
					
					setProperty(v_like,UserUtility.WHOAMI, "like");
					
				} catch (IllegalArgumentException e) { 
					v_like = graph.getVertex(like.getId());
					shared_likes++;
				}
				graph.addEdge(null, v_user, v_like, UserUtility.LIKES);
				statistics.incrementEdgeCount();
			}
		}
		
		return graph;
	}
	
	public static void main(String[] args) throws IOException, FileNotFoundException {
		
		File inputFolder = new File("/home/np2k/Desktop/test_missing");
		File[] files = inputFolder.listFiles();
		
		UsersGraph g = new UsersGraph();
		
		//int i=0;
		int user_count=0, likes_count=0;
		EUser u;
		UserPuker j;
		for (File f : files) {
			j = new  UserPuker(f.getAbsolutePath());
			u = j.getEUser();
			g.addUser(u);
			
			if (u.getFriends()!=null)
				user_count += u.getFriends().size() +  1;
			else 
				user_count += 1;
			
			if (u.getLikes()!=null) 
				likes_count+= u.getLikes().size();
			//System.out.println(files.length-i);
			//i++;
		}
		

		System.out.println("\n--------------------------DEBUG--------------------------");
		
		Graph graph = g.getGraph();
		Collection<Vertex> vertex_coll = (Collection<Vertex>)graph.getVertices();
		Collection<Edge> edge_coll = (Collection<Edge>)graph.getEdges();
		
		if (g.checkLikesNumber(likes_count))
			System.out.println("likes number OK");
		else System.out.println("likes number KO!!!");

		if (g.checkUsersNumber(user_count))
			System.out.println("users number OK");
		else System.out.println("likes number KO!!!");

		
		if (vertex_coll.size() == g.getUsersNumber()+g.getLikesNumber())
			System.out.println("#graph vertex == #user+#likes");
		else
			System.out.println("#graph vertex == #user+#likes!!!!!!");

		File output_net = new File("/home/np2k/Desktop", "net.gml");
		
		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(output_net));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		GMLWriter.outputGraph(graph,bos);
				
		System.out.println("\nnodes number: " + vertex_coll.size());
		System.out.println("edge number: " + edge_coll.size());
		System.out.println("users number: " + g.getUsersNumber());
		System.out.println("likes number: " + g.getLikesNumber());
		
		System.out.println("\nshared friends: " + g.getSharedFriends());
		System.out.println("shared users: " + g.getSharedUsers());
		System.out.println("shared likes: " + g.getSharedLikes());		
		
		System.out.println("\nuser_count: " + user_count);
		System.out.println("likes_count: " + likes_count);
		
		//////////////////////////////////MISSING VALUE////////////////////////////////////////////////
		Vertex a = graph.getVertex("1042024118");
		Vertex n = graph.getVertex("1029116096");
		System.out.println("\n\n*****************\ninferenza missing value...\n");
		g.fillMissingValue(n);
		g.fillMissingValue(a);
		
		Graph newgraph = new TinkerGraph();
		newgraph.addVertex(n);
		newgraph.addVertex(a);
		
		output_net =  new File("/home/np2k/Desktop", "newgraph.gml");
		try {
			bos = new BufferedOutputStream(new FileOutputStream(output_net));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		GMLWriter.outputGraph(graph,bos);
	}
	
	public float getMissingValueRatio(String field) {
		switch(field) {
		case UserUtility.GENDER:
			return statistics.getGender()/statistics.getUserCount();
		case UserUtility.REL_STATUS:
			return statistics.getRelStatus()/statistics.getUserCount();
		case UserUtility.INTERESTED_IN:
			return statistics.getInterestedIn()/statistics.getUserCount();
		case UserUtility.BIRTHDAY:
			return statistics.getBirthday()/statistics.getUserCount();
		case UserUtility.HOMETOWN:
			return statistics.getHometown()/statistics.getUserCount();
		case UserUtility.LOCATION:
			return statistics.getLocation()/statistics.getUserCount();
		case UserUtility.FRIENDS:
			return statistics.getFriends()/statistics.getUserCount();
//		case UserUtility.MUTUALFRIENDS:
//			return statistics.getMutualFriends()/statistics.getUserCount();
		case UserUtility.LIKES:
			return statistics.getLikes()/statistics.getUserCount();
		case UserUtility.CATEGORY:
			return statistics.getCategory()/statistics.getUserCount();
		case UserUtility.CATEGORY_LIST:
			return statistics.getCategoryList()/statistics.getUserCount();
		case UserUtility.EDUCATION:
			return statistics.getEducation()/statistics.getUserCount();
		case UserUtility.HIGH_SCHOOL:
			return statistics.getHighSchool()/statistics.getUserCount();
		case UserUtility.COLLEGE:
			return statistics.getCollege()/statistics.getUserCount();
		case UserUtility.GRADUATE_SCHOOL:
			return statistics.getGraduateSchool()/statistics.getUserCount();
		default:
			throw new IllegalArgumentException("Field '"+field+"' does not exist");		}
	}
}
