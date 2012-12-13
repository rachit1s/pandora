/*
 * ReportHelper.java
 *
 * Created on July 2, 2006, 6:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package transbit.tbits.Helper;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TimeZone;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.Field;
import transbit.tbits.search.Result;
import java.util.Hashtable;

import static transbit.tbits.domain.DataType.BOOLEAN;
import static transbit.tbits.domain.DataType.DATE;
import static transbit.tbits.domain.DataType.DATETIME;
import static transbit.tbits.domain.DataType.INT;
import static transbit.tbits.domain.DataType.USERTYPE;
import static transbit.tbits.domain.DataType.REAL;
import static transbit.tbits.domain.DataType.STRING;
import static transbit.tbits.domain.DataType.TEXT;
import static transbit.tbits.domain.DataType.TIME;
import static transbit.tbits.domain.DataType.TYPE;
import transbit.tbits.webapps.WebUtil;
/**
 *
 * @author Administrator
 */
public class ReportHelper {
    private  ArrayList<String> firstVariantValues ;
    private  ArrayList<String> secondVariantValues ;
    private  Hashtable<String,Hashtable <String,Integer>> categoryChartData;
    private  Hashtable<String,Integer> pieChartData;
    
    public static ArrayList<String> getFieldAllValues(Hashtable<String, Result> requests , String field,int sys_id) throws DatabaseException{
        ArrayList<String> fieldValues = new  ArrayList<String>();
        Enumeration requestEnum = requests.keys();
        Field fieldDetails =Field.lookupBySystemIdAndFieldName( sys_id ,field);
        while(requestEnum.hasMoreElements()) {
            String key = (String) requestEnum.nextElement();
            /*
             *get next result
             */
            Result rs = requests.get(key);
            //get value of first variant in the result
            Object fieldValueObj = rs.get(field);
            if(fieldValueObj == null){
                continue;
            }
            String fieldValue = ConvertToString(fieldDetails ,fieldValueObj);
            //value is not new
            if(fieldValues.indexOf(fieldValue)==-1){
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }
    
      /*
       *default constructor
       */
    public ReportHelper(){
        
    }
    /**
     *
     * Prepares pie chart for single image
     */
    public ReportHelper(Hashtable<String, Result> requests , String firstField , int sys_id) throws DatabaseException {
        /*
         *initialize Arraylist to hold filds possible Values
         */
        setFirstVariantValues(new ArrayList<String>());
        setPieChartData(new Hashtable<String, Integer>());
        Field firstFieldDetails =Field.lookupBySystemIdAndFieldName( sys_id ,firstField );
        /*
         *retrive  request enum to walk through all requests
         */
        Enumeration requestEnum = requests.keys();
        while(requestEnum.hasMoreElements()) {
            String key = (String) requestEnum.nextElement();
            //  out.println("Entering loop "+ "size is "+ requests.size()+"<br>");
            /*
             *get next result
             */
            Result rs = requests.get(key);
            //get value of first variant in the result
            Object firstFieldValueObj = rs.get(firstField);
            if(firstFieldValueObj == null){
                continue;
            }
            String firstFieldValue = ConvertToString(firstFieldDetails ,firstFieldValueObj);
            //value is not new
            if(getFirstVariantValues().indexOf(firstFieldValue) > -1){
                Integer numberOfResult  = getPieChartData().get(firstFieldValue);
                //if this pair has occured again
                if(numberOfResult !=null){
                    numberOfResult++;
                    getPieChartData().put(firstFieldValue,numberOfResult);
                }else{
                    getPieChartData().put(firstFieldValue,1);
                }
            }else{
                
                getFirstVariantValues().add(firstFieldValue);
                getPieChartData().put(firstFieldValue,1);
            }
        }
        
    }
    
    /**
     *Pie chart for multiple image
     */
    public ReportHelper(Hashtable<String, Result> requests , String firstField , String fixField , String fixedFieldValue,int sys_id) throws DatabaseException {
        /*
         *initialize Arraylist to hold filds possible Values
         */
        setFirstVariantValues(new ArrayList<String>());
        setPieChartData(new Hashtable<String, Integer>());
        Field firstFieldDetails =Field.lookupBySystemIdAndFieldName( sys_id ,firstField );
        Field fixedFieldDetails = Field.lookupBySystemIdAndFieldName(sys_id ,fixField);
        /*
         *retrive  request enum to walk through all requests
         */
        Enumeration requestEnum = requests.keys();
        while(requestEnum.hasMoreElements()) {
            String key = (String) requestEnum.nextElement();
            //  out.println("Entering loop "+ "size is "+ requests.size()+"<br>");
            /*
             *get next result
             */
            Result rs = requests.get(key);
            Object fixedFieldValObj = rs.get(fixField);
            if(fixedFieldValObj == null){
                continue;
            }
            String myFixedFieldValue = ConvertToString(fixedFieldDetails ,fixedFieldValObj);
            if(!myFixedFieldValue.equals(fixedFieldValue)){
                continue ;
            }
            //get value of first variant in the result
            Object firstFieldValueObj = rs.get(firstField);
            if(firstFieldValueObj == null){
                continue;
            }
            String firstFieldValue = ConvertToString(firstFieldDetails ,firstFieldValueObj);
            //value is not new
            if(getFirstVariantValues().indexOf(firstFieldValue)!=-1){
                Integer numberOfResult  = getPieChartData().get(firstFieldValue);
                //if this pair has occured again
                if(numberOfResult !=null){
                    numberOfResult++;
                    getPieChartData().put(firstFieldValue,numberOfResult);
                }else{
                    getPieChartData().put(firstFieldValue,1);
                }
            }else{
                
                getFirstVariantValues().add(firstFieldValue);
                getPieChartData().put(firstFieldValue,1);
            }
        }
        
    }
    
    /**
     * Prepares charts other than pie charts for single image.
     */
    public   ReportHelper(Hashtable<String, Result> requests , String firstField , String secondField , int sys_id) throws DatabaseException  {
        /*
         *initialize Arraylist to hold filds possible Values
         */
        setFirstVariantValues(new ArrayList<String>());
        setSecondVariantValues(new ArrayList<String>());
        setCategoryChartData(new Hashtable<String, Hashtable<String, Integer>>());
        Field firstFieldDetails =Field.lookupBySystemIdAndFieldName( sys_id ,firstField );
        Field secondFieldDetails = Field.lookupBySystemIdAndFieldName(sys_id ,secondField);
        
        /*
         *retrive  request enum to walk through all requests
         */
        Enumeration requestEnum = requests.keys();
        while(requestEnum.hasMoreElements()) {
            String key = (String) requestEnum.nextElement();
            //  out.println("Entering loop "+ "size is "+ requests.size()+"<br>");
            /*
             *get next result
             */
            Result rs = requests.get(key);
            //get value of first variant in the result
            Object firstFieldValObj = rs.get(firstField);
            if(firstFieldValObj == null){
                continue;
            }
            String firstFieldValue = ConvertToString(firstFieldDetails ,firstFieldValObj);//firstFieldValObj.toString();
            //if value is not new
            if(getFirstVariantValues().indexOf(firstFieldValue)!=-1){
                //get inner hash table to hold value crresponding to two field pairs
                Hashtable<String,Integer> innerTable = getCategoryChartData().get(firstFieldValue);
                Object secondFieldValueObj = rs.get(secondField);
                if(secondFieldValueObj == null){
                    continue;
                }
                String secondFieldValue = ConvertToString(secondFieldDetails ,secondFieldValueObj);
                //value is not new
                if(getSecondVariantValues().indexOf(secondFieldValue)!=-1){
                    Integer numberOfResult  = innerTable.get(secondFieldValue);
                    //if this pair has occured again
                    if(numberOfResult !=null){
                        numberOfResult++;
                        innerTable.put(secondFieldValue,numberOfResult);
                    }else{
                        innerTable.put(secondFieldValue,1);
                    }
                }else{
                    
                    getSecondVariantValues().add(secondFieldValue);
                    innerTable.put(secondFieldValue,1);
                }
            }else{
                getFirstVariantValues().add(firstFieldValue);
                
                Hashtable<String,Integer> innerTable = new Hashtable<String,Integer>();
                
                getCategoryChartData().put(firstFieldValue,innerTable);
                
                Object secondFieldValueObj = rs.get(secondField);
                if(secondFieldValueObj == null){
                    continue;
                }
                String secondFieldValue = ConvertToString(secondFieldDetails ,secondFieldValueObj);
                //value is not new
                
                if(getSecondVariantValues().indexOf(secondFieldValue)!=-1){
                    Integer numberOfResult  = innerTable.get(secondFieldValue);
                    //if this pair has occured again
                    if(numberOfResult !=null){
                        numberOfResult++;
                        innerTable.put(secondFieldValue,numberOfResult);
                    }else{
                        innerTable.put(secondFieldValue,1);
                    }
                }else{
                    getSecondVariantValues().add(secondFieldValue);
                    innerTable.put(secondFieldValue,1);
                }
            }
        }
        
    }
    
    
    /**
     * Prepares charts other than pie charts for multiple image.
     */
    public ReportHelper(Hashtable<String, Result> requests , String firstField , String secondField , String fixField , String fixedFieldValue ,  int sys_id) throws DatabaseException  {
        /*
         *initialize Arraylist to hold filds possible Values
         */
        setFirstVariantValues(new ArrayList<String>());
        setSecondVariantValues(new ArrayList<String>());
        setCategoryChartData(new Hashtable<String, Hashtable<String, Integer>>());
        Field firstFieldDetails =Field.lookupBySystemIdAndFieldName( sys_id ,firstField );
        Field secondFieldDetails = Field.lookupBySystemIdAndFieldName(sys_id ,secondField);
        Field fixedFieldDetails = Field.lookupBySystemIdAndFieldName(sys_id ,fixField);
        //transbit.tbits.dataProducer.StaticalDataProducer[]  staticalDataProducers = new transbit.tbits.dataProducer.StaticalDataProducer[4];
        
        
        /*
         *retrive  request enum to walk through all requests
         */
        Enumeration requestEnum = requests.keys();
        while(requestEnum.hasMoreElements()) {
            String key = (String) requestEnum.nextElement();
            //  out.println("Entering loop "+ "size is "+ requests.size()+"<br>");
            /*
             *get next result
             */
            Result rs = requests.get(key);
            //get value of first variant in the result
            Object fixedFieldValObj = rs.get(fixField);
            if(fixedFieldValObj == null){
                continue;
            }
            String myFixedFieldValue = ConvertToString(fixedFieldDetails ,fixedFieldValObj);
            if(!myFixedFieldValue.equals(fixedFieldValue)){
                continue ;
            }
            Object firstFieldValObj = rs.get(firstField);
            if(firstFieldValObj == null){
                continue;
            }
            String firstFieldValue = ConvertToString(firstFieldDetails ,firstFieldValObj);//firstFieldValObj.toString();
            //if value is not new
            if(getFirstVariantValues().indexOf(firstFieldValue)!=-1){
                //get inner hash table to hold value crresponding to two field pairs
                Hashtable<String,Integer> innerTable = getCategoryChartData().get(firstFieldValue);
                Object secondFieldValueObj = rs.get(secondField);
                if(secondFieldValueObj == null){
                    continue;
                }
                String secondFieldValue = ConvertToString(secondFieldDetails ,secondFieldValueObj);
                //value is not new
                if(getSecondVariantValues().indexOf(secondFieldValue)!=-1){
                    Integer numberOfResult  = innerTable.get(secondFieldValue);
                    //if this pair has occured again
                    if(numberOfResult !=null){
                        numberOfResult++;
                        innerTable.put(secondFieldValue,numberOfResult);
                    }else{
                        innerTable.put(secondFieldValue,1);
                    }
                }else{
                    
                    getSecondVariantValues().add(secondFieldValue);
                    innerTable.put(secondFieldValue,1);
                }
            }else{
                getFirstVariantValues().add(firstFieldValue);
                
                Hashtable<String,Integer> innerTable = new Hashtable<String,Integer>();
                
                getCategoryChartData().put(firstFieldValue,innerTable);
                
                Object secondFieldValueObj = rs.get(secondField);
                if(secondFieldValueObj == null){
                    continue;
                }
                String secondFieldValue = ConvertToString(secondFieldDetails ,secondFieldValueObj);
                //value is not new
                
                if(getSecondVariantValues().indexOf(secondFieldValue)!=-1){
                    Integer numberOfResult  = innerTable.get(secondFieldValue);
                    //if this pair has occured again
                    if(numberOfResult !=null){
                        numberOfResult++;
                        innerTable.put(secondFieldValue,numberOfResult);
                    }else{
                        innerTable.put(secondFieldValue,1);
                    }
                }else{
                    getSecondVariantValues().add(secondFieldValue);
                    innerTable.put(secondFieldValue,1);
                }
            }
        }
        
    }
    
    public static   String ConvertToString(Field field ,Object value){
        int dataType = field.getDataTypeId();
        StringBuffer buffer = new StringBuffer();
        switch (dataType) {
            case BOOLEAN : {
                String strValue = (value == null)
                ? "-"
                        : value.toString();
                
                buffer.append(strValue);
            }
            
            break;
            case DATE :
            case TIME :
            case DATETIME : {
                if (value instanceof Timestamp) {
                    Timestamp ts = (Timestamp) value;
                    
                    if (ts == null) {
                        buffer.append("-");
                    } else {
                        String dateValue = WebUtil.getDateInFormat(ts, TimeZone.getTimeZone("GMT"), "yyyy-MM-dd");
                        //dateValue = dateValue.replaceAll(" ", "&nbsp;");//.replaceAll("-", "&#8209;");
                        buffer.append(dateValue);
                    }
                } else {
                    buffer.append("-");
                }
            }
            
            break;
            
            case INT : {
                if (value == null) {
                    buffer.append("-");
                } else {
                    buffer.append(value.toString());
                }
            }
            
            break;
            
            case REAL : {
                if (value == null) {
                    buffer.append("-");
                } else {
                    buffer.append(value.toString());
                }
            }
            
            break;
            
            case STRING :
            case TEXT : {
                if (value == null) {
                    buffer.append("-");
                } else {
                    buffer.append(Utilities.htmlEncode(value.toString()));
                }
            }
            
            break;
            case USERTYPE : {
                if (value == null) {
                    buffer.append("-");
                } else {
                    String str = value.toString();
                    str = str.replaceAll("\\.transbittech\\.com", "");
                    buffer.append(str);
                }
            }
            
            break;
            
            case TYPE : {
                if (value == null) {
                    buffer.append("-");
                } else {
                    
                    /*
                     * Check if this starts with a + in which case, it is
                     * a private type.
                     */
                    String strValue = value.toString();
                    
                    strValue = strValue.replaceAll(" ", "&nbsp;").replaceAll("-", "&#8209;");
                    
                    if (strValue.startsWith("+") == true) {
                        strValue = strValue.substring(1) + "&nbsp;&dagger;";
                    }
                    
                    buffer.append(strValue);
                }
            }
            
            break;
        }
        
        return buffer.toString();
    }
    
    public ArrayList<String> getFirstVariantValues() {
        return firstVariantValues;
    }
    
    public void setFirstVariantValues(ArrayList<String> firstVariantValues) {
        this.firstVariantValues = firstVariantValues;
    }
    
    public ArrayList<String> getSecondVariantValues() {
        return secondVariantValues;
    }
    
    public void setSecondVariantValues(ArrayList<String> secondVariantValues) {
        this.secondVariantValues = secondVariantValues;
    }
    
    public Hashtable<String, Hashtable<String, Integer>> getCategoryChartData() {
        return categoryChartData;
    }
    
    public void setCategoryChartData(Hashtable<String, Hashtable<String, Integer>> twoFieldData) {
        this.categoryChartData = twoFieldData;
    }
    
    public Hashtable<String, Integer> getPieChartData() {
        return pieChartData;
    }
    
    public void setPieChartData(Hashtable<String, Integer> pieChartData) {
        this.pieChartData = pieChartData;
    }
}
