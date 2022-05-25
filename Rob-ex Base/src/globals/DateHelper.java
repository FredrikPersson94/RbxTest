package globals;

import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateHelper {

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

	
}
