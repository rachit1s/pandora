package transbit.tbits.dql.treecomponents;

public class Ordering{
	public DqlConstants.Order order;
	public String orderCol;
	public Ordering(String order, String orderCol) {
		if(order.toLowerCase().equals(DqlConstants.ASC_ORDER.toLowerCase()))
			this.order = DqlConstants.Order.ASC;
		else
			this.order = DqlConstants.Order.DESC;
		this.orderCol = orderCol;
	}
}
