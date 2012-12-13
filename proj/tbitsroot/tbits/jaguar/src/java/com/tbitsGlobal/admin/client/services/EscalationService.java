package com.tbitsGlobal.admin.client.services;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public interface EscalationService extends RemoteService {
	public ArrayList<TbitsModelData> getEscalationCondition(int sysId) throws TbitsExceptionClient;

	boolean insertEscalationCondition(int sysId, TbitsModelData tb) throws TbitsExceptionClient;

	boolean deleteEscalationCondition(int sysId, TbitsModelData tb) throws TbitsExceptionClient;

	boolean insertUserhierarchy(int aSystemId, int aChildUserId,
			int aParentUserId) throws TbitsExceptionClient;

	boolean deleteUserhierarchy(int sysId, int userId, int parentId) throws TbitsExceptionClient;

	HashMap<Integer, ArrayList<Integer>> getAllParentChildMapping(int sysID) throws TbitsExceptionClient;
}
