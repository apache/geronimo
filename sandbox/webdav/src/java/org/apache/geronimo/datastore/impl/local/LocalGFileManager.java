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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;

import org.apache.geronimo.datastore.impl.AbstractGFileManager;
import org.apache.geronimo.datastore.impl.DAOException;
import org.apache.geronimo.datastore.impl.GFileDAO;
import org.apache.geronimo.datastore.impl.LockManager;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 * GFileManager using a LocalGFileDAO to interact with the data store.
 *
 * @version $Revision: 1.3 $ $Date: 2004/05/11 12:25:00 $
 */
public class LocalGFileManager
    extends AbstractGFileManager
{

    public static final String IDENTIFIER = "LocalGFileManager";
    public static final String META_DATA = "METADATA";
    public static final String ROOT = "Root";

    /**
     * Root. Paths are resolved based on this root.
     */
    private final File root;
    
    /**
     * Checks compliance of a Root file with this implementation.
     * 
     * @param aReader To read the root file.
     * @return true if the root is supported. 
     */
    public static boolean isRootSupported(PushbackReader aReader)
        throws IOException {
        char[] chars = new char[IDENTIFIER.length()];
        aReader.read(chars);
        boolean supported = true;
        for (int i = 0; i < chars.length; i++) {
            if ( chars[i] != IDENTIFIER.charAt(i) ) {
                supported = false;
                break;
            }
        }
        aReader.unread(chars);
        return supported;
    }
    
    /**
     * Creates a Local GFileManager having the specified name and resolving
     * all the GFile paths based on aRoot.
     * 
     * @param aName Name of this manager.
     * @param aRoot Path are resolved based on this root.
     * @param aLockManager LockManager to lock/unlock GFiles.
     * @throws IOException Indicates that the local filesystem can not
     * be initialized.
     */
    public LocalGFileManager(String aName, File aRoot, LockManager aLockManager)
        throws IOException {
        super(aName, aLockManager);
        if ( !aRoot.exists() ) {
            throw new IllegalArgumentException("Root directory {" + 
                aRoot.getAbsolutePath() + "} does not exist.");
        }
        root = aRoot;
        initialize();
    }

    public void writeRoot(Writer aWriter) throws IOException {
        aWriter.write(IDENTIFIER + ":");
    }

    /**
     * Gets the root of this local filesystem manager.
     * 
     * @return Root. Path are resolved based on this root.
     */
    public File getRoot() {
        return root;
    }
    
    protected GFileDAO newGerFileDAO() throws DAOException {
        return new LocalGFileDAO(root);
    }

    /**
     * Initializes the META_DATA repository where the properties will be stored.
     *   
     * @throws IOException Indicates that the repository can not be set-up.
     */
    private void initialize() throws IOException {
        File metadata = new File(root, META_DATA);
        if ( metadata.exists() ) {
            if ( !metadata.isDirectory() ) {
                throw new IllegalArgumentException(
                        "Can not initialize as the file {" +
                        metadata.getAbsolutePath() + "} exist.");
            }
        } else {
            boolean success = metadata.mkdir();
            if ( !success ) {
                throw new IllegalArgumentException(
                    "Can not create directory {" +
                    metadata.getAbsolutePath() + "}.");
            }
        }
        File rootFile = new File(metadata, ROOT);
        if ( rootFile.exists() ) {
            PushbackReader reader =
                new PushbackReader(new FileReader(rootFile), 100);
            if ( !isRootSupported(reader) ) {
                throw new IllegalArgumentException(
                    "Can not mount GFileManager as the existing {" +
                    root.getAbsolutePath() +
                    "} file defined an unsupported root.");
            }
            reader.close();
            return;
        }
        boolean success = rootFile.createNewFile();
        if ( !success ) {
            throw new IllegalArgumentException(
                "Can not create file {" + root.getAbsolutePath() + "}.");
        }
        FileWriter writer = new FileWriter(rootFile);
        writeRoot(writer);
        writer.close();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory =
            new GBeanInfoFactory(LocalGFileManager.class.getName(), AbstractGFileManager.GBEAN_INFO);
        factory.setConstructor(
            new String[] {"Name", "Root", "LockManager"},
            new Class[] {String.class, File.class, LockManager.class});
        factory.addAttribute("Root", true);
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
