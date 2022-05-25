
package imports;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Row;

public class ExcelReader {

	private FormulaEvaluator evaluator;
	private Workbook wb;
	
	public ExcelReader(File file) {
		try {
			wb = WorkbookFactory.create(new FileInputStream(file));
			evaluator = wb.getCreationHelper().createFormulaEvaluator();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Sheet getSheetAt(int i) {

		return wb.getSheetAt(i);
	}

	public int getNbrOfSheets() {
		return wb.getNumberOfSheets();
	}

	public List<Sheet> getSheets() {
		List<Sheet> list = new ArrayList<Sheet>();
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			list.add(wb.getSheetAt(i));
		}
		return list;
	}
	
	
	public Sheet getSheetWithName(String sheetName) {
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(i);
			if (sheet.getSheetName().equals(sheetName)) {
				return sheet;
			}
		}
		return null;
	}
	
	/**
	 * Returns the value of a cell as a String
	 * @param c
	 * @return
	 */
	public String getCellValue(Cell c) {
		try {

			CellValue cv = evaluator.evaluate(c);
			if (cv == null) {
				return null;
			}
			switch (cv.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				return cv.getStringValue();
			case Cell.CELL_TYPE_NUMERIC:
				Double val = cv.getNumberValue();
				return val.toString();
			case Cell.CELL_TYPE_FORMULA:
				switch (c.getCachedFormulaResultType()) {
				case Cell.CELL_TYPE_NUMERIC:
					Double val2 = c.getNumericCellValue();
					return val2.toString();
				case Cell.CELL_TYPE_STRING:
					return c.getRichStringCellValue().getString();
				}
			}
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * Reads the sheet and collects row info
	 * 
	 * @param sheet
	 * @return a list of row values
	 */
	public List<String[]> getRowValuesAsStr(Sheet sheet, int startRow, int nbrOfCols) {
		// read the Excel sheet into lists itemNames and itemAmounts
		List<String[]> rowVals = new ArrayList<String[]>();
		for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext();) {
			Row row = rit.next();
			if (row.getRowNum() < startRow) { // Ignore the headers
				continue;
			}

			rowVals.add(getRowValueAsStr(row, nbrOfCols));
		}
		return rowVals;
	}
	
	/**
	 * Returns a row as an array of Strings
	 * @param row
	 * @param nbrOfCols
	 * @return
	 */
	public String[] getRowValueAsStr(Row row, int nbrOfCols) {
		String[] rowValue = new String[nbrOfCols];
		for (int i = 0; i < nbrOfCols; i++) {

			Cell cell = row.getCell(i);
			if (cell != null) {
				String cellValue = getCellValue(cell);
				rowValue[i] = cellValue;
			} else {
				rowValue[i] = "";
			}
		}
		return rowValue;
	}
	
	/**
	 * Returns a map with all product names as keys and dimensions of the product as value.
	 * @param sheet
	 * @return
	 */
	
    public HashMap<String, String> mapSheet(Sheet sheet){
        HashMap<String, String> map = new HashMap<String, String>();
        int i = 0;
        Iterator<Row> rit = sheet.rowIterator();
        Row row = rit.next();
        rit.next();
        String key = getCellValue(row.getCell(i));
        String value = getCellValue(row.getCell(i+3));
        map.put(key, value); 
        
        while (rit.hasNext()) {
                   
            row = rit.next();
            key = getCellValue(row.getCell(i));
            value = getCellValue(row.getCell(i+3));
            
            if (key == null) {
                return map;
            } else {
                map.put(key, value);
            }
        }
        return map;
            
    }

}