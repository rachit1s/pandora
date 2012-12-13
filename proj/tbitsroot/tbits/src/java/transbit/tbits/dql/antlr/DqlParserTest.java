package transbit.tbits.dql.antlr;

import junit.framework.TestCase;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import transbit.tbits.dql.treecomponents.Constraint;
import transbit.tbits.dql.treecomponents.Expression;
import transbit.tbits.dql.treecomponents.ParseResult;
import transbit.tbits.dql.treecomponents.Value;
import transbit.tbits.dql.treecomponents.DqlConstants.Operator;

public class DqlParserTest extends TestCase{
	
	private ParseResult parse(String dql) throws Exception{
		CharStream stream = new ANTLRStringStream(dql);
		DQLLexer lexer = new DQLLexer(stream);
		TokenStream tokenStream = new CommonTokenStream(lexer);
		DQLParser parser = new DQLParser(tokenStream);
		try{
			ParseResult pr = parser.query().result;
			parser.errReporter.reportErrors();
			return pr;
		}
		catch(RecognitionException e){
			throw new Exception(e);
		}
	}
	
	/**
	 * Single expression and single constraints
	 * @throws Exception
	 */
	public void testSimple() throws Exception
	{
		ParseResult pr = parse("SELECT * FROM tbits WHERE request_id:1");
		Expression exp = pr.getConstraintRoot().getChildren().get(0);
		assertEquals("request_id", exp.getConstraints().get(0).getField());
		assertNull(exp.getConstraints().get(0).getValues().get(0).getOperator());
		assertEquals("1", exp.getConstraints().get(0).getValues().get(0).getParams().get(0).param);
	}

	public void testColumnName() throws Exception
	{
		String query = "SELECT x, \"y\" FROM tbits";
		ParseResult pr = parse(query);
		assertEquals("x", pr.getReqCols().get(0));
		assertEquals("y", pr.getReqCols().get(1));
	}
	/**
	 * Two expressions with single constraints each
	 * @throws Exception
	 */
	public void testParseComplex() throws Exception
	{
		ParseResult pr = parse("SELECT * FROM tbits WHERE request_id:1 AND request_id:2");
		Expression exp = pr.getConstraintRoot().getChildren().get(0);
		
		Constraint constraint1 = exp.getConstraints().get(0);
		
		assertEquals("request_id", constraint1.getField());
		assertNull(constraint1.getValues().get(0).getOperator());
		assertEquals("1", constraint1.getValues().get(0).getParams().get(0).param);
		
		Constraint constraint2 = exp.getConstraints().get(1);
		assertEquals("request_id", constraint2.getField());
		assertNull(constraint2.getValues().get(0).getOperator());
		assertEquals("2", constraint2.getValues().get(0).getParams().get(0).param);
		//TODO: check the second param too
	}
	
	/**
	 * Single Expression with two constraints
	 * @throws Exception
	 */
	public void testParseComplex1() throws Exception
	{
		ParseResult pr = parse("SELECT * FROM tbits WHERE request_id:(1 AND 2)");
		Expression exp = pr.getConstraintRoot().getChildren().get(0);
		assertEquals("request_id", exp.getConstraints().get(0).getField());
		assertNull(exp.getConstraints().get(0).getValues().get(0).getOperator());
		Value val1 =  exp.getConstraints().get(0).getValues().get(0);
		assertNull(val1.getOperator());
		assertEquals("1", val1.getParams().get(0).param);
		
		Value val2 =  exp.getConstraints().get(0).getValues().get(1);
		assertEquals(Operator.AND, val2.getOperator());
		assertEquals("2", val2.getParams().get(0).param);
	}
	

	/**
	 * Tests the complex expression 
	 * @throws Exception
	 */
	public void testParseComplex2() throws Exception
	{
		ParseResult pr = parse("SELECT * FROM tbits WHERE request_id:(1 AND IN {2,3})");
		Expression exp = pr.getConstraintRoot().getChildren().get(0);
		assertEquals("request_id", exp.getConstraints().get(0).getField());
		assertNull(exp.getConstraints().get(0).getValues().get(0).getOperator());
		Value val1 =  exp.getConstraints().get(0).getValues().get(0);
		assertNull(val1.getOperator());
		assertEquals("1", val1.getParams().get(0).param);
		
		Value val2 =  exp.getConstraints().get(0).getValues().get(1);
		assertEquals(Operator.AND, val2.getOperator());

		//TODO: how I check if "IN" has been parsed correctly??
		assertEquals("2", val2.getParams().get(0).param);
		assertEquals("3", val2.getParams().get(1).param);
		
	}

	/**
	 * This just runs the parser over a set of queries. 
	 * No exception should be thrown while parsing
	 * @throws Exception
	 */
	public void testParseForErrors()
	{
		String[] queries = {
				"SELECT * FROM tbits", 
				"SELECT * FROM tbits WHERE request_id:(IN {1,2})",
				"SELECT x,y FROM tbits",
				"SELECT x, y FROM tbits",
				"SELECT x, \"y\" FROM tbits",
				"SELECT x,	 	y FROM tbits", //tab
				"SELECT x,	 	\"z y\" FROM tbits", 
				"SELECT  *   FROM    tbits1", //spaces
				"SELECT 	*	 FROM	 	tbits", //tabs 
				"SELECT * FROM tbits WHERE request_id:1",
				"SELECT * FROM tbits WHERE request_id: 1",
				"SELECT * FROM tbits WHERE request_id : 1",
				"SELECT * FROM tbits WHERE				      request_id		:			1	", 
				"SELECT * FROM tbits WHERE				      request_id		:			1	\n", 
				"SELECT * FROM tbits WHERE (request_id:1)",
				"SELECT * FROM tbits WHERE (request_id:1) AND (request_id:2)",
				"SELECT * FROM tbits WHERE (request_id:1) OR (request_id:2)",
				"SELECT * FROM tbits WHERE request_id:1 OR request_id:2",
				"SELECT * FROM tbits WHERE request_id:1 AND request_id:2",
				"SELECT * FROM tbits WHERE request_id:\"1 2 3 3 4\" AND request_id:2",
				"SELECT * FROM tbits WHERE \"request_id\":1 AND request_id:2",
				"SELECT * FROM \"WHERE\" WHERE request_id:1 AND request_id:2",
				"SELECT * FROM \"tbits\" WHERE request_id:1 AND request_id:2",
				"SELECT * FROM \"sa sa sa sa sa sa sa\" WHERE request_id:1 AND request_id:2",
				"SELECT * FROM \"sa sa sa sa sa sa sa AND WHERE \" WHERE request_id:1 AND request_id:2",
				
		};
		for(String q:queries)
		{
			boolean caughtException = false;
			caughtException = false;
			
			try {
				ParseResult pr = parse(q);
			} catch (Exception e) {
				caughtException = true;
				e.printStackTrace();
			}
			assertFalse("Parsing of following valid query did not succeeded: " + q, caughtException);
		}
	}
	
	/**
	 * This is kind of negative testCases.
	 * This parse a set of wrong queries and see if an exception is thrown. 
	 * If an exception is not thrown, it is an error otherwise it is a success.
	 */
	public void testForWrongQueries()
	{
		String[] queries = {"SELECT", 
				"SELECT * FROM tbits WH1ERE (request_id:1)",
				"SELECT * FROM tbits where (request_id:1) and (request_id:2)",
				"SELECT * from tbits WHERE (request_id:1) OR (request_id:2)",
				"* SELECT * FROM tbits WHERE request_id:1 OR request_id:2",
				"SELECT SELECT * FROM tbits WHERE request_id:1 AND request_id:2",
				"SELECT * from tbits WHERE (request_id:1) OR (request_id:2"
		};
		for(String q:queries)
		{
			boolean caughtException = false;
			caughtException = false;
			try {
				ParseResult pr = parse(q);
			} catch (Exception e) {
				caughtException = true;
				System.out.println("Error while executing dql: " + q);
				//e.printStackTrace();
			}
			assertTrue("Excution of the following query should have thrown error: " + q, caughtException);
			caughtException = false;
		}
		
	}
}
