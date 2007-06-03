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
/*
 * Copyright 2005 Jeff Genender.
 */

/**
 * @author Jeff Genender
 * @author Grzegorz Slowikowski
 */

package org.apache.geronimo.mavenplugins.jspc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.jasper.JspC;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.InterpolationFilterReader;

public abstract class AbstractJspcMojo extends AbstractMojo {

    private static final String PS = File.pathSeparator;

    private static final String DEFAULT_INJECTION = "</web-app>";

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Character encoding
     * 
     * @parameter
     */
    protected String javaEncoding;

    /**
     * Provide source compatibility with specified release
     * 
     * @parameter
     */
    protected String source;

    /**
     * Generate class files for specific VM version
     * 
     * @parameter
     */
    protected String target;

    /**
     * The working directory to create the generated java source files.
     * 
     * @parameter expression="${project.build.directory}/jsp-source"
     * @required
     */
    protected String workingDirectory;
    
    /**
     * Sets if you want to compile the jsp classes
     * 
     * @parameter default-value="true"
     */
    protected boolean setCompile;

    /**
     * The path and location to the web fragment file.
     * 
     * @parameter expression="${project.build.directory}/web-fragment.xml"
     * @required
     */
    protected String webFragmentFile;

    /**
     * Source directory of the web source.
     * 
     * @parameter expression="${basedir}/src/main/webapp"
     * @required
     */
    protected String warSourceDirectory;

    /**
     * Jsp files to include in compile,
     * specified by comma seperated list of org.apache.tools.ant.DirectoryScanner patterns
     *
     * @parameter
     */
    protected String jspFileIncludes;

    /**
     * Jsp files to exclude from compile,
     * specified by comma seperated list of org.apache.tools.ant.DirectoryScanner patterns
     *
     * @parameter
     */
    protected String jspFileExcludes;
    
    /**
     * Set this to false if you don't want to include the compiled JSPs
     * in your web.xml nor add the generated sources to your project's
     * source roots.
     *
     * @parameter default-value="true"
     */
    protected boolean includeInProject;
 
    /**
     * The path and location of the original web.xml file
     * 
     * @parameter expression="${basedir}/src/main/webapp/WEB-INF/web.xml"
     * @required
     */
    protected String inputWebXml;

    /**
     * The string to look for in the web.xml to replace with the web fragment
     * contents.  If not defined, fragment will be appended before the
     * &lt;/webapp&gt; tag which is fine for srvlet 2.4 and greater.  If using this 
     * parameter its recommanded to use Strings such as 
     * &lt;!-- [INSERT FRAGMENT HERE] --&gt;.  Be aware the &lt; and
     * &gt; are for your pom verbatim.  
     * 
     * @parameter 
     */
    protected String injectString;

    /**
     * The final path and file name of the web.xml.
     * 
     * @parameter expression="${project.build.directory}/jspweb.xml"
     * @required
     */
    protected String outputWebXml;

    /**
     * The package in which the jsp files will be contained.
     * 
     * @parameter default-value="jsp"
     */
    protected String packageName;

    /**
     * Verbose level option for JcpC.i Defaults to 0.
     * 
     * @parameter default-value="0"
     */
    protected int verbose;

    /**
     * Show Success option for JcpC.
     * 
     * @parameter default-value="true"
     */
    protected boolean showSuccess;
    
    /**
     * Set Smap Dumped option for JcpC.
     * 
     * @parameter default-value="false"
     */
    protected boolean setSmapDumped;
    
    /**
     * Set Smap Supressed option for JcpC.
     * 
     * @parameter default-value="false"
     */
    protected boolean setSmapSupressed;

    /**
     * List Errors option for JcpC.
     * 
     * @parameter default-value="true"
     */
    protected boolean listErrors;

    /**
     * Validate XML option for JcpC.
     *
     * @parameter default-value="false"
     */
    protected boolean validateXml;

    /**
     * Removes the spaces from the jsps
     * @parameter default-value="true"
     */
    protected boolean trimSpaces;

    /**
     * Provides filtering of the generated web.xml text
     * @parameter default-value="true"
     */
    protected boolean filtering;

    protected abstract List getClasspathElements();


    public void execute() throws MojoExecutionException {
        boolean isWar = "war".equalsIgnoreCase( project.getPackaging() );
        if ( !isWar || !includeInProject) {
            if ( getLog().isDebugEnabled() )
                getLog().debug( "Compiled JSPs will not be added to the project and web.xml will not be modified, either because includeInProject is set to false or because the project's packaging is not \"war\"." );
        }

        if ( getLog().isDebugEnabled() ) {
            getLog().debug( "Source directory: " + warSourceDirectory );
            getLog().debug( "Classpath: " + getClasspathElements().toString().replace( ',', '\n' ) );
            getLog().debug( "Output directory: " + workingDirectory/*getOutputDirectory()*/ );
        }

        try {
            // Create target directories if they don't already exist
            FileUtils.forceMkdir( new File(workingDirectory));
            FileUtils.forceMkdir( new File(webFragmentFile).getParentFile() );
            FileUtils.forceMkdir( new File(outputWebXml).getParentFile() );
            FileUtils.forceMkdir( new File(project.getBuild().getDirectory()) );
            FileUtils.forceMkdir( new File(project.getBuild().getOutputDirectory()) );

            // Find the tools.jar and add it to the classpath
            String toolsJar = null;
            if (System.getProperty("os.name").equals("Mac OS X")) {
                toolsJar = System.getProperty("java.home")
                    + "/../Classes/classes.jar";
            } else {
                toolsJar = System.getProperty("java.home") + File.separatorChar
                    + ".." + File.separatorChar + "lib"
                    + File.separatorChar + "tools.jar";
            }

            // Create the JspC arguments
            List classpathElements = getClasspathElements();
            String classPath = getPathString(classpathElements);
            ArrayList args = getJspcArgs(classPath);
            String strArgs[] = (String[]) args.toArray(new String[args.size()]);

            // JspC needs URLClassLoader
            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            JspcMojoClassLoader cl = new JspcMojoClassLoader(parent);

            // Add the tools.jar
            cl.addURL(new File(toolsJar).getAbsoluteFile().toURL());

            Thread.currentThread().setContextClassLoader(cl);

            try {
                // Run JspC
                JspC jspc = new JspC();
                jspc.setArgs(strArgs);
                jspc.setSmapDumped(setSmapDumped);
                jspc.setSmapSuppressed(setSmapSupressed);
                jspc.setCompile(setCompile);
                jspc.setValidateXml(validateXml);
                jspc.setTrimSpaces(trimSpaces);
                jspc.setVerbose(verbose);
                
                //Fail on error - important
                jspc.setFailOnError(true);
                
                if (source != null)
                    jspc.setCompilerSourceVM(source);
                if (target != null)
                    jspc.setCompilerTargetVM(target);
                
                jspc.execute();
                
                if (setCompile && isWar)
                {
                    moveClassFiles();
                }
            } finally {
                // Set back the old classloader
                Thread.currentThread().setContextClassLoader(cl);
            }

            if ( isWar && includeInProject ) {
                writeWebXml();
                project.addCompileSourceRoot(workingDirectory);
            }

        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("JSPC Error", e);
        }
    }

    private ArrayList getJspcArgs(String classPath)
        throws MojoExecutionException {
        ArrayList args = new ArrayList();
        args.add("-uriroot");
        args.add(warSourceDirectory);
        args.add("-d");
        args.add(workingDirectory);
        if (javaEncoding != null) {
            args.add("-javaEncoding");
            args.add(javaEncoding);
        }
        if (showSuccess) {

            args.add("-s");
        }
        if (listErrors) {
            args.add("-l");
        }
        args.add("-webinc");
        args.add(webFragmentFile);
        args.add("-p");
        args.add(packageName);
        args.add("-classpath");
        args.add(classPath);

        if(jspFileIncludes != null)
        {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(warSourceDirectory);

            // Build includes
            String[] includes = jspFileIncludes.split(",");
            scanner.setIncludes(includes);
            
            // Build excludes
            if(jspFileExcludes != null)
            {
            	String[] excludes = jspFileExcludes.split(",");
            	scanner.setExcludes(excludes);
            }
            
            scanner.scan();
            String[] files = scanner.getIncludedFiles();
        	for(int i=0; i < files.length; i++)
        	{
        		args.add(warSourceDirectory + File.separator + files[i]);
        	}
        }
        
        this.getLog().debug("jspc args: " + args);

        return args;
    }

    private void writeWebXml() throws Exception {
        String webXml = getFile(inputWebXml);
        String fragmentXml = getFile(webFragmentFile);
        
        String searchString = injectString;
        if (searchString == null)
            searchString = DEFAULT_INJECTION;

        int pos = webXml.indexOf(searchString);

        if (pos < 0) {
            throw new MojoExecutionException("injectString('" + searchString
                    + "') not found in webXml(" + inputWebXml + "')");
        }

        String output = webXml.substring(0, pos)
            + fragmentXml
            + ((injectString == null) ? DEFAULT_INJECTION : "")
            + webXml
            .substring(pos + searchString.length(), webXml.length());
        
        FileWriter fw = new FileWriter(outputWebXml);
        
        //allow generated xml to be filtered
        if( isFiltering() ){
           List filtered = filterText( output, "UTF-8", getFilterWrappers(), getBuildFilterProperties() );
           output = "";
           for(Iterator i = filtered.iterator(); i.hasNext();) {
               output += (String)i.next() + "\n";
           }
        }  
    
        fw.write(output);
        fw.close();
    }

    /* filter support code */
   private Map getBuildFilterProperties() throws MojoExecutionException {
       //just supports pom properties for now - not outside files etc
       Map filterProperties = new Properties();
       filterProperties.putAll( project.getProperties() );
       return filterProperties;
   }

   protected FilterWrapper[] getFilterWrappers() {
       return new FilterWrapper[] {
               // support ${token}
               new FilterWrapper() {
                   public Reader getReader(Reader fileReader, Map filterProperties) {
                       return new InterpolationFilterReader( fileReader, filterProperties, "${", "}" );
                   }
               },
               // support @token@
               new FilterWrapper() {
                   public Reader getReader(Reader fileReader, Map filterProperties) {
                       return new InterpolationFilterReader( fileReader, filterProperties, "@", "@" );
                   }
               }
           };
   }
   
   /**
    * Filters text replacing interpolated strings with values from a mvn <property>
    * @param source the file to read
    * @param encoding the file encoding (UTF-8 etc)
    * @param wrappers an array of filter wrappers
    * @param filterProperties
    * @return A List<String> of filtered lines from the original file
    */
   protected List filterText(String source, String encoding, FilterWrapper[] wrappers, Map filterProperties) {
       Reader fileReader = null;

       List contents = new ArrayList();
       
       try{
           if( encoding == null || encoding.length() < 1 )
               fileReader = new BufferedReader( new InputStreamReader( IOUtils.toInputStream( source ) ) );
           else
               fileReader = new BufferedReader( new InputStreamReader( IOUtils.toInputStream( source ), encoding ) );

           Reader reader = fileReader;
           Set props = filterProperties.entrySet();
           getLog().debug( "Filtering with : " + props.toString() );

           for(int i=0; i<wrappers.length; i++) {
               reader = wrappers[i].getReader( reader, filterProperties );
           }
           contents = IOUtils.readLines(reader);
           
       }catch(IOException e){
           getLog().error( "Error occured during filtering: " + e );
       }
       return contents;
   }
    

    private String getFile(String fName) throws Exception {
        File f = new File(fName);

        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        StringBuffer sb = new StringBuffer();
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            sb.append(line);
            sb.append("\n");
        }
        br.close();
        fr.close();

        return sb.toString();
    }

    public String getPathString(List pathElements) {
        StringBuffer sb = new StringBuffer();

        for (Iterator it = pathElements.iterator(); it.hasNext();) {
            sb.append(it.next()).append(PS);
        }

        return sb.toString();
    }

    protected void moveClassFiles() {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir(workingDirectory);

        scanner.setIncludes(new String[] { "**/*.class" });

        scanner.scan();

        String[] sourceDirectorySources = scanner.getIncludedFiles();

        for (int j = 0; j < sourceDirectorySources.length; j++) {
            File from = new File(workingDirectory, sourceDirectorySources[j]);
            File to = new File(project.getBuild().getOutputDirectory(), sourceDirectorySources[j]);
            File parent = to.getParentFile();
            if (!parent.exists())
        	parent.mkdirs();
            from.renameTo(to);
        }
    }

   public boolean isFiltering() {
       return filtering;
   }

   public void setFiltering(boolean filtering) {
       this.filtering = filtering;
   }
   
   protected interface FilterWrapper {
       public abstract Reader getReader(Reader fileReader, Map filterProperties);
   }

}
