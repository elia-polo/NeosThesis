package converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import main.UserUtility;
import main.UsersGraph;
import utils.Util;

import com.google.code.geocoder.model.LatLng;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import facebook.RelationshipStatus;

public class LACConverter implements Converter {

	private static final String asset_folder = "./assets/LAC/";
	static {
		new File(asset_folder).mkdirs();
	}
	
	private static final int G_MALE = 0;
	private static final int G_FEMALE = 1;
	private static final int AGE = 2;
	private static final int RS_SINGLE = 3;
	private static final int RS_IN_A_RELATIONSHIP = 4;
	private static final int RS_ENGAGED = 5;
	private static final int RS_MARRIED = 6;
	private static final int RS_IN_AN_OPEN_RELATIONSHIP = 7;
	private static final int RS_ITS_COMPLICATED = 8;
	private static final int RS_SEPARATED = 9;
	private static final int RS_DIVORCED = 10;
	private static final int RS_WIDOWED = 11;
	private static final int II_MALE = 12;
	private static final int II_FEMALE = 13;
	private static final int HOMETOWN_LATITUDE = 14;
	private static final int HOMETOWN_LONGITUDE = 15;
	private static final int LOCATION_LATITUDE = 16;
	private static final int LOCATION_LONGITUDE = 17;
	private static final int E_HIGH_SCHOOL = 18;
	private static final int E_COLLEGE = 19;
	private static final int E_GRADUATE_SCHOOL = 20;

	private static final int PROFILE_ATTRIBUTES = 21;
	
	private boolean fromIgraph = false;
	
	public LACConverter(boolean fromIgraph) {
		this.fromIgraph = fromIgraph;
	}
	
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
		long start_time = System.currentTimeMillis();
		// Clear folder
		for(File file: new File(asset_folder).listFiles()) file.delete();
		translateDense(g.getGraph(), g.statistics.getLikeCount());
		System.out.println("Total running time: "+(System.currentTimeMillis() - start_time)+" ms");
	}

	/**
	 * <p>Scans the vertex information looking for the relevant attributes.</p>
	 * 
	 * @param v a graph vertex
	 * @param profile a map of attribute indices and attribute values. The map is filled with values from the given vertex
	 */
	private void loadProfile(Vertex v, Object[] profile) {
		if(profile.length < PROFILE_ATTRIBUTES)
			throw new IllegalArgumentException("The supplied array length is less than the number of profile attributes (0"+PROFILE_ATTRIBUTES+")");
		for(int i=0; i<profile.length; ++i) {
			profile[i] = 0;
		}
		String s = v.getProperty(UserUtility.GENDER);
		if(s != null && !s.equals("null")) {
			switch(s) {
			case "male":
				profile[G_MALE] = 1;
				break;
			case "female":
				profile[G_FEMALE] = 1;
				break;
			}
		}
		s = (String) v.getProperty(UserUtility.BIRTHDAY);
		if(s != null && !s.equals("null")) {
			profile[AGE] = Util.getAge(s);
		}
		if(fromIgraph) {
			s = (String) v.getProperty(UserUtility.REL_STATUS);
		} else {
			s = (String) v.getProperty(UserUtility.REL_STATUS);
		}
		if(s != null && !s.equals("null")) {
			switch(s) {
			case RelationshipStatus.SINGLE:
				profile[RS_SINGLE] = 1;
				break;
			case RelationshipStatus.IN_A_RELATIONSHIP:
				profile[RS_IN_A_RELATIONSHIP] = 1;
				break;
			case RelationshipStatus.ENGAGED:
				profile[RS_ENGAGED] = 1;
				break;
			case RelationshipStatus.MARRIED:
				profile[RS_MARRIED] = 1;
				break;
			case RelationshipStatus.IN_AN_OPEN_RELATIONSHIP:
				profile[RS_IN_AN_OPEN_RELATIONSHIP] = 1;
				break;
			case RelationshipStatus.ITS_COMPLICATED:
				profile[RS_ITS_COMPLICATED] = 1;
				break;
			case RelationshipStatus.SEPARATED:
				profile[RS_SEPARATED] = 1;
				break;
			case RelationshipStatus.DIVORCED:
				profile[RS_DIVORCED] = 1;
				break;
			case RelationshipStatus.WIDOWED:
				profile[RS_WIDOWED] = 1;
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
				profile[II_MALE] = 1;
				break;
			case "female":
				profile[II_FEMALE] = 1;
				break;
			}
		}
		s = v.getProperty(UserUtility.HOMETOWN_NAME);
		if(s != null && !s.equals("null")) {
			LatLng coords = Util.getCoordinates(s);
			if(coords != null) {
				profile[HOMETOWN_LATITUDE] = coords.getLat();
				profile[HOMETOWN_LONGITUDE] = coords.getLng();
			} // else imputation is required TODO
		}
		s = v.getProperty(UserUtility.LOCATION_NAME);
		if(s != null && !s.equals("null")) {
			LatLng coords = Util.getCoordinates(s);
			if(coords != null) {
				profile[LOCATION_LATITUDE] = coords.getLat();
				profile[LOCATION_LONGITUDE] = coords.getLng();
			}
		}
		if(fromIgraph) {
			s = v.getProperty(UserUtility.HIGHSCHOOL.replace("_", ""));
		} else {
			s = v.getProperty(UserUtility.HIGHSCHOOL);
		}
		if(s != null && !s.equals("null")) {
			profile[E_HIGH_SCHOOL] = s;
		}
		if(fromIgraph) {
			s = v.getProperty(UserUtility.COLLEGE.replace("_", ""));
		} else {
			s = v.getProperty(UserUtility.COLLEGE);
		}
		if(s != null && !s.equals("null")) {
			profile[E_COLLEGE] = s;
		}
		if(fromIgraph) {
			s = v.getProperty(UserUtility.GRADUATESCHOOL.replace("_", ""));
		} else {
			s = v.getProperty(UserUtility.GRADUATESCHOOL);
		}
		if(s != null && !s.equals("null")) {
			profile[E_GRADUATE_SCHOOL] = s;
		}
	}
	
	private int like_id;
	
	private NavigableSet<Integer> loadLikes(Vertex v, Map<String,Integer> like_map) {
		NavigableSet<Integer> user_likes = new TreeSet<Integer>();
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
	
	private void translateDense(Graph g, int likes_count) {
		try (BufferedWriter users = Files.newBufferedWriter(Paths.get(asset_folder+"users.txt"), StandardCharsets.UTF_8);
				BufferedWriter dataset = Files.newBufferedWriter(Paths.get(asset_folder+"dataset.txt"), StandardCharsets.UTF_8)) {
			like_id = PROFILE_ATTRIBUTES - 1; // Start from PROFILE_ATTRIBUTES-1 for 0-based indices
			Map<String,Integer> like_map = new HashMap<String,Integer>();
			int count = 0;
//			StringBuilder sb = new StringBuilder();
			for (Vertex v : g.getVertices(UserUtility.WHOAMI,"user")) {
				System.out.println(++count);
				String s = v.getId()+"\t";
				users.write(s+System.lineSeparator());
				Object[] profile = new Object[PROFILE_ATTRIBUTES];
				loadProfile(v, profile);
				s = Util.toXSV(profile, "\t");
				dataset.write(s);
//				sb.append(s);
				
				// Likes are dealt with this way:
				// Collect all likes for the current user, insert them in the likes map if necessary and into a set, identifying them by the unique index provided by the map.
				// Then produce a TSV string.
				// Assuming the current user has 5 likes, with index (starting from 0) respectively 4, 7, 12, 15, 19 and the total number of likes is 25, the following should occur:
				// Keep a position index starting at 0.
				// Compare the index with the current like in the like set, which at the beginning is 4.
				// 4-0 is 4, therefore output a string made of 4 '\t'
				// Output the like identifier of 4 followed by a '\t'. Advance the index by 1 to 5
				// Compute the new difference, using the next like, which is 7. 7 - 5 is 2, therefore output a string made of 2 '\t'.
				// Output the like identifier of 7 followed by a '\t'. Advance the index to 8
				// When the last like has been processed, compute the difference between the total number of likes and the current index, then output as many '\t'
				dataset.write("\t");
//				sb.append("\t");
				if(likes_count != 0) {
					NavigableSet<Integer> user_likes = loadLikes(v, like_map);
					int index = 0;
					while(index < likes_count) {
						Integer like = user_likes.pollFirst();
						if(like != null) {
							like = like - PROFILE_ATTRIBUTES; // 0-based now
							// Fill the gap between index and like
							s = new String(new char[like-index]).replace("\0", "0\t")+"1"+"\t"; // create String of n times '0'+'\t' followed by the current like and terminated by '\t'
							dataset.write(s);
//							sb.append(s);
							index = like + 1;
						} else { // Fill the range between index and likes_count with '\t'
							s = new String(new char[likes_count-index]).replace("\0", "0\t"); // create String of n times '\t'
							dataset.write(s);
//							sb.append(s);
							break;
						}
					}
				}
				s = "0"+System.lineSeparator(); // 0 is a default class label
				dataset.write(s);
//				sb.append(s);
			}
//			dataset.write(sb.toString());
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
	
//	private void translateSparse(Graph g) {
//		try (BufferedWriter users = Files.newBufferedWriter(Paths.get(asset_folder+"users.txt"), StandardCharsets.UTF_8);
//				BufferedWriter dataset = Files.newBufferedWriter(Paths.get(asset_folder+"dataset.txt"), StandardCharsets.UTF_8)) {
//			like_id = PROFILE_ATTRIBUTES; // Start from PROFILE_ATTRIBUTES-1 for 0-based indices
//			Map<String,Integer> like_map = new LinkedHashMap<String,Integer>();
//			// Rows and columns are indexed starting from 1
//			int user_row = 0;
//			for (Vertex v : g.getVertices(UserUtility.WHOAMI,"user")) {
//				users.write(v.getId()+"\t"+String.valueOf(++user_row)+System.lineSeparator()); // Starting from 0 and incrementing if row and column indices start from 1 (start from -1 for 0-based indices)
//				Collection<Map.Entry<Integer, Object>> profile = new Vector<Map.Entry<Integer, Object>>(PROFILE_ATTRIBUTES);
//				loadProfile(v, profile);
//				
//				StringBuilder sb = new StringBuilder();
//				for(Map.Entry<Integer, Object> p : profile) { // row column value
//					sb.append(user_row).append("\t").append(p.getKey().toString()).append("\t").append(p.getValue().toString()).append(System.lineSeparator());
//				}
//				dataset.write(sb.toString());
//				
//				NavigableSet<Integer> user_likes = loadLikes(v, like_map);
//				for(Integer like : user_likes) {
//					dataset.write(String.valueOf(user_row)+"\t"+like.toString()+"\t1"+System.lineSeparator()); // row(user) column(like) 1(does like)
//				}
//			}
//			if(!like_map.isEmpty()) {
//				try(BufferedWriter like = Files.newBufferedWriter(Paths.get(asset_folder+"like.txt"), StandardCharsets.UTF_8)){
//					for (Map.Entry<String, Integer> entry : like_map.entrySet()) {
//						String s = entry.getValue()+"\t"+entry.getKey()+System.lineSeparator();
//						like.write(s);
//					}
//				} catch (IOException e) {
//					System.err.format("IOException: %s%n", e);
//				}			
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	@Override
	public void translate(Graph g) {
		long start_time = System.currentTimeMillis();
		// Clear folder
		for(File file: new File(asset_folder).listFiles()) file.delete();
		int likes_count = 0;
		Iterable<Vertex> v = g.getVertices(UserUtility.WHOAMI, "like");
		if(v instanceof Collection) {
			likes_count = ((Collection<Vertex>) v).size();
		} else {
			for(@SuppressWarnings("unused") Vertex l : v) {
				++likes_count;
			}
		}
		translateDense(g, likes_count);
		System.out.println("Total running time: "+(System.currentTimeMillis() - start_time)+" ms");
	}
}
