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
package org.apache.geronimo.interop.rmi.iiop;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.util.ThreadContext;


public class IDLEntityHelper implements ObjectHelper {
    private static Class[] EMPTY_CLASS_ARRAY = {};

    private static Object[] EMPTY_OBJECT_ARRAY = {};

    private static HashMap _helperMap = new HashMap();

    private Method _id;

    private Method _type;

    private Method _read;

    private Method _write;

    static IDLEntityHelper getInstance(Class theClass) {
        IDLEntityHelper helper = (IDLEntityHelper) _helperMap.get(theClass);
        if (helper == null) {
            synchronized (_helperMap) {
                helper = (IDLEntityHelper) _helperMap.get(theClass);
                if (helper == null) {
                    helper = new IDLEntityHelper(theClass);
                    _helperMap.put(theClass, helper);
                }
            }
        }
        return helper;
    }

    private IDLEntityHelper(Class theClass) {
        try {
            Class helper = ThreadContext.loadClass(theClass.getName() + "Helper", theClass);
            _id = helper.getDeclaredMethod("id", EMPTY_CLASS_ARRAY);
            _type = helper.getDeclaredMethod("type", EMPTY_CLASS_ARRAY);
            _read = helper.getDeclaredMethod("read", new Class[]{org.omg.CORBA.portable.InputStream.class});
            _write = helper.getDeclaredMethod("write", new Class[]{org.omg.CORBA.portable.OutputStream.class, theClass});
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public String id() {
        try {
            return (String) _id.invoke(null, EMPTY_OBJECT_ARRAY);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public org.omg.CORBA.TypeCode type() {
        try {
            return (org.omg.CORBA.TypeCode) _type.invoke(null, EMPTY_OBJECT_ARRAY);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public Object read(ObjectInputStream input) {
        try {
            return _read.invoke(null, new Object[]{input._cdrInput});
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public void write(ObjectOutputStream output, Object value) {
        try {
            _write.invoke(null, new Object[]{output._cdrOutput, value});
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }
}
