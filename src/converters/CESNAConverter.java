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

public class CESNAConverter implements Converter {

	private static final String asset_folder = "/home/np2k/Desktop/CESNA/";
	
	private static final String edges_filename = "net.edges";
	private static final String feat_filename = "net.feat";
	private static final String featname_filename = "net.featname";
	
	private static final char SEPARATOR = '\t';

	/**
	 * CESNA algorithm takes three input files: net.edges, net.feat, net.featnames
	 * 
	 * - net.edges: is simply a list of double-edges between two user id: id1 id2...id2 id1  
	 * - net.feat: is a table where each row represent a couple (user_id feature_value_id)
	 *             e.g. (nicola,gender=male)  ===> 1  3
	 *                      (elia,gender=male)    ===> 2  3
	 *                      (nicola,hometown=sgr) ===> 1  4
	 * - net.featnames: is a matching table between the feature_value_id and the feature's value
	 *                  e.g. 3   gender=male
	 *                       4   hometown=sgr
	 *                          
	 * general idea: 
	 *    * for each user U
	 *      * for each user friend F
	 *         - create an edge U F in net.edges (without no head-tail check)
	 *         
	 *      * for each not missing U's attribute A
	 *         - create a couple U id(value(A)) in the file net.nodefeat
	 *         - if not already in, create a couple id(value(A)) value(A)   
	 *     
	 *  
	 * 
	 * @param g The UsersGraph to be converted
	 * */
	@SuppressWarnings("unused")
	public void translate(UsersGraph g) {
	
		// opening the three input files for write
		try (BufferedWriter bw_edges    = Files.newBufferedWriter(Paths.get(asset_folder+edges_filename), StandardCharsets.UTF_8);
		     BufferedWriter bw_feat     = Files.newBufferedWriter(Paths.get(asset_folder+feat_filename), StandardCharsets.UTF_8);
		     BufferedWriter bw_featname = Files.newBufferedWriter(Paths.get(asset_folder+featname_filename), StandardCharsets.UTF_8)) {

			// map to store the users incremental id  
			Graph graph = g.getGraph();
			
			
			// user facebook id
			String u_id = new String();
			String a_inc_id = new String();			
			
			
			// working str to build the output files
			StringBuilder line_edges = new StringBuilder();
			StringBuilder line_feat = new StringBuilder();
			StringBuilder line_featname  = new StringBuilder();
			
			// attributes values array 
			ArrayList<String> attrs_value = new ArrayList<String>();
			
			HashMap<String, Integer> attrs_inc_id_map = new HashMap<String, Integer>();

			Iterable<Vertex> user_likes;
			
			
			// get the whole user set 
			Iterable<Vertex> users = graph.getVertices(UserUtility.WHOAMI, "user");
			
			for (Vertex u : users) {
				
				// get user fb id
				u_id = u.getId().toString();
				
				// get user friends and append <user_id friend_id> in line_edges string   
				for (Vertex f : u.getVertices(Direction.BOTH, "friend"))
					line_edges.append(ConvUtility.format(SEPARATOR, u_id, f.getId().toString()));
				
				//////////////////////////FARE UNA FUNZIONE usato anche in INC/////////////////////////////////////
				
				// attribute format: attribute_name=attribute_value
				
				attrs_value = ConvUtility.fillAttributeList(u);
				
				for (String v : attrs_value) {
					// get the incremental attribute-value id
					a_inc_id = ConvUtility.getIncId(attrs_inc_id_map, v).toString();
					
					// append the couple <user_id, attribute-value_id> to line_feat
					line_feat.append(ConvUtility.format(SEPARATOR, u_id, a_inc_id));
				}
				
				for (Vertex l : u.getVertices(Direction.BOTH, "likes")) {
					// get the incremental like-id
					a_inc_id = ConvUtility.getIncId(attrs_inc_id_map, "like="+l.getId().toString()).toString();
					
					// append the couple <user_id, like-id> to line_feat
					line_feat.append(ConvUtility.format(SEPARATOR, u_id, a_inc_id));					
				}
				
				bw_edges.write(line_edges.toString(), 0, line_edges.length());
				line_edges.setLength(0);
				
				bw_feat.write(line_feat.toString(), 0, line_feat.length());
				line_feat.setLength(0);
				
				attrs_value.clear();
			}
			
			for (String v : attrs_inc_id_map.keySet()) {
				line_featname.append(ConvUtility.format(SEPARATOR, attrs_inc_id_map.get(v).toString(),v));
			}
			bw_featname.write(line_featname.toString(), 0, line_featname.length());
			line_featname.setLength(0);

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
			j = new UserPuker(f.getAbsolutePath());
			System.out.println(f.getAbsolutePath());
			System.out.println(j.getEUser().getId().toString());
			g.addUser(j.getEUser());			
		}
		new CESNAConverter().translate(g);
	}
}
