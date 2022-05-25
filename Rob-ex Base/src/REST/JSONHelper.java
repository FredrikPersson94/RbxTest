package REST;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONHelper {
	/**
	 * Converts a JsonElement to a string
	 * 
	 * @param jE
	 * @return
	 */
	public static int jsonElementToInt(JsonElement jE) {
		int i = 0;
		if (jE != null) {
			try {
				i = Integer.parseInt(jE.getAsString());
			} catch (Exception e) {
				System.out.println("Could not parse " + jE.toString());
			}
		}
		return i;
	}

	/**
	 * Converts a JsonElement to a String
	 * 
	 * @param jE
	 * @return
	 */
	public static String jsonElementToString(JsonElement jE) {
		if (jE != null) {
			return jE.getAsString();
		}
		return null;
	}

	public static boolean jsonElementToBoolean(JsonElement jE) {
		if (jE != null) {
			return jE.getAsBoolean();
		}
		return false;
	}

	/**
	 * Converts a JsonElement to a JsonArray
	 * 
	 * @param jE
	 * @return
	 */
	public static JsonArray parseJsonElementToJsonArray(JsonElement jE) {
		try {
			return jE.getAsJsonArray();
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * Converts a JsonElement to a JsonObject
	 * 
	 * @param jE
	 * @return
	 */
	public static JsonObject parseJsonElementToJsonObject(JsonElement jE) {
		try {
			return jE.getAsJsonObject();
		} catch (Exception e) {

		}
		return null;
	}

	public static JsonArray parseStringAsJsonArray(String jSON) {
		try {
			@SuppressWarnings("deprecation")
			JsonElement jE = new JsonParser().parse(jSON);
			return parseJsonElementToJsonArray(jE);
		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}

	public static JsonObject parseStringAsJsonObject(String jSON) {
		try {
			@SuppressWarnings("deprecation")
			JsonElement jE = new JsonParser().parse(jSON);
			return parseJsonElementToJsonObject(jE);
		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}
}
