package utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;


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
		return new GregorianCalendar(Integer.parseInt(split[2]), Integer.parseInt(split[0])-1, Integer.parseInt(split[1]));
	}
	
	public static String[] fromCSV(String s) {
		return s.split(",");
	}

	public static String toCSV(String[] s) {
		StringBuilder sb = new StringBuilder();
		String separator = "";
		for(String tmp : s) {
			sb.append(separator).append(tmp);
			separator = ",";
		}
		return sb.toString();
	}
	/* now it calculates mean (not median) value */
	public static Integer median(ArrayList<Integer> list) {
		Integer res = new Integer(0);
		for (Integer i : list)
			res += i;
		
		return (res /= list.size());
	}
	
	/* returns the (first) key with max value */
	public static String retMax(HashMap<String, Integer> map) {
		/* this will return max value in the hashmap */
		int maxValueInMap = (Collections.max(map.values()));
		
		for (java.util.Map.Entry<String, Integer> entry : map.entrySet()) 
            if (entry.getValue()==maxValueInMap) {
                return entry.getKey();
            }
		return "null"; //it may not happen
	}
	
	/* increments the value of 'key' */
	public static void incValue(HashMap<String, Integer> map, String key) {
		int value = 0;
		if (map.containsKey(key)) {
			value = map.get(key);
			value++;
			map.put(key, value);
		} else 
			map.put(key, 1);				

	}
	
	public static String[] fromCSV(String s) {
		return s.split(",");
	}
	
	public static String toCSV(String[] s) {
		StringBuilder sb = new StringBuilder();
		String separator = "";
		for(String tmp : s) {
			sb.append(separator).append(tmp);
			separator = ",";
		}
		return sb.toString();
	}
}
