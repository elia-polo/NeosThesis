import java.util.ArrayList;

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

class UserTags {
	public final static String ID = "id";
	public final static String GENDER = "gender";
	public final static String REL_STATUS = "relationship_status";
	public final static String INTERESTED_IN = "interested_in";
	public final static String BIRTHDAY = "birthday";
	public final static String HOMETOWN = "hometown";
	public final static String LOCATION = "location";
	public final static String NAME = "name";
	public final static String FRIENDS = "friends";
	public final static String MUTUALFRIENDS = "mutualfriends";
	public final static String LIKES = "likes";
	public final static String CATEGORY = "category";
	public final static String CATEGORY_LIST = "category_list";
	public final static String EDUCATION = "education";
	public final static String SCHOOL = "school";
	public final static String YEAR = "year";
	public final static String TYPE = "type";
	public final static String WHOAMI = "whoami";
	
}