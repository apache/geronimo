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
 * Class DConfigBeanVersionTypes defines enumeration values for the J2EE
 * Platform verion number.
 *
 * @version $Rev$ $Date$
 */
public class DConfigBeanVersionType {
    /**
     * J2EE Platform version 1.3
     */
    public static final DConfigBeanVersionType V1_3 = new DConfigBeanVersionType(0);
    /**
     * J2EE Platform version 1.3.1
     */
    public static final DConfigBeanVersionType V1_3_1 = new DConfigBeanVersionType(1);
    /**
     * J2EE Platform version 1.4
     */
    public static final DConfigBeanVersionType V1_4 = new DConfigBeanVersionType(2);

    private static final DConfigBeanVersionType[] enumValueTable = {
        V1_3,
        V1_3_1,
        V1_4,
    };

    private static final String[] stringTable = {
        "V1_3",
        "V1_3_1",
        "V1_4",
    };

    private int value;

    /**
     * Construct a new enumeration value with the given integer value.
     */
    protected DConfigBeanVersionType(int value) {
        this.value = value;
    }

    /**
     * Returns this enumeration value's integer value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the string table for class DConfigBeanVersionType
     */
    protected String[] getStringTable() {
        return stringTable;
    }

    /**
     * Returns the enumeration value table for class DConfigBeanVersionType
     */
    protected DConfigBeanVersionType[] getEnumValueTable() {
        return enumValueTable;
    }

    /**
     * Return an object of the specified value.
     *
     * @param value a designator for the object.
     */
    public static DConfigBeanVersionType getDConfigBeanVersionType(int value) {
        return enumValueTable[value];
    }

    /**
     * Return the string name of this DConfigBeanVersionType or the integer
     * value if outside the bounds of the table
     */
    public String toString() {
        return (value >= 0 && value <= 2) ? getStringTable()[value] : String.valueOf(value);
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