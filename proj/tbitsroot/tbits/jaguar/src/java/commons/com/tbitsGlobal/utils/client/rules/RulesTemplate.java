package commons.com.tbitsGlobal.utils.client.rules;

import java.util.HashMap;

import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class RulesTemplate extends TbitsModelData{
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "name";
	public static final String CUSTOM = "custom";
	
	// enum types for the objType
	public static final int FUNCTION_MAP = 1;
	public static final int CODE = 2;
	
	private int objType;
	private TextField seqNumber;
	private Object obj;
	private RuleDef ruleDef;
	
	public RulesTemplate(){
		ruleDef = new RuleDef();
		seqNumber = null;
	}

	public RulesTemplate(String iClassName){
		this.set(NAME, iClassName);
		ruleDef = new RuleDef(iClassName);
	}
	
	public RulesTemplate(RuleDef rd) {
		ruleDef = rd;
		this.set(NAME, ruleDef.getClassDef().getImplementsClass());
	}

	public void setObjType(int objType) {
		this.objType = objType;
	}

	public int getObjType() {
		return objType;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public Object getObj() {
		return obj;
	}

	public RuleDef getRuleDef() {
		return ruleDef;
	}

	public void setName(String name){
		ruleDef.setName(name);
		ruleDef.getClassDef().setName(name);
	}
	
	public String getName(){
		return ruleDef.getName();
	}

	public void setSeqNumber(TextField seqNumber) {
		this.seqNumber = seqNumber;
	}

	@SuppressWarnings("unchecked")
	public RuleDef prepareRuleDef() {
		
		if(!check())
			return null;
		
		if(!get(RulesTemplate.NAME).equals(RulesTemplate.CUSTOM)){
			HashMap<FunctionDef, TextArea> editorMap = (HashMap<FunctionDef, TextArea>) obj;
			Iterable<FunctionDef> allFuncs = editorMap.keySet();
			for(FunctionDef fd : allFuncs)
				fd.code = (editorMap.get(fd).getValue() == null)?"":editorMap.get(fd).getValue();
		}
		else{
			ruleDef.setRuleCode((((TextArea)obj).getValue() == null)?"":((TextArea)obj).getValue());
		}
		if(seqNumber != null && seqNumber.getValue() != null){
			ruleDef.setSeqNo(Double.parseDouble((String)seqNumber.getValue()));
		}
		return ruleDef;
	}

	public boolean check() {
		
		if(seqNumber == null || seqNumber.getValue() == null)
			return true;
		try{
			Double.parseDouble((String)seqNumber.getValue());
		}
		catch(NumberFormatException e){
			return false;
		}
		return true;
	}
	
}
