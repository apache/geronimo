/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.mavenplugins.testsuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;

import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.codehaus.mojo.pluginsupport.ant.AntHelper;

import org.apache.maven.model.DistributionManagement;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.Server;

import org.codehaus.plexus.util.FileUtils;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.XmlProperty;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;

/**
 * Download the ResultsSummary.html file from the site url.
 * Update it with success rate (in percentage) of the results from each of the top level testsuites.
 * Upload the file back again.
 * 
 * @goal summarize
 *
 * @version $Rev$ $Date$
 */
public class ResultsSummaryMojo
extends MojoSupport {
    //TODO for this to actually work most likely we need to get a more up to date jtidy such as the one formerly in the svn repo version 8.0-20060801
    /**
     * @component
     */
    protected AntHelper ant;

    /**
     * @parameter default-value="${project.build.directory}"
     * @read-only
     */
    private File targetDirectory;

    /**
    * The maven project.
    *
    * @parameter expression="${project}"
    * @required
    * @readonly
    */
    protected MavenProject project = null;

    /**
    * The build settings.
    *
    * @parameter expression="${settings}" default-value="${settings}
    * @required
    * @readonly
    */
    protected Settings settings;

    /**
     * The username
     * 
     * @parameter expression="${username}"
     */
    private String username;

    /**
     * The password
     * 
     * @parameter expression="${password}"
     */
    private String password;

    /**
     * The passphrase
     * 
     * @parameter expression="${passphrase}"
     */
    private String passphrase;

    /**
     * The keyfile
     * 
     * @parameter expression="${keyfile}"
     */
    private String keyFile;

    /**
     * The passphrase
     * 
     * @parameter expression="${buildNumber}"
     */
    private String buildNumber;

    /**
     * show results for only these many tests.
     * 
     * @parameter expression="${numberShown}" default-value="8"
     */
    private int numberShown;

    private NumberFormat numberFormat = NumberFormat.getInstance();

    private static final int PCENT = 100;

    private final String resultsFileName = "ResultsSummary.html";

    private Server server = null;

    private Scp scp;
    private SSHExec ssh;

    protected MavenProject getProject()
    {
        return project;
    }

    protected void init() throws MojoExecutionException, MojoFailureException {
        super.init();

        ant.setProject(getProject());

        String siteId = project.getDistributionManagement().getSite().getId();
        server = settings.getServer(siteId);

        scp = (Scp)ant.createTask("scp");
        scp.setKeyfile(getKeyFile());
        scp.setPassword(getPassword());
        scp.setPassphrase(getPassphrase());
        scp.setTrust(true);

        ssh = (SSHExec)ant.createTask("sshexec");
        ssh.setKeyfile(getKeyFile());
        ssh.setPassword(getPassword());
        ssh.setPassphrase(getPassphrase());
        ssh.setTrust(true);

    }

    private String getKeyFile() {
        if ( keyFile != null ) {
            return keyFile;
        }
        else if ( server != null && server.getPrivateKey() != null ) {
            return server.getPrivateKey();
        }

        return "/home/" + getUsername() + "/.ssh/id_dsa";
    }

    private String getUsername() {
        if ( username != null ) {
            return username;
        }
        else if ( server != null && server.getUsername() != null ) {
            return server.getUsername();
        }

        return System.getProperty("user.name");
    }

    private String getPassword() {
        if ( password != null ) {
            return password;
        }
        else if ( server != null && server.getPassword() != null ) {
            return server.getPassword();
        }

        return " ";
    }

    private String getPassphrase() {
        if ( passphrase != null ) {
            return passphrase;
        }
        else if ( server != null && server.getPassphrase() != null ) {
            return server.getPassphrase();
        }

        return " ";
    }

    /**
     * called by execute from super
     */
    protected void doExecute() throws Exception {
        if ( buildNumber == null ) {
            log.warn("No build number specified; returning");
            return;
        }

        File currentSiteDirectory = new File(targetDirectory, "/site");
        if ( !currentSiteDirectory.exists() ) {
            log.warn("No site directory here; returning");
            return;
        }

        // Download ResultsSummary.html and parse it.
        File resultsFile = null;
        try {
            downloadHTML();
            resultsFile = new File(targetDirectory, resultsFileName);            
        }
        catch ( Exception e ) {
            log.warn("Download failed. " + e.getMessage());
        }

        Tidy tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);

        if ( resultsFile == null || !resultsFile.exists() ) {
            log.info( resultsFileName + " could not be downloaded. Using the template to create anew");
            resultsFile = new File(project.getBasedir(), "src/main/resources/" + resultsFileName);
        }

        FileInputStream is = new FileInputStream( resultsFile );
        Document document = tidy.parseDOM(is, null); 
        is.close();

        File reportsDir = new File(targetDirectory, "surefire-reports");
        if ( !reportsDir.exists() ) {
            log.warn("No surefire-reports directory here");
            return;
        }

        ArrayList files = (ArrayList) FileUtils.getFiles(reportsDir, "TEST-*.xml", null, true);
        if ( files.size() > 0 ) {
            document = insertNewColumn(document);
            if ( document == null ) {
                throw new MojoFailureException("Main table cannot be found in the " + resultsFileName + ". The file may be corrupted");
            }
        }

        for ( Iterator itr=files.iterator(); itr.hasNext(); ) {
            File file = (File) itr.next();
            log.debug("working on " + file.getAbsolutePath() );
            document = processFile(document, file);
        }

        // Use a Transformer for output
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        // write the document back into a temporary file.
        File tempFile = new File(targetDirectory, "ResultsSummary-2.html");
        FileOutputStream os = new FileOutputStream( tempFile );
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(os);
        transformer.transform(source, result); 

        os.flush();
        os.close();

        // tidy the document and create/replace ResultsSummary.html in the target directory
        resultsFile = new File(targetDirectory, resultsFileName);
        is = new FileInputStream( tempFile );
        os = new FileOutputStream( resultsFile );
        tidy.parse(is, os);
        is.close();
        os.close();

        // delete the temp file.
        tempFile.delete();

        try {
            uploadHTML(resultsFile);
        }
        catch ( Exception e ) {
            log.warn("Upload failed. " + e.getMessage());
        }
    }


    private String getRemoteUri() {
        String siteUri = project.getDistributionManagement().getSite().getUrl();

        // chop off the protocol
        int index = siteUri.indexOf("://");
        siteUri = siteUri.substring(index + 3);
        log.debug("siteUri uri is " + siteUri);

        // chop off the buildNumber directory at the end. This is used to deploy site files.
        index = siteUri.lastIndexOf("/");
        siteUri = siteUri.substring(0, index);
        log.debug("siteUri uri is " + siteUri);

        // insert : between the host and path
        index = siteUri.indexOf("/");
        String remoteUri = siteUri.substring(0, index) + ":" + siteUri.substring(index);
        log.debug("siteUri uri is " + remoteUri);


        // construct the uri using username
        remoteUri = getUsername() + ":" + getPassword() + "@" + remoteUri;
        log.info("Remote uri is " + remoteUri);

        return remoteUri;

    }


    /**
     * Download the html from the remote site where it is has been deployed.
     */
    private void downloadHTML()
    {
        String remoteUri = getRemoteUri() + "/" + resultsFileName;

        scp.setFile(remoteUri);
        scp.setTodir(targetDirectory.getAbsolutePath());
        scp.execute();
    }


    /**
     * Upload the html to the remote site where it will be deployed.
     */
    private void uploadHTML(File resultsFile)
    {
        String remoteUri = getRemoteUri();
        scp.setFile( resultsFile.getAbsolutePath() );
        scp.setTodir(remoteUri);
        scp.execute();

        // Use the following block to set 664 perms on the uploaded html file; else synch will fail.
        // ssh is setting the right perms but blocking. setTimeout doesn't seem to work.
        /*
        remoteUri = remoteUri + "/" + resultsFileName;
        int atindex = remoteUri.lastIndexOf("@");
        int index = remoteUri.lastIndexOf(":");
        ssh.setHost(remoteUri.substring(atindex+1, index));
        ssh.setUsername(getUsername());
        ssh.setCommand("chmod 664 " + remoteUri.substring(index+1));
        ssh.setTimeout(30 * 1000);
        ssh.execute();
        */
    }


    /**
     * Append a new column for the latest build. Put the build number in the column header
     */
    private Document insertNewColumn(Document document)
    {
        Element table = getElementById(document.getDocumentElement(), "table", "mainTable");
        if ( table == null ) {
            log.info("table is null");
            return null;
        }

        Element thead = getElementById(table, "thead", "mainTableHead");
        Element tr = (Element) thead.getFirstChild();

        Element td= document.createElement("TD");
        td.setAttribute("class", "servers");

        Element anchor = document.createElement("a");
        anchor.setAttribute("href", "./" + buildNumber + "/surefire-report.html");
        Text text = document.createTextNode(buildNumber);
        anchor.appendChild(text);

        td.appendChild(anchor);
        tr.appendChild(td);

        // increment the cols attribute for the table
        int cols = tr.getChildNodes().getLength();

        // check for number of columns to be shown. 
        // Don't take the suite names column into count.
        if ( cols > (numberShown + 1) ) {
            cols = cleanup(table);
        }

        table.setAttribute("cols", String.valueOf(cols) );


        return document;
    }

    private Document processFile(Document document, File file)
    {
        String pcent = getResultsFromFile(file);

        // strip off TEST- and .xml from the filename to get the suitename
        String fileName = FileUtils.basename(file.getName());
        fileName = fileName.substring(fileName.indexOf("-") + 1);
        fileName = fileName.substring(0, fileName.length()-1);
        document = insertColumn(document, pcent, fileName);

        return document;
    }

    /**
     * Load the surefire-report xml file as an ANT xml property and get the values of the results
     * compute percentage
     */
    private String getResultsFromFile(File xmlFile)
    {
        String prefix = String.valueOf(System.currentTimeMillis());
        loadXMLProperty(xmlFile, prefix);

        String tests = ant.getAnt().getProperty(prefix + ".testsuite.tests");
        String errors = ant.getAnt().getProperty(prefix + ".testsuite.errors");
        String failures = ant.getAnt().getProperty(prefix + ".testsuite.failures");
        String skipped = ant.getAnt().getProperty(prefix + ".testsuite.skipped");

        log.debug("tests: " + tests + "; errors:" + errors + "; failures:" + failures + "; skipped:" + skipped);

        int testsNum = Integer.parseInt(tests);
        int errorsNum = Integer.parseInt(errors);
        int failuresNum = Integer.parseInt(failures);
        int skippedNum = Integer.parseInt(skipped);

        //String pcent = tests + "/" + errors + "/" + failures + " (" + computePercentage(testsNum, errorsNum, failuresNum, skippedNum) + "%)";
        String pcent = tests + "/" + errors + "/" + failures; 
        return pcent;
    }

    /**
     * http://ant.apache.org/manual/CoreTasks/xmlproperty.html
     */
    private void loadXMLProperty(File src, String prefix)
    {
        XmlProperty xmlProperty = (XmlProperty)ant.createTask("xmlproperty");
        xmlProperty.setFile(src);
        if ( prefix != null ) {
            xmlProperty.setPrefix(prefix);
        }
        xmlProperty.setCollapseAttributes(true);
        xmlProperty.execute();
        log.debug("Loaded xml file as ant property with prefix " + prefix);
    }

    /**
     * compute percentage
     */
    public String computePercentage( int tests, int errors, int failures, int skipped )
    {
        float percentage;
        if ( tests == 0 ) {
            percentage = 0;
        }
        else {
            percentage = ( (float) ( tests - errors - failures - skipped ) / (float) tests ) * PCENT;
        }

        return numberFormat.format( percentage );
    }

    /**
     * Insert the rest of the column. If there is no matching row for the suite name, create a new row.
     */
    private Document insertColumn(Document document, String pcent, String suiteName)
    {
        log.debug("inserting column");

        Element table = getElementById(document.getDocumentElement(), "table", "mainTable");
        int cols = Integer.parseInt( table.getAttribute("cols") );

        Element tr = getElementById(table, "tr", suiteName);

        if ( tr != null ) {
            // creating empty cells in the cols for the previous failed builds.
            NodeList nodeList = tr.getElementsByTagName("td");
            Element td;
            for ( int i=nodeList.getLength()+1; i<cols; i++ ) {
                td = document.createElement("TD");
                td.setAttribute("class", "cell");                
                tr.appendChild(td);
            }

            td = document.createElement("TD");
            td.setAttribute("class", "cell");

            Element anchor = document.createElement("a");
            anchor.setAttribute("href", "./" + buildNumber + "/" + suiteName + "/surefire-report.html");
            Text text = document.createTextNode(pcent);
            anchor.appendChild(text);

            td.appendChild(anchor);
            tr.appendChild(td);
        }
        else {
            log.debug("Creating a new row for a new suite");
            tr = document.createElement("TR");
            tr.setAttribute("id", suiteName);

            Element td = document.createElement("TD");
            td.setAttribute("class", "suite");
            Text text = document.createTextNode(suiteName);
            td.appendChild(text);
            tr.appendChild(td);

            // creating empty cells in the cols for the previous builds.
            for ( int i=1; i<cols; i++ ) {
                td = document.createElement("TD");
                td.setAttribute("class", "cell");                
                tr.appendChild(td);
            }

            Element anchor = document.createElement("a");
            anchor.setAttribute("href", "./" + buildNumber + "/" + suiteName + "/surefire-report.html");
            text = document.createTextNode(pcent);
            anchor.appendChild(text);
            td.appendChild(anchor);

            table.appendChild(tr);
        }

        log.debug("inserted column");

        return document;
    }

    /**
     * Get a child element identified by an ID
     */
    private Element getElementById(Element element, String tagName, String id)
    {
        log.debug("Searching for tag " + tagName + " with id=" + id);

        Element foundElement = null;

        NodeList nodeList = element.getElementsByTagName(tagName);

        for ( int i=0; i<nodeList.getLength(); i++ ) {
            foundElement = (Element) nodeList.item(i);
            log.debug("Element is " + foundElement.getTagName() + " " + foundElement.getAttribute("id") );
            if ( id.trim().equals(foundElement.getAttribute("id").trim()) ) {
                break;
            }
            else {
                foundElement = null;
            }
        }

        return foundElement;
    }

    /**
     * Removes the oldest test column(s) from the table based on the value set in 'numberShown' variable
     */
    private int cleanup(Element table)
    {
        log.info("Removing oldest column");

        NodeList nodeList = table.getElementsByTagName("tr");

        int new_cols = 0;
        for ( int i=0; i<nodeList.getLength(); i++ ) {
            Element tr = (Element) nodeList.item(i);
            Element suiteColumn = (Element) tr.getFirstChild();
            Element removeMe = (Element) suiteColumn.getNextSibling();
            tr.removeChild(removeMe);

            // get the count from just the header row since only the header has been added yet
            if ( i==0 ) {
                new_cols = tr.getChildNodes().getLength();
            }
        }

        if ( new_cols > (numberShown + 1) ) {
            new_cols = cleanup(table);
        }

        log.debug( String.valueOf("Returning cols: " + new_cols) );

        return new_cols;
    }
}
