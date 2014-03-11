package metrics;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import main.UserUtility;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.ElementHelper;

public class GraphConnectivity {

	public static class ConnectedComponentsAsSubGraphs {
		private List<Graph> components;
		private final boolean debug = false; 
		
		private Vertex deeplyAddVertex(Graph where, Vertex v) {
			try {
				Vertex newv = where.addVertex(v.getId().toString());
				ElementHelper.copyProperties(v, newv);
				return newv;
			} catch(IllegalArgumentException e) {
				return where.getVertex(v.getId().toString());
			}
		}
		
		public List<Graph> getComponents() { return components; }
		
		public List<Integer> getSize() {
			int count = 0;
			List<Integer> res = new Vector<Integer>();
			
			for (Graph g : components) {
				for (@SuppressWarnings("unused") Vertex v : g.getVertices())
					count ++;

				res.add(count);
				count = 0;
			}
			
			return res;
		}
		public Integer getNumVertices() {
			Integer res = new Integer(0);
			for (Integer i : getSize()) {
				res += i;
			}
			return res;
		}
		
		public Double getBorgattiDistanceFragmentation() {
			Double N = new Double(0.0);
			Double sk = new Double(0.0);
			
			for (Integer i : getSize()) {
				N += i;
				sk += i*(i-1);
			}
			if (debug) {
				System.out.println("nodes number: "  + N);
				System.out.println("sk total: "  + N);
			}
			
			
			return new Double( 1 - (sk / (N*(N-1))));
		}
		
		public ConnectedComponentsAsSubGraphs(Graph G) {
			components = new Vector<Graph>();
			
			Deque<Vertex> stack = new ArrayDeque<Vertex>();
			Vector<String> alreadyin = new Vector<String>();
				
			String edge_id;

			for ( Vertex v : G.getVertices() ) {
				if (!alreadyin.contains(v.getId().toString())) {
					
					stack.push(v);
					alreadyin.add(v.getId().toString());
					
					Graph newg = new TinkerGraph();
					
					Vertex newv = deeplyAddVertex(newg, v);
					
					while(!stack.isEmpty()) {
						
						Vertex current = stack.pop();
						
						for ( Vertex n : current.getVertices(Direction.BOTH, UserUtility.FRIEND) ) {
							
							if (debug)
								System.out.println(current.getId().toString() + " - " + n.getId().toString());
							Vertex newn;
							if (!alreadyin.contains(n.getId().toString())) {
								stack.push(n);								
								alreadyin.add(n.getId().toString());
							} 
							
							newn = deeplyAddVertex(newg, n);
													
							if (newv.getId().toString().compareTo(newn.getId().toString()) > 0)
								edge_id = newv.getId().toString() + "-" + newn.getId().toString();
							else
								edge_id = newn.getId().toString() + "-" + newv.getId().toString();
							try {
								newg.addEdge(edge_id, newv, newn, UserUtility.FRIEND);
							} catch (IllegalArgumentException e) { /* do nothing */ }						
						} // for each neighbor
					} // while stack is not empty
					components.add(newg);
					
				} // if				
			} // for each vertex
			
		}
	}
	
	public static class ConnectedComponents {
	    private Map<Vertex, Integer> components;	// marked[v] = has vertex v been marked?
	    private List<Integer> size;         		// size[id] = number of vertices in given component
	    
	    
		private int count;          				// number of connected components
		

		private void reachableNodes(Vertex v, String[] edge_labels) {
			Deque<Vertex> stack = new ArrayDeque<Vertex>();
			stack.push(v);
			
			while (!stack.isEmpty()) {
				Vertex current = stack.pop();
				for(Vertex n : current.getVertices(Direction.BOTH, edge_labels)) {
					if(!components.containsKey(n)) {
						components.put(n, count);
						size.set(count, size.get(count)+1);
						stack.push(n);
					}
				}
			}
		}
		
	    /**
	     * Computes the connected components of the undirected graph <tt>G</tt>.
	     * @param G the graph
	     */
	    public ConnectedComponents (Graph G) {
	    	components = new HashMap<Vertex,Integer>();
	    	size = new Vector<Integer>();
	    	
	        for (Vertex n : G.getVertices()) {
	            if (!components.containsKey(n)) {
	                size.add(1);
	                components.put(n, count);
	                reachableNodes(n, new String[0]);
	                count++;
	            }
	        }
	    }

	    /**
	     * Returns the component id of the connected component containing vertex <tt>v</tt>.
	     * @param v the vertex
	     * @return the component id of the connected component containing vertex <tt>v</tt>
	     */
	    public int id(Vertex v) {
	        return components.get(v);
	    }

	    /**
	     * Returns the number of vertices in the connected component containing vertex <tt>v</tt>.
	     * @param v the vertex
	     * @return the number of vertices in the connected component containing vertex <tt>v</tt>
	     */
	    public int size(Vertex v) {
	        return size.get(components.get(v));
	    }

	    /**
	     * Returns the number of connected components.
	     * @return the number of connected components
	     */
	    public int count() {
	        return count;
	    }

	    public String toString() {
	    	StringBuilder sb = new StringBuilder();
	    	sb.append("Connected components:").append(System.lineSeparator()).append("Component\t\tSize").append(System.lineSeparator());
	    	for(int i=0; i<size.size(); ++i) {
	    		sb.append("Component ").append(i).append("\t\t").append(size.get(i)).append(System.lineSeparator());
	    	}
	    	sb.append(System.lineSeparator()).append("Vertex\tComponent").append(System.lineSeparator());
	    	for(Map.Entry<Vertex,Integer> c : components.entrySet()) {
	    		sb.append(c.getKey().getId().toString()).append("\t\t").append(c.getValue()).append(System.lineSeparator());
	    	}
	    	return sb.toString();
	    }
	    
	    public List<Integer> getSize() {
			return size;
		}

	}
	
	public static float evaluate() {
		throw new UnsupportedOperationException();
	}
}
