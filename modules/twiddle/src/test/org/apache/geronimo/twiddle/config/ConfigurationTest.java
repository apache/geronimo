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

package org.apache.geronimo.twiddle.config;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

/**
 * Tests for <code>Configuration</code>.
 *
 * @version <code>$Id: ConfigurationTest.java,v 1.4 2003/08/13 16:54:48 jdillon Exp $</code>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ConfigurationTest
    extends TestCase
{
    /**
     * Return the tests included in this test suite.
     */
    public static Test suite()
    {
        return new TestSuite(ConfigurationTest.class);
    }
    
    /**
     * Construct a new instance of this test case.
     *
     * @param name  Name of the test case
     */
    public ConfigurationTest(final String name)
    {
        super(name);
    }
    
    protected ConfigurationReader reader;
    
    /**
     * Set up instance variables required by this test case.
     */
    protected void setUp()
    {
        reader = new ConfigurationReader();
    }
    
    /**
     * Tear down instance variables required by this test case.
     */
    protected void tearDown()
    {
        reader = null;
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                               Tests                                 //
    /////////////////////////////////////////////////////////////////////////
    
    protected Reader getReader(final String filename) throws IOException
    {
        InputStream input = this.getClass().getResourceAsStream(filename);
        
        if (input == null) {
            throw new FileNotFoundException("Missing test data file: " + filename);
        }
        
        return new BufferedReader(new InputStreamReader(input));
    }
    
    protected Configuration read(final String filename) throws Exception
    {
        Reader input = getReader(filename);
        Configuration config = null;
        
        try {
            config = reader.read(input);
        }
        finally {
            input.close();
        }
        
        return config;
    }
    
    public void testReader() throws Exception
    {
        Configuration config = read("test1.xml");
    }
    
    public void testProperties() throws Exception
    {
        Configuration config = read("test1.xml");
        
        PropertiesConfig p = config.getPropertiesConfig();
        Assert.assertNotNull(p);
        
        PropertyConfig[] props = p.getPropertyConfig();
        Assert.assertEquals(2, props.length);
        
        Assert.assertEquals("property1", props[0].getName());
        Assert.assertEquals("value1", props[0].getContent());
        Assert.assertEquals("property2", props[1].getName());
        Assert.assertEquals("value2", props[1].getContent());
    }
    
    /*
    
    public void testLibraries() throws Exception
    {
        Configuration config = read("test1.xml");
        
        LibrariesConfig l = config.getLibrariesConfig();
        Assert.assertNotNull(l);
        
        LibraryConfig[] libs = l.getLibraryConfig();
        Assert.assertEquals(2, libs.length);
        
        Assert.assertEquals("library1", libs[0].getContent());
        Assert.assertEquals("library2", libs[1].getContent());
    }
    
    */
    
    public void testIncludes() throws Exception
    {
        Configuration config = read("test1.xml");
        
        IncludesConfig i = config.getIncludesConfig();
        Assert.assertNotNull(i);
        
        String[] incs = i.getInclude();
        Assert.assertEquals(2, incs.length);
        
        Assert.assertEquals("include1", incs[0]);
        Assert.assertEquals("include2", incs[1]);
    }
    
    /*
    
    public void testSearchPath() throws Exception
    {
        Configuration config = read("test1.xml");
        
        SearchPathConfig s = config.getSearchPathConfig();
        Assert.assertNotNull(s);
        
        String[] paths = s.getPathElement();
        Assert.assertEquals(3, paths.length);
        
        Assert.assertEquals("path1", paths[0]);
        Assert.assertEquals("path2", paths[1]);
        Assert.assertEquals("path3", paths[2]);
    }
    
    */
}
