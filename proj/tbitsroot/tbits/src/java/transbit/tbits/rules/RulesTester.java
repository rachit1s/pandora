package transbit.tbits.rules;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import transbit.tbits.api.IRule;

public class RulesTester {

	public static void main(String arg[]){
		
		ArrayList<Class<?>> preRules = RulesManager.getInstance().getRulesImplementing(IRule.class);
		for(Class<?> rule : preRules){
			System.out.println(rule.getCanonicalName());
			try {
				Method m = rule.getMethod("getSequence", null);
				if(m != null){
					System.out.println(m.invoke(rule.newInstance(), null));
				}
//				System.out.println();
			}
			catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
