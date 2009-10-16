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

package org.apache.geronimo.mavenplugins.geronimo.reporting;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.util.PrettyPrintXMLWriter;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;

/**
 * A reporter that generates Surefire result data, so the Surefire report can be used.
 *
 * @version $Rev$ $Date$
 */
public class SurefireReporter
    implements Reporter
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String LS = System.getProperty("line.separator");

    /**
     * The name of the test (goal name).
     */
    private String testName;

    /**
     * The input log file, may or may not exist.
     */
    private File logFile;

    /**
     * The failure cause.
     */
    private Throwable failureCause;

    /**
     * @parameter expression="${project.build.directory}/surefire-reports"
     */
    private File reportsDirectory = null;
    
    /**
     * The file where the test output text will be written.
     */
    private File outputFile;

    /**
     * The file where the text result xml will be written.
     */
    private File reportFile;

    /**
     * The time when the test started.
     */
    private long startTime;

    /**
     * The number of errors.
     */
    private int numErrors = 0;

    private static final int MS_PER_SEC = 1000;

    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);

    private List results = Collections.synchronizedList(new ArrayList());

    //
    // Reporter
    //

    public void reportBegin(final Reportable source) {
        assert source != null;

        logFile = source.getLogFile();
        testName = source.getName();
        startTime = source.getStartTime().getTime();

        try {
            FileUtils.forceMkdir(reportsDirectory);
        }
        catch (IOException e) {
            //
            // HACK: Maybe need to add a throws to beginReport() ?
            //
            
            log.error("Failed to make reports directory: " + reportsDirectory, e);
        }

        outputFile = new File(reportsDirectory, testName + ".txt");
        reportFile = new File(reportsDirectory, "TEST-" + testName + ".xml");
        if (reportFile.exists()) {
            reportFile.delete();
        }
    }

    public void reportError(final Throwable cause) {
        assert cause != null;

        log.debug("Capturing failed report from cause", cause);
        
        this.failureCause = cause;
    }

    public void reportEnd() {
        try {
            if (logFile.exists()) {
                FileUtils.copyFile(logFile, outputFile);
            }
        }
        catch (Exception e) {
            log.warn("Failed to update outputFile", e);
        }
        
        if (failureCause != null) {
            try {
                boolean append = outputFile.exists();
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, append)));
                try {
                    writer.println("Exception Detail");
                    writer.println("================");
                    failureCause.printStackTrace(writer);
                    writer.flush();
                }
                finally {
                    writer.close();
                }
            }
            catch (Exception e) {
                log.warn("Failed to append error detail to outputFile", e);    
            }

            testFailed();
        }
        else {
            testSucceeded();
        }

        try {
            testSetCompleted();
        }
        catch (ReporterException e) {
            log.warn("Failed to set test completed", e);
        }
    }

    //
    // Surefire Support
    //

    private void testSetCompleted() throws ReporterException {
        long runTime = System.currentTimeMillis() - this.startTime;

        Xpp3Dom testSuite = createTestElement("testsuite", testName, runTime);

        showProperties(testSuite);

        testSuite.setAttribute("tests", "1");
        testSuite.setAttribute("errors", String.valueOf(numErrors));
        testSuite.setAttribute("skipped", "0");
        testSuite.setAttribute("failures", "0");

        for (Iterator i = results.iterator(); i.hasNext();) {
            Xpp3Dom testcase = (Xpp3Dom) i.next();
            testSuite.addChild(testcase);
        }

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile), "UTF-8")));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + LS);
            Xpp3DomWriter.write(new PrettyPrintXMLWriter(writer), testSuite);
        }
        catch (UnsupportedEncodingException e) {
            throw new ReporterException("Unable to use UTF-8 encoding", e);
        }
        catch (FileNotFoundException e) {
            throw new ReporterException("Unable to create file: " + e.getMessage(), e);
        }
        finally {
            writer.close();
        }
    }

    private Xpp3Dom createTestElement(String element, String testName, long runTime) {
        Xpp3Dom testCase = new Xpp3Dom(element);
        testCase.setAttribute("name", testName);
        testCase.setAttribute("time", elapsedTimeAsString(runTime));

        return testCase;
    }

    private Xpp3Dom createElement(Xpp3Dom element, String testName) {
        Xpp3Dom component = new Xpp3Dom(testName);
        element.addChild(component);

        return component;
    }

    private void testSucceeded() {
        long runTime = System.currentTimeMillis() - this.startTime;
        Xpp3Dom testCase = createTestElement("testcase", testName, runTime);
        results.add(testCase);
    }

    private void testFailed() {
        ++numErrors;

        try {
            InputStream input = new BufferedInputStream(new FileInputStream(outputFile));
            int length = input.available();
            byte[] b = new byte[length];
            input.read(b, 0, length);
            writeTestProblems(testName, new String(b));
        }
        catch (IOException e) {
            log.error("Failed to write test problems", e);
        }
    }

    private void writeTestProblems(String testName, String stdErr) {
        long runTime = System.currentTimeMillis() - this.startTime;

        Xpp3Dom testCase = createTestElement("testcase", testName, runTime);
        Xpp3Dom element = createElement(testCase, "failure");

        element.setAttribute("message", escapeAttribute(getMessage(stdErr)));
        element.setAttribute("type", getType(stdErr));
        element.setValue(stdErr);

        results.add(testCase);
    }

    /**
     * Adds system properties to the XML report.
     */
    private void showProperties(Xpp3Dom testSuite) {
        Xpp3Dom properties = createElement(testSuite, "properties");

        Properties systemProperties = System.getProperties();

        if (systemProperties != null) {
            Enumeration propertyKeys = systemProperties.propertyNames();

            while (propertyKeys.hasMoreElements()) {
                String key = (String) propertyKeys.nextElement();
                String value = systemProperties.getProperty(key);

                if (value == null) {
                    value = "null";
                }

                Xpp3Dom property = createElement(properties, "property");

                property.setAttribute("name", key);
                property.setAttribute("value", escapeAttribute(value));
            }
        }
    }

    private static String escapeAttribute(String attribute) {
        // Shouldn't Xpp3Dom do this itself?
        String s = StringUtils.replace(attribute, "<", "&lt;");
        return StringUtils.replace(s, ">", "&gt;");
    }

    private Iterator getResults() {
        return results.iterator();
    }

    private String elapsedTimeAsString(long runTime) {
        return numberFormat.format((double) runTime / MS_PER_SEC);
    }

    /**
     * Gets the messages following the exception type.
     */
    private String getMessage(String stdErr) {
        int beginMarker = stdErr.indexOf("Exception:") + 10;
        int endMarker = stdErr.indexOf("\n", beginMarker);
        return stdErr.substring(beginMarker, endMarker);
    }

    /**
     * Gets the type of exception from the stacktrace.
     */
    private String getType(String stdErr) {
        int endMarker = stdErr.indexOf("Exception:") + 9;
        int beginMarker = stdErr.lastIndexOf("\n", endMarker) + 1;
        return stdErr.substring(beginMarker, endMarker);
    }

    /** 
     * The generated reports xml file for surefire..
     * 
     * @return generated reports xml file
     */
    public File getReportsFile() {
        return this.reportFile;
    }

    /**
     * The text file which holds the stdout or stderr.
     *
     * @return File
     */
    public File getOutputFile() {
        return this.outputFile;
    }
}
