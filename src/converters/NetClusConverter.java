package converters;

import main.UsersGraph;

public class NetClusConverter implements Converter {

	/**
	 * Converts a UsersGraph into a dataset file suitable for the NetClus clustering algorithm:
	 * <ul>
	 * <li>each Node type (EUser, ELike) and Node or Edge property (e.g. Birthday, Hometown) is converted to an Entity
	 * <li>each Edge instance is a relation. There exist as many Relations as <Node type, Node type> pairs connected by an edge in the original graph (e.g. <EUser-Euser>, <EUser-ELike>) and <Node, Node property> pairs, where the Node property is being represented as an Entity (e.g. <EUser-Birthday>, <EUser-Hometown>)
	 * <li>a file is created for each Entity and Relation
	 * <li>each Node instance and Node or Edge property value is assigned a unique (among homogenous values, that is a node instance and node property instance may share the same id) integer id
	 * <li>the Node instance and Node or Edge property value is described as a string
	 * <li>Entity files are tab separated lines of <Entity_id,Entity_value> pairs; the first must be parsable to integer, the second to string
	 * <li>Relation files are tab separated lines of <Entity_id,Entity_id> pairs; both must be parsable to integer
	 * </ul>
	 * @param g The UsersGraph to be converted
	 */
	public void translate(UsersGraph g) {
		

	}

}
