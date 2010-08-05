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

import org.testng.annotations.Test;

import org.apache.geronimo.testsupport.SeleniumTestSupport;

/**
 * Test MultiPart through File upload.
 */
public class TestMultiPartAnnotation extends SeleniumTestSupport {
    @Test
    public void testUploadSuccess() throws Exception {
        String appContextStr = System.getProperty("appContext");
		selenium.open(appContextStr);        
        selenium.click("multiAnnotation");
        waitForPageLoad();
        uploadFile("small.txt");
        waitForPageLoad();
		assertEquals("File Upload System", selenium.getTitle());
        //assertTrue(selenium.isTextPresent("Size=35"));
        assertTrue(selenium.isTextPresent("This is a file no larger than 10k."));
    }

    @Test
    public void testNoFileInput() throws Exception {
        String appContextStr = System.getProperty("appContext");
		selenium.open(appContextStr);        
        selenium.click("multiAnnotation");
        waitForPageLoad();
        selenium.click("//input[@value='Submit The File!']");
        waitForPageLoad();
        assertEquals("File Upload System", selenium.getTitle());
        assertTrue(selenium.isTextPresent("ContentType=application/octet-stream"));
    }

    @Test
    public void testFileTooLarge() throws Exception {
        String appContextStr = System.getProperty("appContext");
		selenium.open(appContextStr);        
        selenium.click("multiAnnotation");
        waitForPageLoad();
        uploadFile("large.txt");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("The file size is 12110b, it's filterd because the file size is limited to 10 kb"));
    }

    private void uploadFile(String fileName) {
        String testFileLocation = System.getProperty("testFileLocation");
        //Test under Linux
        if (testFileLocation.contains("/")) {
        	 selenium.type("testFile", testFileLocation + "/" + fileName);
             selenium.click("//input[@value='Submit The File!']");
        }
        //Test under Windows
        else {
            testFileLocation = testFileLocation.replace("\\", "\\"+"\\");
        	selenium.type("testFile", testFileLocation + "\\" + "\\" + fileName);
            selenium.click("//input[@value='Submit The File!']");
        }
    }

}
