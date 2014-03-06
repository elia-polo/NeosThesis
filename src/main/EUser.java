package main;

import java.util.ArrayList;

import utils.Util;

public class EUser {
	/* json user field */
	private String id;
	private boolean isDAU;
	private String gender;
	private String birthday;
	private int age;
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
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
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

	/**
	 * Formats the Education history of the current user as a vector with three components: <i>HIGH SCHOOL</i>, <i>COLLEGE</i>, <i>GRADUATE SCHOOL</i>.<br>
	 * Each component may take value 1 or 0. 1 means that the user has provided a value for that education level in his profile, 0 means the information is absent (either undisclosed or not applicable)
	 * @return
	 */
	public String[] getEduVec() {
		String[] tmp = {"0","0","0"};
		for(EEducation e: edu) {
			if(e.getType() != null) {
				switch(e.getType()) {
				case UserUtility.HIGH_SCHOOL:
					tmp[0] = "1";
					break;
				case UserUtility.COLLEGE:
					tmp[1] = "1";
					break;
				case UserUtility.GRADUATE_SCHOOL:
					tmp[2] = "1";
					break;
				default:
					throw new IllegalArgumentException("Education type "+e.type+" does not exist");
				}
			}
		}
		return tmp;
	}
	public String getEduCSV() {
		return Util.toXSV(getEduVec(), ",");
	}
	public void setEdu(ArrayList<EEducation> edu) {
		this.edu = edu;
	}

}