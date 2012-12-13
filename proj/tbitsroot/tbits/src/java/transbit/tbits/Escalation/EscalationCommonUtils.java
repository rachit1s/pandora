package transbit.tbits.Escalation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dev.util.collect.HashMap;


import transbit.tbits.common.DataSourcePool;


public class EscalationCommonUtils {
	
	
	public static List<EscalationHierarchies> getEscalationHierarchies(){
		
		ArrayList<EscalationHierarchies> escList=new ArrayList<EscalationHierarchies>();
		
		Connection connection = null;

		try {
			connection = DataSourcePool.getConnection();

			String sql = "select * from escalation_hierarchy_details";

			PreparedStatement ps = connection.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			if (null != rs) {
				while (rs.next()) {
					int escId = rs.getInt("esc_id");
					String name = rs.getString("name");
					String disName=rs.getString("display_name");
					String desc = rs.getString("description");
					
					EscalationHierarchies hierarchies=new EscalationHierarchies();
					
					hierarchies.setEscId(escId);
					hierarchies.setName(name);
					hierarchies.setDisplayName(disName);
					hierarchies.setDescription(desc);
					escList.add(hierarchies);
				}
				rs.close();
				return escList;
			}
			
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
				}
				connection = null;
			}
		}
		
		return escList;
	}
	
	public static List<EscalationHierarchyValues> saveEscalationHierarchyValues(
			EscalationHierarchies hiearachy,
			List<EscalationHierarchyValues> values) {
		
		

		return null;

	}
	
	
	static class Node{
	    public final Integer uId;
	    public final HashSet<Edge> inEdges;
	    public final HashSet<Edge> outEdges;
	    public Node(int uid) {
	      this.uId = uid;
	      inEdges = new HashSet<Edge>();
	      outEdges = new HashSet<Edge>();
	    }
	    public Node addEdge(Node node){
	      Edge e = new Edge(this, node);
	      outEdges.add(e);
	      node.inEdges.add(e);
	      return this;
	    }
	    @Override
	    public String toString() {
	      return uId.toString();
	    }
	    @Override
	    public boolean equals(Object obj)
	    {
	    	Node n=(Node)obj;
	    	if(this.uId==n.uId)
	    		return true;
	    		else 
	    			return false;
	    	
	    }
	  }

	  static class Edge{
	    public final Node from;
	    public final Node to;
	    public Edge(Node from, Node to) {
	      this.from = from;
	      this.to = to;
	    }
	    @Override
	    public boolean equals(Object obj) {
	      Edge e = (Edge)obj;
	      return e.from == from && e.to == to;
	    }
	  }
	  
	  
	  
	  public static int checkCycleInEscalationHierarchy(List<EscalationHierarchyValues> hierarchyValues)
	  {
		  HashMap<Integer,Node> allChildParentNodeMap=new HashMap<Integer, Node>();
		  
		  ArrayList<Node> allChildParentNodeList=new ArrayList<Node>();
		  
		  int returnId=0;
		  
		//L <- Empty list that will contain the sorted elements
		    ArrayList<Node> L = new ArrayList<Node>();

		    //S <- Set of all nodes with no incoming edges
		    HashSet<Node> S = new HashSet<Node>(); 
		    
		  for(EscalationHierarchyValues values:hierarchyValues)
		  {
			  int childId=values.getChlidUser().getUserId();
			  int parentId=values.getParentUser().getUserId();
			  Node childNode=null;
			  Node parentNode=null;
			  if(allChildParentNodeMap.containsKey(childId))
			  {
				  childNode=allChildParentNodeMap.get(childId);
			  }
			  else
			  {
				  childNode=new Node(childId);
				  allChildParentNodeMap.put(childId, childNode);
				  allChildParentNodeList.add(childNode);
			  }
			  if(allChildParentNodeMap.containsKey(parentId))
			  {
				  parentNode=allChildParentNodeMap.get(parentId);
			  }
			  else
			  {
				  parentNode=new Node(parentId);
				  allChildParentNodeMap.put(parentId, parentNode);
				  allChildParentNodeList.add(parentNode);
			  }
			  childNode.addEdge(parentNode);  
		  }
		  
		  for(Node n:allChildParentNodeList)
		  {
			  if(n.inEdges.size()==0)
			  {
				  S.add(n);
			  }
		  }
		  
		  while(!S.isEmpty())
		  {
			  Node n=S.iterator().next();
			  S.remove(n);
			  L.add(n);
			  
			  for(Iterator<Edge> it = n.outEdges.iterator();it.hasNext();){
			        //remove edge e from the graph
			        Edge e = it.next();
			        Node m = e.to;
			        it.remove();//Remove edge from n
			        m.inEdges.remove(e);//Remove edge from m

			        //if m has no other incoming edges then insert m into S
			        if(m.inEdges.isEmpty()){
			          S.add(m);
			        }
			      }
		  }
		  
		  //Check to see if all edges are removed
		    boolean cycle = false;
		    for(Node n : allChildParentNodeList){
		      if(!n.inEdges.isEmpty()){
		        cycle = true;
		        returnId=n.uId;
		        break;
		      }
		    }
		    if(cycle){
		      //System.out.println("Cycle present");
		      return returnId;
		      
		    }else{
		      return returnId;
		    }
	  }

	 /* public static void main(String[] args) {
	    Node seven = new Node(7);
	    Node five = new Node(5);
	    Node three = new Node(3);
	    Node eleven = new Node(11);
	    Node eight = new Node(8);
	    Node two = new Node(2);
	    Node nine = new Node(9);
	    Node ten = new Node(10);
	    seven.addEdge(eleven).addEdge(eight);
	    five.addEdge(eleven);
	    three.addEdge(eight).addEdge(ten);
	    eleven.addEdge(two).addEdge(nine).addEdge(ten);
	    eight.addEdge(nine).addEdge(ten);

	    Node[] allNodes = {seven, five, three, eleven, eight, two, nine, ten};
	    //L <- Empty list that will contain the sorted elements
	    ArrayList<Node> L = new ArrayList<Node>();

	    //S <- Set of all nodes with no incoming edges
	    HashSet<Node> S = new HashSet<Node>(); 
	    for(Node n : allNodes){
	      if(n.inEdges.size() == 0){
	        S.add(n);
	      }
	    }

	    //while S is non-empty do
	    while(!S.isEmpty()){
	      //remove a node n from S
	      Node n = S.iterator().next();
	      S.remove(n);

	      //insert n into L
	      L.add(n);

	      //for each node m with an edge e from n to m do
	      for(Iterator<Edge> it = n.outEdges.iterator();it.hasNext();){
	        //remove edge e from the graph
	        Edge e = it.next();
	        Node m = e.to;
	        it.remove();//Remove edge from n
	        m.inEdges.remove(e);//Remove edge from m

	        //if m has no other incoming edges then insert m into S
	        if(m.inEdges.isEmpty()){
	          S.add(m);
	        }
	      }
	    }
	    //Check to see if all edges are removed
	    boolean cycle = false;
	    for(Node n : allNodes){
	      if(!n.inEdges.isEmpty()){
	        cycle = true;
	        break;
	      }
	    }
	    if(cycle){
	      System.out.println("Cycle present, topological sort not possible");
	    }else{
	      System.out.println("Topological Sort: "+Arrays.toString(L.toArray()));
	    }
	  }*/
	  
	  

}
