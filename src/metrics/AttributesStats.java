package metrics;
import java.util.ArrayList;
import java.util.HashMap;

import main.UserUtility;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class AttributesStats {

	private HashMap<String, HashMap<?,?>> listAttrsStats; 
											
	
	private HashMap<String, Integer> attrValuesCardinality;

	public HashMap<String, Integer> getattrValuesCardinality() {
		return attrValuesCardinality;
	}
	
	public HashMap<String, HashMap<?,?>> getListAttrsStats() {
		return listAttrsStats;
	}
	
	public void getStats( Graph g ) {
		ArrayList<String> attrs_name = new ArrayList<String>();
		
		listAttrsStats = new HashMap<String, HashMap<?,?>>();
		
		attrs_name.add(UserUtility.AGE);
		attrs_name.add(UserUtility.GENDER);
		attrs_name.add(UserUtility.INTERESTED_IN);
		attrs_name.add(UserUtility.HOMETOWN);
		attrs_name.add(UserUtility.LOCATION);
		attrs_name.add(UserUtility.REL_STATUS);
		attrs_name.add(UserUtility.HIGH_SCHOOL);
		attrs_name.add(UserUtility.COLLEGE);
		attrs_name.add(UserUtility.GRADUATE_SCHOOL);
		
		HashMap<?, ?> spec_map;
		for (String a : attrs_name) {
			spec_map = attrStats(g,a);
			listAttrsStats.put(a, spec_map);
		}
	}
	
	public void getCard( Graph g ) {
		ArrayList<String> attrs_name = new ArrayList<String>();
		
		attrValuesCardinality = new HashMap<String, Integer>();
		
		attrs_name.add(UserUtility.AGE);
		attrs_name.add(UserUtility.GENDER);
		attrs_name.add(UserUtility.INTERESTED_IN);
		attrs_name.add(UserUtility.HOMETOWN);
		attrs_name.add(UserUtility.LOCATION);
		attrs_name.add(UserUtility.REL_STATUS);
		attrs_name.add(UserUtility.HIGH_SCHOOL);
		attrs_name.add(UserUtility.COLLEGE);
		attrs_name.add(UserUtility.GRADUATE_SCHOOL);
		
		HashMap<?, ?> spec_map;
		for (String a : attrs_name) {
			spec_map = attrStats(g,a);
			attrValuesCardinality.put(a, new Integer(spec_map.size()));
		}
		
	}
	
	public void getStatsAndCard( Graph g ) {
		ArrayList<String> attrs_name = new ArrayList<String>();
		
		listAttrsStats = new HashMap<String, HashMap<?,?>>();
		attrValuesCardinality = new HashMap<String, Integer>();
		
		attrs_name.add(UserUtility.AGE);
		attrs_name.add(UserUtility.GENDER);
		attrs_name.add(UserUtility.INTERESTED_IN);
		attrs_name.add(UserUtility.HOMETOWN);
		attrs_name.add(UserUtility.LOCATION);
		attrs_name.add(UserUtility.REL_STATUS);
		attrs_name.add(UserUtility.HIGH_SCHOOL);
		attrs_name.add(UserUtility.COLLEGE);
		attrs_name.add(UserUtility.GRADUATE_SCHOOL);
		
		HashMap<?, ?> spec_map;
		for (String a : attrs_name) {
			
			spec_map = attrStats(g,a);
			listAttrsStats.put(a, spec_map);
			attrValuesCardinality.put(a, new Integer(spec_map.size()));
		}
		
	}
	
	public HashMap<?,?> attrStats(Graph g, String attr) {
		
		GremlinPipeline<Iterable<Vertex>, Vertex>pipe = 
				new GremlinPipeline<>();
		
		return (HashMap<?, ?>) pipe.start(g.getVertices()).has("whoami","user")
								   .property(attr)
								   .groupCount()
								   .cap()
								   .next();
		
	}
	
	public static void main(String args[]) {
		Graph g = new TinkerGraph("/home/np2k/Desktop/", TinkerGraph.FileType.GML); 
				

		HashMap<?,?> map = new AttributesStats().attrStats(g, "age");
		
        System.out.println(map.size());
        
        System.out.println(map);
         
			

	}
}