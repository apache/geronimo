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
 * Defines enumerated values for the available deployment commands.
 *
 * @version $Rev$ $Date$
 */
public class CommandType {
    /**
     * The DeploymentManger action operation being processed is distribute.
     */
    public static final CommandType DISTRIBUTE = new CommandType(0);
    /**
     * The DeploymentManger action operation being processed is start.
     */
    public static final CommandType START = new CommandType(1);
    /**
     * The DeploymentManger action operation being processed is stop.
     */
    public static final CommandType STOP = new CommandType(2);
    /**
     * The DeploymentManger action operation being processed is undeploy.
     */
    public static final CommandType UNDEPLOY = new CommandType(3);
    /**
     * he DeploymentManger action operation being processed is redeploy.
     */
    public static final CommandType REDEPLOY = new CommandType(4);

    private static final CommandType[] enumValueTable = new CommandType[]{
        DISTRIBUTE,
        START,
        STOP,
        UNDEPLOY,
        REDEPLOY,
    };

    private static final String[] stringTable = new String[]{
        "distribute",
        "start",
        "stop",
        "undeploy",
        "redeploy",
    };

    private int value;

    /**
     * Construct a new enumeration value with the given integer value.
     */
    protected CommandType(int value) {
        this.value = value;
    }

    /**
     * Returns this enumeration value's integer value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the string table for class CommandType
     */
    protected String[] getStringTable() {
        return stringTable;
    }

    /**
     * Returns the enumeration value table for class CommandType
     */
    protected CommandType[] getEnumValueTable() {
        return enumValueTable;
    }

    /**
     * Return an object of the specified value.
     *
     * @param value a designator for the object.
     */
    public static CommandType getCommandType(int value) {
        return enumValueTable[value];
    }

    /**
     * Return the string name of this CommandType or the integer value if
     * outside the bounds of the table
     */
    public String toString() {
        return (value >= 0 && value <= 4) ? stringTable[value] : String.valueOf(value);
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
