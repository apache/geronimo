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
    protected static SkelFactory _sf = new SkelFactory();

    protected SkelFactory() {
    }

    public static SkelFactory getInstance() {
        return _sf;
    }

    // private data

    private static HashMap _skelClassMap;

    // internal methods

    protected void init() {
        _skelClassMap = new HashMap();
    }

    protected Class loadStub(Class remoteInterface) {
        String className = remoteInterface.getName();
        String skelClassName = className + "_Skeleton";

        /*
        StubClass sc = new StubClass();
        if (sc.skelClass == null)
        {
            // Try generating skel class now.
            System.out.println( "TODO: StubFactory.loadStub(): className = " + className );
            StubCompiler skelCompiler = StubCompiler.getInstance(remoteInterface);
            sc.skelClass = skelCompiler.getStubClass();
        }

        if (sc.skelClass != null)
        {
            try
            {
                sc.getInstance = sc.skelClass.getMethod("$getInstance", ArrayUtil.EMPTY_CLASS_ARRAY);
            }
            catch (Exception ex)
            {
                throw new SystemException(ex);
            }
        }

        return sc;
        */

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

    // public methods

    public RemoteObject getSkel(Class remoteInterface) {
        System.out.println("SkelFactory.getSkel(): remoteInterface: " + remoteInterface);
        try {
            Class sc = (Class) _skelClassMap.get(remoteInterface);
            if (sc == null) {
                synchronized (_skelClassMap) {
                    sc = (Class) _skelClassMap.get(remoteInterface);
                    if (sc == null) {
                        sc = loadStub(remoteInterface);
                        _skelClassMap.put(remoteInterface, sc);
                    }
                }
            }
            //return (ObjectRef)sc.getInstance.invoke(sc.skelClass, ArrayUtil.EMPTY_OBJECT_ARRAY);
            return (RemoteObject) sc.newInstance();
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public Object getSkel(String remoteInterface) {
        return getSkel(ThreadContext.loadClass(remoteInterface));
    }
}
