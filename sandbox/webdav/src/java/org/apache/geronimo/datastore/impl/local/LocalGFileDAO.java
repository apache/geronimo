/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.datastore.impl.local;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.geronimo.datastore.GFile;
import org.apache.geronimo.datastore.impl.DAOException;
import org.apache.geronimo.datastore.impl.GFileDAO;
import org.apache.geronimo.datastore.impl.GFileTO;

/**
 * DAO for a local filesystem. It works approximately and needs re-working.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class LocalGFileDAO
    implements GFileDAO
{

    /**
     * Root directory. All the paths are resolved based on this root directory.
     */
    private final File root;

    /**
     * Creates a DAO resolving paths based on the provided root. 
     * 
     * @param aRoot Root.
     */
    public LocalGFileDAO(File aRoot) {
        root = aRoot;
    }
    
    public void create(GFileTO aFileTO)
        throws DAOException {
        File target = new File(root, aFileTO.getPath());
        
        if ( target.exists() ) {
            throw new DAOException("{" + target.getAbsolutePath() +
                "} already exist. Can not write state.");
        }
        
        if ( GFile.NULL != aFileTO.getStream() ) {
            writeFileContent(aFileTO, target);
        }
        
        writeProperties(aFileTO, target);
    }

    public GFileTO read(String aPath)
        throws DAOException {
        File target = new File(root, aPath);
        File metaDataFile = getMetaDataFile(target);
        
        if ( !metaDataFile.exists() ) {
            GFileTO fileTO = new GFileTO(aPath, new HashMap(), GFile.UNSET);
            fileTO.setExists(false);
            return fileTO;
        }
        
        Map properties = new HashMap();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(metaDataFile));
            String line;
            while ( null != (line = reader.readLine()) ) {
                int separator = line.indexOf('=');
                properties.put(line.substring(0, separator),
                    line.substring(separator + 1));
            }
        } catch (IOException e) {
            throw new DAOException(e);
        } finally {
            try {
                if ( null != reader ) { reader.close(); }
            } catch (IOException e) {}
        }
        
        GFileTO fileTO = new GFileTO(aPath, properties, GFile.UNSET);
        fileTO.setDirectory(target.isDirectory());
        fileTO.setExists(target.exists());
        fileTO.setFile(target.isFile());
        return fileTO;
    }
    
    public void update(GFileTO aFileTO)
        throws DAOException {
        File target = new File(root, aFileTO.getPath());
        
        if ( GFile.NULL == aFileTO.getStream() ) {
            target.delete();
        } else if ( GFile.UNSET != aFileTO.getStream() ) {
            writeFileContent(aFileTO, target);
        }
        
        writeProperties(aFileTO, target);
    }
    
    public void delete(String aPath)
        throws DAOException {
        File target = new File(root, aPath);
        
        if ( !target.exists() ) {
            throw new DAOException("{" + target.getAbsolutePath() + 
                "} does not exist. Can not delete.");
        }
        target.delete();
        
        File metadaFile = getMetaDataFile(target);
        metadaFile.delete();
    }
    
    public String[] listFiles(String aPath) throws DAOException {
        File target = new File(root, aPath);
        return target.list();
    }
    
    public InputStream getInputStream(String aPath) throws DAOException {
        File target = new File(root, aPath);
        try {
            return new FileInputStream(target);
        } catch (FileNotFoundException e) {
            throw new DAOException(e);
        }
    }
    
    public void flush() {
    }
    
    public void unflush() {
    }
    
    /**
     * Gets the file storing the meta-data of the provided file.
     * 
     * @param aFile File whose meta-data file should be returned.
     * @return File pointing the meta-data.
     * @throws DAOException Indicates that the meta-data file can not be
     * located.
     */
    private File getMetaDataFile(File aFile)
        throws DAOException {
        File metadata = new File(aFile.getParentFile(),
            LocalGFileManager.META_DATA + File.separator + aFile.getName());
        if ( metadata.exists() ) {
           if ( metadata.isDirectory() ) {
               throw new DAOException("{" +
                   metadata.getAbsolutePath() + "} should be a file");
           }
        } else {
            boolean success;
            try {
                success = metadata.createNewFile();
            } catch (IOException e) {
                throw new DAOException(e);
            }
            if ( !success ) {
                throw new DAOException("Can not create file {" +
                    metadata.getAbsolutePath() + "}");
            }
        }
        return metadata;
    }

    /**
     * Writes the content of a GFileTO.
     * 
     * @param aFileTO GFileTO defining the content to be written.
     * @param aTarget File where the content should be written.
     * @throws DAOException Indicates that the content can not be written.
     */
    private void writeFileContent(GFileTO aFileTO, File aTarget)
        throws DAOException {
        InputStream in = aFileTO.getStream();
        BufferedOutputStream out2 = null;
        BufferedInputStream in2 = null;
        try {
            aTarget.createNewFile();
            FileOutputStream out = new FileOutputStream(aTarget);
            out2 = new BufferedOutputStream(out);
            in2 = new BufferedInputStream(in);
            byte[] buffer = new byte[1024];
            int nbRead;
            while ( -1 < (nbRead = in2.read(buffer)) ) {
                out2.write(buffer, 0, nbRead);
            }
        } catch (IOException e) {
            throw new DAOException(e);
        } finally {
            try {
                if ( null != out2) { out2.close(); }
            } catch (IOException e) {}
        }
    }

    /**
     * Writes the properties of a GFileTO.
     * 
     * @param aFileTO GFileTO whose properties should be written.
     * @param aTarget Where the properties should be written.
     * @throws DAOException Indicates that the properties can not be written.
     */
    private void writeProperties(GFileTO aFileTO, File aTarget)
        throws DAOException {
        File metaDataFile = getMetaDataFile(aTarget);
        metaDataFile.delete();
        PrintWriter writer = null;
        try {
            FileOutputStream out = new FileOutputStream(metaDataFile);
            writer = new PrintWriter(out);
            Map properties = aFileTO.getProperties();
            for (Iterator iter = properties.entrySet().iterator();
                    iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
        } catch (IOException e) {
            throw new DAOException(e);
        } finally {
            if ( null != writer ) { writer.close(); }
        }
    }
    
}
