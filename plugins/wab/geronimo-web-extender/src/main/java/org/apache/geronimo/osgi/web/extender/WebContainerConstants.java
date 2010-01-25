/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.osgi.web.extender;

public interface WebContainerConstants {

    public static final String TOPIC_DEPLOYING = "org/osgi/service/web/DEPLOYING";
    public static final String TOPIC_DEPLOYED = "org/osgi/service/web/DEPLOYED";
    public static final String TOPIC_UNDEPLOYING = "org/osgi/service/web/UNDEPLOYING";
    public static final String TOPIC_UNDEPLOYED = "org/osgi/service/web/UNDEPLOYED";
    public static final String TOPIC_FAILED = "org/osgi/service/web/FAILED";    
    
    /**
     * The Blueprint extender bundle that is generating this event. This
     * property is of type <code>Bundle</code>.
     */
    public static final String EXTENDER_BUNDLE = "extender.bundle";

    /**
     * The bundle id of the Blueprint extender bundle that is generating this
     * event. This property is of type <code>Long</code>.
     */
    public static final String EXTENDER_BUNDLE_ID = "extender.bundle.id";

    /**
     * The bundle symbolic of the Blueprint extender bundle that is generating
     * this event. This property is of type <code>String</code>.
     */
    public static final String EXTENDER_BUNDLE_SYMBOLICNAME = "extender.bundle.symbolicName";

    /**
     * The bundle version of the Blueprint extender bundle that is generating
     * this event. This property is of type <code>Version</code>.
     */
    public static final String EXTENDER_BUNDLE_VERSION = "extender.bundle.version";

    
    public static final String CONTEXT_PATH = "context.path";
    
    public static final String COLLISION = "collision";
    
    public static final String COLLISION_BUNDLES = "collision.bundles";
}

