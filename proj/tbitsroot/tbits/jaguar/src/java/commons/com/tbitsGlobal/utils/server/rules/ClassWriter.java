package commons.com.tbitsGlobal.utils.server.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Scanner;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.customcompiler.CustomCompiler;

import commons.com.tbitsGlobal.utils.client.rules.ClassDef;
import commons.com.tbitsGlobal.utils.client.rules.FunctionDef;
import commons.com.tbitsGlobal.utils.client.rules.RuleDef;
import commons.com.tbitsGlobal.utils.client.rules.VarDef;

/**
 * This class writes the rules into java classes and compiles class files from the java files.
 * The java file and the class file are available as File objects.
 * The compilation results are stored as a string.
 * 
 * @author Karan Gupta
 *
 */
public class ClassWriter {
	
	//================================================================================

	public static final String RULES_PATH = Configuration.findPath("tmp")+"/rules/";
	
	private Writer out;
	private File javaFile;
	private File classFile;
	private String compilationResult;
	
	//================================================================================

	// Get methods
	
	public File getJavaFile(){
		return javaFile;
	}
	
	public File getClassFile(){
		return classFile;
	}
	
	/**
	 * @return the code of the java file created
	 */
	public String getJavaCode(){
		
		if(javaFile == null)
			return null;
		else{
			StringBuilder code = new StringBuilder();
		    Scanner scanner = null;
		    try {
		    	scanner = new Scanner(new FileInputStream(javaFile));
		    	while (scanner.hasNextLine()){
		    		code.append(scanner.nextLine() + System.getProperty("line.separator"));
		    	}
		    } 
		    catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		    finally{
		      scanner.close();
		    }
		    return code.toString();
		}
	}
	
	/**
	 * @return the classbytes of the class file created
	 * @throws IOException
	 */
	public byte[] getClassBytes() throws IOException{
		
		if(classFile == null)
			throw new IOException("Class file not found. The file might not be constructed. Call constructClassFile() first.");
		
		// Convert the class to an array of bytes
		InputStream is = new FileInputStream(classFile); 
		// Get the size of the file 
		long length = classFile.length(); 
		if (length > Integer.MAX_VALUE) { 
			throw new IOException("Class file is too large!"); 
		} 
		byte[] classbytes = new byte[(int)length]; 
		int offset = 0; 
		int numRead = 0; 
		while (offset < classbytes.length && (numRead=is.read(classbytes, offset, classbytes.length-offset)) >= 0) { 
			offset += numRead; 
		} 
		// Ensure all the bytes have been read in 
		if (offset < classbytes.length) { 
			throw new IOException("Could not completely read file " + classFile.getName()); 
		} 
		is.close(); 
		
		return classbytes;
	}

	//================================================================================

	/**
	 * Create a java file from the provided rule definition
	 * @param ruleDef
	 */
	public void constructJavaFile(RuleDef ruleDef) {
		
		ClassDef classDef = ruleDef.getClassDef();
		
		javaFile = new File(RULES_PATH + classDef.getName()+".java");
		File parentDir = new File(javaFile.getParent());
		parentDir.mkdirs();
		
		try {
			javaFile.createNewFile();
			out = new OutputStreamWriter(new FileOutputStream(javaFile));
			
			if(ruleDef.getRuleCode()  == null){
				startClass(classDef);
				for(FunctionDef fd : classDef.getFunctions()){
					writeFunction(fd);
				}
				endClass();
			}
			else{
				out.write(ruleDef.getRuleCode());
			}
			
			out.flush();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				out.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	//================================================================================

	/**
	 * Construct a class file from the java file
	 * @return true if the class was compiled and constructed successully. False otherwise.
	 * @throws IOException 
	 */
	public boolean constructClassFile() throws IOException {
		
		// Throw error if the javaFile is not constructed 
		if(javaFile == null)
			throw new IOException("Java file not found. The file might not be set. Call constructJavaFile(RuleDef) first.");
	    
		CustomCompiler compiler = new CustomCompiler();
		boolean success = compiler.compileFile(javaFile);
		compilationResult = compiler.getResult();

	    if(success){
	    	String classFileName = javaFile.getName().substring(0, javaFile.getName().indexOf(".java"));
	    	classFile = new File(RULES_PATH + classFileName + ".class");
	    	if(!classFile.exists()){
	    		classFile = null;
	    	}
	    }
	    return success;
	}

	//================================================================================

	/**
	 * @return the compilation diagnostic result
	 */
	public String getCompilerOutput() {
		
		return compilationResult;
	}

	//================================================================================

	/**
	 * Sanitise after the classwriter's work is done.
	 * !! This method needs to be called explicitly in order to remove the files made in the temp directory.
	 * TODO incorporate this function with the destructor
	 */
	public void sanitise() {
		if(javaFile != null && javaFile.exists())
			javaFile.delete();
		if(classFile != null && classFile.exists())
			classFile.delete();
	}

	//================================================================================
	
	// Utility Functions
	
	/**
	 * Write a function to the java file using the provided function definition
	 * @param fd
	 * @throws IOException
	 */
	private void writeFunction(FunctionDef fd) throws IOException {

		out.write(fd.modifiers + " ");
		out.write(fd.returnType + " ");
		out.write(fd.name);
		out.write("(");
		for(int i=0; i<fd.params.size(); i++){
			if(i>0){
				out.write(", ");
			}
			VarDef p = fd.params.get(i);
			out.write(p.varType + " " + p.varName);
		}
		out.write(") {\n");
		out.write(fd.code);
		out.write("\n}\n");
	}

	/**
	 * End a class definition
	 * @throws IOException
	 */
	private void endClass() throws IOException {
		out.write("\n}\n");
	}

	/**
	 * Start a class definition
	 * @param classDef
	 * @throws IOException
	 */
	private void startClass(ClassDef classDef) throws IOException {
		out.write("public class ");
		out.write(classDef.getName());
		out.write(" implements ");
		out.write(classDef.getImplementsClass());
		out.write(" {\n");
	}

	//================================================================================
	
}
