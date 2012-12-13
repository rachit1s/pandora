package transbit.tbits.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

import junit.framework.TestCase;
/**
 * id	op	atachment list	field prop	field perm	result
 * 1	add request	null	carryover/non-carryover	permission/np	null
 * 2	add request	somethin/blank	carryover/non-carryover	permission	somethin/blank
 * 3	add request	something/blank	carryover/non-carryover	np	error!
 * 4	update request	null	carryover	permission/np	prev value
 * 5	update request	null	non-carryover	permission	blank
 * 6	update request	null	non-carryover	np	blank
 * 7	update request	something/blank	carryover/non-carryover	permission	something/blank
 * 8	update request	something/blank	carryover/non-carryover	np	error!
 * 
 * BA: tbits
 * Field: attachment, extatt
 * User: root -- permission to add/update both fields, mohit.saxena -- no permission to add/update any
 * @author sandeepgiri
 *
 */
public class TestAttachmentHandling extends TestCase {

	
	private static final String USER_WITH_PERMS = "root";
	private static final String USER_WITHOUT_PERMS = "bghosh";
	private static final String TBITS_BA = "55";
	private static final String FIELD_WITH_CO = "co";
	private static final String FIELD_WITHOUT_CO = "wco";
	
	private String attachmentInfo;
	
	public TestAttachmentHandling(String name) {
		super(name);
	}
	
	/*
	 * id	op	atachment list	field prop	field perm	result
	 * 1	add request	null	carryover/non-carryover	permission/np	null
	 */
	public void test1AddNULL() throws APIException
	{
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put(Field.BUSINESS_AREA, TBITS_BA);
		params.put(Field.USER, USER_WITH_PERMS);
		
		AddRequest addRequest = new AddRequest();
		Request request = addRequest.addRequest(params);
		
		assertEquals(null, request.getObject(FIELD_WITH_CO));
		assertEquals(null, request.getObject(FIELD_WITHOUT_CO));
	}
	
	/*
	 *  * id	op	atachment list	field prop	field perm	result
	 	* 2	add request	somethin/blank	carryover/non-carryover	permission	somethin/blank
	 */
	public void test2AddBlankAndSomethingWIthPerms() throws APIException
	{
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put(Field.BUSINESS_AREA, TBITS_BA);
		params.put(Field.USER, USER_WITH_PERMS);
		params.put(FIELD_WITH_CO, AttachmentInfo.toJson(new ArrayList<AttachmentInfo>()));
		params.put(FIELD_WITHOUT_CO, attachmentInfo);

		AddRequest addRequest = new AddRequest();
		Request request = addRequest.addRequest(params);
		
		assertEquals(0, ((Collection<AttachmentInfo>) request.getObject(FIELD_WITH_CO)).size());
		assertEquals(1, ((Collection<AttachmentInfo>) request.getObject(FIELD_WITHOUT_CO)).size());
	}
	/**
	 * * 3	add request	something/blank	carryover/non-carryover	np	error!
	 * @throws  
	 */
	public void test3AddSomethingWOPerms()
	{
		Hashtable<String, String> params = new Hashtable<String, String>();
		
		params.put(Field.BUSINESS_AREA, TBITS_BA);
		params.put(Field.USER, USER_WITHOUT_PERMS);
		params.put(FIELD_WITH_CO, AttachmentInfo.toJson(new ArrayList<AttachmentInfo>()));

		AddRequest addRequest = new AddRequest();
		boolean gotException = false;
		try {
			Request request = addRequest.addRequest(params);
		} catch (APIException e) {
			gotException = true;
			e.printStackTrace();
		}
		assertEquals(true, gotException);
		
		params = new Hashtable<String, String>();
		
		params.put(Field.BUSINESS_AREA, TBITS_BA);
		params.put(Field.USER, USER_WITHOUT_PERMS);
		params.put(FIELD_WITHOUT_CO, attachmentInfo);

		addRequest = new AddRequest();
		gotException = false;
		try {
			Request request = addRequest.addRequest(params);
		} catch (APIException e) {
			gotException = true;
			e.printStackTrace();
		}
		assertEquals(true, gotException);
	}
	
	/**
	 * * 4	update request	null	carryover	permission/np	prev value
	 * @throws APIException 
	 * @throws TBitsException 
	 * @throws DatabaseException 
	 */
	public void test4UpdateNullCO() throws APIException, TBitsException, DatabaseException
	{
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put(Field.BUSINESS_AREA, TBITS_BA);
		params.put(Field.USER, USER_WITH_PERMS);
		params.put(FIELD_WITH_CO, attachmentInfo);

		AddRequest addRequest = new AddRequest();
		Request request = addRequest.addRequest(params);
		
		Hashtable<String, String> uparams = new Hashtable<String, String>();
		uparams.put(Field.BUSINESS_AREA, TBITS_BA);
		uparams.put(Field.USER, USER_WITH_PERMS);
		uparams.put(Field.REQUEST, request.getRequestId()+"");
		
		Request updateRequest = new UpdateRequest().updateRequest(uparams);
		Request checkupdateRequest = Request.lookupBySystemIdAndRequestId(55, updateRequest.getRequestId());
		assertEquals(1, ((Collection<AttachmentInfo>)updateRequest.getObject(FIELD_WITH_CO)).size());
		
		uparams.put(Field.USER, USER_WITHOUT_PERMS);
		updateRequest = new UpdateRequest().updateRequest(uparams);
		assertEquals(1, ((Collection<AttachmentInfo>)updateRequest.getObject(FIELD_WITH_CO)).size());
		
	}
	
	/*
	 * * 5	update request	null	non-carryover	permission	blank
	 * 	6	update request	null	non-carryover	np	blank
	 */
	public void test56UpdateNullWCOnp() throws APIException, TBitsException
	{
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put(Field.BUSINESS_AREA, TBITS_BA);
		params.put(Field.USER, USER_WITH_PERMS);
		params.put(FIELD_WITHOUT_CO, attachmentInfo);

		AddRequest addRequest = new AddRequest();
		Request request = addRequest.addRequest(params);
		
		Hashtable<String, String> uparams = new Hashtable<String, String>();
		uparams.put(Field.BUSINESS_AREA, TBITS_BA);
		uparams.put(Field.USER, USER_WITH_PERMS);
		uparams.put(Field.REQUEST, request.getRequestId()+"");
		
		Request updateRequest = new UpdateRequest().updateRequest(uparams);
		assertEquals(0, ((Collection<AttachmentInfo>)updateRequest.getObject(FIELD_WITHOUT_CO)).size());
		
		uparams.put(Field.USER, USER_WITHOUT_PERMS);
		updateRequest = new UpdateRequest().updateRequest(uparams);
		assertEquals(0, ((Collection<AttachmentInfo>)updateRequest.getObject(FIELD_WITHOUT_CO)).size());
		
	}
	
	/*
	 * * 7	update request	something/blank	carryover/non-carryover	permission	something/blank
	 */
	public void test7UpdateSomethingAndBlankWP() throws APIException, TBitsException
	{
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put(Field.BUSINESS_AREA, TBITS_BA);
		params.put(Field.USER, USER_WITH_PERMS);
		params.put(FIELD_WITHOUT_CO, attachmentInfo);
		params.put(FIELD_WITH_CO, AttachmentInfo.toJson(new ArrayList<AttachmentInfo>()));
		
		AddRequest addRequest = new AddRequest();
		Request request = addRequest.addRequest(params);
		
		Hashtable<String, String> uparams = new Hashtable<String, String>();
		uparams.put(Field.BUSINESS_AREA, TBITS_BA);
		uparams.put(Field.USER, USER_WITH_PERMS);
		uparams.put(Field.REQUEST, request.getRequestId()+"");
		uparams.put(FIELD_WITHOUT_CO, AttachmentInfo.toJson(new ArrayList<AttachmentInfo>()));
		uparams.put(FIELD_WITH_CO, attachmentInfo);
		
		Request updateRequest = new UpdateRequest().updateRequest(uparams);
		assertEquals(0, ((Collection<AttachmentInfo>)updateRequest.getObject(FIELD_WITHOUT_CO)).size());
		assertEquals(1, ((Collection<AttachmentInfo>)updateRequest.getObject(FIELD_WITH_CO)).size());
		
	}
	
	/*
	 * * 8	update request	something/blank	carryover/non-carryover	np	error!
	 */
	public void test8UpdateSomethingAndBlankWOP() throws APIException
	{
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put(Field.BUSINESS_AREA, TBITS_BA);
		params.put(Field.USER, USER_WITH_PERMS);
		params.put(FIELD_WITHOUT_CO, attachmentInfo);
		params.put(FIELD_WITH_CO, AttachmentInfo.toJson(new ArrayList<AttachmentInfo>()));
		
		AddRequest addRequest = new AddRequest();
		Request request = addRequest.addRequest(params);
		
		Hashtable<String, String> uparams = new Hashtable<String, String>();
		uparams.put(Field.BUSINESS_AREA, TBITS_BA);
		uparams.put(Field.USER, USER_WITHOUT_PERMS);
		uparams.put(Field.REQUEST, request.getRequestId()+"");
		uparams.put(FIELD_WITHOUT_CO, AttachmentInfo.toJson(new ArrayList<AttachmentInfo>()));
		uparams.put(FIELD_WITH_CO, attachmentInfo);
		
		Request updateRequest;
		boolean errorEncountered = false;
		try {
			updateRequest = new UpdateRequest().updateRequest(uparams);
		} catch (Throwable e) {
			errorEncountered = true;
			e.printStackTrace();
		}
		
		assertEquals(true, errorEncountered);
		
	}
	
	public void test9UpdateModifyDeleteWP() throws APIException, TBitsException
	{
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put(Field.BUSINESS_AREA, TBITS_BA);
		params.put(Field.USER, USER_WITH_PERMS);
		
		AttachmentInfo ai1 = new AttachmentInfo("Somefile1.txt", 101, 0, 10);
		AttachmentInfo ai2 = new AttachmentInfo("Somefile2.txt", 102, 0, 10);
		AttachmentInfo ai3 = new AttachmentInfo("Somefile3.txt", 103, 0, 10);
		Collection<AttachmentInfo> ais = new ArrayList<AttachmentInfo>();
		ais.add(ai1);
		ais.add(ai2);
		ais.add(ai3);
		String attInfo = AttachmentInfo.toJson(ais);
		params.put(FIELD_WITHOUT_CO, attInfo);
		params.put(FIELD_WITH_CO, attInfo);
		
		AddRequest addRequest = new AddRequest();
		Request request = addRequest.addRequest(params);
		
		ais = new ArrayList<AttachmentInfo>();
		Collection<AttachmentInfo> attInfosAdded = (Collection<AttachmentInfo>) request.getObject(FIELD_WITH_CO);
		for(AttachmentInfo att: attInfosAdded){
			if(att.name.equals("Somefile1.txt")){
				// Modify
				att.repoFileId = 104;
				att.size = 20;
				ais.add(att);
			}
			else if(att.name.equals("Somefile2.txt")){
				// Add new
				att.repoFileId = 105;
				att.requestFileId = 0;
				att.size = 20;
				ais.add(att);
			}
		}
		attInfo = AttachmentInfo.toJson(ais);
		
		Hashtable<String, String> uparams = new Hashtable<String, String>();
		uparams.put(Field.BUSINESS_AREA, TBITS_BA);
		uparams.put(Field.USER, USER_WITH_PERMS);
		uparams.put(Field.REQUEST, request.getRequestId()+"");
		uparams.put(FIELD_WITHOUT_CO, attInfo);
		uparams.put(FIELD_WITH_CO, attInfo);
		
		Request updateRequest;
		updateRequest = new UpdateRequest().updateRequest(uparams);
		
		assertEquals(2, ((Collection<AttachmentInfo>) updateRequest.getObject(FIELD_WITHOUT_CO)).size());
		assertEquals(2, ((Collection<AttachmentInfo>) updateRequest.getObject(FIELD_WITH_CO)).size());
	}
	
	
	protected void setUp() throws Exception {
		super.setUp();
		
		AttachmentInfo ai = new AttachmentInfo("Somefile.txt", 1, 0, 10);
		
		Collection<AttachmentInfo> ais = new ArrayList<AttachmentInfo>();
		ais.add(ai);
		this.attachmentInfo = AttachmentInfo.toJson(ais);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
}
