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

package org.apache.geronimo.common.mutable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import org.apache.geronimo.common.Primitives;
import org.apache.geronimo.common.NotCoercibleException;

/**
 * A mutable float class.
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:26 $
 */
public class MuFloat
    extends MuNumber
{
    /** Float value */
    private float value; // = 0;
    
    /**
     * Construct a new mutable float.
     */
    public MuFloat() {}
    
    /**
     * Construct a new mutable float.
     *
     * @param f    <code>float</code> value.
     */
    public MuFloat(float f) {
        value = f;
    }
    
    /**
     * Construct a new mutable float.
     *
     * @param obj     Object to convert to a <code>float</code> value.
     */
    public MuFloat(Object obj) {
        setValue(obj);
    }
    
    /**
     * Set the value.
     *
     * @param f    <code>float</code> value.
     * @return     The previous value.
     */
    public float set(float f) {
        float old = value;
        value = f;
        return old;
    }
    
    /**
     * Get the current value.
     *
     * @return  The current value.
     */
    public float get() {
        return value;
    }
    
    /**
     * Set the value to value only if the current value is equal to 
     * the assumed value.
     *
     * @param assumed  The assumed value.
     * @param b        The new value.
     * @return         True if value was changed.
     */
    public boolean commit(float assumed, float b) {
        boolean success = Primitives.equals(assumed, value);
        if (success) value = b;
        return success;
    }
    
    /**
     * Swap values with another mutable float.
     *
     * @param b       Mutable float to swap values with.
     * @return        The new value.
     */
    public float swap(MuFloat b) {
        if (b == this) return value;
        
        float temp = value;
        value = b.value;
        b.value = temp;
        
        return value;
    }
    
    /**
     * Add the specified amount.
     *
     * @param amount  Amount to add.
     * @return        The new value.
     */
    public float add(float amount) {
        return value += amount;
    }
    
    /**
     * Subtract the specified amount.
     *
     * @param amount  Amount to subtract.
     * @return        The new value.
     */
    public float subtract(float amount) {
        return value -= amount;
    }
    
    /**
     * Multiply by the specified factor.
     *
     * @param factor  Factor to multiply by.
     * @return        The new value.
     */
    public float multiply(float factor) {
        return value *= factor;
    }
    
    /**
     * Divide by the specified factor.
     *
     * @param factor  Factor to divide by.
     * @return        The new value.
     */
    public float divide(float factor) {
        return value /= factor;
    }
    
    /**
     * Set the value to the negative of its current value.
     *
     * @return     The new value.
     */
    public float negate() {
        value = (-value);
        return value;
    }
    
    /**
     * Compares this object with the specified float for order.
     *
     * @param other   Value to compare with.
     * @return        A negative integer, zero, or a positive integer as
     *                this object is less than, equal to, or greater than
     *                the specified object.
     */
    public int compareTo(float other) {
        return (value < other) ? -1 : Primitives.equals(value, other) ? 0 : 1;
    }
    
    /**
     * Compares this object with the specified object for order.
     *
     * @param obj     Value to compare with.
     * @return        A negative integer, zero, or a positive integer as
     *                this object is less than, equal to, or greater than
     *                the specified object.
     *
     * @throws ClassCastException    Object is not a MuFloat.
     */
    public int compareTo(Object obj) {
        return compareTo(((MuFloat)obj).get());
    }
    
    /**
     * Convert this mutable float to a string.
     *
     * @return   String value.
     */
    public String toString() {
        return String.valueOf(value);
    }
    
    /**
     * Get the hash code for this mutable float.
     *
     * @return   Hash code.
     */
    public int hashCode() {
        return new HashCodeBuilder().append(value).toHashCode();
    }
    
    /**
     * Test the equality of this mutable double with another object.
     *
     * @param obj    Object to test equality with.
     * @return       True if object is equal.
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj != null && obj.getClass() == getClass()) {
            return Primitives.equals(value, ((MuFloat)obj).floatValue());
        }
        
        return false;
    }


    /////////////////////////////////////////////////////////////////////////
    //                             Number Support                          //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Return the <code>byte</code> value of this object.
     *
     * @return   <code>byte</code> value.
     */
    public byte byteValue() {
        return (byte)value;
    }
    
    /**
     * Return the <code>short</code> value of this object.
     *
     * @return   <code>short</code> value.
     */
    public short shortValue() {
        return (short)value;
    }
    
    /**
     * Return the <code>int</code> value of this object.
     *
     * @return   <code>int</code> value.
     */
    public int intValue() {
        return (int)value;
    }
    
    /**
     * Return the <code>long</code> value of this object.
     *
     * @return   <code>long</code> value.
     */
    public long longValue() {
        return (long)value;
    }
    
    /**
     * Return the <code>float</code> value of this object.
     *
     * @return   <code>float</code> value.
     */
    public float floatValue() {
        return value;
    }
    
    /**
     * Return the <code>double</code> value of this object.
     *
     * @return   <code>double</code> value.
     */
    public double doubleValue() {
        return (double)value;
    }


    /////////////////////////////////////////////////////////////////////////
    //                            Mutable Support                          //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Set the value of this mutable float.
     *
     * @param obj  Object to convert to a <code>float</code> value.
     *
     * @throws NotCoercibleException    Can not convert to <code>float</code>.
     */
    public void setValue(Object obj) {
        if (obj instanceof Number) {
            value = ((Number)obj).floatValue();
        }
        else if (obj instanceof String) {
            try {
                value = Float.parseFloat(String.valueOf(obj));
            }
            catch (Exception e) {
                throw new NotCoercibleException("can not convert to 'float': " + obj);
            }
        }
        else {
            throw new NotCoercibleException("can not convert to 'float': " + obj);
        }
    }
    
    /**
     * Get the float value of this mutable float.
     *
     * @return   <code>java.lang.Float</code> value.
     */
    public Object getValue() {
        return new Float(value);
    }
}