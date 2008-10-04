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
package org.apache.geronimo.console.jmsmanager;

import java.io.Serializable;

/**
 * @version $Rev$ $Date$
 */
public class JMSMessageInfo implements Serializable{
    private String adminObjName;
    private String adminObjType;
    private String physicalName;
    private String adapterObjectName;
    private String correlationId;
    private boolean isPersistent;
    private String replyTo;
    private int priority;
    private String jmsType;
    private String message;
    private String messageID;
    private long timeStamp;
    private String destination;
    private long expiration;
    
    
    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageId(String messageID) {
        this.messageID = messageID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
   }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAdapterObjectName() {
        return adapterObjectName;
    }

    public void setAdapterObjectName(String adapterObjectName) {
        this.adapterObjectName = adapterObjectName;
    }

    public String getAdminObjName() {
        return adminObjName;
    }

    public void setAdminObjName(String adminObjName) {
        this.adminObjName = adminObjName;
    }

    public String getAdminObjType() {
        return adminObjType;
    }

    public void setAdminObjType(String adminObjType) {
        this.adminObjType = adminObjType;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public void setPersistent(boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public String getJmsType() {
        return jmsType;
    }

    public void setJmsType(String jmsType) {
        this.jmsType = jmsType;
    }

    public String getPhysicalName() {
        return physicalName;
    }

    public void setPhysicalName(String physicalName) {
        this.physicalName = physicalName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
}
