package converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.TreeSet;

import main.UserUtility;
import main.UsersGraph;
import utils.Util;

import com.google.code.geocoder.model.LatLng;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import facebook.RelationshipStatus;

public class MOCConverter implements Converter {

	private static final String asset_folder = "./assets/MOC/";
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
	
	/**
	 * <p>Converts a UsersGraph into a dataset file suitable for the MOC clustering algorithm.</p>
	 * The expected input is a matrix <b>M</b> with <b>N</b> rows and <b>D</b> columns, where N is the number of samples and D the number of features.<br>
	 * The element M<sub>ij</sub> is the j<sup>th</sup> feature of the i<sup>th</sup> element of the dataset.<br>
	 * Numeric attributes are kept numeric, while categoric attributes are converted to a binary representation.
	 * This effectively increases the number of dimensions by the size of the domain of each attribute (which is dominated by the number of like entities in the dataset)<br>
	 * The output file is a sequence of TSV (Tab Separated Values) lines, each one associated to a row of the data matrix. All rows have the same number of elements, in the same order.
	 * The columns are as follows:<br>
	 * <ol>
	 * <li>GENDER {MALE, FEMALE}</li>
	 * <li>RELATIONSHIP STATUS {SINGLE, IN_A_RELATIONSHIP, ENGAGED, MARRIED, IN_AN_OPEN_RELATIONSHIP, ITS_COMPLICATED, SEPARATED, DIVORCED, WIDOWED}</li>
	 * <li>INTERESTED IN {MALE, FEMALE}</li>
	 * <li>HOMETOWN - LATITUDE</li>
	 * <li>HOMETOWN - LONGITUDE</li>
	 * <li>LOCATION - LATITUDE</li>
	 * <li>LOCATION - LONGITUDE</li>
	 * <li>EDUCATION {HIGH SCHOOL,COLLEGE,GRADUATE SCHOOL}</li>
	 * <li>LIKES</li>
	 * </ol>
	 * This means that column 1 is ID, column 2 is GENDER_MALE, column 3 is GENDER_FEMALE, column 4 is RELATIONSHIP_SINGLE and so on.<br>
	 * Among all categorical attributes, GENDER and RELATIONSHIP are exclusive while INTERESTED IN and EDUCATION allow multiple values simultaneously
	 * @param g The UsersGraph to be converted
	 */
	public void translate(UsersGraph g) {
		long start_time = System.currentTimeMillis();
		// Clear folder
		for(File file: new File(asset_folder).listFiles()) file.delete();
		try (BufferedWriter users = Files.newBufferedWriter(Paths.get(asset_folder+"users.txt"), StandardCharsets.UTF_8);
				BufferedWriter dataset = Files.newBufferedWriter(Paths.get(asset_folder+"dataset.txt"), StandardCharsets.UTF_8)) {
			HashMap<String,Integer> like_map = new HashMap<String,Integer>();
			final int likes_count = g.statistics.getLikeCount();
			int like_id = -1;
			for (Vertex v : g.getGraph().getVertices(UserUtility.WHOAMI,"user")) {
				String s = v.getId()+"\t";
				users.write(s,0,s.length());
				Object[] profile = new Object[PROFILE_ATTRIBUTES];
				for(int i=0; i<profile.length; ++i) {
					profile[i] = "0";
				}
				switch(v.getProperty(UserUtility.GENDER).toString()) {
				case "male":
					profile[G_MALE] = "1";
					break;
				case "female":
					profile[G_FEMALE] = "1";
					break;
				}
				profile[AGE] = Util.getAge(v.getProperty(UserUtility.BIRTHDAY).toString());
				s = v.getProperty(UserUtility.REL_STATUS).toString();
				if(s != null && !s.equals("null")) {
					switch(s) {
					case RelationshipStatus.SINGLE:
						profile[RS_SINGLE] = "1";
						break;
					case RelationshipStatus.IN_A_RELATIONSHIP:
						profile[RS_IN_A_RELATIONSHIP] = "1";
						break;
					case RelationshipStatus.ENGAGED:
						profile[RS_ENGAGED] = "1";
						break;
					case RelationshipStatus.MARRIED:
						profile[RS_MARRIED] = "1";
						break;
					case RelationshipStatus.IN_AN_OPEN_RELATIONSHIP:
						profile[RS_IN_AN_OPEN_RELATIONSHIP] = "1";
						break;
					case RelationshipStatus.ITS_COMPLICATED:
						profile[RS_ITS_COMPLICATED] = "1";
						break;
					case RelationshipStatus.SEPARATED:
						profile[RS_SEPARATED] = "1";
						break;
					case RelationshipStatus.DIVORCED:
						profile[RS_DIVORCED] = "1";
						break;
					case RelationshipStatus.WIDOWED:
						profile[RS_WIDOWED] = "1";
						break;
					}
				}
				switch(v.getProperty(UserUtility.INTERESTED_IN).toString()) {
				case "male":
					profile[II_MALE] = "1";
					break;
				case "female":
					profile[II_FEMALE] = "1";
					break;
				}
				s = v.getProperty(UserUtility.HOMETOWN).toString();
				if(s != null && !s.equals("null")) {
					LatLng coords = Util.getCoordinates(s);
					if(coords != null) {
						profile[HOMETOWN_LATITUDE] = coords.getLat();
						profile[HOMETOWN_LONGITUDE] = coords.getLng();
					} // else imputation is required TODO
				}
				s = v.getProperty(UserUtility.LOCATION).toString();
				if(s != null && !s.equals("null")) {
					LatLng coords = Util.getCoordinates(s);
					profile[LOCATION_LATITUDE] = coords.getLat();
					profile[LOCATION_LONGITUDE] = coords.getLng();
				}
				String[] vec = Util.fromXSV(v.getProperty(UserUtility.EDUCATION).toString(), ",");
				profile[E_HIGH_SCHOOL] = vec[0];
				profile[E_COLLEGE] = vec[1];
				profile[E_GRADUATE_SCHOOL] = vec[2];
				s = Util.toXSV(profile, "\t");
				dataset.write(s, 0, s.length());
				
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
				int index = 0;
				while(index < likes_count) {
					Integer like = user_likes.pollFirst();
					if(like != null) {
						// Fill the gap between index and like
						s = new String(new char[like-index]).replace("\0", "\t")+like+"\t"; // create String of n times '\t' followed by the current like and terminated by '\t'
						dataset.write(s, 0, s.length());
						index = like +1;
					} else { // Fill the range between index and likes_count with '\t'
						s = new String(new char[likes_count-index]).replace("\0", "\t"); // create String of n times '\t'
						dataset.write(s, 0, s.length());
						break;
					}
				}
				s = System.lineSeparator();
				dataset.write(s, 0, s.length());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Total running time: "+(System.currentTimeMillis() - start_time)+" ms");
	}

}
