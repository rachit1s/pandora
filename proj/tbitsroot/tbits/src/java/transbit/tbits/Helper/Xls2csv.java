/*
 * Xls2csv.java
 *
 * Created on August 12, 2006, 5:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package transbit.tbits.Helper;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 *
 * @author Administrator
 */
public class Xls2csv {
    
    private String cellSeparator = ",";
    private String lineSeparator = "\n";
    private String sheetSeparator = "\n";
    public String getText(String fileName) throws FileNotFoundException, IOException {
        return getText(new FileInputStream(fileName));
    }
    
    /*
     * Converts an excel sheet to csv - a comma separated file.
     * Note: A formala is evaluated and the corresponding numeric value is appended.
     *
     */
    public String getText(FileInputStream fiS) throws IOException {
        POIFSFileSystem fs =   new POIFSFileSystem(fiS);
        HSSFWorkbook workBook = new HSSFWorkbook(fs);
        int numOfSheets = workBook.getNumberOfSheets();
        StringBuffer output = new StringBuffer();
        boolean isFirstSheet = true;
        for (int i = 0; i < numOfSheets; i++) {
            if(isFirstSheet)
                isFirstSheet = false;
            else
                output.append(sheetSeparator);
            HSSFSheet sheet = workBook.getSheetAt(i);
            Iterator rowIterator = sheet.rowIterator();
            boolean isFirstRow = true;
            while(rowIterator.hasNext()) {
                if(isFirstRow)
                    isFirstRow = false;
                else
                    output.append(lineSeparator);
                HSSFRow row = (HSSFRow) rowIterator.next();
                Iterator cellIterator = row.cellIterator();
                
                boolean isFirstCell = true;
                while(cellIterator.hasNext()) {
                    if(isFirstCell) {
                        isFirstCell = false;
                    } else
                        output.append(cellSeparator);
                    //TODO: Comment out ","
                    HSSFCell cell = (HSSFCell) cellIterator.next();
                    int cellType = cell.getCellType();
                    switch(cellType) {
                        case HSSFCell.CELL_TYPE_BOOLEAN:
                            output.append(commentString(Boolean.toString(cell.getBooleanCellValue())));
                            break;
                        case HSSFCell.CELL_TYPE_FORMULA:
                        case HSSFCell.CELL_TYPE_NUMERIC:
                            output.append(commentString(Double.toString(cell.getNumericCellValue())));
                            break;
                        case HSSFCell.CELL_TYPE_STRING:
                            output.append(commentString(cell.getStringCellValue()));
                            break;
                        default:
                            break;
                    }//end switch itr
                }//end cell itr
            }//end row itr
        }//end sheet itr
        return output.toString();
    }
    
    private String commentString(String str) {
        return str.replaceAll(cellSeparator, "\"" + cellSeparator + "\"");
    }
    
    public String getCellSeparator() {
        return cellSeparator;
    }
    
    public void setCellSeparator(String cellSeparator) {
        this.cellSeparator = cellSeparator;
    }
    public static void main(String[] args) {
        if(args.length != 1)
        {
            System.out.println("Xls2csv <file name>");
            return;
        }
        try {
            System.out.println((new Xls2csv()).getText(args[0]));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

