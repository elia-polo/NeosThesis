package main;
/**
 * GmlPuker:   converts a set of json files in a .gml file.
 *             It assumes that exist no duplicate 'id' field 
 *             in the json files (in that case, data are 
 *             not consistent ==> solution: keep a list of ego-user
 *             and place a 'continue' instruction in the for loop
 *             in the case of duplicate 'id' field).).
 *                       
 * */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader; 
import java.io.IOException;
//DEBUG: import java.io.PrintWriter;
import java.util.Collection;
import java.util.Random;


import com.eclipsesource.json.*;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.gml.GMLWriter;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class GmlPuker {
	/* usage: program inputfolder outputfolder */
	public static void main(String args[])  throws IOException  {
		long startTime = System.currentTimeMillis();
		
		File inputFolder;
		File outputFolder;
		
		Graph graph = new TinkerGraph();
		
		JsonObject jsonObj;                                        /* json root */               
		Vertex ego, f,v; 
		String rgb;                                           	   /* "ego" (i.e engage user) string, and rgb color string */
		                   
		JsonArray friends_list;                                    /* ego's friends list */
		JsonArray likes_list;                                      /* ego's likes list */
		String like_str;
		Random rand_color = new Random(); 					       /* random rgb color generator */
		final byte LIKE_SIZE = 7;
		final byte EGO_SIZE  = 5;

		//DEBUG: PrintWriter pw = new PrintWriter("/home/np2k/Desktop/like.txt");
		
		if (args.length==0) {
			inputFolder = new File("/home/np2k/Desktop/json_user");
			outputFolder = new File("/home/np2k/Desktop/");
		} else {
			inputFolder = new File(args[0]);
			outputFolder = new File(args[1]);
		}
		
		File[] files = inputFolder.listFiles();
		if (files.length==0) {
			System.out.println("no json files in the input folder.");
			return;
		}
		BufferedReader br = null;
		
		for (int i = 0; i < files.length; i++) {
			System.out.print(String.valueOf(i+1) + ". file: " + files[i].getName()+"...");
			/* read json root */
			try {
				br = new BufferedReader(new FileReader(files[i]));
				
			} catch (FileNotFoundException e1) {
				System.out.println(files[i].getAbsolutePath() + " not found");
				continue;
			}
			try {
				jsonObj = JsonObject.readFrom(br);
				br.close();
			} catch (IOException e) {
				System.out.println("ioexception in readFrom.");
				return;
			}
			
			/* extract "ego" uid (i.e. engage user id) */
			try {
				ego = graph.addVertex(jsonObj.get("id").asString());
			} catch (IllegalArgumentException e) {
				ego = graph.getVertex(jsonObj.get("id").asString());     /*  an ego-user may have been previously entered: 
				 														  *  e.g. as a friend of another engage-user */				 															 				
			}
				
			/* generate a random rgb triple for "ego" node */
			rgb =  String.valueOf(rand_color.nextInt(255)) + "," +  	  //R
						 String.valueOf(rand_color.nextInt(255)) + "," +  //G
						 String.valueOf(rand_color.nextInt(255));         //B
			ego.setProperty("node_color", rgb);
			ego.setProperty("node_size", EGO_SIZE);

			try {
				if ( (jsonObj.get("friends")) != null )
					friends_list = jsonObj.get("friends").asObject().get("data").asArray();
				else /* if no mutualfriends tag exists throws illegal..exception */
					friends_list = jsonObj.get("mutualfriends").asObject().get("data").asArray();
				
				String edge_id;
				for (JsonValue friend : friends_list) {
						try {
							f = graph.addVertex(friend.asObject().get("id").asString());
						} catch(IllegalArgumentException e) { 
							f = graph.getVertex(friend.asObject().get("id").asString());
						}					
						/* create an unique id for the edge between a user and its friend */
						if (ego.getId().toString().compareTo(f.getId().toString()) > 0) 
							edge_id = ego.getId().toString()+ "-" + f.getId().toString();
						else
							edge_id = ego.getId().toString() + "-" + f.getId().toString() ;
						
						try {
							graph.addEdge(edge_id, ego, f, "friend");
						} catch (IllegalArgumentException e) { /* do nothing */ }

				}
			} catch (NullPointerException e) { /* DO NOTHING */}
			
			/* extract likes list */
			try {
				likes_list = jsonObj.get("likes").asObject().get("data").asArray();				
				/* does not insert likes already in */
				for (JsonValue like : likes_list) {
					like_str = like.asObject().get("id").asString();
					try {
						v = graph.addVertex(like_str);
						v.setProperty("node_size", LIKE_SIZE);
						v.setProperty("node_color", "255,255,255");
					} catch(IllegalArgumentException e) {
						v = graph.getVertex(like_str);
						//DEBUG: pw.println(like_str);  
					}
					graph.addEdge(null,  ego, v, "likeship");
				}
			} catch (NullPointerException e) { /* DO NOTHING */ }
			System.out.println("done!");
		} //end for		
		
		//DEBUG: System.out.println(Thread.currentThread().getName() + " partial_dotfile length: " + partial_dotfile.length());
		//DEBUG: pw.close();
		
		
		File output_net = new File(outputFolder, "net.gml");
		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(output_net));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return;
		}
		GMLWriter.outputGraph(graph,bos);
		
		Collection<Vertex> vertex_coll = (Collection<Vertex>)graph.getVertices();
		Collection<Edge> edge_coll = (Collection<Edge>)graph.getEdges();
		
		long stopTime = System.currentTimeMillis();
		System.out.println("\nElapsed time: "+String.valueOf(stopTime-startTime) + " msec");
		System.out.println(output_net.getName() + " size: " + String.valueOf((float)output_net.length()/1000) + " kbyte");
		System.out.println("nodes number: " + vertex_coll.size());
		System.out.println("edge number: " + edge_coll.size());

	}

}
