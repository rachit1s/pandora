package corrGeneric.com.tbitsGlobal.client.modelData;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.BaseModelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class ReportTypeClient extends TbitsModelData{
	
	protected ArrayList<TypeClient> reportType1;
	protected ArrayList<TypeClient> reportType2;
	protected ArrayList<TypeClient> reportType3;
	protected ArrayList<TypeClient> reportType4;
	protected ArrayList<TypeClient> reportType5;
	
	public ReportTypeClient(){
		super();
		reportType1 = new ArrayList<TypeClient>();
		reportType2	= new ArrayList<TypeClient>();
		reportType3	= new ArrayList<TypeClient>();
		reportType4	= new ArrayList<TypeClient>();
		reportType5	= new ArrayList<TypeClient>();
	}
	
	public void addReportType(String reportType, TypeClient value){
		if(reportType.equals(GenericParams.ReportType1))
			reportType1.add(value);
		else if(reportType.equals(GenericParams.ReportType2))
			reportType2.add(value);
		else if(reportType.equals(GenericParams.ReportType3))
			reportType3.add(value);
		else if(reportType.equals(GenericParams.ReportType4))
			reportType4.add(value);
		else if(reportType.equals(GenericParams.ReportType5))
			reportType5.add(value);
	}
	
	public void addReportTypeList(String reportType, ArrayList<TypeClient> values){
		if((!values.isEmpty()) && (values != null)){
			if(reportType.equals(GenericParams.ReportType1))
				reportType1.addAll(values);
			else if(reportType.equals(GenericParams.ReportType2))
				reportType2.addAll(values);
			else if(reportType.equals(GenericParams.ReportType3))
				reportType3.addAll(values);
			else if(reportType.equals(GenericParams.ReportType4))
				reportType4.addAll(values);
			else if(reportType.equals(GenericParams.ReportType5))
				reportType5.addAll(values);
		}
	}
	
	public ArrayList<TypeClient> getReportTypeList(String reportType){
		if(reportType.equals(GenericParams.ReportType1))
			return reportType1;
		else if(reportType.equals(GenericParams.ReportType2))
			return reportType2;
		else if(reportType.equals(GenericParams.ReportType3))
			return reportType3;
		else if(reportType.equals(GenericParams.ReportType4))
			return reportType4;
		else if(reportType.equals(GenericParams.ReportType5))
			return reportType5;
		return null;
	}

}
