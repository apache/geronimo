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
 *        Apache Software Foundation (http:www.apache.org/)."
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
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.kernel.deployment.goal;

import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.kernel.deployment.GeronimoTargetModule;

import java.io.File;
import java.net.URL;


/**
 * Tests the {@link org.apache.geronimo.kernel.deployment.goal.DeployURL} deployment goal
 * @see org.apache.geronimo.kernel.deployment.goal.AbstractDeploymentGoalTest
 * @see org.apache.geronimo.kernel.deployment.goal.DeploymentGoal
 * @version $Revision: 1.1 $ $Date: 2003/12/06 18:27:45 $
 */
public class DeployURLTest extends AbstractDeploymentGoalTest {

    protected URL url = null;
    protected DeployURL deployURL = null;

    /**
     * Creates an instance of {@link org.apache.geronimo.kernel.deployment.goal.DeployURL} to be used in deployment goal tests
     * performed by the base class. This implementation creates an instance
     * of {@link java.net.URL} for <code>http://localhost/</code> and uses
     * {@link org.apache.geronimo.kernel.deployment.scanner.URLType#PACKED_ARCHIVE} as DeployURL URLtype.
     *
     * @param targetModule target to create deployment goal
     * @return {@link org.apache.geronimo.kernel.deployment.goal.DeployURL} deployment goal
     */
    public DeploymentGoal getDeploymentGoal(GeronimoTargetModule targetModule) {
        try {
            url = new URL("http://localhost/");
        } catch (java.net.MalformedURLException mue) {
            throw new RuntimeException(mue);
        }
        deployURL = new DeployURL(targetModule, url, URLType.PACKED_ARCHIVE);
        return deployURL;
    }

    /**
     * Tests the accessor to <code>url</code> attribute in case of non-file protocol URL.
     * The current implementation of {@link org.apache.geronimo.kernel.deployment.goal.DeployURL} in this case returns the
     * original url as is.
     */
    public void testGetNonFileURL() {
        assertSame("The URL wasn't changed for http protocol", deployURL.getUrl(), url);
    }

    /**
     * Tests the accessor to <code>type</code> attribute.
     */
    public void testGetType() {
        assertSame("The URLType returned is correct", deployURL.getType(), URLType.PACKED_ARCHIVE);
    }

    /**
     * Test the normalizing algorithm and the accessor to <code>url</code> attribute in case of file protocol URL.
     * @throws java.lang.Exception
     */
    public void testNormalizeURL() throws Exception {
        File newFile = new File("deployURLTest.tmp");
        URL fileURL = newFile.toURI().toURL();
        DeployURL deployURL2 = new DeployURL(targetModule, fileURL, URLType.PACKED_ARCHIVE);
        URL normalizedURL = deployURL2.getUrl();
        assertNotSame("URL for file protocol is not the same (presumably normalized)", normalizedURL, fileURL);
        URL normalizedURL2 = new File(fileURL.getFile().replace('/', File.separatorChar)).toURI().toURL();
        assertEquals("URL is normalized", normalizedURL2, normalizedURL);
    }

    /**
     * Tests the current behaviour of {@link org.apache.geronimo.kernel.deployment.goal.DeployURL} constructor that does not accept
     * <code>null</code> for the url argument.
     */
    public void testNullURL() {
        try {
            new DeployURL(targetModule, null, URLType.PACKED_ARCHIVE);
            assertTrue("An IllegalArgumentException is thrown when URL is passed as null", false);
        } catch (IllegalArgumentException iae) {
        }
    }

    /**
     * Tests the current behaviour of {@link org.apache.geronimo.kernel.deployment.goal.DeployURL} constructor that does not accept
     * <code>null</code> for the type argument.
     */
    public void testNullType() {
        try {
            new DeployURL(targetModule, url, null);
            assertTrue("An IllegalArgumentException is thrown when URLType is passed as null", false);
        } catch (IllegalArgumentException iae) {
        }
    }
}

