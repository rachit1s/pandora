package transbit.tbits.rules;

/**
 * Custom classloader for the java rules.
 * 
 * @author karan
 *
 */
public class RulesClassLoader extends ClassLoader{

	//================================================================================

	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		
		try{
			Class<?> c = null;
			// Since we do not wish to use the cache of the classloader, try loading the class from the rules manager
	        byte[] classbytes = RulesManager.getInstance().getClassBytes(name);
        	if(classbytes != null){
        		// Now call an inherited method to convert those bytes into a Class
        		c = defineClass(name, classbytes, 0, classbytes.length);
        	}
        	
        	// In case the rules manager does not return a class, check the classloader cache
        	// Our ClassLoader superclass has a built-in cache of classes it has
	        // already loaded.
	        if(c == null){
        		c = findLoadedClass(name);
        	}
	        
	        // After this method loads a class, it will be called again to
	        // load the superclasses. Since these may be system classes, we've
	        // got to be able to load those too. So try to load the class as
	        // a system class (i.e. from the CLASSPATH) and ignore any errors
	        if (c == null) {
	          try { c = findSystemClass(name); }
	          catch (Exception ex) {}
	        }
	        
	        if(c == null)
	        {
	        	try
	        	{
	        		c = this.getClass().getClassLoader().loadClass(name);
	        	}
	        	catch (Exception e) {
					// TODO: handle exception
				}
	        }
	        
	        // If the class wasn't found by any of the above attempts, then
	        // throw exception
	        if (c == null) {
	        	throw new ClassNotFoundException();
	        }

	        // If the resolve argument is true, call the inherited resolveClass method.
	        if (resolve) resolveClass(c);

	        // And we're done. Return the Class object we've loaded.
	        return c;
		}
		// If anything goes wrong, throw a ClassNotFoundException error
		catch (Exception ex) { 
			throw new ClassNotFoundException(ex.toString()); 
		}
	}
	
	//================================================================================

}
