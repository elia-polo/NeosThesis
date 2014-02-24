package utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Util {
	private static final Calendar now;
	
	// Since the resolution is the day, it is unlikely that the day will change during program execution. A static initialization will suffice
	static {
		now = Calendar.getInstance();
	}
	
	public static int getAge(Calendar dob) {
		if (dob.after(now)) {
			throw new IllegalArgumentException("Can't be born in the future");
		}
		int year1 = now.get(Calendar.YEAR);
		int year2 = dob.get(Calendar.YEAR);
		int age = year1 - year2;
		int month1 = now.get(Calendar.MONTH);
		int month2 = dob.get(Calendar.MONTH);
		if (month2 > month1) {
			age--;
		} else if (month1 == month2) {
			int day1 = now.get(Calendar.DAY_OF_MONTH);
			int day2 = dob.get(Calendar.DAY_OF_MONTH);
			if (day2 > day1) {
				age--;
			}
		}
		return age;
	}
	
	public static Calendar parseDate(String date) {
		String[] split = date.split("/");
		return new GregorianCalendar(Integer.parseInt(split[2]), Integer.parseInt(split[1])-1, Integer.parseInt(split[0]));
	}
}
