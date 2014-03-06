package converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import main.UserUtility;
import main.UsersGraph;
import utils.Util;

import com.google.code.geocoder.model.LatLng;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class ELKIConverter implements Converter {

	private static final String asset_folder = "./assets/ELKI/";
	static {
		new File(asset_folder).mkdirs();
	}
	
	/**
	 * <p>Converts a graph to a format suitable for the ELKI knowledge data discovery framework.</p>
	 * The output file is consistent with the format expected by the TermFrequencyParser class in ELKI<br>
	 * Each user is associated to a list of attributes in sparse format. Attributes come in pairs of the format "non-numeric numeric" where the non-numeric part is the column identifier and the other is the associated attribute value, parsable to double
	 * 
	 * @param g The UsersGraph to be converted
	 */
	public void translate(UsersGraph g) {
		long start_time = System.currentTimeMillis();
		// Clear folder
		for(File file: new File(asset_folder).listFiles()) file.delete();
		
		try (BufferedWriter dataset = Files.newBufferedWriter(Paths.get(asset_folder+"dataset.txt"), StandardCharsets.UTF_8)) {
			Map<String,Integer> like_map = new LinkedHashMap<String,Integer>();
			int like_id = -1;
			for (Vertex v : g.getGraph().getVertices(UserUtility.WHOAMI,"user")) {
				Vector<String> user_data = new Vector<String>();
				user_data.add("ID_"+(String)v.getId()); // Starting object label
				String s = v.getProperty(UserUtility.GENDER);
				if(s != null && !s.equals("null")) {
					user_data.add("GENDER_"+s+" 1");
				}
				s = Util.getAge((String) v.getProperty(UserUtility.BIRTHDAY));
				if(s != null && !s.equals("null")) {
					user_data.add("AGE "+s);
				}
				s = v.getProperty(UserUtility.REL_STATUS);
				if(s != null && !s.equals("null")) {
					user_data.add("RS_"+s+" 1");
				}
				s = v.getProperty(UserUtility.INTERESTED_IN);
				if(s != null && !s.equals("null")) {
					user_data.add("INTERESTED_IN_"+s+" 1");
				}
				s = v.getProperty(UserUtility.HOMETOWN_NAME);
				if(s != null && !s.equals("null")) {
					LatLng coords = Util.getCoordinates(s);
					if(coords != null) {
						user_data.add("HOMETOWN_LATITUDE "+coords.getLat());
						user_data.add("HOMETOWN_LONGITUDE "+coords.getLng());
					} // else imputation is required TODO
				}
				s = v.getProperty(UserUtility.LOCATION_NAME);
				if(s != null && !s.equals("null")) {
					LatLng coords = Util.getCoordinates(s);
					if(coords != null) {
						user_data.add("LOCATION_LATITUDE "+coords.getLat());
						user_data.add("LOCATION_LONGITUDE "+coords.getLng());
					}
				}
				s = v.getProperty(UserUtility.HIGH_SCHOOL);
				if(s != null && !s.equals("null")) {
					user_data.add("HIGH_SCHOOL "+s);
				}
				s = v.getProperty(UserUtility.COLLEGE);
				if(s != null && !s.equals("null")) {
					user_data.add("COLLEGE "+s);
				}
				s = v.getProperty(UserUtility.GRADUATE_SCHOOL);
				if(s != null && !s.equals("null")) {
					user_data.add("GRADUATE_SCHOOL "+s);
				}
	
				TreeSet<Integer> user_likes = new TreeSet<Integer>();
				for (Edge e : v.getEdges(Direction.OUT, UserUtility.LIKES)) {
					// A like edge is always (tail) user -> like (head). Edge.getVertex(Direction) returns the tail/out or head/in vertex.
					Vertex l = e.getVertex(Direction.IN);
					Integer like_remapped_id;
					if((like_remapped_id = like_map.get(l.getId())) == null) {
						like_map.put(l.getId().toString(), ++like_id);
						like_remapped_id = like_id;
					}
					user_likes.add(like_remapped_id);
				}
				for(Integer like : user_likes) {
					user_data.add("LIKE_"+like.toString()+" 1"); // row(user) column(like) 1(does like)
				}
				// Write row to file
				dataset.write(Util.toXSV(user_data, " "));
				dataset.write(System.lineSeparator());
			}
			if(!like_map.isEmpty()) {
				try(BufferedWriter like = Files.newBufferedWriter(Paths.get(asset_folder+"like.txt"), StandardCharsets.UTF_8)){
					for (Map.Entry<String, Integer> entry : like_map.entrySet()) {
						String s = entry.getValue()+"\t"+entry.getKey()+System.lineSeparator();
						like.write(s);
					}
				} catch (IOException e) {
					System.err.format("IOException: %s%n", e);
				}			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Total running time: "+(System.currentTimeMillis() - start_time)+" ms");
	}

}
