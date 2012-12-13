package transbit.tbits.common.customcompiler;

import java.io.File;
import java.io.IOException;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class CustomCompiler {

	private String compilationResult;
	
	public String getResult(){
		return compilationResult;
	}
	
	public boolean compileFile(File javaFile){
		
		if(javaFile == null)
			return false;
		
		// get the instances of the compiler, diagnostics and the file manager
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	    final StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
	    final JavaFileManager fileManager = new CustomJavaFileManager(this.getClass().getClassLoader(), standardFileManager);
	    
	    // Open the javaFile using the file manager
	    Iterable<? extends JavaFileObject> compilationUnits = standardFileManager.getJavaFileObjects(javaFile);
	    
	    // Make a compilation task and call it
	    JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
	    boolean success = task.call();

	    // Save the compilation results
	    StringBuilder compilationDiagnostic = new StringBuilder();
	    for(Diagnostic<? extends JavaFileObject> diag : diagnostics.getDiagnostics()){
	    	compilationDiagnostic.append(diag.getCode()+"\n");
	    	compilationDiagnostic.append(diag.getKind()+"\n");
	    	compilationDiagnostic.append(diag.getMessage(null)+"\n\n");
	    }
	    compilationResult = compilationDiagnostic.toString();
	    
	    // Close the file manager.
	    try {
			fileManager.close();
		} 
	    catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}
	
}
