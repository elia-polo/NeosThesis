package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import utils.Util;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class UserPuker {
	
	private String json_file;
	private JsonObject jsonObj;
	private EUser user;
	
	
	public UserPuker(String filename) throws FileNotFoundException, IOException {
		
		json_file = filename;
		user = new EUser();
		
		BufferedReader br;
		br = new BufferedReader(new FileReader(json_file));   /* open the user json file 								
			 												   * could throws FileNotFoundException */
		
		jsonObj = JsonObject.readFrom(br);                    /* read the whole json object 
		 													   * could throws IOExcetpion */
		br.close();
		
	}
	
	public EUser getEUser() {
		
		if (json_file.contains("DAU"))           // if the filename contains the DAU substring  
			user.setDAU(true);                   // then it is a DAU user
		else user.setDAU(false);
		
		/* 
		 * Getting id, gender, birthday and relationship_status fields.  
		 * NB: the get_str_value function returns null if the
		 * specified tag doesn't exist in the json object .
		 * */
		
		user.setId(jsonObj.get_str_value(UserUtility.ID));
		user.setGender(jsonObj.get_str_value(UserUtility.GENDER));
		user.setRelationship_status(jsonObj.get_str_value(UserUtility.REL_STATUS));
		
		String bDay = jsonObj.get_str_value(UserUtility.BIRTHDAY);
		if (bDay!=null)
			user.setBirthday(Util.getYear(bDay));
		else
			user.setBirthday("null");
		
		/*
		 * checks if interested_in exists, in that case 
		 * sets the field with the right value, else with null
		 * */
		
		try {
			user.setInterested_in(jsonObj.get(UserUtility.INTERESTED_IN).asArray().get(0).asString());
		} catch(NullPointerException e) {
			user.setInterested_in(null);
		}
		
		/* if exists education field */
		user.setEdu(parseEducation(jsonObj));
		
		/* getting hometown&location fields */
		JsonValue where_obj;
		CoupleNameId where = new CoupleNameId();
		
		try {
		    where_obj = jsonObj.get(UserUtility.HOMETOWN);
			where.setId(where_obj.asObject().get_str_value(UserUtility.ID));
			where.setName(where_obj.asObject().get_str_value(UserUtility.NAME));
			user.setHometown(where);
		} catch (NullPointerException e) { user.setHometown(null); }
		
		where = new CoupleNameId();
		
		try { 
			where_obj = jsonObj.get(UserUtility.LOCATION);
			where.setId(where_obj.asObject().get_str_value(UserUtility.ID));
			where.setName(where_obj.asObject().get_str_value(UserUtility.NAME));
			user.setLocation(where);
		} catch(NullPointerException e) { user.setLocation(null); }
		
		/* getting id friends */
		user.setFriends(parseFriends(jsonObj, user.isDAU()));		
		
		/* getting like objects */
		JsonObject likes_obj;
		
		/* if exists likes tag */
		try 
		{
			likes_obj = jsonObj.get(UserUtility.LIKES).asObject();
			
			/* JSON likes array */
			JsonArray likes = likes_obj.get("data").asArray();
			
			/* with this likes_array must be filled
			 * the user fields */
			ArrayList<ELike> likes_array = new ArrayList<ELike>();
			
			for (JsonValue l : likes)
			{
				/* single like */
				ELike like = new ELike();
				
				like.setId(l.asObject().get_str_value(UserUtility.ID));
				like.setCategory(l.asObject().get_str_value(UserUtility.CATEGORY));
				like.setName(l.asObject().get_str_value(UserUtility.NAME));				
				
				try  {
					/* JSON category list array */
					JsonArray cl = l.asObject().get(UserUtility.CATEGORY_LIST).asArray();
					
					/* with this cat_list must be filled the 'like' object */
					ArrayList<CoupleNameId> cat_list = new ArrayList<CoupleNameId>();
				
					/* filling out the cat_list array with the entries
					 * from json category list array */
					for (JsonValue c : cl) 
						cat_list.add(new CoupleNameId(c.asObject().get_str_value(UserUtility.NAME), 
								c.asObject().get_str_value(UserUtility.ID)));
			
					like.setCategory_list(cat_list);
				} catch (NullPointerException e) { like.setCategory_list(null); }
				likes_array.add(like);
			}
			user.setLikes(likes_array);
		} catch(NullPointerException e) { user.setLikes(null); }
		
		return user;
	}
	
	public static ArrayList<EEducation> parseEducation(JsonObject o) {
		try {
			/* creates an education array */
			JsonArray edu_array = o.get(UserUtility.EDUCATION).asArray();
			ArrayList<EEducation> edu_list = new ArrayList<EEducation>();
			for (JsonValue single_edu : edu_array) {
				/* single education element */
				EEducation edu = new EEducation();
				/* set edu 'type' fields */
				edu.setType(single_edu.asObject().get_str_value(UserUtility.TYPE));
				//System.out.println(o.get_str_value("id")  + "******************" + edu.getType().toString());
				/* if exists 'school' fields... */
				try {
					edu.setName_id(new CoupleNameId(single_edu.asObject()
							.get(UserUtility.SCHOOL).asObject()
							.get_str_value(UserUtility.NAME), single_edu
							.asObject().get(UserUtility.SCHOOL).asObject()
							.get_str_value(UserUtility.ID)));
				} catch (NullPointerException e) {
					edu.setName_id(null);
				}
				try {
					edu.setYear(single_edu.asObject().get(UserUtility.YEAR)
							.asObject().get_str_value(UserUtility.NAME));
				} catch (NullPointerException e) {
					edu.setYear(null);
				}
				edu_list.add(edu);
			}
			return edu_list;
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public static ArrayList<EEducation> parseEducationNoTryCatch(JsonObject o) {
		/* creates an education array */
		JsonValue tmp = o.get(UserUtility.EDUCATION);
		if(tmp == null)
			return null;
		// Education is a JSON array, even if it contains a single element
		JsonArray edu_array = tmp.asArray();
		ArrayList<EEducation> edu_list = new ArrayList<EEducation>();
		for (JsonValue single_edu : edu_array) {
			/* single education element */
			EEducation edu = new EEducation();
			/* set edu 'type' fields */
			tmp = single_edu.asObject().get(UserUtility.TYPE);
			if(tmp != null)
				edu.setType(tmp.asString());
			/* if exists 'school' fields... */
			try {
				edu.setName_id(new CoupleNameId(single_edu.asObject()
						.get(UserUtility.SCHOOL).asObject()
						.get(UserUtility.NAME).asString(), single_edu
						.asObject().get(UserUtility.SCHOOL).asObject()
						.get(UserUtility.ID).asString()));
			} catch (NullPointerException e) {
				edu.setName_id(null);
			}
//			try {
//				edu.setYear(single_edu.asObject().get(UserUtility.YEAR)
//						.asObject().get_str_value(UserUtility.NAME));
//			} catch (NullPointerException e) {
//				edu.setYear(null);
//			}
			edu_list.add(edu);
		}
		return edu_list;
	}
	
	public static ArrayList<String> parseFriends(JsonObject o, boolean isDAU) {
		/* getting id friends */
		JsonObject friends_obj;
		String fr;
		if (isDAU==true)
			fr = UserUtility.FRIENDS;
		else
			fr = UserUtility.MUTUALFRIENDS;
		try {
			friends_obj = o.get(fr).asObject();
			JsonArray friends = friends_obj.get("data").asArray();
			ArrayList<String> friends_id = new ArrayList<String>();
			for (JsonValue f : friends) 
				friends_id.add(f.asObject().get_str_value(UserUtility.ID));
			return friends_id;
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public static ArrayList<String> parseFriendsNoTryCatch(JsonObject o, boolean isDAU) {
		/* getting id friends */
		String fr;
		if (isDAU==true)
			fr = UserUtility.FRIENDS;
		else
			fr = UserUtility.MUTUALFRIENDS;
		JsonValue tmp = o.get(fr);
		if(tmp == null)
			return null;
		JsonObject friends_obj = tmp.asObject();
		// Even with no friends, there exists a data field of type array
		JsonArray friends = friends_obj.get("data").asArray();
		ArrayList<String> friends_id = new ArrayList<String>();
		for (JsonValue f : friends) {
			// If a friends exist, it always has an ID
			friends_id.add(f.asObject().get(UserUtility.ID).asString());
		}
		return friends_id;
	}
	
	public static void main(String args[])  throws Exception  {
		File inputFolder = new File("/home/np2k/Desktop/json.debug");
		
		int count = 0;
		for (File fn : inputFolder.listFiles()) {
			System.out.println("-----------------------------------" + count +"------------------------------------------");
			count++;
			String name = fn.getAbsolutePath();
			System.out.println(name);
			UserPuker ju = new UserPuker(name);
			EUser e = ju.getEUser();
			System.out.println("ID: " + e.getId());
			System.out.println("isDAU: " + e.isDAU());
			System.out.println("gender: " + e.getGender());
			System.out.println("birthday: " + e.getBirthday());
			System.out.println("relationship_status: " + e.getRelationship_status());
			System.out.println("interested_in: " + e.getInterested_in());
			
			if (e.getHometown()!=null)
				System.out.println("hometown: " + e.getHometown().getId() + " " + e.getHometown().getName());
			
			if (e.getLocation() != null)
				System.out.println("location: " + e.getLocation().getId() + " " + e.getLocation().getName());
			
			
			if (e.getLikes()!=null)
				System.out.println("likes number: "  + e.getLikes().size());
			
			if (e.getFriends()!=null)
				System.out.println("friends number: "  + e.getFriends().size());
			
			if (e.getEdu()!=null) { 
				System.out.println("education number: " + e.getEdu().size());
				for (int i=0;i<e.getEdu().size(); i++)
				{
					
					System.out.println("education #" + i +": " + e.getEdu().get(i).getName_id().getName() + " -- " +
							                               e.getEdu().get(i).getType() +  " -- " + e.getEdu().get(i).getYear());
				}
			}
			
			if (e.getLikes()!=null)
				for (int i=0; i<e.getLikes().size(); i++)
				{
					System.out.println("----------------------------------");
					System.out.println("Like #" +i + ". " + e.getLikes().get(i).getName() + " -- " + e.getLikes().get(i).getCategory());
					
					if (e.getLikes().get(i).getCategory_list() != null) {
						for (int j=0; j< e.getLikes().get(i).getCategory_list().size(); j++) {
							System.out.print(e.getLikes().get(i).getCategory_list().get(j).getName() + ", ");
						}
						System.out.println();
					}			
				}

			if (e.getFriends()!=null)
				for (int i=0; i<e.getFriends().size(); i++)
					System.out.println("Friend #" + i + ". " + e.getFriends().get(i));
		}
	
	}

}

