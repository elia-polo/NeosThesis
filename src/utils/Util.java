package utils;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

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
	
	public static String[] fromXSV(String s, String separator) {
		return s.split(separator);
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
	
	public static String toXSV(Collection<Object> o, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for(Object tmp : o) {
			sb.append(sep).append(tmp.toString());
			sep = separator;
		}
		return sb.toString();
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
