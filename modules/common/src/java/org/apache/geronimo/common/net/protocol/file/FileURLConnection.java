/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.common.net.protocol.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.net.URLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.security.Permission;
import java.io.FilePermission;

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.Primitives;

import sun.net.www.ParseUtil;

/**
 * A URLConnection for the 'file' protocol.
 *
 * <p>Correctly returns headers.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/01 15:18:25 $
 */
public class FileURLConnection
    extends URLConnection
{
    protected File file;
    
    public FileURLConnection(final URL url, final File file)
        throws MalformedURLException, IOException
    {
        super(url);
        
        if (file == null) {
            throw new NullArgumentException("file");
        }
        
        this.file = file;
    }
    
    public File getFile()
    {
        return file;
    }
    
    public void connect() throws IOException
    {
        if (connected) {
            return;
        }
        
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString());
        }
        
        connected = true;
    }
    
    /**
     * Return the input stream for the file.
     *
     * <p>Sun's URL connections use buffered streams, so we do too.
     *
     * <p>This impl will return a new stream for each call.
     */
    public InputStream getInputStream() throws IOException
    {
        if (!connected) {
            connect();
        }
        
        return new BufferedInputStream(new FileInputStream(file));
    }
    
     /**
     * Return the output stream for the file.
     *
     * <p>Sun's URL connections use buffered streams, so we do too.
     *
     * <p>This impl will return a new stream for each call.
     */
    public OutputStream getOutputStream() throws IOException
    {
        if (!connected) {
            connect();
        }
        
        return new BufferedOutputStream(new FileOutputStream(file));
    }
    
    /**
     * Return the permission for the file.
     *
     * <p>Sun's impl always returns "read", but no reason why we can
     *    not also write to a file URL, so we do.
     */
    public Permission getPermission() throws IOException
    {
        // Detect if we have read/write perms
        String perms = null;
        
        if (file.canRead()) {
            perms = "read";
        }
        if (file.canWrite()) {
            if (perms != null) {
                perms += ",write";
            }
            else {
                perms = "write";
            }
        }
        
        // File perms need filename to be in system format
        String filename = ParseUtil.decode(url.getPath());
        if (File.separatorChar != '/') {
            filename.replace('/', File.separatorChar);
        }
        
        return new FilePermission(filename, perms);
    }
    
    /**
     * Always return the last-modified from the file.
     *
     * <p>NOTE: Sun's impl caches this value, so it will appear to never change
     *          even if the underlying file's last-modified has changed.
     */
    public long getLastModified()
    {
        return file.lastModified();
    }
    
    /**
     * Returns the last modified time of the file.
     */
    public long getDate()
    {
        return getLastModified();
    }
    
    /**
     * Returns the length of the file.
     *
     * @throws DataConversionException  File size is too large to convert to int.
     */
    public int getContentLength()
    {
        return Primitives.toInt(file.length());
    }
    
    /**
     * Returns the content type of the file as mapped by the filename map.
     */
    public String getContentType()
    {
        return getFileNameMap().getContentTypeFor(file.getName());
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                                Headers                              //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Get a header field by name.
     *
     * <p>Supported headers:
     * <ul>
     * <li>last-modified
     * <li>content-length
     * <li>content-type
     * <li>date
     * </ul>
     *
     * <p>Hook into our local methods to get headers.  URLConnection
     *    normally goes the other way around.  ie. URLConnection.getDate()
     *    calls getHeaderField('date'), but for file usage this is wasteful
     *    string creation as normally the getHeaderField() will not be called.
     */
    public String getHeaderField(final String name)
    {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        String headerName = name.toLowerCase();
        
        if (headerName.equals("last-modified")) {
            return String.valueOf(getLastModified());
        }
        else if (headerName.equals("content-length")) {
            return String.valueOf(getContentLength());
        }
        else if (headerName.equals("content-type")) {
            return getContentType();
        }
        else if (headerName.equals("date")) {
            return String.valueOf(getDate());
        }
        
        return super.getHeaderField(name);
    }
    
    /**
     * Returns supported headers.
     *
     * @see #getHeaderField(String)
     */
    public Map getHeaderFields()
    {
        Map headers = new HashMap();
        String[] headerNames = {
            "last-modified",
            "content-length",
            "content-type",
            "date"
        };
        
        for (int i=0; i<headerNames.length; i++) {
            List list = new ArrayList(1);
            list.add(getHeaderField(headerNames[i]));
            headers.put(headerNames[i], Collections.unmodifiableList(list));
        }
        
        return Collections.unmodifiableMap(headers);
    }
    
    //
    // TODO: implement these... this is fairly assbackwards so screw it for now
    //
    
    public String getHeaderFieldKey(final int n)
    {
        return getHeaderFieldKey(n);
    }
    
    public String getHeaderField(final int n)
    {
        return getHeaderField(n);
    }
}
