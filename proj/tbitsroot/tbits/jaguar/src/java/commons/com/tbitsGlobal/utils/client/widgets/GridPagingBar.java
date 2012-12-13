package commons.com.tbitsGlobal.utils.client.widgets;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar.PagingToolBarImages;

/**
 * A {@link ToolBar} that provides paging facility for a grid
 * 
 * @author sourabh
 *
 */
public abstract class GridPagingBar extends ToolBar{
	protected Status pages;
	protected SimpleComboBox<Integer> selectPage;
	
	protected int currentPage;
	protected int maxPage;
	protected int totalRecords;
	protected int pageSize;
	
	protected Button first;
	protected Button pre;
	protected Button next;
	protected Button last;
	
	private List<Integer> allowedSizes;
	
	public GridPagingBar(int pageSize) {
		super();
		
		this.pageSize = pageSize;
	}
	
	@Override
	protected void beforeRender() {
		super.beforeRender();
		
		PagingToolBar bar = new PagingToolBar(100);
		PagingToolBarImages images = bar.getImages();
		
		first = new Button();	// Loads the first page
		first.setIcon(images.getFirst());
		first.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				loadPage(1);
			}
		});
		this.add(first);
		
		pre = new Button();		// Loads the previos page
		pre.setIcon(images.getPrev());
		pre.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				loadPage(currentPage - 1);
			}
		});
		this.add(pre);
		
		selectPage = new SimpleComboBox<Integer>();		// shows all page nos. in a drop down
		selectPage.setEditable(false);
		selectPage.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<Integer>>(){
			@Override
			public void selectionChanged(
					SelectionChangedEvent<SimpleComboValue<Integer>> se) {
				int page = se.getSelectedItem().getValue();
				if(page == currentPage)
					return;
				loadPage(page);
			}});
		this.add(selectPage);

		next = new Button();				// Loads next page
		next.setIcon(images.getNext());
		next.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				loadPage(currentPage + 1);
			}
		});
		this.add(next);
		
		last = new Button();			// Loads last page
		last.setIcon(images.getLast());
		last.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				loadPage(maxPage);
			}
		});
		this.add(last);
		
		SimpleComboBox<Integer> noOfRecords = new SimpleComboBox<Integer>();	// Sets the page size
		if(allowedSizes != null && allowedSizes.size() > 0){
			for(int size : allowedSizes){
				noOfRecords.add(size);
			}
		}else{
			noOfRecords.add(50);
			noOfRecords.add(100);
			noOfRecords.add(150);
			noOfRecords.add(200);
		}
		noOfRecords.setEmptyText("Set Page Size");
		noOfRecords.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<Integer>>(){
			@Override
			public void selectionChanged(
					SelectionChangedEvent<SimpleComboValue<Integer>> se) {
				int val = se.getSelectedItem().getValue();
				if(val <= 0)
					return;
				GridPagingBar.this.pageSize = val;
				loadPage(1);
			}
		});
		this.add(noOfRecords);
		
		this.add(new FillToolItem());
		pages = new Status();
		pages.setBox(true);
		pages.setText("0-0 of 0");
		this.add(pages);
	}
	
	/**
	 * Enables and disable the buttons according to the currentPage value.
	 * Also sets the value in status box and combo.
	 * 
	 * @param totalPages. Total Pages in the grid.
	 */
	public void adjustButtons(int page, int totalRecords){
		if(rendered){
			this.currentPage = page;
			this.totalRecords = totalRecords;
			
			int totalPages = getTotalPages();	//Get total no. of pages
			if(maxPage != totalPages){			// If no. of pages have changed refill the page select drop down
				maxPage = totalPages;
				selectPage.removeAll();
				for(int i = 1; i <= maxPage; i++)
					selectPage.add(i);
			}
			
			// Enable all buttons for now
			first.enable();
			pre.enable();
			next.enable();
			last.enable();
			
			if(currentPage == 1){	// If first page
				first.disable();
				pre.disable();
			}
			if(currentPage == maxPage){	// If last page
				last.disable();
				next.disable();
			}
			
			int lowerBound = ((page - 1) * pageSize) + 1;	// least record no. on the page
			int uppperBound = lowerBound + pageSize - 1;	// max record no. on the page
			if(uppperBound > totalRecords)	// Max record no. can not be greater than totalRecords
				uppperBound = totalRecords;
			
			pages.setText(lowerBound + "-" + uppperBound + " of " + totalRecords);
			
			selectPage.setSimpleValue(currentPage);	// Select the current page
		}
	}
	
	/**
	 * @return Calculates the no. of pages
	 */
	public int getTotalPages(){
		if(pageSize > 0){
			int temp = totalRecords/pageSize;
			if(totalRecords % pageSize > 0)
				temp++;
			return temp;
		}
		return 0;
	}
	
	/**
	 * Loads the specified page in the grid
	 * @param page
	 */
	protected abstract void loadPage(int page);

	public int getCurrentPage() {
		return currentPage;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param allowedSizes. A list of allowed page sizes
	 */
	public void setAllowedSizes(List<Integer> allowedSizes) {
		this.allowedSizes = allowedSizes;
	}

	public List<Integer> getAllowedSizes() {
		return allowedSizes;
	}
}
