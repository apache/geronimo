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
package org.apache.geronimo.cli.client;

import org.apache.geronimo.cli.AbstractCLI;
import org.apache.geronimo.cli.CLParser;
import org.apache.geronimo.kernel.util.MainConfigurationBootstrapper;


/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class ClientCLI extends AbstractCLI {

    public static void main(String[] args) {
        int status = new ClientCLI(args).executeMain();
        System.exit(status);
    }

    protected ClientCLI(String[] args) {
        super(args, System.err);
    }
    
    @Override
    protected CLParser getCLParser() {
        return new ClientCLParser(System.out);
    }

    @Override
    protected MainConfigurationBootstrapper newMainConfigurationBootstrapper() {
        return new MainConfigurationBootstrapper();
    }

}
