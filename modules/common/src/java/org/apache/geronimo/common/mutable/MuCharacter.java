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

import org.apache.geronimo.common.CloneableObject;
import org.apache.geronimo.common.NotCoercibleException;

/**
 * A mutable character class.
 *
 * @version $Revision: 1.3 $ $Date: 2003/09/03 17:39:07 $
 */
public class MuCharacter
    extends CloneableObject
    implements Comparable, Serializable, Mutable
{
    /** <code>char</code> value */
    private char value = 0;
    
    /**
     * Construct a new mutable character.
     */
    public MuCharacter() {}
    
    /**
     * Construct a new mutable character.
     *
     * @param c    <code>char</code> value.
     */
    public MuCharacter(char c) {
        value = c;
    }
    
    /**
     * Construct a new mutable character.
     *
     * @param obj  Object to convert to a <code>char</code>.
     */
    public MuCharacter(Object obj) {
        setValue(obj);
    }
    
    /**
     * Set the value.
     *
     * @param c    <code>char</code> value.
     * @return     The previous value.
     */
    public char set(char c) {
        char old = value;
        value = c;
        return old;
    }
    
    /**
     * Get the current value.
     *
     * @return     The current value.
     */
    public char get() {
        return value;
    }
    
    /**
     * Return the <code>char</code> value of this mutable character.
     *
     * @return   <code>char</code> value.
     */
    public char charValue() {
        return value;
    }
    
    /**
     * Compares this object with the specified long for order.
     *
     * @param other   Value to compare with.
     * @return        A negative integer, zero, or a positive integer as
     *                this object is less than, equal to, or greater than
     *                the specified object.
     */
    public int compareTo(char other) {
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
     * @throws ClassCastException    Object is not a MuCharacter.
     */
    public int compareTo(Object obj) {
        return compareTo(((MuCharacter)obj).get());
    }
    
    /**
     * Convert this mutable character to a string.
     *
     * @return   String value.
     */
    public String toString() {
        return String.valueOf(value);
    }
    
    /**
     * Get the hash code of this mutable character.
     *
     * @return   Hash code.
     */
    public int hashCode() {
        return value;
    }
    
    /**
     * Test the equality of this mutable character and another object.
     *
     * @param obj    Qbject to test equality with.
     * @return       True if object is equal.
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj != null && obj.getClass() == getClass()) {
            return value == ((MuCharacter)obj).charValue();
        }
        
        return false;
    }
    

    /////////////////////////////////////////////////////////////////////////
    //                            Mutable Support                          //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Set the value of this mutable character.
     *
     * @param obj  Object to convert to a <code>char</code>.
     *
     * @throws NotCoercibleException    Can not convert to <code>char</code>.
     */
    public void setValue(Object obj) {
        if (obj instanceof MuCharacter) {
            value = ((MuCharacter)obj).value;
        }
        else if (obj instanceof Character) {
            value = ((Character)obj).charValue();
        }
        else if (obj instanceof Number) {
            value = (char)((Number)obj).intValue();
        }
        else {
            throw new NotCoercibleException("can not convert to 'char': " + obj);
        }
    }
    
    /**
     * Return the char value of this mutable character.
     *
     * @return   <code>java.lang.Character</code> value.
     */
    public Object getValue() {
        return new Character(value);
    }
}