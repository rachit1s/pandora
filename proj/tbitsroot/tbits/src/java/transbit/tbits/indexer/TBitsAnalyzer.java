/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



/**
 * TBitsAnalyzer.java
 *
 *
 */
package transbit.tbits.indexer;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

//TBits Imports
import transbit.tbits.common.TBitsLogger;

import static transbit.tbits.Helper.TBitsConstants.PKG_INDEXER;

//~--- JDK imports ------------------------------------------------------------

//lucene imports
import java.io.Reader;

//~--- classes ----------------------------------------------------------------

/**
 * Analyzer that will be used during indexing and searching.
 *
 *
 * @author : Vaibhav.
 * @version : $Id: $
 */
public class TBitsAnalyzer extends Analyzer {

    // Name of the logger.
    public static final String LOGGER_NAME = "indexer";

    // Application Logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(LOGGER_NAME, PKG_INDEXER);

    //~--- methods ------------------------------------------------------------

    /**
     * This method tokenizes the data present in the reader. Currently
     * the data is passed through the following filters:
     *  1. Whitespace tokenizer
     *  2. Lowercase filter.
     *  3. PorterStem filter.
     *  4. Stop filter.
     *
     */
    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream ts = null;

        ts = new TBitsTokenizer(reader);
        ts = new LowerCaseFilter(ts);
        ts = new StopFilter(ts, StandardAnalyzer.STOP_WORDS);
       // ts = new PorterStemFilter(ts);

        return ts;
    }
}
