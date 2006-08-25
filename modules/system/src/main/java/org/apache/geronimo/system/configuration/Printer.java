/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * This code has been borrowed from the Apache Xerces project. We're copying the code to
 * keep from adding a dependency on Xerces in the Geronimo kernel.
 */

package org.apache.geronimo.system.configuration;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;


/**
 * The printer is responsible for sending text to the output stream
 * or writer. This class performs direct writing for efficiency.
 * {@link IndentPrinter} supports indentation and line wrapping by
 * extending this class.
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 */
public class Printer
{


    /**
     * The output format associated with this serializer. This will never
     * be a null reference. If no format was passed to the constructor,
     * the default one for this document type will be used. The format
     * object is never changed by the serializer.
     */
    protected final OutputFormat format;


    /**
     * The writer to which the document is written.
     */
    protected Writer             writer;


    /**
     * The DTD writer. When we switch to DTD mode, all output is
     * accumulated in this DTD writer. When we switch out of it,
     * the output is obtained as a string. Must not be reset to
     * null until we're done with the document.
     */
    protected StringWriter       dtdWriter;


    /**
     * Holds a reference to the document writer while we are
     * in DTD mode.
     */
    protected Writer          docWriter;


    /**
     * Holds the exception thrown by the serializer.  Exceptions do not cause
     * the serializer to quit, but are held and one is thrown at the end.
     */
    protected IOException     exception;


    /**
     * The size of the output buffer.
     */
    private static final int BufferSize = 4096;


    /**
     * Output buffer.
     */
    private final char[]  buffer = new char[ BufferSize ];


    /**
     * Position within the output buffer.
     */
    private int           pos = 0;


    public Printer( Writer writer, OutputFormat format)
    {
        this.writer = writer;
        this.format = format;
        exception = null;
        dtdWriter = null;
        docWriter = null;
        pos = 0;
    }


    public IOException getException()
    {
        return exception;
    }


    /**
     * Called by any of the DTD handlers to enter DTD mode.
     * Once entered, all output will be accumulated in a string
     * that can be printed as part of the document's DTD.
     * This method may be called any number of time but will only
     * have affect the first time it's called. To exist DTD state
     * and get the accumulated DTD, call {@link #leaveDTD}.
     */
    public void enterDTD()
        throws IOException
    {
        // Can only enter DTD state once. Once we're out of DTD
        // state, can no longer re-enter it.
        if ( dtdWriter == null ) {
        flushLine( false );

            dtdWriter = new StringWriter();
            docWriter = writer;
            writer = dtdWriter;
        }
    }


    /**
     * Called by the root element to leave DTD mode and if any
     * DTD parts were printer, will return a string with their
     * textual content.
     */
    public String leaveDTD()
        throws IOException
    {
        // Only works if we're going out of DTD mode.
        if ( writer == dtdWriter ) {
        flushLine( false );

            writer = docWriter;
            return dtdWriter.toString();
        } else
            return null;
    }


    public void printText( String text )
        throws IOException
    {
        try {
            int length = text.length();
            for ( int i = 0 ; i < length ; ++i ) {
                if ( pos == BufferSize ) {
                    writer.write( buffer );
                    pos = 0;
                }
                buffer[ pos ] = text.charAt( i );
                ++pos;
            }
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( exception == null )
                exception = except;
            throw except;
        }
    }


    public void printText( StringBuffer text )
        throws IOException
    {
        try {
            int length = text.length();
            for ( int i = 0 ; i < length ; ++i ) {
                if ( pos == BufferSize ) {
                    writer.write( buffer );
                    pos = 0;
                }
                buffer[ pos ] = text.charAt( i );
                ++pos;
            }
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( exception == null )
                exception = except;
            throw except;
        }
    }


    public void printText( char[] chars, int start, int length )
        throws IOException
    {
        try {
            while ( length-- > 0 ) {
                if ( pos == BufferSize ) {
                    writer.write( buffer );
                    pos = 0;
                }
                buffer[ pos ] = chars[ start ];
                ++start;
                ++pos;
            }
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( exception == null )
                exception = except;
            throw except;
        }
    }


    public void printText( char ch )
        throws IOException
    {
        try {
            if ( pos == BufferSize ) {
                writer.write( buffer );
                pos = 0;
            }
            buffer[ pos ] = ch;
            ++pos;
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( exception == null )
                exception = except;
            throw except;
        }
    }


    public void printSpace()
        throws IOException
    {
        try {
            if ( pos == BufferSize ) {
                writer.write( buffer );
                pos = 0;
            }
            buffer[ pos ] = ' ';
            ++pos;
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( exception == null )
                exception = except;
            throw except;
        }
    }


    public void breakLine()
        throws IOException
    {
        try {
            if ( pos == BufferSize ) {
                writer.write( buffer );
                pos = 0;
            }
            buffer[ pos ] = '\n';
            ++pos;
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( exception == null )
                exception = except;
            throw except;
        }
    }


    public void breakLine( boolean preserveSpace )
        throws IOException
    {
        breakLine();
    }


    public void flushLine( boolean preserveSpace )
        throws IOException
    {
        // Write anything left in the buffer into the writer.
        try {
            writer.write( buffer, 0, pos );
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( exception == null )
                exception = except;
        }
        pos = 0;
    }


    /**
     * Flush the output stream. Must be called when done printing
     * the document, otherwise some text might be buffered.
     */
    public void flush()
        throws IOException
    {
        try {
            writer.write( buffer, 0, pos );
            writer.flush();
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( exception == null )
                exception = except;
            throw except;
        }
        pos = 0;
    }


    public void indent()
    {
        // NOOP
    }


    public void unindent()
    {
        // NOOP
    }


    public int getNextIndent()
    {
        return 0;
    }


    public void setNextIndent( int indent )
    {
    }


    public void setThisIndent( int indent )
    {
    }


}
