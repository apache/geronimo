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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.util.ArrayUtil;
import org.apache.geronimo.interop.util.ExceptionUtil;
import org.apache.geronimo.interop.util.JavaClass;
import org.apache.geronimo.interop.util.JavaType;
import org.apache.geronimo.interop.util.SystemUtil;
import org.apache.geronimo.interop.util.ThreadContext;
import org.omg.CORBA.TCKind;

/**
 ** A wrapper over java.lang.Class to help improve performance of using
 ** the Java reflection API for valuetype marshalling. We keep as much
 ** derived information as possible for optimal performance.
 **/
public class ValueType
{
    public static ValueType getInstance(Class forClass)
    {
        ValueType vt = (ValueType)_valueTypeMap.get(forClass);
        if (vt == null)
        {
            synchronized (_valueTypeMap)
            {
                vt = (ValueType)_valueTypeMap.get(forClass);
                if (vt == null)
                {
                    vt = new ValueType();
                    _valueTypeMap.put(forClass, vt);
                    vt.init(forClass);
                }
            }
        }
        return vt;
    }

    public static ValueType getInstanceByID(String id)
    {
        // TODO: handle multiple class loaders???
        ValueType vt = (ValueType)_idTypeMap.get(id);
        if (vt == null)
        {
            synchronized (_idTypeMap)
            {
                vt = (ValueType)_idTypeMap.get(id);
                if (vt == null)
                {
                    Class theClass = getClass(id);
                    vt = getInstance(theClass);
                    _idTypeMap.put(id, vt);
                }
            }
        }
        return vt;
    }

    // -----------------------------------------------------------------------
    // public data
    // -----------------------------------------------------------------------

    public Class _class;

    public org.apache.geronimo.interop.rmi.iiop.ObjectHelper helper;

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private static HashMap _valueTypeMap = new HashMap();

    private static HashMap _initMap = new HashMap();

    private static HashMap _idTypeMap = new HashMap();

    private static final boolean JDK14 = SystemUtil.isJDK14();

    private static Method _allocateNewObject;

    private static Object[] _allocateNewObjectArgs;

    private static Method _newInstance;

    private ObjectStreamClass _objectStreamClass;

    private Method _readExternal;
    private Method _readObject;
    private Method _readResolve;
    private Method _writeExternal;
    private Method _writeObject;
    private Method _writeReplace;

    // -----------------------------------------------------------------------
    // package-private data
    // -----------------------------------------------------------------------

    static final int NULL_VALUE_TAG                    = 0;
    static final int NO_TYPE_VALUE_TAG                 = 0x7fffff00;
    static final int SINGLE_TYPE_VALUE_TAG             = 0x7fffff02;
    static final int TRUNCATABLE_NO_TYPE_VALUE_TAG     = 0x7fffff08;
    static final int TRUNCATABLE_SINGLE_TYPE_VALUE_TAG = 0x7fffff0a;
    static final int TYPE_LIST_VALUE_TAG               = 0x7fffff06;
    static final int INDIRECTION_TAG                   = 0xffffffff;

    static final int CASE_ARRAY = 1;
    static final int CASE_CLASS = 2;
    static final int CASE_IDL_ENTITY = 3;
    static final int CASE_IDL_OBJECT = 4;
    static final int CASE_STRING = 5;

    static final ValueType OBJECT_VALUE_TYPE = getInstance(Object.class);

    static final ValueType STRING_VALUE_TYPE = getInstance(String.class);

    static final org.omg.CORBA.TypeCode TC_NULL = new TypeCode(TCKind.tk_null);

    static TypeCode TC_ABSTRACT_BASE;

    String id; // CORBA Repository ID

    TypeCode tc;

    ValueType parent;

    ValueTypeField[] fields; // just the serializable fields.

    ValueType element; // if array, this is ValueType for elements.

    boolean hasParentState;
    boolean hasReadObject;
    boolean hasReadOrWriteObject;
    boolean hasWriteObject;
    boolean hasReadResolve;
    boolean hasWriteReplace;

    boolean isAbstractInterface;
    boolean isAny;
    boolean isAnyOrObjectRefOrAbstractInterface;
    boolean isArray;
    boolean isExternalizable;
    boolean isIDLEntity;
    boolean isObjectRef;

    int primitiveArray;

    int readWriteCase;

    boolean requiresCustomSerialization;

    boolean skipCustomFlags; // TODO: init this

    // -----------------------------------------------------------------------
    // static initializer
    // -----------------------------------------------------------------------

    static
    {
        TC_ABSTRACT_BASE = new TypeCode(TCKind.tk_abstract_interface);
        TC_ABSTRACT_BASE.id("IDL:omg.org/CORBA/AbstractBase:1.0");
        TC_ABSTRACT_BASE.name("");

        try
        {
            if (JDK14)
            {
                _newInstance = java.io.ObjectStreamClass.class.getDeclaredMethod("newInstance", new Class[] {});
                _newInstance.setAccessible(true);
            }
            else
            {
                _allocateNewObject = java.io.ObjectInputStream.class.getDeclaredMethod("allocateNewObject", new Class[] { Class.class, Class.class });
                _allocateNewObject.setAccessible(true);
            }
        }
        catch (Exception ex)
        {
            throw ExceptionUtil.getRuntimeException(ex);
        }
    }

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public Object newInstance()
    {
        try
        {
            if (JDK14)
            {
                if (_class == Object.class)
                {
                    return new Object();
                }
                else
                {
                    return _newInstance.invoke(_objectStreamClass, ArrayUtil.EMPTY_OBJECT_ARRAY);
                }
            }
            else
            {
                return _allocateNewObject.invoke(null, _allocateNewObjectArgs);
            }
        }
        catch (Exception ex)
        {
            throw ExceptionUtil.getRuntimeException(ex);
        }
    }

    public String toString()
    {
        return "ValueType:" + JavaType.getName(_class);
    }

    public void readObject(Object _this, org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input)
    {
        try
        {
            _readObject.invoke(_this, input.thisAsObjectArray);
        }
        catch (Exception ex)
        {
            throw ExceptionUtil.getRuntimeException(ex);
        }
    }

    public void writeObject(Object _this, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output)
    {
        try
        {
            _writeObject.invoke(_this, output.thisAsObjectArray);
        }
        catch (Exception ex)
        {
            throw ExceptionUtil.getRuntimeException(ex);
        }
    }

    public Object readResolve(Object _this)
    {
        try
        {
            return _readResolve.invoke(_this, ArrayUtil.EMPTY_OBJECT_ARRAY);
        }
        catch (Exception ex)
        {
            throw ExceptionUtil.getRuntimeException(ex);
        }
    }

    public Object writeReplace(Object _this)
    {
        try
        {
            return _writeReplace.invoke(_this, ArrayUtil.EMPTY_OBJECT_ARRAY);
        }
        catch (Exception ex)
        {
            throw ExceptionUtil.getRuntimeException(ex);
        }
    }

    public void readExternal(Object _this, org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input)
    {
        try
        {
            _readExternal.invoke(_this, input.thisAsObjectArray);
        }
        catch (Exception ex)
        {
            throw ExceptionUtil.getRuntimeException(ex);
        }
    }

    public void writeExternal(Object _this, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output)
    {
        try
        {
            _writeExternal.invoke(_this, output.thisAsObjectArray);
        }
        catch (Exception ex)
        {
            throw ExceptionUtil.getRuntimeException(ex);
        }
    }

    // -----------------------------------------------------------------------
    // protected methods
    // -----------------------------------------------------------------------

    protected void init(Class theClass)
    {
        boolean recursive = false;
        if (_initMap.get(theClass) != null)
        {
//              recursive = true;
            return; // Already initializing (recursive 'init' call).
        }
        _initMap.put(theClass, Boolean.TRUE);
        try
        {
            _class = theClass;
            _objectStreamClass = ObjectStreamClass.lookup(_class);
            if (org.omg.CORBA.Object.class.isAssignableFrom(theClass)
                || javax.ejb.EJBHome.class.isAssignableFrom(theClass)
                || javax.ejb.EJBObject.class.isAssignableFrom(theClass)
                || java.rmi.Remote.class.isAssignableFrom(theClass))
            {
                helper = ObjectRefHelper.getInstance(theClass);
                isObjectRef = true;
                readWriteCase = CASE_IDL_OBJECT;
            }
            else if (org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(theClass))
            {
                helper = IDLEntityHelper.getInstance(theClass);
                isIDLEntity = true;
                readWriteCase = CASE_IDL_ENTITY;
            }
            else if (theClass == String.class)
            {
                helper = StringHelper.SINGLETON;
                readWriteCase = CASE_STRING;
            }
            else if (theClass.isArray())
            {
                Class elementClass = theClass.getComponentType();
                element = getInstance(elementClass);
                isArray = true;
                if (elementClass.isPrimitive())
                {
                    primitiveArray = PrimitiveType.get(elementClass);
                    helper = PrimitiveType.getArrayHelper(elementClass);
                }
                else
                {
                    helper = new ArrayHelper(elementClass);
                }
                readWriteCase = CASE_ARRAY;
            }
            else if (theClass == Class.class)
            {
                readWriteCase = CASE_CLASS;
            }
            if (_allocateNewObject != null)
            {
                Class bc = _class;
                while (Serializable.class.isAssignableFrom(bc) && (bc.getSuperclass() != null))
                {
                    bc = bc.getSuperclass();
                }
                _allocateNewObjectArgs = new Object[] { _class, bc };
            }

            isAny = _class == java.lang.Object.class
                    || _class == java.io.Externalizable.class
                    || _class == java.io.Serializable.class;

            isExternalizable = java.io.Externalizable.class.isAssignableFrom(_class);
            if (isExternalizable)
            {
                _readExternal = _class.getDeclaredMethod("readExternal", new Class[] { ObjectInput.class } );
                _writeExternal = _class.getDeclaredMethod("writeExternal", new Class[] { ObjectOutput.class } );
            }

            // SG: Hopefully we got all the info that is needed
            if(recursive)
            {
                return;
            }


            java.lang.Class tmpClass = _class;
            ArrayList fieldList = new ArrayList();
            Field[] javaFields = tmpClass.getDeclaredFields();

            // TODO: suppress sort for IDL-generated valuetypes
            Arrays.sort(javaFields, FieldComparator.SINGLETON);

            // Create vector of non-static, non-transient fields.
            // Ensure that all fields are readable/writable using reflection.
            int nf = javaFields.length;
            for (int f = 0; f < nf; f++)
            {
                Field javaField = javaFields[f];
                int modifiers = javaField.getModifiers();
                if ((modifiers & (Modifier.STATIC | Modifier.TRANSIENT)) != 0)
                {
                    continue;
                }
                if (! javaField.isAccessible())
                {
                    javaField.setAccessible(true);
                }
                ValueTypeField field = new ValueTypeField(javaField);
                fieldList.add(field);
            }

            fields = (ValueTypeField[])fieldList.toArray(new ValueTypeField[fieldList.size()]);

            // Check methods for readObject/writeObject. Also check for
            // abstract interfaces.
            Method[] methods = _class.getDeclaredMethods();
            int countThrowsRemoteException = 0;
            int nm = methods.length;
            for (int m = 0; m < nm; m++)
            {
                Method method = methods[m];
                Class[] types = method.getParameterTypes();
                if (types.length == 1
                    && types[0] == java.io.ObjectInputStream.class
                    && (method.getModifiers() & Modifier.PRIVATE) != 0
                    && method.getName().equals("readObject"))
                {
                    _readObject = method;
                    if (! _readObject.isAccessible())
                    {
                        _readObject.setAccessible(true);
                    }
                }
                if (types.length == 1
                    && types[0] == java.io.ObjectOutputStream.class
                    && (method.getModifiers() & Modifier.PRIVATE) != 0
                    && method.getName().equals("writeObject"))
                {
                    _writeObject = method;
                    if (! _writeObject.isAccessible())
                    {
                        _writeObject.setAccessible(true);
                    }
                }

                if (types.length == 0
                    && method.getReturnType() == java.lang.Object.class
                    && method.getName().equals("writeReplace"))
                {
                    _writeReplace = method;
                    if (! _writeReplace.isAccessible())
                    {
                        _writeReplace.setAccessible(true);
                    }
                }
                if (types.length == 0
                && method.getReturnType() == java.lang.Object.class
                    && method.getName().equals("readResolve"))
                {
                    _readResolve = method;
                    if (! _readResolve.isAccessible())
                    {
                        _readResolve.setAccessible(true);
                    }
                }
                Class[] exceptions = method.getExceptionTypes();
                for (int i = 0; i < exceptions.length; i++)
                {
                    Class exception = exceptions[i];
                    if (exception.isAssignableFrom(java.rmi.RemoteException.class))
                    {
                        // TODO: check Java to IDL wording for this
                        countThrowsRemoteException++;
                        break;
                    }
                }
            }

            hasReadOrWriteObject = _readObject != null || _writeObject != null;
            hasReadObject = _readObject != null;
            hasWriteObject = _writeObject != null;
            hasWriteReplace = _writeReplace != null;
            hasReadResolve = _readResolve != null;

            isAbstractInterface = ! isObjectRef
                && _class.isInterface()
                && countThrowsRemoteException == methods.length;

            Class superclass = _class.getSuperclass();
            if((superclass != null) && (superclass != java.lang.Object.class) && (!isIDLEntity ))
            {
                parent = getInstance(superclass);
            }

            hasParentState = parent != null
                && (parent.fields.length > 0
                    || parent.isExternalizable
                    || parent.hasReadOrWriteObject
                    || parent.hasParentState);

            requiresCustomSerialization = hasWriteObject || isExternalizable;

            initRepositoryID();
            initTypeCode();

            isAnyOrObjectRefOrAbstractInterface = isAny || isObjectRef || isAbstractInterface;
        }
        catch (Exception ex)
        {
            throw ExceptionUtil.getRuntimeException(ex);
        }
        finally
        {
            if(!recursive)
            {
            _initMap.remove(theClass);
        }
    }
    }

    protected void initRepositoryID()
    {
        final String sixteenZeros = "0000000000000000";
        final int requiredLength = 16;
       /* if (isAny)
        {
            id = "#ANY-TODO#";
            return;
        }*/
        if (isArray && primitiveArray != 0)
        {
            id = "RMI:" + _class.getName() + ":" + sixteenZeros;
            return;
        }
        if (_class == String.class)
        {
            id = "IDL:omg.org/CORBA/WStringValue:1.0";
            return;
        }
        if (isObjectRef)
        {
            id = "RMI:" + _class.getName() + ":" + sixteenZeros;
            return;
        }
        if (_class == java.lang.Class.class)
        {
            id = "RMI:javax.rmi.CORBA.ClassDesc:2BABDA04587ADCCC:CFBF02CF5294176B";
            return;
        }
        if (_class == java.math.BigInteger.class)
        {
            id = "RMI:java.math.BigInteger:E2F79B6E7A470003:8CFC9F1FA93BFB1D";
            return;
        }
        if (_objectStreamClass == null)
        {
            id = "???";
            return;
        }
        long structuralUID = computeStructuralUID(this);
        long serialVersionUID = _objectStreamClass.getSerialVersionUID();
        String structuralUIDString = Long.toHexString(structuralUID).toUpperCase();
        String serialVersionUIDString = Long.toHexString(serialVersionUID).toUpperCase();
        int currentLength;
        int lengthNeeded;
        currentLength = structuralUIDString.length();
        if (currentLength < requiredLength)
        {
            lengthNeeded = requiredLength - currentLength;
            structuralUIDString = sixteenZeros.substring(0, lengthNeeded) + structuralUIDString;
        }
        currentLength = serialVersionUIDString.length();
        if (currentLength < requiredLength)
        {
            lengthNeeded = requiredLength - currentLength;
            serialVersionUIDString = sixteenZeros.substring(0, lengthNeeded) + serialVersionUIDString;
        }
        id = "RMI:" + _class.getName() + ":" + structuralUIDString + ":" + serialVersionUIDString;
    }

    protected void initTypeCode()
    {
        if (isObjectRef)
        {
            tc = new TypeCode(TCKind.tk_objref);
            tc.id(id);
            tc.name("");
        }
        else if (isArray || isIDLEntity || _class == String.class)
        {
            tc = new TypeCode(TCKind.tk_value_box);
            tc.id(id);
            tc.name("");
            if (_class == String.class)
            {
                tc.content_type(new TypeCode(TCKind.tk_wstring));
            }
            else if (isArray)
            {
                TypeCode seqTC = new TypeCode(TCKind.tk_sequence);
                if (primitiveArray != 0)
                {
                    seqTC.content_type(PrimitiveType.getTypeCode(primitiveArray));
                }
                else
                {
                    seqTC.content_type(element.tc);
                }
                tc.content_type(seqTC);
            }
            else if (isIDLEntity)
            {
                // TODO tc.content_type(helper.type());
            }
        }
        else
        {
            tc = new TypeCode(TCKind.tk_value);
            tc.id(id);
            tc.name("");

            // TODO: value modifier
            if (requiresCustomSerialization)
            {
                tc.type_modifier((short)1);
            }
            else if (isAbstractInterface)
            {
                tc.type_modifier((short)2);
            }
            else
            {
                tc.type_modifier((short)0);
            }
            if (parent == null)
            {
                tc.concrete_base_type(TC_NULL);
            }
            else
            {
                // TODO: check validity of this
                tc.concrete_base_type(TC_NULL);
                // tc.concrete_base_type(getTypeCode(parent));
            }
            // TODO: member fields
            tc.member_count(0);
        }
    }

    static long computeStructuralUID(ValueType vt)
    {
        Class c = vt._class;
        ObjectStreamClass osc = vt._objectStreamClass;
        ByteArrayOutputStream devnull = new ByteArrayOutputStream(512);
        long h = 0;
        try
        {
            if (! java.io.Serializable.class.isAssignableFrom(c)
                || c.isInterface())
            {
                return 0;
            }
            if (java.io.Externalizable.class.isAssignableFrom(c))
            {
                return 1;
            }
            MessageDigest md = MessageDigest.getInstance("SHA");
            DigestOutputStream mdo = new DigestOutputStream(devnull, md);
            DataOutputStream data = new DataOutputStream(mdo);
            if (vt.parent != null)
            {
                data.writeLong(computeStructuralUID(vt.parent));
            }
            if (vt.hasWriteObject)
            {
                data.writeInt(2);
            }
            else
            {
                data.writeInt(1);
            }
            List fieldList = new ArrayList(vt.fields.length);
            for (int i = 0; i < vt.fields.length; i++)
            {
                fieldList.add(vt.fields[i].javaField);
            }
            Field[] fields = (Field[])fieldList.toArray(new Field[fieldList.size()]);
            Arrays.sort(fields, FieldByNameComparator.SINGLETON);
            for (int i = 0; i < vt.fields.length; i++)
            {
                Field f = fields[i];
                data.writeUTF(f.getName());
                data.writeUTF(JavaClass.getSignature(f.getType()));
            }
            data.flush();
            byte[] hasharray = md.digest();
            for (int i = 0; i < Math.min(8, hasharray.length); i++)
            {
                h += (long)(hasharray[i] & 255) << (i * 8);
            }
            return h;
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    /**
     ** Map an RMI/IDL Repository ID to a java.lang.Class.
     **/
    static Class getClass(String id)
    {
        if (id.startsWith("RMI:"))
        {
            int endClass = id.indexOf(':', 4);
            if (endClass == -1)
            {
                throw new org.omg.CORBA.INV_IDENT(id);
            }
            String className = id.substring(4, endClass);
            if (className.equals("javax.rmi.CORBA.ClassDesc"))
            {
                return Class.class;
            }
            else
            {
                return loadClass(className);
            }
        }
        else if (id.equals("IDL:omg.org/CORBA/WStringValue:1.0"))
        {
            return java.lang.String.class;
        }
        else if (id.startsWith("IDL:omg.org/"))
        {
            int endClass = id.indexOf(':', 12);
            if (endClass == -1)
            {
                throw new org.omg.CORBA.INV_IDENT(id);
            }
            String className = "org.omg" + id.substring( "IDL:omg.org".length(), endClass).replace('/', '.');
            return loadClass(className);
        }
        else if (id.startsWith("IDL:"))
        {
            int endClass = id.indexOf(':', 4);
            if (endClass == -1)
            {
                throw new org.omg.CORBA.INV_IDENT(id);
            }
            String className = id.substring(4, endClass).replace('/', '.');
            return loadClass(className);
        }
        else
        {
            throw new org.omg.CORBA.INV_IDENT(id);
        }
    }

    public Class getTheClass()
    {
        return _class;
    }

    static Class loadClass(String className)
    {
        return ThreadContext.loadClass(className);
    }
}
