/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.twiddle.config;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import junit.framework.TestCase;

/**
 * Tests for <code>Configuration</code>.
 *
 * @version <code>$Rev$ $Date$</code>
 */
public class ConfigurationTest
    extends TestCase
{
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
        assertNotNull(p);
        
        PropertyConfig[] props = p.getPropertyConfig();
        assertEquals(2, props.length);
        
        assertEquals("property1", props[0].getName());
        assertEquals("value1", props[0].getContent());
        assertEquals("property2", props[1].getName());
        assertEquals("value2", props[1].getContent());
    }
    
    /*
    
    public void testLibraries() throws Exception
    {
        Configuration config = read("test1.xml");
        
        LibrariesConfig l = config.getLibrariesConfig();
        assertNotNull(l);
        
        LibraryConfig[] libs = l.getLibraryConfig();
        assertEquals(2, libs.length);
        
        assertEquals("library1", libs[0].getContent());
        assertEquals("library2", libs[1].getContent());
    }
    
    */
    
    public void testIncludes() throws Exception
    {
        Configuration config = read("test1.xml");
        
        IncludesConfig i = config.getIncludesConfig();
        assertNotNull(i);
        
        String[] incs = i.getInclude();
        assertEquals(2, incs.length);
        
        assertEquals("include1", incs[0]);
        assertEquals("include2", incs[1]);
    }
    
    /*
    
    public void testSearchPath() throws Exception
    {
        Configuration config = read("test1.xml");
        
        SearchPathConfig s = config.getSearchPathConfig();
        assertNotNull(s);
        
        String[] paths = s.getPathElement();
        assertEquals(3, paths.length);
        
        assertEquals("path1", paths[0]);
        assertEquals("path2", paths[1]);
        assertEquals("path3", paths[2]);
    }
    
    */
}
