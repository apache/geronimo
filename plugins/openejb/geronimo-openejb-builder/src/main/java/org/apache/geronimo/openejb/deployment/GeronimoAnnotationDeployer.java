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


package org.apache.geronimo.openejb.deployment;

import org.apache.openejb.config.AnnotationDeployer;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoAnnotationDeployer extends AnnotationDeployer {

    public GeronimoAnnotationDeployer() {
        super();
//        super(new DiscoverAnnotatedBeans(), new EnvEntriesPropertiesDeployer(), new GeronimoProcessAnnotatedBeans());
    }

//    public static class GeronimoProcessAnnotatedBeans extends ProcessAnnotatedBeans {
//
//        @Override
//        public void buildAnnotatedRefs(JndiConsumer consumer, ClassFinder classFinder, ClassLoader classLoader) throws OpenEJBException {
//        //do nothing, we handle this later
//        }
//
//        //TODO we may need to override the web service client handler processing too.
//    }
}
