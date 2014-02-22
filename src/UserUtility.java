import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

class EUser {
	/* json user field */
	private String id;
	private boolean isDAU;
	private String gender;
	private String birthday;
	private String relationship_status;
	private String interested_in;
	
	private ArrayList<String> friends;
	private ArrayList<ELike> likes;
	
	private CoupleNameId hometown;
	private CoupleNameId location;
		
	//hometown, education, location
	private ArrayList<EEducation> edu;
	
	/* getter&setter */
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isDAU() {
		return isDAU;
	}
	public void setDAU(boolean isDAU) {
		this.isDAU = isDAU;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getRelationship_status() {
		return relationship_status;
	}
	public void setRelationship_status(String relationship_status) {
		this.relationship_status = relationship_status;
	}
	public String getInterested_in() {
		return interested_in;
	}
	public void setInterested_in(String interested_in) {
		this.interested_in = interested_in;
	}
	public ArrayList<String> getFriends() {
		return friends;
	}
	public void setFriends(ArrayList<String> friends) {
		this.friends = friends;
	}
	public ArrayList<ELike> getLikes() {
		return likes;
	}
	public void setLikes(ArrayList<ELike> likes) {
		this.likes = likes;
	}
	public CoupleNameId getHometown() {
		return hometown;
	}
	public void setHometown(CoupleNameId hometown) {
		this.hometown = hometown;
	}
	public CoupleNameId getLocation() {
		return location;
	}
	public void setLocation(CoupleNameId location) {
		this.location = location;
	}
	public ArrayList<EEducation> getEdu() {
		return edu;
	}
	public void setEdu(ArrayList<EEducation> edu) {
		this.edu = edu;
	}
}

class ELike {
	private String id;
	private String name;
	private String category;
	
	private ArrayList<CoupleNameId> category_list;
		
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	public ArrayList<CoupleNameId> getCategory_list() {
		return category_list;
	}
	public void setCategory_list(ArrayList<CoupleNameId> category_list) {
		this.category_list = category_list;
	}	
}

class EEducation {
	CoupleNameId name_id;
	String year;
	String type;

	public void SetNameId(String name, String id) {
		name_id.setName(name);
		name_id.setId(id);
	}
	
	public CoupleNameId getName_id() {
		return name_id;
	}
	public void setName_id(CoupleNameId name_id) {
		this.name_id = name_id;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}

class CoupleNameId {
	private String id;
	private String name;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public CoupleNameId() { }
	public CoupleNameId(String name, String id) {
		this.name = name;
		this.id = id;
	}
}


class JsonUser {
	
	private String json_file;
	private JsonObject jsonObj;
	private EUser user;
	
	/* json tag */
	private final String ID = "id";
	private final String GENDER = "gender";
	private final String REL_STATUS = "relationship_status";
	private final String INTERESTED_IN = "interested_in";
	private final String BIRTHDAY = "birthday";
	private final String HOMETOWN = "hometown";
	private final String LOCATION = "location";
	private final String NAME = "name";
	private final String FRIENDS = "friends";
	private final String MUTUALFRIENDS = "mutualfriends";
	private final String LIKES = "likes";
	private final String CATEGORY = "category";
	private final String CATEGORY_LIST = "category_list";
	
	private final String EDUCATION = "education";
	private final String SCHOOL = "school";
	private final String YEAR = "year";
	private final String TYPE = "type";
	
	
	JsonUser(String filename) throws FileNotFoundException, IOException {
		
		json_file = filename;
		user = new EUser();
		
		BufferedReader br;		
		br = new BufferedReader(new FileReader(json_file));   /* open the user json file 								
															   * could throws FileNotFoundException */
		
		jsonObj = JsonObject.readFrom(br);                    /* read the whole json object 
		 													   * could throws IOExcetpion */
		br.close();
		
	}
	
	EUser getEUser() {
		
		if (json_file.contains("DAU"))           // if the filename contains the DAU substring  
			user.setDAU(true);                   // then it is a DAU user
		else user.setDAU(false);
		
		/* 
		 * Getting id, gender, birthday and relationship_status fields.  
		 * NB: the get_str_value function returns null if the
		 * specified tag doesn't exist in the json object .
		 * */
		
		user.setId(jsonObj.get_str_value(ID));
		user.setGender(jsonObj.get_str_value(GENDER));
		user.setRelationship_status(jsonObj.get_str_value(REL_STATUS));
		user.setBirthday(jsonObj.get_str_value(BIRTHDAY));
		
		/*
		 * checks if interested_in exists, in that case 
		 * sets the field with the right value, else with null
		 * */
		
		try {
			user.setInterested_in(jsonObj.get(INTERESTED_IN).asArray().get(0).asString());
		} catch(NullPointerException e) {
			user.setInterested_in(null);
		}
		
		
		/* if exists education field */
		try {
			/* creates an education array */
			
			JsonArray edu_array = jsonObj.get(EDUCATION).asArray();
			
			ArrayList<EEducation> edu_list = new ArrayList<EEducation>();
			
			for (JsonValue single_edu : edu_array)
			{
				/* single education element */
				EEducation edu = new EEducation();
				
				/* set edu 'type' fields */
				edu.setType(single_edu.asObject().get_str_value(TYPE));
				
				/* if exists 'school' fields... */
				try {
					edu.setName_id(new CoupleNameId(single_edu.asObject().get(SCHOOL).asObject().get_str_value(NAME),
								   single_edu.asObject().get(SCHOOL).asObject().get_str_value(ID)));
				} catch (NullPointerException e) { edu.setName_id(null); }
				
				try  {
					edu.setYear(single_edu.asObject().get(YEAR).asObject().get_str_value(NAME));
				} catch (NullPointerException e) { edu.setYear(null); }
				
				edu_list.add(edu);
			}
			user.setEdu(edu_list);
		} catch(NullPointerException e) { user.setEdu(null); }
		
		/* getting hometown&location fields */
		JsonValue where_obj;
		CoupleNameId where = new CoupleNameId();
		
		try {
		    where_obj = jsonObj.get(HOMETOWN);
			where.setId(where_obj.asObject().get_str_value(ID));
			where.setName(where_obj.asObject().get_str_value(NAME));
			user.setHometown(where);
		} catch (NullPointerException e) { user.setHometown(null); }
		
		where = new CoupleNameId();
		
		try { 
			where_obj = jsonObj.get(LOCATION);
			where.setId(where_obj.asObject().get_str_value(ID));
			where.setName(where_obj.asObject().get_str_value(NAME));
			user.setLocation(where);
		} catch(NullPointerException e) { user.setLocation(null); }
		
		/* getting id friends */
		JsonObject friends_obj;
		
		String fr;
		if (user.isDAU()==true) fr = FRIENDS;
		else fr = MUTUALFRIENDS;
		
		try {

			friends_obj = jsonObj.get(fr).asObject();
			JsonArray friends = friends_obj.get("data").asArray();
			ArrayList<String> friends_id = new ArrayList<String>();
			
			for (JsonValue f : friends)
				friends_id.add(f.asObject().get_str_value(ID));
		
			user.setFriends(friends_id);
		} catch (NullPointerException e) { user.setFriends(null); }
		
		
		/* getting like objects */
		JsonObject likes_obj;
		
		/* if ec*/
		try 
		{
			likes_obj = jsonObj.get(LIKES).asObject();
			
			/* JSON likes array */
			JsonArray likes = likes_obj.get("data").asArray();
			
			/* with this likes_array must be filled
			 * the user fields */
			ArrayList<ELike> likes_array = new ArrayList<ELike>();
			
			for (JsonValue l : likes)
			{
				/* single like */
				ELike like = new ELike();
				
				like.setId(l.asObject().get_str_value(ID));
				like.setCategory(l.asObject().get_str_value(CATEGORY));
				like.setName(l.asObject().get_str_value(NAME));				
				
				try  {
					/* JSON category list array */
					JsonArray cl = l.asObject().get(CATEGORY_LIST).asArray();
					
					/* with this cat_list must be filled the 'like' object */
					ArrayList<CoupleNameId> cat_list = new ArrayList<CoupleNameId>();
				
					/* filling out the cat_list array with the entries
					 * from json category list array */
					for (JsonValue c : cl) 
						cat_list.add(new CoupleNameId(c.asObject().get_str_value(NAME), 
								c.asObject().get_str_value(ID)));
			
					like.setCategory_list(cat_list);
				} catch (NullPointerException e) { like.setCategory_list(null); }
				likes_array.add(like);
			}
			user.setLikes(likes_array);
		} catch(NullPointerException e) { user.setLikes(null); }
		
		return user;
	}
}

public class UserUtility {
	/* for debug  purposes*/
	public static void main(String args[])  throws Exception  {
		File inputFolder = new File("/home/np2k/Desktop/json_user");
		
		int count = 0;
		for (File fn : inputFolder.listFiles()) {
			System.out.println("-----------------------------------" + count +"------------------------------------------");
			count++;
			String name = fn.getAbsolutePath();
			System.out.println(name);
			JsonUser ju = new JsonUser(name);
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
