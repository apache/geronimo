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
 * Class ModuleTypes defines enumeration values for the J2EE module types.
 *
 * @version $Rev$ $Date$
 */
public class ModuleType {
    /**
     * The module is an EAR archive.
     */
    public static final ModuleType EAR = new ModuleType(0);
    /**
     * The module is an Enterprise Java Bean archive.
     */
    public static final ModuleType EJB = new ModuleType(1);
    /**
     * The module is an Client Application archive.
     */
    public static final ModuleType CAR = new ModuleType(2);
    /**
     * The module is an Connector archive.
     */
    public static final ModuleType RAR = new ModuleType(3);
    /**
     * The module is an Web Application archive.
     */
    public static final ModuleType WAR = new ModuleType(4);

    private static final ModuleType[] enumValueTable = {
        EAR,
        EJB,
        CAR,
        RAR,
        WAR,
    };

    private static final String[] stringTable = {
        "ear",
        "ejb",
        "car",
        "rar",
        "war",
    };

    private static final String[] moduleExtensionTable = {
        ".ear",
        ".jar",
        ".jar",
        ".rar",
        ".war",
    };

    private int value;

    /**
     * Construct a new enumeration value with the given integer value.
     */
    protected ModuleType(int value) {
        this.value = value;
    }

    /**
     * Returns this enumeration value's integer value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the string table for class ModuleType
     */
    protected String[] getStringTable() {
        return stringTable;
    }

    /**
     * Returns the enumeration value table for class ModuleType
     */
    protected ModuleType[] getEnumValueTable() {
        return enumValueTable;
    }

    /**
     * Return the file extension string for this enumeration.
     */
    public String getModuleExtension() {
        return moduleExtensionTable[value];
    }

    /**
     * Return an object of the specified value.
     *
     * @param value a designator for the object.
     */
    public static ModuleType getModuleType(int value) {
        return enumValueTable[value];
    }

    /**
     * Return the string name of this ModuleType or the integer value if
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