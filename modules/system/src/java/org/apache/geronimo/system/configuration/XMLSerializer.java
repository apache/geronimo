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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Enumeration;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * Implements an XML serializer supporting both DOM and SAX pretty
 * serializing. For usage instructions see {@link Serializer}.
 * <p>
 * If an output stream is used, the encoding is taken from the
 * output format (defaults to <tt>UTF-8</tt>). If a writer is
 * used, make sure the writer uses the same encoding (if applies)
 * as specified in the output format.
 * <p>
 * The serializer supports both DOM and SAX. DOM serializing is done
 * by calling {@link #serialize} and SAX serializing is done by firing
 * SAX events and using the serializer as a document handler.
 * <p>
 * If an I/O exception occurs while serializing, the serializer
 * will not throw an exception directly, but only throw it
 * at the end of serializing (either DOM or SAX's {@link
 * org.xml.sax.DocumentHandler#endDocument}.
 * <p>
 * For elements that are not specified as whitespace preserving,
 * the serializer will potentially break long text lines at space
 * boundaries, indent lines, and serialize elements on separate
 * lines. Line terminators will be regarded as spaces, and
 * spaces at beginning of line will be stripped.
 *
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 * @see Serializer
 */
public class XMLSerializer extends BaseMarkupSerializer
{

    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public XMLSerializer()
    {
        super( new OutputFormat( Method.XML, null, false ) );
    }


    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public XMLSerializer( OutputFormat format )
    {
        super( format != null ? format : new OutputFormat( Method.XML, null, false ) );
        this.format.setMethod( Method.XML );
    }


    /**
     * Constructs a new serializer that writes to the specified writer
     * using the specified output format. If <tt>format</tt> is null,
     * will use a default output format.
     *
     * @param writer The writer to use
     * @param format The output format to use, null for the default
     */
    public XMLSerializer( Writer writer, OutputFormat format )
    {
        super( format != null ? format : new OutputFormat( Method.XML, null, false ) );
        this.format.setMethod( Method.XML );
        setOutputCharStream( writer );
    }


    /**
     * Constructs a new serializer that writes to the specified output
     * stream using the specified output format. If <tt>format</tt>
     * is null, will use a default output format.
     *
     * @param output The output stream to use
     * @param format The output format to use, null for the default
     */
    public XMLSerializer( OutputStream output, OutputFormat format )
    {
        super( format != null ? format : new OutputFormat( Method.XML, null, false ) );
        this.format.setMethod( Method.XML );
        setOutputByteStream( output );
    }


    public void setOutputFormat( OutputFormat format )
    {
        super.setOutputFormat( format != null ? format : new OutputFormat( Method.XML, null, false ) );
    }


    //-----------------------------------------//
    // SAX content handler serializing methods //
    //-----------------------------------------//


    public void startElement( String namespaceURI, String localName,
                              String rawName, Attributes attrs )
        throws SAXException
    {
        int          i;
        boolean      preserveSpace;
        ElementState state;
        String       name;
        String       value;

        try {
        if ( printer == null )
            throw new IllegalStateException( "SER002 No writer supplied for serializer" );

        state = getElementState();
        if ( isDocumentState() ) {
            // If this is the root element handle it differently.
            // If the first root element in the document, serialize
            // the document's DOCTYPE. Space preserving defaults
            // to that of the output format.
            if ( ! started )
                    startDocument( ( localName == null || localName.length() == 0 ) ? rawName : localName );
        } else {
            // For any other element, if first in parent, then
            // close parent's opening tag and use the parnet's
            // space preserving.
            if ( state.empty )
                printer.printText( '>' );
            // Must leave CData section first
            if ( state.inCData )
            {
                printer.printText( "]]>" );
                state.inCData = false;
            }
            // Indent this element on a new line if the first
            // content of the parent element or immediately
            // following an element or a comment
            if ( indenting && ! state.preserveSpace &&
                 ( state.empty || state.afterElement || state.afterComment) )
                printer.breakLine();
        }
        preserveSpace = state.preserveSpace;

            //We remove the namespaces from the attributes list so that they will
            //be in _prefixes
            attrs = extractNamespaces(attrs);

        // Do not change the current element state yet.
        // This only happens in endElement().
            if ( rawName == null || rawName.length() == 0 ) {
                if ( localName == null )
                    throw new SAXException( "No rawName and localName is null" );
                if ( namespaceURI != null && ! namespaceURI.equals( "" ) ) {
                String prefix;
                prefix = getPrefix( namespaceURI );
                    if ( prefix != null && prefix.length() > 0 )
                    rawName = prefix + ":" + localName;
                    else
                        rawName = localName;
                } else
                    rawName = localName;
        }

        printer.printText( '<' );
        printer.printText( rawName );
        printer.indent();

        // For each attribute print it's name and value as one part,
        // separated with a space so the element can be broken on
        // multiple lines.
        if ( attrs != null ) {
            for ( i = 0 ; i < attrs.getLength() ; ++i ) {
                printer.printSpace();

                name = attrs.getQName( i );
                    if ( name != null && name.length() == 0 ) {
                    String prefix;
                    String attrURI;

                    name = attrs.getLocalName( i );
                    attrURI = attrs.getURI( i );
                        if ( ( attrURI != null && attrURI.length() != 0 ) &&
                             ( namespaceURI == null || namespaceURI.length() == 0 ||
                                              ! attrURI.equals( namespaceURI ) ) ) {
                        prefix = getPrefix( attrURI );
                        if ( prefix != null && prefix.length() > 0 )
                            name = prefix + ":" + name;
                    }
                }

                value = attrs.getValue( i );
                if ( value == null )
                    value = "";
                printer.printText( name );
                printer.printText( "=\"" );
                printEscaped( value );
                printer.printText( '"' );

                // If the attribute xml:space exists, determine whether
                // to preserve spaces in this and child nodes based on
                // its value.
                if ( name.equals( "xml:space" ) ) {
                    if ( value.equals( "preserve" ) )
                        preserveSpace = true;
                    else
                        preserveSpace = format.getPreserveSpace();
                }
            }
        }

            if ( prefixes != null ) {
            Enumeration keyEnum; 

            keyEnum = prefixes.keys();
            while ( keyEnum.hasMoreElements() ) {
                printer.printSpace();
                value = (String) keyEnum.nextElement();
                name = (String) prefixes.get( value );
                if ( name.length() == 0 ) {
                    printer.printText( "xmlns=\"" );
                    printEscaped( value );
                    printer.printText( '"' );
                } else {
                    printer.printText( "xmlns:" );
                    printer.printText( name );
                    printer.printText( "=\"" );
                    printEscaped( value );
                    printer.printText( '"' );
                }
            }
        }

        // Now it's time to enter a new element state
        // with the tag name and space preserving.
        // We still do not change the curent element state.
        state = enterElementState( namespaceURI, localName, rawName, preserveSpace );
            name = ( localName == null || localName.length() == 0 ) ? rawName : namespaceURI + "^" + localName;
            state.doCData = format.isCDataElement( name );
            state.unescaped = format.isNonEscapingElement( name );
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }


    public void endElement( String namespaceURI, String localName,
                            String rawName )
        throws SAXException
    {
        try {
            endElementIO( namespaceURI, localName, rawName );
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }


    public void endElementIO( String namespaceURI, String localName,
                            String rawName )
        throws IOException
    {
        ElementState state;

        // Works much like content() with additions for closing
        // an element. Note the different checks for the closed
        // element's state and the parent element's state.
        printer.unindent();
        state = getElementState();
        if ( state.empty ) {
            printer.printText( "/>" );
        } else {
            // Must leave CData section first
            if ( state.inCData )
                printer.printText( "]]>" );
            // This element is not empty and that last content was
            // another element, so print a line break before that
            // last element and this element's closing tag.
            if ( indenting && ! state.preserveSpace && (state.afterElement || state.afterComment) )
                printer.breakLine();
            printer.printText( "</" );
            printer.printText( state.rawName );
            printer.printText( '>' );
        }
        // Leave the element state and update that of the parent
        // (if we're not root) to not empty and after element.
        state = leaveElementState();
        state.afterElement = true;
        state.afterComment = false;
        state.empty = false;
        if ( isDocumentState() )
            printer.flush();
    }


    //------------------------------------------//
    // SAX document handler serializing methods //
    //------------------------------------------//


    public void startElement( String tagName, AttributeList attrs )
        throws SAXException
    {
        int          i;
        boolean      preserveSpace;
        ElementState state;
        String       name;
        String       value;

        try {
        if ( printer == null )
            throw new IllegalStateException( "SER002 No writer supplied for serializer" );

        state = getElementState();
        if ( isDocumentState() ) {
            // If this is the root element handle it differently.
            // If the first root element in the document, serialize
            // the document's DOCTYPE. Space preserving defaults
            // to that of the output format.
            if ( ! started )
                startDocument( tagName );
        } else {
            // For any other element, if first in parent, then
            // close parent's opening tag and use the parnet's
            // space preserving.
            if ( state.empty )
                printer.printText( '>' );
            // Must leave CData section first
            if ( state.inCData )
            {
                printer.printText( "]]>" );
                state.inCData = false;
            }
            // Indent this element on a new line if the first
            // content of the parent element or immediately
            // following an element.
            if ( indenting && ! state.preserveSpace &&
                 ( state.empty || state.afterElement || state.afterComment) )
                printer.breakLine();
        }
        preserveSpace = state.preserveSpace;

        // Do not change the current element state yet.
        // This only happens in endElement().

        printer.printText( '<' );
        printer.printText( tagName );
        printer.indent();

        // For each attribute print it's name and value as one part,
        // separated with a space so the element can be broken on
        // multiple lines.
        if ( attrs != null ) {
            for ( i = 0 ; i < attrs.getLength() ; ++i ) {
                printer.printSpace();
                name = attrs.getName( i );
                value = attrs.getValue( i );
                if ( value != null ) {
                    printer.printText( name );
                    printer.printText( "=\"" );
                    printEscaped( value );
                    printer.printText( '"' );
                }

                // If the attribute xml:space exists, determine whether
                // to preserve spaces in this and child nodes based on
                // its value.
                if ( name.equals( "xml:space" ) ) {
                    if ( value.equals( "preserve" ) )
                        preserveSpace = true;
                    else
                        preserveSpace = format.getPreserveSpace();
                }
            }
        }
        // Now it's time to enter a new element state
        // with the tag name and space preserving.
        // We still do not change the curent element state.
        state = enterElementState( null, null, tagName, preserveSpace );
        state.doCData = format.isCDataElement( tagName );
        state.unescaped = format.isNonEscapingElement( tagName );
        } catch ( IOException except ) {
            throw new SAXException( except );
    }

    }


    public void endElement( String tagName )
        throws SAXException
    {
        endElement( null, null, tagName );
    }



    //------------------------------------------//
    // Generic node serializing methods methods //
    //------------------------------------------//


    /**
     * Called to serialize the document's DOCTYPE by the root element.
     * The document type declaration must name the root element,
     * but the root element is only known when that element is serialized,
     * and not at the start of the document.
     * <p>
     * This method will check if it has not been called before ({@link #started}),
     * will serialize the document type declaration, and will serialize all
     * pre-root comments and PIs that were accumulated in the document
     * (see {@link #serializePreRoot}). Pre-root will be serialized even if
     * this is not the first root element of the document.
     */
    protected void startDocument( String rootTagName )
        throws IOException
    {
        int    i;
        String dtd;

        dtd = printer.leaveDTD();
        if ( ! started ) {

            if ( ! format.getOmitXMLDeclaration() ) {
                StringBuffer    buffer;

                // Serialize the document declaration appreaing at the head
                // of very XML document (unless asked not to).
                buffer = new StringBuffer( "<?xml version=\"" );
                if ( format.getVersion() != null )
                    buffer.append( format.getVersion() );
                else
                    buffer.append( "1.0" );
                buffer.append( '"' );
                if ( format.getEncoding() != null ) {
                    buffer.append( " encoding=\"" );
                    buffer.append( format.getEncoding() );
                    buffer.append( '"' );
                }
                if ( format.getStandalone() && docTypeSystemId == null &&
                     docTypePublicId == null )
                    buffer.append( " standalone=\"yes\"" );
                buffer.append( "?>" );
                printer.printText( buffer );
                printer.breakLine();
            }

            if ( ! format.getOmitDocumentType() ) {
                if ( docTypeSystemId != null ) {
                    // System identifier must be specified to print DOCTYPE.
                    // If public identifier is specified print 'PUBLIC
                    // <public> <system>', if not, print 'SYSTEM <system>'.
                    printer.printText( "<!DOCTYPE " );
                    printer.printText( rootTagName );
                    if ( docTypePublicId != null ) {
                        printer.printText( " PUBLIC " );
                        printDoctypeURL( docTypePublicId );
                        if ( indenting ) {
                            printer.breakLine();
                            for ( i = 0 ; i < 18 + rootTagName.length() ; ++i )
                                printer.printText( " " );
                        } else
                            printer.printText( " " );
                    printDoctypeURL( docTypeSystemId );
                    }
                    else {
                        printer.printText( " SYSTEM " );
                        printDoctypeURL( docTypeSystemId );
                    }

                    // If we accumulated any DTD contents while printing.
                    // this would be the place to print it.
                    if ( dtd != null && dtd.length() > 0 ) {
                        printer.printText( " [" );
                        printText( dtd, true, true );
                        printer.printText( ']' );
                    }

                    printer.printText( ">" );
                    printer.breakLine();
                } else if ( dtd != null && dtd.length() > 0 ) {
                    printer.printText( "<!DOCTYPE " );
                    printer.printText( rootTagName );
                    printer.printText( " [" );
                    printText( dtd, true, true );
                    printer.printText( "]>" );
                    printer.breakLine();
                }
            }
        }
        started = true;
        // Always serialize these, even if not te first root element.
        serializePreRoot();
    }


    /**
     * Called to serialize a DOM element. Equivalent to calling {@link
     * #startElement}, {@link #endElement} and serializing everything
     * inbetween, but better optimized.
     */
    protected void serializeElement( Element elem )
        throws IOException
    {
        Attr         attr;
        NamedNodeMap attrMap;
        int          i;
        Node         child;
        ElementState state;
        boolean      preserveSpace;
        String       name;
        String       value;
        String       tagName;

        tagName = elem.getTagName();
        state = getElementState();
        if ( isDocumentState() ) {
            // If this is the root element handle it differently.
            // If the first root element in the document, serialize
            // the document's DOCTYPE. Space preserving defaults
            // to that of the output format.
            if ( ! started )
                startDocument( tagName );
        } else {
            // For any other element, if first in parent, then
            // close parent's opening tag and use the parnet's
            // space preserving.
            if ( state.empty )
                printer.printText( '>' );
            // Must leave CData section first
            if ( state.inCData )
            {
                printer.printText( "]]>" );
                state.inCData = false;
            }
            // Indent this element on a new line if the first
            // content of the parent element or immediately
            // following an element.
            if ( indenting && ! state.preserveSpace &&
                 ( state.empty || state.afterElement || state.afterComment) )
                printer.breakLine();
        }
        preserveSpace = state.preserveSpace;

        // Do not change the current element state yet.
        // This only happens in endElement().

        printer.printText( '<' );
        printer.printText( tagName );
        printer.indent();

        // Lookup the element's attribute, but only print specified
        // attributes. (Unspecified attributes are derived from the DTD.
        // For each attribute print it's name and value as one part,
        // separated with a space so the element can be broken on
        // multiple lines.
        attrMap = elem.getAttributes();
        if ( attrMap != null ) {
            for ( i = 0 ; i < attrMap.getLength() ; ++i ) {
                attr = (Attr) attrMap.item( i );
                name = attr.getName();
                value = attr.getValue();
                if ( value == null )
                    value = "";
                if ( attr.getSpecified() ) {
                    printer.printSpace();
                    printer.printText( name );
                    printer.printText( "=\"" );
                    printEscaped( value );
                    printer.printText( '"' );
                }
                // If the attribute xml:space exists, determine whether
                // to preserve spaces in this and child nodes based on
                // its value.
                if ( name.equals( "xml:space" ) ) {
                    if ( value.equals( "preserve" ) )
                        preserveSpace = true;
                    else
                        preserveSpace = format.getPreserveSpace();
                }
            }
        }

        // If element has children, then serialize them, otherwise
        // serialize en empty tag.
        if ( elem.hasChildNodes() ) {
            // Enter an element state, and serialize the children
            // one by one. Finally, end the element.
            state = enterElementState( null, null, tagName, preserveSpace );
            state.doCData = format.isCDataElement( tagName );
            state.unescaped = format.isNonEscapingElement( tagName );
            child = elem.getFirstChild();
            while ( child != null ) {
                serializeNode( child );
                child = child.getNextSibling();
            }
            endElementIO( null, null, tagName );
        } else {
            printer.unindent();
            printer.printText( "/>" );
            // After element but parent element is no longer empty.
            state.afterElement = true;
            state.afterComment = false;
            state.empty = false;
            if ( isDocumentState() )
                printer.flush();
        }
    }


    protected String getEntityRef( int ch )
    {
        // Encode special XML characters into the equivalent character references.
        // These five are defined by default for all XML documents.
        switch ( ch ) {
        case '<':
            return "lt";
        case '>':
            return "gt";
        case '"':
            return "quot";
        case '\'':
            return "apos";
        case '&':
            return "amp";
        }
        return null;
    }


    /** Retrieve and remove the namespaces declarations from the list of attributes.
     *
     */
    private Attributes extractNamespaces( Attributes attrs )
        throws SAXException
    {
        AttributesImpl attrsOnly;
        String         rawName;
        int            i;
        int            length;

        length = attrs.getLength();
        attrsOnly = new AttributesImpl( attrs );

        for ( i = length - 1 ; i >= 0 ; --i ) {
            rawName = attrsOnly.getQName( i );

            //We have to exclude the namespaces declarations from the attributes
            //Append only when the feature http://xml.org/sax/features/namespace-prefixes"
            //is TRUE
            if ( rawName.startsWith( "xmlns" ) ) {
                if (rawName.length() == 5) {
                    startPrefixMapping( "", attrs.getValue( i ) );
                    attrsOnly.removeAttribute( i );
                } else if (rawName.charAt(5) == ':') {
                    startPrefixMapping(rawName.substring(6), attrs.getValue(i));
                    attrsOnly.removeAttribute( i );
                }
            }
        }
        return attrsOnly;
    }
}
