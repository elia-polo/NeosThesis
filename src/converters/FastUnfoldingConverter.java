package converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import main.UserUtility;
import main.UsersGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;

public class FastUnfoldingConverter implements Converter {

	private static final String asset_folder = "./assets/FastUnfolding/";
	static {
		new File(asset_folder).mkdirs();
	}
	
	@Override
	public void translate(UsersGraph g) {
		long start_time = System.currentTimeMillis();
		// Clear folder
		for(File file: new File(asset_folder).listFiles()) file.delete();
		
		try (BufferedWriter dataset = Files.newBufferedWriter(Paths.get(asset_folder+"dataset.txt"), StandardCharsets.UTF_8)) {
			for(Edge e : g.getGraph().getEdges()) {
				if(e.getLabel().equals(UserUtility.FRIEND)) {
					dataset.write(e.getVertex(Direction.OUT).getId()+" "+e.getVertex(Direction.IN).getId()+System.lineSeparator());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Total running time: "+(System.currentTimeMillis() - start_time)+" ms");
	}
}