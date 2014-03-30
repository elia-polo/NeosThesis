package converters;

import com.tinkerpop.blueprints.Graph;

import main.UsersGraph;

public interface Converter {
	public void translate(UsersGraph g);
	public void translate(Graph g);
}
