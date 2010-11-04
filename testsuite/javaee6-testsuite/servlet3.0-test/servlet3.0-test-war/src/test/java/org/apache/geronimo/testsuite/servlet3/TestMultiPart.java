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

package org.apache.geronimo.testsuite.servlet3;

import java.io.File;
import java.io.IOException;

import org.apache.geronimo.testsuite.servlet3.app.FileMessageFilter;
import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;

/**
 * Test MultiPart through File upload.
 */
public class TestMultiPart extends SeleniumTestSupport {
    public final static String DEFAULT_URL = "http:8080//servlet3.0-test-war";

    @Test
    public void testUploadSuccess() throws Exception {
        String appContextStr = System.getProperty("appContext", DEFAULT_URL);
        selenium.open(appContextStr);
        selenium.click("link=Test File Upload.");
        waitForPageLoad();
        uploadFile("small.txt");
        waitForPageLoad();
        assertEquals("File Upload System", selenium.getTitle());
        assertTrue(selenium.isTextPresent("This is a file no larger than 10k."));
    }

    @Test
    public void testNoFileInput() throws Exception {
        String appContextStr = System.getProperty("appContext", DEFAULT_URL);
        selenium.open(appContextStr);
        selenium.click("link=Test File Upload.");
        waitForPageLoad();
        selenium.click("//input[@value='Submit The File!']");
        waitForPageLoad();
        assertEquals("File Upload System", selenium.getTitle());
        assertTrue(selenium.isTextPresent("ContentType=application/octet-stream"));
    }

    @Test
    public void testFileTooLarge() throws Exception {
        String appContextStr = System.getProperty("appContext", DEFAULT_URL);
        selenium.open(appContextStr);
        selenium.click("fileupload");
        waitForPageLoad();
        uploadFile("large.txt");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent(FileMessageFilter.FILTERED_STRING));
    }

    private void uploadFile(String fileName) throws IOException {
        File file = new File(new File(new File(getBaseDir(), "target"), "test-classes"), fileName);
        selenium.type("testFile", file.getCanonicalPath());
        selenium.click("//input[@value='Submit The File!']");
    }

}
