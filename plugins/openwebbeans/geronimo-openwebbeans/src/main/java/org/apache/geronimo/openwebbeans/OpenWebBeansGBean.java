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


package org.apache.geronimo.openwebbeans;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;


/**
 * For help on using gbean annotations see:
 * http://cwiki.apache.org/GMOxDOC22/gbean-annotations.html
 *  
 * @version $Rev: 698441 $ $Date: 2008-09-24 00:10:08 -0700 (Wed, 24 Sep 2008) $
 */
@GBean
public class OpenWebBeansGBean implements GBeanLifecycle {
    

    public OpenWebBeansGBean() {
    }


    public void doStart() {
    }

    public void doStop() {
    }

    public void doFail() {
        doStop();
    }

}
