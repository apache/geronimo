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

package org.apache.geronimo.common.net.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Map;

import java.security.Permission;

/**
 * An delegating URLConnection support class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/30 11:58:42 $
 */
public class DelegatingURLConnection
    extends URLConnection
{
    protected URL delegateUrl;
    protected URLConnection delegateConnection;
    
    public DelegatingURLConnection(final URL url)
        throws MalformedURLException, IOException
    {
        super(url);
        
        delegateUrl = makeDelegateUrl(url);
        assert delegateUrl != null;
        
        delegateConnection = makeDelegateUrlConnection(delegateUrl);
        assert delegateConnection != null;
    }
    
    protected URL makeDelegateUrl(final URL url)
        throws MalformedURLException, IOException
    {
        assert url != null;
        
        return url;
    }
    
    protected URLConnection makeDelegateUrlConnection(final URL url)
        throws IOException
    {
        assert url != null;
        
        return url.openConnection();
    }
    
    public void connect() throws IOException
    {
        delegateConnection.connect();
    }
    
    public URL getURL() {
        return delegateConnection.getURL();
    }
    
    public int getContentLength() {
        return delegateConnection.getContentLength();
    }
    
    public String getContentType() {
        return delegateConnection.getContentType();
    }
    
    public String getContentEncoding() {
        return delegateConnection.getContentEncoding();
    }
    
    public long getExpiration() {
        return delegateConnection.getExpiration();
    }
    
    public long getDate() {
        return delegateConnection.getDate();
    }
    
    public long getLastModified() {
        return delegateConnection.getLastModified();
    }
    
    public String getHeaderField(String name) {
        return delegateConnection.getHeaderField(name);
    }
    
    public Map getHeaderFields() {
        return delegateConnection.getHeaderFields();
    }
    
    public int getHeaderFieldInt(String name, int _default) {
        return delegateConnection.getHeaderFieldInt(name, _default);
    }
    
    public long getHeaderFieldDate(String name, long _default) {
        return delegateConnection.getHeaderFieldDate(name, _default);
    }
    
    public String getHeaderFieldKey(int n) {
        return delegateConnection.getHeaderFieldKey(n);
    }
    
    public String getHeaderField(int n) {
        return delegateConnection.getHeaderField(n);
    }
    
    public Object getContent() throws IOException {
        return delegateConnection.getContent();
    }
    
    public Object getContent(Class[] classes) throws IOException {
        return delegateConnection.getContent(classes);
    }
    
    public Permission getPermission() throws IOException {
        return delegateConnection.getPermission();
    }
    
    public InputStream getInputStream() throws IOException {
        return delegateConnection.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException {
        return delegateConnection.getOutputStream();
    }
    
    public String toString() {
        return super.toString() + "{ " + delegateConnection + " }";
    }
    
    public void setDoInput(boolean doinput) {
        delegateConnection.setDoInput(doinput);
    }
    
    public boolean getDoInput() {
        return delegateConnection.getDoInput();
    }
    
    public void setDoOutput(boolean dooutput) {
        delegateConnection.setDoOutput(dooutput);
    }
    
    public boolean getDoOutput() {
        return delegateConnection.getDoOutput();
    }
    
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        delegateConnection.setAllowUserInteraction(allowuserinteraction);
    }
    
    public boolean getAllowUserInteraction() {
        return delegateConnection.getAllowUserInteraction();
    }
    
    public void setUseCaches(boolean usecaches) {
        delegateConnection.setUseCaches(usecaches);
    }
    
    public boolean getUseCaches() {
        return delegateConnection.getUseCaches();
    }
    
    public void setIfModifiedSince(long ifmodifiedsince) {
        delegateConnection.setIfModifiedSince(ifmodifiedsince);
    }
    
    public long getIfModifiedSince() {
        return delegateConnection.getIfModifiedSince();
    }
    
    public boolean getDefaultUseCaches() {
        return delegateConnection.getDefaultUseCaches();
    }
    
    public void setDefaultUseCaches(boolean defaultusecaches) {
        delegateConnection.setDefaultUseCaches(defaultusecaches);
    }
    
    public void setRequestProperty(String key, String value) {
        delegateConnection.setRequestProperty(key, value);
    }
    
    public void addRequestProperty(String key, String value) {
        delegateConnection.addRequestProperty(key, value);
    }
    
    public String getRequestProperty(String key) {
        return delegateConnection.getRequestProperty(key);
    }
    
    public Map getRequestProperties() {
        return delegateConnection.getRequestProperties();
    }
}
