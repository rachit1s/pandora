package transbit.tbits.dql.treecomponents;


/**
 * This class encapsulates the PARAMETER in DQL.
 * The PARAMETER is the lowest level in the grammar and comtains
 * a parameter of a particular type and the comparator for the search
 * 
 * @author Karan Gupta
 *
 */
public class Parameter implements DqlConstants {

	//====================================================================================

	public Comparator comp;
	public ParamType type;
	public String param;
	public boolean negateList = false;
	
	//====================================================================================
	
	public void setNegation(boolean negate) {
		if(negate){
			this.negateList = true;
			invertComparator();
		}
	}

	//====================================================================================

	/**
	 * @return Comparator information
	 */
	public String getComparator() {
		switch(comp){
		case E :
			return E_COMP;
		case NE : 
			return NE_COMP;
		case LE :
			return LE_COMP;
		case L :
			return L_COMP;
		case GE : 
			return GE_COMP;
		case G :
			return G_COMP;
		}
		return E_COMP;
	}

	//====================================================================================

	/**
	 * Invert the comparator. That is : <br>
	 * 	* E <-> NE<br>
	 * 	* LE <-> G<br>
	 * 	* GE <-> L<br>
	 */
	public void invertComparator() {
		switch(comp){
		case E :
			comp = Comparator.NE;
			break;
		case NE : 
			comp = Comparator.E;
			break;
		case LE :
			comp = Comparator.G;
			break;
		case L :
			comp = Comparator.GE;
			break;
		case GE : 
			comp = Comparator.L;
			break;
		case G :
			comp = Comparator.LE;
			break;
		}
	}
	
	//====================================================================================

	/**
	 * Determines the type and the value of the parameter and returns the information.
	 * 
	 * @param paramString
	 * @return
	 * @throws Exception
	 */
	public static Parameter determineTypeAndValue(String paramString) throws Exception {

		// Check if special
		Parameter toRet = SpecialParameters.getInstance().getSpecialParam(paramString);
		if(toRet != null)
			return toRet;
		
		toRet = new Parameter();
		// Check if the parameter is a string
		if(paramString.startsWith(STRING_OPEN)){
			toRet.type = ParamType.STRING;
			int strClose = paramString.indexOf(STRING_CLOSE, STRING_OPEN.length());
			if(strClose <=0 )
				throw new Exception("Invalid DQL!");
			toRet.param = paramString.substring(STRING_OPEN.length(), strClose);
		}
		// Check if the parameter is a boolean
		else if(paramString.equals(YES_BOOLEAN) || paramString.equals(TRUE_BOOLEAN)){
			toRet.type = ParamType.BOOLEAN;
			toRet.param = 1+"";
		}
		else if(paramString.equals(NO_BOOLEAN) || paramString.equals(FALSE_BOOLEAN)){
			toRet.type = ParamType.BOOLEAN;
			toRet.param = 0+"";
		}
		// Check if the parameter is a NULL parameter
		else if(paramString.equals(NULL)){
			toRet.type = ParamType.NULL;
			toRet.param = null;
		}
		// Check if the parameter is a datetime or numeric
		else{
			int dateDelim = paramString.indexOf(DATE_DELIM);
			if(dateDelim < 0){
				toRet.type = ParamType.NUMERIC;
				toRet.param = paramString;
			}
			// Return datetime value
			else{
				toRet.type = ParamType.DATETIME;
				toRet.param = getDateTimeValue(paramString);
			}
		}
		return toRet;
	}

	//====================================================================================

	/**
	 * Verify the datetime value specified by the paramString and return the formatted parameter value.
	 * 
	 * @param paramString
	 * @return
	 * @throws Exception
	 */
	public static String getDateTimeValue(String paramString) throws Exception {
		
		Parameter toRet = SpecialParameters.getInstance().getSpecialParam(paramString);
		if(toRet != null)
			return toRet.param;
	    
		return paramString;
	}

	//====================================================================================
	
}
