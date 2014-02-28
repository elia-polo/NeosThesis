package metrics;

import java.text.DecimalFormat;

public class Stats {
	// Proper users, possibly with properties
	private int user_count = 0;
	// Empty nodes allowed, therefore node_count >= user_count
	private int node_count = 0;
	private int like_count = 0;
	private int edge_count = 0; 
	private int gender = 0;
	private int rel_status = 0;
	private int interested_in = 0;
	private int birthday = 0;
	private int hometown = 0;
	private int location = 0;
	private int friends = 0;
//	private int mutual_friends = 0;
	private int likes = 0;
	private int category = 0;
	private int category_list = 0;
	private int education = 0;
	private int high_school = 0;
	private int college = 0;
	private int graduate_school = 0;
	
	public int getUserCount() {
		return user_count;
	}
	public void incrementUserCount() {
		++this.user_count;
	}
	public int getNodeCount() {
		return node_count;
	}
	public void incrementNodeCount() {
		++this.node_count;
	}
	public int getLikeCount() {
		return like_count;
	}
	public void incrementLikeCount() {
		++this.like_count;
	}
	public int getEdgeCount() {
		return edge_count;
	}
	public void incrementEdgeCount() {
		++this.edge_count;
	}
	public int getGender() {
		return gender;
	}
	public void incrementGender() {
		++this.gender;
	}
	public int getRelStatus() {
		return rel_status;
	}
	public void incrementRelStatus() {
		++this.rel_status;
	}
	public int getInterestedIn() {
		return interested_in;
	}
	public void incrementInterestedIn() {
		++this.interested_in;
	}
	public int getBirthday() {
		return birthday;
	}
	public void incrementBirthday() {
		++this.birthday;
	}
	public int getHometown() {
		return hometown;
	}
	public void incrementHometown() {
		++this.hometown;
	}
	public int getLocation() {
		return location;
	}
	public void incrementLocation() {
		++this.location;
	}
	public int getFriends() {
		return friends;
	}
	public void incrementFriends() {
		++this.friends;
	}
//	public int getMutualFriends() {
//		return mutual_friends;
//	}
//	public void incrementMutualFriends() {
//		++this.mutual_friends;
//	}
	public int getLikes() {
		return likes;
	}
	public void incrementLikes() {
		++this.likes;
	}
	public int getCategory() {
		return category;
	}
	public void incrementCategory() {
		++this.category;
	}
	public int getCategoryList() {
		return category_list;
	}
	public void incrementCategoryList() {
		++this.category_list;
	}
	public int getEducation() {
		return education;
	}
	public void incrementEducation() {
		++this.education;
	}
	public int getHighSchool() {
		return high_school;
	}
	public void incrementHighSchool() {
		++this.high_school;
	}
	public int getCollege() {
		return college;
	}
	public void incrementCollege() {
		++this.college;
	}
	public int getGraduateSchool() {
		return graduate_school;
	}
	public void incrementGraduateSchool() {
		++this.graduate_school;
	}
	
	public String toString() {
		DecimalFormat df = new DecimalFormat("#0.#");
		StringBuilder sb = new StringBuilder();
		sb.append("Unique users: ").append(user_count).append(System.lineSeparator());
		sb.append("Total nodes: ").append(node_count).append(System.lineSeparator());
		sb.append("Unique likes: ").append(like_count).append(System.lineSeparator());
		sb.append("Total edges: ").append(edge_count).append(System.lineSeparator());
		sb.append("Gender missing ratio: ").append((int)100-gender/user_count*100.0).append("%").append(System.lineSeparator());
		sb.append("Birthday missing ratio: ").append(df.format(100-100.0*birthday/user_count)).append("%").append(System.lineSeparator());
		sb.append("Relationship missing ratio: ").append(df.format(100-100.0*rel_status/user_count)).append("%").append(System.lineSeparator());
		sb.append("InterestedIn missing ratio: ").append(df.format(100-100.0*interested_in/user_count)).append("%").append(System.lineSeparator());
		sb.append("Hometown missing ratio: ").append(df.format(100-100.0*hometown/user_count)).append("%").append(System.lineSeparator());
		sb.append("Location missing ratio: ").append(df.format(100-100.0*location/user_count)).append("%").append(System.lineSeparator());
		sb.append("Friends missing ratio: ").append(df.format(100-100.0*friends/user_count)).append("%").append(System.lineSeparator());
//		sb.append("MutualFriends missing ratio: ").append(Float.toString((1-(float)mutual_friends/user_count)*100)).append("%").append(System.lineSeparator());
		sb.append("Likes missing ratio: ").append(df.format(100-100.0*likes/user_count)).append("%").append(System.lineSeparator());
		sb.append("Education missing ratio: ").append(df.format(100-100.0*education/user_count)).append("%").append(System.lineSeparator());
		sb.append("HighSchool missing ratio: ").append(df.format(100-100.0*high_school/user_count)).append("%").append(System.lineSeparator());
		sb.append("College missing ratio: ").append(df.format(100-100.0*college/user_count)).append("%").append(System.lineSeparator());
		sb.append("GraduateSchool missing ratio: ").append(df.format(100-100.0*graduate_school/user_count)).append("%").append(System.lineSeparator());
		return sb.toString();
	}
}