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
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */
package javax.enterprise.deploy.shared;

/**
 * Class ActionTypes defines enumeration values for the J2EE DeploymentStatus
 * actions.
 *
 * @version $Revision: 1.3 $ $Date: 2003/10/22 20:33:15 $
 */
public class ActionType {
    /**
     * The action is currently executing.
     */
    public static final ActionType EXECUTE = new ActionType(0);
    /**
     * The action has been canceled.
     */
    public static final ActionType CANCEL = new ActionType(1);
    /**
     * A stop operation is being performed on the DeploymentManager action command.
     */
    public static final ActionType STOP = new ActionType(2);

    private static final ActionType[] enumValueTable = new ActionType[]{
        EXECUTE,
        CANCEL,
        STOP,
    };

    private static final String[] stringTable = new String[]{
        "execute",
        "cancel",
        "stop",
    };

    private int value;

    /**
     * Construct a new enumeration value with the given integer value.
     */
    protected ActionType(int value) {
        this.value = value;
    }

    /**
     * Returns this enumeration value's integer value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the string table for class ActionType
     */
    protected String[] getStringTable() {
        return stringTable;
    }

    /**
     * Returns the enumeration value table for class ActionType
     */
    protected ActionType[] getEnumValueTable() {
        return enumValueTable;
    }

    /**
     * Return an object of the specified value.
     *
     * @param value a designator for the object.
     */
    public static ActionType getActionType(int value) {
        return enumValueTable[value];
    }

    /**
     * Return the string name of this ActionType or the integer value if
     * outside the bounds of the table
     */
    public String toString() {
        return (value >= 0 && value <= 2) ? stringTable[value] : String.valueOf(value);
    }

    /**
     * Returns the lowest integer value used by this enumeration value's
     * enumeration class.
     *
     * @return the offset of the lowest enumeration value.
     */
    protected int getOffset() {
        return 0;
    }
}