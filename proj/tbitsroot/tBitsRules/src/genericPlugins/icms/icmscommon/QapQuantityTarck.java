package icms.icmscommon;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;

public class QapQuantityTarck
  implements IRule
{
  public RuleResult execute(Connection connection, BusinessArea ba, Request oldRequest, Request currentRequest, int Source, User user, boolean isAddRequest)
  {
	int total_qty=0; 
	String Agency;
	String Contractor;
    String sysPrefix = ba.getSystemPrefix();
    String inspectionProp = PropertiesHandler.getProperty("comma_seperated_list_of_inspection_ba");
    String qapProp = PropertiesHandler.getProperty("comma_seperated_list_of_qap_ba");
    boolean insFlag = false;
    boolean qapFlag = false;

    if (inspectionProp != null) {
      String[] listOfInspectionBa = inspectionProp.split(",");

      if ((listOfInspectionBa != null) && (listOfInspectionBa.length > 0))
      {
        for (int i = 0; i < listOfInspectionBa.length; i++) {
          if (listOfInspectionBa[i].equalsIgnoreCase(sysPrefix)) {
            insFlag = true;
            break;
          }
        }
      }

    }

    if (qapProp != null) {
      String[] listOfQapBa = qapProp.split(",");
      if ((listOfQapBa != null) && (listOfQapBa.length > 0))
      {
        for (int i = 0; i < listOfQapBa.length; i++) {
          if (listOfQapBa[i].equalsIgnoreCase(sysPrefix)) {
            qapFlag = true;
            break;
          }
        }
      }
    }

    if (qapFlag)
    {
      String testSerialNo = currentRequest.get("test_sr_no");
      String serialNo = currentRequest.get("QAPSerialNo");
      String finalSerial = serialNo + "-" + testSerialNo;
      if ((testSerialNo.equalsIgnoreCase("1")) && (isAddRequest))
      {
        Object orderQty = currentRequest.getObject("order_qty");
        currentRequest.setObject("total_qty", orderQty);
        currentRequest.setObject("inventory", orderQty);
      }

      if (isAddRequest)
      {
        currentRequest.setObject("serial_test", finalSerial);
      }

    }

    if ((insFlag) && (!isAddRequest))
    {
      Integer nan = (Integer)currentRequest.getObject("no_of_nan");
      Integer irn = (Integer)currentRequest.getObject("no_of_irn");
      Integer oldirn = (Integer)oldRequest.getObject("no_of_irn");
      Integer mdcc = (Integer)currentRequest.getObject("no_of_mdcc");
      Integer offerQty = Integer.valueOf(Integer.parseInt((String)currentRequest.getObject("OfferQuantity")));
      Integer oldofferQty = Integer.valueOf(Integer.parseInt((String)oldRequest.getObject("OfferQuantity")));
      String Analysisresult = currentRequest.get("InspectionResult");
     
      if ((irn.intValue() + nan.intValue() != offerQty.intValue()) && !Analysisresult.equalsIgnoreCase("none") )
      {
        return new RuleResult(false, "Irn+NAN must be equal to offer Quantity ");
      }
      String linkedRequest = currentRequest.getRelatedRequests();

      System.out.println(nan.intValue() + irn.intValue() + mdcc.intValue());
      Collection<RequestDataType> rdt = APIUtil.getRequestCollection(linkedRequest);
      String sql = "SELECT request_id FROM requests_ex where sys_id = ? and field_id=? and varchar_value=?";

      UpdateRequest upRequest = new UpdateRequest();
      for (RequestDataType rd : rdt)
      {
        int reqId = rd.getRequestId();
        int sysId = rd.getSysId();
        try {
          Request r = Request.lookupBySystemIdAndRequestId(sysId, reqId);
          Field qapSerialField = Field.lookupBySystemIdAndFieldName(sysId, "QAPSerialNo");
          String serial = r.get("QAPSerialNo");
          String next = r.get("next_test_no");
          if (!next.equalsIgnoreCase("mdcc"))
          {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, sysId);
            ps.setInt(2, qapSerialField.getFieldId());
            ps.setString(3, serial);
            ArrayList reqList = new ArrayList();
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
              reqList.add(Integer.valueOf(rs.getInt("request_id")));
            }
            rs.close();
            ps.close();
            ArrayList<Request> reqListQap = Request.lookupBySystemIdAndRequestIdList(sysId, reqList);
            Request nextReq = null;
            for (Request req : reqListQap)
            {
              String nextTestNo = req.get("test_sr_no");
              if (!nextTestNo.equalsIgnoreCase(next))
                continue;
              nextReq = req;
              break;
            }
            Request r1 = Request.lookupBySystemIdAndRequestId(sysId, (Integer)reqList.get(0));
            total_qty= Integer.parseInt(r1.get("total_qty"));
            Agency = (String)r1.get("Agency");
            Contractor = (String)r1.get("Contractor");

            if (nextReq != null)
            {
              System.out.println("next Req Id :" + nextReq.getRequestId());

              Integer nextTestInven = (Integer)nextReq.getObject("inventory");
              Integer nextTestTotal = (Integer)nextReq.getObject("total_qty");
              Integer upInNext = irn.intValue();
              Integer upTotNext = Integer.valueOf(nextTestTotal.intValue());
              Hashtable paramTable1 = new Hashtable();
              paramTable1.put("sys_id", Integer.toString(nextReq.getSystemId()));
              paramTable1.put("user_id", "root");
              paramTable1.put("request_id", Integer.toString(nextReq.getRequestId().intValue()));
              paramTable1.put("total_qty", Integer.toString(total_qty));
              paramTable1.put("inventory", Integer.toString(upInNext.intValue()));
              paramTable1.put("Agency", Agency);
              paramTable1.put("Contractor", Contractor);
              //nextReq.setObject("inventory", upInNext);
              Request localRequest1 = upRequest.updateRequest(paramTable1);
            }

          }

          if(offerQty.intValue()==oldofferQty.intValue())
          {
          Hashtable paramTable2 = new Hashtable();
          paramTable2.put("sys_id", Integer.toString(sysId));
          paramTable2.put("user_id", "root");
          paramTable2.put("request_id", Integer.toString(reqId));
          Integer preTestInven = (Integer)r.getObject("inventory");
          int upInvenTest = preTestInven.intValue() + (oldirn.intValue() - irn.intValue());
          //r.setObject("inventory",preTestInven);
          paramTable2.put("inventory", Integer.toString(upInvenTest));
          paramTable2.put("description", "Desicion Received on [" + sysPrefix + "#" + currentRequest.getRequestId() + "]" + 
            " via TPIA DOC NO#" + currentRequest.get("TPIANo") + "  IRN : " + irn + " NAN :" + nan);
          Request baseReq = upRequest.updateRequest(paramTable2);
          }

        }
        catch (DatabaseException e)
        {
           e.printStackTrace();
        }
        catch (SQLException e) {
          e.printStackTrace();
        }
        catch (APIException e) {
          e.printStackTrace();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }

    }

    return new RuleResult(true);
  }

  public String getName()
  {
    return null;
  }

  public double getSequence()
  {
    return 4.0;
  }
}