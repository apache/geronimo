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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.datastore.GFile;
import org.apache.geronimo.datastore.GFileManager;
import org.apache.geronimo.datastore.GFileManagerException;
import org.apache.geronimo.gbean.GBeanContext;
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
 * @version $Revision: 1.2 $ $Date: 2004/03/03 13:10:07 $
 */
public abstract class AbstractGFileManager
    implements GFileManager
{

    /**
     * LockManager to be used to lock and unlock GFile.
     */
    private final LockManager lockManager;
    
    /**
     * State managers currently registered by this instance.
     */
    private final Set stateManagers;
    
    /**
     * Indicates if an interaction is started. We are between a start and a
     * end invocation. 
     */
    private boolean isStarted;
    
    /**
     * Used to perform isStarted operation. 
     */
    private final Object stateLock = new Object();

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
        stateManagers = new HashSet();
    }

    /**
     * Gets the name of this GFileManager.
     * 
     * @return Name.
     */
    public String getName() {
        return name;
    }
    
    public GFile factoryGFile(String aPath) throws GFileManagerException {
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
    
    public void persistNew(GFile aFile) {
        checkState(true);
        StateManager stateManager = ((GFileImpl)aFile).getStateManager(); 
        stateManager.setIsNew(true);
        stateManagers.add(stateManager);
    }
    
    public void persistUpdate(GFile aFile) {
        checkState(true);
        StateManager stateManager = ((GFileImpl)aFile).getStateManager(); 
        stateManager.setIsDirty(true);
        stateManagers.add(stateManager);
    }

    public void persistDelete(GFile aFile) {
        checkState(true);
        StateManager stateManager = ((GFileImpl)aFile).getStateManager(); 
        stateManager.setIsDelete(true);
        stateManagers.add(stateManager);
    }

    public void start() {
        switchState(true);
    }

    public void end() throws GFileManagerException {
        checkState(true);
        try {
            doInternalEnd();
        } finally {
            synchronized(stateManagers) {
                stateManagers.clear();
            }
            switchState(false);
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
     * @throws GFileManagerException Indicates that a StateManager is not able
     * to flush its state.
     */
    private void doInternalEnd() throws GFileManagerException {
        List flushedManagers = new ArrayList();
        try {
            // Prepare all the state to be flushed.
            for (Iterator iter = stateManagers.iterator(); iter.hasNext();) {
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
        for (Iterator iter = stateManagers.iterator(); iter.hasNext();) {
            StateManager stateManager = (StateManager) iter.next();
            flushedManagers.add(stateManager);
            stateManager.flush();
        }
    }

    /**
     * Registers a new StateManagers for the current interaction.
     * 
     * @param aStateManager StateManager to be registered.
     */
    protected void registerStateManager(StateManager aStateManager) {
        synchronized (stateManagers) {
            stateManagers.add(aStateManager);
        }
    }

    /**
     * Checks the state (inside a start/end call or not) of this instance.
     * 
     * @param anExpectedState Expected state.
     * @throws IllegalStateException Indicates that the current state is not
     * the expected one.
     */
    private final void checkState(boolean anExpectedState) {
        synchronized (stateLock) {
            if ( isStarted == anExpectedState ) {
                return;
            }
            throw new IllegalStateException(
                    anExpectedState?"Already started":"Not yet started");
        }
    }
    
    /**
     * Switches the state of this instance.
     * 
     * @param anExpectedState New state.
     * @throws IllegalStateException Indicates that the current state is not
     * compliant with the new state.
     */
    private final void switchState(boolean aTargetState) {
        synchronized (stateLock) {
            if ( isStarted == aTargetState ) {
                throw new IllegalStateException(
                     isStarted?"Already started":"Not yet started");
            }
            isStarted = aTargetState;
        }
    }

    public void setGBeanContext(GBeanContext context) {}

    public void doStart() throws WaitingException, Exception{}

    public void doStop() throws WaitingException, Exception {}

    public void doFail() {};
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(AbstractGFileManager.class);
        factory.addAttribute("Name", true);
        factory.addAttribute("LockManager", true);
        factory.addOperation("start");
        factory.addOperation("factoryGFile", new Class[]{String.class});
        factory.addOperation("persistNew", new Class[]{GFile.class});
        factory.addOperation("persistUpdate", new Class[]{GFile.class});
        factory.addOperation("persistDelete", new Class[]{GFile.class});
        factory.addOperation("end");
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
