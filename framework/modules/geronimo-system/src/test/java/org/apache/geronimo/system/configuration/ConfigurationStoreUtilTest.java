/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationStoreUtilTest extends TestCase {
    private File basedir = new File(System.getProperty("basedir"));
    private File testFile;
    private static final String BAD_SUM = "Stinky Cheese";
    private File sumFile;

    public void testChecksum() throws IOException {
        String actualChecksum = ConfigurationStoreUtil.getActualChecksum(testFile);
        assertNotNull(actualChecksum);

        // we haven't writen the checksum yet so it should be null
        String expectedChecksum = ConfigurationStoreUtil.getExpectedChecksum(testFile);
        assertNull(expectedChecksum);

        // write the sum file
        ConfigurationStoreUtil.writeChecksumFor(testFile);

        // get the new sum
        expectedChecksum = ConfigurationStoreUtil.getExpectedChecksum(testFile);
        assertNotNull(expectedChecksum);

        // should be the same
        assertEquals(expectedChecksum, actualChecksum);

        assertTrue(ConfigurationStoreUtil.verifyChecksum(testFile));
    }

    public void testBadChecksum() throws IOException {
        String actualChecksum = ConfigurationStoreUtil.getActualChecksum(testFile);
        assertNotNull(actualChecksum);

        // we haven't writen the checksum yet so it should be null
        String expectedChecksum = ConfigurationStoreUtil.getExpectedChecksum(testFile);
        assertNull(expectedChecksum);

        // write the bad sum file
        FileWriter writer = new FileWriter(sumFile);
        writer.write(BAD_SUM);
        writer.close();

        // get the new sum
        expectedChecksum = ConfigurationStoreUtil.getExpectedChecksum(testFile);
        assertEquals(BAD_SUM, expectedChecksum);

        // should not be the same
        assertFalse(expectedChecksum.equals(actualChecksum));

        assertFalse(ConfigurationStoreUtil.verifyChecksum(testFile));
    }

    public void testEmptyFile() throws IOException {
        testFile.delete();
        testFile.createNewFile();

        String actualChecksum = ConfigurationStoreUtil.getActualChecksum(testFile);
        assertNotNull(actualChecksum);


        // we haven't writen the checksum yet so it should be null
        String expectedChecksum = ConfigurationStoreUtil.getExpectedChecksum(testFile);
        assertNull(expectedChecksum);

        // create an empty sum file
        sumFile.createNewFile();

        expectedChecksum = ConfigurationStoreUtil.getExpectedChecksum(testFile);
        assertNull(expectedChecksum);
    }

    public void testOutputStreamChecksum() throws Exception {
        testFile.delete();
        ConfigurationStoreUtil.ChecksumOutputStream out = new ConfigurationStoreUtil.ChecksumOutputStream(new FileOutputStream(testFile));
        OutputStreamWriter writer = new OutputStreamWriter(out);
        writer.write("cvmnxc,vmnx,cmvn,xmcvlsjnv,mcnvjshfgmsnfvoiwrhjfjlnvkjhnfgornfgonviohjfowehldnf\n");
        writer.write("uncwcdwncicjevhinfmcnrfviefjnvun4d49jf93efv78y4bhc3hf3jdf83hf8ejkdhyfuh9iuwdhfw\n");
        writer.write("cvmnxc,vmnx,cmvn,xmcvlsjnv,mcnvjshfgmsnfvoiwrhjfjlnvkjhnfgornfgonviohjfowehldnf\n");
        writer.write("uncwcdwncicjevhinfmcnrfviefjnvun4d49jf93efv78y4bhc3hf3jdf83hf8ejkdhyfuh9iuwdhfw\n");
        writer.write("cvmnxc,vmnx,cmvn,xmcvlsjnv,mcnvjshfgmsnfvoiwrhjfjlnvkjhnfgornfgonviohjfowehldnf\n");
        writer.write("uncwcdwncicjevhinfmcnrfviefjnvun4d49jf93efv78y4bhc3hf3jdf83hf8ejkdhyfuh9iuwdhfw\n");
        writer.write("cvmnxc,vmnx,cmvn,xmcvlsjnv,mcnvjshfgmsnfvoiwrhjfjlnvkjhnfgornfgonviohjfowehldnf\n");
        writer.write("uncwcdwncicjevhinfmcnrfviefjnvun4d49jf93efv78y4bhc3hf3jdf83hf8ejkdhyfuh9iuwdhfw\n");
        writer.write("cvmnxc,vmnx,cmvn,xmcvlsjnv,mcnvjshfgmsnfvoiwrhjfjlnvkjhnfgornfgonviohjfowehldnf\n");
        writer.write("uncwcdwncicjevhinfmcnrfviefjnvun4d49jf93efv78y4bhc3hf3jdf83hf8ejkdhyfuh9iuwdhfw\n");
        writer.write("cvmnxc,vmnx,cmvn,xmcvlsjnv,mcnvjshfgmsnfvoiwrhjfjlnvkjhnfgornfgonviohjfowehldnf\n");
        writer.write("uncwcdwncicjevhinfmcnrfviefjnvun4d49jf93efv78y4bhc3hf3jdf83hf8ejkdhyfuh9iuwdhfw\n");
        writer.write("cvmnxc,vmnx,cmvn,xmcvlsjnv,mcnvjshfgmsnfvoiwrhjfjlnvkjhnfgornfgonviohjfowehldnf\n");
        writer.write("uncwcdwncicjevhinfmcnrfviefjnvun4d49jf93efv78y4bhc3hf3jdf83hf8ejkdhyfuh9iuwdhfw\n");
        writer.write("end\n");
        writer.close();

        String actualChecksum = out.getChecksum();
        String expectedChecksum = ConfigurationStoreUtil.getActualChecksum(testFile);
        assertEquals(expectedChecksum, actualChecksum);
    }

    protected void setUp() throws Exception {
        super.setUp();
        testFile = new File(basedir, "target/checksumTest/test.data");
        testFile.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(testFile);
        writer.write("lflkfjkljkldfaskljsadflkjasdflweoiurhlmvniouwehnflikmnfubhgkajnbfgk;ausuhfoubhr\n");
        writer.write("uweyrueruewrncihweruhnwjnfduhfrwejbnruhnfuhfjnceucnfierouhfljkdnciuehfinbvcusdu\n");
        writer.write("lflkfjkljkldfaskljsadflkjasdflweoiurhlmvniouwehnflikmnfubhgkajnbfgk;ausuhfoubhr\n");
        writer.write("uweyrueruewrncihweruhnwjnfduhfrwejbnruhnfuhfjnceucnfierouhfljkdnciuehfinbvcusdu\n");
        writer.write("lflkfjkljkldfaskljsadflkjasdflweoiurhlmvniouwehnflikmnfubhgkajnbfgk;ausuhfoubhr\n");
        writer.write("uweyrueruewrncihweruhnwjnfduhfrwejbnruhnfuhfjnceucnfierouhfljkdnciuehfinbvcusdu\n");
        writer.write("lflkfjkljkldfaskljsadflkjasdflweoiurhlmvniouwehnflikmnfubhgkajnbfgk;ausuhfoubhr\n");
        writer.write("uweyrueruewrncihweruhnwjnfduhfrwejbnruhnfuhfjnceucnfierouhfljkdnciuehfinbvcusdu\n");
        writer.write("lflkfjkljkldfaskljsadflkjasdflweoiurhlmvniouwehnflikmnfubhgkajnbfgk;ausuhfoubhr\n");
        writer.write("uweyrueruewrncihweruhnwjnfduhfrwejbnruhnfuhfjnceucnfierouhfljkdnciuehfinbvcusdu\n");
        writer.write("lflkfjkljkldfaskljsadflkjasdflweoiurhlmvniouwehnflikmnfubhgkajnbfgk;ausuhfoubhr\n");
        writer.write("uweyrueruewrncihweruhnwjnfduhfrwejbnruhnfuhfjnceucnfierouhfljkdnciuehfinbvcusdu\n");
        writer.write("end\n");
        writer.close();
        sumFile = new File(testFile.getParentFile(), testFile.getName() + ".sha1");
        sumFile.delete();
    }

    protected void tearDown() throws Exception {
        testFile.delete();
        sumFile.delete();
        super.tearDown();
    }
}
