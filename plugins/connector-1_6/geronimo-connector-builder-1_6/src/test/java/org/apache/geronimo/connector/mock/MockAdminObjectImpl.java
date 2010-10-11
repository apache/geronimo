/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.connector.mock;

import javax.validation.constraints.Pattern;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class MockAdminObjectImpl implements MockAdminObject {

    private String tweedle;

    // add a simple validation pattern to these fields to allow validation tests.  This 
    // pattern will allow any word characters as a value, but not the null string. 
    @Pattern(regexp="[\\w-]+" )
    public String getTweedle() {
        return tweedle;
    }

    public void setTweedle(String tweedle) {
        this.tweedle = tweedle;
    }

    public MockAdminObject getSomething() {
        return this;
    }
}
