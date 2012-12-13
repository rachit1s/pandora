package transbit.tbits.Escalation;

import java.io.Serializable;
import java.util.ArrayList;

public class EscalationConditionDetail implements Serializable {

	public final long serialVersionUID = 1L;
	public int ESC_COND_ID;
	public String DISPLAY_NAME;
	public String DESCRIPTION;
	public EscalationHierarchies ESC_HIERARCHY;
	public String SRC_BA;
	public String SRC_USER_FIELD;
	public String SRC_DATE_FIELD;
	public String DES_USER_FIELD;
	public String DES_DATE_FIELD;
	public String SPAN;
	public String ON_BEHALF_USER;
	public String DQL;
	public ArrayList<EscalationConditionParameters> ESC_COND_PARAMETERS;
	public Boolean IS_ACTIVE;

	public Integer getEscCondId() {

		return ESC_COND_ID;
	}

	public void setEscCondId(Integer escCondId) {
		this.ESC_COND_ID = escCondId;
	}

	public void setDisName(String disName) {

		this.DISPLAY_NAME = disName;

	}

	public String getDisName() {
		return DISPLAY_NAME;
	}

	public void setDescription(String des) {

		this.DESCRIPTION = des;

	}

	public String getDescription() {
		return DESCRIPTION;
	}

	public void setEscHierarchy(EscalationHierarchies hierarchy) {

		this.ESC_HIERARCHY = hierarchy;

	}

	public EscalationHierarchies getEscHierarchy() {
		return ESC_HIERARCHY;
	}

	public void setSrcBa(String bac) {

		this.SRC_BA = bac;

	}

	public String getSrcBa() {
		return SRC_BA;
	}

	public void setSrcUserField(String fcu) {

		this.SRC_USER_FIELD = fcu;

	}

	public String getSrcUserField() {
		return SRC_USER_FIELD;
	}

	public void setSrcDateField(String fcd) {

		this.SRC_DATE_FIELD = fcd;

	}

	public String getSrcDateField() {
		return SRC_DATE_FIELD;
	}

	public void setDesUserField(String fcu) {

		this.DES_USER_FIELD = fcu;

	}

	public String getDesUserField() {
		return DES_USER_FIELD;
	}

	public void setDesDateField(String fcd) {

		this.DES_DATE_FIELD = fcd;

	}

	public String getDesDateField() {
		return DES_DATE_FIELD;
	}

	public void setSpan(String span) {

		this.SPAN = span;

	}

	public String getSpan() {
		return SPAN;
	}

	public void setOnBehalfUser(String uc) {

		this.ON_BEHALF_USER = uc;

	}

	public String getOnBehalfUser() {
		return ON_BEHALF_USER;
	}

	public void setDql(String dql) {

		this.DQL = dql;

	}

	public String getDql() {
		return DQL;
	}

	public void setParams(ArrayList<EscalationConditionParameters> params) {

		this.ESC_COND_PARAMETERS = params;

	}

	public ArrayList<EscalationConditionParameters> getParams() {
		return ESC_COND_PARAMETERS;
	}

	public void setIsActive(Boolean isActive) {

		this.IS_ACTIVE = isActive;

	}

	public Boolean getIsActive() {
		return IS_ACTIVE;
	}

}
