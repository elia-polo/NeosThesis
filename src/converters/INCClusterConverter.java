/**
 * NB: see triple exclamation mark (!!!) to target the doubts
 * 
 * 
 * Ask the authors:
 * 
 *   1) magic constant: how to calculate it?
 * 1.1) in a scenario like ours (with very rich attributes set) the magic constant 
 *      lost its meaning (since _it seems_ it's divided by the attributes number)
 * 1.2) maybe it could be useful the paper: Clustering Analysis in Large Graphs with Rich Attributes (Yang Zhou, Ling Liu)
 *      
 *   2) how to deal with the absence of an attribute? Is it possbile to not specify a couple (user_id, attribute)?
 *      e.g. not specify in the dataset file the presence of (user_id, attribute_value_id, trans_prob) and
 *                       in the attributes table the presence of (user_id, attribute, attribute_value)?
 *                            
 *   3) Does there exist a two-attributes-no-hard-coded version of the source?
 * 
 */

package converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;


import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import main.UserPuker;
import main.UserUtility;
import main.UsersGraph;
import metrics.AttributesStats;


public class INCClusterConverter implements Converter {
	private static final String asset_folder = "/home/np2k/Desktop/INC/";
	
	private static final String dataset_filename = "Dataset.txt";
	private static final String data_attr_finalename = "DataAttribute.txt";
	
	private static final char SEPARATOR = ' ';
	
	private static final float magic_constant = 0.45456F; 
	
	public void translate(UsersGraph g) {
		
		
		try (BufferedWriter bw_dataset = Files.newBufferedWriter(Paths.get(asset_folder+dataset_filename), StandardCharsets.UTF_8);
		     BufferedWriter bw_attr = Files.newBufferedWriter(Paths.get(asset_folder+data_attr_finalename), StandardCharsets.UTF_8)) {

			Graph graph = g.getGraph();
			
			AttributesStats as = new AttributesStats();
			
			as.getStats(graph);
			
			// get the attributes-value map (gender={male=32,female=12},age={young=...})
			HashMap<String, HashMap<?,?>> attrs_value_map = as.getListAttrsStats();
			
			// get the attributes occurrence map (how many different value of age are there in the graph? etc)
			HashMap<String, Integer> attrs_occ = as.getattrValuesCardinality();
			
			int users_number = g.statistics.getUserCount();
			
			// sum of attributes occurrence
			int sum_attr_occ = 0;
			for (String a : attrs_occ.keySet())
				sum_attr_occ += attrs_occ.get(a);
			
			
			// the likes id starts after the users and attributes id   
			Integer l_start_from  = new Integer(users_number + sum_attr_occ);

			
			// map to store the users incremental id  
			HashMap<String, Integer> users_inc_id_map = new HashMap<String, Integer>();
			
			// map to store the attributes value incremental id  
			HashMap<String, HashMapWL<String,Integer>> attr_inc_id_map = new HashMap<String, HashMapWL<String,Integer>>();
			
			// map to store the likes value incremental id
			HashMap<String, HashMapWL<String, Integer>> likes_inc_id_map = new HashMap<String, HashMapWL<String,Integer>>();
			
			// User, Attribute, Like INCremental IDentificator
			String u_inc_id, a_inc_id, l_inc_id;
			
			// working integer variable
			Integer res = new Integer(0);


			StringBuilder line_user = new StringBuilder();
			StringBuilder line_attr_user = new StringBuilder();
			StringBuilder line_attr  = new StringBuilder();

			// get the whole user set 
			Iterable<Vertex> users = graph.getVertices(UserUtility.WHOAMI, "user");
			
			// get the whole likes set 
			Iterable<Vertex> likes = graph.getVertices(UserUtility.WHOAMI, "like");
			
			// the user likes
			Iterable<Vertex> user_likes;
			
			String trans_prob_user;
			String trans_prob_attr;
			
			// attributes values array 
			ArrayList<String> attrs_value = new ArrayList<String>();
			
			for ( Vertex u : users ) {
				u_inc_id = ConvUtility.getIncId( users_inc_id_map, u.getId().toString() ).toString();
				trans_prob_user = new Float( (1 / new Float(ConvUtility.getDegree(u, "friend").toString())) * (1 - magic_constant) ).toString();
				
				
				// get the _user_ like (here Direction.OUT is enough) 
				user_likes = u.getVertices(Direction.BOTH, "likes");

				
				// edge <user, friend> 
				for ( Vertex f : u.getVertices(Direction.BOTH, "friend") ) {
					// NB: in this (INC Cluster) case we must add the two edge, user1-->user2 and user2-->user1
					//     so no head-tail condition is checked
					line_user.append(ConvUtility.format(SEPARATOR, u_inc_id, 
							                            ConvUtility.getIncId(users_inc_id_map, f.getId().toString()).toString(), 
							                            trans_prob_user));
				}
				
				// ********* second bunch of writes *********
				// write into dataset.txt file and empty the line_user 
				bw_dataset.write(line_user.toString(), 0, line_user.length());
				line_user.setLength(0);

				
				// !!! un nodo che non presenta un certo attributo???? 
				// !!! devo metterlo per forza oppure puo' non esserci quell'attributo?				
				
				// attribute format: attribute_name=attribute_value
				attrs_value.add(UserUtility.AGE+"=" + u.getProperty(UserUtility.AGE).toString());
				 
				attrs_value.add(UserUtility.GENDER+"="  + u.getProperty(UserUtility.GENDER).toString());
				attrs_value.add(UserUtility.INTERESTED_IN+"=" + u.getProperty(UserUtility.INTERESTED_IN).toString());
				
				attrs_value.add(UserUtility.HOMETOWN+"=" + u.getProperty(UserUtility.HOMETOWN).toString());
				attrs_value.add(UserUtility.LOCATION+"=" + u.getProperty(UserUtility.LOCATION).toString());
				attrs_value.add(UserUtility.REL_STATUS+"=" + u.getProperty(UserUtility.REL_STATUS).toString());
				
				attrs_value.add(UserUtility.HIGH_SCHOOL+"=" + u.getProperty(UserUtility.HIGH_SCHOOL).toString());
				attrs_value.add(UserUtility.COLLEGE+"=" + u.getProperty(UserUtility.COLLEGE).toString());
				attrs_value.add(UserUtility.GRADUATE_SCHOOL+"=" + u.getProperty(UserUtility.GRADUATE_SCHOOL).toString());
				
				int i = 1;      // index counter useful to keep track of the increment id used in ___attributes__ table file
				Float occ ;     // occurrence of a particular attribute
				
				trans_prob_attr = new Float(magic_constant / (attrs_value.size() + g.statistics.getLikeCount()) ).toString();
				
				for (String attr : attrs_value) {
					
					res = ConvUtility.getIncIdHMS(attr_inc_id_map, attr, users_number, attrs_occ, null);
					
					a_inc_id = res.toString();
					
					line_user.append(ConvUtility.format(SEPARATOR, u_inc_id, 
							                            a_inc_id,
							                            trans_prob_attr)); 

					// set line_attr for the attributes table file
					line_attr.append(ConvUtility.format(SEPARATOR, u_inc_id, new Integer(i++).toString(), attr));
					
					// get the occurrence of the couple <attribute name, attribute value>
					// attribute name => attr.split("=")[0], attribute value => attr.split("=")[1]
					occ = new Float(attrs_value_map.get(attr.split("=")[0]).get(attr.split("=")[1]).toString());
					
					line_attr_user.append(ConvUtility.format(SEPARATOR, a_inc_id, u_inc_id, 
							                                 new Float(1 / occ).toString()));
				}
				
				// ********* second bunch of writes *********
				
				// write into dataset file and empty the line_user 
				bw_dataset.write(line_user.toString(), 0, line_user.length());
				line_user.setLength(0);
				
				// write into attribute file and empty the line_attr
				bw_attr.write(line_attr.toString(), 0, line_attr.length());
				line_attr.setLength(0);

			
				int like_degree;
				boolean isin;
				
				for (Vertex l : likes) {
					String l_id = l.getId().toString();
					like_degree = ConvUtility.getDegree(l, "likes");
					
					isin = ConvUtility.contains(user_likes, l);
					
					String like_value = l_id + "=" + (isin ? "1" : "0");
					
					res = ConvUtility.getIncIdHMS(likes_inc_id_map, like_value, l_start_from, null, 2);
					l_inc_id = res.toString();
					
					line_user.append(ConvUtility.format(SEPARATOR, u_inc_id, 
							                                       l_inc_id,
							                                       trans_prob_attr));
					
					line_attr.append(ConvUtility.format(SEPARATOR, u_inc_id, new Integer(i++).toString(), like_value));
					
					occ = new Float(isin ? like_degree : (users_number - like_degree));  
					line_attr_user.append(ConvUtility.format(SEPARATOR, l_inc_id, u_inc_id,
															 new Float(1 / occ).toString()));
				}
				
				attrs_value.clear();
				
				// ********* third bunch of writes *********
				
				// write into dataset file and empty the line_user 
				bw_dataset.write(line_user.toString(), 0, line_user.length());
				line_user.setLength(0);
				
				// write into  attribute file and empty the line_attr
				bw_attr.write(line_attr.toString(), 0, line_attr.length());
				line_attr.setLength(0);
			} //users FOR
			
			//!!!this can be maybe splitted in various for-in writes
			//!!!the only effect is to have the edges attr---user not at the end of file
			//!!!here only for debug purposes
			bw_dataset.write(line_attr_user.toString(), 0, line_attr_user.length());

				
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		File inputFolder = new File("/home/np2k/Desktop/json.debug");
		File[] files = inputFolder.listFiles();
		
		UsersGraph g = new UsersGraph();
	
		UserPuker j;
		
		for (File f : files) {
			j = new  UserPuker(f.getAbsolutePath());
			System.out.println(f.getAbsolutePath());
			g.addUser(j.getEUser());			
		}
		
		new INCClusterConverter().translate(g);

	}
}
