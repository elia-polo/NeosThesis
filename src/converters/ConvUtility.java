package converters;

import java.util.HashMap;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

class TwoObj { 
	Object obj1;
	Object obj2;
	
	public TwoObj(Object o1, Object o2) {
		obj1 = o1;
		obj2 = o2;
	}
	
	public TwoObj() { /* do nothing */ }

	public Object getObj1() { return obj1; }
	public void setObj1(Object obj1) { this.obj1 = obj1; }
	public Object getObj2() { return obj2; }
	public void setObj2(Object obj2) { this.obj2 = obj2; }
}

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
	 * the last incremental id + 1, starting from 'start_from').
	 * If the  'id' has already been inserted into the hashmap
	 * the matching incremental id is returned. 
	 * 
	 * @param map, the incremental user id hashmap
	 * @param id, the FB user id
	 * @return the incremental user id
	 * 
	 * */
	
	public static Integer getIncId(HashMap<String, Integer> map, String id, int start_from) {
		if (map.containsKey(id)) 
			return map.get(id);
		 else {
			Integer res = new Integer(map.size() + 1 + start_from);
			map.put(id, res);
			return res;
		 }
	}
	
	public static Integer getIncId(HashMap<String, Integer> map, String id) {
		return getIncId(map, id, 0);
	}
	
	public static TwoObj getAndCountIncId(HashMap<String, HashMapWL<String, TwoObj>> map, 
										  String id, 
										  int start_from, 
										  HashMap<String, Integer>attr_offset)
	{
		String attr_name = id.split("=")[0];
		
		HashMapWL<String, TwoObj> spec_map;
		Integer offset = new Integer(0);

		TwoObj t;
		if (map.containsKey(attr_name)) {
			spec_map = map.get(attr_name);
			
			if (spec_map.containsKey(id)) {
				t = spec_map.get(id);	
				t.setObj2((Integer) t.getObj2() + 1);
				spec_map.put(id, t);
				map.put(attr_name, spec_map);
				return t;
			} else {	
				t = new TwoObj((Integer)(((TwoObj)(spec_map.getLast())).getObj1())+1, 1);
				spec_map.put(id, t);
				map.put(attr_name, spec_map);
				return t;
			}
		} else {
			//compute offset
			for (String a : map.keySet()) {
				offset += attr_offset.get(a);
			}

			spec_map = new HashMapWL<String, TwoObj>();
			
			t = new TwoObj(new Integer(1 + start_from + offset), 1);
			spec_map.put(id, t);
			map.put(attr_name, spec_map);
			return t;
		}
	}
	
	public static TwoObj getAndCountIncId(HashMap<String, HashMapWL<String, TwoObj>> map, 
			  String id, 
			  int start_from,
			  int os)
	{
			String attr_name = id.split("=")[0];
			int offset = 0;
			HashMapWL<String, TwoObj> spec_map;
			
			TwoObj t;
			if (map.containsKey(attr_name)) {
				spec_map = map.get(attr_name);
			
				if (spec_map.containsKey(id)) {
					t = spec_map.get(id);	
					t.setObj2((Integer) t.getObj2() + 1);
					spec_map.put(id, t);
					map.put(attr_name, spec_map);
					return t;
				} else {	
					t = new TwoObj((Integer)(((TwoObj)(spec_map.getLast())).getObj1())+1, 1);
					spec_map.put(id, t);
					map.put(attr_name, spec_map);
					return t;
				}
			} else {
				spec_map = new HashMapWL<String, TwoObj>();
				offset = os * map.size();
				t = new TwoObj(new Integer(1 + start_from + offset), 1);
				spec_map.put(id, t);
				map.put(attr_name, spec_map);
				return t;
			}
	}

	
	
	
	/**
	 * given the set of string arguments the method returns 
	 * a String separated with the 'sep' Character and
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
	
}
