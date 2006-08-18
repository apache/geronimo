/**
 *  Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.testsupport;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides support for tests.
 *
 * @version $Rev$ $Date$
 */
public abstract class TestSupport
    extends TestCase
{
    /**
     * The base-directory which tests should be run from.
     */
    private static final File BASEDIR;
    
    static {
        //
        // TODO: Add some special magic here to figure this out when we are running from
        //       and IDE, like IDEA or Eclipse.  user.dir/target might work... but need
        //       to validate what env each IDE has this set to.
        //
        
        String tmp = System.getProperty("basedir");
        if (tmp == null) {
            throw new Error("Missing 'basedir' property; tests need this property set to run correctly");
        }
        
        BASEDIR = new File(tmp);
    }
    
    /**
     * Instance logger which tests should use to produce tracing information.
     */
    protected Log log = LogFactory.getLog(getClass());
}
