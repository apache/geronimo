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

package org.apache.geronimo.transaction.log;

import javax.transaction.xa.Xid;

import junit.framework.TestCase;
import org.apache.geronimo.transaction.manager.XidImpl;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/06 04:00:51 $
 *
 * */
public class XidSpeedTest extends TestCase {

    public void testBufferTransferSpeed() throws Exception {
        HOWLLog log = new HOWLLog();
        Xid xid = new XidImpl(new byte[Xid.MAXGTRIDSIZE]);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            log.getBytes(xid, HOWLLog.PREPARE);
        }
        long end = System.currentTimeMillis();
        System.err.println("millis for 1M getBytes impl1: " + (end - start));
    }

    public void testBufferTransferSpeed2() throws Exception {
        XidImpl2 xid = new XidImpl2(new byte[Xid.MAXGTRIDSIZE]);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            xid.getBuffer(HOWLLog.PREPARE);
        }
        long end = System.currentTimeMillis();
        System.err.println("millis for 1M getBytes impl2: " + (end - start));

    }


    public void testXidImplCreationSpeed() throws Exception {
        Xid xid = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            xid = new XidImpl(new byte[Xid.MAXGTRIDSIZE]);
        }
        long end = System.currentTimeMillis();
        System.err.println("millis for 1M create global xid impl1: " + (end - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            xid = new XidImpl(xid, new byte[Xid.MAXGTRIDSIZE]);
        }
        end = System.currentTimeMillis();
        System.err.println("millis for 1M create branch xid impl1: " + (end - start));

    }

    public void testXidImpl2CreationSpeed() throws Exception {
        Xid xid = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            xid = new XidImpl2(new byte[Xid.MAXGTRIDSIZE]);
        }
        long end = System.currentTimeMillis();
        System.err.println("millis for 1M create global xid impl2: " + (end - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            xid = new XidImpl2(xid, new byte[Xid.MAXGTRIDSIZE]);
        }
        end = System.currentTimeMillis();
        System.err.println("millis for 1M create branch xid impl2: " + (end - start));

    }
}
