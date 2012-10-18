package com.tbitsglobal.ddc.dao;

import com.tbitsglobal.ddc.domain.FirmProperty;

public class FirmPropertyDao 
{
	private static FirmProperty fp;
	static
	{
		fp = new FirmProperty(0, "BA1", "@gmail.com", "crazy.nattu@gmail.com", 1, "number1Field", 2, "number2Field", 3," number3Field", 1);
	}
	private static FirmPropertyDao fpDao = new FirmPropertyDao();;
	
	// no need of synch etc.. 
	public static FirmPropertyDao getInstance()
	{
		return fpDao;
	}

	public FirmProperty findFirmPropertyByEmailId(String emailId)
	{
		return fp;
	}
	
	public FirmProperty findFirmPropertyByDocController(String userLogin)
	{
		return fp;
	}
}
