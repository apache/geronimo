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

package org.apache.geronimo.common.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * A mock MBean for testing.
 *
 * @jmx:mbean
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:04 $
 */
public class MockObject //implements MockObjectMBean
{
//
//    protected String string = "MyString";
//    protected boolean something;
//
//    /**
//     * @jmx:managed-attribute
//     */
//    public void setString(String value)
//    {
//        this.string = value;
//    }
//
//    /**
//     * @jmx:managed-attribute
//     */
//    public String getString()
//    {
//        return string;
//    }
//
//    /**
//     * @jmx:managed-attribute
//     */
//    public void setSomething(boolean flag)
//    {
//        something = flag;
//    }
//
//    /**
//     * @jmx:managed-attribute
//     */
//    public boolean isSomething()
//    {
//        return something;
//    }
//
//    /**
//     * @jmx:managed-operation
//     */
//    public String doIt()
//    {
//        return "done";
//    }
//
//    /**
//     * @jmx:managed-operation
//     */
//    public String setPoorlyNameOperation()
//    {
//        return "bad";
//    }
//
//    /**
//     * @jmx:managed-operation
//     */
//    public String someOperation()
//    {
//        return "someop";
//    }
//
//    /**
//     * @jmx:managed-operation
//     */
//    public String someOperation(Object arg)
//    {
//        return "someop" + arg;
//    }
//
//    /**
//     * @jmx:managed-operation
//     */
//    public String someOperation(boolean arg)
//    {
//        return "somebooleanop" + arg;
//    }
}
