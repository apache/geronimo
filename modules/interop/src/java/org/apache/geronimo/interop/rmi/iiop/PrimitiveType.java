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

import org.apache.geronimo.interop.util.ArrayUtil;

public class PrimitiveType
{
    public static final int BOOLEAN = 1;
    public static final int BYTE    = 2;
    public static final int CHAR    = 3;
    public static final int DOUBLE  = 4;
    public static final int FLOAT   = 5;
    public static final int INT     = 6;
    public static final int LONG    = 7;
    public static final int SHORT   = 8;

    public static final ObjectHelper BOOLEAN_ARRAY_HELPER = new ObjectHelper()
    {
        public Object read(ObjectInputStream input)
        {
            int n = input._cdrInput.read_long();
            if (n == 0)
            {
                return ArrayUtil.EMPTY_BOOLEAN_ARRAY;
            }
            boolean[] array = new boolean[n];
            input._cdrInput.read_boolean_array(array, 0, n);
            return array;
        }

        public void write(ObjectOutputStream output, Object value)
        {
            boolean[] array = (boolean[])value;
            int n = array.length;
            output._cdrOutput.write_long(n);
            output._cdrOutput.write_boolean_array(array, 0, n);
        }
    }
    ;

    public static final ObjectHelper BYTE_ARRAY_HELPER = new ObjectHelper()
    {
        public Object read(ObjectInputStream input)
        {
            int n = input._cdrInput.read_long();
            if (n == 0)
            {
                return ArrayUtil.EMPTY_BYTE_ARRAY;
            }
            byte[] array = new byte[n];
            input._cdrInput.read_octet_array(array, 0, n);
            return array;
        }

        public void write(ObjectOutputStream output, Object value)
        {
            byte[] array = (byte[])value;
            int n = array.length;
            output._cdrOutput.write_long(n);
            output._cdrOutput.write_octet_array(array, 0, n);
        }
    }
    ;

    public static final ObjectHelper CHAR_ARRAY_HELPER = new ObjectHelper()
    {
        public Object read(ObjectInputStream input)
        {
            int n = input._cdrInput.read_long();
            if (n == 0)
            {
                return ArrayUtil.EMPTY_CHAR_ARRAY;
            }
            char[] array = new char[n];
            input._cdrInput.read_wchar_array(array, 0, n);
            return array;
        }

        public void write(ObjectOutputStream output, Object value)
        {
            char[] array = (char[])value;
            int n = array.length;
            output._cdrOutput.write_long(n);
            output._cdrOutput.write_wchar_array(array, 0, n);
        }
    }
    ;

    public static final ObjectHelper DOUBLE_ARRAY_HELPER = new ObjectHelper()
    {
        public Object read(ObjectInputStream input)
        {
            int n = input._cdrInput.read_long();
            if (n == 0)
            {
                return ArrayUtil.EMPTY_DOUBLE_ARRAY;
            }
            double[] array = new double[n];
            input._cdrInput.read_double_array(array, 0, n);
            return array;
        }

        public void write(ObjectOutputStream output, Object value)
        {
            double[] array = (double[])value;
            int n = array.length;
            output._cdrOutput.write_long(n);
            output._cdrOutput.write_double_array(array, 0, n);
        }
    }
    ;

    public static final ObjectHelper FLOAT_ARRAY_HELPER = new ObjectHelper()
    {
        public Object read(ObjectInputStream input)
        {
            int n = input._cdrInput.read_long();
            if (n == 0)
            {
                return ArrayUtil.EMPTY_FLOAT_ARRAY;
            }
            float[] array = new float[n];
            input._cdrInput.read_float_array(array, 0, n);
            return array;
        }

        public void write(ObjectOutputStream output, Object value)
        {
            float[] array = (float[])value;
            int n = array.length;
            output._cdrOutput.write_long(n);
            output._cdrOutput.write_float_array(array, 0, n);
        }
    }
    ;

    public static final ObjectHelper INT_ARRAY_HELPER = new ObjectHelper()
    {
        public Object read(ObjectInputStream input)
        {
            int n = input._cdrInput.read_long();
            if (n == 0)
            {
                return ArrayUtil.EMPTY_INT_ARRAY;
            }
            int[] array = new int[n];
            input._cdrInput.read_long_array(array, 0, n);
            return array;
        }

        public void write(ObjectOutputStream output, Object value)
        {
            int[] array = (int[])value;
            int n = array.length;
            output._cdrOutput.write_long(n);
            output._cdrOutput.write_long_array(array, 0, n);
        }
    }
    ;

    public static final ObjectHelper LONG_ARRAY_HELPER = new ObjectHelper()
    {
        public Object read(ObjectInputStream input)
        {
            int n = input._cdrInput.read_long();
            if (n == 0)
            {
                return ArrayUtil.EMPTY_LONG_ARRAY;
            }
            long[] array = new long[n];
            input._cdrInput.read_longlong_array(array, 0, n);
            return array;
        }

        public void write(ObjectOutputStream output, Object value)
        {
            long[] array = (long[])value;
            int n = array.length;
            output._cdrOutput.write_long(n);
            output._cdrOutput.write_longlong_array(array, 0, n);
        }
    }
    ;

    public static final ObjectHelper SHORT_ARRAY_HELPER = new ObjectHelper()
    {
        public Object read(ObjectInputStream input)
        {
            int n = input._cdrInput.read_long();
            if (n == 0)
            {
                return ArrayUtil.EMPTY_SHORT_ARRAY;
            }
            short[] array = new short[n];
            input._cdrInput.read_short_array(array, 0, n);
            return array;
        }

        public void write(ObjectOutputStream output, Object value)
        {
            short[] array = (short[])value;
            int n = array.length;
            output._cdrOutput.write_long(n);
            output._cdrOutput.write_short_array(array, 0, n);
        }
    }
    ;

    public static int get(Class _class)
    {
        if (_class == boolean.class)
        {
            return BOOLEAN;
        }
        else if (_class == byte.class)
        {
            return BYTE;
        }
        else if (_class == char.class)
        {
            return CHAR;
        }
        else if (_class == double.class)
        {
            return DOUBLE;
        }
        else if (_class == float.class)
        {
            return FLOAT;
        }
        else if (_class == int.class)
        {
            return INT;
        }
        else if (_class == long.class)
        {
            return LONG;
        }
        else if (_class == short.class)
        {
            return SHORT;
        }
        else
        {
            throw new IllegalArgumentException("class = " +_class.getName());
        }
    }

    public static ObjectHelper getArrayHelper(Class _class)
    {
        if (_class == boolean.class)
        {
            return BOOLEAN_ARRAY_HELPER;
        }
        else if (_class == byte.class)
        {
            return BYTE_ARRAY_HELPER;
        }
        else if (_class == char.class)
        {
            return CHAR_ARRAY_HELPER;
        }
        else if (_class == double.class)
        {
            return DOUBLE_ARRAY_HELPER;
        }
        else if (_class == float.class)
        {
            return FLOAT_ARRAY_HELPER;
        }
        else if (_class == int.class)
        {
            return INT_ARRAY_HELPER;
        }
        else if (_class == long.class)
        {
            return LONG_ARRAY_HELPER;
        }
        else if (_class == short.class)
        {
            return SHORT_ARRAY_HELPER;
        }
        else
        {
            throw new IllegalArgumentException("class = " +_class.getName());
        }
    }

    public static org.omg.CORBA.TypeCode getTypeCode(int p)
    {
        switch (p)
        {
            case BOOLEAN:
                return TypeCode.BOOLEAN;
            case BYTE:    // java byte is IDL octet
                return TypeCode.OCTET;
            case CHAR:
                return TypeCode.CHAR;
            case DOUBLE:
                return TypeCode.DOUBLE;
            case FLOAT:
                return TypeCode.FLOAT;
            case INT:     // java int is IDL long
                return TypeCode.LONG;
            case LONG:    // java long is IDL long long
                return TypeCode.LONGLONG;
            case SHORT:
                return TypeCode.SHORT;
            default:
                throw new IllegalArgumentException("primitive type = " + p);
        }
    }
}
