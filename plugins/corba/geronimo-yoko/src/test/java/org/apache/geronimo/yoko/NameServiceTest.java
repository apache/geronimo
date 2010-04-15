/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.apache.geronimo.yoko;

import java.util.Properties;

import org.apache.geronimo.corba.NameService;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/**
 * @version $Revision: 452600 $ $Date: 2006-10-03 12:29:42 -0700 (Tue, 03 Oct 2006) $
 */
public class NameServiceTest extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(NameServiceTest.class);
    private ORB orb;
    private NameService nameService;

    protected void setUp() throws Exception {
        // before we do anything make sure the sun orb is present
        try {
            getClass().getClassLoader().loadClass("org.apache.yoko.orb.CosNaming.tnaming.TransientNameService");
        } catch (ClassNotFoundException e) {
            log.info("Yoko orb is not present in this vm, so this test can't run");
            return;
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        ServerInfo serverInfo = new BasicServerInfo(tmpDir);
        Bundle bundle = new MockBundle(getClass().getClassLoader(), "", 0);
        ORBConfigAdapter adapter = new ORBConfigAdapter(bundle);
        // make sure all system properties are initialized.
        nameService = new NameService(serverInfo, adapter, "localhost", 8050);
        nameService.doStart();

        // create the ORB
        Properties properties = new Properties();
        String[] initArgs = { "-ORBInitRef", "NameService=" + nameService.getURI() };
        orb = ORB.init(initArgs, properties);
    }

    protected void tearDown() throws Exception {
        if (nameService == null) {
            return;
        }
        orb.destroy();
        nameService.doStop();
    }

    public void testOrb() throws Exception {
        if (nameService == null) {
            return;
        }

        NamingContextExt ctx = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
        NamingContextExt rootNamingContext = ctx;
        NameComponent name[] = ctx.to_name("foo/bar/baz");
        for (int i = 0; i < name.length; i++) {
            NameComponent nameComponent = name[i];
            ctx = NamingContextExtHelper.narrow(ctx.bind_new_context(new NameComponent[] {nameComponent}));
        }
        ctx.rebind(ctx.to_name("plan"), rootNamingContext);
    }
}
