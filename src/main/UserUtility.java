package main;
import java.util.ArrayList;

public class UserUtility {
	public final static String ID = "id";
	public final static String GENDER = "gender";
	public final static String MALE = "male";
	public final static String FEMALE = "female";
	public final static String REL_STATUS = "relationship_status";
	public final static String INTERESTED_IN = "interested_in";
	public final static String BIRTHDAY = "birthday";
	public final static String AGE = "age";
	public final static String HOMETOWN = "hometown";
	public final static String LOCATION = "location";
	public final static String HOMETOWN_NAME = "hometown_name";
	public final static String LOCATION_NAME = "location_name";
	public final static String HOMETOWN_LAT = "hometown_lat";
	public final static String HOMETOWN_LNG = "hometown_lng";
	public final static String HOMETOWN_COUNTY = "hometown_county";
	public final static String HOMETOWN_STATE = "hometown_state";
	public final static String HOMETOWN_COUNTRY = "hometown_country";
	public final static String LOCATION_LAT = "location_lat";
	public final static String LOCATION_LNG = "location_lng";
	public final static String LOCATION_COUNTY = "location_county";
	public final static String LOCATION_STATE = "location_state";
	public final static String LOCATION_COUNTRY = "location_country";
	public final static String NAME = "name";
	public final static String FRIENDS = "friends";
	public final static String FRIEND = "friend";
	public final static String MUTUALFRIENDS = "mutualfriends";
	public final static String LIKES = "likes";
	public final static String CATEGORY = "category";
	public final static String CATEGORY_LIST = "category_list";
	public final static String EDUCATION = "education";
	public final static String SCHOOL = "school";
	public final static String YEAR = "year";
	public final static String TYPE = "type";
	public final static String HIGH_SCHOOL = "High School";
	public final static String COLLEGE = "College";
	public final static String GRADUATE_SCHOOL = "Graduate School";
	public final static String HIGHSCHOOL = "High_School";
	public final static String GRADUATESCHOOL = "Graduate_School";
	public final static String WHOAMI = "whoami";
	public final static String ISDAU = "is_dau";
	
	
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
