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
package org.apache.geronimo.interop.util;

public class ArrayUtil {
    public static final boolean[] EMPTY_BOOLEAN_ARRAY =
            {
            }
            ;
    public static final char[] EMPTY_CHAR_ARRAY =
            {
            }
            ;
    public static final byte[] EMPTY_BYTE_ARRAY =
            {
            }
            ;
    public static final short[] EMPTY_SHORT_ARRAY =
            {
            }
            ;
    public static final int[] EMPTY_INT_ARRAY =
            {
            }
            ;
    public static final long[] EMPTY_LONG_ARRAY =
            {
            }
            ;
    public static final float[] EMPTY_FLOAT_ARRAY =
            {
            }
            ;
    public static final double[] EMPTY_DOUBLE_ARRAY =
            {
            }
            ;
    public static final Class[] EMPTY_CLASS_ARRAY =
            {
            }
            ;
    public static final Object[] EMPTY_OBJECT_ARRAY =
            {
            }
            ;
    public static final String[] EMPTY_STRING_ARRAY =
            {
            }
            ;

    public static byte[] copy(byte[] x) {
        return getBytes(x, 0, x.length);
    }

    public static byte[] concat(byte[] x, byte[] y) {
        byte[] z = new byte[x.length + y.length];
        System.arraycopy(x, 0, z, 0, x.length);
        System.arraycopy(y, 0, z, x.length, y.length);
        return z;
    }

    public static byte[] getBytes(byte[] x, int offset, int length) {
        byte[] y = new byte[length];
        System.arraycopy(x, offset, y, 0, length);
        return y;
    }

    public static int indexOf(byte[] x, byte b) {
        return indexOf(x, b, 0);
    }

    public static int indexOf(byte[] x, byte b, int startOffset) {
        int n = x.length;
        for (int i = startOffset; i < n; i++) {
            if (x[i] == b) {
                return i;
            }
        }
        return -1;
    }

    public static boolean[] newBooleanArray(int size, boolean[] init) {
        boolean[] array = new boolean[size];
        if (init != null) {
            System.arraycopy(init, 0, array, 0, Math.min(size, init.length));
        }
        return array;
    }

    public static char[] newCharArray(int size, char[] init) {
        char[] array = new char[size];
        if (init != null) {
            System.arraycopy(init, 0, array, 0, Math.min(size, init.length));
        }
        return array;
    }

    public static byte[] newByteArray(int size, byte[] init) {
        byte[] array = new byte[size];
        if (init != null) {
            System.arraycopy(init, 0, array, 0, Math.min(size, init.length));
        }
        return array;
    }

    public static short[] newShortArray(int size, short[] init) {
        short[] array = new short[size];
        if (init != null) {
            System.arraycopy(init, 0, array, 0, Math.min(size, init.length));
        }
        return array;
    }

    public static int[] newIntArray(int size, int[] init) {
        int[] array = new int[size];
        if (init != null) {
            System.arraycopy(init, 0, array, 0, Math.min(size, init.length));
        }
        return array;
    }

    public static long[] newLongArray(int size, long[] init) {
        long[] array = new long[size];
        if (init != null) {
            System.arraycopy(init, 0, array, 0, Math.min(size, init.length));
        }
        return array;
    }

    public static float[] newFloatArray(int size, float[] init) {
        float[] array = new float[size];
        if (init != null) {
            System.arraycopy(init, 0, array, 0, Math.min(size, init.length));
        }
        return array;
    }

    public static double[] newDoubleArray(int size, double[] init) {
        double[] array = new double[size];
        if (init != null) {
            System.arraycopy(init, 0, array, 0, Math.min(size, init.length));
        }
        return array;
    }

    public static Object[] newObjectArray(int size, Object[] init) {
        Object[] array = new Object[size];
        if (init != null) {
            System.arraycopy(init, 0, array, 0, Math.min(size, init.length));
        }
        return array;
    }

    public static Object[] newObjectArray(int size, Object[] init, Class type) {
        Object[] array = (Object[]) java.lang.reflect.Array.newInstance(type, size);
        if (init != null) {
            System.arraycopy(init, 0, array, 0, Math.min(size, init.length));
        }
        return array;
    }
}
