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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.shared;

/**
 * Class ActionTypes defines enumeration values for the J2EE DeploymentStatus
 * actions.
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:50 $
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