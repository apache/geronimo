/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.security.remoting.jmx;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.geronimo.kernel.ClassLoading;

/**
 * @version $Rev$ $Date$
 */
public class MarshalledMethod implements Serializable {

    String declaringClass;
    String signature;

    /**
     * 
     */
    public MarshalledMethod() {
    }

    /**
     * @param method
     */
    public MarshalledMethod(Method method) {
        declaringClass = method.getDeclaringClass().getName();
        signature = getSignature( method );
    }

    /**
     * @param method
     * @return
     */
    static public String getSignature(Method method) {
        StringBuffer sb = new StringBuffer();
        sb.append(method.getName());
        sb.append(' ');
        Class[] args = method.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            sb.append(' ');
            sb.append( ClassLoading.getClassName(args[i]) );
        }
        return sb.toString();
    }

    /**
     * @return
     */
    public Method getMethod() throws ClassNotFoundException {
        Class c = Thread.currentThread().getContextClassLoader().loadClass(declaringClass);
        Map sigs = getCachedSignatureMap(c);        
        return (Method) sigs.get(signature);
    }

    /**
     * TODO: try to cache the results.
     * @param clazz
     * @return
     */
    static private Map getSignatureMapFor(Class clazz) {
        Map rc = new HashMap();
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            rc.put(getSignature(method), method);
        }
        return rc;
    }

    private static Map SignatureMapCache= Collections.synchronizedMap(new WeakHashMap());
    static class CacheValue {
        Class clazz;
        Map sigs;
    }


    public static Map getCachedSignatureMap(Class clazz) {
        String cacheKey = clazz.getName();
        CacheValue rc = (CacheValue) SignatureMapCache.get(cacheKey);
        if (rc == null) {
            rc = new CacheValue();
            rc.clazz = clazz;
            rc.sigs = getSignatureMapFor(clazz);
            SignatureMapCache.put(cacheKey, rc);
            return rc.sigs;
        } else if ( rc.clazz.equals( clazz ) ) {
            return rc.sigs;
        } else {
            // the previously cache class name might not be the same class
            // due to classloader issues.
            return getSignatureMapFor(clazz);
        }
        
    }

    

}
