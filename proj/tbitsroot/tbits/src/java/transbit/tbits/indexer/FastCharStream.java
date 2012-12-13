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
 * FastCharStream.java
 *
 *
 */
package transbit.tbits.indexer;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Reader;

//~--- classes ----------------------------------------------------------------

/**
 * An efficient implementation of JavaCC's CharStream interface.
 * <p>
 * Note that this does not do line-number counting, but instead keeps track of
 * the character position of the token in the input, as required by Lucene's
 * {@link org.apache.lucene.analysis.Token} API.
 */
public final class FastCharStream implements CharStream {
    char[] buffer         = null;
    int    bufferLength   = 0;    // end of valid chars
    int    bufferPosition = 0;    // next char to read
    int    tokenStart     = 0;    // offset in buffer
    int    bufferStart    = 0;    // position in file of buffer
    Reader input;                 // source of chars

    //~--- constructors -------------------------------------------------------

    /** Constructs from a Reader. */
    public FastCharStream(Reader r) {
        input = r;
    }

    //~--- methods ------------------------------------------------------------

    public final char BeginToken() throws IOException {
        tokenStart = bufferPosition;

        return readChar();
    }

    public final void Done() {
        try {
            input.close();
        } catch (IOException e) {
            System.err.println("Caught: " + e + "; ignoring.");
        }
    }

    public final String GetImage() {
        return new String(buffer, tokenStart, bufferPosition - tokenStart);
    }

    public final char[] GetSuffix(int len) {
        char[] value = new char[len];

        System.arraycopy(buffer, bufferPosition - len, value, 0, len);

        return value;
    }

    public final void backup(int amount) {
        bufferPosition -= amount;
    }

    public final char readChar() throws IOException {
        if (bufferPosition >= bufferLength) {
            refill();
        }

        return buffer[bufferPosition++];
    }

    private final void refill() throws IOException {
        int newPosition = bufferLength - tokenStart;

        if (tokenStart == 0) {                             // token won't fit in buffer
            if (buffer == null) {                          // first time: alloc buffer
                buffer = new char[2048];
            } else if (bufferLength == buffer.length) {    // grow buffer
                char[] newBuffer = new char[buffer.length * 2];

                System.arraycopy(buffer, 0, newBuffer, 0, bufferLength);
                buffer = newBuffer;
            }
        } else {                                           // shift token to front
            System.arraycopy(buffer, tokenStart, buffer, 0, newPosition);
        }

        bufferLength   = newPosition;    // update state
        bufferPosition = newPosition;
        bufferStart    += tokenStart;
        tokenStart     = 0;

        int charsRead =    // fill space in buffer
            input.read(buffer, newPosition, buffer.length - newPosition);

        if (charsRead == -1) {
            throw new IOException("read past eof");
        } else {
            bufferLength += charsRead;
        }
    }

    //~--- get methods --------------------------------------------------------

    public final int getBeginColumn() {
        return bufferStart + tokenStart;
    }

    public final int getBeginLine() {
        return 1;
    }

    public final int getColumn() {
        return bufferStart + bufferPosition;
    }

    public final int getEndColumn() {
        return bufferStart + bufferPosition;
    }

    public final int getEndLine() {
        return 1;
    }

    public final int getLine() {
        return 1;
    }
}
