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

package org.apache.geronimo.common.mutable;

/**
 * An abstract mutable number class.
 *
 * <p>This is a base wrapper class for <code>java.lang.Number</code>.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:03 $
 */
public abstract class MuNumber 
    extends Number
    implements Comparable, Cloneable, Mutable
{
    /**
     * Returns the value of the specified number as a <code>byte</code>.
     * This may involve rounding or truncation.
     *
     * @return  The numeric value represented by this object after conversion
     *          to type <code>byte</code>.
     */
    public byte byteValue() {
        return (byte)longValue();
    }
    
    /**
     * Returns the value of the specified number as a <code>short</code>.
     * This may involve rounding or truncation.
     *
     * @return  The numeric value represented by this object after conversion
     *          to type <code>short</code>.
     */
    public short shortValue() {
        return (short)longValue();
    }
    
    /**
     * Returns the value of the specified number as a <code>int</code>.
     * This may involve rounding or truncation.
     *
     * @return  The numeric value represented by this object after conversion
     *          to type <code>int</code>.
     */
    public int intValue() {
        return (int)longValue();
    }
    
    /**
     * Returns the value of the specified number as a <code>float</code>.
     * This may involve rounding or truncation.
     *
     * @return  The numeric value represented by this object after conversion
     *          to type <code>float</code>.
     */
    public float floatValue() {
        return (float)doubleValue();
    }
    
    /**
     * Return a cloned copy of this mutable double.
     *
     * @return   Cloaned mutable double.
     */
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
