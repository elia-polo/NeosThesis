package converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import utils.Util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import main.UserPuker;
import main.UserUtility;
import main.UsersGraph;
	 
public class BAGCConverter  implements Converter {	
	private static final String asset_folder = "/home/np2k/Desktop/BAGC/";
	
	private static final char SEPARATOR = ' ';
	
	/**
	 * @return true if vertex v is in the Iterable c
	 *         false otherwise 
	 **/
	private boolean contains(Iterable<Vertex> c, Vertex v) {
		for (Vertex e : c) {
			if (e.getId().toString().equals(v.getId().toString()))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets the incremental user id.
	 * If the 'id' has never been inserted into the hashmap then
	 * it is added to the hashmap with an incremental value (i.e
	 * the last incremental id + 1).
	 * If the  'id' has already been inserted into the hashmap
	 * the matching incremental id is returned. 
	 * 
	 * @param map, the incremental user id hashmap
	 * @param id, the FB user id
	 * 
	 * @return the incremental user id
	 * 
	 * */
	private Integer getIncId(HashMap<String, Integer> map, String id) {
		if (map.containsKey(id)) 
			return map.get(id);
		 else {
			Integer res = new Integer(map.size()+1);
			map.put(id, res);
			return res;
		}
	} 
	
	/**
	 * Converts a UsersGraph into a dataset file suitable for the BAGC clustering algorithm.
	 * The first expected input is a COO file, where each row is a triple (user id, user id, value).
	 * The couple of users id represents the (friend) relationship of users.  The user id is an 
	 * incremental value which indentify uniquely a Facebook user.
	 * The second input file is a txt table in which each row number 'i' represents an attributes 
	 * vector associated with the 'i' incremental user id: so, for example, the first row is the
	 * attributes vector of the user with incremental id=1. Each attributes has a categorical domain.
	 * 
	 * Each user must have the a vector a of T attribute: i.e. the 'v' schema is the same for each user.
	 * 
	 * @param g The UsersGraph to be converted
	 * */
	public void translate(UsersGraph g) {
			
		try (BufferedWriter adj_coo  = Files.newBufferedWriter(Paths.get(asset_folder+"adj_coo.txt"), StandardCharsets.UTF_8);
			 BufferedWriter attr_tab = Files.newBufferedWriter(Paths.get(asset_folder+"attr_tab.txt"), StandardCharsets.UTF_8)) {

			// map to store the users incremental id  
			HashMap<String, Integer> users_inc_id_map = new HashMap<String, Integer>();
			
			Graph graph = g.getGraph();
			
			String line_user = new String();
			String line_friend = new String();
			String line_attr  = new String();
			
			Iterable<Vertex> likes =                   /* the whole likes set */
					graph.getVertices(UserUtility.WHOAMI, "like");
			
			Iterable<Vertex> user_likes;
			
			/* get the whole user set */
			Iterable<Vertex> users = graph.getVertices(UserUtility.WHOAMI, "user");
			
			Integer u_inc_id, f_inc_id;      //user incremental id, friend incremental id
			
			for (Vertex u : users) {
				u_inc_id = getIncId(users_inc_id_map, u.getId().toString());
				line_user = u_inc_id.toString() + SEPARATOR;
				
				//get user friends
				Iterable<Vertex> friends = u.getVertices(Direction.BOTH, "friend");
				 
				for (Vertex f : friends) {
					f_inc_id = getIncId(users_inc_id_map, f.getId().toString());
					//add only once edge 
					if (u_inc_id < f_inc_id) {
						adj_coo.write(line_friend, 0, line_friend.length());
						line_friend = line_user + f_inc_id.toString() + SEPARATOR + "1" + System.lineSeparator();
					}
				}
				/*
				 * attributes order
				 * age | gender | home | loc | rel_stat | int_in | HS | C | GS | like1 |...| likeN
				 *(HS = high school, C = college, GS = graduate school -- in binary form)
				 * */
				
				Integer age = Integer.parseInt(u.getProperty(UserUtility.AGE).toString());
				
				line_attr = Util.discretizeAge(age) + SEPARATOR +
				            u.getProperty(UserUtility.GENDER).toString() + SEPARATOR +
				            u.getProperty(UserUtility.HOMETOWN).toString() + SEPARATOR +
				            u.getProperty(UserUtility.LOCATION).toString() + SEPARATOR +
				            u.getProperty(UserUtility.REL_STATUS).toString() + SEPARATOR +
				            u.getProperty(UserUtility.INTERESTED_IN).toString() + SEPARATOR +
				            
				            //a list of space-separated binary numbers (edu vector) 
				            u.getProperty(UserUtility.EDUCATION).toString().replace(","," ") + SEPARATOR;
				
				/* get the _user_ like (here Direction.OUT is enough) */
				user_likes = u.getVertices(Direction.BOTH, "likes");
				
				/* likes binary vector */
				String likes_vec = new String();
				
				/* for each user like */
				for (Vertex l : likes) {
					likes_vec += contains(user_likes,l) ? "1" : "0";
					likes_vec += SEPARATOR;
				}
				
				line_attr += likes_vec + System.lineSeparator();
				attr_tab.write(line_attr, 0, line_attr.length());
			}
			
			//add N N 0
			String N = new Integer(g.statistics.getNodeCount()).toString();
			adj_coo.write(N + SEPARATOR + N + SEPARATOR + "0", 0, 2*N.length()+3);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		
	public static void main(String[] args) throws IOException {
		File inputFolder = new File("/home/np2k/Desktop/test_missing");
		File[] files = inputFolder.listFiles();
		
		UsersGraph g = new UsersGraph();
		
		int i=0;
		UserPuker j;
		
		for (File f : files) {
			j = new  UserPuker(f.getAbsolutePath());
			System.out.println(f.getAbsolutePath());
			g.addUser(j.getEUser());			
			i++;
		}
		i = 0;
		for (Vertex v : g.getGraph().getVertices(UserUtility.WHOAMI, "user")) {
			g.fillMissingValue(v);
			i++;
		}
		System.out.println(i);
		
		new BAGCConverter().translate(g);
	}
	
}
