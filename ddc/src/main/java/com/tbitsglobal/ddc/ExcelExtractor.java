package com.tbitsglobal.ddc;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

public class ExcelExtractor {
	
	private List<String> fieldNames;
	
	private List<String> fieldTypes;
	
	private List<Map<Integer,Object>> values;
	
	private static Logger logger = Logger.getLogger(ExcelExtractor.class);

	public List<String> getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(List<String> fieldNames) {
		this.fieldNames = fieldNames;
	}

	public List<String> getFieldTypes() {
		return fieldTypes;
	}

	public void setFieldTypes(List<String> fieldTypes) {
		this.fieldTypes = fieldTypes;
	}

	public List<Map<Integer,Object>> getValues() {
		return values;
	}

	public void setValues(List<Map<Integer,Object>> values) {
		this.values = values;
	}
	
	public ExcelExtractor(){
		fieldNames = null;
		fieldTypes = null;
		values = new ArrayList<Map<Integer,Object>>();
	}

	public  void extract(String fileName) {

		try {
			
			InputStream inp = new FileInputStream(fileName);
//			System.out.println(FileContentExtracter.extractContent(new File("/home/rahul/Desktop/imported.xls")));
			Workbook wb = WorkbookFactory.create(inp);

			
			Sheet sheet = wb.getSheetAt(0);
//			System.out.println(sheet.getLastRowNum() + "," + sheet);
			boolean firstRow = true;
			boolean secondRow = false;
			boolean laterRows = false;
			for (Row row : sheet) {
				Map<Integer,Object> rowObjects = null;
				if(firstRow){
					fieldNames = new ArrayList<String>();
				}
				else if(secondRow){
					fieldTypes = new ArrayList<String>();
				}
				else {
					rowObjects = new HashMap<Integer,Object>();
				}
				for (Cell cell : row) {
					
					CellReference cellRef = new CellReference(row.getRowNum(),
							cell.getColumnIndex());
					int index = cell.getColumnIndex();
					

					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						if(firstRow){
							fieldNames.add(cell.getRichStringCellValue()
									.getString());
							
						}
						else if(secondRow){
							fieldTypes.add(cell.getRichStringCellValue()
									.getString());
						}
						else{
							rowObjects.put(index,cell.getRichStringCellValue().getString());
						}
						
						break;
					case Cell.CELL_TYPE_NUMERIC:
						if (DateUtil.isCellDateFormatted(cell)) {
							Date date = cell.getDateCellValue();
							rowObjects.put(index,date);
						} else {
							rowObjects.put(index,cell.getNumericCellValue());
							
						}
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						rowObjects.put(index,cell.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_FORMULA:
						rowObjects.put(index,cell.getCellFormula());
						
						break;
					default:
						
					}
					
					
					
				}
				if(firstRow){
					firstRow = false;
					secondRow = true;
				}
				else if(secondRow){
					secondRow = false;
					laterRows = true;
				}
				else {
					values.add(rowObjects);
				}
			}
			
			
//			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void printFieldNames(){
		System.out.println("fieldNames:"+fieldNames);
	}
	
	public void printFieldTypes(){
		System.out.println("fieldTypes:"+fieldTypes);
		
	}
	public void printValues(){
		
		System.out.println("values:");
		for(Map<Integer,Object> rowData : values){
			
			for(int i=0;i<fieldNames.size();i++){
				System.out.print(rowData.get(i)+", ");
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {
		ExcelExtractor extractor = new ExcelExtractor();
		extractor.extract("/home/rahul/Desktop/imported.xls");
		extractor.printFieldNames();
		extractor.printFieldTypes();
		extractor.printValues();
	}

}
