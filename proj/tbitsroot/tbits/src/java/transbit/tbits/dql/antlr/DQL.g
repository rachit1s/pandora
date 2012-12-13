grammar DQL;

options {
  language = Java;
  output=AST;
}

@header {
  package transbit.tbits.dql.antlr;
  import java.util.ArrayList;
  import transbit.tbits.dql.treecomponents.*;
  import transbit.tbits.dql.treecomponents.ParseResult.*;
  import transbit.tbits.dql.antlr.DQLErrorReporter;
}

@lexer::header {
  package transbit.tbits.dql.antlr;
}

@members {
	public DQLErrorReporter errReporter = new DQLErrorReporter();
	
   	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
    	String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        errReporter.addError(hdr + " " + msg);
    }
}

// Grammar
query returns [ParseResult result]
	: 				{$result = new ParseResult();}
		select 		{$result.setReqCols($select.result);}
		(from		{$result.setBAs($from.result);}							)? 
		(where		{$result.setConstraintRoot($where.result);}				)? 
		(has_text	{$result.setTextRoot($has_text.result);}				)? 
		(order_by	{$result.setOrdering($order_by.result);}				)? 
		(limit		{$result.setLimits($limit.pageNumber, $limit.pageSize);})?
		EOF
	;

select returns [ArrayList<String> result]
	:							{$result = new ArrayList<String>();}
		'SELECT' 
		(
		'*'						{$result.add(DqlConstants.REQUEST_COL);}
		|(
		fn1=fieldName 			{$result.add($fn1.result);} 
		(','  fn2=fieldName 	{$result.add($fn2.result);} )* )
		)
	;
	
from returns [ArrayList<String> result]
	:						{$result = new ArrayList<String>();}
		'FROM' ba1=baName 	{$result.add($ba1.result);} 
		(',' ba2=baName		{$result.add($ba2.result);} )*
	;
	
where returns [Expression result]
	:	'WHERE' expressionset		{$result=$expressionset.result;}
	;
	
has_text returns [Expression result]
	:	'HAS TEXT' expressionset	{$result=$expressionset.result;}
	;
	
order_by returns [ArrayList<Ordering> result]
	:								{$result = new ArrayList<Ordering>();}
		'ORDER BY' co1=column_order {$result.add($co1.result);}
		(',' co2=column_order		{$result.add($co2.result);})*
	;
	
limit returns [int pageNumber, int pageSize]
	:	'LIMIT' op1=INTEGER {$pageNumber=Integer.parseInt($op1.text);}
		',' op2=INTEGER		{$pageSize=Integer.parseInt($op2.text);}
	;
	
// For expressions
expression returns [Expression result]
	:									{$result = new Expression(); boolean negate = false;}
	(	constraintset					{$result.setConstraints($constraintset.result);}
	|	(NOT {negate = !negate;} )* 
		'(' expressionset ')'			{$result = $expressionset.result; $result.setNegation(negate);}
	)
	;
	
expressionset returns [Expression result]
	:							{$result = new Expression();}
		e1=expression 			{$result.addChild(null, e1.result);}
		(operator e2=expression	{$result.addChild($operator.result, e2.result);} )*
	;
	
// For constraints
constraint returns [Constraint result]
	:	
		fieldName 		{$result = new Constraint($fieldName.result);}
		':' valueset	{$result.setValues($valueset.result);}
	;
	
constraintset returns [ArrayList<Constraint> result]
	:							{$result = new ArrayList<Constraint>();}
		c1=constraint			{$result.add(c1.result);}
		(operator c2=constraint	{Constraint c = c2.result; c.setOperator($operator.result); $result.add(c);})*
	;
	
// For values
value returns [Value result]
	:									{$result = new Value(); boolean negate = false;}
		(NOT {negate = !negate;} )*
		( parameter 					{$result.addParam($parameter.result);}
		| 'IN' '{' parameterset '}'		{$result.setParams($parameterset.result);})
										{$result.setNegation(negate);}
	;

valueset returns [ArrayList<Value> result]
	:						{$result = new ArrayList<Value>();}
	(	v0=value 			{$result.add(v0.result);}
	|	'(' v1=value 		{$result.add(v1.result);}
		(operator v2=value	{Value v = v2.result; v.setOperator($operator.result); $result.add(v);})* ')'
	)
	;
	
// For parameters
parameter returns [Parameter result]
	:					{$result = new Parameter(); DqlConstants.Comparator comparator = DqlConstants.Comparator.E;}
		(comparator		{comparator = $comparator.result;})? {$result.comp = comparator;}
		(NULL 			{$result.type = DqlConstants.ParamType.NULL; $result.param = $NULL.text;}
		|STRING_LITERAL {$result.type = DqlConstants.ParamType.STRING; $result.param = $STRING_LITERAL.text;}
		|INTEGER 		{$result.type = DqlConstants.ParamType.NUMERIC; $result.param = $INTEGER.text;}
		|REAL			{$result.type = DqlConstants.ParamType.NUMERIC; $result.param = $REAL.text;}
		|BOOLEAN		{$result.type = DqlConstants.ParamType.BOOLEAN; $result.param = $BOOLEAN.text;}
		|DATETIME		{$result.type = DqlConstants.ParamType.DATETIME; $result.param = $DATETIME.text;}
		|LITERAL		{$result.type = DqlConstants.ParamType.UNKNOWN; $result.param = $LITERAL.text;}
		)
	;
	
parameterset returns [ArrayList<Parameter> result]
	:						{$result = new ArrayList<Parameter>();}
		p1=parameter 		{$result.add(p1.result);}
		(',' p2=parameter	{$result.add(p2.result);})*
	;
	
// Utility
column_order returns [Ordering result]
	: 	fieldName ORDER		{$result = new Ordering($ORDER.text, $fieldName.result);}
	;
operator returns [DqlConstants.Operator result]
	: 	'AND'	{$result = DqlConstants.Operator.AND;} 
	| 	'OR' 	{$result = DqlConstants.Operator.OR;}
	| 	'and' 	{$result = DqlConstants.Operator.AND;}
	| 	'or'	{$result = DqlConstants.Operator.OR;}
	;
comparator returns [DqlConstants.Comparator result]
	: 	'=' 	{$result = DqlConstants.Comparator.E;}
	| 	'<>' 	{$result = DqlConstants.Comparator.NE;}
	| 	'<' 	{$result = DqlConstants.Comparator.L;}
	| 	'<=' 	{$result = DqlConstants.Comparator.LE;}
	| 	'>' 	{$result = DqlConstants.Comparator.G;}
	| 	'>='	{$result = DqlConstants.Comparator.GE;}
	;
baName returns [String result]
	: 	
		LITERAL	{$result = $LITERAL.text;}
	|	STRING_LITERAL {$result = $STRING_LITERAL.text;}
	;
fieldName returns [String result]
	: 	
		LITERAL	{$result = $LITERAL.text;}
	|	STRING_LITERAL {$result = $STRING_LITERAL.text;}
	;

// Tokens

fragment DIGIT : '0'..'9';
fragment LETTER : 'a'..'z' | 'A'..'Z';

ORDER : 'ASC' | 'DESC';
NULL : 'NULL';
NOT	: 'NOT';
BOOLEAN : (('yes'|'true') {setText("1");}) | (('no'|'false') {setText("0");});
DATETIME : DIGIT (DIGIT)? '/' DIGIT (DIGIT)? '/' DIGIT DIGIT DIGIT DIGIT (('+'|'-') DIGIT+ ('m'|'h'|'d'|'M'|'y'))*;

INTEGER : DIGIT+;
REAL	: DIGIT* '.' DIGIT+;
LITERAL : (LETTER | DIGIT | '_' | '%' | '.' | '*' | '\'' | '+' | '-' | '?')+ ;

ESCAPE : 					{String str = "";}
			'\\' 
			('\\\\'			{str += "\\";}	)*		
							{setText(str);}
		;
STRING_LITERAL:										{StringBuilder b = new StringBuilder();}
		'"'( 							
		ESCAPE '"'									{b.append($ESCAPE.text); b.appendCodePoint('"');}
		| c = ~('\\' | '"' | '\n' | '\f' | '\r')	{b.appendCodePoint(c);}
		)*'"'										{setText(b.toString());}
	;
	
WS_OTHERS : (' ' | '\t' | '\n' | '\f' | '\r')+ {$channel=HIDDEN;};