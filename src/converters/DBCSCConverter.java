package converters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import main.UserPuker;
import main.UsersGraph;
import main.UserUtility;

public class DBCSCConverter implements Converter {
	
	private PrintWriter pw;

	
	public DBCSCConverter(String filename) throws FileNotFoundException {
		
		pw = new PrintWriter(filename);
		
		String init = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n" +
		"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
		"xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n" + 
		"http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n" + 
		"<graph id=\"G\" edgedefault=\"undirected\">\n";
		
		pw.write(init);
	}
	
	private boolean contains(Iterable<Vertex> c, Vertex v) {
		for (Vertex e : c) {
			if (e.getId().toString().equals(v.getId().toString()))
				return true;
		}
		return false;
	}
	
	public void translate(UsersGraph g) {
		
		Graph graph = g.getGraph();
		
		StringBuilder working_str = new StringBuilder(),
		 			    attribute = new StringBuilder();
		
		Iterable<Vertex> users =                   /* the whole users set */ 
				graph.getVertices(UserUtility.WHOAMI, "user");
		
		Iterable<Vertex> likes =                   /* the whole likes set */
				graph.getVertices(UserUtility.WHOAMI, "like");
		
		Iterable<Vertex> user_likes;
		

		for (Vertex u : users) {

			/* <node id="123456789" */
			working_str.append("<node id=\"" + u.getId().toString() + "\" ");
			
			/* get the _user_ like */
			user_likes = u.getVertices(Direction.BOTH, "likes");
			
			/* for each user like */
			for (Vertex l : likes) {
				attribute.append("like_" + l.getId().toString()+"=\"");
				attribute.append(contains(user_likes,l) ? 1 : 0) ;
				attribute.append("\" ");
				working_str.append(attribute);
				attribute.setLength(0);
			}
			working_str.append("/>\n");
			

			
			for (Vertex f : u.getVertices(Direction.BOTH, "friend")) {

				/*
				 * Code to add no duplicate edge:
				 * the general idea is to adding an edge between id1 and id2 (friends)
				 * if and only if id1>id2. But if id1 is DAU and id2 is not DAU
				 * then id2 does not contain an edge to her friend id1, so in that
				 * case an edge between id1 and id2 is added anyway. 
				 * Moreover if id1 is non-DAU and id2 is DAU then no any edge is added
				 * between them (because when it is the turn of id2 to be a User (and not a friend)
				 * and and of id2 to be a Friend it will, for sure, add an edge between id2 and id1  
				 */
				if(u.getProperty(UserUtility.ISDAU) == Boolean.TRUE &&
					   f.getProperty(UserUtility.ISDAU) == Boolean.FALSE) {
						working_str.append("<edge source=\"" + u.getId().toString() +
								"\" target=\"" + f.getId().toString() + "\"/>\n");					
				} else {
					if (u.getId().toString().compareTo(f.getId().toString()) > 0 &&
						!(u.getProperty(UserUtility.ISDAU) == Boolean.FALSE &&
						  f.getProperty(UserUtility.ISDAU) == Boolean.TRUE))
						working_str.append("<edge source=\"" + u.getId().toString() +
								"\" target=\"" + f.getId().toString() + "\"/>\n");
				}
			}
			
			
			pw.write(working_str.toString(),0, working_str.length());
		}
		pw.write("</graph>\n</graphml>");
		pw.close();
	}
	
	public static void main(String args[]) throws FileNotFoundException, IOException {
		File inputFolder = new File("/home/np2k/Desktop/jx");
		File[] files = inputFolder.listFiles();
		
		UsersGraph g = new UsersGraph();
		
		int i=0;
		UserPuker j;
		
		for (File f : files) {
			j = new  UserPuker (f.getAbsolutePath());
			g.addUser(j.getEUser());			
			System.out.println(files.length-i);
			i++;
		}
		
		new DBCSCConverter("/home/np2k/Desktop/output.graphml").translate(g);
	}
}
