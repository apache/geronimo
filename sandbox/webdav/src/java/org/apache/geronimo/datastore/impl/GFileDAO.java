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

package org.apache.geronimo.datastore.impl;

import java.io.InputStream;

/**
 * Data Access Object contracts for a GFile. It is used to contact a data store
 * and retrieve the state of a file; perform queries et cetera.
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/11 12:24:59 $
 */
public interface GFileDAO
{

    /**
     * Creates a new Gfile.
     * 
     * @param aFileTO File to be created.
     * @throws DAOException If a DAO error has occured.
     */
    public void create(GFileTO aFileTO) throws DAOException;
    
    /**
     * Reads a Gfile from the data store.
     * 
     * @param aPath Path of the file to be read.
     * @return GFileTO holding the state of the read GFile having the provided
     * path.
     * @throws DAOException If a DAO error has occured.
     */
    public GFileTO read(String aPath) throws DAOException;

    /**
     * Updates a Gfile.
     *
     * @param aFileTO New state. 
     * @throws DAOException If a DAO error has occured.
     */
    public void update(GFileTO aFileTO) throws DAOException;
    
    /**
     * Deletes a GFile.
     * 
     * @param aPath Path of the file to be deleted.
     * @throws DAOException If a DAO error has occured.
     */
    public void delete(String aPath) throws DAOException;

    /**
     * Gets the children of the directory identified by the provided path.
     * 
     * @param aPath Directory path.
     * @return Children.
     * @throws DAOException If a DAO error has occured.
     */
    public String[] listFiles(String aPath) throws DAOException;

    /**
     * Flushed the updates performed by this instance to the underlying
     * datastore.
     */
    public void flush();

    /**
     * Unflushes the updates performed since the last flush operation.
     */
    public void unflush();
    
    /**
     * Gets an InputStream of the content of a GFile.
     * 
     * @param aPath Path.
     * @return Content of the GFile.
     * @throws DAOException If a DAO error has occured.
     */
    public InputStream getInputStream(String aPath) throws DAOException;
    
}