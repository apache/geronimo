/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.core.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/22 02:08:41 $
 */
public class ClassUtil {
    private static final Map primitives = new HashMap();

    static {
        primitives.put("void", Void.TYPE);
        primitives.put("boolean", Boolean.TYPE);
        primitives.put("byte", Byte.TYPE);
        primitives.put("short", Short.TYPE);
        primitives.put("int", Integer.TYPE);
        primitives.put("long", Long.TYPE);
        primitives.put("float", Float.TYPE);
        primitives.put("double", Double.TYPE);
        primitives.put("char", Character.TYPE);
    }

    static HashMap vmPrimitives = new HashMap();
    static {
        primitives.put("B", byte.class);
        primitives.put("C", char.class);
        primitives.put("D", double.class);
        primitives.put("F", float.class);
        primitives.put("I", int.class);
        primitives.put("J", long.class);
        primitives.put("S", short.class);
        primitives.put("Z", boolean.class);
    }

    public static Class getClassForName(String name) throws ClassNotFoundException {
        return getClassForName(Thread.currentThread().getContextClassLoader(), name);
    }

    public static Class getClassForName(ClassLoader loader, String name) throws ClassNotFoundException {
        Class clazz = (Class) primitives.get(name);
        if (clazz == null) {
            return loader.loadClass(name);
        }
        return clazz;
    }
    

    /**
     * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
     */
    static public Class resolveObjectStreamClass(ClassLoader loader, String className) throws IOException, ClassNotFoundException {
        
        // Is it a normal class??
        if (!className.startsWith("[")) 
            return loader.loadClass(className);        
        
        // Is it an array class??
        Class type;             
        int arrayDimension = className.lastIndexOf('[')+1;             

        // Is the array type a primitive?
        if (className.charAt(arrayDimension) != 'L') {
            if (className.length() != arrayDimension + 1) 
                throw new ClassNotFoundException(className);                
            type = (Class) vmPrimitives.get(className.substring(arrayDimension,1));
        } else {           
            String cn = className.substring(arrayDimension + 1, className.length() - 1);
            type = loader.loadClass(cn);
        }
        
        // This kinda sucks.. there must be an easier way at getting
        // to Class of an array.. 
        int dim[] = new int[arrayDimension];
        for (int i = 0; i < arrayDimension; i++) 
            dim[i] = 0;        
        Object o = Array.newInstance(type, dim);
        return o.getClass();
    }
    
}
