package utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.LatLng;


public class Util {
	private static final Calendar now;

	// Since the resolution is the day, it is unlikely that the day will change during program execution. A static initialization will suffice
	static {
		now = Calendar.getInstance();
	}

	public static String getAge(String dob) {
		
		if (dob.equals("null"))
			return "null";
		
		int bDay = Integer.parseInt(dob);
		int yNow = now.get(Calendar.YEAR);
		
		if (bDay > yNow) {
			throw new IllegalArgumentException("Can't be born in the future");
		}
	
		return new Integer(yNow-bDay).toString();


	}

	public static String getYear(String date) {
		String [] split = date.split("/");
		if (split.length < 3)
			return "null";
		else return split[2];
	}
	
	public static Calendar parseDate(String date) {
		  String[] split = date.split("/");
		  return new GregorianCalendar(Integer.parseInt(split[2]), Integer.parseInt(split[0])-1, Integer.parseInt(split[1]));
		}
	
	public static String[] fromCSV(String s) {
		return s.split(",");
}
	public static String[] fromXSV(String s, String separator) {
		return s.split(separator);
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

	public static String toXSV(Object[] o, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for(Object tmp : o) {
			sb.append(sep).append(tmp.toString());
			sep = separator;
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
	/**
	 * TO DO!
	 * 
	 * */
	public static String discretizeAge(Integer age) {
		return "young";
	}

	public static LatLng getCoordinates(String place) {
		final Geocoder geocoder = new Geocoder();
		GeocoderRequest request = new GeocoderRequestBuilder()
				.setAddress(place).setLanguage("en")
				.getGeocoderRequest();
		GeocodeResponse response = geocoder.geocode(request);
		if(!response.getResults().isEmpty()) {
			// Pick the most relevant match
			return response.getResults().get(0).getGeometry().getLocation();
		} else {
			return null;
		}
	}
}