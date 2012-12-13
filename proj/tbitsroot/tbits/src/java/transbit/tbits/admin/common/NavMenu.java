package transbit.tbits.admin.common;

import java.util.ArrayList;

import com.google.gson.Gson;

/*
 * It is a singleton Navigation Menu. Each tab will register with the Menu.
 */
public class NavMenu {
	static NavMenu instance = null;
	
	public synchronized static NavMenu getInstance()
	{
		if(instance == null)
		{
			instance = new NavMenu();
		}
		return instance;
	}
	
	private NavMenu()
	{
		AppMenu = new ArrayList<MenuItem>();
		BAMenu = new ArrayList<MenuItem>();
	}
	public ArrayList<MenuItem> AppMenu;
	public ArrayList<MenuItem> BAMenu;
	
	String toJson()
	{
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public static void main(String[] args)
	{
		NavMenu nav = NavMenu.getInstance();
		nav.AppMenu.add(new MenuItem("Scheduled tasks", "/tasks", ""));
		nav.AppMenu.add(new MenuItem("Reports", "/reports", ""));
		nav.AppMenu.add(new MenuItem("Users", "users", ""));
		
		nav.BAMenu.add(new MenuItem("Properties", "/props", ""));
		nav.BAMenu.add(new MenuItem("Ba Users", "/bausers", ""));
		
		System.out.println("\n" + nav.toJson());
	}
}
