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

import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * GFile manager. It allows to retrieve files from the data store and perform
 * CRUD operations against them in the context of a single interaction.
 * <BR>
 * An interaction is delimited by a start and an end invocations. 
 *
 * @version $Rev$ $Date$
 */
public interface GFileManager extends GBeanLifecycle
{

    /**
     * Gets the name of this manager.
     * 
     * @return Manager name.
     */
    public String getName();
    
    /**
     * Builds a GFile.
     * 
     * @param anOpaque An opaque object identifying the interaction.
     * @param aPath Path of the GFile.
     * @return A GFile managed by this GFileManager.
     * @throws GFileManagerException Indicates that the resource can not be
     * created.
     */
    public GFile factoryGFile(Object anOpaque, String aPath)
        throws GFileManagerException;
        
    /**
     * Flags the provided GFile as new.
     * <BR>
     * This operation must be performed in the scope of a start/stop sequence.
     * 
     * @param anOpaque An opaque object identifying the interaction.
     * @param aFile GFile to be created.
     */
    public void persistNew(Object anOpaque, GFile aFile);
    
    /**
     * Flags the provided GFile as updated.
     * <BR>
     * This operation must be performed in the scope of a start/stop sequence.
     * 
     * @param anOpaque An opaque object identifying the interaction.
     * @param aFile GFile to be updated.
     */
    public void persistUpdate(Object anOpaque, GFile aFile);
    
    /**
     * Flags the provided GFile as deleted.
     * <BR>
     * This operation must be performed in the scope of a start/stop sequence.
     * 
     * @param anOpaque An opaque object identifying the interaction.
     * @param aFile GFile to be deleted.
     */
    public void persistDelete(Object anOpaque, GFile aFile);
    
    /**
     * Starts an interaction with this manager. persistXXX operations must
     * be performed in the scope of a start/end sequence.
     * 
     * @return An opaque object identifying the interaction. This object must
     * be provided to the persistXXX operations in order to relate an operation
     * to an interaction.
     */
    public Object startInteraction();
    
    /**
     * Ends the interaction and "commits" all the updates performed between
     * the last startInteraction call and now to the underlying data store. 
     * 
     * @param anOpaque An opaque object identifying the interaction.
     * 
     * @throws GFileManagerException Indicates a problem when "commiting" the
     * operations.
     */
    public void endInteraction(Object anOpaque) throws GFileManagerException;

}