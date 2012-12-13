// $ANTLR 3.3 Nov 30, 2010 12:50:56 /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g 2011-10-18 11:10:30

  package transbit.tbits.dql.antlr;
  import java.util.ArrayList;
  import transbit.tbits.dql.treecomponents.*;
  import transbit.tbits.dql.treecomponents.ParseResult.*;
  import transbit.tbits.dql.antlr.DQLErrorReporter;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class DQLParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "INTEGER", "NOT", "NULL", "STRING_LITERAL", "REAL", "BOOLEAN", "DATETIME", "LITERAL", "ORDER", "DIGIT", "LETTER", "ESCAPE", "WS_OTHERS", "'SELECT'", "'*'", "','", "'FROM'", "'WHERE'", "'HAS TEXT'", "'ORDER BY'", "'LIMIT'", "'('", "')'", "':'", "'IN'", "'{'", "'}'", "'AND'", "'OR'", "'and'", "'or'", "'='", "'<>'", "'<'", "'<='", "'>'", "'>='"
    };
    public static final int EOF=-1;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__19=19;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int T__29=29;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int T__40=40;
    public static final int INTEGER=4;
    public static final int NOT=5;
    public static final int NULL=6;
    public static final int STRING_LITERAL=7;
    public static final int REAL=8;
    public static final int BOOLEAN=9;
    public static final int DATETIME=10;
    public static final int LITERAL=11;
    public static final int ORDER=12;
    public static final int DIGIT=13;
    public static final int LETTER=14;
    public static final int ESCAPE=15;
    public static final int WS_OTHERS=16;

    // delegates
    // delegators


        public DQLParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public DQLParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return DQLParser.tokenNames; }
    public String getGrammarFileName() { return "/home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g"; }


    	public DQLErrorReporter errReporter = new DQLErrorReporter();
    	
       	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        	String hdr = getErrorHeader(e);
            String msg = getErrorMessage(e, tokenNames);
            errReporter.addError(hdr + " " + msg);
        }


    public static class query_return extends ParserRuleReturnScope {
        public ParseResult result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "query"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:31:1: query returns [ParseResult result] : select ( from )? ( where )? ( has_text )? ( order_by )? ( limit )? EOF ;
    public final DQLParser.query_return query() throws RecognitionException {
        DQLParser.query_return retval = new DQLParser.query_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EOF7=null;
        DQLParser.select_return select1 = null;

        DQLParser.from_return from2 = null;

        DQLParser.where_return where3 = null;

        DQLParser.has_text_return has_text4 = null;

        DQLParser.order_by_return order_by5 = null;

        DQLParser.limit_return limit6 = null;


        Object EOF7_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:32:2: ( select ( from )? ( where )? ( has_text )? ( order_by )? ( limit )? EOF )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:32:8: select ( from )? ( where )? ( has_text )? ( order_by )? ( limit )? EOF
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new ParseResult();
            pushFollow(FOLLOW_select_in_query66);
            select1=select();

            state._fsp--;

            adaptor.addChild(root_0, select1.getTree());
            retval.result.setReqCols((select1!=null?select1.result:null));
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:34:3: ( from )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==20) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:34:4: from
                    {
                    pushFollow(FOLLOW_from_in_query75);
                    from2=from();

                    state._fsp--;

                    adaptor.addChild(root_0, from2.getTree());
                    retval.result.setBAs((from2!=null?from2.result:null));

                    }
                    break;

            }

            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:35:3: ( where )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==21) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:35:4: where
                    {
                    pushFollow(FOLLOW_where_in_query93);
                    where3=where();

                    state._fsp--;

                    adaptor.addChild(root_0, where3.getTree());
                    retval.result.setConstraintRoot((where3!=null?where3.result:null));

                    }
                    break;

            }

            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:36:3: ( has_text )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==22) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:36:4: has_text
                    {
                    pushFollow(FOLLOW_has_text_in_query108);
                    has_text4=has_text();

                    state._fsp--;

                    adaptor.addChild(root_0, has_text4.getTree());
                    retval.result.setTextRoot((has_text4!=null?has_text4.result:null));

                    }
                    break;

            }

            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:37:3: ( order_by )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==23) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:37:4: order_by
                    {
                    pushFollow(FOLLOW_order_by_in_query122);
                    order_by5=order_by();

                    state._fsp--;

                    adaptor.addChild(root_0, order_by5.getTree());
                    retval.result.setOrdering((order_by5!=null?order_by5.result:null));

                    }
                    break;

            }

            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:38:3: ( limit )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==24) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:38:4: limit
                    {
                    pushFollow(FOLLOW_limit_in_query136);
                    limit6=limit();

                    state._fsp--;

                    adaptor.addChild(root_0, limit6.getTree());
                    retval.result.setLimits((limit6!=null?limit6.pageNumber:0), (limit6!=null?limit6.pageSize:0));

                    }
                    break;

            }

            EOF7=(Token)match(input,EOF,FOLLOW_EOF_in_query145); 
            EOF7_tree = (Object)adaptor.create(EOF7);
            adaptor.addChild(root_0, EOF7_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "query"

    public static class select_return extends ParserRuleReturnScope {
        public ArrayList<String> result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "select"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:42:1: select returns [ArrayList<String> result] : 'SELECT' ( '*' | (fn1= fieldName ( ',' fn2= fieldName )* ) ) ;
    public final DQLParser.select_return select() throws RecognitionException {
        DQLParser.select_return retval = new DQLParser.select_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal8=null;
        Token char_literal9=null;
        Token char_literal10=null;
        DQLParser.fieldName_return fn1 = null;

        DQLParser.fieldName_return fn2 = null;


        Object string_literal8_tree=null;
        Object char_literal9_tree=null;
        Object char_literal10_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:43:2: ( 'SELECT' ( '*' | (fn1= fieldName ( ',' fn2= fieldName )* ) ) )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:43:10: 'SELECT' ( '*' | (fn1= fieldName ( ',' fn2= fieldName )* ) )
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new ArrayList<String>();
            string_literal8=(Token)match(input,17,FOLLOW_17_in_select170); 
            string_literal8_tree = (Object)adaptor.create(string_literal8);
            adaptor.addChild(root_0, string_literal8_tree);

            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:45:3: ( '*' | (fn1= fieldName ( ',' fn2= fieldName )* ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==18) ) {
                alt7=1;
            }
            else if ( (LA7_0==STRING_LITERAL||LA7_0==LITERAL) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:46:3: '*'
                    {
                    char_literal9=(Token)match(input,18,FOLLOW_18_in_select179); 
                    char_literal9_tree = (Object)adaptor.create(char_literal9);
                    adaptor.addChild(root_0, char_literal9_tree);

                    retval.result.add(DqlConstants.REQUEST_COL);

                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:47:4: (fn1= fieldName ( ',' fn2= fieldName )* )
                    {
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:47:4: (fn1= fieldName ( ',' fn2= fieldName )* )
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:48:3: fn1= fieldName ( ',' fn2= fieldName )*
                    {
                    pushFollow(FOLLOW_fieldName_in_select197);
                    fn1=fieldName();

                    state._fsp--;

                    adaptor.addChild(root_0, fn1.getTree());
                    retval.result.add((fn1!=null?fn1.result:null));
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:49:3: ( ',' fn2= fieldName )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==19) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:49:4: ',' fn2= fieldName
                    	    {
                    	    char_literal10=(Token)match(input,19,FOLLOW_19_in_select208); 
                    	    char_literal10_tree = (Object)adaptor.create(char_literal10);
                    	    adaptor.addChild(root_0, char_literal10_tree);

                    	    pushFollow(FOLLOW_fieldName_in_select213);
                    	    fn2=fieldName();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, fn2.getTree());
                    	    retval.result.add((fn2!=null?fn2.result:null));

                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);


                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "select"

    public static class from_return extends ParserRuleReturnScope {
        public ArrayList<String> result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "from"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:53:1: from returns [ArrayList<String> result] : 'FROM' ba1= baName ( ',' ba2= baName )* ;
    public final DQLParser.from_return from() throws RecognitionException {
        DQLParser.from_return retval = new DQLParser.from_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal11=null;
        Token char_literal12=null;
        DQLParser.baName_return ba1 = null;

        DQLParser.baName_return ba2 = null;


        Object string_literal11_tree=null;
        Object char_literal12_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:54:2: ( 'FROM' ba1= baName ( ',' ba2= baName )* )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:54:9: 'FROM' ba1= baName ( ',' ba2= baName )*
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new ArrayList<String>();
            string_literal11=(Token)match(input,20,FOLLOW_20_in_from250); 
            string_literal11_tree = (Object)adaptor.create(string_literal11);
            adaptor.addChild(root_0, string_literal11_tree);

            pushFollow(FOLLOW_baName_in_from254);
            ba1=baName();

            state._fsp--;

            adaptor.addChild(root_0, ba1.getTree());
            retval.result.add((ba1!=null?ba1.result:null));
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:56:3: ( ',' ba2= baName )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==19) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:56:4: ',' ba2= baName
            	    {
            	    char_literal12=(Token)match(input,19,FOLLOW_19_in_from263); 
            	    char_literal12_tree = (Object)adaptor.create(char_literal12);
            	    adaptor.addChild(root_0, char_literal12_tree);

            	    pushFollow(FOLLOW_baName_in_from267);
            	    ba2=baName();

            	    state._fsp--;

            	    adaptor.addChild(root_0, ba2.getTree());
            	    retval.result.add((ba2!=null?ba2.result:null));

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "from"

    public static class where_return extends ParserRuleReturnScope {
        public Expression result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "where"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:59:1: where returns [Expression result] : 'WHERE' expressionset ;
    public final DQLParser.where_return where() throws RecognitionException {
        DQLParser.where_return retval = new DQLParser.where_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal13=null;
        DQLParser.expressionset_return expressionset14 = null;


        Object string_literal13_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:60:2: ( 'WHERE' expressionset )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:60:4: 'WHERE' expressionset
            {
            root_0 = (Object)adaptor.nil();

            string_literal13=(Token)match(input,21,FOLLOW_21_in_where289); 
            string_literal13_tree = (Object)adaptor.create(string_literal13);
            adaptor.addChild(root_0, string_literal13_tree);

            pushFollow(FOLLOW_expressionset_in_where291);
            expressionset14=expressionset();

            state._fsp--;

            adaptor.addChild(root_0, expressionset14.getTree());
            retval.result =(expressionset14!=null?expressionset14.result:null);

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "where"

    public static class has_text_return extends ParserRuleReturnScope {
        public Expression result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "has_text"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:63:1: has_text returns [Expression result] : 'HAS TEXT' expressionset ;
    public final DQLParser.has_text_return has_text() throws RecognitionException {
        DQLParser.has_text_return retval = new DQLParser.has_text_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal15=null;
        DQLParser.expressionset_return expressionset16 = null;


        Object string_literal15_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:64:2: ( 'HAS TEXT' expressionset )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:64:4: 'HAS TEXT' expressionset
            {
            root_0 = (Object)adaptor.nil();

            string_literal15=(Token)match(input,22,FOLLOW_22_in_has_text310); 
            string_literal15_tree = (Object)adaptor.create(string_literal15);
            adaptor.addChild(root_0, string_literal15_tree);

            pushFollow(FOLLOW_expressionset_in_has_text312);
            expressionset16=expressionset();

            state._fsp--;

            adaptor.addChild(root_0, expressionset16.getTree());
            retval.result =(expressionset16!=null?expressionset16.result:null);

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "has_text"

    public static class order_by_return extends ParserRuleReturnScope {
        public ArrayList<Ordering> result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "order_by"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:67:1: order_by returns [ArrayList<Ordering> result] : 'ORDER BY' co1= column_order ( ',' co2= column_order )* ;
    public final DQLParser.order_by_return order_by() throws RecognitionException {
        DQLParser.order_by_return retval = new DQLParser.order_by_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal17=null;
        Token char_literal18=null;
        DQLParser.column_order_return co1 = null;

        DQLParser.column_order_return co2 = null;


        Object string_literal17_tree=null;
        Object char_literal18_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:68:2: ( 'ORDER BY' co1= column_order ( ',' co2= column_order )* )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:68:11: 'ORDER BY' co1= column_order ( ',' co2= column_order )*
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new ArrayList<Ordering>();
            string_literal17=(Token)match(input,23,FOLLOW_23_in_order_by341); 
            string_literal17_tree = (Object)adaptor.create(string_literal17);
            adaptor.addChild(root_0, string_literal17_tree);

            pushFollow(FOLLOW_column_order_in_order_by345);
            co1=column_order();

            state._fsp--;

            adaptor.addChild(root_0, co1.getTree());
            retval.result.add((co1!=null?co1.result:null));
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:70:3: ( ',' co2= column_order )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==19) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:70:4: ',' co2= column_order
            	    {
            	    char_literal18=(Token)match(input,19,FOLLOW_19_in_order_by352); 
            	    char_literal18_tree = (Object)adaptor.create(char_literal18);
            	    adaptor.addChild(root_0, char_literal18_tree);

            	    pushFollow(FOLLOW_column_order_in_order_by356);
            	    co2=column_order();

            	    state._fsp--;

            	    adaptor.addChild(root_0, co2.getTree());
            	    retval.result.add((co2!=null?co2.result:null));

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "order_by"

    public static class limit_return extends ParserRuleReturnScope {
        public int pageNumber;
        public int pageSize;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "limit"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:73:1: limit returns [int pageNumber, int pageSize] : 'LIMIT' op1= INTEGER ',' op2= INTEGER ;
    public final DQLParser.limit_return limit() throws RecognitionException {
        DQLParser.limit_return retval = new DQLParser.limit_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token op1=null;
        Token op2=null;
        Token string_literal19=null;
        Token char_literal20=null;

        Object op1_tree=null;
        Object op2_tree=null;
        Object string_literal19_tree=null;
        Object char_literal20_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:74:2: ( 'LIMIT' op1= INTEGER ',' op2= INTEGER )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:74:4: 'LIMIT' op1= INTEGER ',' op2= INTEGER
            {
            root_0 = (Object)adaptor.nil();

            string_literal19=(Token)match(input,24,FOLLOW_24_in_limit377); 
            string_literal19_tree = (Object)adaptor.create(string_literal19);
            adaptor.addChild(root_0, string_literal19_tree);

            op1=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_limit381); 
            op1_tree = (Object)adaptor.create(op1);
            adaptor.addChild(root_0, op1_tree);

            retval.pageNumber =Integer.parseInt((op1!=null?op1.getText():null));
            char_literal20=(Token)match(input,19,FOLLOW_19_in_limit387); 
            char_literal20_tree = (Object)adaptor.create(char_literal20);
            adaptor.addChild(root_0, char_literal20_tree);

            op2=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_limit391); 
            op2_tree = (Object)adaptor.create(op2);
            adaptor.addChild(root_0, op2_tree);

            retval.pageSize =Integer.parseInt((op2!=null?op2.getText():null));

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "limit"

    public static class expression_return extends ParserRuleReturnScope {
        public Expression result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expression"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:79:1: expression returns [Expression result] : ( constraintset | ( NOT )* '(' expressionset ')' ) ;
    public final DQLParser.expression_return expression() throws RecognitionException {
        DQLParser.expression_return retval = new DQLParser.expression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT22=null;
        Token char_literal23=null;
        Token char_literal25=null;
        DQLParser.constraintset_return constraintset21 = null;

        DQLParser.expressionset_return expressionset24 = null;


        Object NOT22_tree=null;
        Object char_literal23_tree=null;
        Object char_literal25_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:80:2: ( ( constraintset | ( NOT )* '(' expressionset ')' ) )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:80:12: ( constraintset | ( NOT )* '(' expressionset ')' )
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new Expression(); boolean negate = false;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:81:2: ( constraintset | ( NOT )* '(' expressionset ')' )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==STRING_LITERAL||LA11_0==LITERAL) ) {
                alt11=1;
            }
            else if ( (LA11_0==NOT||LA11_0==25) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:81:4: constraintset
                    {
                    pushFollow(FOLLOW_constraintset_in_expression424);
                    constraintset21=constraintset();

                    state._fsp--;

                    adaptor.addChild(root_0, constraintset21.getTree());
                    retval.result.setConstraints((constraintset21!=null?constraintset21.result:null));

                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:82:4: ( NOT )* '(' expressionset ')'
                    {
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:82:4: ( NOT )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( (LA10_0==NOT) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:82:5: NOT
                    	    {
                    	    NOT22=(Token)match(input,NOT,FOLLOW_NOT_in_expression436); 
                    	    NOT22_tree = (Object)adaptor.create(NOT22);
                    	    adaptor.addChild(root_0, NOT22_tree);

                    	    negate = !negate;

                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);

                    char_literal23=(Token)match(input,25,FOLLOW_25_in_expression446); 
                    char_literal23_tree = (Object)adaptor.create(char_literal23);
                    adaptor.addChild(root_0, char_literal23_tree);

                    pushFollow(FOLLOW_expressionset_in_expression448);
                    expressionset24=expressionset();

                    state._fsp--;

                    adaptor.addChild(root_0, expressionset24.getTree());
                    char_literal25=(Token)match(input,26,FOLLOW_26_in_expression450); 
                    char_literal25_tree = (Object)adaptor.create(char_literal25);
                    adaptor.addChild(root_0, char_literal25_tree);

                    retval.result = (expressionset24!=null?expressionset24.result:null); retval.result.setNegation(negate);

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expression"

    public static class expressionset_return extends ParserRuleReturnScope {
        public Expression result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expressionset"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:87:1: expressionset returns [Expression result] : e1= expression ( operator e2= expression )* ;
    public final DQLParser.expressionset_return expressionset() throws RecognitionException {
        DQLParser.expressionset_return retval = new DQLParser.expressionset_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DQLParser.expression_return e1 = null;

        DQLParser.expression_return e2 = null;

        DQLParser.operator_return operator26 = null;



        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:88:2: (e1= expression ( operator e2= expression )* )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:88:10: e1= expression ( operator e2= expression )*
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new Expression();
            pushFollow(FOLLOW_expression_in_expressionset485);
            e1=expression();

            state._fsp--;

            adaptor.addChild(root_0, e1.getTree());
            retval.result.addChild(null, e1.result);
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:90:3: ( operator e2= expression )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( ((LA12_0>=31 && LA12_0<=34)) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:90:4: operator e2= expression
            	    {
            	    pushFollow(FOLLOW_operator_in_expressionset495);
            	    operator26=operator();

            	    state._fsp--;

            	    adaptor.addChild(root_0, operator26.getTree());
            	    pushFollow(FOLLOW_expression_in_expressionset499);
            	    e2=expression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, e2.getTree());
            	    retval.result.addChild((operator26!=null?operator26.result:null), e2.result);

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expressionset"

    public static class constraint_return extends ParserRuleReturnScope {
        public Constraint result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constraint"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:94:1: constraint returns [Constraint result] : fieldName ':' valueset ;
    public final DQLParser.constraint_return constraint() throws RecognitionException {
        DQLParser.constraint_return retval = new DQLParser.constraint_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal28=null;
        DQLParser.fieldName_return fieldName27 = null;

        DQLParser.valueset_return valueset29 = null;


        Object char_literal28_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:95:2: ( fieldName ':' valueset )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:96:3: fieldName ':' valueset
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_fieldName_in_constraint524);
            fieldName27=fieldName();

            state._fsp--;

            adaptor.addChild(root_0, fieldName27.getTree());
            retval.result = new Constraint((fieldName27!=null?fieldName27.result:null));
            char_literal28=(Token)match(input,27,FOLLOW_27_in_constraint532); 
            char_literal28_tree = (Object)adaptor.create(char_literal28);
            adaptor.addChild(root_0, char_literal28_tree);

            pushFollow(FOLLOW_valueset_in_constraint534);
            valueset29=valueset();

            state._fsp--;

            adaptor.addChild(root_0, valueset29.getTree());
            retval.result.setValues((valueset29!=null?valueset29.result:null));

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "constraint"

    public static class constraintset_return extends ParserRuleReturnScope {
        public ArrayList<Constraint> result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constraintset"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:100:1: constraintset returns [ArrayList<Constraint> result] : c1= constraint ( operator c2= constraint )* ;
    public final DQLParser.constraintset_return constraintset() throws RecognitionException {
        DQLParser.constraintset_return retval = new DQLParser.constraintset_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DQLParser.constraint_return c1 = null;

        DQLParser.constraint_return c2 = null;

        DQLParser.operator_return operator30 = null;



        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:101:2: (c1= constraint ( operator c2= constraint )* )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:101:10: c1= constraint ( operator c2= constraint )*
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new ArrayList<Constraint>();
            pushFollow(FOLLOW_constraint_in_constraintset564);
            c1=constraint();

            state._fsp--;

            adaptor.addChild(root_0, c1.getTree());
            retval.result.add(c1.result);
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:103:3: ( operator c2= constraint )*
            loop13:
            do {
                int alt13=2;
                alt13 = dfa13.predict(input);
                switch (alt13) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:103:4: operator c2= constraint
            	    {
            	    pushFollow(FOLLOW_operator_in_constraintset573);
            	    operator30=operator();

            	    state._fsp--;

            	    adaptor.addChild(root_0, operator30.getTree());
            	    pushFollow(FOLLOW_constraint_in_constraintset577);
            	    c2=constraint();

            	    state._fsp--;

            	    adaptor.addChild(root_0, c2.getTree());
            	    Constraint c = c2.result; c.setOperator((operator30!=null?operator30.result:null)); retval.result.add(c);

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "constraintset"

    public static class value_return extends ParserRuleReturnScope {
        public Value result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "value"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:107:1: value returns [Value result] : ( NOT )* ( parameter | 'IN' '{' parameterset '}' ) ;
    public final DQLParser.value_return value() throws RecognitionException {
        DQLParser.value_return retval = new DQLParser.value_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NOT31=null;
        Token string_literal33=null;
        Token char_literal34=null;
        Token char_literal36=null;
        DQLParser.parameter_return parameter32 = null;

        DQLParser.parameterset_return parameterset35 = null;


        Object NOT31_tree=null;
        Object string_literal33_tree=null;
        Object char_literal34_tree=null;
        Object char_literal36_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:108:2: ( ( NOT )* ( parameter | 'IN' '{' parameterset '}' ) )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:108:12: ( NOT )* ( parameter | 'IN' '{' parameterset '}' )
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new Value(); boolean negate = false;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:109:3: ( NOT )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==NOT) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:109:4: NOT
            	    {
            	    NOT31=(Token)match(input,NOT,FOLLOW_NOT_in_value611); 
            	    NOT31_tree = (Object)adaptor.create(NOT31);
            	    adaptor.addChild(root_0, NOT31_tree);

            	    negate = !negate;

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:110:3: ( parameter | 'IN' '{' parameterset '}' )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==INTEGER||(LA15_0>=NULL && LA15_0<=LITERAL)||(LA15_0>=35 && LA15_0<=40)) ) {
                alt15=1;
            }
            else if ( (LA15_0==28) ) {
                alt15=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:110:5: parameter
                    {
                    pushFollow(FOLLOW_parameter_in_value622);
                    parameter32=parameter();

                    state._fsp--;

                    adaptor.addChild(root_0, parameter32.getTree());
                    retval.result.addParam((parameter32!=null?parameter32.result:null));

                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:111:5: 'IN' '{' parameterset '}'
                    {
                    string_literal33=(Token)match(input,28,FOLLOW_28_in_value635); 
                    string_literal33_tree = (Object)adaptor.create(string_literal33);
                    adaptor.addChild(root_0, string_literal33_tree);

                    char_literal34=(Token)match(input,29,FOLLOW_29_in_value637); 
                    char_literal34_tree = (Object)adaptor.create(char_literal34);
                    adaptor.addChild(root_0, char_literal34_tree);

                    pushFollow(FOLLOW_parameterset_in_value639);
                    parameterset35=parameterset();

                    state._fsp--;

                    adaptor.addChild(root_0, parameterset35.getTree());
                    char_literal36=(Token)match(input,30,FOLLOW_30_in_value641); 
                    char_literal36_tree = (Object)adaptor.create(char_literal36);
                    adaptor.addChild(root_0, char_literal36_tree);

                    retval.result.setParams((parameterset35!=null?parameterset35.result:null));

                    }
                    break;

            }

            retval.result.setNegation(negate);

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "value"

    public static class valueset_return extends ParserRuleReturnScope {
        public ArrayList<Value> result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "valueset"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:115:1: valueset returns [ArrayList<Value> result] : (v0= value | '(' v1= value ( operator v2= value )* ')' ) ;
    public final DQLParser.valueset_return valueset() throws RecognitionException {
        DQLParser.valueset_return retval = new DQLParser.valueset_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal37=null;
        Token char_literal39=null;
        DQLParser.value_return v0 = null;

        DQLParser.value_return v1 = null;

        DQLParser.value_return v2 = null;

        DQLParser.operator_return operator38 = null;


        Object char_literal37_tree=null;
        Object char_literal39_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:116:2: ( (v0= value | '(' v1= value ( operator v2= value )* ')' ) )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:116:9: (v0= value | '(' v1= value ( operator v2= value )* ')' )
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new ArrayList<Value>();
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:117:2: (v0= value | '(' v1= value ( operator v2= value )* ')' )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( ((LA17_0>=INTEGER && LA17_0<=LITERAL)||LA17_0==28||(LA17_0>=35 && LA17_0<=40)) ) {
                alt17=1;
            }
            else if ( (LA17_0==25) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:117:4: v0= value
                    {
                    pushFollow(FOLLOW_value_in_valueset684);
                    v0=value();

                    state._fsp--;

                    adaptor.addChild(root_0, v0.getTree());
                    retval.result.add(v0.result);

                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:118:4: '(' v1= value ( operator v2= value )* ')'
                    {
                    char_literal37=(Token)match(input,25,FOLLOW_25_in_valueset694); 
                    char_literal37_tree = (Object)adaptor.create(char_literal37);
                    adaptor.addChild(root_0, char_literal37_tree);

                    pushFollow(FOLLOW_value_in_valueset698);
                    v1=value();

                    state._fsp--;

                    adaptor.addChild(root_0, v1.getTree());
                    retval.result.add(v1.result);
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:119:3: ( operator v2= value )*
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( ((LA16_0>=31 && LA16_0<=34)) ) {
                            alt16=1;
                        }


                        switch (alt16) {
                    	case 1 :
                    	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:119:4: operator v2= value
                    	    {
                    	    pushFollow(FOLLOW_operator_in_valueset707);
                    	    operator38=operator();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, operator38.getTree());
                    	    pushFollow(FOLLOW_value_in_valueset711);
                    	    v2=value();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, v2.getTree());
                    	    Value v = v2.result; v.setOperator((operator38!=null?operator38.result:null)); retval.result.add(v);

                    	    }
                    	    break;

                    	default :
                    	    break loop16;
                        }
                    } while (true);

                    char_literal39=(Token)match(input,26,FOLLOW_26_in_valueset717); 
                    char_literal39_tree = (Object)adaptor.create(char_literal39);
                    adaptor.addChild(root_0, char_literal39_tree);


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "valueset"

    public static class parameter_return extends ParserRuleReturnScope {
        public Parameter result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parameter"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:124:1: parameter returns [Parameter result] : ( comparator )? ( NULL | STRING_LITERAL | INTEGER | REAL | BOOLEAN | DATETIME | LITERAL ) ;
    public final DQLParser.parameter_return parameter() throws RecognitionException {
        DQLParser.parameter_return retval = new DQLParser.parameter_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token NULL41=null;
        Token STRING_LITERAL42=null;
        Token INTEGER43=null;
        Token REAL44=null;
        Token BOOLEAN45=null;
        Token DATETIME46=null;
        Token LITERAL47=null;
        DQLParser.comparator_return comparator40 = null;


        Object NULL41_tree=null;
        Object STRING_LITERAL42_tree=null;
        Object INTEGER43_tree=null;
        Object REAL44_tree=null;
        Object BOOLEAN45_tree=null;
        Object DATETIME46_tree=null;
        Object LITERAL47_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:125:2: ( ( comparator )? ( NULL | STRING_LITERAL | INTEGER | REAL | BOOLEAN | DATETIME | LITERAL ) )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:125:8: ( comparator )? ( NULL | STRING_LITERAL | INTEGER | REAL | BOOLEAN | DATETIME | LITERAL )
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new Parameter(); DqlConstants.Comparator comparator = DqlConstants.Comparator.E;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:126:3: ( comparator )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( ((LA18_0>=35 && LA18_0<=40)) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:126:4: comparator
                    {
                    pushFollow(FOLLOW_comparator_in_parameter746);
                    comparator40=comparator();

                    state._fsp--;

                    adaptor.addChild(root_0, comparator40.getTree());
                    comparator = (comparator40!=null?comparator40.result:null);

                    }
                    break;

            }

            retval.result.comp = comparator;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:127:3: ( NULL | STRING_LITERAL | INTEGER | REAL | BOOLEAN | DATETIME | LITERAL )
            int alt19=7;
            switch ( input.LA(1) ) {
            case NULL:
                {
                alt19=1;
                }
                break;
            case STRING_LITERAL:
                {
                alt19=2;
                }
                break;
            case INTEGER:
                {
                alt19=3;
                }
                break;
            case REAL:
                {
                alt19=4;
                }
                break;
            case BOOLEAN:
                {
                alt19=5;
                }
                break;
            case DATETIME:
                {
                alt19=6;
                }
                break;
            case LITERAL:
                {
                alt19=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }

            switch (alt19) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:127:4: NULL
                    {
                    NULL41=(Token)match(input,NULL,FOLLOW_NULL_in_parameter758); 
                    NULL41_tree = (Object)adaptor.create(NULL41);
                    adaptor.addChild(root_0, NULL41_tree);

                    retval.result.type = DqlConstants.ParamType.NULL; retval.result.param = (NULL41!=null?NULL41.getText():null);

                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:128:4: STRING_LITERAL
                    {
                    STRING_LITERAL42=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_parameter768); 
                    STRING_LITERAL42_tree = (Object)adaptor.create(STRING_LITERAL42);
                    adaptor.addChild(root_0, STRING_LITERAL42_tree);

                    retval.result.type = DqlConstants.ParamType.STRING; retval.result.param = (STRING_LITERAL42!=null?STRING_LITERAL42.getText():null);

                    }
                    break;
                case 3 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:129:4: INTEGER
                    {
                    INTEGER43=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_parameter775); 
                    INTEGER43_tree = (Object)adaptor.create(INTEGER43);
                    adaptor.addChild(root_0, INTEGER43_tree);

                    retval.result.type = DqlConstants.ParamType.NUMERIC; retval.result.param = (INTEGER43!=null?INTEGER43.getText():null);

                    }
                    break;
                case 4 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:130:4: REAL
                    {
                    REAL44=(Token)match(input,REAL,FOLLOW_REAL_in_parameter784); 
                    REAL44_tree = (Object)adaptor.create(REAL44);
                    adaptor.addChild(root_0, REAL44_tree);

                    retval.result.type = DqlConstants.ParamType.NUMERIC; retval.result.param = (REAL44!=null?REAL44.getText():null);

                    }
                    break;
                case 5 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:131:4: BOOLEAN
                    {
                    BOOLEAN45=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_parameter793); 
                    BOOLEAN45_tree = (Object)adaptor.create(BOOLEAN45);
                    adaptor.addChild(root_0, BOOLEAN45_tree);

                    retval.result.type = DqlConstants.ParamType.BOOLEAN; retval.result.param = (BOOLEAN45!=null?BOOLEAN45.getText():null);

                    }
                    break;
                case 6 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:132:4: DATETIME
                    {
                    DATETIME46=(Token)match(input,DATETIME,FOLLOW_DATETIME_in_parameter801); 
                    DATETIME46_tree = (Object)adaptor.create(DATETIME46);
                    adaptor.addChild(root_0, DATETIME46_tree);

                    retval.result.type = DqlConstants.ParamType.DATETIME; retval.result.param = (DATETIME46!=null?DATETIME46.getText():null);

                    }
                    break;
                case 7 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:133:4: LITERAL
                    {
                    LITERAL47=(Token)match(input,LITERAL,FOLLOW_LITERAL_in_parameter809); 
                    LITERAL47_tree = (Object)adaptor.create(LITERAL47);
                    adaptor.addChild(root_0, LITERAL47_tree);

                    retval.result.type = DqlConstants.ParamType.UNKNOWN; retval.result.param = (LITERAL47!=null?LITERAL47.getText():null);

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parameter"

    public static class parameterset_return extends ParserRuleReturnScope {
        public ArrayList<Parameter> result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parameterset"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:137:1: parameterset returns [ArrayList<Parameter> result] : p1= parameter ( ',' p2= parameter )* ;
    public final DQLParser.parameterset_return parameterset() throws RecognitionException {
        DQLParser.parameterset_return retval = new DQLParser.parameterset_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal48=null;
        DQLParser.parameter_return p1 = null;

        DQLParser.parameter_return p2 = null;


        Object char_literal48_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:138:2: (p1= parameter ( ',' p2= parameter )* )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:138:9: p1= parameter ( ',' p2= parameter )*
            {
            root_0 = (Object)adaptor.nil();

            retval.result = new ArrayList<Parameter>();
            pushFollow(FOLLOW_parameter_in_parameterset843);
            p1=parameter();

            state._fsp--;

            adaptor.addChild(root_0, p1.getTree());
            retval.result.add(p1.result);
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:140:3: ( ',' p2= parameter )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==19) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:140:4: ',' p2= parameter
            	    {
            	    char_literal48=(Token)match(input,19,FOLLOW_19_in_parameterset852); 
            	    char_literal48_tree = (Object)adaptor.create(char_literal48);
            	    adaptor.addChild(root_0, char_literal48_tree);

            	    pushFollow(FOLLOW_parameter_in_parameterset856);
            	    p2=parameter();

            	    state._fsp--;

            	    adaptor.addChild(root_0, p2.getTree());
            	    retval.result.add(p2.result);

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parameterset"

    public static class column_order_return extends ParserRuleReturnScope {
        public Ordering result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "column_order"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:144:1: column_order returns [Ordering result] : fieldName ORDER ;
    public final DQLParser.column_order_return column_order() throws RecognitionException {
        DQLParser.column_order_return retval = new DQLParser.column_order_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ORDER50=null;
        DQLParser.fieldName_return fieldName49 = null;


        Object ORDER50_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:145:2: ( fieldName ORDER )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:145:5: fieldName ORDER
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_fieldName_in_column_order878);
            fieldName49=fieldName();

            state._fsp--;

            adaptor.addChild(root_0, fieldName49.getTree());
            ORDER50=(Token)match(input,ORDER,FOLLOW_ORDER_in_column_order880); 
            ORDER50_tree = (Object)adaptor.create(ORDER50);
            adaptor.addChild(root_0, ORDER50_tree);

            retval.result = new Ordering((ORDER50!=null?ORDER50.getText():null), (fieldName49!=null?fieldName49.result:null));

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "column_order"

    public static class operator_return extends ParserRuleReturnScope {
        public DqlConstants.Operator result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "operator"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:147:1: operator returns [DqlConstants.Operator result] : ( 'AND' | 'OR' | 'and' | 'or' );
    public final DQLParser.operator_return operator() throws RecognitionException {
        DQLParser.operator_return retval = new DQLParser.operator_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal51=null;
        Token string_literal52=null;
        Token string_literal53=null;
        Token string_literal54=null;

        Object string_literal51_tree=null;
        Object string_literal52_tree=null;
        Object string_literal53_tree=null;
        Object string_literal54_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:148:2: ( 'AND' | 'OR' | 'and' | 'or' )
            int alt21=4;
            switch ( input.LA(1) ) {
            case 31:
                {
                alt21=1;
                }
                break;
            case 32:
                {
                alt21=2;
                }
                break;
            case 33:
                {
                alt21=3;
                }
                break;
            case 34:
                {
                alt21=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }

            switch (alt21) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:148:5: 'AND'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal51=(Token)match(input,31,FOLLOW_31_in_operator898); 
                    string_literal51_tree = (Object)adaptor.create(string_literal51);
                    adaptor.addChild(root_0, string_literal51_tree);

                    retval.result = DqlConstants.Operator.AND;

                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:149:5: 'OR'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal52=(Token)match(input,32,FOLLOW_32_in_operator907); 
                    string_literal52_tree = (Object)adaptor.create(string_literal52);
                    adaptor.addChild(root_0, string_literal52_tree);

                    retval.result = DqlConstants.Operator.OR;

                    }
                    break;
                case 3 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:150:5: 'and'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal53=(Token)match(input,33,FOLLOW_33_in_operator916); 
                    string_literal53_tree = (Object)adaptor.create(string_literal53);
                    adaptor.addChild(root_0, string_literal53_tree);

                    retval.result = DqlConstants.Operator.AND;

                    }
                    break;
                case 4 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:151:5: 'or'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal54=(Token)match(input,34,FOLLOW_34_in_operator925); 
                    string_literal54_tree = (Object)adaptor.create(string_literal54);
                    adaptor.addChild(root_0, string_literal54_tree);

                    retval.result = DqlConstants.Operator.OR;

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "operator"

    public static class comparator_return extends ParserRuleReturnScope {
        public DqlConstants.Comparator result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comparator"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:153:1: comparator returns [DqlConstants.Comparator result] : ( '=' | '<>' | '<' | '<=' | '>' | '>=' );
    public final DQLParser.comparator_return comparator() throws RecognitionException {
        DQLParser.comparator_return retval = new DQLParser.comparator_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal55=null;
        Token string_literal56=null;
        Token char_literal57=null;
        Token string_literal58=null;
        Token char_literal59=null;
        Token string_literal60=null;

        Object char_literal55_tree=null;
        Object string_literal56_tree=null;
        Object char_literal57_tree=null;
        Object string_literal58_tree=null;
        Object char_literal59_tree=null;
        Object string_literal60_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:154:2: ( '=' | '<>' | '<' | '<=' | '>' | '>=' )
            int alt22=6;
            switch ( input.LA(1) ) {
            case 35:
                {
                alt22=1;
                }
                break;
            case 36:
                {
                alt22=2;
                }
                break;
            case 37:
                {
                alt22=3;
                }
                break;
            case 38:
                {
                alt22=4;
                }
                break;
            case 39:
                {
                alt22=5;
                }
                break;
            case 40:
                {
                alt22=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }

            switch (alt22) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:154:5: '='
                    {
                    root_0 = (Object)adaptor.nil();

                    char_literal55=(Token)match(input,35,FOLLOW_35_in_comparator942); 
                    char_literal55_tree = (Object)adaptor.create(char_literal55);
                    adaptor.addChild(root_0, char_literal55_tree);

                    retval.result = DqlConstants.Comparator.E;

                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:155:5: '<>'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal56=(Token)match(input,36,FOLLOW_36_in_comparator951); 
                    string_literal56_tree = (Object)adaptor.create(string_literal56);
                    adaptor.addChild(root_0, string_literal56_tree);

                    retval.result = DqlConstants.Comparator.NE;

                    }
                    break;
                case 3 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:156:5: '<'
                    {
                    root_0 = (Object)adaptor.nil();

                    char_literal57=(Token)match(input,37,FOLLOW_37_in_comparator960); 
                    char_literal57_tree = (Object)adaptor.create(char_literal57);
                    adaptor.addChild(root_0, char_literal57_tree);

                    retval.result = DqlConstants.Comparator.L;

                    }
                    break;
                case 4 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:157:5: '<='
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal58=(Token)match(input,38,FOLLOW_38_in_comparator969); 
                    string_literal58_tree = (Object)adaptor.create(string_literal58);
                    adaptor.addChild(root_0, string_literal58_tree);

                    retval.result = DqlConstants.Comparator.LE;

                    }
                    break;
                case 5 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:158:5: '>'
                    {
                    root_0 = (Object)adaptor.nil();

                    char_literal59=(Token)match(input,39,FOLLOW_39_in_comparator978); 
                    char_literal59_tree = (Object)adaptor.create(char_literal59);
                    adaptor.addChild(root_0, char_literal59_tree);

                    retval.result = DqlConstants.Comparator.G;

                    }
                    break;
                case 6 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:159:5: '>='
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal60=(Token)match(input,40,FOLLOW_40_in_comparator987); 
                    string_literal60_tree = (Object)adaptor.create(string_literal60);
                    adaptor.addChild(root_0, string_literal60_tree);

                    retval.result = DqlConstants.Comparator.GE;

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comparator"

    public static class baName_return extends ParserRuleReturnScope {
        public String result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "baName"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:161:1: baName returns [String result] : ( LITERAL | STRING_LITERAL );
    public final DQLParser.baName_return baName() throws RecognitionException {
        DQLParser.baName_return retval = new DQLParser.baName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LITERAL61=null;
        Token STRING_LITERAL62=null;

        Object LITERAL61_tree=null;
        Object STRING_LITERAL62_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:162:2: ( LITERAL | STRING_LITERAL )
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==LITERAL) ) {
                alt23=1;
            }
            else if ( (LA23_0==STRING_LITERAL) ) {
                alt23=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }
            switch (alt23) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:163:3: LITERAL
                    {
                    root_0 = (Object)adaptor.nil();

                    LITERAL61=(Token)match(input,LITERAL,FOLLOW_LITERAL_in_baName1007); 
                    LITERAL61_tree = (Object)adaptor.create(LITERAL61);
                    adaptor.addChild(root_0, LITERAL61_tree);

                    retval.result = (LITERAL61!=null?LITERAL61.getText():null);

                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:164:4: STRING_LITERAL
                    {
                    root_0 = (Object)adaptor.nil();

                    STRING_LITERAL62=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_baName1014); 
                    STRING_LITERAL62_tree = (Object)adaptor.create(STRING_LITERAL62);
                    adaptor.addChild(root_0, STRING_LITERAL62_tree);

                    retval.result = (STRING_LITERAL62!=null?STRING_LITERAL62.getText():null);

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "baName"

    public static class fieldName_return extends ParserRuleReturnScope {
        public String result;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fieldName"
    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:166:1: fieldName returns [String result] : ( LITERAL | STRING_LITERAL );
    public final DQLParser.fieldName_return fieldName() throws RecognitionException {
        DQLParser.fieldName_return retval = new DQLParser.fieldName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LITERAL63=null;
        Token STRING_LITERAL64=null;

        Object LITERAL63_tree=null;
        Object STRING_LITERAL64_tree=null;

        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:167:2: ( LITERAL | STRING_LITERAL )
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==LITERAL) ) {
                alt24=1;
            }
            else if ( (LA24_0==STRING_LITERAL) ) {
                alt24=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:168:3: LITERAL
                    {
                    root_0 = (Object)adaptor.nil();

                    LITERAL63=(Token)match(input,LITERAL,FOLLOW_LITERAL_in_fieldName1034); 
                    LITERAL63_tree = (Object)adaptor.create(LITERAL63);
                    adaptor.addChild(root_0, LITERAL63_tree);

                    retval.result = (LITERAL63!=null?LITERAL63.getText():null);

                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:169:4: STRING_LITERAL
                    {
                    root_0 = (Object)adaptor.nil();

                    STRING_LITERAL64=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_fieldName1041); 
                    STRING_LITERAL64_tree = (Object)adaptor.create(STRING_LITERAL64);
                    adaptor.addChild(root_0, STRING_LITERAL64_tree);

                    retval.result = (STRING_LITERAL64!=null?STRING_LITERAL64.getText():null);

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "fieldName"

    // Delegated rules


    protected DFA13 dfa13 = new DFA13(this);
    static final String DFA13_eotS =
        "\u0093\uffff";
    static final String DFA13_eofS =
        "\1\5\u0092\uffff";
    static final String DFA13_minS =
        "\1\26\4\5\1\uffff\2\33\10\4\7\uffff\1\35\11\4\7\32\1\35\6\4\7\23"+
        "\4\4\1\uffff\2\4\1\uffff\7\4\7\32\1\35\6\4\7\23\6\4\7\23\2\4\1\32"+
        "\6\4\7\23\6\4\7\23\1\4\1\32\6\4\7\23";
    static final String DFA13_maxS =
        "\1\42\4\31\1\uffff\2\33\2\50\6\13\7\uffff\1\35\3\50\6\13\7\42\1"+
        "\35\6\13\7\36\4\50\1\uffff\2\50\1\uffff\1\50\6\13\7\42\1\35\6\13"+
        "\7\36\6\13\7\36\2\50\1\42\6\13\7\36\6\13\7\36\1\50\1\42\6\13\7\36";
    static final String DFA13_acceptS =
        "\5\uffff\1\2\12\uffff\7\1\43\uffff\1\1\2\uffff\1\1\125\uffff";
    static final String DFA13_specialS =
        "\u0093\uffff}>";
    static final String[] DFA13_transitionS = {
            "\3\5\1\uffff\1\5\4\uffff\1\1\1\2\1\3\1\4",
            "\1\5\1\uffff\1\7\3\uffff\1\6\15\uffff\1\5",
            "\1\5\1\uffff\1\7\3\uffff\1\6\15\uffff\1\5",
            "\1\5\1\uffff\1\7\3\uffff\1\6\15\uffff\1\5",
            "\1\5\1\uffff\1\7\3\uffff\1\6\15\uffff\1\5",
            "",
            "\1\10",
            "\1\10",
            "\1\22\1\11\1\20\1\21\1\23\1\24\1\25\1\26\15\uffff\1\30\2\uffff"+
            "\1\27\6\uffff\1\12\1\13\1\14\1\15\1\16\1\17",
            "\1\22\1\11\1\20\1\21\1\23\1\24\1\25\1\26\20\uffff\1\27\6\uffff"+
            "\1\12\1\13\1\14\1\15\1\16\1\17",
            "\1\22\1\uffff\1\20\1\21\1\23\1\24\1\25\1\26",
            "\1\22\1\uffff\1\20\1\21\1\23\1\24\1\25\1\26",
            "\1\22\1\uffff\1\20\1\21\1\23\1\24\1\25\1\26",
            "\1\22\1\uffff\1\20\1\21\1\23\1\24\1\25\1\26",
            "\1\22\1\uffff\1\20\1\21\1\23\1\24\1\25\1\26",
            "\1\22\1\uffff\1\20\1\21\1\23\1\24\1\25\1\26",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\31",
            "\1\43\1\32\1\41\1\42\1\44\1\45\1\46\1\47\20\uffff\1\50\6\uffff"+
            "\1\33\1\34\1\35\1\36\1\37\1\40",
            "\1\61\1\uffff\1\57\1\60\1\62\1\63\1\64\1\65\27\uffff\1\51\1"+
            "\52\1\53\1\54\1\55\1\56",
            "\1\43\1\32\1\41\1\42\1\44\1\45\1\46\1\47\20\uffff\1\50\6\uffff"+
            "\1\33\1\34\1\35\1\36\1\37\1\40",
            "\1\43\1\uffff\1\41\1\42\1\44\1\45\1\46\1\47",
            "\1\43\1\uffff\1\41\1\42\1\44\1\45\1\46\1\47",
            "\1\43\1\uffff\1\41\1\42\1\44\1\45\1\46\1\47",
            "\1\43\1\uffff\1\41\1\42\1\44\1\45\1\46\1\47",
            "\1\43\1\uffff\1\41\1\42\1\44\1\45\1\46\1\47",
            "\1\43\1\uffff\1\41\1\42\1\44\1\45\1\46\1\47",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\73",
            "\1\61\1\uffff\1\57\1\60\1\62\1\63\1\64\1\65",
            "\1\61\1\uffff\1\57\1\60\1\62\1\63\1\64\1\65",
            "\1\61\1\uffff\1\57\1\60\1\62\1\63\1\64\1\65",
            "\1\61\1\uffff\1\57\1\60\1\62\1\63\1\64\1\65",
            "\1\61\1\uffff\1\57\1\60\1\62\1\63\1\64\1\65",
            "\1\61\1\uffff\1\57\1\60\1\62\1\63\1\64\1\65",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\107\1\76\1\105\1\106\1\110\1\111\1\112\1\113\20\uffff\1"+
            "\114\6\uffff\1\77\1\100\1\101\1\102\1\103\1\104",
            "\1\107\1\76\1\105\1\106\1\110\1\111\1\112\1\113\20\uffff\1"+
            "\114\6\uffff\1\77\1\100\1\101\1\102\1\103\1\104",
            "\1\107\1\76\1\105\1\106\1\110\1\111\1\112\1\113\20\uffff\1"+
            "\114\6\uffff\1\77\1\100\1\101\1\102\1\103\1\104",
            "\1\107\1\76\1\105\1\106\1\110\1\111\1\112\1\113\20\uffff\1"+
            "\114\6\uffff\1\77\1\100\1\101\1\102\1\103\1\104",
            "",
            "\1\125\1\uffff\1\123\1\124\1\126\1\127\1\130\1\131\27\uffff"+
            "\1\115\1\116\1\117\1\120\1\121\1\122",
            "\1\142\1\uffff\1\140\1\141\1\143\1\144\1\145\1\146\27\uffff"+
            "\1\132\1\133\1\134\1\135\1\136\1\137",
            "",
            "\1\107\1\76\1\105\1\106\1\110\1\111\1\112\1\113\20\uffff\1"+
            "\114\6\uffff\1\77\1\100\1\101\1\102\1\103\1\104",
            "\1\107\1\uffff\1\105\1\106\1\110\1\111\1\112\1\113",
            "\1\107\1\uffff\1\105\1\106\1\110\1\111\1\112\1\113",
            "\1\107\1\uffff\1\105\1\106\1\110\1\111\1\112\1\113",
            "\1\107\1\uffff\1\105\1\106\1\110\1\111\1\112\1\113",
            "\1\107\1\uffff\1\105\1\106\1\110\1\111\1\112\1\113",
            "\1\107\1\uffff\1\105\1\106\1\110\1\111\1\112\1\113",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\147",
            "\1\125\1\uffff\1\123\1\124\1\126\1\127\1\130\1\131",
            "\1\125\1\uffff\1\123\1\124\1\126\1\127\1\130\1\131",
            "\1\125\1\uffff\1\123\1\124\1\126\1\127\1\130\1\131",
            "\1\125\1\uffff\1\123\1\124\1\126\1\127\1\130\1\131",
            "\1\125\1\uffff\1\123\1\124\1\126\1\127\1\130\1\131",
            "\1\125\1\uffff\1\123\1\124\1\126\1\127\1\130\1\131",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\142\1\uffff\1\140\1\141\1\143\1\144\1\145\1\146",
            "\1\142\1\uffff\1\140\1\141\1\143\1\144\1\145\1\146",
            "\1\142\1\uffff\1\140\1\141\1\143\1\144\1\145\1\146",
            "\1\142\1\uffff\1\140\1\141\1\143\1\144\1\145\1\146",
            "\1\142\1\uffff\1\140\1\141\1\143\1\144\1\145\1\146",
            "\1\142\1\uffff\1\140\1\141\1\143\1\144\1\145\1\146",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\74\12\uffff\1\75",
            "\1\162\1\uffff\1\160\1\161\1\163\1\164\1\165\1\166\27\uffff"+
            "\1\152\1\153\1\154\1\155\1\156\1\157",
            "\1\177\1\uffff\1\175\1\176\1\u0080\1\u0081\1\u0082\1\u0083"+
            "\27\uffff\1\167\1\170\1\171\1\172\1\173\1\174",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\162\1\uffff\1\160\1\161\1\163\1\164\1\165\1\166",
            "\1\162\1\uffff\1\160\1\161\1\163\1\164\1\165\1\166",
            "\1\162\1\uffff\1\160\1\161\1\163\1\164\1\165\1\166",
            "\1\162\1\uffff\1\160\1\161\1\163\1\164\1\165\1\166",
            "\1\162\1\uffff\1\160\1\161\1\163\1\164\1\165\1\166",
            "\1\162\1\uffff\1\160\1\161\1\163\1\164\1\165\1\166",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\177\1\uffff\1\175\1\176\1\u0080\1\u0081\1\u0082\1\u0083",
            "\1\177\1\uffff\1\175\1\176\1\u0080\1\u0081\1\u0082\1\u0083",
            "\1\177\1\uffff\1\175\1\176\1\u0080\1\u0081\1\u0082\1\u0083",
            "\1\177\1\uffff\1\175\1\176\1\u0080\1\u0081\1\u0082\1\u0083",
            "\1\177\1\uffff\1\175\1\176\1\u0080\1\u0081\1\u0082\1\u0083",
            "\1\177\1\uffff\1\175\1\176\1\u0080\1\u0081\1\u0082\1\u0083",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\150\12\uffff\1\151",
            "\1\u008e\1\uffff\1\u008c\1\u008d\1\u008f\1\u0090\1\u0091\1"+
            "\u0092\27\uffff\1\u0086\1\u0087\1\u0088\1\u0089\1\u008a\1\u008b",
            "\1\72\4\uffff\1\66\1\67\1\70\1\71",
            "\1\u008e\1\uffff\1\u008c\1\u008d\1\u008f\1\u0090\1\u0091\1"+
            "\u0092",
            "\1\u008e\1\uffff\1\u008c\1\u008d\1\u008f\1\u0090\1\u0091\1"+
            "\u0092",
            "\1\u008e\1\uffff\1\u008c\1\u008d\1\u008f\1\u0090\1\u0091\1"+
            "\u0092",
            "\1\u008e\1\uffff\1\u008c\1\u008d\1\u008f\1\u0090\1\u0091\1"+
            "\u0092",
            "\1\u008e\1\uffff\1\u008c\1\u008d\1\u008f\1\u0090\1\u0091\1"+
            "\u0092",
            "\1\u008e\1\uffff\1\u008c\1\u008d\1\u008f\1\u0090\1\u0091\1"+
            "\u0092",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085",
            "\1\u0084\12\uffff\1\u0085"
    };

    static final short[] DFA13_eot = DFA.unpackEncodedString(DFA13_eotS);
    static final short[] DFA13_eof = DFA.unpackEncodedString(DFA13_eofS);
    static final char[] DFA13_min = DFA.unpackEncodedStringToUnsignedChars(DFA13_minS);
    static final char[] DFA13_max = DFA.unpackEncodedStringToUnsignedChars(DFA13_maxS);
    static final short[] DFA13_accept = DFA.unpackEncodedString(DFA13_acceptS);
    static final short[] DFA13_special = DFA.unpackEncodedString(DFA13_specialS);
    static final short[][] DFA13_transition;

    static {
        int numStates = DFA13_transitionS.length;
        DFA13_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA13_transition[i] = DFA.unpackEncodedString(DFA13_transitionS[i]);
        }
    }

    class DFA13 extends DFA {

        public DFA13(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 13;
            this.eot = DFA13_eot;
            this.eof = DFA13_eof;
            this.min = DFA13_min;
            this.max = DFA13_max;
            this.accept = DFA13_accept;
            this.special = DFA13_special;
            this.transition = DFA13_transition;
        }
        public String getDescription() {
            return "()* loopback of 103:3: ( operator c2= constraint )*";
        }
    }
 

    public static final BitSet FOLLOW_select_in_query66 = new BitSet(new long[]{0x0000000001F00000L});
    public static final BitSet FOLLOW_from_in_query75 = new BitSet(new long[]{0x0000000001E00000L});
    public static final BitSet FOLLOW_where_in_query93 = new BitSet(new long[]{0x0000000001C00000L});
    public static final BitSet FOLLOW_has_text_in_query108 = new BitSet(new long[]{0x0000000001800000L});
    public static final BitSet FOLLOW_order_by_in_query122 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_limit_in_query136 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_query145 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_17_in_select170 = new BitSet(new long[]{0x0000000000040880L});
    public static final BitSet FOLLOW_18_in_select179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldName_in_select197 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_19_in_select208 = new BitSet(new long[]{0x0000000000040880L});
    public static final BitSet FOLLOW_fieldName_in_select213 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_20_in_from250 = new BitSet(new long[]{0x0000000000000880L});
    public static final BitSet FOLLOW_baName_in_from254 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_19_in_from263 = new BitSet(new long[]{0x0000000000000880L});
    public static final BitSet FOLLOW_baName_in_from267 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_21_in_where289 = new BitSet(new long[]{0x00000000020408A0L});
    public static final BitSet FOLLOW_expressionset_in_where291 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_22_in_has_text310 = new BitSet(new long[]{0x00000000020408A0L});
    public static final BitSet FOLLOW_expressionset_in_has_text312 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_order_by341 = new BitSet(new long[]{0x0000000000040880L});
    public static final BitSet FOLLOW_column_order_in_order_by345 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_19_in_order_by352 = new BitSet(new long[]{0x0000000000040880L});
    public static final BitSet FOLLOW_column_order_in_order_by356 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_24_in_limit377 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_INTEGER_in_limit381 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_19_in_limit387 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_INTEGER_in_limit391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constraintset_in_expression424 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_expression436 = new BitSet(new long[]{0x0000000002000020L});
    public static final BitSet FOLLOW_25_in_expression446 = new BitSet(new long[]{0x00000000020408A0L});
    public static final BitSet FOLLOW_expressionset_in_expression448 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_expression450 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_expressionset485 = new BitSet(new long[]{0x0000000780000002L});
    public static final BitSet FOLLOW_operator_in_expressionset495 = new BitSet(new long[]{0x00000000020408A0L});
    public static final BitSet FOLLOW_expression_in_expressionset499 = new BitSet(new long[]{0x0000000780000002L});
    public static final BitSet FOLLOW_fieldName_in_constraint524 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_constraint532 = new BitSet(new long[]{0x000001F812000FF0L});
    public static final BitSet FOLLOW_valueset_in_constraint534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constraint_in_constraintset564 = new BitSet(new long[]{0x0000000780000002L});
    public static final BitSet FOLLOW_operator_in_constraintset573 = new BitSet(new long[]{0x0000000000040880L});
    public static final BitSet FOLLOW_constraint_in_constraintset577 = new BitSet(new long[]{0x0000000780000002L});
    public static final BitSet FOLLOW_NOT_in_value611 = new BitSet(new long[]{0x000001F810000FF0L});
    public static final BitSet FOLLOW_parameter_in_value622 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_value635 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_value637 = new BitSet(new long[]{0x000001F800000FD0L});
    public static final BitSet FOLLOW_parameterset_in_value639 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_value641 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_valueset684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_valueset694 = new BitSet(new long[]{0x000001F810000FF0L});
    public static final BitSet FOLLOW_value_in_valueset698 = new BitSet(new long[]{0x0000000784000000L});
    public static final BitSet FOLLOW_operator_in_valueset707 = new BitSet(new long[]{0x000001F810000FF0L});
    public static final BitSet FOLLOW_value_in_valueset711 = new BitSet(new long[]{0x0000000784000000L});
    public static final BitSet FOLLOW_26_in_valueset717 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparator_in_parameter746 = new BitSet(new long[]{0x0000000000000FD0L});
    public static final BitSet FOLLOW_NULL_in_parameter758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_parameter768 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_parameter775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REAL_in_parameter784 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOLEAN_in_parameter793 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DATETIME_in_parameter801 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LITERAL_in_parameter809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_in_parameterset843 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_19_in_parameterset852 = new BitSet(new long[]{0x000001F800000FD0L});
    public static final BitSet FOLLOW_parameter_in_parameterset856 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_fieldName_in_column_order878 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ORDER_in_column_order880 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_operator898 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_operator907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_33_in_operator916 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_operator925 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_comparator942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_comparator951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_37_in_comparator960 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_38_in_comparator969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_39_in_comparator978 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_comparator987 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LITERAL_in_baName1007 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_baName1014 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LITERAL_in_fieldName1034 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_fieldName1041 = new BitSet(new long[]{0x0000000000000002L});

}