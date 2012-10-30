/**
 * 
 */
package com.tbitsglobal.ddc.test;

import com.tbitsglobal.ddc.dao.FirmPropertyDao;
import com.tbitsglobal.ddc.dao.exception.FailedToInsert;
import com.tbitsglobal.ddc.dao.exception.FailedToUpdate;
import com.tbitsglobal.ddc.domain.FirmProperty;
import com.tbitsglobal.ddc.exception.FailedToFindObject;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class FirmPropertyDaoTest 
{
	public static FirmProperty addFirmProperty()
	{
		FirmProperty fp = new FirmProperty(-1, "BA1", "@tbitsglobal.com", "nitiraj.r@tbitsglobal.com", 1L, "NumberField1", 2L,"NumberField2", 3L, "NumberField3", 1L);
		try {
			FirmProperty addedFP = FirmPropertyDao.getInstance().insert(fp);
			return addedFP;
		} catch (FailedToInsert e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static FirmProperty updateFirmProperty(FirmProperty fp)
	{
		try {
			return FirmPropertyDao.getInstance().update(fp);
		} catch (FailedToUpdate e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static FirmProperty deleteFirmProperty()
	{
		FirmProperty fp = new FirmProperty(-1, "BA1", "@tbitsglobal.com", "nitiraj.r@tbitsglobal.com", 1L, "NumberField1", 2L,"NumberField2", 3L, "NumberField3", 1L);
		try {
			FirmProperty addedFP = FirmPropertyDao.getInstance().insert(fp);
			return addedFP;
		} catch (FailedToInsert e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static FirmProperty getById(Long id) throws FailedToFindObject
	{
		return FirmPropertyDao.getInstance().getById(id);
	}
	
	public static void main(String argv[])
	{
//		FirmProperty fp = addFirmProperty();
//		System.out.println("Added FP : " + fp);
		
		
		try {
			FirmProperty fp = getById(1L);
			fp.setLoggingBAName("bugtesting");
			FirmProperty updatedFP = updateFirmProperty(fp);
			System.out.println("Updated fp : " + updatedFP);
		} catch (FailedToFindObject e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
