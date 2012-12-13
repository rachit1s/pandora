package mom.com.tbitsGlobal.client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mom.com.tbitsGlobal.client.Extensions.MOMGridTab;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Time;
import com.extjs.gxt.ui.client.widget.form.TimeField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.Uploader.AttachmentFieldContainer;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCombo;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldMultiValue;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;
import commons.com.tbitsGlobal.utils.client.pojo.POJODate;
import commons.com.tbitsGlobal.utils.client.pojo.POJOString;
import commons.com.tbitsGlobal.utils.client.widgets.DateTimeControl;
import commons.com.tbitsGlobal.utils.client.widgets.TypeFieldControl;
import commons.com.tbitsGlobal.utils.client.widgets.UserPicker;

@SuppressWarnings("unchecked")
public class MeetingHeader extends ContentPanel implements IFixedFields, MOMConstants, IFormConstants {
	public static String CONTEXT_CAPTION = "caption";
	public static String CONTEXT_PARENT_TAB = "tab";

	private UIContext myContext;
	private FormData formData;
	private HashMap<String, Field> fields;
	private MOMGridTab parentTab;
	private String caption;
	
	private AttachmentFieldContainer attContainer;
	
//	private Time endTime;

	private TbitsTreeRequestData headerModel;

	public MeetingHeader(UIContext parentContext) {
		super();

		this.myContext = parentContext;
		this.setLayout(new FormLayout());

		this.setScrollMode(Scroll.AUTO);
		this.setHeading("Details");

		formData = new FormData("-20");
		fields = new HashMap<String, Field>();
		caption = myContext.getValue(CONTEXT_CAPTION, String.class);
		parentTab = myContext.getValue(CONTEXT_PARENT_TAB, MOMGridTab.class);

		this.setHeading(caption + " Details");
	}

	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);

		this.getLayoutTarget().setStyleAttribute("padding", "5px");
		
		FieldCache fieldCache = CacheRepository.getInstance().getCache(FieldCache.class);
		
		BAField field = fieldCache.getObject(FORM_TITLE);
		if(field != null){
			TextField<String> meetingName = new TextField<String>();
			meetingName.setFieldLabel(field.getDisplayName());
			meetingName.setName(FORM_TITLE);
			meetingName.setEmptyText("Enter title of the " + caption);
			meetingName.setLabelStyle("font-weight:bold");
			meetingName.addListener(Events.Change, new Listener<FieldEvent>() {
				public void handleEvent(FieldEvent be) {
					String title = (String) be.getField().getValue();
					if (title.length() > 20)
						title = title.substring(0, 17) + "...";
					parentTab.setText(caption + " - " + title);
				}
			});
			this.add(meetingName, formData);
			fields.put(FORM_TITLE, meetingName);
		}
		
		field = fieldCache.getObject(FORM_MEETING_TYPE);
		if(field != null){
			TypeFieldControl meetingType = new TypeFieldControl((BAFieldCombo) field);
			meetingType.setEmptyText("Enter type of the " + caption);
			this.add(meetingType, formData);
			fields.put(FORM_MEETING_TYPE, meetingType);
		}
		
		field = fieldCache.getObject(FORM_VENUE);
		if(field != null){
			TextField<String> meetingVenue = new TextField<String>();
			meetingVenue.setFieldLabel(field.getDisplayName());
			meetingVenue.setName(FORM_VENUE);
			meetingVenue.setEmptyText("Enter venue of the " + caption);
			meetingVenue.setLabelStyle("font-weight:bold");
			this.add(meetingVenue, formData);
			fields.put(FORM_VENUE, meetingVenue);
		}

		field = fieldCache.getObject(FORM_START_DATE);
		if(field != null){
			DateTimeControl meetingDate = new DateTimeControl();
			meetingDate.setWidth(200);
			meetingDate.setFieldLabel(field.getDisplayName());
			meetingDate.setName(FORM_START_DATE);
			meetingDate.setFormat(ClientUtils.getCurrentUser().getWebDateFormat());
			meetingDate.setEmptyText("Enter Start Date of the " + caption);
			meetingDate.setLabelStyle("font-weight:bold");
			this.add(meetingDate, formData);
			meetingDate.setValue(new Date());
			fields.put(FORM_START_DATE, meetingDate);
		}

		field = fieldCache.getObject(FORM_START_TIME);
		if(field != null){
			TimeField meetingStartTime = new TimeField();
			meetingStartTime.setWidth(200);
			meetingStartTime.setFieldLabel(field.getDisplayName());
			meetingStartTime.setName(FORM_START_TIME);
			meetingStartTime.setLabelStyle("font-weight:bold");
			meetingStartTime.setDateValue(new Date());
			this.add(meetingStartTime, formData);
			fields.put(FORM_START_TIME, meetingStartTime);
		}
		
		field = fieldCache.getObject(FORM_END_DATE);
		if(field != null){
			DateTimeControl meetingDate = new DateTimeControl();
			meetingDate.setWidth(200);
			meetingDate.setFieldLabel(field.getDisplayName());
			meetingDate.setName(FORM_END_DATE);
			meetingDate.setFormat(ClientUtils.getCurrentUser().getWebDateFormat());
			meetingDate.setEmptyText("Enter End Date of the " + caption);
			meetingDate.setLabelStyle("font-weight:bold");
			this.add(meetingDate, formData);
			meetingDate.setValue(new Date());
			fields.put(FORM_END_DATE, meetingDate);
		}
		
		field = fieldCache.getObject(FORM_END_TIME);
		if(field != null){
			TimeField meetingEndTime = new TimeField();
			meetingEndTime.setWidth(200);
			meetingEndTime.setFieldLabel(field.getDisplayName());
			meetingEndTime.setName(FORM_END_TIME);
			meetingEndTime.setLabelStyle("font-weight:bold");
			meetingEndTime.setDateValue(new Date());
			this.add(meetingEndTime, formData);
			fields.put(FORM_END_TIME, meetingEndTime);
		}

		field = fieldCache.getObject(FORM_SUBSCRIBERS);
		if(field != null){
			UserPicker subscribers = new UserPicker((BAFieldMultiValue) field);
			subscribers.setName(FORM_SUBSCRIBERS);
			subscribers.setFieldLabel(field.getDisplayName());
			subscribers.setLabelStyle("font-weight:bold");
			this.add(subscribers, formData);
			fields.put(FORM_SUBSCRIBERS, subscribers);
		}

		field = fieldCache.getObject(FORM_ASSIGNEES);
		if(field != null){
			UserPicker assignees = new UserPicker((BAFieldMultiValue) field);
			assignees.setName(FORM_ASSIGNEES);
			assignees.setFieldLabel(field.getDisplayName());
			assignees.setLabelStyle("font-weight:bold");
			this.add(assignees, formData);
			fields.put(FORM_ASSIGNEES, assignees);
		}
		
		field = fieldCache.getObject(FORM_ACCESS_TO);
		if(field != null){
			UserPicker access = new UserPicker((BAFieldMultiValue) field);
			access.setName(FORM_ACCESS_TO);
			access.setFieldLabel(field.getDisplayName());
			access.setLabelStyle("font-weight:bold");
			this.add(access, formData);
			fields.put(FORM_ACCESS_TO, access);
		}
		
		field = fieldCache.getObject(FORM_ATTACHMENTS);
		if(field != null){
			attContainer = new AttachmentFieldContainer(Mode.EDIT, ClientUtils.getSysPrefix(), 
					this.headerModel, (BAFieldAttachment) field);
			this.add(attContainer, formData);
		}
		

//		TimeField meetingEndTime = new TimeField();
//		meetingEndTime.setName(IFormConstants.FORM_END_TIME);
//		fields.put(IFormConstants.FORM_END_TIME, meetingEndTime);
		
		if (this.caption.equals(CAPTION_MEETING)){
			LabelField attendees = new LabelField();
			attendees.setFieldLabel("Attendees");
			attendees.setLabelStyle("font-weight:bold");
			this.add(attendees, formData);
	
			Button addAgency = new Button("Add Agency", new SelectionListener<ButtonEvent>() {
						@Override
						public void componentSelected(ButtonEvent ce) {
							Window.alert("Attendees will not be saved in the draft");
							addAgencyField(indexOf((Component) ce.getSource()));
							layout();
						}
					});
			this.add(addAgency);
	
			addAgencyField(this.indexOf(addAgency));
		}
	}
	
	@Override
	protected void afterRender() {
		super.afterRender();
		
		if(this.headerModel != null){
			this.fill();
		}
	}

	private void addAgencyField(int index) {
		TextField<String> agencies = new TextField<String>();
		agencies.setFieldLabel("Firm");
		agencies.setName("agency" + index);
		agencies.setEmptyText("Enter an Agency");
		agencies.setLabelStyle("font-weight:bold");
		this.insert(agencies, index, formData);
		fields.put("agency" + index, agencies);

		TextField<String> members = new TextField<String>();
		members.setFieldLabel("Members");
		members.setName("members" + index);
		members.setEmptyText("Enter members of the agency separated by a ','");
		members.setLabelStyle("font-weight:bold");
		this.insert(members, index + 1, formData);
		fields.put("members" + index, members);
	}

	private void fill() {
		TbitsTreeRequestData model = this.headerModel.clone();
		if (model != null) {
			if(this.caption.equals(CAPTION_MEETING)){
				this.headerModel.setRequestId(0);
				this.headerModel.setMaxActionId(0);
			}
			
			if (!model.getAsString(SUBJECT).equals("")) {
				String title = model.getAsString(SUBJECT);
				if (title.length() > 20)
					title = title.substring(0, 17) + "...";
				parentTab.setText(caption + " - " + title);
			}
//			if(!model.getAsString(IFormConstants.FORM_END_TIME).equals("")){
//				try{
//					String endTimeValue = model.getAsString(IFormConstants.FORM_END_TIME);
//					Date d = DateTimeFormat.getShortTimeFormat().parse(endTimeValue);
//					this.endTime = new Time(d);
//				}catch(Exception e){
//					Log.error("Error filling meeting header", e);
//				}
//			}
			for (String fieldName : fields.keySet()) {
				if (model.get(fieldName) != null) {
					try {
						if (fields.get(fieldName) instanceof TypeFieldControl) {
							((TypeFieldControl) fields.get(fieldName)).setStringValue(model.getAsString(fieldName));
						} else if (fields.get(fieldName) instanceof TimeField) {
							POJO obj = model.getAsPOJO(fieldName);
							if (obj != null && obj instanceof POJOString){
								String value = (String) obj.getValue();
								Date d = DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT).parse(value);
								((TimeField) fields.get(fieldName)).setDateValue(d);
							}
						} else if (fields.get(fieldName) instanceof DateField) {
							POJO obj = model.getAsPOJO(fieldName);
							if (obj != null && obj instanceof POJODate)
								((DateField) fields.get(fieldName)).setValue((Date) obj.getValue());
						} else if (fields.get(fieldName) instanceof UserPicker) {
							((UserPicker) fields.get(fieldName)).setStringValue(model.getAsString(fieldName));
						} else {
							fields.get(fieldName).setValue(model.getAsString(fieldName));
						}
					} catch (Exception ne) {
						ne.printStackTrace();
						Log.error("Error filling meeting header", ne);
					}
				}
			}
			
			POJO obj = model.getAsPOJO(IFixedFields.ATTACHMENTS);
			if (obj != null && obj instanceof POJOAttachment){
				List<FileClient> value = ((POJOAttachment)obj).getValue();
				attContainer.setFiles(value);
			}
		}
	}

	public TbitsTreeRequestData getValues() {
		TbitsTreeRequestData result = this.headerModel;
		if(result == null)
			result = new TbitsTreeRequestData();
		HashMap<Integer, String> agencies = new HashMap<Integer, String>();
		HashMap<Integer, String[]> members = new HashMap<Integer, String[]>();
		for (Field<?> f : fields.values()) {
			if (f.getName() == null)
				continue;
			if (f.getName().startsWith("agency")) {
				int index = Integer.parseInt(f.getName().split("agency")[1]);
				if (f.getValue() != null)
					agencies.put(index, (String) f.getValue());
			} else if (f.getName().startsWith("members")) {
				int index = Integer.parseInt(f.getName().split("members")[1]);
				if (f.getValue() != null)
					members.put(index, ((String) f.getValue()).split(","));
			} else {
				if (f instanceof DateField && f.getValue() != null) {
					POJODate o = new POJODate(((DateField) f).getValue());
					result.set(f.getName(), o);
				} else if (f instanceof TimeField && f.getValue() != null) {
					Date d = ((TimeField) f).getValue().getDate();
					if(d != null){
						String value = DateTimeFormat.getShortTimeFormat().format(d);
						POJOString o = new POJOString(value);
						result.set(f.getName(), o);
					}
				} else if (f instanceof TypeFieldControl && f.getValue() != null) {
					POJOString o = new POJOString(((TypeFieldControl) f).getStringValue());
					result.set(f.getName(), o);
				} else if (f instanceof UserPicker && ((UserPicker) f).getStringValue() != null) {
					POJOString o = new POJOString(((UserPicker) f).getStringValue());
					result.set(f.getName(), o);
				} else {
					if (f.getValue() != null) {
						POJOString o = new POJOString((String) f.getValue());
						result.set(f.getName(), o);
					}
				}
			}
		}
		
		List<FileClient> files = attContainer.getUploadProgressGrid().getFiles();
		if(files != null){
			POJOAttachment pojo = new POJOAttachment(files);
			result.set(IFixedFields.ATTACHMENTS, pojo);
		}
		
		String attendees = "";
		for (Integer i : agencies.keySet()) {
			if (members.get(i) != null) {
				for (String s : members.get(i))
					attendees += s + "[" + agencies.get(i) + "],";
			}
		}
		if (attendees.length() > 0)
			attendees = attendees.substring(0, attendees.length() - 1);
		POJOString o = new POJOString(attendees);
		result.set(IFormConstants.FORM_EXT_ATTENDEE, o);
//		if(this.endTime != null){
//			result.set(IFormConstants.FORM_END_TIME, new POJOString(DateTimeFormat.getShortTimeFormat().format(endTime.getDate())));
//		}
		return result;
	}

	public void setEndTime(Time time) {
//		this.endTime = time;
	}

	public void setHeaderModel(TbitsTreeRequestData headerModel) {
		this.headerModel = headerModel;
	}
}
