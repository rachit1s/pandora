package transbit.tbits.dql.antlr;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import transbit.tbits.dql.treecomponents.ParseResult;

public class Tester {

	public static void main(String[] args) throws RecognitionException {
		CharStream stream =
			new ANTLRStringStream(	"SELECT * \n" +
									"FROM tbits \n" +
									"WHERE (status_id:\"Acti ve\" OR request_id : NOT IN{1, 2, 3} AND ((assignee_id:karan.g)) AND some:234) \n" +
									"LIMIT 1, 50");
		DQLLexer lexer = new DQLLexer(stream);
		TokenStream tokenStream = new CommonTokenStream(lexer);
		DQLParser parser = new DQLParser(tokenStream);
		ParseResult result = parser.query().result;
		System.out.println("ok");
	}

}
