//
//  CSVReader.java
//  Project waoapp
//
//  Created by ktsubaki on Wed Jan 16 2002.
//  Copyright (c) 2002 ArtesWAre. All rights reserved.
//

package workflow.csv;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.ArrayList;

import workflow.util.Logging;


/**
 *
 * @author  Kazuo Tsubaki
 * @version 1.1
 * @see CSVConsumer
 */

public final class CSVReader {

    private static final char DoubleQuote = 34;
    private static final char Comma = 44;
    private static final char CR = 13;
    private static final char LF = 10;
    private static final char Space = 32;
    private static final char Tab = 9;
    private static final int StateFirstLine = 1;
    private static final int StateBeginningOfLine = 2;
    private static final int StateBeginningOfField = 3;
    private static final int StateEndOfField = 4;
    private static final int StateInUnquotedField = 5;
    private static final int StateInQuotedField = 6;
    private static final int StateEOF = 7;
    static final int TokenBufferSize = 8192;
    private CSVConsumer _csvConsumer;
    private String _encoding;

    /**
     * 
     */
    public CSVReader(CSVConsumer csvConsumer) {
        _csvConsumer = csvConsumer;
    }

    /**
     */
    public String encoding() {
        return _encoding;
    }
    
    /**
     */
    public BufferedReader bufferedReader(File file, String encoding) 
    throws IOException
    {
        return bufferedReader(((InputStream) (new FileInputStream(file.getPath()))), encoding);
    }
    
    /**
     */
    public BufferedReader bufferedReader(InputStream is, String encoding)
        throws IOException
    {
        return new BufferedReader(new InputStreamReader(is, encoding));
    }

    /**
     */
    public void read(URL url, String defaultEncoding) throws IOException {
        URLConnection urlConn = url.openConnection();
        _encoding = urlConn.getContentEncoding();
        if(_encoding == null)
            _encoding = defaultEncoding;
        Reader in = bufferedReader(url.openStream(), _encoding);
        read(in, url.toString());
        in.close();
    }
    
    /**
     */
    public void read(File file, String encoding) throws IOException {
        Reader in =bufferedReader(file, encoding);
        read(in, file.getCanonicalPath());
        in.close();
    }

    /**
     */
    public void read(InputStream inputStream, String encoding) throws IOException {
        Reader in =bufferedReader(inputStream, encoding);
        read(in, null);
        in.close();
    }

    /**
     */
    public final void read(Reader reader, String location) throws IOException
    {
        int next = 0;
        int lineNumber = 1;
        int ch = 0;
        int state = StateFirstLine;
        List<String> tokens = new ArrayList<String>();
        char token[] = new char[TokenBufferSize];
        boolean currentTokenIsQuoted = false;
        if(!(reader instanceof BufferedReader))
            reader = new BufferedReader(reader);
        try {
            while(state != StateEOF) 
                switch(state) {
                case StateFirstLine:
                    ch = reader.read();
                    state = StateBeginningOfField;
                    break;

                case StateBeginningOfLine: //
                    _csvConsumer.consumeLineOfTokens(location, lineNumber - 1, tokens);
                    tokens = new ArrayList<String>();
                    state = StateBeginningOfField;
                    break;

                case StateBeginningOfField:
                    for(; ch == Space || ch == Tab; ch = reader.read());
                    if(ch == DoubleQuote) {
                        state = StateInQuotedField;
                        currentTokenIsQuoted = true;
                        ch = reader.read();
                    } else {
                        state = StateInUnquotedField;
                        currentTokenIsQuoted = false;
                    }
                    break;

                case StateEndOfField:
                    String currentToken = new String(token, 0, next);
                    if(!currentTokenIsQuoted)
                        currentToken = currentToken.trim();
                    for(; ch == Space || ch == Tab; ch = reader.read());
                    tokens.add(currentToken);
                    next = 0;
                    if(ch == Comma) {
                        state = StateBeginningOfField;
                        ch = reader.read();
                    } else {
                        if(ch == LF || ch == CR) {
                            state = StateBeginningOfLine;
                            if(ch == LF)
                                lineNumber++;
                            while(ch == LF || ch == CR)  {
                                ch = reader.read();
                                if(ch == LF)
                                    lineNumber++;
                            }
                            if(ch == -1)
                                state = StateEOF;
                        } else {
                            if(ch == -1) {
                                state = StateEOF;
                                lineNumber++;
                            } else {
                                Logging.custom.warn("Warning at line " + lineNumber, null);
                                state = StateBeginningOfField;
                            }
                        }
                    }
                    break;

                case StateInUnquotedField:
                    for(; ch >= 0 && ch != Comma && ch != CR && ch != LF; ch = reader.read())
                        if(next < TokenBufferSize)
                            token[next++] = (char)ch;

                    state = StateEndOfField;
                    break;

                case StateInQuotedField:
                    while(state == StateInQuotedField)  {
                        while(ch >= 0 && ch != DoubleQuote)  {
                            if(ch != CR && next < TokenBufferSize)
                                token[next++] = (char)ch;
                            ch = reader.read();
                        }
                        if(ch == DoubleQuote) {
                            ch = reader.read();
                            if(ch != DoubleQuote)
                                break;
                            if(next < TokenBufferSize)
                                token[next++] = (char)ch;
                            ch = reader.read();
                            continue;
                        }
                        Logging.custom.warn("Warning at line " + lineNumber, null);
                        break;
                    }
                    state = StateEndOfField;
                    break;

                default:
                    state = StateEOF;
                    break;
                }
            if(!tokens.isEmpty())
                _csvConsumer.consumeLineOfTokens(location, lineNumber - 1, tokens);
        } catch (Exception exception) {
            throw new IOException("Error during read csv. Original exception was: " + exception.getClass().getName() + " " + exception.getMessage());
        }
        finally {
            reader.close();
        }
    }
}
