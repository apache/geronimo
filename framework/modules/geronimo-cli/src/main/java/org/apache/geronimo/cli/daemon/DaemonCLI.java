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
package org.apache.geronimo.cli.daemon;

import java.util.Arrays;

import org.apache.geronimo.cli.AbstractCLI;
import org.apache.geronimo.cli.CLParser;
import org.apache.geronimo.main.Bootstrapper;


/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class DaemonCLI extends AbstractCLI {
    
    public static void main(String[] args) {
        int status = new DaemonCLI(args).executeMain();
        System.exit(status);
    }

    protected DaemonCLI(String[] args) {
        super(args, System.err);
    }

    @Override
    protected CLParser getCLParser() {
        return new DaemonCLParser(System.out);
    }

    @Override
    protected Bootstrapper createBootstrapper(CLParser parser) {
        Bootstrapper boot = super.createBootstrapper(parser);
        boot.setWaitForStop(true);
        boot.setStartBundles(Arrays.asList("org.apache.geronimo.framework/j2ee-system//car"));
        boot.setLog4jConfigFile("var/log/server-log4j.properties");
        boot.setCleanStorage( ((DaemonCLParser) parser).isCleanCache() );
        return boot;
    }
}
