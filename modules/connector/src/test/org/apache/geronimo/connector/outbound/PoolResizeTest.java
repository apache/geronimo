/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.connector.outbound;

import junit.framework.TestCase;

/**
 * @version $Rev:  $ $Date$
 */
public class PoolResizeTest extends TestCase {

    private final int oldCheckedOut = 20;
    private final int oldConnectionCount = 40;
    private final int oldPermitsAvailable = oldConnectionCount - oldCheckedOut;
    private final int oldMaxSize = 60;

    public void testOne() throws Exception {
        int oldMinSize = 5;
        int newMaxSize = 10;
        AbstractSinglePoolConnectionInterceptor.ResizeInfo resizeInfo = new AbstractSinglePoolConnectionInterceptor.ResizeInfo(oldMinSize, oldPermitsAvailable, oldConnectionCount, newMaxSize);
        assertEquals("wrong shrinkLater", 10, resizeInfo.getShrinkLater());
        assertEquals("wrong shrinkNow", 20, resizeInfo.getShrinkNow());
        assertEquals("wrong newMinSize", 5, resizeInfo.getNewMinSize());
        assertEquals("wrong transferCheckedOut", 10, resizeInfo.getTransferCheckedOut());
    }

    public void testTwo() throws Exception {
        int oldMinSize = 5;
        int newMaxSize = 30;
        AbstractSinglePoolConnectionInterceptor.ResizeInfo resizeInfo = new AbstractSinglePoolConnectionInterceptor.ResizeInfo(oldMinSize, oldPermitsAvailable, oldConnectionCount, newMaxSize);
        assertEquals("wrong shrinkLater", 0, resizeInfo.getShrinkLater());
        assertEquals("wrong shrinkNow", 10, resizeInfo.getShrinkNow());
        assertEquals("wrong newMinSize", 5, resizeInfo.getNewMinSize());
        assertEquals("wrong transferCheckedOut", 20, resizeInfo.getTransferCheckedOut());
    }

    public void testThree() throws Exception {
        int oldMinSize = 5;
        int newMaxSize = 50;
        AbstractSinglePoolConnectionInterceptor.ResizeInfo resizeInfo = new AbstractSinglePoolConnectionInterceptor.ResizeInfo(oldMinSize, oldPermitsAvailable, oldConnectionCount, newMaxSize);
        assertEquals("wrong shrinkLater", 00, resizeInfo.getShrinkLater());
        assertEquals("wrong shrinkNow", 0, resizeInfo.getShrinkNow());
        assertEquals("wrong newMinSize", 5, resizeInfo.getNewMinSize());
        assertEquals("wrong transferCheckedOut", 20, resizeInfo.getTransferCheckedOut());
    }

    public void testFour() throws Exception {
        int oldMinSize = 5;
        int newMaxSize = 70;
        AbstractSinglePoolConnectionInterceptor.ResizeInfo resizeInfo = new AbstractSinglePoolConnectionInterceptor.ResizeInfo(oldMinSize, oldPermitsAvailable, oldConnectionCount, newMaxSize);
        assertEquals("wrong shrinkLater", 0, resizeInfo.getShrinkLater());
        assertEquals("wrong shrinkNow", 0, resizeInfo.getShrinkNow());
        assertEquals("wrong newMinSize", 5, resizeInfo.getNewMinSize());
        assertEquals("wrong transferCheckedOut", 20, resizeInfo.getTransferCheckedOut());
    }


}
