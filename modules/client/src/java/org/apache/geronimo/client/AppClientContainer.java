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
package org.apache.geronimo.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.ReferenceCollection;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public final class AppClientContainer {
    private static final Class[] MAIN_ARGS = {String[].class};

    private final String mainClassName;
    private final ReferenceCollection plugins;
    private final ObjectName appClientModuleName;
    private final Method mainMethod;

    public AppClientContainer(String mainClassName,  ObjectName appClientModuleName, ReferenceCollection plugins ,ClassLoader classLoader) throws Exception {
        this.mainClassName = mainClassName;
        this.plugins = plugins;
        this.appClientModuleName = appClientModuleName;

        try {
            Class mainClass = classLoader.loadClass(mainClassName);
            mainMethod = mainClass.getMethod("main", MAIN_ARGS);
        } catch (ClassNotFoundException e) {
            throw new AppClientInitializationException("Unable to load Main-Class " + mainClassName, e);
        } catch (NoSuchMethodException e) {
            throw new AppClientInitializationException("Main-Class " + mainClassName + " does not have a main method", e);
        }
    }

    public ObjectName getAppClientModuleName() {
        return appClientModuleName;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public void main(String[] args) throws Exception {
        Throwable throwable = null;
        try {
            for (Iterator iterator = plugins.iterator(); iterator.hasNext();) {
                AppClientPlugin plugin = (AppClientPlugin) iterator.next();
                plugin.startClient(appClientModuleName);
            }

            mainMethod.invoke(null, args);

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throwable = cause;
            } else if (cause instanceof Error) {
                throwable =  cause;
            }
            throwable = e;
        } finally {
            for (Iterator iterator = plugins.iterator(); iterator.hasNext();) {
                AppClientPlugin plugin = (AppClientPlugin) iterator.next();
                plugin.stopClient(appClientModuleName);
            }

            if (throwable instanceof Exception) {
                throw (Exception) throwable;
            } else {
                throw (Error) throwable;
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AppClientContainer.class);

        infoFactory.addOperation("main", new Class[]{String[].class});
        infoFactory.addAttribute("mainClassName", String.class, true);
        infoFactory.addAttribute("appClientModuleObjectName", ObjectName.class, true);
        infoFactory.addReference("plugins", AppClientPlugin.class);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.setConstructor(new String[]{"mainClassName", "appClientModuleName", "plugins", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
