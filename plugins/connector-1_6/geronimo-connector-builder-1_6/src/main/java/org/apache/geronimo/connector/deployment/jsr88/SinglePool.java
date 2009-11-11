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
package org.apache.geronimo.connector.deployment.jsr88;

import org.apache.geronimo.deployment.dconfigbean.XmlBeanSupport;
import org.apache.geronimo.xbeans.connector.GerSinglepoolType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * Settings for connectionmanager/single-pool
 *
 * @version $Rev$ $Date$
 */
public class SinglePool extends XmlBeanSupport {
    public SinglePool() {
        super(null);
    }
    public SinglePool(GerSinglepoolType pool) {
        super(null);
        configure(pool);
    }

    protected GerSinglepoolType getSinglePool() {
        return (GerSinglepoolType) getXmlObject();
    }

    protected void configure(GerSinglepoolType pool) {
        setXmlObject(pool);
        if(!isSelectOneAssumeMatch() && !isMatchOne() && !isMatchAll()) {
            setMatchOne(true);
        }
    }

    // ----------------------- JavaBean Properties for single-pool ----------------------

    public Integer getMinSize() {
        return getSinglePool().isSetMinSize() ? new Integer(getSinglePool().getMinSize()) : null;
    }

    public void setMinSize(Integer value) {
        Integer old = getMinSize();
        if(value == null) {
            if(getSinglePool().isSetMinSize()) {
                getSinglePool().unsetMinSize();
            }
        } else {
            getSinglePool().setMinSize(value.intValue());
        }
        pcs.firePropertyChange("minSize", old, value);
    }

    public Integer getMaxSize() {
        return getSinglePool().isSetMaxSize() ? new Integer(getSinglePool().getMaxSize()) : null;
    }

    public void setMaxSize(Integer value) {
        Integer old = getMaxSize();
        if(value == null) {
            if(getSinglePool().isSetMaxSize()) {
                getSinglePool().unsetMaxSize();
            }
        } else {
            getSinglePool().setMaxSize(value.intValue());
        }
        pcs.firePropertyChange("maxSize", old, value);
    }

    public Integer getBlockingTimeoutMillis() {
        return getSinglePool().isSetBlockingTimeoutMilliseconds() ? new Integer(getSinglePool().getBlockingTimeoutMilliseconds()) : null;
    }

    public void setBlockingTimeoutMillis(Integer value) {
        Integer old = getBlockingTimeoutMillis();
        if(value == null) {
            if(getSinglePool().isSetBlockingTimeoutMilliseconds()) {
                getSinglePool().unsetBlockingTimeoutMilliseconds();
            }
        } else {
            getSinglePool().setBlockingTimeoutMilliseconds(value.intValue());
        }
        pcs.firePropertyChange("blockingTimeoutMillis", old, value);
    }

    public Integer getIdleTimeoutMinutes() {
        return getSinglePool().isSetIdleTimeoutMinutes() ? new Integer(getSinglePool().getIdleTimeoutMinutes()) : null;
    }

    public void setIdleTimeoutMinutes(Integer value) {
        Integer old = getIdleTimeoutMinutes();
        if(value == null) {
            if(getSinglePool().isSetIdleTimeoutMinutes()) {
                getSinglePool().unsetIdleTimeoutMinutes();
            }
        } else {
            getSinglePool().setIdleTimeoutMinutes(value.intValue());
        }
        pcs.firePropertyChange("idleTimeoutMinutes", old, value);
    }

    public boolean isMatchAll() {
        return getSinglePool().isSetMatchAll();
    }

    public void setMatchAll(boolean set) {
        if(set) {
            if(!isMatchAll()) {
                getSinglePool().addNewMatchAll();
                pcs.firePropertyChange("matchAll", !set, set);
            }
            if(isMatchOne()) setMatchOne(false);
            if(isSelectOneAssumeMatch()) setSelectOneAssumeMatch(false);
        } else {
            if(isMatchAll()) {
                getSinglePool().unsetMatchAll();
                pcs.firePropertyChange("matchAll", !set, set);
            }
        }
    }

    public boolean isMatchOne() {
        return getSinglePool().isSetMatchOne();
    }

    public void setMatchOne(boolean set) {
        if(set) {
            if(!isMatchOne()) {
                getSinglePool().addNewMatchOne();
                pcs.firePropertyChange("matchOne", !set, set);
            }
            if(isMatchAll()) setMatchAll(false);
            if(isSelectOneAssumeMatch()) setSelectOneAssumeMatch(false);
        } else {
            if(isMatchOne()) {
                getSinglePool().unsetMatchOne();
                pcs.firePropertyChange("matchOne", !set, set);
            }
        }
    }

    public boolean isSelectOneAssumeMatch() {
        return getSinglePool().isSetSelectOneAssumeMatch();
    }

    public void setSelectOneAssumeMatch(boolean set) {
        if(set) {
            if(!isSelectOneAssumeMatch()) {
                getSinglePool().addNewSelectOneAssumeMatch();
                pcs.firePropertyChange("selectOneAssumeMatch", !set, set);
            }
            if(isMatchAll()) setMatchAll(false);
            if(isMatchOne()) setMatchOne(false);
        } else {
            if(isSelectOneAssumeMatch()) {
                getSinglePool().unsetSelectOneAssumeMatch();
                pcs.firePropertyChange("selectOneAssumeMatch", !set, set);
            }
        }
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
