package transbit.tbits.admin.common;

import java.util.ArrayList;

public class MappingNode {
	public MappingNode(String urlPart, Class c) {
		super();
		this.urlPart = urlPart;
		this.className = c;
	}
	String urlPart;
	Class className;
	ArrayList<MappingNode> children = new ArrayList<MappingNode>();
}
