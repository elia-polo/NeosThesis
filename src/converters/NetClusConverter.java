package converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import main.UserUtility;
import main.UsersGraph;
import utils.Util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class NetClusConverter implements Converter {

	private static final int male = 0;
	private static final int female = 1;
	private static final int male_and_female = 2;
	private static final int high_school = 0;
	private static final int college = 1;
	private static final int graduate_school = 2;
	private static final String asset_folder = "./assets/Netclus/";
	static {
		new File(asset_folder).mkdirs();
	}
	/**
	 * Converts a UsersGraph into a dataset file suitable for the NetClus clustering algorithm:
	 * <ul>
	 * <li>each Node type (EUser, ELike) and Node or Edge property (e.g. Birthday, Hometown) is converted to an Entity
	 * <li>each Edge instance is a relation. There exist as many Relations as <Node type, Node type> pairs connected by an edge in the original graph (e.g. &lt;EUser-Euser&gt;, &lt;EUser-ELike&gt;) and <Node, Node property> pairs, where the Node property is being represented as an Entity (e.g. &lt;EUser-Birthday&gt;, &lt;EUser-Hometown&gt;)
	 * <li>a file is created for each Entity and Relation
	 * <li>each Node instance and Node or Edge property value is assigned a unique (among homogenous values, that is a node instance and node property instance may share the same id) integer id
	 * <li>the Node instance and Node or Edge property value is described as a string
	 * <li>Entity files are tab separated values (TSV) lines of &lt;Entity_id,Entity_value&gt; pairs; the first entity must be parsable to integer, the second entity must be parsable to string
	 * <li>Relation files are tab separated values (TSV) lines of &lt;Entity_id,Entity_id&gt; pairs; both entities must be parsable to integer
	 * </ul>
	 * @param g The UsersGraph to be converted
	 */
	public void translate(UsersGraph g) {
		long start_time = System.currentTimeMillis();
		// Clear folder
		for(File file: new File(asset_folder).listFiles()) file.delete();
		// Maps for remapping dataset values with unique integer keys
		HashMap<Integer, Integer> age_map = new HashMap<Integer,Integer>();
		HashMap<String, Integer> hometown_map = new HashMap<String, Integer>();
		HashMap<String, Integer> location_map = new HashMap<String, Integer>();
		HashMap<String, Integer> relationship_map = new HashMap<String, Integer>();
		HashMap<String, Integer> like_map = new HashMap<String, Integer>();
		try (BufferedWriter users = Files.newBufferedWriter(Paths.get(asset_folder+"user.txt"), StandardCharsets.UTF_8);
				BufferedWriter user2birthday = Files.newBufferedWriter(Paths.get(asset_folder+"user2birthday.txt"), StandardCharsets.UTF_8);
				BufferedWriter user2hometown = Files.newBufferedWriter(Paths.get(asset_folder+"user2hometown.txt"), StandardCharsets.UTF_8);
				BufferedWriter user2location = Files.newBufferedWriter(Paths.get(asset_folder+"user2location.txt"), StandardCharsets.UTF_8);
				BufferedWriter user2gender = Files.newBufferedWriter(Paths.get(asset_folder+"user2gender.txt"), StandardCharsets.UTF_8);
				BufferedWriter user2interest = Files.newBufferedWriter(Paths.get(asset_folder+"user2interest.txt"), StandardCharsets.UTF_8);
				BufferedWriter user2relationship = Files.newBufferedWriter(Paths.get(asset_folder+"user2relationship.txt"), StandardCharsets.UTF_8);
				BufferedWriter user2education = Files.newBufferedWriter(Paths.get(asset_folder+"user2education.txt"), StandardCharsets.UTF_8);
				BufferedWriter user2like = Files.newBufferedWriter(Paths.get(asset_folder+"user2like.txt"), StandardCharsets.UTF_8)) {
			// For each graph node of type use, declare an entity
			int user_id = -1, age_id = -1, hometown_id = -1, location_id = -1, relationship_id = -1, like_id = -1;
			for (Vertex v : g.getGraph().getVertices(UserUtility.WHOAMI,"user")) {
				// Insert user into users file
				String s = String.valueOf(++user_id)+"\t"+v.getId().toString()+System.lineSeparator();
				users.write(s);
				// Insert user-birthday relation
				String o = v.getProperty(UserUtility.BIRTHDAY);
				if(o != null && !o.equals("null")) {
					Integer user_age = new Integer(Util.getAge(o));
					Integer age_remapped_id;
					if((age_remapped_id = age_map.get(user_age)) == null) {
						age_map.put(user_age, ++age_id);
						age_remapped_id = age_id;
					}
					s = String.valueOf(user_id)+"\t"+age_remapped_id+System.lineSeparator();
					user2birthday.write(s);
				}
				o = v.getProperty(UserUtility.HOMETOWN);
				if(o != null && !o.equals("null")) {
					Integer hometown_remapped_id;
					if((hometown_remapped_id = hometown_map.get(o)) == null) {
						hometown_map.put(o, ++hometown_id);
						hometown_remapped_id = hometown_id;
					}
					s = String.valueOf(user_id)+"\t"+hometown_remapped_id+System.lineSeparator();
					user2hometown.write(s);
				}
				o = v.getProperty(UserUtility.LOCATION);
				if(o != null && !o.equals("null")) {
					Integer location_remapped_id;
					if((location_remapped_id = location_map.get(o)) == null) {
						location_map.put(o, ++location_id);
						location_remapped_id = location_id;
					}
					s = String.valueOf(user_id)+"\t"+location_remapped_id+System.lineSeparator();
					user2location.write(s);
				}
				o = v.getProperty(UserUtility.GENDER);
				if(o != null && !o.equals("null")) {
					s = String.valueOf(user_id)+"\t"+(o.equals("male")?String.valueOf(male):String.valueOf(female))+System.lineSeparator();
					user2gender.write(s);
				}
				o = v.getProperty(UserUtility.INTERESTED_IN);
				if(o != null && !o.equals("null")) {
					int interest_id;
					if(o.equals("male")) {
						interest_id = male;
					} else if(o.equals("female")) {
						interest_id = female;
					} else {
						interest_id = male_and_female;
					}
					s = String.valueOf(user_id)+"\t"+String.valueOf(interest_id)+System.lineSeparator();
					user2interest.write(s);
				}
				o = v.getProperty(UserUtility.REL_STATUS);
				if(o != null && !o.equals("null")) {
					Integer relationship_remapped_id;
					if((relationship_remapped_id = relationship_map.get(o)) == null) {
						relationship_map.put(o, ++relationship_id);
						relationship_remapped_id = relationship_id;
					}
					s = String.valueOf(user_id)+"\t"+relationship_remapped_id+System.lineSeparator();
					user2relationship.write(s);
				}
				o = v.getProperty(UserUtility.HIGH_SCHOOL);
				if(o != null) {
					s = String.valueOf(user_id)+"\t"+String.valueOf(high_school)+System.lineSeparator();
					user2education.write(s);
				}
				o = v.getProperty(UserUtility.COLLEGE);
				if(o != null) {
					s = String.valueOf(user_id)+"\t"+String.valueOf(college)+System.lineSeparator();
					user2education.write(s);
				}
				o = v.getProperty(UserUtility.GRADUATE_SCHOOL);
				if(o != null) {
					s = String.valueOf(user_id)+"\t"+String.valueOf(graduate_school)+System.lineSeparator();
					user2education.write(s);
				}
				// Now add to the file for the user2like relation (relations among homogenous entities are not allowed by this algorithm)
				for (Edge e : v.getEdges(Direction.OUT, UserUtility.LIKES)) {
					// A like edge is always (tail) user -> like (head). Edge.getVertex(Direction) returns the tail/out or head/in vertex.
					Vertex l = e.getVertex(Direction.IN);
					Integer like_remapped_id;
					if((like_remapped_id = like_map.get(l.getId())) == null) {
						like_map.put(l.getId().toString(), ++like_id);
						like_remapped_id = like_id;
					}
					s = user_id+"\t"+like_remapped_id.toString()+System.lineSeparator();
					user2like.write(s);
				}
			}
		} catch (IOException e) {
		    System.err.format("IOException: %s%n", e);
		}
		// Now make a file for each remaining entity
		try (BufferedWriter gender = Files.newBufferedWriter(Paths.get(asset_folder+"gender.txt"), StandardCharsets.UTF_8)) {
			String s = male+"\tmale"+System.lineSeparator()+female+"\tfemale";
			gender.write(s);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		if(!age_map.isEmpty()) {
			try (BufferedWriter age = Files.newBufferedWriter(Paths.get(asset_folder+"birthday.txt"), StandardCharsets.UTF_8)) {
				for (Map.Entry<Integer, Integer> entry : age_map.entrySet()) {
					String s = entry.getValue()+"\t"+entry.getKey()+System.lineSeparator();
					age.write(s);
				}
			} catch (IOException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
		if(!hometown_map.isEmpty()) {
			try (BufferedWriter hometown = Files.newBufferedWriter(Paths.get(asset_folder+"hometown.txt"), StandardCharsets.UTF_8)) {
				for (Map.Entry<String, Integer> entry : hometown_map.entrySet()) {
					String s = entry.getValue()+"\t"+entry.getKey()+System.lineSeparator();
					hometown.write(s);
				}
			} catch (IOException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
		if(!location_map.isEmpty()) {
			try (BufferedWriter location = Files.newBufferedWriter(Paths.get(asset_folder+"location.txt"), StandardCharsets.UTF_8)) {
				for (Map.Entry<String, Integer> entry : location_map.entrySet()) {
					String s = entry.getValue()+"\t"+entry.getKey()+System.lineSeparator();
					location.write(s);
				}
			} catch (IOException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
		try(BufferedWriter interested_in = Files.newBufferedWriter(Paths.get(asset_folder+"interested_in.txt"), StandardCharsets.UTF_8)) {
			String s = male+"\tmale"+System.lineSeparator()+female+"\tfemale"+System.lineSeparator()+male_and_female+"\tmale_female";
			interested_in.write(s);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		if(!relationship_map.isEmpty()) {
			try(BufferedWriter relationship = Files.newBufferedWriter(Paths.get(asset_folder+"relationship.txt"), StandardCharsets.UTF_8)){
				for (Map.Entry<String, Integer> entry : relationship_map.entrySet()) {
					String s = entry.getValue()+"\t"+entry.getKey()+System.lineSeparator();
					relationship.write(s);
				}
			} catch (IOException e) {
				System.err.format("IOException: %s%n", e);
			}
		}
		try (BufferedWriter education = Files.newBufferedWriter(Paths.get(asset_folder+"education.txt"), StandardCharsets.UTF_8)) {
			String s = high_school+"\thigh_school"+System.lineSeparator()+college+"\tcollege"+System.lineSeparator()+graduate_school+"\tgraduate_school";
			education.write(s);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
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
		// Finally delete empty files
		for(File file: new File(asset_folder).listFiles()) if(file.length() == 0) {file.delete(); }
		System.out.println("Total running time: "+(System.currentTimeMillis() - start_time)+" ms");
	}
}
