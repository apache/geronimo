/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop.compiler;

import java.util.HashMap;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.rmi.iiop.RemoteObject;
import org.apache.geronimo.interop.util.ThreadContext;

public class SkelFactory {
    private static SkelFactory sf = new SkelFactory();

    protected SkelFactory() {
    }

    public static SkelFactory getInstance() {
        return sf;
    }

    private static HashMap skelClassMap;

    protected void init() {
        skelClassMap = new HashMap();
    }

    /*
    protected Class loadStub(Class remoteInterface) {
        String className = remoteInterface.getName();
        String skelClassName = className + "_Skeleton";

        Class sc = null;
        try {
            sc = Class.forName(skelClassName);
            SkelCompiler skelCompiler = new SkelCompiler(sc);
            sc = skelCompiler.getSkelClass();
        } catch (Exception ex) {
            throw new SystemException(ex);
        }

        return sc;
    }
    */

    /*
    public RemoteObject getSkel(Class remoteInterface) {
        System.out.println("SkelFactory.getSkel(): remoteInterface: " + remoteInterface);
        try {
            Class sc = (Class) skelClassMap.get(remoteInterface);
            if (sc == null) {
                synchronized (skelClassMap) {
                    sc = (Class) skelClassMap.get(remoteInterface);
                    if (sc == null) {
                        sc = loadStub(remoteInterface);
                        skelClassMap.put(remoteInterface, sc);
                    }
                }
            }
            //return (ObjectRef)sc.getInstance.invoke(sc.skelClass, ArrayUtil.EMPTY_OBJECT_ARRAY);
            return (RemoteObject) sc.newInstance();
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }
    */

    /*
    public Object getSkel(String remoteInterface) {
        return getSkel(ThreadContext.loadClass(remoteInterface));
    }
    */
}
