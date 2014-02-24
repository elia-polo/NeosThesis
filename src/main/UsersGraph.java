package main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;


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

	private static final boolean debug = true; 
	
	private Graph graph;                 /* the whole users graph */

	/**************************** debug purposes zone ****************************/
	private static int users_number = 0;        //numero utenti totali
	private static int likes_number = 0;        //numero like totali
	//users_number + likes_number == numero nodi
	
	private static int shared_users = 0;        //numero di utenti (EUser) gia' inseriti nel grafo
	private static int shared_friends = 0;      //sommatoria di amici in comune
 	private static int shared_likes = 0;        //sommatoria di likes in comune
	
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
	 * @return void
	 *  
	 * */
	private void setProperty(Vertex v, String name, Object o)
	{
		if (o!=null)
			v.setProperty(name, o);
		else
			v.setProperty(name, "null");
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
		Vertex v_user;
		try {
			v_user = graph.addVertex(user.getId());
			users_number++;
		} catch(IllegalArgumentException e) {		
			//System.out.println(user.getId() + ": already in!"); //DEBUG
			v_user = graph.getVertex(user.getId());
			shared_users++;
		}
		
		setProperty(v_user,UserUtility.WHOAMI, "user");
		
		// !!!setProperty(v_user,"ide", user.getId()); not necessary because of addVertex(user.getId())
		
		setProperty(v_user,UserUtility.ISDAU, user.isDAU());
		setProperty(v_user,UserUtility.GENDER, user.getGender());
		setProperty(v_user,UserUtility.BIRTHDAY, user.getBirthday());
		setProperty(v_user,UserUtility.REL_STATUS, user.getRelationship_status());
		setProperty(v_user,UserUtility.INTERESTED_IN, user.getInterested_in());
		
		//!!!only id is used
		if (user.getHometown()!=null)
			setProperty(v_user,UserUtility.HOMETOWN, user.getHometown().getId());
		
		//!!!only id is used
		if (user.getLocation()!=null)
			setProperty(v_user,UserUtility.LOCATION, user.getLocation().getId());
		
		/* friends list */
		if (user.getFriends()!=null) {
			Vertex v_friend;
			String edge_id;
			for (String f_id : user.getFriends()) { 
				try {
					v_friend = graph.addVertex(f_id);
					if(debug) {
						v_friend.setProperty(UserUtility.WHOAMI, "user");
					}
					users_number++;
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
				} catch (IllegalArgumentException e) { /* do nothing */ }

			}
		}
			
		/* likes list */
		if (user.getLikes()!=null) {
			Vertex v_like;
			for (ELike like : user.getLikes()) {			
				try {
					//!!!only id is used
					v_like = graph.addVertex(like.getId());
					likes_number++;
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
			}
		}
		
		return graph;
	}
	
	public static void main(String[] args) throws IOException, FileNotFoundException {
		
		File inputFolder = new File("/home/np2k/Desktop/json_user");
		File[] files = inputFolder.listFiles();
		
		UsersGraph g = new UsersGraph();
		
		int i=0;
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
			System.out.println(files.length-i);
			i++;
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

		/*File output_net = new File("/home/np2k/Desktop", "net.gml");
		
		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(output_net));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		GMLWriter.outputGraph(graph,bos);*/
				
		System.out.println("\nnodes number: " + vertex_coll.size());
		System.out.println("edge number: " + edge_coll.size());
		System.out.println("users number: " + g.getUsersNumber());
		System.out.println("likes number: " + g.getLikesNumber());
		
		System.out.println("\nshared friends: " + g.getSharedFriends());
		System.out.println("shared users: " + g.getSharedUsers());
		System.out.println("shared likes: " + g.getSharedLikes());		
		
		System.out.println("\nuser_count: " + user_count);
		System.out.println("likes_count: " + likes_count);
		
	}
}
