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

import org.apache.geronimo.common.coerce.NotCoercibleException;

/**
 * A mutable integer class.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/27 09:08:10 $
 */
public class MuInteger
    extends MuNumber
{
    /** Integer value */
    private int value;
    
    /**
     * Construct a new mutable integer.
     */
    public MuInteger() {}
    
    /**
     * Construct a new mutable integer.
     *
     * @param i    Integer value.
     */
    public MuInteger(int i) {
        value = i;
    }
    
    /**
     * Construct a new mutable integer.
     *
     * @param obj  Object to convert to a <code>int</code> value.
     */
    public MuInteger(Object obj) {
        setValue(obj);
    }

    /**
     * Set the value.
     *
     * @param i    <code>int</code> value.
     * @return     The previous value.
     */
    public int set(int i) {
        int old = value;
        value = i;
        return old;
    }

    /**
     * Get the current value.
     *
     * @return  The current value.
     */
    public int get() {
        return value;
    }


    /**
     * Set the value to value only if the current value is equal to 
     * the assumed value.
     *
     * @param assumed  The assumed value.
     * @param i        The new value.
     * @return         True if value was changed.
     */
    public boolean commit(int assumed, int i) {
        boolean success = (assumed == value);
        if (success) value = i;
        return success;
    }
    
    /**
     * Swap values with another mutable integer.
     *
     * @param i    Mutable integer to swap values with.
     * @return     The new value.
     */
    public int swap(MuInteger i) {
        if (i == this) return i.value;
        
        int temp = value;
        value = i.value;
        i.value = temp;
        
        return value;
    }
    
    /**
     * Increment the value of this mutable integer.
     *
     * @return  Int value.
     */
    public int increment() {
        return ++value;
    }
    
    /**
     * Decrement the value of this mutable integer.
     *
     * @return  Int value.
     */
    public int decrement() {
        return --value;
    }
    
    /**
     * Add the specified amount.
     *
     * @param amount  Amount to add.
     * @return        The new value.
     */
    public int add(int amount) {
        return value += amount;
    }
    
    /**
     * Subtract the specified amount.
     *
     * @param amount  Amount to subtract.
     * @return        The new value.
     */
    public int subtract(int amount) {
        return value -= amount;
    }
    
    /**
     * Multiply by the specified factor.
     *
     * @param factor  Factor to multiply by.
     * @return        The new value.
     */
    public int multiply(int factor) {
        return value *= factor;
    }
    
    /**
     * Divide by the specified factor.
     *
     * @param factor  Factor to divide by.
     * @return        The new value.
     */
    public int divide(int factor) {
        return value /= factor;
    }
    
    /**
     * Set the value to the negative of its current value.
     *
     * @return     The new value.
     */
    public int negate() {
        value = -value;
        return value;
    }
    
    /**
     * Set the value to its complement.
     *
     * @return     The new value.
     */
    public int complement() {
        value = ~value;
        return value;
    }
    
    /**
     * <i>AND</i>s the current value with the specified value.
     *
     * @param b    Value to <i>and</i> with.
     * @return     The new value.
     */
    public int and(int b) {
        value = value & b;
        return value;
    }
    
    /**
     * <i>OR</i>s the current value with the specified value.
     *
     * @param b    Value to <i>or</i> with.
     * @return     The new value.
     */
    public int or(int b) {
        value = value | b;
        return value;
    }
    
    /**
     * <i>XOR</i>s the current value with the specified value.
     *
     * @param b    Value to <i>xor</i> with.
     * @return     The new value.
     */
    public int xor(int b) {
        value = value ^ b;
        return value;
    }
    
    /**
     * Shift the current value to the <i>right</i>.
     *
     * @param bits    The number of bits to shift.
     * @return        The new value.
     */
    public int shiftRight(int bits) {
        value >>= bits;
        return value;
    }
    
    /**
     * Shift the current value to the <i>right</i> with a zero extension.
     *
     * @param bits    The number of bits to shift.
     * @return        The new value.
     */
    public int shiftRightZero(int bits) {
        value >>>= bits;
        return value;
    }
    
    /**
     * Shift the current value to the <i>left</i>.
     *
     * @param bits    The number of bits to shift.
     * @return        The new value.
     */
    public int shiftLeft(int bits) {
        value <<= bits;
        return value;
    }
    
    /**
     * Compares this object with the specified int for order.
     *
     * @param other   Value to compare with.
     * @return        A negative integer, zero, or a positive integer as
     *                this object is less than, equal to, or greater than
     *                the specified object.
     */
    public int compareTo(int other) {
        return (value < other) ? -1 : (value == other) ? 0 : 1;
    }
    
    /**
     * Compares this object with the specified object for order.
     *
     * @param obj     Value to compare with.
     * @return        A negative integer, zero, or a positive integer as
     *                this object is less than, equal to, or greater than
     *                the specified object.
     *
     * @throws ClassCastException    Object is not a MuInteger.
     */
    public int compareTo(Object obj) {
        return compareTo(((MuInteger)obj).value);
    }
    
    /**
     * Convert this mutable integer to a string.
     *
     * @return   String value
     */
    public String toString() {
        return String.valueOf(value);
    }
    
    /**
     * Get the hash code for this mutable integer.
     *
     * @return   Hash code.
     */
    public int hashCode() {
        return value;
    }
    
    /**
     * Test the equality of this mutable integer and another object.
     *
     * @param obj    Qbject to test equality with.
     * @return       True if object is equal.
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj != null && obj.getClass() == getClass()) {
            return value == ((MuInteger)obj).intValue();
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
        return value;
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
     * Set the value of this mutable integer.
     *
     * @param obj  Object to convert to an integer value.
     *
     * @throws NotCoercibleException    Can not convert to <code>int</code>.
     */
    public void setValue(Object obj) {
        if (obj instanceof Number) {
            value = ((Number)obj).intValue();
        }
        else {
            throw new NotCoercibleException("can not convert to 'int': " + obj);
        }
    }
    
    /**
     * Get the value of this mutable integer.
     *
     * @return   <code>java.lang.Integer</code> value.
     */
    public Object getValue() {
        return new Integer(value);
    }
}