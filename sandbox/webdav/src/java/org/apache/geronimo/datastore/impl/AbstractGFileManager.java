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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.datastore.GFile;
import org.apache.geronimo.datastore.GFileManager;
import org.apache.geronimo.datastore.GFileManagerException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;


/**
 * Base implementation of a GFileManager. The intent of this base class is to
 * allow for the plugging of various DAO strategies.
 * <BR>
 * Indeed, a full GFileManager just have to provide its own GFileDAO
 * implementation in order to support all the GFileManager contract.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractGFileManager
    implements GFileManager
{

    /**
     * LockManager to be used to lock and unlock GFile.
     */
    private final LockManager lockManager;
    
    /**
     * Used to create interaction identifiers.
     */
    private volatile int seqInterac = 0;
    
    /**
     * Interaction identifier to Set of StateManagers. 
     */
    private final Map interToStateManagers;
    
    /**
     * Name of this GFileManager.
     */
    protected final String name;

    /**
     * Creates a GFileManager having the provided name.
     * 
     * @param aName Name of the GFileManager.
     * @param aLockManager LockManager to be used to lock/unlock GFiles.
     */
    public AbstractGFileManager(String aName, LockManager aLockManager) {
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        } else if ( null == aLockManager ) {
            throw new IllegalArgumentException("LockManager is required.");
        }
        name = aName;
        lockManager = aLockManager;
        interToStateManagers = new HashMap();
    }

    /**
     * Gets the name of this GFileManager.
     * 
     * @return Name.
     */
    public String getName() {
        return name;
    }
    
    public GFile factoryGFile(Object anOpaque, String aPath)
        throws GFileManagerException {
        retrieveStateManagers(anOpaque);
        GFileImpl gFile;
        try {
            // TODO Refactor; this is too intertwined.
            GFileDAO fileDAO = newGerFileDAO();
            GFileDelegate delegate = new GFileDelegateImpl(fileDAO, lockManager);
            GFileStateManager stateManager =
                new GFileStateManager(fileDAO, new DirtyMarkerImpl());
            stateManager.setGFileDelegate(delegate);
            gFile = new GFileImpl(aPath, stateManager);
        } catch (DAOException e) {
            throw new GFileManagerException(e);
        }
        return gFile;
    }
    
    public void persistNew(Object anOpaque, GFile aFile) {
        Set stateManagers = retrieveStateManagers(anOpaque);
        StateManager stateManager = ((GFileImpl)aFile).getStateManager(); 
        stateManager.setIsNew(true);
        stateManagers.add(stateManager);
    }
    
    public void persistUpdate(Object anOpaque, GFile aFile) {
        Set stateManagers = retrieveStateManagers(anOpaque);
        StateManager stateManager = ((GFileImpl)aFile).getStateManager(); 
        stateManager.setIsDirty(true);
        stateManagers.add(stateManager);
    }

    public void persistDelete(Object anOpaque, GFile aFile) {
        Set stateManagers = retrieveStateManagers(anOpaque);
        StateManager stateManager = ((GFileImpl)aFile).getStateManager(); 
        stateManager.setIsDelete(true);
        stateManagers.add(stateManager);
    }

    public Object startInteraction() {
        Integer id = new Integer(seqInterac++);
        synchronized (interToStateManagers) {
            interToStateManagers.put(id, new HashSet());
        }
        return id;
    }

    public void endInteraction(Object anOpaque) throws GFileManagerException {
        Set stateManagers = retrieveStateManagers(anOpaque);
        try {
            doInteractionEnd(stateManagers);
        } finally {
            synchronized(interToStateManagers) {
                interToStateManagers.remove(anOpaque);
            }
        }
    }

    /**
     * This method is intended to be implemented by GFileManagers providing
     * various DAO implementations.
     *  
     * @return GFileDAO DAO to be used to interact withe the data store.
     * @throws DAOException Indicates that the DAO can not be created.
     */
    protected abstract GFileDAO newGerFileDAO() throws DAOException;
    
    /**
     * Actually ends an interaction. One iterates over the StateManagers and
     * one lets them prepare/flush/unflush their states.
     * <BR>
     * If a StateManager is not able to prepare its state, all the previously
     * StateManagers are unflushed.
     * 
     * @param aStateManagers StateManagers related to the interaction 
     * being completed.
     * @throws GFileManagerException Indicates that a StateManager is not able
     * to flush its state.
     */
    private void doInteractionEnd(Set aStateManagers)
        throws GFileManagerException {
        List flushedManagers = new ArrayList();
        try {
            // Prepare all the state to be flushed.
            for (Iterator iter = aStateManagers.iterator(); iter.hasNext();) {
                StateManager stateManager = (StateManager) iter.next();
                flushedManagers.add(stateManager);
                stateManager.prepare();
            }
        } catch (IOException e) {
            // There was a problem with a StateManager. One unflushes all the
            // prepared StateManagers.
            for (Iterator iter = flushedManagers.iterator(); iter.hasNext();) {
                StateManager stateManager = (StateManager) iter.next();
                stateManager.unflush();
            }
            throw new GFileManagerException("Can not flush GFile", e);
        }
        // One can flush all the states.
        for (Iterator iter = aStateManagers.iterator(); iter.hasNext();) {
            StateManager stateManager = (StateManager) iter.next();
            flushedManagers.add(stateManager);
            stateManager.flush();
        }
    }

    /**
     * Retrieves the StateManagers related to the interaction identified by
     * anOpaque.
     * 
     * @param anOpaque An opaque object identifying the interaction whose
     * StateManagers need to be retrieved.
     * @return Set of StateManagers related to the interaction identified by
     * anOpaque.
     * @throws IllegalStateException Indicates that the provided identifier
     * does not define an interaction.
     */
    private final Set retrieveStateManagers(Object anOpaque) {
        Set stateManagers;
        synchronized (interToStateManagers) {
            stateManagers = (Set) interToStateManagers.get(anOpaque);
            if ( null == stateManagers ) {
                throw new IllegalStateException(
                    "Unknow interaction identifier.");
            }
        }
        return stateManagers;
    }

    public void doStart() throws WaitingException, Exception{}

    public void doStop() throws WaitingException, Exception {}

    public void doFail() {}

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(AbstractGFileManager.class);
        factory.addAttribute("name", String.class, true);
        factory.addAttribute("lockManager", LockManager.class, true);
        factory.addOperation("startInteraction");
        factory.addOperation("factoryGFile", new Class[]{Object.class, String.class});
        factory.addOperation("persistNew", new Class[]{Object.class, GFile.class});
        factory.addOperation("persistUpdate", new Class[]{Object.class, GFile.class});
        factory.addOperation("persistDelete", new Class[]{Object.class, GFile.class});
        factory.addOperation("endInteraction", new Class[]{Object.class});
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
