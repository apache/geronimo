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
package org.apache.geronimo.webservices.saaj;

import javax.xml.soap.MessageFactory;

import org.apache.geronimo.testsupport.TestSupport;

public class SAAJUniverseTest extends TestSupport {

    private static final String SUN_MESSAGE_CLASS =
        "com.sun.xml.messaging.saaj.soap.ver1_1.Message1_1Impl";

    private static final String AXIS1_MESSAGE_CLASS =
        "org.apache.axis.Message";

    private static final String DEFAULT_MESSAGE_CLASS = SUN_MESSAGE_CLASS;

    public void testBasic() throws Exception {
        /* TODO:  This is using a non-bundlized version of axis currently.  Disable until
           we get the axis jars sorted out

        SAAJGBean b = new SAAJGBean();
        b.doStart();

        // case 1, universe not set
        assertEquals(DEFAULT_MESSAGE_CLASS,
                     MessageFactory.newInstance().createMessage().getClass().getName());

        // case 2, default universe set
        SAAJUniverse u = new SAAJUniverse();
        u.set(SAAJUniverse.DEFAULT);
        assertEquals(DEFAULT_MESSAGE_CLASS,
                     MessageFactory.newInstance().createMessage().getClass().getName());
        u.unset();

        // case 3, Sun universe set
        u.set(SAAJUniverse.SUN);
        assertEquals(SUN_MESSAGE_CLASS,
                     MessageFactory.newInstance().createMessage().getClass().getName());
        u.unset();

        // case 4, Axis1 universe set
        u.set(SAAJUniverse.AXIS1);
        assertEquals(AXIS1_MESSAGE_CLASS,
                     MessageFactory.newInstance().createMessage().getClass().getName());
        u.unset();
        */
    }

    public void testNested() throws Exception {
        /* TODO:  This is using a non-bundlized version of axis currently.  Disable until
           we get the axis jars sorted out
        SAAJGBean b = new SAAJGBean();
        b.doStart();

        assertEquals(DEFAULT_MESSAGE_CLASS,
                     MessageFactory.newInstance().createMessage().getClass().getName());

        SAAJUniverse u = new SAAJUniverse();

        // set axis1
        u.set(SAAJUniverse.AXIS1);
        assertEquals(AXIS1_MESSAGE_CLASS,
                     MessageFactory.newInstance().createMessage().getClass().getName());

        // set sun, nested
        u.set(SAAJUniverse.SUN);
        assertEquals(SUN_MESSAGE_CLASS,
                     MessageFactory.newInstance().createMessage().getClass().getName());

        // unset sun
        u.unset();

        // should be axis
        assertEquals(AXIS1_MESSAGE_CLASS,
                     MessageFactory.newInstance().createMessage().getClass().getName());

        u.unset();

        assertEquals(DEFAULT_MESSAGE_CLASS,
                     MessageFactory.newInstance().createMessage().getClass().getName());
        */
    }

}
