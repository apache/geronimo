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
import org.apache.geronimo.interop.rmi.iiop.ObjectRef;
import org.apache.geronimo.interop.util.JavaClass;
import org.apache.geronimo.interop.util.ThreadContext;

public class StubFactory {
    protected static StubFactory sf;

    protected StubFactory() {
    }

    public static StubFactory getInstance() {
        if (sf == null) {
            synchronized (StubFactory.class) {
                if (sf == null) {
                    sf = new StubFactory();
                    sf.init();
                }
            }
        }

        return sf;
    }

    private static HashMap stubClassMap;

    protected void init() {
        stubClassMap = new HashMap();
    }

    protected Class loadStub(Class remoteInterface) {
        System.out.println("StubFactory.loadStub(): remoteInterface: " + remoteInterface);
        String className = remoteInterface.getName();
        String stubClassName = JavaClass.addPackageSuffix(className, "iiop_stubs");
        System.out.println("StubFactory.loadStub(): stubClassName: " + stubClassName);

        Class sc = null;
        //
        // Try to load the stub.  No need to generate if the stub is already present.
        //
        try {
            sc = Class.forName(stubClassName + "_Stub");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        if (sc == null) {
            //
            // Try generating stub class now.
            //

            //StubCompiler stubCompiler = new StubCompiler(remoteInterface);
            //System.out.println("StubFactory.loadStub(): stubCompiler: " + stubCompiler);
            //sc = stubCompiler.getStubClass();
            System.out.println("StubFactory.loadStub(): sc: " + sc);
        }

        /*
        if (sc.stubClass != null)
        {
            try
            {
                sc.getInstance = sc.stubClass.getMethod("$getInstance", ArrayUtil.EMPTY_CLASS_ARRAY);
            }
            catch (Exception ex)
            {
                throw new SystemException(ex);
            }
        }
        */

        return sc;
    }

    public ObjectRef getStub(Class remoteInterface) {
        System.out.println("StubFactory.getStub(): remoteInterface: " + remoteInterface);
        try {
            Class sc = (Class) stubClassMap.get(remoteInterface);
            System.out.println("StubFactory.getStub(): sc: " + sc);
            if (sc == null) {
                synchronized (stubClassMap) {
                    sc = (Class) stubClassMap.get(remoteInterface);
                    if (sc == null) {
                        sc = loadStub(remoteInterface);
                        System.out.println("StubFactory.getStub(): sc: " + sc);
                        stubClassMap.put(remoteInterface, sc);
                    }
                }
            }

            if (sc == null) {
                throw new SystemException("Error: Unable to load stub for remote interface: " + remoteInterface);
            }

            java.lang.Object sobj = sc.newInstance();

            if (!(sobj instanceof ObjectRef)) {
                throw new SystemException("Error: Stub for remote interface: '" + remoteInterface + "' is not a valid ObjectRef.");
            }

            return (ObjectRef) sobj;
            //return (ObjectRef)sc.getInstance.invoke(sc.stubClass, ArrayUtil.EMPTY_OBJECT_ARRAY);
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public Object getStub(String remoteInterface) {
        return getStub(ThreadContext.loadClass(remoteInterface));
    }
}
