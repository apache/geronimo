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

package org.apache.geronimo.common.mutable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import org.apache.geronimo.common.Primitives;
import org.apache.geronimo.common.coerce.NotCoercibleException;

/**
 * A mutable float class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/24 20:51:22 $
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
        value = ((float)-value);
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
     * @param other   Value to compare with.
     * @return        A negative integer, zero, or a positive integer as
     *                this object is less than, equal to, or greater than
     *                the specified object.
     *
     * @throws ClassCastException    Object is not a MuFloat.
     */
    public int compareTo(Object obj) {
        return compareTo((MuFloat)obj);
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
        return (float)value;
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
