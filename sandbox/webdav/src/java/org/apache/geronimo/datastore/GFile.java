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

package org.apache.geronimo.datastore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Abstract a file.
 * <BR>
 * A file is uniquely identified by its path, which is relative to the root
 * of its associated GFileManager. It is an InputStream and a map of properties.
 *
 * @version $Rev$ $Date$
 */
public interface GFile
{

    /**
     * To be used to create an empty GFile.
     */
    public static final InputStream NULL = new ByteArrayInputStream(new byte[0]);
    
    /**
     * To be used when the Content of a GFile is unset. It means that its
     * content should not be updated.
     */
    public static final InputStream UNSET = new ByteArrayInputStream(new byte[0]); 
    
    /**
     * Gets the path of this file.
     * 
     * @return Path relative to the GFileManager providing this file.
     */
    public String getPath();

    /**
     * Does this file exist?
     * 
     * @return true if exist.
     * @throws IOException If an I/O error has occurred.
     */
    public boolean exists() throws IOException;

    /**
     * Is this file a directory?
     * 
     * @return true if directory.
     * @throws IOException If an I/O error has occurred.
     */
    public boolean isDirectory() throws IOException;

    /**
     * Is this file a file/resource.
     * 
     * @return true if it is a resource.
     * @throws IOException If an I/O error has occurred.
     */
    public boolean isFile() throws IOException;

    /**
     * Gets all the files contained by this file, which should be a directory.
     * 
     * @return Children.
     * @throws IOException If an I/O error has occurred.
     */
    public String[] listFiles() throws IOException;

    /**
     * Lock this file. 
     * 
     * TODO pass a LockInfo.
     * 
     * @throws IOException If an I/O error has occurred.
     */
    public void lock() throws IOException;

    /**
     * Unlock this file.
     * 
     * @throws IOException If an I/O error has occurred.
     */
    public void unlock() throws IOException;

    /**
     * Gets the properties related to this file.
     * 
     * @return properties.
     * @throws IOException If an I/O error has occurred.
     */
    public Map getProperties() throws IOException;

    /**
     * Sets the new content of this file. A GFile does not provide a method to
     * retrieve the OutputStream of the underlying resource. This method is
     * the mean to define the new content of a file.
     * 
     * @param anIn New file content.
     */
    public void setContent(InputStream anIn);

    /**
     * Gets the new content of this file.
     * 
     * @return file content.
     */
    public InputStream getContent();

    /**
     * Gets the input bytes of the underlying file.
     * 
     * @return underlying file input stream
     * @throws IOException If an I/O error has occurred.
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Gets the properties having the specified names.
     *  
     * @param aCollOfNames Collection of property names.
     * @return Map of the properties defined by this file and requested.
     * @throws IOException If an I/O error has occurred.
     */
    public Map getPropertiesByName(Collection aCollOfNames) throws IOException;

    /**
     * Adds a property to this file.
     * 
     * @param aName Property name
     * @param aValue Value
     * @throws IOException If an I/O error has occurred.
     */
    public void addProperty(String aName, String aValue) throws IOException;
    
    /**
     * Removes a property.
     * 
     * @param aName Property name
     * @throws IOException If an I/O error has occurred.
     */
    public void removeProperty(String aName) throws IOException;
    
}
