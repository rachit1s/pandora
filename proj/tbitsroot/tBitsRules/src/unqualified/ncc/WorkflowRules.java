/**
 * 
 */
package ncc;

import java.util.ArrayList;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class WorkflowRules {
	
	//private static final String ENGINEERING = "Engineering";
	private static final String DETAILED_ENGINEERING = "DetailedEngineering";
	private static final String BASIC_ENGINEERING = "BasicEngineering";

	public static boolean isBelongsToWorkFlow1(ArrayList<Request>requests, StringBuffer details) throws TBitsException{
		boolean isBelongsToSameClassification = false;
		int requestCount = 0;
		String tempString = "";
		for (Request dcrRequest : requests){
			
			try {
				Type enggType = dcrRequest.getExType("EngineeringType");
				Type disciplineType = dcrRequest.getCategoryId();
				Type areaType = dcrRequest.getExType("Area");

				if (areaType.getName().equals("BTG") && 
						disciplineType.getName().equals("Civil") && 
						(enggType.getName().equals(BASIC_ENGINEERING) || (enggType.getName().equals(DETAILED_ENGINEERING)))){//ENGINEERING)){
						requestCount++;
					}
					else{
						String tString = "'Area:" + areaType.getDisplayName()
						+ ", Discipline: " + disciplineType.getDisplayName() 
						+ ", EngineeringTye: " + enggType.getDisplayName() 
						+ ", document/drawing: " + dcrRequest.getExString(NCCTransmittalUtils.DRAWING_NO) + ".'\n";
						tempString = (tempString.equals("")) ? tString : tempString + tString;
					}
			} catch (IllegalStateException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			}					
		}

		if ((requests.size()>0) && (requestCount != requests.size())){
			isBelongsToSameClassification = false;
			details.append("\nNot all drawings/documents selected for transmittal belong to same, 'Area:BTG'," +
					"'Discipline:Civil' and 'EngineeringType:Basic/Detailed Engineering'. \n" +
					"The details is as follows: ").append("\n").append(tempString);
		}
		else if ((requestCount>0) && (requestCount == requests.size())){
			isBelongsToSameClassification = true;
		}
		return isBelongsToSameClassification;
	}
	
	public static boolean isBelongsToWorkFlow2(ArrayList<Request>requests, StringBuffer details) throws TBitsException{
		boolean isBelongsToSameClassification = true;
		int requestCount = 0;
		String tempString = "";
		for (Request dcrRequest : requests){
			try {
				Type disciplineType = dcrRequest.getCategoryId();
				Type areaType = dcrRequest.getExType("Area");
				Type enggType = dcrRequest.getExType("EngineeringType");

				if (areaType.getName().equals("BTG") && 
						(disciplineType.getName().equals("Electrical")
								|| disciplineType.getName().equals("CnI")
								|| disciplineType.getName().equals("Mechanical")) &&
						(enggType.getName().equals(BASIC_ENGINEERING) || (enggType.getName().equals(DETAILED_ENGINEERING)))){
					requestCount++;
				}
				else{
					String tString = "'Area:" + areaType.getDisplayName()
					+ ", Discipline: " + disciplineType.getDisplayName() 
					+ ", EngineeringTye: " + enggType.getDisplayName() 
					+ ", document/drawing: " + dcrRequest.getExString(NCCTransmittalUtils.DRAWING_NO) + ".'\n";
					tempString = (tempString.equals("")) ? tString : tempString + tString;											
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			} catch (DatabaseException e) {
				e.printStackTrace();
				TBitsException tbe = new TBitsException(e);
				tbe.setDescription(e.getDescription());
				throw tbe;
			}					
		}
		
		if ((requests.size()>0) && (requestCount != requests.size())){
			isBelongsToSameClassification = false;
			details.append("\nNot all drawings/documents selected for transmittal belong to same, 'Area:BTG'," +
					"'Discipline:Electrical & C&I/Mechanical' and 'EngineeringType:Basic Engineering/DetailedEngineering'. \n" +
					"The details is as follows: \n").append(tempString) ;
		}
		else if ((requestCount>0) && (requestCount == requests.size())){
			isBelongsToSameClassification = true;
		}
		
		return isBelongsToSameClassification;
	}
		
	public static boolean isBelongsToWorkFlow3(ArrayList<Request>requests, StringBuffer details) throws TBitsException{
		boolean isBelongsToSameClassification = false;
		int requestCount = 0;
		String tempString = "";
		for (Request dcrRequest : requests){
			try {
				Type disciplineType = dcrRequest.getCategoryId();
				Type areaType = dcrRequest.getExType("Area");
				Type enggType = dcrRequest.getExType("EngineeringType");

				if (areaType.getName().equals("BOP") && 
						disciplineType.getName().equals("Civil") &&
						(enggType.getName().equals(BASIC_ENGINEERING) || (enggType.getName().equals(DETAILED_ENGINEERING)))){					
					requestCount++;
				}
				else{
					String tString = "'Area:" + areaType.getDisplayName()
					+ ", Discipline: " + disciplineType.getDisplayName() 
					+ ", EngineeringTye: " + enggType.getDisplayName() 
					+ ", document/drawing: " + dcrRequest.getExString(NCCTransmittalUtils.DRAWING_NO) + ".'\n";
					tempString = (tempString.equals("")) ? tString : tempString + tString;
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			}					
		}
		
		if ((requests.size()>0) && (requestCount != requests.size())){
			isBelongsToSameClassification = false;
			details.append("\nNot all drawings/documents selected for transmittal belong to same, 'Area:BOP'," +
					"'Discipline:Civil' and 'EngineeringType:Basic Engineering/Detailed Engineering'. \n" +
					"The details is as follows: ").append("\n").append(tempString);
		}
		else if ((requestCount>0) && (requestCount == requests.size())){
			isBelongsToSameClassification = true;
		}
		
		return isBelongsToSameClassification;
	}	
	
	public static boolean isBelongsToWorkFlow4(ArrayList<Request>requests, StringBuffer details) throws TBitsException{
		boolean isBelongsToSameClassification = true;
		int requestCount = 0;
		String tempString = "";
		for (Request dcrRequest : requests){
			try {
				Type disciplineType = dcrRequest.getCategoryId();
				Type areaType = dcrRequest.getExType("Area");
				Type packageType = dcrRequest.getSeverityId();
				Type enggType = dcrRequest.getExType("EngineeringType");

				if (areaType.getName().equals("BOP") && 
						disciplineType.getName().equals("Civil") && 
						packageType.getName().equals("Chimney") && 
						(enggType.getName().equals(BASIC_ENGINEERING) || enggType.getName().equals(DETAILED_ENGINEERING))){
					requestCount++;
				}
				else{
					String tString = "'Area:" + areaType.getDisplayName()
					+ ", Discipline: " + disciplineType.getDisplayName() 
					+ ", EngineeringTye: " + enggType.getDisplayName() 
					+ ", document/drawing: " + dcrRequest.getExString(NCCTransmittalUtils.DRAWING_NO) + ".'\n";
					tempString = (tempString.equals("")) ? tString : tempString + tString;
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			}					
		}
		
		if ((requests.size()>0) && (requestCount != requests.size())){
			isBelongsToSameClassification = false;
			details.append("\nNot all drawings/documents selected for transmittal belong to same, 'Area:BOP'," +
					"'Discipline:Civil' and 'Package:Chimney' and 'EngineeringType:Basic/Detailed Engineering'. \n" +
					"The details is as follows: \n").append(tempString) ;
		}
		else if ((requestCount>0) && (requestCount == requests.size())){
			isBelongsToSameClassification = true;
		}
		
		return isBelongsToSameClassification;
	}
	
	public static boolean isBelongsToWorkFlow5(ArrayList<Request>requests, StringBuffer details) throws TBitsException{
		boolean isBelongsToSameClassification = true;
		int requestCount = 0;
		String tempString = "";
		for (Request dcrRequest : requests){
			try {
				Type disciplineType = dcrRequest.getCategoryId();
				Type areaType = dcrRequest.getExType("Area");
				Type enggType = dcrRequest.getExType("EngineeringType");

				if (areaType.getName().equals("BOP") && 
						(disciplineType.getName().equals("Electrical") || 
								disciplineType.getName().equals("CnI") ||
								disciplineType.getName().equals("Mechanical")) &&
						enggType.getName().equals(BASIC_ENGINEERING)){
						requestCount++;
					}
					else{
						String tString = "'Area:" + areaType.getDisplayName()
						+ ", Discipline: " + disciplineType.getDisplayName() 
						+ ", EngineeringTye: " + enggType.getDisplayName() 
						+ ", document/drawing: " + dcrRequest.getExString(NCCTransmittalUtils.DRAWING_NO) + ".'\n";
						tempString = (tempString.equals("")) ? tString : tempString + tString;
					}
			} catch (IllegalStateException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			}					
		}
		
		if ((requests.size()>0) && (requestCount != requests.size())){
			isBelongsToSameClassification = false;
			details.append("\nNot all drawings/documents selected for transmittal belong to same, 'Area:BOP'," +
					"'Discipline:Electrical / C&I/Mechanical' and 'EngineeringType:Basic Engineering'.\n" +
					"The details is as follows: \n").append(tempString) ;			
		}
		else if ((requestCount>0) && (requestCount == requests.size())){
			isBelongsToSameClassification = true;
		}
		
		return isBelongsToSameClassification;
	}
	
	public static boolean isBelongsToWorkFlow6(ArrayList<Request>requests, StringBuffer details) throws TBitsException{
		boolean isBelongsToSameClassification = true;
		int requestCount = 0;
		String tempString = "";
		for (Request dcrRequest : requests){
			try {
				Type disciplineType = dcrRequest.getCategoryId();
				Type areaType = dcrRequest.getExType("Area");
				Type enggType = dcrRequest.getExType("EngineeringType");

				if (areaType.getName().equals("BOP") && 
						(disciplineType.getName().equals("Electrical") 
								|| disciplineType.getName().equals("CnI") 
								|| disciplineType.getName().equals("Mechanical")) &&
						enggType.getName().equals(DETAILED_ENGINEERING)){
						requestCount++;
					}
					else{
						String tString = "'Area:" + areaType.getDisplayName()
						+ ", Discipline: " + disciplineType.getDisplayName() 
						+ ", EngineeringTye: " + enggType.getDisplayName() 
						+ ", document/drawing: " + dcrRequest.getExString(NCCTransmittalUtils.DRAWING_NO) + ".'\n";
						tempString = (tempString.equals("")) ? tString : tempString + tString;
					}
			} catch (IllegalStateException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException(e);
			}					
		}
		
		if ((requests.size()>0) && (requestCount != requests.size())){
			isBelongsToSameClassification = false;
			details.append("\nNot all drawings/documents selected for transmittal belong to same, 'Area:BOP'," +
					"'Discipline:Electrical or C&I or Mechanical' and 'EngineeringType:Detailed Engineering'. \n" +
					"The details is as follows: \n").append(tempString) ;				
		}
		else if ((requestCount>0) && (requestCount == requests.size())){
			isBelongsToSameClassification = true;
		}
		
		return isBelongsToSameClassification;
	}
}
