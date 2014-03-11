package converters;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import main.UserUtility;
import main.UsersGraph;
import metrics.UserStats;

public class DLFormatConverter  {

	private static final String asset_folder = "/home/np2k/Desktop/DLFormat/"; 
	
	/**
	 * Convert a TinkerPop graph (note: not a UsersGraph) to 
	 * DL format (edgeslist) suitable for ucinet software. 
	 * */
	public void translate(Graph graph) {
		int size = 0;
		
		for (@SuppressWarnings("unused") Vertex u : graph.getVertices() )
			++size;
		
		try (BufferedWriter bw_edgeslist = Files.newBufferedWriter(Paths.get(asset_folder+"edgeslist.txt"), StandardCharsets.UTF_8)) {
			
			String init = "dl n = " + size + " format = edgelist1\ndata:\n";
			
			HashMap<String, Integer> users_inc_id_map = new HashMap<String, Integer>();
			
			bw_edgeslist.write(init);
			for(Edge e : graph.getEdges()) {
					if (e.getLabel().equals(UserUtility.FRIEND)) {
						bw_edgeslist.write(ConvUtility.getIncId(users_inc_id_map, e.getVertex(Direction.OUT).getId().toString()) + " " + 
										   ConvUtility.getIncId(users_inc_id_map, e.getVertex(Direction.IN).getId().toString()) + 
										   System.lineSeparator());
					}
			}
		} catch(IOException e)  { e.printStackTrace(); }
		
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		System.out.print("begin loading json files....");
		Graph g = UserStats.loadFromJson();
		System.out.println("end!\n");
		
		System.out.print("begin dl-format converting....");
		new DLFormatConverter().translate(g);
		System.out.println("end!");
	}
}
