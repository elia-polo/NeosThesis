package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.UserUtility;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.gml.GMLReader;

public class Util {
	private static final Calendar now;
	private static final LRUCache<String, LatLng> geocoder_cache;
	private static final LRUCache<String, Map<String,Object>> geocoder_complex_cache;
	private static final Geocoder geocoder;

	// Since the resolution is the day, it is unlikely that the day will change
	// during program execution. A static initialization will suffice
	static {
		now = Calendar.getInstance();
		geocoder_cache = new LRUCache<String, LatLng>(500);
		geocoder_complex_cache = new LRUCache<String, Map<String,Object>>(500);
		geocoder = new Geocoder();
	}

	public static class GeocoderAddressComponentType {
		public static final String locality = "locality";
		public static final String administrative_area_level_2 = "administrative_area_level_2";
		public static final String administrative_area_level_1 = "administrative_area_level_1";
		public static final String country = "country";
		public static final String coordinates = "coordinates";
	}
	
	public static String getAge(String dob) {

		if (dob.equals("null"))
			return "null";

		int bDay = Integer.parseInt(dob);
		int yNow = now.get(Calendar.YEAR);

		if (bDay > yNow) {
			throw new IllegalArgumentException("Can't be born in the future");
		}

		return new Integer(yNow - bDay).toString();

	}

	public static String getYear(String date) {
		String[] split = date.split("/");
		if (split.length < 3)
			return "null";
		else
			return split[2];
	}

	public static Calendar parseDate(String date) {
		String[] split = date.split("/");
		return new GregorianCalendar(Integer.parseInt(split[2]),
				Integer.parseInt(split[0]) - 1, Integer.parseInt(split[1]));
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
			if (entry.getValue() == maxValueInMap) {
				return entry.getKey();
			}
		return "null"; // it may not happen
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
	 * Now very trivial discretize function with static label
	 * 
	 * TO DO: needing of the whole age set to make the function smart
	 *        (i.e dynamically labeled age) 
	 * 
	 * @param age, numerical value to discretize
	 * @return nominal value (very_young, young, not_so_young, not_young, not_so_old, old)
	 * 
	 * */
	public static String discretizeAge(Integer age) {
		if (age < 20) 
			return "very_young";
		else if (age >= 20 && age < 30)
			return "young";
		else if (age >= 30 && age < 40)
			return "not_so_young";
		else if (age >=40 && age < 50)
			return "not_young";
		else if (age >= 50 && age < 60)
			return "not_so_old";
		else //(age >=60 )
			return "old";
		
	}

	public static LatLng getCoordinates(String place) {
		LatLng result;
		if ((result = geocoder_cache.get(place)) == null) {
			GeocoderRequest request = new GeocoderRequestBuilder()
					.setAddress(place).setLanguage("en").getGeocoderRequest();
			boolean retried = false;
			do {
				GeocodeResponse response = geocoder.geocode(request);
				if(response.getStatus() == GeocoderStatus.OK) {
					// Pick the most relevant match
					result = response.getResults().get(0).getGeometry().getLocation();
					geocoder_cache.put(place, result);
					break;
				} else if(response.getStatus() == GeocoderStatus.OVER_QUERY_LIMIT) {
					if(!retried) {
						// Retry after an interval. If it fails again, daily limit was reached. Throw an exception
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
							// Try making a new request nonetheless
						}
						retried = true;
					} else {
						throw new IllegalStateException("Daily usage limit exceeded. No more requests can be fulfilled");
					}
				} else {
					System.err.println("Geocode failed with status "+response.getStatus());
					result = null;
					break;
				}
			} while(true);
		}
		return result;
	}
	
	public static Map<String,Object> getAddressStructure(String place) throws IllegalStateException {
		Map<String,Object> result;
		if ((result = geocoder_complex_cache.get(place)) == null) {
			GeocoderRequest request = new GeocoderRequestBuilder()
					.setAddress(place).setLanguage("en").getGeocoderRequest();
			boolean retried = false;
			do {
				GeocodeResponse response = geocoder.geocode(request);
				if(response.getStatus() == GeocoderStatus.OK) {
					// Pick the most relevant match
					List<GeocoderAddressComponent> tmp = response.getResults().get(0).getAddressComponents();
					result = new HashMap<String,Object>();
					for(GeocoderAddressComponent gac : tmp) {
						if(gac.getTypes().contains(GeocoderAddressComponentType.locality)) { // città
							result.put(GeocoderAddressComponentType.locality, gac.getLongName());
						} else if(gac.getTypes().contains(GeocoderAddressComponentType.administrative_area_level_2)) {
							result.put(GeocoderAddressComponentType.administrative_area_level_2, gac.getLongName());
						} else if(gac.getTypes().contains(GeocoderAddressComponentType.administrative_area_level_1)) {
							result.put(GeocoderAddressComponentType.administrative_area_level_1, gac.getLongName());
						} else if(gac.getTypes().contains(GeocoderAddressComponentType.country)) {
							result.put(GeocoderAddressComponentType.country, gac.getLongName());
						}
					}
					result.put(GeocoderAddressComponentType.coordinates, response.getResults().get(0).getGeometry().getLocation());
					geocoder_complex_cache.put(place, result);
					break;
				} else if(response.getStatus() == GeocoderStatus.OVER_QUERY_LIMIT) {
					if(!retried) {
						// Retry after an interval. If it fails again, daily limit was reached. Throw an exception
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
							// Try making a new request nonetheless
						}
						retried = true;
					} else {
						throw new IllegalStateException("Daily usage limit exceeded. No more requests can be fulfilled");
					}
				} else {
					System.err.println("Geocode failed with status "+response.getStatus());
					result = null;
					break;
				}
			} while(true);
		}
		return result;
	}
	
	public static String[] fromXSV(String s, String separator) {
		return s.split(separator);
	}

	public static <E> String toXSV(Collection<E> o, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (E tmp : o) {
			sb.append(sep).append(tmp.toString());
			sep = separator;
		}
		return sb.toString();
	}
	
	public static String toXSV(Object[] o, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (Object tmp : o) {
			sb.append(sep).append(tmp.toString());
			sep = separator;
		}
		return sb.toString();
	}
	
	public static String[] fromCSV(String s) {
		return s.split(",");
	}

	public static String toCSV(String[] s) {
		StringBuilder sb = new StringBuilder();
		String separator = "";
		for (String tmp : s) {
			sb.append(separator).append(tmp);
			separator = ",";
		}
		return sb.toString();
	}

	/**
	 * Introduces 5 geographical attributes for both hometown and location, whenever possible (hometown and location information is available), retrieved from Google geocoding API
	 * These attributes are: county (provincia), state (regione), country, latitude. longitude
	 * @param file the path to a GML encoded graph file
	 * @return a Tinkerpop Graph object
	 */
	public static Graph enhanceGmlWithDiscretizedGeograficalAttributes(String file) {
		// Enhance graph from Gml graph file
		Graph graph = new TinkerGraph();
		try {
			GMLReader.inputGraph(graph, file);
		} catch (IOException e1) {
			System.err.println(e1.getMessage());
			return null;
		}
		for(Vertex v : graph.getVertices(UserUtility.WHOAMI, "user")) {
			System.out.println("Vertex "+v.getId());
			// Assuming the attributes named 'hometown_name'and 'location_name' exist, enrich the graph with coordinates and names for intermediate geographical entities (counties, states)
			String s = v.getProperty(UserUtility.HOMETOWN_NAME);
			if(s != null && !s.equals("null")) {
				// Set additional properties, unless already present. This part is required due to limited service availability from Google geocoding API
				if(v.getProperty(UserUtility.HOMETOWN_COUNTY)==null && v.getProperty(UserUtility.HOMETOWN_STATE)==null && v.getProperty(UserUtility.HOMETOWN_COUNTRY)==null && v.getProperty(UserUtility.HOMETOWN_LAT)==null && v.getProperty(UserUtility.HOMETOWN_LNG)==null) {
					try {
						Map<String,Object> result = Util.getAddressStructure(s);
						if(result != null) {
							Object value = result.get(GeocoderAddressComponentType.administrative_area_level_2);
							if(value != null)
								v.setProperty(UserUtility.HOMETOWN_COUNTY, value);
							value = result.get(GeocoderAddressComponentType.administrative_area_level_1);
							if(value != null)
								v.setProperty(UserUtility.HOMETOWN_STATE, value);
							value = result.get(GeocoderAddressComponentType.country);
							if(value != null)
								v.setProperty(UserUtility.HOMETOWN_COUNTRY, value);
							value = ((LatLng)result.get(GeocoderAddressComponentType.coordinates)).getLat();
							if(value != null)
								v.setProperty(UserUtility.HOMETOWN_LAT, value);
							value = ((LatLng)result.get(GeocoderAddressComponentType.coordinates)).getLng();
							if(value != null)
								v.setProperty(UserUtility.HOMETOWN_LNG, value);
						}
					} catch(IllegalStateException e) {
						System.err.println(e.getMessage());
						break;
					}
				} // some geographical properties are already present (missing properties are assumed not to exist for the current record)
			}
			s = v.getProperty(UserUtility.LOCATION_NAME);
			if(s != null && !s.equals("null")) {
				if(v.getProperty(UserUtility.LOCATION_COUNTY)==null && v.getProperty(UserUtility.LOCATION_STATE)==null && v.getProperty(UserUtility.LOCATION_COUNTRY)==null && v.getProperty(UserUtility.LOCATION_LAT)==null && v.getProperty(UserUtility.LOCATION_LNG)==null) {
					// Set additional properties
					try {
						Map<String,Object> result = Util.getAddressStructure(s);
						if(result != null) {
							if(result != null) {
								Object value = result.get(GeocoderAddressComponentType.administrative_area_level_2);
								if(value != null)
									v.setProperty(UserUtility.LOCATION_COUNTY, value);
								value = result.get(GeocoderAddressComponentType.administrative_area_level_1);
								if(value != null)
									v.setProperty(UserUtility.LOCATION_STATE, value);
								value = result.get(GeocoderAddressComponentType.country);
								if(value != null)
									v.setProperty(UserUtility.LOCATION_COUNTRY, value);
								value = ((LatLng)result.get(GeocoderAddressComponentType.coordinates)).getLat();
								if(value != null)
									v.setProperty(UserUtility.LOCATION_LAT, value);
								value = ((LatLng)result.get(GeocoderAddressComponentType.coordinates)).getLng();
								if(value != null)
									v.setProperty(UserUtility.LOCATION_LNG, value);
							}
						}
					} catch(IllegalStateException e) {
						System.err.println(e.getMessage());
						break;
					}
				}
			}
		}
		return graph;
	}
	
	/**
	 * Geographical attributes are: hometown_county, hometown_state, hometown_country, hometown_lat, howetown_lng, locatio_county, location_state, location_country, location_lat, location_lng
	 * For each of these attributes, if missing, the property is set for each interested vertex with a default "null" string value
	 * @param file the path to a GML encoded graph file
	 * @return a Tinkerpop Graph object
	 */
	public static Graph ensureGeographicalAttributesExist(String file) {
		Graph graph = new TinkerGraph();
		try {
			GMLReader.inputGraph(graph, file);
		} catch (IOException e1) {
			System.err.println(e1.getMessage());
			return null;
		}
		for(Vertex v : graph.getVertices(UserUtility.WHOAMI, "user")) {
			if(v.getProperty(UserUtility.HOMETOWN_COUNTY)==null)
				v.setProperty(UserUtility.HOMETOWN_COUNTY, "null");
			if(v.getProperty(UserUtility.HOMETOWN_STATE)==null)
				v.setProperty(UserUtility.HOMETOWN_STATE, "null");
			if(v.getProperty(UserUtility.HOMETOWN_COUNTRY)==null)
				v.setProperty(UserUtility.HOMETOWN_COUNTRY, "null");
			if(v.getProperty(UserUtility.HOMETOWN_LAT)==null)
				v.setProperty(UserUtility.HOMETOWN_LAT, "null");
			if(v.getProperty(UserUtility.HOMETOWN_LNG)==null)
				v.setProperty(UserUtility.HOMETOWN_LNG, "null");
			if(v.getProperty(UserUtility.LOCATION_COUNTY)==null)
				v.setProperty(UserUtility.LOCATION_COUNTY, "null");
			if(v.getProperty(UserUtility.LOCATION_STATE)==null)
				v.setProperty(UserUtility.LOCATION_STATE, "null");
			if(v.getProperty(UserUtility.LOCATION_COUNTRY)==null)
				v.setProperty(UserUtility.LOCATION_COUNTRY, "null");
			if(v.getProperty(UserUtility.LOCATION_LAT)==null)
				v.setProperty(UserUtility.LOCATION_LAT, "null");
			if(v.getProperty(UserUtility.LOCATION_LNG)==null)
				v.setProperty(UserUtility.LOCATION_LNG, "null");
		}
		return graph;
	}
}
