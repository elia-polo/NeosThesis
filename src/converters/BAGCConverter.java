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
	private static final String adj_coo_filename = "adj_coo.txt";
	private static final String attr_tab_filename = "attr_tab.txt";
	
	private static final char SEPARATOR = ' ';
	
		
	/**
	 * Converts a UsersGraph into a dataset file suitable for the BAGC clustering algorithm.
	 * The first expected input is a COO file, where each row is a 3-tuple (user id, user id, value).
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
			
		try (BufferedWriter adj_coo  = Files.newBufferedWriter(Paths.get(asset_folder+adj_coo_filename), StandardCharsets.UTF_8);
			 BufferedWriter attr_tab = Files.newBufferedWriter(Paths.get(asset_folder+attr_tab_filename), StandardCharsets.UTF_8)) {

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
				u_inc_id = ConvUtility.getIncId(users_inc_id_map, u.getId().toString());
				line_user = u_inc_id.toString() + SEPARATOR;
				
				//get user friends
				Iterable<Vertex> friends = u.getVertices(Direction.BOTH, "friend");
				 
				for (Vertex f : friends) {
					f_inc_id = ConvUtility.getIncId(users_inc_id_map, f.getId().toString());
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
				            
				            u.getProperty(UserUtility.HIGH_SCHOOL).toString() + SEPARATOR + 
							u.getProperty(UserUtility.COLLEGE).toString() + SEPARATOR  +
							u.getProperty(UserUtility.GRADUATE_SCHOOL).toString() + SEPARATOR  ;
				
				/* get the _user_ like (here, Direction.OUT, is enough) */
				user_likes = u.getVertices(Direction.BOTH, "likes");
				
				/* likes binary vector */
				String likes_vec = new String();
				
				/* for each graph likes */
				for (Vertex l : likes) {
					likes_vec += ConvUtility.contains(user_likes,l) ? "1" : "0";
					likes_vec += SEPARATOR;
				}
				
				line_attr += likes_vec + System.lineSeparator();
				attr_tab.write(line_attr, 0, line_attr.length());
			}
			
			//add N N 0!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
		
		UserPuker j;
		
		for (File f : files) {
			j = new  UserPuker(f.getAbsolutePath());
			System.out.println(f.getAbsolutePath());
			g.addUser(j.getEUser());			
		}
		
		new BAGCConverter().translate(g);
	}
	
}
