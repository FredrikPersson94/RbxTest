package globals;

import java.sql.Timestamp;
import java.text.NumberFormat;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ParseHelper {

	public static String dateTimeToString(DateTime dt, String pattern) {
		String dtStr = null;
		try {
			DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
			dtStr = dtf.print(dt);
		} catch (Exception e) {
			System.err.println("Could not convert dateTime " + dt + " with the pattern " + pattern);
		}
		return dtStr;
	}
	
	public static DateTime stringToDateTime(String dateStr, String pattern) {
		try {
			DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
			return dtf.parseDateTime(dateStr);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	/**
	 * 
	 * @param dt
	 * @return
	 */
	public static Timestamp getTimestampFromDateTime(DateTime dt) {
		try {
			return new Timestamp(dt.getMillis());
		} catch (Exception e) {
		}
		return getTimestampFromDateTime(new DateTime());
	}
	
	/**
	 * Parses a string to int
	 * 
	 * @param nbr
	 * @return the parsed number if successful, 0 if the string could not be parsed
	 */
	public static int strToInt(String nbr) {
		int val = 0;
		try {

			val = (int) strToDouble(nbr);

		} catch (Exception e) {
		}
		return val;
	}

	/**
	 * Parses a string to double
	 * 
	 * @param nbr
	 * @return the parsed number if successful, 0 if the string could not be parsed
	 */
	public static double strToDouble(String nbr) {
		double val = 0;
		try {
			val = Double.parseDouble(nbr);
		} catch (Exception e) {
//			System.err.println("cant parse " + e);
//			e.printStackTrace();
		}
		return val;
	}
	
	/**
	 * Parses a string to double
	 * 
	 * @param nbr
	 * @return the parsed number if successful, 0 if the string could not be parsed
	 */
	public static double strToDouble(String nbr, NumberFormat nf) {
		double val = 0;
		try {
			val = (double) nf.parse(nbr);
		} catch (Exception e) {
			System.err.println("cant parse " + e);
		}
		return val;
	}
	
	public static String[] parseNumbersAsStrInArray(String[] list) {
		if (list.length == 1) {
			String resName = list[0];
			String[] split = resName.split("\\.");
			if (split.length > 0) {
				list[0] = split[0];
			}
		}

		return list;
	}

	
}
