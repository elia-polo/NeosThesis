package converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.Vector;

import main.UserUtility;
import main.UsersGraph;
import utils.Util;

import com.google.code.geocoder.model.LatLng;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import facebook.RelationshipStatus;

public class MOCConverter implements Converter {

	private static final String asset_folder = "./assets/MOC/";
	static {
		new File(asset_folder).mkdirs();
	}
	
	private static final int G_MALE = 1;
	private static final int G_FEMALE = 2;
	private static final int AGE = 3;
	private static final int RS_SINGLE = 4;
	private static final int RS_IN_A_RELATIONSHIP = 5;
	private static final int RS_ENGAGED = 6;
	private static final int RS_MARRIED = 7;
	private static final int RS_IN_AN_OPEN_RELATIONSHIP = 8;
	private static final int RS_ITS_COMPLICATED = 9;
	private static final int RS_SEPARATED = 10;
	private static final int RS_DIVORCED = 11;
	private static final int RS_WIDOWED = 12;
	private static final int II_MALE = 13;
	private static final int II_FEMALE = 14;
	private static final int HOMETOWN_LATITUDE = 15;
	private static final int HOMETOWN_LONGITUDE = 16;
	private static final int LOCATION_LATITUDE = 17;
	private static final int LOCATION_LONGITUDE = 18;
	private static final int E_HIGH_SCHOOL = 19;
	private static final int E_COLLEGE = 20;
	private static final int E_GRADUATE_SCHOOL = 21;

	private static final int PROFILE_ATTRIBUTES = 21;
	
	public static enum DATA_DENSITY {
		DENSE, SPARSE;
	}
	
//	private DATA_DENSITY density = DATA_DENSITY.SPARSE;
	private boolean fromIgraph;
	
	public MOCConverter(boolean fromIgraph) {
		this.fromIgraph = fromIgraph;
	}
	
//	public void setDataDensity(DATA_DENSITY density) {
//		this.density = density;
//	}
	
	/**
	 * <p>Converts a UsersGraph into a dataset file suitable for the MOC clustering algorithm.</p>
	 * The expected input is a matrix <b>M</b> with <b>N</b> rows and <b>D</b> columns, where N is the number of samples and D the number of features.<br>
	 * The element M<sub>ij</sub> is the j<sup>th</sup> feature of the i<sup>th</sup> element of the dataset.<br>
	 * Numeric attributes are kept numeric, while categoric attributes are converted to a binary representation.
	 * This effectively increases the number of dimensions by the size of the domain of each attribute (which is dominated by the number of like entities in the dataset)<br>
	 * The output file depends on the dimensionality of the data.<br>
	 * For dense data, it is a sequence of TSV (Tab Separated Values) lines, each one associated to a row of the data matrix.
	 * All rows have the same number of elements, in the same order.
	 * For sparse data, the matrix is encoded as a list of coordinates (COO format), producing a three-column file whose
	 * first column is a list of row indices, second column is a list of column indices, and third column is a list of nonzero values.<br>
	 * The columns are as follows:<br>
	 * <ol>
	 * <li>GENDER {MALE, FEMALE}</li>
	 * <li>AGE</li>
	 * <li>RELATIONSHIP STATUS {SINGLE, IN_A_RELATIONSHIP, ENGAGED, MARRIED, IN_AN_OPEN_RELATIONSHIP, ITS_COMPLICATED, SEPARATED, DIVORCED, WIDOWED}</li>
	 * <li>INTERESTED IN {MALE, FEMALE}</li>
	 * <li>HOMETOWN - LATITUDE</li>
	 * <li>HOMETOWN - LONGITUDE</li>
	 * <li>LOCATION - LATITUDE</li>
	 * <li>LOCATION - LONGITUDE</li>
	 * <li>EDUCATION {HIGH SCHOOL,COLLEGE,GRADUATE SCHOOL}</li>
	 * <li>LIKES</li>
	 * </ol>
	 * This means that column 1 is GENDER_MALE, column 2 is GENDER_FEMALE, column 3 is AGE, column 4 is RELATIONSHIP_SINGLE and so on.<br>
	 * Among all categorical attributes, GENDER and RELATIONSHIP are exclusive while INTERESTED IN and EDUCATION allow multiple values simultaneously
	 * @param g The UsersGraph to be converted
	 */
	public void translate(UsersGraph g) {
		translate(g.getGraph());
	}

	/**
	 * <p>Scans the vertex information looking for the relevant attributes.</p>
	 * 
	 * @param v a graph vertex
	 * @param profile a map of attribute indices and attribute values. The map is filled with values from the given vertex
	 */
	private void loadProfile(Vertex v, Collection<Map.Entry<Integer,Object>> profile) {
		String s = v.getProperty(UserUtility.GENDER);
		if(s != null && !s.equals("null")) {
			switch(s) {
			case "male":
				profile.add(new SimpleEntry<Integer,Object>(G_MALE, "1"));
				break;
			case "female":
				profile.add(new SimpleEntry<Integer,Object>(G_FEMALE, "1"));
				break;
			}
		}
		s = v.getProperty(UserUtility.BIRTHDAY);
		if(s != null && !s.equals("null")) {
			profile.add(new SimpleEntry<Integer,Object>(AGE, Util.getAge((String) v.getProperty(UserUtility.BIRTHDAY))));
		}
		if(fromIgraph) {
			s = (String) v.getProperty(UserUtility.REL_STATUS);
		} else {
			s = (String) v.getProperty(UserUtility.REL_STATUS);
		}
		if(s != null && !s.equals("null")) {
			switch(s) {
			case RelationshipStatus.SINGLE:
				profile.add(new SimpleEntry<Integer,Object>(RS_SINGLE, "1"));
				break;
			case RelationshipStatus.IN_A_RELATIONSHIP:
				profile.add(new SimpleEntry<Integer,Object>(RS_IN_A_RELATIONSHIP, "1"));
				break;
			case RelationshipStatus.ENGAGED:
				profile.add(new SimpleEntry<Integer,Object>(RS_ENGAGED, "1"));
				break;
			case RelationshipStatus.MARRIED:
				profile.add(new SimpleEntry<Integer,Object>(RS_MARRIED, "1"));
				break;
			case RelationshipStatus.IN_AN_OPEN_RELATIONSHIP:
				profile.add(new SimpleEntry<Integer,Object>(RS_IN_AN_OPEN_RELATIONSHIP, "1"));
				break;
			case RelationshipStatus.ITS_COMPLICATED:
				profile.add(new SimpleEntry<Integer,Object>(RS_ITS_COMPLICATED, "1"));
				break;
			case RelationshipStatus.SEPARATED:
				profile.add(new SimpleEntry<Integer,Object>(RS_SEPARATED, "1"));
				break;
			case RelationshipStatus.DIVORCED:
				profile.add(new SimpleEntry<Integer,Object>(RS_DIVORCED, "1"));
				break;
			case RelationshipStatus.WIDOWED:
				profile.add(new SimpleEntry<Integer,Object>(RS_WIDOWED, "1"));
				break;
			}
		}
		if(fromIgraph) {
			s = v.getProperty(UserUtility.INTERESTED_IN.replace("_", ""));
		} else {
			s = v.getProperty(UserUtility.INTERESTED_IN);
		}
		if(s != null && !s.equals("null")) {
			switch(s) {
			case "male":
				profile.add(new SimpleEntry<Integer,Object>(II_MALE, "1"));
				break;
			case "female":
				profile.add(new SimpleEntry<Integer,Object>(II_FEMALE, "1"));
				break;
			}
		}
		s = v.getProperty(UserUtility.HOMETOWN_NAME);
		if(s != null && !s.equals("null")) {
			LatLng coords = Util.getCoordinates(s);
			if(coords != null) {
				profile.add(new SimpleEntry<Integer,Object>(HOMETOWN_LATITUDE, coords.getLat()));
				profile.add(new SimpleEntry<Integer,Object>(HOMETOWN_LONGITUDE, coords.getLng()));
			} // else imputation is required TODO
		}
		s = v.getProperty(UserUtility.LOCATION_NAME);
		if(s != null && !s.equals("null")) {
			LatLng coords = Util.getCoordinates(s);
			if(coords != null) {
				profile.add(new SimpleEntry<Integer,Object>(LOCATION_LATITUDE, coords.getLat()));
				profile.add(new SimpleEntry<Integer,Object>(LOCATION_LONGITUDE, coords.getLng()));
			}
		}
		if(fromIgraph) {
			s = v.getProperty(UserUtility.HIGHSCHOOL.replace("_", ""));
		} else {
			s = v.getProperty(UserUtility.HIGHSCHOOL);
		}
		if(s != null && !s.equals("null")) {
			profile.add(new SimpleEntry<Integer,Object>(E_HIGH_SCHOOL, s));
		}
		if(fromIgraph) {
			s = v.getProperty(UserUtility.COLLEGE.replace("_", ""));
		} else {
			s = v.getProperty(UserUtility.COLLEGE);
		}
		if(s != null && !s.equals("null")) {
			profile.add(new SimpleEntry<Integer,Object>(E_COLLEGE, s));
		}
		if(fromIgraph) {
			s = v.getProperty(UserUtility.GRADUATESCHOOL.replace("_", ""));
		} else {
			s = v.getProperty(UserUtility.GRADUATESCHOOL);
		}
		if(s != null && !s.equals("null")) {
			profile.add(new SimpleEntry<Integer,Object>(E_GRADUATE_SCHOOL, s));
		}
	}
	
	private int like_id;
	
	private NavigableSet<Integer> loadLikes(Vertex v, Map<String,Integer> like_map) {
		NavigableSet<Integer> user_likes= new TreeSet<Integer>();
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
		return user_likes;
	}
	
	private void translateSparse(Graph g) {
		try (BufferedWriter users = Files.newBufferedWriter(Paths.get(asset_folder+"users.txt"), StandardCharsets.UTF_8);
				BufferedWriter dataset = Files.newBufferedWriter(Paths.get(asset_folder+"dataset.txt"), StandardCharsets.UTF_8)) {
			like_id = PROFILE_ATTRIBUTES; // Start from PROFILE_ATTRIBUTES-1 for 0-based indices
			Map<String,Integer> like_map = new LinkedHashMap<String,Integer>();
			// Rows and columns are indexed starting from 1
			int user_row = 0;
			for (Vertex v : g.getVertices(UserUtility.WHOAMI,"user")) {
				users.write(v.getId()+"\t"+String.valueOf(++user_row)+System.lineSeparator()); // Starting from 0 and incrementing if row and column indices start from 1 (start from -1 for 0-based indices)
				Collection<Map.Entry<Integer, Object>> profile = new Vector<Map.Entry<Integer, Object>>(PROFILE_ATTRIBUTES);
				loadProfile(v, profile);
				
				StringBuilder sb = new StringBuilder();
				for(Map.Entry<Integer, Object> p : profile) { // row column value
					sb.append(user_row).append("\t").append(p.getKey().toString()).append("\t").append(p.getValue().toString()).append(System.lineSeparator());
				}
				dataset.write(sb.toString());
				
				NavigableSet<Integer> user_likes = loadLikes(v, like_map);
				for(Integer like : user_likes) {
					dataset.write(String.valueOf(user_row)+"\t"+like.toString()+"\t1"+System.lineSeparator()); // row(user) column(like) 1(does like)
				}
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
	}

	@Override
	public void translate(Graph g) {
		long start_time = System.currentTimeMillis();
		// Clear folder
		for(File file: new File(asset_folder).listFiles()) file.delete();
		translateSparse(g);
		System.out.println("Total running time: "+(System.currentTimeMillis() - start_time)+" ms");
	}
}
