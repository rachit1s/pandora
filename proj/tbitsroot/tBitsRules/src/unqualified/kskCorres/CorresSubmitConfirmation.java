package kskCorres;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.IAddRequestFooterSlotFiller;
import transbit.tbits.ExtUI.ISubRequestFooterSlotFiller;
import transbit.tbits.ExtUI.IUpdateRequestFooterSlotFiller;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class CorresSubmitConfirmation implements IAddRequestFooterSlotFiller,
		ISubRequestFooterSlotFiller, IUpdateRequestFooterSlotFiller 
{
	public static final String myJS = "<script type='text/javascript'>" +
	"\nfunction PreSubmitHandler(name, customSubmitEvent) {\n" +
	"    this.name = name; // name of this instance of the handler \n" +
	"    this.myCustomSubmitEvent = customSubmitEvent; // keep the event registered with your self\n" +
	"    this.myCustomSubmitEvent."+CUSTOM_PRE_SUBMIT_EVENT_PROP+".subscribe(this.onPreSubmit, this); // subscribe to the event with a method\n" +
	"}\n" +
	"\n" +
	"PreSubmitHandler.prototype.onPreSubmit = function(typeOfEvent,args,me)  // create the handler method registered above\n" +
	"{	\n" +
	"	var result = args[0];	// args : the array of arguments passed during the event fire. Here it is an object of type PreSubmitResult\n" +
	"				// which has two properties. 1. canContinue : one should only set it false if the submit should be canceled.\n" +
	"				// else leave it as it is so that other plugins can set it to false ( its default initial value is true. )\n" +
	"				// 2. message : this is any message you want to show to the user after all handlers have executed and atleast\n" +
	"				// one of them set the canContinue to false.\n" +
	"\n" +
	"	var ok = confirm('Do you really want to Submit the request ?');\n" +
	"	/*\n" +
	"	Use only confirm, alert or prompt methods of javascript for any user-interaction as these methods stops the execution till the user\n" +
	"	responds. Or, you will have to use your own method which will not return till user responds. Otherwise the onSubmit will resume and submit\n" +
	"	and not wait for user's action.\n" +
	"	*/\n" +
	"\n" +
	"	if(!ok)\n" +
	"	{\n" +
	"		result."+ PRE_SUBMIT_RESULT_CAN_CONTINUE_PROP +" = false ; // set canContinue to false. You can use constants defined in ISlotFiller\n" +
	"	}\n" +
	"}\n" +
	"var psh = new PreSubmitHandler('ConfirmationPopUp',"+ CUSTOM_SUBMIT_EVENT_VAR +" ); // this also subscribes with the event\n" +
	"</script>" ;
	
	public String getAddRequestFooterHtml(HttpServletRequest httpRequest,
		HttpServletResponse httpResponse, BusinessArea ba, User user) 
	{		
		return process( httpRequest, httpResponse,  ba,  user);
	}
	
	private String process(HttpServletRequest httpRequest,
		HttpServletResponse httpResponse, BusinessArea ba, User user) 
	{		
		if(ba != null && ba.getSystemPrefix() != null &&  ba.getSystemPrefix().equalsIgnoreCase(KskConstants.CORR_SYSPREFIX) )
			return myJS ;
		else
			return "" ;
	}
	
	public double getAddRequestFooterSlotFillerOrder() 
	{		
		return 0;
	}
	
	public String getSubRequestFooterHtml(HttpServletRequest httpRequest,
		HttpServletResponse httpResponse, BusinessArea ba,
		Request parentRequest, User user) 
	{		
		return process( httpRequest, httpResponse,  ba,  user);
	}
	
	public double getSubRequestFooterOrder() 
	{		
		return 0;
	}
	
	public String getUpdateRequestFooterHtml(HttpServletRequest httpRequest,
		HttpServletResponse httpResponse, BusinessArea ba,
		Request oldRequest, User user) 
	{		
		return process( httpRequest, httpResponse,  ba,  user);
	}
	
	public double getUpdateRequestFooterSlotFillerOrder() 
	{	
		return 0;
	}
}
