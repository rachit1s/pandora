// $ANTLR 3.3 Nov 30, 2010 12:50:56 /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g 2011-10-18 11:10:31

  package transbit.tbits.dql.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class DQLLexer extends Lexer {
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

    public DQLLexer() {;} 
    public DQLLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public DQLLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g"; }

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:11:7: ( 'SELECT' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:11:9: 'SELECT'
            {
            match("SELECT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:12:7: ( '*' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:12:9: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__18"

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:13:7: ( ',' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:13:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:14:7: ( 'FROM' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:14:9: 'FROM'
            {
            match("FROM"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:15:7: ( 'WHERE' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:15:9: 'WHERE'
            {
            match("WHERE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:16:7: ( 'HAS TEXT' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:16:9: 'HAS TEXT'
            {
            match("HAS TEXT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:17:7: ( 'ORDER BY' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:17:9: 'ORDER BY'
            {
            match("ORDER BY"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:18:7: ( 'LIMIT' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:18:9: 'LIMIT'
            {
            match("LIMIT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:19:7: ( '(' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:19:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:20:7: ( ')' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:20:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:21:7: ( ':' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:21:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:22:7: ( 'IN' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:22:9: 'IN'
            {
            match("IN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "T__29"
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:23:7: ( '{' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:23:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__29"

    // $ANTLR start "T__30"
    public final void mT__30() throws RecognitionException {
        try {
            int _type = T__30;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:24:7: ( '}' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:24:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__30"

    // $ANTLR start "T__31"
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:25:7: ( 'AND' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:25:9: 'AND'
            {
            match("AND"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__31"

    // $ANTLR start "T__32"
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:26:7: ( 'OR' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:26:9: 'OR'
            {
            match("OR"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__32"

    // $ANTLR start "T__33"
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:27:7: ( 'and' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:27:9: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__33"

    // $ANTLR start "T__34"
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:28:7: ( 'or' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:28:9: 'or'
            {
            match("or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__34"

    // $ANTLR start "T__35"
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:29:7: ( '=' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:29:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__35"

    // $ANTLR start "T__36"
    public final void mT__36() throws RecognitionException {
        try {
            int _type = T__36;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:30:7: ( '<>' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:30:9: '<>'
            {
            match("<>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__36"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:31:7: ( '<' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:31:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "T__38"
    public final void mT__38() throws RecognitionException {
        try {
            int _type = T__38;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:32:7: ( '<=' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:32:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "T__39"
    public final void mT__39() throws RecognitionException {
        try {
            int _type = T__39;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:33:7: ( '>' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:33:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__39"

    // $ANTLR start "T__40"
    public final void mT__40() throws RecognitionException {
        try {
            int _type = T__40;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:34:7: ( '>=' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:34:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__40"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:174:16: ( '0' .. '9' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:174:18: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:175:17: ( 'a' .. 'z' | 'A' .. 'Z' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "LETTER"

    // $ANTLR start "ORDER"
    public final void mORDER() throws RecognitionException {
        try {
            int _type = ORDER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:177:7: ( 'ASC' | 'DESC' )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='A') ) {
                alt1=1;
            }
            else if ( (LA1_0=='D') ) {
                alt1=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:177:9: 'ASC'
                    {
                    match("ASC"); 


                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:177:17: 'DESC'
                    {
                    match("DESC"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ORDER"

    // $ANTLR start "NULL"
    public final void mNULL() throws RecognitionException {
        try {
            int _type = NULL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:178:6: ( 'NULL' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:178:8: 'NULL'
            {
            match("NULL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NULL"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:179:5: ( 'NOT' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:179:7: 'NOT'
            {
            match("NOT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "BOOLEAN"
    public final void mBOOLEAN() throws RecognitionException {
        try {
            int _type = BOOLEAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:9: ( ( ( 'yes' | 'true' ) ) | ( ( 'no' | 'false' ) ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='t'||LA4_0=='y') ) {
                alt4=1;
            }
            else if ( (LA4_0=='f'||LA4_0=='n') ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:11: ( ( 'yes' | 'true' ) )
                    {
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:11: ( ( 'yes' | 'true' ) )
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:12: ( 'yes' | 'true' )
                    {
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:12: ( 'yes' | 'true' )
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0=='y') ) {
                        alt2=1;
                    }
                    else if ( (LA2_0=='t') ) {
                        alt2=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 2, 0, input);

                        throw nvae;
                    }
                    switch (alt2) {
                        case 1 :
                            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:13: 'yes'
                            {
                            match("yes"); 


                            }
                            break;
                        case 2 :
                            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:19: 'true'
                            {
                            match("true"); 


                            }
                            break;

                    }

                    setText("1");

                    }


                    }
                    break;
                case 2 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:46: ( ( 'no' | 'false' ) )
                    {
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:46: ( ( 'no' | 'false' ) )
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:47: ( 'no' | 'false' )
                    {
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:47: ( 'no' | 'false' )
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0=='n') ) {
                        alt3=1;
                    }
                    else if ( (LA3_0=='f') ) {
                        alt3=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 3, 0, input);

                        throw nvae;
                    }
                    switch (alt3) {
                        case 1 :
                            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:48: 'no'
                            {
                            match("no"); 


                            }
                            break;
                        case 2 :
                            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:180:53: 'false'
                            {
                            match("false"); 


                            }
                            break;

                    }

                    setText("0");

                    }


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BOOLEAN"

    // $ANTLR start "DATETIME"
    public final void mDATETIME() throws RecognitionException {
        try {
            int _type = DATETIME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:181:10: ( DIGIT ( DIGIT )? '/' DIGIT ( DIGIT )? '/' DIGIT DIGIT DIGIT DIGIT ( ( '+' | '-' ) ( DIGIT )+ ( 'm' | 'h' | 'd' | 'M' | 'y' ) )* )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:181:12: DIGIT ( DIGIT )? '/' DIGIT ( DIGIT )? '/' DIGIT DIGIT DIGIT DIGIT ( ( '+' | '-' ) ( DIGIT )+ ( 'm' | 'h' | 'd' | 'M' | 'y' ) )*
            {
            mDIGIT(); 
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:181:18: ( DIGIT )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:181:19: DIGIT
                    {
                    mDIGIT(); 

                    }
                    break;

            }

            match('/'); 
            mDIGIT(); 
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:181:37: ( DIGIT )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:181:38: DIGIT
                    {
                    mDIGIT(); 

                    }
                    break;

            }

            match('/'); 
            mDIGIT(); 
            mDIGIT(); 
            mDIGIT(); 
            mDIGIT(); 
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:181:74: ( ( '+' | '-' ) ( DIGIT )+ ( 'm' | 'h' | 'd' | 'M' | 'y' ) )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='+'||LA8_0=='-') ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:181:75: ( '+' | '-' ) ( DIGIT )+ ( 'm' | 'h' | 'd' | 'M' | 'y' )
            	    {
            	    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}

            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:181:85: ( DIGIT )+
            	    int cnt7=0;
            	    loop7:
            	    do {
            	        int alt7=2;
            	        int LA7_0 = input.LA(1);

            	        if ( ((LA7_0>='0' && LA7_0<='9')) ) {
            	            alt7=1;
            	        }


            	        switch (alt7) {
            	    	case 1 :
            	    	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:181:85: DIGIT
            	    	    {
            	    	    mDIGIT(); 

            	    	    }
            	    	    break;

            	    	default :
            	    	    if ( cnt7 >= 1 ) break loop7;
            	                EarlyExitException eee =
            	                    new EarlyExitException(7, input);
            	                throw eee;
            	        }
            	        cnt7++;
            	    } while (true);

            	    if ( input.LA(1)=='M'||input.LA(1)=='d'||input.LA(1)=='h'||input.LA(1)=='m'||input.LA(1)=='y' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DATETIME"

    // $ANTLR start "INTEGER"
    public final void mINTEGER() throws RecognitionException {
        try {
            int _type = INTEGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:183:9: ( ( DIGIT )+ )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:183:11: ( DIGIT )+
            {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:183:11: ( DIGIT )+
            int cnt9=0;
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>='0' && LA9_0<='9')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:183:11: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INTEGER"

    // $ANTLR start "REAL"
    public final void mREAL() throws RecognitionException {
        try {
            int _type = REAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:184:6: ( ( DIGIT )* '.' ( DIGIT )+ )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:184:8: ( DIGIT )* '.' ( DIGIT )+
            {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:184:8: ( DIGIT )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>='0' && LA10_0<='9')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:184:8: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            match('.'); 
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:184:19: ( DIGIT )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>='0' && LA11_0<='9')) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:184:19: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt11 >= 1 ) break loop11;
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "REAL"

    // $ANTLR start "LITERAL"
    public final void mLITERAL() throws RecognitionException {
        try {
            int _type = LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:185:9: ( ( LETTER | DIGIT | '_' | '%' | '.' | '*' | '\\'' | '+' | '-' | '?' )+ )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:185:11: ( LETTER | DIGIT | '_' | '%' | '.' | '*' | '\\'' | '+' | '-' | '?' )+
            {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:185:11: ( LETTER | DIGIT | '_' | '%' | '.' | '*' | '\\'' | '+' | '-' | '?' )+
            int cnt12=0;
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0=='%'||LA12_0=='\''||(LA12_0>='*' && LA12_0<='+')||(LA12_0>='-' && LA12_0<='.')||(LA12_0>='0' && LA12_0<='9')||LA12_0=='?'||(LA12_0>='A' && LA12_0<='Z')||LA12_0=='_'||(LA12_0>='a' && LA12_0<='z')) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:
            	    {
            	    if ( input.LA(1)=='%'||input.LA(1)=='\''||(input.LA(1)>='*' && input.LA(1)<='+')||(input.LA(1)>='-' && input.LA(1)<='.')||(input.LA(1)>='0' && input.LA(1)<='9')||input.LA(1)=='?'||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt12 >= 1 ) break loop12;
                        EarlyExitException eee =
                            new EarlyExitException(12, input);
                        throw eee;
                }
                cnt12++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LITERAL"

    // $ANTLR start "ESCAPE"
    public final void mESCAPE() throws RecognitionException {
        try {
            int _type = ESCAPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:187:8: ( '\\\\' ( '\\\\\\\\' )* )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:187:15: '\\\\' ( '\\\\\\\\' )*
            {
            String str = "";
            match('\\'); 
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:189:4: ( '\\\\\\\\' )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0=='\\') ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:189:5: '\\\\\\\\'
            	    {
            	    match("\\\\"); 

            	    str += "\\";

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);

            setText(str);

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ESCAPE"

    // $ANTLR start "STRING_LITERAL"
    public final void mSTRING_LITERAL() throws RecognitionException {
        try {
            int _type = STRING_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            CommonToken ESCAPE1=null;
            int c;

            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:192:15: ( '\"' ( ESCAPE '\"' | c=~ ( '\\\\' | '\"' | '\\n' | '\\f' | '\\r' ) )* '\"' )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:192:26: '\"' ( ESCAPE '\"' | c=~ ( '\\\\' | '\"' | '\\n' | '\\f' | '\\r' ) )* '\"'
            {
            StringBuilder b = new StringBuilder();
            match('\"'); 
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:193:6: ( ESCAPE '\"' | c=~ ( '\\\\' | '\"' | '\\n' | '\\f' | '\\r' ) )*
            loop14:
            do {
                int alt14=3;
                int LA14_0 = input.LA(1);

                if ( (LA14_0=='\\') ) {
                    alt14=1;
                }
                else if ( ((LA14_0>='\u0000' && LA14_0<='\t')||LA14_0=='\u000B'||(LA14_0>='\u000E' && LA14_0<='!')||(LA14_0>='#' && LA14_0<='[')||(LA14_0>=']' && LA14_0<='\uFFFF')) ) {
                    alt14=2;
                }


                switch (alt14) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:194:3: ESCAPE '\"'
            	    {
            	    int ESCAPE1Start511 = getCharIndex();
            	    int ESCAPE1StartLine511 = getLine();
            	    int ESCAPE1StartCharPos511 = getCharPositionInLine();
            	    mESCAPE(); 
            	    ESCAPE1 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ESCAPE1Start511, getCharIndex()-1);
            	    ESCAPE1.setLine(ESCAPE1StartLine511);
            	    ESCAPE1.setCharPositionInLine(ESCAPE1StartCharPos511);
            	    match('\"'); 
            	    b.append((ESCAPE1!=null?ESCAPE1.getText():null)); b.appendCodePoint('"');

            	    }
            	    break;
            	case 2 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:195:5: c=~ ( '\\\\' | '\"' | '\\n' | '\\f' | '\\r' )
            	    {
            	    c= input.LA(1);
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||input.LA(1)=='\u000B'||(input.LA(1)>='\u000E' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}

            	    b.appendCodePoint(c);

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            match('\"'); 
            setText(b.toString());

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING_LITERAL"

    // $ANTLR start "WS_OTHERS"
    public final void mWS_OTHERS() throws RecognitionException {
        try {
            int _type = WS_OTHERS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:199:11: ( ( ' ' | '\\t' | '\\n' | '\\f' | '\\r' )+ )
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:199:13: ( ' ' | '\\t' | '\\n' | '\\f' | '\\r' )+
            {
            // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:199:13: ( ' ' | '\\t' | '\\n' | '\\f' | '\\r' )+
            int cnt15=0;
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( ((LA15_0>='\t' && LA15_0<='\n')||(LA15_0>='\f' && LA15_0<='\r')||LA15_0==' ') ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt15 >= 1 ) break loop15;
                        EarlyExitException eee =
                            new EarlyExitException(15, input);
                        throw eee;
                }
                cnt15++;
            } while (true);

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS_OTHERS"

    public void mTokens() throws RecognitionException {
        // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:8: ( T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | ORDER | NULL | NOT | BOOLEAN | DATETIME | INTEGER | REAL | LITERAL | ESCAPE | STRING_LITERAL | WS_OTHERS )
        int alt16=35;
        alt16 = dfa16.predict(input);
        switch (alt16) {
            case 1 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:10: T__17
                {
                mT__17(); 

                }
                break;
            case 2 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:16: T__18
                {
                mT__18(); 

                }
                break;
            case 3 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:22: T__19
                {
                mT__19(); 

                }
                break;
            case 4 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:28: T__20
                {
                mT__20(); 

                }
                break;
            case 5 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:34: T__21
                {
                mT__21(); 

                }
                break;
            case 6 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:40: T__22
                {
                mT__22(); 

                }
                break;
            case 7 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:46: T__23
                {
                mT__23(); 

                }
                break;
            case 8 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:52: T__24
                {
                mT__24(); 

                }
                break;
            case 9 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:58: T__25
                {
                mT__25(); 

                }
                break;
            case 10 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:64: T__26
                {
                mT__26(); 

                }
                break;
            case 11 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:70: T__27
                {
                mT__27(); 

                }
                break;
            case 12 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:76: T__28
                {
                mT__28(); 

                }
                break;
            case 13 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:82: T__29
                {
                mT__29(); 

                }
                break;
            case 14 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:88: T__30
                {
                mT__30(); 

                }
                break;
            case 15 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:94: T__31
                {
                mT__31(); 

                }
                break;
            case 16 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:100: T__32
                {
                mT__32(); 

                }
                break;
            case 17 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:106: T__33
                {
                mT__33(); 

                }
                break;
            case 18 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:112: T__34
                {
                mT__34(); 

                }
                break;
            case 19 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:118: T__35
                {
                mT__35(); 

                }
                break;
            case 20 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:124: T__36
                {
                mT__36(); 

                }
                break;
            case 21 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:130: T__37
                {
                mT__37(); 

                }
                break;
            case 22 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:136: T__38
                {
                mT__38(); 

                }
                break;
            case 23 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:142: T__39
                {
                mT__39(); 

                }
                break;
            case 24 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:148: T__40
                {
                mT__40(); 

                }
                break;
            case 25 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:154: ORDER
                {
                mORDER(); 

                }
                break;
            case 26 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:160: NULL
                {
                mNULL(); 

                }
                break;
            case 27 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:165: NOT
                {
                mNOT(); 

                }
                break;
            case 28 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:169: BOOLEAN
                {
                mBOOLEAN(); 

                }
                break;
            case 29 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:177: DATETIME
                {
                mDATETIME(); 

                }
                break;
            case 30 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:186: INTEGER
                {
                mINTEGER(); 

                }
                break;
            case 31 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:194: REAL
                {
                mREAL(); 

                }
                break;
            case 32 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:199: LITERAL
                {
                mLITERAL(); 

                }
                break;
            case 33 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:207: ESCAPE
                {
                mESCAPE(); 

                }
                break;
            case 34 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:214: STRING_LITERAL
                {
                mSTRING_LITERAL(); 

                }
                break;
            case 35 :
                // /home/karan/workspace/trunk/src/java/transbit/tbits/dql/antlr/DQL.g:1:229: WS_OTHERS
                {
                mWS_OTHERS(); 

                }
                break;

        }

    }


    protected DFA16 dfa16 = new DFA16(this);
    static final String DFA16_eotS =
        "\1\uffff\1\35\1\42\1\uffff\5\35\3\uffff\1\35\2\uffff\3\35\1\uffff"+
        "\1\57\1\61\6\35\1\71\1\35\4\uffff\1\35\1\uffff\3\35\1\102\1\35\1"+
        "\104\3\35\1\110\5\uffff\5\35\1\116\1\35\1\uffff\1\71\1\uffff\1\121"+
        "\5\35\1\uffff\1\35\1\uffff\1\130\1\131\1\132\1\uffff\2\35\1\135"+
        "\1\116\1\35\1\uffff\1\35\1\71\1\uffff\1\35\1\141\1\35\1\uffff\2"+
        "\35\3\uffff\1\131\1\145\1\uffff\1\116\2\35\1\uffff\1\150\1\35\1"+
        "\152\1\uffff\1\116\1\153\4\uffff";
    static final String DFA16_eofS =
        "\154\uffff";
    static final String DFA16_minS =
        "\1\11\1\105\1\45\1\uffff\1\122\1\110\1\101\1\122\1\111\3\uffff\1"+
        "\116\2\uffff\1\116\1\156\1\162\1\uffff\2\75\1\105\1\117\1\145\1"+
        "\162\1\157\1\141\1\45\1\60\4\uffff\1\114\1\uffff\1\117\1\105\1\123"+
        "\1\45\1\115\1\45\1\104\1\103\1\144\1\45\5\uffff\1\123\1\114\1\124"+
        "\1\163\1\165\1\45\1\154\1\uffff\1\45\1\uffff\1\45\1\105\1\115\1"+
        "\122\1\40\1\105\1\uffff\1\111\1\uffff\3\45\1\uffff\1\103\1\114\2"+
        "\45\1\145\1\uffff\1\163\1\45\1\uffff\1\103\1\45\1\105\1\uffff\1"+
        "\122\1\124\3\uffff\2\45\1\uffff\1\45\1\145\1\124\1\uffff\1\45\1"+
        "\40\1\45\1\uffff\2\45\4\uffff";
    static final String DFA16_maxS =
        "\1\175\1\105\1\172\1\uffff\1\122\1\110\1\101\1\122\1\111\3\uffff"+
        "\1\116\2\uffff\1\123\1\156\1\162\1\uffff\1\76\1\75\1\105\1\125\1"+
        "\145\1\162\1\157\1\141\1\172\1\71\4\uffff\1\114\1\uffff\1\117\1"+
        "\105\1\123\1\172\1\115\1\172\1\104\1\103\1\144\1\172\5\uffff\1\123"+
        "\1\114\1\124\1\163\1\165\1\172\1\154\1\uffff\1\172\1\uffff\1\172"+
        "\1\105\1\115\1\122\1\40\1\105\1\uffff\1\111\1\uffff\3\172\1\uffff"+
        "\1\103\1\114\2\172\1\145\1\uffff\1\163\1\172\1\uffff\1\103\1\172"+
        "\1\105\1\uffff\1\122\1\124\3\uffff\2\172\1\uffff\1\172\1\145\1\124"+
        "\1\uffff\1\172\1\40\1\172\1\uffff\2\172\4\uffff";
    static final String DFA16_acceptS =
        "\3\uffff\1\3\5\uffff\1\11\1\12\1\13\1\uffff\1\15\1\16\3\uffff\1"+
        "\23\12\uffff\1\40\1\41\1\42\1\43\1\uffff\1\2\12\uffff\1\24\1\26"+
        "\1\25\1\30\1\27\7\uffff\1\36\1\uffff\1\35\6\uffff\1\20\1\uffff\1"+
        "\14\3\uffff\1\22\5\uffff\1\34\2\uffff\1\37\3\uffff\1\6\2\uffff\1"+
        "\17\1\31\1\21\2\uffff\1\33\3\uffff\1\4\3\uffff\1\32\2\uffff\1\5"+
        "\1\7\1\10\1\1";
    static final String DFA16_specialS =
        "\154\uffff}>";
    static final String[] DFA16_transitionS = {
            "\2\40\1\uffff\2\40\22\uffff\1\40\1\uffff\1\37\2\uffff\1\35\1"+
            "\uffff\1\35\1\11\1\12\1\2\1\35\1\3\1\35\1\34\1\uffff\12\33\1"+
            "\13\1\uffff\1\23\1\22\1\24\1\35\1\uffff\1\17\2\35\1\25\1\35"+
            "\1\4\1\35\1\6\1\14\2\35\1\10\1\35\1\26\1\7\3\35\1\1\3\35\1\5"+
            "\3\35\1\uffff\1\36\2\uffff\1\35\1\uffff\1\20\4\35\1\32\7\35"+
            "\1\31\1\21\4\35\1\30\4\35\1\27\1\35\1\15\1\uffff\1\16",
            "\1\41",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "\1\43",
            "\1\44",
            "\1\45",
            "\1\46",
            "\1\47",
            "",
            "",
            "",
            "\1\50",
            "",
            "",
            "\1\51\4\uffff\1\52",
            "\1\53",
            "\1\54",
            "",
            "\1\56\1\55",
            "\1\60",
            "\1\62",
            "\1\64\5\uffff\1\63",
            "\1\65",
            "\1\66",
            "\1\67",
            "\1\70",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\1\35\1\34\1\73\12\72"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\12\74",
            "",
            "",
            "",
            "",
            "\1\75",
            "",
            "\1\76",
            "\1\77",
            "\1\100",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\3\35\1\101\26\35\4\uffff\1\35\1\uffff"+
            "\32\35",
            "\1\103",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\105",
            "\1\106",
            "\1\107",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "",
            "",
            "",
            "",
            "\1\111",
            "\1\112",
            "\1\113",
            "\1\114",
            "\1\115",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\117",
            "",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\1\35\1\34\1\73\12\120"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\74"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\122",
            "\1\123",
            "\1\124",
            "\1\125",
            "\1\126",
            "",
            "\1\127",
            "",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "\1\133",
            "\1\134",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\136",
            "",
            "\1\137",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\1\35\1\34\1\uffff\12"+
            "\120\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "\1\140",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\142",
            "",
            "\1\143",
            "\1\144",
            "",
            "",
            "",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\146",
            "\1\147",
            "",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\151",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "\1\35\1\uffff\1\35\2\uffff\2\35\1\uffff\2\35\1\uffff\12\35"+
            "\5\uffff\1\35\1\uffff\32\35\4\uffff\1\35\1\uffff\32\35",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
    static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
    static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
    static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
    static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
    static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
    static final short[][] DFA16_transition;

    static {
        int numStates = DFA16_transitionS.length;
        DFA16_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
        }
    }

    class DFA16 extends DFA {

        public DFA16(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 16;
            this.eot = DFA16_eot;
            this.eof = DFA16_eof;
            this.min = DFA16_min;
            this.max = DFA16_max;
            this.accept = DFA16_accept;
            this.special = DFA16_special;
            this.transition = DFA16_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | ORDER | NULL | NOT | BOOLEAN | DATETIME | INTEGER | REAL | LITERAL | ESCAPE | STRING_LITERAL | WS_OTHERS );";
        }
    }
 

}