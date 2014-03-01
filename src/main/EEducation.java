package main;

public class EEducation {
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