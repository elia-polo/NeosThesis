package metrics;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import main.EEducation;
import main.UserPuker;
import main.UserUtility;
import utils.Util;
import utils.Util.GeocoderAddressComponentType;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.gml.GMLWriter;

public class UserStats {
	/**
	 * Map of &lt;attribute &lt;name,value&gt; , attribute count&gt; computed over the neighbours of this user, for all attributes Entry.name such that their value is missing for this user
	 */
	private HashMap<String, HashMap<Object,Integer>> stats;
	
	private List<SimpleEntry<String,Float>> missing_ratios;
	
	public UserStats() {
		stats = new HashMap<String, HashMap<Object,Integer>>();
	}
	
	private static final boolean debug = false;
	private static final String null_key = "null";
	private static final String valid_user = "VALID";
	
	/**
	 * Compute the map of &lt;attribute value, attribute count&gt; pairs for the neighbours of the given node
	 * @param v
	 */
	public static UserStats evaluate(Vertex v) {
		// Iterate over all relevant properties, until a missing value is found
		Vector<String> missing = new Vector<String>();
		UserStats result = new UserStats();
		String s = v.getProperty(UserUtility.GENDER);
		if(isNull(s)) {
			missing.add(UserUtility.GENDER);
			HashMap<Object, Integer> tmp = new HashMap<Object,Integer>();
			tmp.put(null_key, 0);
			result.stats.put(UserUtility.GENDER, tmp);
		}
		s = v.getProperty(UserUtility.REL_STATUS);
		if(isNull(s)) {
			missing.add(UserUtility.REL_STATUS);
			HashMap<Object, Integer> tmp = new HashMap<Object,Integer>();
			tmp.put(null_key, 0);
			result.stats.put(UserUtility.REL_STATUS, tmp);
		}
		s = v.getProperty(UserUtility.INTERESTED_IN);
		if(isNull(s)) {
			missing.add(UserUtility.INTERESTED_IN);
			HashMap<Object, Integer> tmp = new HashMap<Object,Integer>();
			tmp.put(null_key, 0);
			result.stats.put(UserUtility.INTERESTED_IN, tmp);
		}
		s = v.getProperty(UserUtility.BIRTHDAY);
		if(isNull(s)) {
			missing.add(UserUtility.BIRTHDAY);
			HashMap<Object, Integer> tmp = new HashMap<Object,Integer>();
			tmp.put(null_key, 0);
			result.stats.put(UserUtility.BIRTHDAY, tmp);
		}
		s = v.getProperty(UserUtility.HOMETOWN);
		if(isNull(s)) {
			missing.add(UserUtility.HOMETOWN);
			HashMap<Object, Integer> tmp = new HashMap<Object,Integer>();
			tmp.put(null_key, 0);
			result.stats.put(UserUtility.HOMETOWN, tmp);
		}
		s = v.getProperty(UserUtility.LOCATION);
		if(isNull(s)) {
			missing.add(UserUtility.LOCATION);
			HashMap<Object, Integer> tmp = new HashMap<Object,Integer>();
			tmp.put(null_key, 0);
			result.stats.put(UserUtility.LOCATION, tmp);
		}
		s = v.getProperty(UserUtility.EDUCATION);
		if(isNull(s)) {
			missing.add(UserUtility.EDUCATION);
			HashMap<Object, Integer> tmp = new HashMap<Object,Integer>();
			tmp.put(null_key, 0);
			result.stats.put(UserUtility.EDUCATION, tmp);
		}
		// Navigate the neighbours
		Iterable<Vertex> neighbours = v.getVertices(Direction.BOTH, UserUtility.FRIEND);
		int neighbour_count = 0;
		for(Vertex n : neighbours) {
			++neighbour_count;
			for(String m : missing) { // For each missing attribute, if a neighbour has value v for that attribute, increment the count of v in the map 
				s = n.getProperty(m);
				if(!isNull(s)) {
					Integer value = result.stats.get(m).get(s);
					if(value == null) {
						result.stats.get(m).put(s, 1);
					} else {
						result.stats.get(m).put(s, value + 1);
					}
				} else {
					result.stats.get(m).put(null_key, result.stats.get(m).get(null_key) + 1);
				}
			}
		}
		result.missing_ratios = new ArrayList<SimpleEntry<String,Float>>();
		for(Entry<String, HashMap<Object,Integer>> k : result.stats.entrySet()) {
			// Find the null entry if it exists in the inner map
			SimpleEntry<String,Float> entry = new SimpleEntry<String,Float>(k.getKey(), (float) 0);
			for(Entry<Object, Integer> e : k.getValue().entrySet()) {
				if(e.getKey().equals(null_key)) {
					entry.setValue((float)100.0*(float)e.getValue()/(float)neighbour_count);
					break;
				}
			}
			result.missing_ratios.add(entry);
		}
		return result;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Entry<String, HashMap<Object,Integer>> k : stats.entrySet()) {
			sb.append(k.getKey()).append(System.lineSeparator());
			for(Entry<Object, Integer> e : k.getValue().entrySet()) {
				sb.append("\t").append(e.getKey().toString()).append(" = ").append(e.getValue()).append(System.lineSeparator());
			}
		}
		return sb.toString();
	}
	
	public String summary() {
		DecimalFormat df = new DecimalFormat("#0.#");
		StringBuilder sb = new StringBuilder();
		for(Entry<String, Float> e : missing_ratios) {
			sb.append(e.getKey().toString()).append(" : ").append(df.format(e.getValue())).append("%").append(System.lineSeparator());
		}
		return sb.toString();
	}
	
	private static boolean isNull(String o) {
//		return o == null || o.equals("null");
		return o == null;
	}
	
	private static String asString(JsonValue jsonValue) {
		if(jsonValue == null)
			return null;
		return jsonValue.asString();
	}
	
	private static String[] getEduVec(ArrayList<EEducation> edu) {
		String[] tmp = {"0","0","0"};
		if(edu != null) {
			for (EEducation e : edu) {
				if(e == null || e.getType() == null) continue;
				switch (e.getType()) {
				case UserUtility.HIGH_SCHOOL:
					tmp[0] = "1";
					break;
				case UserUtility.COLLEGE:
					tmp[1] = "1";
					break;
				case UserUtility.GRADUATE_SCHOOL:
					tmp[2] = "1";
					break;
				default:
					throw new IllegalArgumentException("Education type "
							+ e.getType() + " does not exist");
				}
			}
		}
		// Upper levels of education (College) imply lower levels (HighSchool)
		for(int i=tmp.length-1; i>=0; --i) {
			if(tmp[i].equals("1")) {
				--i;
				while(i>=0) {
					tmp[i] = "1";
				}
			}
		}
		return tmp;
	}
	
	private static String getInterestedIn(JsonValue jv) {
		if(jv == null) {
			return null;
		} else {
			return asString(jv.asArray().get(0));
		}
	}
	
	private static String[] getHometown(JsonValue jv) {
		if(jv == null) {
			return null;
		} else {
			return new String[] {asString(jv.asObject().get(UserUtility.ID)), asString(jv.asObject().get(UserUtility.NAME)) };
		}
	}
	
	private static String[] getLocation(JsonValue jv) {
		if(jv == null) {
			return null;
		} else {
			return new String[] {asString(jv.asObject().get(UserUtility.ID)), asString(jv.asObject().get(UserUtility.NAME)) };
		}
	}
	
	public static Graph loadFromJson() {
		// Build graph from Json files
		Graph graph = new TinkerGraph();
//		File[] files = new File("/home/np2k/Desktop/repository").listFiles();
		File[] files = new File("C:\\Programming\\Git\\json_users").listFiles();
		for (File f : files) {
			try (BufferedReader json = Files.newBufferedReader(f.toPath(),StandardCharsets.UTF_8)) {
				JsonObject jsonObj = JsonObject.readFrom(json);
				String s;
				s = jsonObj.get(UserUtility.ID).asString();
				if (debug) {
					System.out.println(UserUtility.ID + " " + s);
				}
				Vertex user;
				try {
					user = graph.addVertex(s);
				} catch (IllegalArgumentException e) {
					user = graph.getVertex(s);
				}
				user.setProperty(valid_user, true); // Only users associated to a file are kept
				user.setProperty(UserUtility.WHOAMI, "user");
				user.setProperty(UserUtility.ISDAU, f.getName().contains("DAU"));
				s = asString(jsonObj.get(UserUtility.GENDER));
				if (debug) {
					System.out.println(UserUtility.GENDER + " " + s);
				}
				if (s != null) {
					user.setProperty(UserUtility.GENDER, s);
				}
				s = asString(jsonObj.get(UserUtility.REL_STATUS));
				if (debug) {
					System.out.println(UserUtility.REL_STATUS + " " + s);
				}
				if (s != null) {
					user.setProperty(UserUtility.REL_STATUS, s);
				}
				s = getInterestedIn(jsonObj.get(UserUtility.INTERESTED_IN));
				if (debug) {
					System.out.println(UserUtility.INTERESTED_IN + " " + s);
				}
				if (s != null) {
					user.setProperty(UserUtility.INTERESTED_IN, s);
				}
				s = asString(jsonObj.get(UserUtility.BIRTHDAY));
				if (s != null) {
					String[] split = s.split("/");
					if (split.length < 3)
						s = null;
					else
						s = split[2];
				}
				if (debug) {
					System.out.println(UserUtility.BIRTHDAY + " " + s);
				}
				if (s != null) {
					user.setProperty(UserUtility.BIRTHDAY, s);
				}
				String vec[] = getHometown(jsonObj.get(UserUtility.HOMETOWN));
				if (debug) {
					System.out.println(UserUtility.HOMETOWN + " " + vec[1]);
				}
				if (s != null) {
					user.setProperty(UserUtility.HOMETOWN, vec[0]);
					user.setProperty(UserUtility.HOMETOWN_NAME, vec[1]);
					// Set additional properties
					try {
						Map<String,Object> result = Util.getAddressStructure(vec[1]);
						if(result != null) {
							user.setProperty(UserUtility.HOMETOWN+"_"+GeocoderAddressComponentType.administrative_area_level_2, result.get(GeocoderAddressComponentType.administrative_area_level_2));
							user.setProperty(UserUtility.HOMETOWN+"_"+GeocoderAddressComponentType.administrative_area_level_1, result.get(GeocoderAddressComponentType.administrative_area_level_1));
							user.setProperty(UserUtility.HOMETOWN+"_"+GeocoderAddressComponentType.country, result.get(GeocoderAddressComponentType.country));
						}
					} catch(IllegalStateException e) {
						System.err.println(e.getMessage());
					}
				}
				vec = getLocation(jsonObj.get(UserUtility.LOCATION));
				if (debug) {
					System.out.println(UserUtility.LOCATION + " " + vec[1]);
				}
				if (s != null) {
					user.setProperty(UserUtility.LOCATION, vec[0]);
					user.setProperty(UserUtility.LOCATION_NAME, vec[1]);
					// Set additional properties
					try {
						Map<String,Object> result = Util.getAddressStructure(vec[1]);
						if(result != null) {
							user.setProperty(UserUtility.LOCATION+"_"+GeocoderAddressComponentType.administrative_area_level_2, result.get(GeocoderAddressComponentType.administrative_area_level_2));
							user.setProperty(UserUtility.LOCATION+"_"+GeocoderAddressComponentType.administrative_area_level_1, result.get(GeocoderAddressComponentType.administrative_area_level_1));
							user.setProperty(UserUtility.LOCATION+"_"+GeocoderAddressComponentType.country, result.get(GeocoderAddressComponentType.country));
						}
					} catch(IllegalStateException e) {
						System.err.println(e.getMessage());
					}
				}
				vec = getEduVec(UserPuker.parseEducationNoTryCatch(jsonObj));
				if (debug) {
					System.out.println(UserUtility.HIGH_SCHOOL + " " + vec[0]);
					System.out.println(UserUtility.COLLEGE + " " + vec[1]);
					System.out.println(UserUtility.GRADUATE_SCHOOL + " " + vec[2]);
				}
				user.setProperty(UserUtility.HIGHSCHOOL, vec[0]);
				user.setProperty(UserUtility.COLLEGE, vec[1]);
				user.setProperty(UserUtility.GRADUATESCHOOL, vec[2]);
				// Add friends, node and edges
				ArrayList<String> friends = UserPuker.parseFriends(jsonObj,(Boolean) user.getProperty(UserUtility.ISDAU));
				if (friends != null) {
					for (String fr : friends) {
						Vertex friend;
						try {
							friend = graph.addVertex(fr);
						} catch (IllegalArgumentException e) {
							friend = graph.getVertex(fr);
						}
						String edge_id;
						if (user.getId().toString().compareTo(friend.getId().toString()) > 0)
							edge_id = user.getId().toString() + "-" + friend.getId().toString();
						else
							edge_id = friend.getId().toString() + "-" + user.getId().toString();

						try {
							graph.addEdge(edge_id, user, friend,UserUtility.FRIEND);
						} catch (IllegalArgumentException e) { /* do nothing */
						}
					}
				}
			} catch (IOException e) {
				System.err.println("IO Exception in main\n" + e.getMessage());
			}
		}
		// Scan the graph looking for users without the VALID flag
		int count = 0;
		for(Vertex v : graph.getVertices()) {
			if(v.getProperty(valid_user) == null) {
				++count;
				graph.removeVertex(v);
			} else {
				v.removeProperty(valid_user);
			}
		}
		System.out.println("Removed "+count+" nodes. No property file was available");
		return graph;
	}
	
	public static void main(String[] args) {
		Graph graph = new TinkerGraph();
		graph = loadFromJson();
//		try {
//			GMLReader.inputGraph(graph, "./graph_backup.gml");
//		} catch (IOException e1) {
//			e1.printStackTrace();
//			return;
//		}
		// Save gml of the graph
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("./graph.gml"))){
			GMLWriter.outputGraph(graph,bos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (BufferedWriter stat_file = Files.newBufferedWriter(Paths.get("./stats.txt"), StandardCharsets.UTF_8);
				BufferedWriter stat_summary = Files.newBufferedWriter(Paths.get("./stats_summary.txt"), StandardCharsets.UTF_8)) {
			Iterable<Vertex> users = graph.getVertices();
			for (Vertex v : users) {
				String s = "User " + v.getId()+System.lineSeparator();
				stat_file.write(s);
				stat_summary.write(s);
				UserStats stat = UserStats.evaluate(v);
				stat_summary.write(stat.summary());
				stat_file.write(stat.toString());
				stat_file.write(System.lineSeparator()+System.lineSeparator());
				stat_summary.write(System.lineSeparator()+System.lineSeparator());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}