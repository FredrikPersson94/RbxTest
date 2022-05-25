package imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import globals.ParseHelper;

/**
 * Class for reading CVS files
 * 
 * @author fredrikp
 *
 */
public class CSVReader {

	/**
	 * Reads a CSV file and return them as a list with array of strings
	 * 
	 * @param fileLocation
	 * @param separator
	 * @param startRow
	 * @return
	 */
	public static List<String[]> importCSV(File file, String separator, int startRow) {
		List<String[]> csvList = new ArrayList<String[]>();
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(file));
			String row;
			int i = 0;
			while ((row = csvReader.readLine()) != null) {

				if (i++ >= startRow) {
					csvList.add(stringToStrArray(row, separator));
				}
			}
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return csvList;
	}

	public static List<String[]> splitIntoStringMatrix(String csvStr, String separator1, String separator2) {

		String[] split = csvStr.split(separator1);
		List<String[]> matrix = new ArrayList<String[]>();
		for (String string : split) {
			String[] stringToStrCsv = stringToStrArray(string, separator2);
			matrix.add(stringToStrCsv);
		}

		return matrix;
	}

	public static List<int[]> splitIntoIntMatrix(String csvStr, String separator1, String separator2) {

		String[] split = csvStr.split(separator1);
		List<int[]> matrix = new ArrayList<int[]>();
		for (String string : split) {
			int[] stringToIntCsv = stringToIntArray(string, separator2);
			if(stringToIntCsv == null || stringToIntCsv.length == 0){
				stringToIntCsv = new int[] {ParseHelper.strToInt(string)};
			}
			matrix.add(stringToIntCsv);

		}

		return matrix;
	}

	/**
	 * Converts a CVS string to an array of strings
	 * 
	 * @param csvStr
	 * @param separator
	 * @return
	 */
	public static String[] stringToStrArray(String csvStr, String separator) {
		if (csvStr != null && !csvStr.isEmpty()) {
			return csvStr.split(separator);
		}
		return new String[0];
	}

	/**
	 * Converts a CVS string to an array of strings
	 * 
	 * @param csvStr
	 * @param separator
	 * @return
	 */
	public static int[] stringToIntArray(String csvStr, String separator) {
		try {

			if (csvStr != null && !csvStr.isEmpty()) {
				return Stream.of(csvStr.split(separator)).mapToInt(Integer::parseInt).toArray();

			}
		} catch (Exception e) {
		}
		return null;
	}

}
