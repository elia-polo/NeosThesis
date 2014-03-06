package converters;

import java.util.ArrayList;
import java.util.HashMap;

import main.UserUtility;
import utils.Util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;


/**
 * HashMapWL (With Last) extends HashMap to achieve
 * the goal of storing the last inserted element.
 * 
 * NB: the last stored element is intended to be the __value__
 * of the last entry
 *  
 * */
class HashMapWL<K,V> extends HashMap<K, V> {
	private static final long serialVersionUID = 1L;
	private V last;
	
	public V put(K key, V value) {
		this.last = value;
		return super.put(key, value);
	}
	public Object getLast() { return last; }
}


public class ConvUtility {
	
	/**
	 * return vertex degree (i.e. the sum of incoming and
	 * outcoming edges labeled with 'label' string)
	 * 
	 * @param v, vertex
	 * @return Integer, degree of the vertex parameter
	 * */
	@SuppressWarnings("unused")
	public static Integer getDegree(Vertex v, String label) {
		Integer i = 0;
		
		for ( Edge e : v.getEdges(Direction.BOTH, label) )
			i++;
		return i;
	}
	
	/**
	 * @return true if vertex v is in the Iterable c
	 *         false otherwise 
	 **/
	public static boolean contains(Iterable<Vertex> c, Vertex v) {
		for (Vertex e : c) {
			if (e.getId().toString().equals(v.getId().toString()))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets the incremental user id.
	 * If the 'id' has never been inserted into the hashmap then
	 * it is added to the hashmap with an incremental value (i.e
	 * the last incremental id + 1, starting from 'start_after').
	 * If the  'id' has already been inserted into the hashmap
	 * the matching incremental id is returned. 
	 * 
	 * @param map, the incremental user id hashmap
	 * @param id, the FB user id
	 * @return the incremental user id
	 * 
	 * */
	
	public static Integer getIncId(HashMap<String, Integer> map, String id, int start_after) {
		if (map.containsKey(id)) 
			return map.get(id);
		 else {
			Integer res = new Integer(map.size() + 1 + start_after);
			map.put(id, res);
			return res;
		 }
	}
	
	public static Integer getIncId(HashMap<String, Integer> map, String id) {
		return getIncId(map, id, 0);
	}
	
	/**
	 * The 'map' argument is an hashmap with a key String (e.g. containing the attributes name)
	 * and an hashmap as the value element. This inner hashmap is a with a key String (containing
	 * the couple attribute_name=attribute_value (NB: '='-separated)) and an Integer value
	 * representing the incremental __inner__ id of the specified couple. The inner id depends
	 * on the domains cardinality (a-priori known) of the attributes already present in the map.
	 * 
	 * _____Example_____
	 * 
	 * Let's have two attributes (age and gender). The cardinality of each attribute
	 * is specified by the attrs_cardinality argument, let's now suppose that is 5 
	 * for the age and 2 for the gender. Moreover, the couples attribute-value are: 
	 * age=young, age=old, gender=male, gender=female.
	 * 
	 * Suppose that the map already contains the age values: 
	 * {
	 *     _age_     { {age    = young, 1}, { age    = old,    2 }, { age = null, 3 }  }
	 * }
	 * 
	 * Now, arrive the gender values, so the map would be
	 * {
	 *   _age_     { {age    = young, 1}, { age    = old,    2 }, { age = null, 3 } , 
	 *   _gender_  { {gender = male,  6}, { gender = female, 5 } } 
	 * }
	 * 
	 * The first gender=value id is calculate as the sum of the already present attributes domains  
	 * cardinality (in these case only age with a card equal to 5) plus 1.
	 * 
	 * Let's moreover suppose to have a new incoming couple age=very_young the method updates
	 * the hashmap to:
	 * {
	 *   _age_     { ...... , {age=very_young, 4} }
	 *   ... 
	 * }
	 * and returs an Integer object equal to 4.
	 * 
	 * Note1: The method uses also a 'start_after' int parameter, in these case all the ids is
	 *        start_after-shifted (i.e. the first couple of the first attribute name begins  
	 *        from 'start_after + 1' and not from '1')
	 * 
	 * Note2: The method uses an Integer 'os' (OffSet) (mut. excl. to attrs_cardinality): in this way
	 *        it is likely to have a attrs_cardinality map with a fixed cardinality for each attributes. 
	 *      
	 *
	 * NB: in the above example the whitespacea between an attribute name, the '=' and the attribute value 
	 *     are used only for a graphical representation, the real format is name=value, without whitespace.
	 *     
	 * @param map
	 * @param couple
	 * @param start_after
	 * @param attrs_cardinality
	 * @param os
	 * 
	 * @return the incremental __inner__ id for couple name=value 
	 * @throws IllegalArgumentException in two case: 1. if 'attrs_cardinality' and 'os' equal to null.
	 *         2. if attrs_cardinality doesn't contain a key specified in map HashMap 
	 *         
	 * 
	 * */
	public static Integer getIncIdHMS(HashMap<String, HashMapWL<String, Integer>> map, 
										   String couple, 
										   int start_after, 
										   HashMap<String, Integer>attrs_cardinality, Integer os) 
												   throws IllegalArgumentException
	{
		if (attrs_cardinality == null && os == null) 
				throw new IllegalArgumentException("'attr_offset and' and 'os' parameters " +
		                                            "cannot be simultaneously equal to null.");
		String attr_name = couple.split("=")[0];
		
		HashMapWL<String, Integer> spec_map;
		Integer offset = new Integer(0);
		Integer res;

		if (map.containsKey(attr_name)) {
			spec_map = map.get(attr_name);
			
			if (spec_map.containsKey(couple)) {
				return spec_map.get(couple);
				
			} else {
				res = (Integer)spec_map.getLast() + 1;
				spec_map.put(couple, res );
				map.put(attr_name, spec_map);
				return res;
			}
		} else {
			//compute offset
			if (os == null) {
				for (String a : map.keySet()) 
					try {
						offset += attrs_cardinality.get(a); 
					} catch (NullPointerException e) {
						throw new IllegalArgumentException("no domain cardinality found for " +
					                                       a + " attribute.");
					}
			}
			else {
				offset = os * map.size();
			}
			
			spec_map = new HashMapWL<String, Integer>();
		
			res = 1 + start_after + offset;
			spec_map.put(couple, res);
			map.put(attr_name, spec_map);
			return res;
		}
	}
		
	/**
	 * given the set of string arguments the method returns 
	 * a String separated with the 'sep' character and
	 * ended with a newline
	 * 
	 *  @param sep, the strings separator
	 *  @param strings, the set of string arguments
	 *  
	 *  @return the string separated with 'sep' argument
	 * */
	public static String format( char sep, String... strings )
	{
		StringBuilder res = new StringBuilder();
		
		for ( String s : strings ) 
			res.append(s + sep);
		
		res.replace(res.length()-1, res.length(), System.lineSeparator());
	
		return res.toString();
	}
	
	/**
	 * */
	public static ArrayList<String> fillAttributeList(Vertex u) {
		ArrayList<String>attrs_value = new ArrayList<String>();
		
		String a;
		if ( (a = u.getProperty(UserUtility.AGE).toString()).equals("null") == false )
			attrs_value.add(UserUtility.AGE+"=" + Util.discretizeAge(new Integer(a)));
		if ( (a = u.getProperty(UserUtility.GENDER).toString()).equals("null") == false )
			attrs_value.add(UserUtility.GENDER+"=" + a);
		if ( (a = u.getProperty(UserUtility.INTERESTED_IN).toString()).equals("null") == false )
			attrs_value.add(UserUtility.INTERESTED_IN+"=" + a);
		if ( (a = u.getProperty(UserUtility.HOMETOWN).toString()).equals("null") == false )
			attrs_value.add(UserUtility.HOMETOWN+"=" + a);
		if ( (a = u.getProperty(UserUtility.LOCATION).toString()).equals("null") == false )
			attrs_value.add(UserUtility.LOCATION+"=" + a);
		if ( (a = u.getProperty(UserUtility.REL_STATUS).toString()).equals("null") == false )
			attrs_value.add(UserUtility.REL_STATUS+"=" + a);
		if ( (a = u.getProperty(UserUtility.HIGH_SCHOOL).toString()).equals("null") == false )
			attrs_value.add(UserUtility.HIGH_SCHOOL+"=" + a);
		if ( (a = u.getProperty(UserUtility.COLLEGE).toString()).equals("null") == false )
			attrs_value.add(UserUtility.COLLEGE+"=" + a);
		if ( (a = u.getProperty(UserUtility.GRADUATE_SCHOOL).toString()).equals("null") == false )
			attrs_value.add(UserUtility.GRADUATE_SCHOOL+"=" + a);
		
		return attrs_value;

	}
}


/*	public static Integer getIncIdHMS(HashMap<String, HashMapWL<String, Integer>> map, 
String id, 
int start_after,
int os)
{
int offset = 0 ;

String attr_name = id.split("=")[0];

HashMapWL<String, Integer> spec_map;
Integer res;

if (map.containsKey(attr_name)) {
spec_map = map.get(attr_name);

if (spec_map.containsKey(id)) {
return spec_map.get(id);

} else {
res = (Integer)spec_map.getLast() +1 ;
spec_map.put(id, res );
map.put(attr_name, spec_map);
return res;
}
} else {
//compute offset
offset = os * map.size();
spec_map = new HashMapWL<String, Integer>();

res = 1 + start_after + offset;
spec_map.put(id, res);
map.put(attr_name, spec_map);
return res;
}
}*/
