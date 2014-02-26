package main;
/**
 * DotPuker:   converts a set of json files in a .dot file.
 *             It assumes that exist no duplicate 'id' field 
 *             in the json files (in that case, data are 
 *             not consistent ==> solution: keep a list of ego-user
 *             and place a 'continue' instruction in the for loop
 *             in the case of duplicate 'id' field).).
 *                       
 * */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader; 
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;


class MyThread implements Runnable {
	private static HashSet<String> global_likes_list                       /* useful to check like already inserted */ 
								     = new HashSet<String>();
	
	private static Random rand_color = new Random(); 					   /* random rgb color generator */
		
	private Thread thread;                                                 /* thread to manage json files */
	private final static int MAX_THREAD = 4;                               /* max thread number */
	private final byte LIKE_SIZE = 7;
	private final byte EGO_SIZE  = 5;
	
	private BufferedReader[] br;                                           /* file readers list */	
	private StringBuilder partial_dotfile = new StringBuilder();           /* partial dot file resulting from one thread */
	
	public Thread getThread() { return thread; }
	public StringBuilder getDotFile() { return partial_dotfile; }
	
	private File[] files;
	
	/* 
	 * MyThread constructor: initializes FileReader[] 
	 * and starts a thread to manage a certain number
	 * of json files. 
	 * */
	MyThread(File[] files) {
		thread = new Thread(this);		
		br = new BufferedReader[files.length];
		
		this.files = files;
		
		thread.start();
	}
	
	/*
	 * Parameter 'files' indicates the complete dot list. 
	 * Creates num_thread threads. Assigns a num_file_per_thread files 
	 * to one thread.
	 * Returns the threads list just created.
	 */
	public static MyThread[] init(File[] files) throws FileNotFoundException
	{
		System.out.println("---------------------begin initialization---------------------");
		int num_thread;                                             /* number of thread to manage json files */
		int i,j=0, k=0;                                             /* auxiliary variables */ 
		if (files.length <= MAX_THREAD)
			num_thread = files.length;
		else 
			num_thread = MAX_THREAD; 
		
		System.out.println("total num of files: " + files.length);
		System.out.println("num_thread: " + num_thread);
		
		MyThread[] mt = new MyThread[num_thread];                   /* define a list of mythread threads */
		/* read all dot file in the input folder */
		File[] sublist;                                             /* files sub list as input in MyThread constructor */
		
		int ratio = files.length / num_thread;
		
		System.out.println("ratio: " + ratio);
		
		for (i=0; i<num_thread; i++) {
			if (i==num_thread-1)
				ratio += files.length%num_thread;
			
			sublist = new File[ratio];
			for (k=0; k<ratio; k++, j++)
				sublist[k] = files[j];
			mt[i] = new MyThread(sublist);
			System.out.println("***" + mt[i].thread.getName() + " with " + sublist.length + " file(s)");	
		}
		System.out.println("---------------------end initialization---------------------");
		return mt;

	}
	
	public void run() {
		System.out.flush();
		System.out.println(Thread.currentThread().getName() + " starting...");
		
		JsonObject jsonObj;                                        /* json root */               
		String ego, rgb;                                           /* "ego" (i.e engage user) string, and rgb color string */
		                   
		JsonArray friends_list;                                    /* ego's friends list */
		JsonArray likes_list;                                      /* ego's likes list */
		//JsonObject friends;
		JsonObject likes;
		String like_str;
		
		boolean contains;                                          /* true if (in the for cycle) we discover duplicate like*/
		StringBuilder edge = new StringBuilder();                  /* the edge taken from json, to be inserted in dot file */
		StringBuilder like_node =  new StringBuilder();            /* string id of a like node */


		
		for (int i=0; i<br.length; i++){
			
			try {
				br[i] = new BufferedReader(new FileReader(files[i]));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/* read json root */
			try {
				jsonObj = JsonObject.readFrom(br[i]);
				try {
					br[i].close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				System.out.println("ioexception in readFrom.");
				return;
			}
			/* extract "ego" uid (i.e. engage user id) */
			try {
				ego = jsonObj.get("id").toString();
			} catch (NullPointerException e) {
				//SHOULD NOT ENTER HERE, anyway...
				ego = String.valueOf(Math.abs(rand_color.nextInt()));
				System.out.println("!!!WARNING!!!");
			}
	
			/* generate a random rgb triple for "ego" node */
			rgb = "\"" + String.valueOf(rand_color.nextInt(255)) + "," +  //R
						 String.valueOf(rand_color.nextInt(255)) + "," +  //G
						 String.valueOf(rand_color.nextInt(255))+ "\"";   //B
			
			partial_dotfile.append(ego + " [node_color=" + rgb + ", node_size="+EGO_SIZE+"]\n");
			
			try {
				if (jsonObj.get("friends") != null)				
					friends_list = jsonObj.get("friends").asObject().get("data").asArray();
				else  {
					friends_list = jsonObj.get("mutualfriends").asObject().get("data").asArray();
				}
				
				/* edge friendship format: ego--{friend1 friend2...friendN} */
				edge.append(ego + "--{");
				for (JsonValue friend : friends_list) {
					edge.append(friend.asObject().get("id").toString() + " ");
				}
				
				edge.append("}\n");
				partial_dotfile.append(edge);
			} catch(NullPointerException e) { /* DO NOTHING */ }
			
			edge.setLength(0);
			
			try { 
				likes = jsonObj.get("likes").asObject();
				
				/* edge "likeship" format: ego--{like1 like2...likeK} */
				edge.append(ego + "--{");
	
				/* extract likes list */
				likes_list = likes.get("data").asArray();
				like_node.setLength(0);
				
				/* does not insert likes already in */
				for (JsonValue like : likes_list) {
					like_str = like.asObject().get("id").toString();
					synchronized (global_likes_list) {
					     contains = global_likes_list.contains(like_str);
					}
					if (!contains) {
					       like_node.append(like_str + 
					          " [node_color=\"255,255,255\", node_size="+LIKE_SIZE+"]\n");
					       synchronized (global_likes_list) {
					              global_likes_list.add(like_str);
					      }
					}						
					edge.append(like.asObject().get("id").toString() + " ");
				}
				edge.append("}\n");
				
				partial_dotfile.append(edge);
				partial_dotfile.append(like_node);
			} catch(NullPointerException e) { }
			partial_dotfile.append("\n");
			edge.setLength(0); 
		} //end for		
		//System.out.println(Thread.currentThread().getName() + " partial_dotfile length: " + partial_dotfile.length());
		System.out.println(Thread.currentThread().getName() + ": finished!");
	}
}

public class DotPuker {
	/* usage: program inputfolder outputfolder */
	public static void main(String args[]) throws IOException {
		long startTime = System.currentTimeMillis();
				
		File inputFolder;
		File outputFolder;
		StringBuilder dotfile = new StringBuilder();
		BufferedWriter bw;
		
		if (args.length==0) {
			inputFolder = new File("/home/np2k/Desktop/jx");
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

		MyThread[] mt = MyThread.init(files);
		
		int num_thread = mt.length;
		dotfile.append("graph engage_net {\n");

		/* 
		 * main thread is waiting for the end of each 
		 * single thread in order to create a global
		 * dot file 
		 * */
		for (int i = 0; i < num_thread; i++) {
			try {
				System.out.println("waiting for " + mt[i].getThread().getName());
				mt[i].getThread().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			dotfile.append(mt[i].getDotFile());
		}
		dotfile.append("}");
		
		/* setting up FileWriter */
		File output_net;
		try {
			output_net = new File(outputFolder, "net.dot");
			bw = new BufferedWriter(new FileWriter(output_net));
		} catch (FileNotFoundException e) {
			System.out.println("error opening output file.");
			return;
		}
		bw.write(dotfile.toString());
		bw.close();
		
		long stopTime = System.currentTimeMillis();
		//System.out.println(dotfile);
		System.out.println("Elapsed time: "+String.valueOf(stopTime-startTime) + " msec");
		System.out.println(output_net.getName() + " size: " + String.valueOf((float)output_net.length()/1000) + " kbyte");	
	}
}
