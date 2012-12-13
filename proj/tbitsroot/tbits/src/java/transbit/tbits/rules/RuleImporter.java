package transbit.tbits.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;

/**
 * This class imports the rules into the database. The main function can be invoked from
 * the command prompt with the file paths as the arguments. The importer does not alter the
 * java files provided.
 * 
 * @author Karan Gupta
 *
 */
public class RuleImporter {

	//================================================================================

	public static final String RULES_PATH = Configuration.findPath("tmp")+"/rules/";

	//================================================================================

	/**
	 * Run this method with the file paths of the rules' java files as arguments.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Create an instance of the rule importer
		RuleImporter importer = new RuleImporter();
		
		for(String filePath : args){
			
			if(!filePath.endsWith(".java")){
				System.out.println(filePath + " is not a java file. Pass the path of the .java files of the rules for import.");
				continue;
			}
			System.out.println("Importing : " + filePath);
			
			// Fetch the java file
			File javaFile = new File(filePath);
			if(!javaFile.exists()){
				System.out.println(filePath + " : File not present!");
				continue;
			}
			
			String fileName = javaFile.getName().substring(0, javaFile.getName().indexOf(".java"));
			
			// Create a temporary file in the temporary folder
			File tempJavaFile = new File(RULES_PATH + javaFile.getName());
			File parent = new File(tempJavaFile.getParent());
			if(!parent.exists())
				parent.mkdirs();
			if(tempJavaFile.exists()){
				System.out.println("Temporary file already exists : " + tempJavaFile.getAbsolutePath());
				continue;
			}
			// Add all the code to the temporary file except for the package information
			try {
				tempJavaFile.createNewFile();
				importer.prepareJavaFile(javaFile, tempJavaFile);
			}
			catch (IOException e1) {
				e1.printStackTrace();
				if(tempJavaFile.exists())
					tempJavaFile.delete();
				continue;
			}
			
			
			// Compile the javaFile
			File classFile = importer.compileJavaFile(tempJavaFile);
			if(classFile == null){
				System.out.println(filePath + " : Error compiling code!");
				if(tempJavaFile.exists())
					tempJavaFile.delete();
				continue;
			}
			
			// Deploy the rule
			try {
				if(!importer.deployRule(fileName, getClassBytes(classFile), getJavaCode(tempJavaFile))){
					System.out.println(filePath + " : Error deploying class!");
					if(classFile.exists())
						classFile.delete();
					if(tempJavaFile.exists())
						tempJavaFile.delete();
					continue;
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				System.out.println(filePath + " : Error getting classbytes!");
				if(classFile.exists())
					classFile.delete();
				if(tempJavaFile.exists())
					tempJavaFile.delete();
				continue;
			}
			
			// Sanitise and move to the next rule
			if(classFile.exists())
				classFile.delete();
			if(tempJavaFile.exists())
				tempJavaFile.delete();
			
			System.out.println(fileName + " : Successfully deployed rule.");
		}
		
		return;
	}
	
	//================================================================================

	/**
	 * Prepares a temporary java file for compilation. The package information from the original java file is removed.
	 * @param src
	 * @param dest
	 * @throws IOException 
	 */
	private void prepareJavaFile(File src, File dest) throws IOException {
		
		BufferedReader input = new BufferedReader(new FileReader(src));
		BufferedWriter output = new BufferedWriter(new FileWriter(dest));
		
		String line = null;
		while (( line = input.readLine()) != null){
        	
        	while(line.contains("package ")){
        		int from = line.indexOf("package ", 0);
        		int to = line.indexOf(";", from);
        		String head = line.substring(0, from);
        		String tail = line.substring(to, line.length());
        		line = head + tail;
        	}
        		
        	output.append(line);
        	output.append(System.getProperty("line.separator"));
        }

		
		input.close();
		output.close();
		
	}

	//================================================================================

	/**
	 * Compile the given java file and return the class file
	 * 
	 * @param javaFile
	 * @return
	 */
	public File compileJavaFile(File javaFile){
		
		if(javaFile == null)
			return null;
	    
		System.out.println("Compiling : " + javaFile.getName());
		
		// get the instances of the compiler, diagnostics and the file manager
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
	    
	    // Open the javaFile using the file manager
	    Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(javaFile);
	    
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
	    
	    if(!compilationDiagnostic.toString().trim().equals(""))
	    	System.out.println(compilationDiagnostic.toString());
	    
	    // Close the file manager.
	    try {
			fileManager.close();
		} 
	    catch (IOException e) {
			e.printStackTrace();
		}

	    // Class file to be returned
	    File classFile = null;
	    if(success){
	    	String classFileName = javaFile.getName().substring(0, javaFile.getName().indexOf(".java"));
	    	classFile = new File(javaFile.getParent() + File.separator + classFileName + ".class");
	    	if(!classFile.exists()){
	    		classFile = null;
	    	}
	    }
	    return classFile;
	}

	//================================================================================

	/**
	 * Deploy the rule. Save the code, class and the details in the database.
	 * Set a deault sequence number.
	 * 
	 * @param ruleDef
	 * @param classbytes
	 * @return true if the rule was deployed successfully. False otherwise.
	 */
	public boolean deployRule(String name, byte[] classbytes, String javaCode) {
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			String query = "select * from rules_definitions where name = ?";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				System.out.println(name + " : Rule with same name already exists!");
				return false;
			}
			rs.close();
			stmt.close();
			
			// Add rule to the ruleManager
			RuleClass rc_temp = new RuleClass(name);
			RulesManager.getInstance().putRule(rc_temp, classbytes);
			
			RulesClassLoader rcl = new RulesClassLoader();
			Class<?> c = rcl.loadClass(name, false);
			
			double sequenceNumber = -1.0;
			if(sequenceNumber < 0){
				query = "select max(seq_number) from rules_definitions";
				stmt = conn.prepareStatement(query);
				rs = stmt.executeQuery();
				if(rs.next())
					sequenceNumber = rs.getDouble(1);
				sequenceNumber++;
			}
			
			String type = "custom";
			Class<?>[] interfaces = c.getInterfaces();
			if(interfaces.length > 0){
				type = interfaces[0].getSimpleName();
			}
			
			System.out.println("Deploying rule : " + name + " : " + type + " : " + sequenceNumber);
			
			addRule(conn, name, type, sequenceNumber, classbytes, javaCode);
			
			conn.commit();
			
			// Add rule to the ruleManager
			RuleClass rc = new RuleClass(name, type, sequenceNumber);
			RulesManager.getInstance().putRule(rc, classbytes);
			return true;
		} 
		catch (Exception e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} 
			catch (SQLException e1) {
				e1.printStackTrace();
			}
			return false;
		}
		finally{
			if(conn != null)
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
		}

	}
	
	//================================================================================

	/**
	 * @return the code of the java file created
	 */
	public static String getJavaCode(File javaFile){
		
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
		    	if(scanner != null)
		    		scanner.close();
		    }
		    return code.toString();
		}
	}
	
	//================================================================================

	/**
	 * @return the classbytes of the class file created
	 * @throws IOException
	 */
	public static byte[] getClassBytes(File classFile) throws IOException{
		
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
	 * Add a new rule with specified details to the database
	 * @param name
	 * @param type
	 * @param sequenceNumber
	 * @param classbytes
	 * @param source
	 * @throws SQLException
	 */
	private void addRule(Connection conn, String name, String type, double sequenceNumber, byte[] classbytes, String source) throws SQLException {
			
		String query = "select max(id) from rules_definitions";
		int max_id = 0;
		PreparedStatement stmt = conn.prepareStatement(query);
		ResultSet rs = stmt.executeQuery();
		if(rs.next())
			max_id = rs.getInt(1);
		max_id++;
		rs.close();
		stmt.close();
		
		query = "insert into rules_definitions values (?,?,?,?)";
		stmt = conn.prepareStatement(query);
		stmt.setInt(1, max_id);
		stmt.setString(2, name);
		stmt.setString(3, type);
		stmt.setDouble(4, sequenceNumber);
		stmt.executeUpdate();
		stmt.close();

		query = "insert into rules_storage values (?,?,?,?)";
		stmt = conn.prepareStatement(query);
		stmt.setInt(1, max_id);
		stmt.setString(2, name);
		stmt.setString(3, source);
		stmt.setBytes(4, classbytes);
		stmt.executeUpdate();
		stmt.close();
			
	}
	
	//================================================================================

}
