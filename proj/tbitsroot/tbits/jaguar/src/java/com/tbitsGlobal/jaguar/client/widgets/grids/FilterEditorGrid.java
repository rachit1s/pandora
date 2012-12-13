package com.tbitsGlobal.jaguar.client.widgets.grids;


/**
 * Grid having filtering capabilities.
 * 
 * TODO : Implementation incomplete
 * 
 * @author sourabh
 *
 */
public class FilterEditorGrid {
//	private TbitsEditorGridBaseFilter storeFilter;
//	private HashMap<String, ArrayList<POJO>> distictMap;
//	
//	private boolean enableFilter = false; 
//	
//	class TbitsEditorGridBaseFilter implements StoreFilter<TbitsTreeRequestData>{
//		private HashMap<String, ArrayList<POJO>> map;
//		
//		public TbitsEditorGridBaseFilter() {
//			map = new HashMap<String, ArrayList<POJO>>();
//		}
//		
//		public boolean select(Store<TbitsTreeRequestData> store,
//				TbitsTreeRequestData parent, TbitsTreeRequestData item,
//				String property) {
//			List<ColumnConfig> columns = cm.getColumns();
//			for(ColumnConfig column : columns){
//				String colId = column.getId();
//				ArrayList<POJO> objList = map.get(colId);
//				if(objList != null){
//					if(item.getAsPOJO(colId) != null && !objList.contains(item.getAsPOJO(colId)))
//						return false;
//				}
//			}
//			return true;
//		}
//		
//		public void addParam(String key, POJO obj){
//			if(!map.containsKey(key)){
//				map.put(key, new ArrayList<POJO>());
//			}
//			map.get(key).add(obj);
//		}
//		
//		public void removeParam(String key, POJO obj){
//			if(map.containsKey(key))
//				map.get(key).remove(obj);
//		}
//		
//	}
//	
//	public FilterEditorGrid(UIContext parentContext) {
//		super(parentContext);
//		
//		TreeGridView view = new TreeGridView(){
//			@Override
//			protected void onColumnWidthChange(int column, int width) {
//				super.onColumnWidthChange(column, width);
//				this.refresh(false);
//			}
//			
//			@Override
//			protected Menu createContextMenu(int colIndex) {
//				Menu menu = new Menu();
//				MenuItem filter = new MenuItem("Filter");
//				
//				Menu filterMenu = new Menu();
//				final String colId = cm.getColumn(colIndex).getId();
//				List<TbitsTreeRequestData> models = FilterEditorGrid.this.treeStore.getModels();
//				ArrayList<POJO> distictObjs = new ArrayList<POJO>();
//				for(TbitsTreeRequestData model : models){
//					POJO value = model.getAsPOJO(colId);
//					if(!distictObjs.contains(value)){
//						distictObjs.add(value);
//					}
//				}
//				
//				for(final POJO obj : distictObjs){
//					final CheckMenuItem check = new CheckMenuItem();
//					check.setHideOnClick(false);
//				    check.setText(obj.toString());
//				    check.setChecked(true);
//				    check.addSelectionListener(new SelectionListener<MenuEvent>() {
//				        public void componentSelected(MenuEvent ce) {
//				        	if(check.isChecked())
//				        		storeFilter.addParam(colId, obj);
//				        	else
//				        		storeFilter.removeParam(colId, obj);
//				        	applyFilter();
//				        }
//				      });
//				    filterMenu.add(check);
//				}
//				
//				filter.setSubMenu(filterMenu);
//				menu.add(filter);
//				return menu;
//			}
//		};
//		view.setEmptyText("No records fetched for this Business Area");
//		this.setView(view);
//	}
//	
//	private void setEnableFilter(boolean enableFilter) {
//		this.enableFilter = enableFilter;
//		
//		if(this.enableFilter){
//			storeFilter = new TbitsEditorGridBaseFilter();
//			this.treeStore.addFilter(storeFilter);
//			
//			distictMap = new HashMap<String, ArrayList<POJO>>();
//			
//			final DelayedTask task = new DelayedTask(new Listener<BaseEvent>(){
//				public void handleEvent(BaseEvent be) {
//					calculateDistict();
//				}});
//			
//			StoreListener<TbitsTreeRequestData> storeListener = new StoreListener<TbitsTreeRequestData>(){
//				@Override
//				public void storeAdd(StoreEvent<TbitsTreeRequestData> se) {
//					task.delay(100);
//				}
//				
//				@Override
//				public void storeDataChanged(StoreEvent<TbitsTreeRequestData> se) {
//					task.delay(100);
//				}
//				
//				@Override
//				public void storeClear(StoreEvent<TbitsTreeRequestData> se) {
//					task.delay(100);
//				}
//				
//				@Override
//				public void storeRemove(StoreEvent<TbitsTreeRequestData> se) {
//					task.delay(100);
//				}
//				
//				@Override
//				public void storeUpdate(StoreEvent<TbitsTreeRequestData> se) {
//					task.delay(100);
//				}
//			};
//			
//			treeStore.addStoreListener(storeListener);
//		}
//	}
//
//	public boolean isEnableFilter() {
//		return enableFilter;
//	}
//	
//	private void calculateDistict(){
//		List<ColumnConfig> columns = cm.getColumns();
//		List<TbitsTreeRequestData> models = treeStore.getModels();
//		for(ColumnConfig column : columns){
//			String colId = column.getId();
//			if(!distictMap.containsKey(colId))
//				distictMap.put(colId, new ArrayList<POJO>());
//			
//			ArrayList<POJO> obsoleteList = new ArrayList<POJO>();
//			for(POJO obj : distictMap.get(colId)){
//				if(treeStore.findModel(colId, obj) == null)
//					obsoleteList.add(obj);
//			}
//			if(obsoleteList.size() > 0){
//				distictMap.get(colId).removeAll(obsoleteList);
//				for(POJO obj : obsoleteList)
//					storeFilter.removeParam(colId, obj);
//			}
//			for(TbitsTreeRequestData model : models){
//				POJO value = model.getAsPOJO(colId);
//				if(value != null){
//					if(!distictMap.get(colId).contains(value)){
//						distictMap.get(colId).add(value);
//						storeFilter.addParam(colId, value);
//					}
//				}
//			}
//		}
//		applyFilter();
//	}
//	
//	private void applyFilter(){
////		List<ColumnConfig> columns = cm.getColumns();
////		for(ColumnConfig col : columns){
////			String colId = col.getId();
////			
////		}
//		FilterEditorGrid.this.treeStore.applyFilters(null);
//	}
}	
