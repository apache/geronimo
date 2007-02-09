/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/


package org.apache.geronimo.corba;

import junit.framework.TestCase;

/**
 *
 * Make sure various GBeanInfos have correct constructor info
 * @version $Rev: 465108 $ $Date: 2006-10-17 17:23:40 -0700 (Tue, 17 Oct 2006) $
 */
public class GBeanInfosTest extends TestCase {

    public void testCSSBeanGBean() throws Exception {
        new CSSBeanGBean();
    }

    public void testCORBABeanGBean() throws Exception {
        new CORBABeanGBean();
    }

    public void testTSSBeanGBean() throws Exception {
        new TSSBeanGBean();
    }

    public void testTSSLink() throws Exception {
        new TSSLink();
    }

    public void testNameServiceGBean() throws Exception {
        new NameServiceGBean();
    }

}
