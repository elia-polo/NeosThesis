package metrics;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class GraphConnectivity {

	public static class ConnectedComponents {
	    private Map<Vertex, Integer> components;	// marked[v] = has vertex v been marked?
	    private List<Integer> size;         	// size[id] = number of vertices in given component
	    private int count;          			// number of connected components

		private void reachableNodes(Vertex v, String[] edge_labels) {
			Deque<Vertex> stack = new ArrayDeque<Vertex>();
			stack.push(v);
			components.put(v, count);
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
	}
	
	public static float evaluate() {
		throw new UnsupportedOperationException();
	}
}
