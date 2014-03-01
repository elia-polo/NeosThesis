package metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import main.EEducation;
import main.UserPuker;
import main.UserUtility;
import utils.Util;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class UserStats {
	/**
	 * Map of &lt;attribute &lt;name,value&gt; , attribute count&gt; computed over the neighbours of this user, for all attributes Entry.name such that their value is missing for this user
	 */
	private HashMap<String, HashMap<Object,Integer>> stats;
	
	public UserStats() {
		stats = new HashMap<String, HashMap<Object,Integer>>();
	}
	
	private static final boolean debug = false;
	private static final String null_key = "null";
	
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
		for(Vertex n : neighbours) {
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
		for(EEducation e: edu) {
			switch(e.getType()) {
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
				throw new IllegalArgumentException("Education type "+e.getType()+" does not exist");
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
	
	private static String getHometown(JsonValue jv) {
		if(jv == null) {
			return null;
		} else {
			return asString(jv.asObject().get(UserUtility.ID));
		}
	}
	
	private static String getLocation(JsonValue jv) {
		if(jv == null) {
			return null;
		} else {
			return asString(jv.asObject().get(UserUtility.ID));
		}
	}
	
	public static void main(String[] args) {
		// Build graph from Json files
		Graph graph = new TinkerGraph();
		File[] files = new File("./assets/json.debug").listFiles();
		for(File f : files) {
			try (BufferedReader json = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)){
				JsonObject jsonObj = JsonObject.readFrom(json);
				String s;
				s = jsonObj.get(UserUtility.ID).asString();
				if(debug) {if(debug) {System.out.println(UserUtility.ID+" "+s);}}
				Vertex user;
				try {
					user = graph.addVertex(s);
				} catch (IllegalArgumentException e) { user = graph.getVertex(s); }
				user.setProperty(UserUtility.ISDAU, f.getName().contains("DAU"));
				s = asString(jsonObj.get(UserUtility.GENDER));
				if(debug) {System.out.println(UserUtility.GENDER+" "+s);}
				if (s != null) {
					user.setProperty(UserUtility.GENDER, s);
				}
				s = asString(jsonObj.get(UserUtility.REL_STATUS));
				if(debug) {System.out.println(UserUtility.REL_STATUS+" "+s);}
				if (s != null) {
					user.setProperty(UserUtility.REL_STATUS, s);
				}
				s = getInterestedIn(jsonObj.get(UserUtility.INTERESTED_IN));
				if(debug) {System.out.println(UserUtility.INTERESTED_IN+" "+s);}
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
				if(debug) {System.out.println(UserUtility.BIRTHDAY+" "+s);}
				if (s != null) {
					user.setProperty(UserUtility.BIRTHDAY, s);
				}
				s = getHometown(jsonObj.get(UserUtility.HOMETOWN));
				if(debug) {System.out.println(UserUtility.HOMETOWN+" "+s);}
				if (s != null) {
					user.setProperty(UserUtility.HOMETOWN, s);
				}
				s = getLocation(jsonObj.get(UserUtility.LOCATION));
				if(debug) {System.out.println(UserUtility.LOCATION+" "+s);}
				if (s != null) {
					user.setProperty(UserUtility.LOCATION, s);
				}
				s = Util.toXSV(getEduVec(UserPuker.parseEducation(jsonObj)),",");
				if(debug) {System.out.println(UserUtility.EDUCATION+" "+s);}
				if (s != null) {
					user.setProperty(UserUtility.EDUCATION, s);
				}
				// Add friends, node and edges
				ArrayList<String> friends = UserPuker.parseFriends(jsonObj, (Boolean) user.getProperty(UserUtility.ISDAU));
				for(String fr : friends) {
					Vertex friend;
					try {
						friend = graph.addVertex(fr);
					} catch (IllegalArgumentException e) { friend = graph.getVertex(fr); }
					String edge_id;
					if (user.getId().toString().compareTo(friend.getId().toString()) > 0) 
						edge_id = user.getId().toString()+ "-" + friend.getId().toString();
					else
						edge_id = friend.getId().toString() + "-" + user.getId().toString() ;
					
					try {
						graph.addEdge(edge_id, user, friend, UserUtility.FRIEND);
					} catch (IllegalArgumentException e) { /* do nothing */ }
				}
			} catch (IOException e) {
				System.err.println("IO Exception in main\n" + e.getMessage());
			}
		}
		Iterable<Vertex> users = graph.getVertices();
		for(Vertex v : users) {
			System.out.println("User " + v.getId());
			System.out.println(UserStats.evaluate(v).toString()+"\n");
		}
	}
}
