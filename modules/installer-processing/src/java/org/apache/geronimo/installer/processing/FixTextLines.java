/**
 *
 * Copyright 2003-2006 The Apache Software Foundation
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
package org.apache.geronimo.installer.processing;
import java.io.*;

public class FixTextLines {
   // get the install path
   // get list of file types to fix
   // search for file types and fix them
   protected static String fileTypes[] = {
      ".dtd",
      ".ent",
      ".htm",
      ".html",
      ".java",
      ".js",
      ".jsp",
      ".properties",
      ".sql",
      ".txt",
      ".wsdl",
      ".xml",
      ".xsd",
      ".xsl",
      "STATUS"
   };
   protected static String installPath = null;
   protected static String tempSubDir = "var/temp";
   protected static String tempPath = null;

   public static void main( String argv[] ) {
      String outStr = null;
      if( argv.length < 1 ) {
         outStr = "FixTextLines requires the path of the Geronimo installation.";
         System.out.println( outStr );
         throw new RuntimeException( outStr );
      }
      installPath = argv[0];
      tempPath = installPath + "/" + tempSubDir;
      File tempPathFile = new File( tempPath );
      if( tempPathFile.exists() == false ) {
         outStr = "FixTextLines: temporary directory does not exist.  Check installPath.";
         System.out.println( outStr );
         throw new RuntimeException( outStr );
      } 
      if( tempPathFile.isDirectory() == false ) {
         outStr = "FixTextLines: temporary directory exists, but is not a directory.  Check installPath.";
         System.out.println( outStr );
         throw new RuntimeException( outStr );
      }
      FixTextLines ftl = new FixTextLines();
      ftl.fixFiles( new File( installPath ));
      System.out.println( "FixTextLines processing complete." );
   }

   public void fixFiles( File dir ) {
      File files[] = null;
      try { 
        files = dir.listFiles();
      } catch( Exception e ) {
         System.err.println( "FixTextLines: error obtaining list of files to process -" + dir.getPath() );
      }
      for( int i = 0; i < files.length; ++i ) {
         String path = null;
         try {
             path = files[i].getCanonicalPath();
         } catch( Exception e1 ) {
             System.err.println("FixTextLines: error getting file path name." );
         }
         if( files[i].isDirectory( )) {
            // recurse into all directories except .../var/temp
            if( path.equalsIgnoreCase( tempPath ) == false ) {
               fixFiles( files[ i ] );
            }
         } else if( isToBeFixed( files[ i ] )) {
            fixCrLf( files[ i ] );
         }
      }
   }
   public boolean isToBeFixed( File file ) {
      boolean fRet = false;
      for( int i = 0; i < fileTypes.length; ++i ) {
         String name = file.getName();
         if( fileTypes[i].startsWith( "." )) {
            int idx = name.lastIndexOf( "." );
            if( idx > -1 ) {
                if( fileTypes[i].equals( name.substring( idx ))) {
                   fRet = true;
                   break;
                }
            }
         } else {
            if( name.equals( fileTypes[i] )) {
                fRet = true;
                break;
            } 
         }
      }
      return fRet;
   }
   public void fixCrLf( File file ) {
      // copy the file to temp dir while making change
      // move original file to ".original"
      // move temp version to original directory
      // delete ".original" version if everything completes successfully
      boolean fErr = false;
      BufferedReader br = null;
      BufferedWriter bw = null;
      try {
          br = new BufferedReader( new FileReader( file ));
      } catch( IOException ioe ) {
         System.err.println( "FixTextLines: " + file.getPath() + " cannot be opened." );
         fErr = true;
      }
      String tmpname = tempPath + "/" + file.getName();
      File tmpFile = new File( tmpname );
      if( fErr == false ) {
         try {
            bw = new BufferedWriter( new FileWriter( tmpFile ));
         } catch( IOException ioe ) {
            System.err.println( "FixTextLines: " + tmpname + " cannot be created." );
            fErr = true;
         }
      }
      if( fErr == false ) {
        try {
            String inStr = br.readLine();
            while( inStr != null ) {
                bw.write( inStr );
                bw.newLine();
                inStr = br.readLine();
            }
            bw.newLine();
            bw.close();
            br.close();
        } catch( IOException ioe ) {
           fErr = true;
           System.err.println( "FixTextLines: Error adjusting CRLF format for: " + file.getPath() + " during copy." );
        }  
      }
      if( fErr == false ) {
         try {
            boolean fSuccess = false;
            String originalName = file.getCanonicalPath();
            fSuccess = file.renameTo( new File( file.getCanonicalPath() + ".original" ));
            if( fSuccess ) {
               fSuccess = tmpFile.renameTo( new File( originalName ) );
               if( fSuccess ) {
                  //System.out.println( originalName + " successfully processed." );
                  File orig = new File( originalName + ".original" );
                  orig.delete(); // delete the original (xxx.original)
               } else {
                  System.err.println( "FixTextLines: " + originalName + " final rename failed." );
                  fErr = true;
               }
            }
         } catch( Exception e ) {
            System.err.println( "FixTextLines: Error moving files. " + file.getPath() );
            fErr = true;
         }
      }
   }
}
