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

import java.io.Serializable;

import org.apache.geronimo.common.CloneableObject;
import org.apache.geronimo.common.NotCoercibleException;

/**
 * A mutable character class.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:03 $
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