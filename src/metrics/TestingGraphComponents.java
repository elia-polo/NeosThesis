package metrics;

import java.io.IOException;
import java.util.List;

import com.tinkerpop.blueprints.Graph;

public class TestingGraphComponents {
	public static void main(String args[]) throws IOException {

		long start = System.currentTimeMillis();
		System.out.print("begin loading json files....");
		Graph g = UserStats.loadFromJson();
		System.out.println("end! (" + String.valueOf( (long)(System.currentTimeMillis() - start) / 1000 ) + " sec)");
		

		start = System.currentTimeMillis();
		System.out.print("begin getting graph components...");
		GraphConnectivity.ConnectedComponentsAsSubGraphs cc 
												= new GraphConnectivity.ConnectedComponentsAsSubGraphs(g);
		System.out.println("end! (" + String.valueOf( (long)(System.currentTimeMillis() - start) / 60000 ) + " min)");
		
	
		List<Graph> comps = cc.getComponents();
		
		System.out.println("number of vertices: " + cc.getNumVertices());
		System.out.println("number of components: " + comps.size());
		
		System.out.println("F: " + cc.getBorgattiDistanceFragmentation());
	
	}
}
