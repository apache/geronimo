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

package org.apache.geronimo.jms.test.mdb.to;

import java.util.Date;

/**
 * @author <a href="mailto:tomasz[et]mazan[dot]pl">Tomasz Mazan</a>
 */
public class SimpleTransferObject extends TransferObject {

    /**
     * serialization version identifier
     */
    private static final long serialVersionUID = -1054412046957557066L;

    /**
     * Name
     */
    private String name;

    /**
     * id
     */
    private int id;

    /**
     * Processing flag
     */
    private Boolean processed;

    /**
     * Create's date
     */
    private Date created;

    /**
     * Default constructor
     *
     * @param name name of object
     * @param id id of the object
     */
    public SimpleTransferObject(String name, int id) {
        this.name = name;
        this.id = id;
        this.created = new Date();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the processed
     */
    public Boolean getProcessed() {
        return processed;
    }

    /**
     */
    public void markProcessed() {
        this.processed = true;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }


}
