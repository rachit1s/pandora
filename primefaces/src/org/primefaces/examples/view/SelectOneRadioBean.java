package org.primefaces.examples.view;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

import org.primefaces.event.SelectEvent;


/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com  )
 * Creation Date : Apr 16, 2012
 */

class Student
{
	
	@Override
	public String toString() {
		return "Student [id=" + id + ", name=" + name + "]";
	}
	int id;
	String name;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @param id
	 * @param name
	 */
	public Student(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	
}
@ManagedBean(name="radioBean")
@ViewScoped
public class SelectOneRadioBean implements Serializable
{
	String selectedName;
	String selectedId;
	
	public String getSelectedId() {
		return selectedId;
	}

	public void setSelectedId(String selectedId) {
		this.selectedId = selectedId;
	}

	public ArrayList<Student> getStudents() {
		return students;
	}

	public void setStudents(ArrayList<Student> students) {
		this.students = students;
	}

	public String getSelectedName() {
		return selectedName;
	}

	public void setSelectedName(String selectedName) {
		this.selectedName = selectedName;
	}

	public SelectOneRadioBean()
	{
		students = new ArrayList<Student>();
		for(int i = 1 ;i <= 10 ; i++)
		{
			students.add(new Student(i,"name#"+i));
		}
		
		this.setSelectedId(students.get(0).getId()+"");
	}
	
	ArrayList<Student> students;
//	public ArrayList<String> getNames() {
//		return names;
//	}
//
//	public void setNames(ArrayList<String> names) {
//		this.names = names;
//	}

//	public List<SelectItem> getBaRolesList()
//	{
//		ArrayList<SelectItem> roles = new ArrayList<SelectItem>();
//		for(int i = 1 ;i <= 10 ; i++)
//		{
//			roles.add(new SelectItem("Item#"+i, "Item#"+i));
//		}
//		
//		return roles;
//	}

	public List<SelectItem> getStudentList()
	{
		ArrayList<SelectItem> list = new ArrayList<SelectItem>();
		for( Student s : students )
		{
			list.add(new SelectItem(s.getId()+"",s.getName()));
		}
		return list;
	}
	
	public void onChange()
	{
		System.out.println("on change radio");
	}
	
	public void radioChangeHandler(AjaxBehaviorEvent se)
	{
		System.out.println("on Change radio");
	}
}
