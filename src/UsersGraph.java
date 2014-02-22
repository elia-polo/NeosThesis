import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.gml.GMLWriter;



public class UsersGraph {

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


	public Graph getGraph() { return graph ; } 
	public UsersGraph() {
		graph = new TinkerGraph();
		
	}

	
	/**
	 * 
	 * Vertex.setProperty façade
	 * @param v, vertex to which set the properties 
	 * @param name, properties name
	 * @param o, properties value
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
	 * add an EUser (see UserUtility.java) to the usersGraph<br>
	 * <br>
	 * NB: The addVertex function throws a IllegalArgumentExeception
	 *     when an already-in vertex has been added.
	 *     
	 * @param user, the user to be added
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
			System.out.println(user.getId() + ": already in!");
			graph.removeVertex(graph.getVertex(user.getId()));
			v_user = graph.addVertex(user.getId());
			shared_users++;
		}
		
		setProperty(v_user,"whoami", "user");
		
		/* !!!should be used a final class with tag members */
		// !!!setProperty(v_user,"ide", user.getId()); not necessary because of addVertex(user.getId())
		
		setProperty(v_user,"isDAU", user.isDAU());
		setProperty(v_user,"gender", user.getGender());
		setProperty(v_user,"birthday", user.getBirthday());
		setProperty(v_user,"relationship_status", user.getRelationship_status());
		setProperty(v_user,"interested_in", user.getInterested_in());
		
		if (user.getHometown()!=null)
			setProperty(v_user,"hometown", user.getHometown().getId());
		
		if (user.getLocation()!=null)
			setProperty(v_user,"location", user.getLocation().getId());
		
		/* friends list */
		if (user.getFriends()!=null) {
			Vertex v_friend;
			for (String f_id : user.getFriends()) { 
				try {
					v_friend = graph.addVertex(f_id);
					users_number++;
				} catch (IllegalArgumentException e) { 
					v_friend = graph.getVertex(f_id);
					shared_friends++;
				}
				graph.addEdge(null, v_user, v_friend, "friend");
			}
		}
			
		/* likes list */
		if (user.getLikes()!=null) {
			Vertex v_like;
			for (ELike like : user.getLikes()) {			
				try {
					v_like = graph.addVertex(like.getId());
					likes_number++;
					//setProperty(v_like, "ide", like.getId());
					
					setProperty(v_like, "name", like.getName());
					setProperty(v_like, "category", like.getCategory());
					
					try {
						String cat_list  = new String();
						for (CoupleNameId cni : like.getCategory_list())
							cat_list += cni.getId() + ",";
						cat_list = cat_list.substring(0, cat_list.length()-1);	
						setProperty(v_like,"category_list", cat_list);
					} catch(NullPointerException e) { }
					
					setProperty(v_like,"whoami", "like");
					
				} catch (IllegalArgumentException e) { 
					v_like = graph.getVertex(like.getId());
					shared_likes++;
				}
				graph.addEdge(null, v_user, v_like, "likes");
			}
		}
		
		return graph;
	}
	
	public static void main(String[] args) throws IOException, FileNotFoundException {
		
		File inputFolder = new File("/home/np2k/Desktop/jxx");
		File[] files = inputFolder.listFiles();
		
		UsersGraph g = new UsersGraph();
		
		int i=0;
		int user_count=0, likes_count=0;
		EUser u;
		for (File f : files) {
			JsonUser j = new  JsonUser(f.getAbsolutePath());
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
		

		Graph graph = g.getGraph();
		Collection<Vertex> vertex_coll = (Collection<Vertex>)graph.getVertices();
		Collection<Edge> edge_coll = (Collection<Edge>)graph.getEdges();
		
		System.out.println("\n--------------------------DEBUG--------------------------");
		
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