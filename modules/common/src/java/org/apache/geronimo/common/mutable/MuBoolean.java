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

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import org.apache.geronimo.common.CloneableObject;
import org.apache.geronimo.common.coerce.NotCoercibleException;

/**
 * A mutable boolean class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/24 20:51:22 $
 */
public class MuBoolean
    extends CloneableObject
    implements Comparable, Serializable, Mutable
{
    /** Boolean value */
    private boolean value; // = false;
    
    /**
     * Construct a new mutable boolean.
     */
    public MuBoolean() {}
    
    /**
     * Construct a new mutable boolean.
     *
     * @param b    Boolean value.
     */
    public MuBoolean(boolean b) {
        value = b;
    }
    
    /**
     * Construct a new mutable boolean.
     *
     * @param obj  Object to convert to a boolean value.
     */
    public MuBoolean(Object obj) {
        setValue(obj);
    }
    
    /**
     * Construct a new mutable boolean.
     *
     * @param value  String to convert to a boolean value.
     */
    public MuBoolean(String value) {
        set(Boolean.valueOf(value));
    }
    
    /**
     * Return the value of this mutable boolean.
     *
     * @return   Boolean value.
     */
    public boolean booleanValue() {
        return value;
    }
    
    /**
     * Set the value.
     *
     * @param b    Boolean value.
     * @return     The previous value.
     */
    public boolean set(boolean b) {
        boolean old = value;
        value = b;
        return old;
    }
    
    /**
     * Set the value.
     *
     * @param b    Boolean value.
     * @return     The previous value.
     */
    public boolean set(Boolean b) {
        boolean old = value;
        value = b.booleanValue();
        return old;
    }
    
    /**
     * Set the value.
     *
     * @param b    Boolean value.
     * @return     The previous value.
     */
    public boolean set(MuBoolean b) {
        boolean old = value;
        value = b.value;
        return old;
    }
    
    /**
     * Get the current value.
     *
     * @return  The current value.
     */
    public boolean get() {
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
    public boolean commit(boolean assumed, boolean b) {
        boolean success = (assumed == value);
        if (success) value = b;
        return success;
    }
    
    /**
     * Swap values with another mutable boolean.
     *
     * @param b    Mutable boolean to swap values with.
     * @return     The new value.
     */
    public boolean swap(MuBoolean b) {
        if (b == this) return value;
        
        boolean temp = value;
        value = b.value;
        b.value = temp;
        
        return value;
    }
    
    /**
     * Set the value to its complement.
     *
     * @return  The new value.
     */
    public boolean complement() {
        value = !value;
        return value;
    }
    
    /**
     * <i>AND</i>s the current value with the specified value.
     *
     * @param b    Value to <i>and</i> with.
     * @return     The new value.
     */
    public boolean and(boolean b) {
        value &= b;
        
        return value;
    }
    
    /**
     * <i>OR</i>s the current value with the specified value.
     *
     * @param b    Value to <i>or</i> with.
     * @return     The new value.
     */
    public boolean or(boolean b) {
        value |= b;
        
        return value;
    }
    
    /**
     * <i>XOR</i>s the current value with the specified value.
     *
     * @param b    Value to <i>xor</i> with.
     * @return     The new value.
     */
    public boolean xor(boolean b) {
        value ^= b;
        
        return value;
    }
    
    /**
     * Compares this object with the specified boolean for order.
     *
     * @param bool    Boolean to compare with.
     * @return        A negative integer, zero, or a positive integer as
     *                this object is less than, equal to, or greater than
     *                the specified object.
     */
    public int compareTo(boolean bool) {
        return (value == bool) ? 0 : (value) ? 1 : -1;
    }
    
    /**
     * Compares this object with the specified object for order.
     *
     * @param bool    Boolean to compare with.
     * @return        A negative integer, zero, or a positive integer as
     *                this object is less than, equal to, or greater than
     *                the specified object.
     *
     * @throws ClassCastException    Object is not a MuBoolean.
     */
    public int compareTo(Object obj) throws ClassCastException {
        return compareTo(((MuBoolean)obj).value);
    }
    
    /**
     * Convert to a string.
     *
     * @return   String value
     */
    public String toString() {
        return String.valueOf(value);
    }
    
    /**
     * Return the hash code for this mutable boolean.
     *
     * @return   Hash code
     */
    public int hashCode() {
        return new HashCodeBuilder().append(value).toHashCode();
    }
    
    /**
     * Test the equality of another object.
     *
     * @param obj  Object to test equality with.
     * @return     True if object is equal to this.
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj != null && obj.getClass() == getClass()) {
            MuBoolean bool = (MuBoolean)obj;
            return value == bool.value;
        }
        
        return false;
    }


    /////////////////////////////////////////////////////////////////////////
    //                            Mutable Support                          //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Set the value of this mutable boolean.
     *
     * <p>If value is a <code>java.lang.Boolean</code>, then use 
     *    <code>Boolean.booleanValue()</code> to determin value, if
     *    the object is non-null then the value is <i>true</i>, else
     *    it is <i>false</i>.
     *
     * @param obj  Object to convert to a boolean value.
     */
    public void setValue(Object obj) {
        if (obj instanceof MuBoolean) {
            value = ((MuBoolean)obj).value;
        }
        else if (obj instanceof Boolean) {
            value = ((Boolean)obj).booleanValue();
        }
        else if (obj != null) {
            value = true;
        }
        else {
            value = false;
        }
    }
    
    /**
     * Get the boolean value of this mutable boolean.
     *
     * @return   <code>java.lang.Boolean</code> value.
     */
    public Object getValue() {
        return value ? Boolean.TRUE : Boolean.FALSE;
    }
}

