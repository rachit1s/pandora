/*
 * StaticalDataProducer.java
 *
 * Created on July 29, 2006, 3:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package transbit.tbits.dataProducer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.ReportHelper;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import static transbit.tbits.Helper.TBitsConstants.PKG_COMMON;

import de.laures.cewolf.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.DatasetProducer;
import de.laures.cewolf.links.CategoryItemLinkGenerator;
import de.laures.cewolf.tooltips.CategoryToolTipGenerator;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.RolePermission;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.search.MultiSearcher;
import transbit.tbits.search.ParseEntry;
import transbit.tbits.webapps.HtmlSearch;
import transbit.tbits.webapps.WebUtil;

/**
 * An example data producer.
 * @author
 */
public class StaticalDataProducer implements DatasetProducer, CategoryToolTipGenerator, CategoryItemLinkGenerator, Serializable {
    // private static final Log log = LogFactory.getLog(StaticalDataProducer.class);
    public static TBitsLogger LOG = TBitsLogger.getLogger(PKG_COMMON);
    // These values would normally not be hard coded but produced by
    // some kind of data source like a database or a file
    private DefaultCategoryDataset categoryDataset;   
    private DefaultPieDataset pieDataset;
    private String[] toolTip;
    private String type;
    
    public void generateCategoryData(ReportHelper myReportHelper){
           categoryDataset = new DefaultCategoryDataset(){
            /**
             * @see java.lang.Object#finalize()
             */
            protected void finalize() throws Throwable {
                super.finalize();
                LOG.debug(this +" finalized.");
            }
        };    
        ArrayList<String> firstVariantValues = myReportHelper.getFirstVariantValues();
        ArrayList<String> secondVariantValues = myReportHelper.getSecondVariantValues() ;
        toolTip = new String[firstVariantValues.size()];
        int currentfirstVariant = 0;
        for(String firstVariant : firstVariantValues) {
            toolTip[currentfirstVariant++]=firstVariant;
            for(String secondVriant : secondVariantValues) {
                if(myReportHelper.getCategoryChartData().get(firstVariant).get(secondVriant) != null){
                    categoryDataset.addValue(myReportHelper.getCategoryChartData().get(firstVariant).get(secondVriant), firstVariant, secondVriant);    
                }
            }
        }
    }
    
    public void generatePieData(ReportHelper myReportHelper){
        pieDataset = new DefaultPieDataset();
        ArrayList<String> firstVariantValues = myReportHelper.getFirstVariantValues();
        toolTip = new String[firstVariantValues.size()];
        int currentfirstVariant = 0;
        for(String firstVariant : firstVariantValues) {
            toolTip[currentfirstVariant++]=firstVariant; 
                if(myReportHelper.getPieChartData().get(firstVariant)!= null){
                    pieDataset.setValue(firstVariant,myReportHelper.getPieChartData().get(firstVariant));    
            }
        }   
    }
    public void generateData(ReportHelper myReportHelper , String type){
        this.type = type;
        if(this.type == "category"){
            generateCategoryData( myReportHelper);   
        }else if(this.type == "pie"){
            generatePieData(myReportHelper);
        }
    }
    
    /**
     *  Produces some random data.
     */
    public Object produceDataset(Map params) throws DatasetProduceException { 
        if(this.type == "category"){
              return categoryDataset;
        }
        else if(this.type == "pie"){
            return pieDataset;
        }  
        return null;
    }
    
    public DefaultCategoryDataset getStaticalData(){
        return null;
    } 
    
    // <editor-fold defaultstate="collapsed" >
    /**
     * This producer's data is invalidated after 5 seconds. By this method the
     * producer can influence Cewolf's caching behaviour the way it wants to.
     */
    public boolean hasExpired(Map params, Date since) {
        LOG.debug(getClass().getName() + "hasExpired()");
        return (System.currentTimeMillis() - since.getTime())  > 5000;
    }
    
    /**
     * Returns a unique ID for this DatasetProducer
     */
    public String getProducerId() {
        return "Tbits DatasetProducer";
    }
    
    /**
     * Returns a link target for a special data item.
     */
    public String generateLink(Object data, int series, Object category) {
        if(this.type == "category"){
            return this.toolTip[series];  
        }else if(this.type == "pie"){
            return this.toolTip[series];
        }
        return null ;
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug(this + " finalized.");
    }
  
    public String generateToolTip(CategoryDataset arg0, int series, int arg2) {
        return toolTip[series];
    }
     public String generateToolTip(PieDataset arg0, int series) {
        return toolTip[series];
    }
    // </editor-fold >
    
    
    
}
