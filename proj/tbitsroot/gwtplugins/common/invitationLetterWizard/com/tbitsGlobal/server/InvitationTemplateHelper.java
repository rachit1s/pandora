package invitationLetterWizard.com.tbitsGlobal.server;

import java.util.ArrayList;

public class InvitationTemplateHelper {
	private String ref;
	private String subject;
	private String address;
	private String body1;
	private String body2;
	private String footer;
	private ArrayList<String[]> ScheduleList, UserList;

	private String batchNo;
	private String batchRef;
	private String applicants;
	
	private String project;
	private String embassy;

	public InvitationTemplateHelper(String ref, String subject, String address,
			String body1, String body2, String footer,
			ArrayList<String[]> userList) {
		this.ref = ref;
		this.subject = subject;
		this.address = address;
		this.body1 = body1;
		this.body2 = body2;
		this.footer = footer;
		this.UserList = userList;
	}

	public InvitationTemplateHelper(String ref, String subject, String address,
			String body1, String body2, String footer,
			ArrayList<String[]> userList, ArrayList<String[]> scheduleList) {
		this(ref, subject, address, body1, body2, footer, userList);
		this.ScheduleList = scheduleList;

	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getBody1() {
		return body1;
	}

	public void setBody1(String body1) {
		this.body1 = body1;
	}

	public String getBody2() {
		return body2;
	}

	public void setBody2(String body2) {
		this.body2 = body2;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getRef() {
		return ref;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public ArrayList<String[]> getScheduleList() {
		return ScheduleList;
	}

	public void setScheduleList(ArrayList<String[]> ScheduleList) {
		this.ScheduleList = ScheduleList;
	}

	public ArrayList<String[]> getUserList() {
		return UserList;
	}

	public void setUserList(ArrayList<String[]> UserList) {
		this.UserList = UserList;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchRef(String batchRef) {
		this.batchRef = batchRef;
	}

	public String getBatchRef() {
		return batchRef;
	}

	public void setApplicants(String applicants) {
		this.applicants = applicants;
	}

	public String getApplicants() {
		return applicants;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getProject() {
		return project;
	}

	public void setEmbassy(String embassy) {
		this.embassy = embassy;
	}

	public String getEmbassy() {
		return embassy;
	}
}
