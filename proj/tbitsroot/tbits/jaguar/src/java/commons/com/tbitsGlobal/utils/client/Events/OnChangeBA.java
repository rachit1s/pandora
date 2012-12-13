package commons.com.tbitsGlobal.utils.client.Events;

import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class OnChangeBA extends TbitsBaseEvent {
	private BusinessAreaClient ba;
	private String sysPrefix;
	
	public OnChangeBA(String sysPrefix) {
		super("Loading Business Area... Please Wait...", 
				"Some error occurred after the BA was loaded... Please see the logs");
		this.sysPrefix = sysPrefix;
	}
	
	@Override
	public boolean beforeFire() {
		return true;
	}
	
	@Override
	public void afterFire() {
		super.afterFire();
	}

	public void setBa(BusinessAreaClient ba) {
		this.ba = ba;
	}

	public BusinessAreaClient getBa() {
		return ba;
	}

	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}
}
