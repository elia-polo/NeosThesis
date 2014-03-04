package converters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import utils.Util;

import com.google.code.geocoder.model.LatLng;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import facebook.RelationshipStatus;
import main.UserPuker;
import main.UsersGraph;
import main.UserUtility;

@SuppressWarnings("unused")
public class DBCSCConverter implements Converter {
	
	private PrintWriter pw;

	private static final String asset_folder = "./assets/DBCSC/";
	
	private static final int G_MALE = 0;	
	private static final int G_FEMALE = 1;
	private static final int AGE = 2;
	private static final int RS_SINGLE = 3;
	private static final int RS_IN_A_RELATIONSHIP = 4;
	private static final int RS_ENGAGED = 5;
	private static final int RS_MARRIED = 6;
	private static final int RS_IN_AN_OPEN_RELATIONSHIP = 7;
	private static final int RS_ITS_COMPLICATED = 8;
	private static final int RS_SEPARATED = 9;
	private static final int RS_DIVORCED = 10;
	private static final int RS_WIDOWED = 11;
	private static final int II_MALE = 12;
	private static final int II_FEMALE = 13;
	private static final int HOMETOWN_LATITUDE = 14;
	private static final int HOMETOWN_LONGITUDE = 15;
	private static final int LOCATION_LATITUDE = 16;
	private static final int LOCATION_LONGITUDE = 17;
	private static final int E_HIGH_SCHOOL = 18;
	private static final int E_COLLEGE = 19;
	private static final int E_GRADUATE_SCHOOL = 20;

	private static final int PROFILE_ATTRIBUTES = 21;
	

	
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
		
	private String genGraphMLAttr(Object[] profile) {
		StringBuilder str = new StringBuilder(); 
		Integer i = new Integer(0);
		for (Object p : profile)  {
			str.append("att_" + i.toString() + "=\"" +profile[i].toString() + "\" ");
			i++;
		}
		return str.toString();	
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
						
			Object[] profile = new Object[PROFILE_ATTRIBUTES];
			for (int i=0; i<profile.length; ++i) { profile[i] = "0"; }

			String s; /* working str */
			
			/* <node id="123456789" */
			working_str.append("<node id=\"" + u.getId().toString() + "\" ");
			
			/****GENDER*****/
			switch(u.getProperty(UserUtility.GENDER).toString()) {
				case "male":
					profile[G_MALE] = "1";
					break;
				case "female":
					profile[G_FEMALE] = "1";
					break;
			}
			
			/*****AGE*****/
			profile[AGE] = Util.getAge(u.getProperty(UserUtility.BIRTHDAY).toString());
			
			
			/****REL_STATUS****/
			s = u.getProperty(UserUtility.REL_STATUS).toString();
			if(s != null && !s.equals("null")) {
				switch(s) {
				case RelationshipStatus.SINGLE:
					profile[RS_SINGLE] = "1";
					break;
				case RelationshipStatus.IN_A_RELATIONSHIP:
					profile[RS_IN_A_RELATIONSHIP] = "1";
					break;
				case RelationshipStatus.ENGAGED:
					profile[RS_ENGAGED] = "1";
					break;
				case RelationshipStatus.MARRIED:
					profile[RS_MARRIED] = "1";
					break;
				case RelationshipStatus.IN_AN_OPEN_RELATIONSHIP:
					profile[RS_IN_AN_OPEN_RELATIONSHIP] = "1";
					break;
				case RelationshipStatus.ITS_COMPLICATED:
					profile[RS_ITS_COMPLICATED] = "1";
					break;
				case RelationshipStatus.SEPARATED:
					profile[RS_SEPARATED] = "1";
					break;
				case RelationshipStatus.DIVORCED:
					profile[RS_DIVORCED] = "1";
					break;
				case RelationshipStatus.WIDOWED:
					profile[RS_WIDOWED] = "1";
					break;
				}
			} //else all profile relationship_status-element are setted to 0 

			/*** INTERESTED IN****/
			switch(u.getProperty(UserUtility.INTERESTED_IN).toString()) {
				case "male":
					profile[II_MALE] = "1";
					break;
				case "female":
					profile[II_FEMALE] = "1";
					break;
			}
			
			/****HOMETOWN*****/
			s = u.getProperty(UserUtility.HOMETOWN_NAME).toString();
			System.out.println("********************* " + s);
			if(s != null && !s.equals("null")) {
				LatLng coords = Util.getCoordinates(s);
				profile[HOMETOWN_LATITUDE] = coords.getLat();
				profile[HOMETOWN_LONGITUDE] = coords.getLng();
			}
			
			
			/***LOCATION***/
			s = u.getProperty(UserUtility.LOCATION_NAME).toString();
			if(s != null && !s.equals("null")) {
				LatLng coords = Util.getCoordinates(s);
				profile[LOCATION_LATITUDE] = coords.getLat();
				profile[LOCATION_LONGITUDE] = coords.getLng();
			}
			
			/***EDUCATION****/
			s = u.getProperty(UserUtility.HIGH_SCHOOL).toString();
			if (s.equals("1"))
				profile[E_HIGH_SCHOOL] = 1;
			else
				profile[E_HIGH_SCHOOL] = 0;
			
			s = u.getProperty(UserUtility.COLLEGE).toString();
			if (s.equals("1"))
				profile[E_COLLEGE] = 1;
			else
				profile[E_COLLEGE] = 0;

			s = u.getProperty(UserUtility.GRADUATE_SCHOOL).toString();
			if (s.equals("1"))
				profile[E_GRADUATE_SCHOOL] = 1;
			else
				profile[E_GRADUATE_SCHOOL] = 0;

			
			attribute.append(genGraphMLAttr(profile));
			
			/* get the _user_ like (here Direction.OUT is enough) */
			user_likes = u.getVertices(Direction.BOTH, "likes");
			
			/* for each graph like */
			for (Vertex l : likes) {
				attribute.append("like_" + l.getId().toString()+"=\"");
				attribute.append(ConvUtility.contains(user_likes,l) ? 1 : 0) ;
				attribute.append("\" ");
				working_str.append(attribute);
				attribute.setLength(0);
			}
			working_str.append("/>\n");
			
			for (Vertex f : u.getVertices(Direction.BOTH, "friend")) {
				if (u.getId().toString().compareTo(f.getId().toString()) > 0)					
						working_str.append("<edge source=\"" + u.getId().toString() +
								"\" target=\"" + f.getId().toString() + "\"/>\n");
			}
			
			
			pw.write(working_str.toString(),0, working_str.length());
			working_str.setLength(0);
		}
		pw.write("</graph>\n</graphml>");
		pw.close();
	}
	
	public static void main(String args[]) throws FileNotFoundException, IOException {
		File inputFolder = new File("/home/np2k/Desktop/test_missing");
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
