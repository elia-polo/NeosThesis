package utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

public class GraphTraversals {

	/**
	 * <p>Traverse all reachable nodes in arbitrary order and perform the specified function on each of them.</p>
	 * The implementation is iterative
	 * @param v
	 * @param function
	 */
	public static void reachableNodes(Vertex v, String[] edge_labels, Callable<Vertex, Void> function) {
		Set<Vertex> visited = new HashSet<Vertex>();
		Deque<Vertex> stack = new ArrayDeque<Vertex>();
		stack.push(v);
		visited.add(v);
		while (!stack.isEmpty()) {
			Vertex current = stack.pop();
			function.call(current);
			for(Vertex n : current.getVertices(Direction.BOTH, edge_labels)) {
				if(!visited.contains(n)) {
					visited.add(n);
					stack.push(n);
				}
			}
		}
//		
//		Set<Vertex> visited = new HashSet<Vertex>();
//		visited.add(v);
//		Deque<Vertex> stack = new ArrayDeque<Vertex>();
//		Iterable<Vertex> neighbours = v.getVertices(Direction.BOTH, edge_labels);
//		if(neighbours instanceof Collection) {
//			stack.addAll((Collection<Vertex>) neighbours);
//		} else {
//			for(Vertex n : neighbours) {
//				stack.add(n);
//			}
//		}
//		while (!stack.isEmpty()) {
//			Vertex current = stack.pop();
//			function.call(current);
//			for(Vertex n : current.getVertices(Direction.BOTH, edge_labels)) {
//				if(!visited.contains(n)) {
//					visited.add(n);
//					stack.add(n);
//				}
//			}
//		}
	}

}
