/**
 * 
 */
package transbit.tbits.addons;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;


/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class AddonUtils 
{
	public static Logger logger = Logger.getLogger("transbit.tbits.addons"); 
	public static List<Config> parseConfigs(String configs) throws AddonException
	{
		List<Config> configList = new ArrayList<Config>();
		StringTokenizer lineTokenizer = new StringTokenizer(configs, "\n");
		for(int lineNumber = 1 ; lineTokenizer.hasMoreTokens(); lineNumber++ )
		{
			String line = lineTokenizer.nextToken();
			
			if( null == line || line.trim().equals("") || line.trim().startsWith("#"))
				continue; 
			
			line = line.trim();
			String[] parts = line.split(":");
			if(parts.length != 3 )
				throw new AddonException("Line#" + lineNumber + " was ill-formatted. It did not had 3 tokens. line = " + line);
			
			Double version = Double.parseDouble(parts[0].trim());
			String type = parts[1].trim().toLowerCase();
			String value = parts[2].trim();
			
			Config config = new Config(lineNumber, version, type, value);
			configList.add(config);
		}
		
		return configList;
	}
}
