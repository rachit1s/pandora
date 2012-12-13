/*
 * BaReport.java
 *
 * Created on June 30, 2006, 5:16 AM
 */

package transbit.tbits.webapps;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.servlet.*;
import javax.servlet.http.*;
import transbit.tbits.Helper.Messages;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.common.Utilities;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.search.Result;
import transbit.tbits.search.Searcher;

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
/**
 * helper class for report generation used to grnrrate form for getting report field
 * this class validates user and retrives all accesible fielda of the corresponding user
 * @author Vaibhav Sharma
 * @version 3.1
 *
 */
public class BaReport extends HttpServlet {
    private User      user   ;
    private WebConfig userConfig = null;
    private Hashtable<String, Object> reqParams;
    private BusinessArea     ba  ;
    private ArrayList<Field> fieldList;
    
    private ArrayList<String> firstVariantValues;
    
    private ArrayList<String> secondVariantValues;
    
    private Hashtable<String, Hashtable<String, Integer>> categoryChartData;
        private  Hashtable<String,Integer> pieChartData;
    
    /** Creates a new instance of ReportHelper */
    
   private void ReportHelper(Hashtable<String, Result> requests , String firstField , String secondField , String fixField , String fixedFieldValue , HttpServletResponse aResponse , int sys_id) throws DatabaseException, IOException  {
        /*
         *initialize Arraylist to hold filds possible Values
         */
        aResponse.setContentType("text/html;charset=UTF-8");
        PrintWriter out = aResponse.getWriter();
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
            if(myFixedFieldValue.equals(fixedFieldValue)){
                out.println("Fixed field should be :" + fixedFieldValue + " but it is :" + myFixedFieldValue +"<br>");
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
        for(String s1 : getFirstVariantValues()){        
                Hashtable<String,Integer> innerTable = getTwoFieldData().get(s1);
                for(String s2 : getSecondVariantValues()){
                          out.println(s1+ " , "+ s2+ "  "+ innerTable.get(s2)+"<br>");     
             }
               
        }   
        
    }
     
    
    
    
    
   private void ReportHelper(Hashtable<String, Result> requests , String firstField ,HttpServletResponse aResponse , int sys_id) throws DatabaseException, IOException {
        /*
         *initialize Arraylist to hold filds possible Values
         */
        aResponse.setContentType("text/html;charset=UTF-8");
        PrintWriter out = aResponse.getWriter();
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
       
        
        ArrayList<String> firstVariantValues =getFirstVariantValues();
        for(String firstVariant : firstVariantValues) {
                if(getPieChartData().get(firstVariant)!= null){
                    out.println(firstVariant + getPieChartData().get(firstVariant) + "<br>");    
            }
        }   
        
    }
    

    private void  ReportHelper(Hashtable<String, Result> requests , String firstField , String secondField,HttpServletResponse aResponse , int sys_id) throws IOException, DatabaseException {
        aResponse.setContentType("text/html;charset=UTF-8");
        PrintWriter out = aResponse.getWriter();
        out.println(firstField + "\n");
        out.println(secondField+ "\n");  
        /*
         *initialize Arraylist to hold filds possible Values
         */
        
        setFirstVariantValues(new ArrayList<String>());
        setSecondVariantValues(new ArrayList<String>());
        setTwoFieldData(new Hashtable<String, Hashtable<String, Integer>>());
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
                out.println(" object is null " +"<br>");
                continue;
            }
            String firstFieldValue = ConvertToString(firstFieldDetails ,firstFieldValObj);//firstFieldValObj.toString();
            //if value is not new
            if(getFirstVariantValues().indexOf(firstFieldValue)!=-1){
                //get inner hash table to hold value crresponding to two field pairs
                Hashtable<String,Integer> innerTable = getTwoFieldData().get(firstFieldValue);
                Object secondFieldValueObj = rs.get(secondField);
                if(secondFieldValueObj == null){
                    out.println(" object is null ");
                    continue;
                }
                String secondFieldValue = ConvertToString(secondFieldDetails ,secondFieldValueObj);
               out.println("Value is "+secondFieldValue );
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
                
                getTwoFieldData().put(firstFieldValue,innerTable);
                
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
        for(String s1 : getFirstVariantValues()){        
                Hashtable<String,Integer> innerTable = getTwoFieldData().get(s1);
                for(String s2 : getSecondVariantValues()){
                          out.println(s1+ " , "+ s2+ "  "+ innerTable.get(s2)+"<br>");     
             }
               
        }   
        
    }
    
    private  String ConvertToString(Field field ,Object value){
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
                        String dateValue = WebUtil.getDateInFormat(ts, TimeZone.getTimeZone("GMT"), "yyyy-MM-dd HH:mm:ss.SSS a");
                        dateValue = dateValue.replaceAll(" ", "&nbsp;").replaceAll("-", "&#8209;");
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
    
    public Hashtable<String, Hashtable<String, Integer>> getTwoFieldData() {
        return getCategoryChartData();
    }
    
    public void setTwoFieldData(Hashtable<String, Hashtable<String, Integer>> twoFieldData) {
        this.setCategoryChartData(twoFieldData);
    }
    protected void processRequest2(HttpServletRequest aRequest, HttpServletResponse aResponse) throws IOException {
        
        ArrayList<Result> results;
        User user = null;
        WebConfig userConfig=null;
        Hashtable<String, Object> reqParams=null;
        BusinessArea ba = null;
        try {
            user = WebUtil.validateUser(aRequest);
            userConfig = WebConfig.getWebConfig(user.getWebConfig());
            reqParams = WebUtil.getRequestParams(aRequest, userConfig, WebUtil.SEARCH);
            ba        = (BusinessArea) reqParams.get(Field.BUSINESS_AREA);
            if (ba == null) {
                throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
            }
            int       systemId  = ba.getSystemId();
            String    sysPrefix = ba.getSystemPrefix();
            SysConfig sc        = ba.getSysConfigObject();
            // Get all the fields which the user can view in this business_area
            ArrayList<Field> fieldList = Field.getFieldsBySystemIdAndUserId(systemId, user.getUserId());
            
            int       listSize   = fieldList.size();
            String    fieldDisplayName  = null;
            String    fieldName ;
            String    fieldConboBoxOptions = "";
            for (int i = 0; i < listSize; i++){
                Field field = (Field) fieldList.get(i);
                fieldDisplayName = field.getDisplayName();
                fieldName = field.getName();
         
                fieldConboBoxOptions += "<option VALUE=\""+fieldName+"\">"+fieldDisplayName+"\r\n";
            }
            Searcher searcher = new Searcher(2,user.getUserId(),"");
            searcher.setCurrentPageSize(10000);
            searcher.search();
            results = searcher.getResultList();
            Hashtable<String, Result> requests = searcher.getRequestsTable();
            ReportHelper(requests , "status_id","category_id" ,"logger_ids", "vinod" , aResponse,2);
        }catch (TBitsException ex) {
            ex.printStackTrace();
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        } catch (ServletException ex) {
            ex.printStackTrace();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
  // searcher.setTotalResultCount(1000);    
    }
    // <editor-fold defaultstate="collapsed" >
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest2(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest2(request, response);
    }
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Servlet To generate Ba reports";
    }
    // </editor-fold>

    public Hashtable<String, Integer> getPieChartData() {
        return pieChartData;
    }

    public void setPieChartData(Hashtable<String, Integer> pieChartData) {
        this.pieChartData = pieChartData;
    }

    public Hashtable<String, Hashtable<String, Integer>> getCategoryChartData() {
        return categoryChartData;
    }

    public void setCategoryChartData(Hashtable<String, Hashtable<String, Integer>> categoryChartData) {
        this.categoryChartData = categoryChartData;
    }
    
}
