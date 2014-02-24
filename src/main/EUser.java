package main;

import java.util.ArrayList;

public class EUser {
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