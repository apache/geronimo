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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.testng.annotations.Test;

import org.apache.geronimo.testsupport.SeleniumTestSupport;

/**
 * Test MultiPart through File upload.
 */
public class TestMultiPart extends SeleniumTestSupport {

    @Test
    public void testUploadSuccess() throws Exception {
        copyFile("small.txt");
        String appContextStr = System.getProperty("appContext");
		selenium.open(appContextStr);
        selenium.selectFrame("sampleDocumentFrame");
        selenium.click("link=Test File Upload.");
        waitForPageLoad();
        // TODO: Figure out how to upload a file
        selenium.type("testFile", "c:\\test_file\\small.txt");
        selenium.click("//input[@value='Submit The File!']");
        waitForPageLoad();
        assertEquals("File Upload System", selenium.getTitle());
        assertEquals("A listener is dectecting online person number.", selenium.getText("xpath=/html/body/h2[1]"));
        // assertEquals("Currently,there are 1 people visiting this file upload system!",
        // selenium.getText("xpath=/html/body/h3"));
    }

    @Test
    public void testNoFileInput() throws Exception {
        String appContextStr = System.getProperty("appContext");
		selenium.open(appContextStr);
        selenium.selectFrame("sampleDocumentFrame");
        selenium.click("link=Test File Upload.");
        waitForPageLoad();
        selenium.click("//input[@value='Submit The File!']");
        waitForPageLoad();
        assertEquals("File Upload System", selenium.getTitle());
        assertEquals("A listener is dectecting online person number.", selenium.getText("xpath=/html/body/h2[1]"));
    }

    @Test
    public void testFileTooLarge() throws Exception {
        String appContextStr = System.getProperty("appContext");
		selenium.open(appContextStr);
        selenium.selectFrame("sampleDocumentFrame");
        selenium.click("fileupload");
        waitForPageLoad();
        // TODO: Figure out how to upload a file
        selenium.type("testFile", "D:\\allServers\\apache-james-2.3.2.zip");
        selenium.click("//input[@value='Submit The File!']");
        waitForPageLoad();
        //assertTrue(selenium.isTextPresent("The file size is 7996381b, it's filterd because the file size is limited to 10 kb"));
    }

    private void copyFile(String fileName) {
        File file = new File(fileName);
        FileInputStream fis;
        FileOutputStream fos;
        try {
            fis = new FileInputStream(file);
/*            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            File localFile = new File("c:\\test_file\\" + fileName);
            localFile.mkdirs();
            fos = new FileOutputStream(localFile);
            fos.write(buffer);*/

        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            
        }

        File filel = new File("c://test_file//" + fileName);
        System.out.println(filel.getName());
    }

}
