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
 * Defines enumeration values for the various states of a deployment action.
 *
 * @version $Rev$ $Date$
 */
public class StateType {
    /**
     * The action operation is running normally.
     */
    public static final StateType RUNNING = new StateType(0);
    /**
     * The action operation has completed normally.
     */
    public static final StateType COMPLETED = new StateType(1);
    /**
     * The action operation has failed.
     */
    public static final StateType FAILED = new StateType(2);
    /**
     * The DeploymentManager is running in disconnected mode.
     */
    public static final StateType RELEASED = new StateType(3);

    private static final StateType[] enumValueTable = {
        RUNNING,
        COMPLETED,
        FAILED,
        RELEASED,
    };

    private static final String[] stringTable = {
        "running",
        "completed",
        "failed",
        "released",
    };

    private int value;

    /**
     * Construct a new enumeration value with the given integer value.
     */
    protected StateType(int value) {
        this.value = value;
    }

    /**
     * Returns this enumeration value's integer value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the string table for class StateType
     */
    protected String[] getStringTable() {
        return stringTable;
    }

    /**
     * Returns the enumeration value table for class StateType
     */
    protected StateType[] getEnumValueTable() {
        return enumValueTable;
    }

    /**
     * Return an object of the specified value.
     *
     * @param value a designator for the object.
     */
    public static StateType getStateType(int value) {
        return enumValueTable[value];
    }

    /**
     * Return the string name of this StateType or the integer value if
     * outside the bounds of the table
     */
    public String toString() {
        return (value >= 0 && value <= 3) ? stringTable[value] : String.valueOf(value);
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