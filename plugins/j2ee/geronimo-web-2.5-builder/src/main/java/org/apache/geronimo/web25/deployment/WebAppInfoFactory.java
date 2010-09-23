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


package org.apache.geronimo.web25.deployment;

import org.apache.geronimo.web.info.FilterInfo;
import org.apache.geronimo.web.info.SecurityConstraintInfo;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;

/**
 * @version $Rev:$ $Date:$
 */
public interface WebAppInfoFactory {

    WebAppInfo newWebAppInfo();

    FilterInfo newFilterInfo();

    ServletInfo newServletInfo();

    void complete(WebAppInfo webAppInfo);

    ServletInfo newJspInfo(String jspFile);

    ServletInfo copy(ServletInfo servletInfo);

    FilterInfo copy(FilterInfo filterInfo);

    SecurityConstraintInfo newSecurityConstraintInfo();

    SecurityConstraintInfo copy(SecurityConstraintInfo securityConstraint);

}
