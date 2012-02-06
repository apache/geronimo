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

package org.apache.geronimo.tomcat;

import java.beans.PropertyChangeListener;

import javax.naming.directory.DirContext;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.util.LifecycleBase;
import org.apache.naming.resources.DirContextURLStreamHandler;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoWebAppLoader extends LifecycleBase implements Loader {

    private final Loader delegate;

    private final GeronimoStandardContext standardContext;

    public GeronimoWebAppLoader(GeronimoStandardContext standardContext, Loader delegate) {
        this.delegate = delegate;
        this.standardContext = standardContext;
    }

    @Override
    public void backgroundProcess() {
        delegate.backgroundProcess();
    }

    @Override
    public ClassLoader getClassLoader() {
        return standardContext.getParentClassLoader();
    }

    @Override
    public Container getContainer() {
        return delegate.getContainer();
    }

    @Override
    public void setContainer(Container container) {
        delegate.setContainer(container);
    }

    @Override
    public boolean getDelegate() {
        return delegate.getDelegate();
    }

    @Override
    public void setDelegate(boolean delegateBoolean) {
        delegate.setDelegate(delegateBoolean);
    }

    @Override
    public String getInfo() {
        return delegate.getInfo();
    }

    @Override
    public boolean getReloadable() {
        return false;
    }

    @Override
    public void setReloadable(boolean reloadable) {
        if (reloadable) {
            throw new UnsupportedOperationException("Reloadable context is not supported.");
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
    }

    @Override
    public void addRepository(String repository) {
        delegate.addRepository(repository);
    }

    @Override
    public String[] findRepositories() {
        return delegate.findRepositories();
    }

    @Override
    public boolean modified() {
        return delegate.modified();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
    }

    @Override
    protected void startInternal() throws LifecycleException {
        DirContext resources = delegate.getContainer().getResources();
        if (resources == null) {
            throw new IllegalStateException("JNDI environment was not set up correctly due to previous error");
        }
        DirContextURLStreamHandler.bind(standardContext.getParentClassLoader(), resources);
        setState(LifecycleState.STARTING);
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        setState(LifecycleState.STOPPING);
        DirContextURLStreamHandler.unbind(standardContext.getParentClassLoader());
    }

    @Override
    protected void destroyInternal() throws LifecycleException {
    }

    @Override
    protected void initInternal() throws LifecycleException {
    }
}
