/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.testsupport;

import java.io.File;

import org.slf4j.Logger;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 * Provides support for tests.
 *
 * @version $Rev: 653782 $ $Date: 2008-05-07 00:10:14 +1000 (Wed, 07 May 2008) $
 */
public abstract class RMockTestSupport
    extends RMockTestCase
{

    private final TestUtil testUtil;
    
    /**
     * The base-directory which tests should be run from.
     */
    protected final File BASEDIR;
    
    /**
     * Instance logger which tests should use to produce tracing information.
     *
     * <p>
     * Unless you have a really good reason to, do not change this field from your sub-class.
     * And if you do, please document why you have done so.
     */
    protected final Logger log;

    
    /**
     * Constructor for tests that specify a specific test name.
     *
     * @see #TestSupport()  This is the prefered constructor for sub-classes to use.
     */
    protected RMockTestSupport(final String name) {
        super(name);
        
        testUtil = new TestUtil(getClass());
        
        BASEDIR = testUtil.getBaseDir();
        log = testUtil.getLog();
        
        log.info("Initialized");
    }
    
    /**
     * Default constructor.
     */
    protected RMockTestSupport() {
        super();

        testUtil = new TestUtil(getClass());
        
        BASEDIR = testUtil.getBaseDir();
        log = testUtil.getLog();

        log.info("Initialized");
    }
    
    /**
     * Resolve the given path to a file rooted to {@link #BASEDIR}.
     *
     * @param path  The path to resolve.
     * @return      The resolved file for the given path.
     */
    protected final File resolveFile(final String path) {
        return testUtil.resolveFile(path);
    }
    
    /**
     * Resolve the given path to a path rooted to {@link #BASEDIR}.
     *
     * @param path  The path to resolve.
     * @return      The resolved path for the given path.
     *
     * @see #resolveFile(String)
     */
    protected final String resolvePath(final String path) {
        return testUtil.resolvePath(path);
    }
}
