package transbit.tbits.common.customcompiler;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

public class CustomJavaFileManager implements JavaFileManager{

	private final ClassLoader classLoader;
	 private final StandardJavaFileManager standardFileManager;
	 private final PackageInternalsFinder finder;
	 
	 public CustomJavaFileManager(ClassLoader classLoader, StandardJavaFileManager standardFileManager) {
	  this.classLoader = classLoader;
	  this.standardFileManager = standardFileManager;
	  finder = new PackageInternalsFinder(classLoader);
	 }
	 
	 
	 public ClassLoader getClassLoader(Location location) {
	  return classLoader;
	 }
	 
	 
	 public String inferBinaryName(Location location, JavaFileObject file) {
	  if (file instanceof CustomJavaFileObject) {
	   return ((CustomJavaFileObject) file).binaryName();
	  } else { // if it's not CustomJavaFileObject, then it's coming from standard file manager - let it handle the file
	   return standardFileManager.inferBinaryName(location, file);
	  }
	 }
	 
	 
	 public boolean isSameFile(FileObject a, FileObject b) {
		 return standardFileManager.isSameFile(a, b);
	 }
	 
	 
	 public boolean handleOption(String current, Iterator<String> remaining) {
		 return standardFileManager.handleOption(current, remaining);
	 }
	 
	 
	 public boolean hasLocation(Location location) {
		// we don't care about source and other location types - not needed for compilation
	  return location == StandardLocation.CLASS_PATH || location == StandardLocation.PLATFORM_CLASS_PATH; 
	 }
	 
	 
	 public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
		 return standardFileManager.getJavaFileForInput(location, className, kind);
	 }
	 
	 
	 public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
		 return standardFileManager.getJavaFileForOutput(location, className, kind, sibling);
	 }
	 
	 
	 public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
		 return standardFileManager.getFileForInput(location, packageName, relativeName);
	 }
	 
	 
	 public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
		 return standardFileManager.getFileForOutput(location, packageName, relativeName, sibling);
	 }
	 
	 
	 public void flush() throws IOException {
		 standardFileManager.flush();
	 }
	 
	 
	 public void close() throws IOException {
		 standardFileManager.close();
	 }
	 
	 
	 public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
		 if (location == StandardLocation.PLATFORM_CLASS_PATH) { // let standard manager handle
			 return standardFileManager.list(location, packageName, kinds, recurse);
		 } 
		 else if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
			 if (packageName.startsWith("java")) { 
				 // a hack to let standard manager handle locations like "java.lang" or "java.util". Prob would make sense to join results of standard manager with those of my finder here
				 return standardFileManager.list(location, packageName, kinds, recurse);
			 } 
			 else { 
				 // app specific classes are here
				 return finder.find(packageName);
			 }
		 }
		 return Collections.emptyList();
	 
	 }
	 
	 
	 public int isSupportedOption(String option) {
		 return standardFileManager.isSupportedOption(option);
	 }

}
