/*
 * Apache 2.0 license
 *
*/
package org.apache.geronimo.installer.processing;
import org.apache.xerces.dom.TextImpl;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Text;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import java.lang.reflect.Method;
import java.util.Vector;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class ConfigInstaller  {
   // read the configure.xml
   // punch it into LocalConfigInstaller along with moving any 
   //    exes necessary (do proper chmods)
   protected static String installRoot = null;
   protected static String xmlName = null; // offset from installRoot
   protected static boolean fTrace = false;
   protected static URLClassLoader cl = null;

   protected static void initClassLoader() {
      Vector vjars = new Vector();
      try {
         getJars( new File(installRoot), vjars );
      } catch( Exception e ) {
         throw new RuntimeException( "unable to build list of jars for classloader" );
      }
      Object ojars[] = vjars.toArray();
      URL jars[] = new URL[ ojars.length ];
      System.arraycopy( ojars, 0, jars, 0, ojars.length );
      //for( int i = 0; i < jars.length; ++i ) {
      //   System.out.println( "jar: " + jars[i].toString() );
      //}
      cl = new URLClassLoader( jars, Thread.currentThread().getContextClassLoader() );
      //System.out.println( "CL: " + cl );
   }

   protected static void getJars( File baseDir, Vector jars ) throws Exception {
      File files[] = baseDir.listFiles(); 
      for( int i = 0; i < files.length; ++i ) {
         if( files[ i ].isDirectory() ) {
            getJars( files[ i ], jars );
         } else {
            String name = files[ i ].getCanonicalPath();
            String nameUp = name.toUpperCase();
            if( nameUp.endsWith( ".JAR" )) {
                jars.add( files[ i ].toURL() );
                //System.out.println( "getJars: added " + files[ i ].toURL() );
            }
         }
      }
   }

   // args:
   //    installRoot
   //    configuration xml (var/config/configure.xml)

   public static void main( String args[] ) {
      if( args.length >= 1 ) 
         installRoot = args[0];
      if( args.length >= 2 )
         xmlName = args[1];
      if( args.length >=3 ) {
         String parts[] = args[2].split("=");
         String varName = parts[0];
         String value = parts[1];
         System.out.println( "Found var: " + varName + " value = " + value );
         if( varName.equalsIgnoreCase( "trace" )) {
            fTrace = false;
            if( value.equalsIgnoreCase( "true" ))
               fTrace = true;
         }
      }
      if( installRoot == null || xmlName == null ) {
         throw new RuntimeException( "ConfigInstaller: installRoot and xmlname required" );
      }
      initClassLoader();
      ConfigThread ct = new ConfigThread();
      ct.setContextClassLoader( cl );
      ct.start();
      trace( "Joining ConfigThread..." );
      try { ct.join(); } catch( Exception e ) { };
      trace( "ConfigThread done." );
      System.out.println( "Configuration complete." );
   }
   protected static void trace( String output ) {
      if( fTrace ) {
         System.out.println( output );
      }
   }

}
class ConfigWorker {
   protected Class installerCls = null;
   protected Object installerObj = null;
   protected Method execute = null;
   protected Method setTargetRoot = null;
   protected Method setTargetConfigStore = null; //"config-store"
   protected Method setTargetRepository = null; //"repository"
   protected Method setSourceRepository = null; //"${maven.repo.local}"
   protected Method setArtifact = null;
   protected DOMParser parser = null;
   protected boolean fTrace = false;

   protected void trace( String output ) {
      if( fTrace ) {
         System.out.println( output );
      }
   }

   protected void doit( ClassLoader cl, Boolean fTrace ) {
      //ConfigWorker cw = new ConfigWorker();
      this.fTrace = fTrace.booleanValue();
      trace( ">ConfigWorker.doit()" );
      if( init( cl ) && parse() ) {
         processXML();
      }
      trace( "<ConfigWorker.doit()" );
   }
   protected ConfigWorker() {
   }

   protected void initLocalConfigInstaller( ClassLoader cl ) {
      trace( ">ConfigWorker.initLocalConfigInstaller()" );
      try {
         installerCls = cl.loadClass( "org.apache.geronimo.plugin.assembly.LocalConfigInstaller" );
      } catch( Exception e ) {
         
         throw new RuntimeException( "Unable to load LocalConfigInstaller class" );
      }
      try {
         execute = installerCls.getDeclaredMethod( "execute", null );
      } catch( Exception e ) {
         throw new RuntimeException( "Unable to get method object \"execute\" from LocalConfigInstaller class" );
      }
      try {
         setTargetRoot = installerCls.getSuperclass().getDeclaredMethod( "setTargetRoot", new Class[] { File.class } );
      } catch( Exception e ) {
         throw new RuntimeException( "Unable to get method object \"setTargetRoot\" from LocalConfigInstaller class" );
      }
      try {
         setTargetConfigStore = installerCls.getSuperclass().getDeclaredMethod( "setTargetConfigStore", new Class[] { String.class } );
      } catch( Exception e ) {
         throw new RuntimeException( "Unable to get method object \"setTargetConfigStore\" from LocalConfigInstaller class" );
      }
      try {
         setTargetRepository = installerCls.getSuperclass().getDeclaredMethod( "setTargetRepository", new Class[] { String.class } );
      } catch( Exception e ) {
         throw new RuntimeException( "Unable to get method object \"setTargetRepository\" from LocalConfigInstaller class" );
      }
      try {
         setSourceRepository = installerCls.getSuperclass().getDeclaredMethod( "setSourceRepository", new Class[] { File.class } );
      } catch( Exception e ) {
         throw new RuntimeException( "Unable to get method object \"setSourceRepository\" from LocalConfigInstaller class" );
      }
      try {
         setArtifact = installerCls.getSuperclass().getDeclaredMethod( "setArtifact", new Class[] { String.class } );
      } catch( Exception e ) {
         throw new RuntimeException( "Unable to get method object \"setArtifact\" from LocalConfigInstaller class" );
      }
      try {
         installerObj = installerCls.newInstance();
      } catch( Exception e ) {
         throw new RuntimeException( "Unable to create instanceof LocalConfigInstaller" );
      }
      trace( "<ConfigWorker.initLocalConfigInstaller()" );
   }
   
   protected boolean init(ClassLoader cl) {
      boolean fRet = true;
      trace( ">ConfigWorker.init()" );
      initLocalConfigInstaller(cl);
      try {
         parser = new DOMParser();
      } catch (Exception e) {
         System.err.println("error: Unable to instantiate XML parser");
         fRet = false;
      }
      
      trace( "<ConfigWorker.init()" );
      return fRet;
   }


   protected boolean parse() {
      boolean fRet = true;
      trace( ">ConfigWorker.parse()" );
      try {
      parser.parse( ConfigInstaller.installRoot + '/' + ConfigInstaller.xmlName );
      } catch( Exception e ) {
         System.err.println( "Exception while parsing: " + ConfigInstaller.installRoot + "/" + ConfigInstaller.xmlName );
         fRet = false;
      }
      trace( "<ConfigWorker.parse()" );
      return fRet;
   }
   protected void processXML() { 
      trace( ">ConfigWorker.processXML()" );
      Document doc = parser.getDocument();
      Node node = doc.getDocumentElement();
      processConfigurations(node);
      trace( "<ConfigWorker.processXML()" );
   }
   protected int numElements = 0;
   protected boolean fSelected = false;
   protected String artifact = null;
   protected String executable = null;

   public void processConfigurations(Node node) {
      trace( ">ConfigWorker.processConfigurations()" );

        // is there anything to do?
        if (node == null) {
            return;
        }
        String nodeName = node.getNodeName();
        //System.out.println( "nodeName: " + nodeName );
        if( nodeName.equalsIgnoreCase("configurations") == false ) {
           throw new RuntimeException("ConfigInstaller: Malformed XML.  \"configurations\" element expected.");
        }
        Node child = node.getFirstChild();
        while (child != null) {
           int nodeType = child.getNodeType();
           if( nodeType == Node.ENTITY_REFERENCE_NODE
               || nodeType == Node.ENTITY_NODE
               || nodeType == Node.ELEMENT_NODE )
              processConfiguration(child);
           child = child.getNextSibling();
        }
      trace( "<ConfigWorker.processConfigurations()" );
  }

  public void processConfiguration( Node node ) {
      trace( ">ConfigWorker.processConfiguration()" );
        // is there anything to do?
        if (node == null) {
            return;
        }
        artifact = null;
        executable = null;
        fSelected = false;

        String nodeName = node.getNodeName();
        //System.out.println( "nodeName: " + nodeName );        
        if( nodeName.equalsIgnoreCase("configuration") == false ) {
           throw new RuntimeException("ConfigInstaller: Malformed XML.  \"configuration\" element expected. (" + nodeName + ")" );
        }
        Node child = node.getFirstChild();
        while (child != null) {
           int nodeType = child.getNodeType();
           if( nodeType != Node.ENTITY_REFERENCE_NODE 
               && nodeType != Node.ENTITY_NODE 
               && nodeType != Node.ELEMENT_NODE ) {
              child = child.getNextSibling();
              continue;
           }
           nodeName = child.getNodeName();
           if( nodeName.equalsIgnoreCase( "selected" )) {
              Text text = (Text)child.getFirstChild();
              fSelected = false;
              if( text.getNodeValue().trim().equalsIgnoreCase( "true" )) {
                 fSelected = true;
              }
           }
           else if( nodeName.equalsIgnoreCase( "executable" )) {
              Text text = (Text)child.getFirstChild();
              executable = text.getNodeValue();
              executable = executable.trim();
           }
           else if( nodeName.equalsIgnoreCase( "artifact" )) {
              Text text = (Text)child.getFirstChild();
              artifact = text.getNodeValue();
              artifact = artifact.trim();
           }
           else {
              throw new RuntimeException( "ConfigInstaller: Malformed XML. Element of type \"selected\" or \"executable\" or \"artifact\" expected. (" + nodeName + ")" );
           }
           child = child.getNextSibling();
        }
        if( fSelected ) {
           //System.out.println( "Install config.  A: " + artifact + " E: " + executable );
           doInstall();
           
        } else {
           //System.out.println( "NO Install config.  A: " + artifact + " E: " + executable );
        }
      trace( "<ConfigWorker.processConfiguration()" );
  }
  protected void doInstall() {
     trace( ">ConfigWorker.doInstall()" );
     //System.out.println( "doInstall()" );
     doMethod( setTargetConfigStore, new Object[] { "config-store" } );
     doMethod( setTargetRepository, new Object[]  { "repository" } );
     doMethod( setArtifact, new Object[] { artifact } );
     doMethod( setTargetRoot, new Object[] { new File( ConfigInstaller.installRoot ) } );
     doMethod( setSourceRepository, new Object[] { new File( ConfigInstaller.installRoot + "/repository" ) } );
     doMethod( execute, new Object[] { } );
     trace( "<ConfigWorker.doInstall()" );
  }
  protected void doMethod( Method m, Object[] parms ) {
     trace( ">ConfigWorker.doMethod()" );
     try {
        m.invoke( installerObj, parms );
     } catch( Exception e ) {
        System.err.println( e.getMessage() );
        e.printStackTrace();
        throw new RuntimeException( "Exception while executing: LocalConfigInstaller." + m.getName() );
     }
      trace( "<ConfigWorker.doMethod()" );
  }
}

class ConfigThread extends Thread {
   public void run() {
      //ConfigWorker.doit(); <-- this would be nice, but...
      // using reflection is required to get the classloader override to work
      Class cwc = null;
      Object cw = null;
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      //System.out.println( "CL: " + cl.toString() );
      Boolean fTrace = new Boolean( ConfigInstaller.fTrace );
      try {
         cwc = cl.loadClass( "org.apache.geronimo.installer.processing.ConfigWorker" );
         cw = cwc.newInstance();
      } catch( Exception e ) {
         e.printStackTrace();
         throw new RuntimeException( "Unable to load ConfigWorker class" );
      }
      Method doit = null;
      try {
         doit = cwc.getDeclaredMethod( "doit", new Class[] { ClassLoader.class, Boolean.class } );
      } catch( Exception e ) {
         e.printStackTrace();
         throw new RuntimeException( "Unable to load ConfigWorker doit method" );
      }
      try {
        doit.invoke( cw, new Object[] { cl, fTrace }  );
     } catch( Exception e ) {
        e.printStackTrace();
        throw new RuntimeException( "Exception while executing: ConfigWorker.doit()"  );
     }

  }
}
