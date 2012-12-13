package transbit.tbits.autovue.connector;

import javax.swing.JRootPane;

import com.cimmetry.jvue.JVue;
/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * Customized Entry Point for AutoVue
 */
public class JvueTbits extends JVue {

	public void setFile(String arg0) {
		super.setFile(arg0);
		super.openMarkup("*");
	}

}
