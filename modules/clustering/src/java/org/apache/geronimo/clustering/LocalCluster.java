/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.clustering;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;

/**
 * An initial Cluster impl, which only clusters within a single
 * VM. Thus development on Clustering can start before an inter-vm
 * transport layer has been put in place...
 *
 * @version $Revision: 1.6 $ $Date: 2003/12/30 21:16:03 $
 */
public class LocalCluster
        extends AbstractCluster
        implements MetaDataListener, DataListener, DataDeltaListener {
    protected Log log = LogFactory.getLog(LocalCluster.class);

    // LocalCluster

    protected LocalChannel channel;

    public List getMembers() {
        return channel.getMembers();
    }

    public void join() {
        channel.join(this);
    }

    public void leave() {
        channel.leave(this);
    }

    public void doStart() {
        log = LogFactory.getLog(getClass().getName() + "#" + getName() + "/" + getNode());
        log.info("starting");
        channel = LocalChannel.find(getName());
        synchronized (channel) {
            setData(channel.getData());
            join();
        }
    }

    public void doStop() {
        log.info("stopping");
        leave();
    }

    public void doFail() {
        log.info("failing");
        leave();			// ??
    }

    // MetaDataListener
    public void setMetaData(List members) {
        log.info("membership changed: " + members);
    }

    // DataListener
    protected Data _data;

    // TODO - should probably return byte[] - needs renaming
    public Data getData() {
        return _data;
    }

    // TODO - should probably expect byte[] - needs renaming
    public void
            setData(Data data) {
        String xtra = "we must be the first node up";

        if (data != null) {
            xtra = "we are joining an extant cluster";
            _data = data;
        } else {
            _data = new Data();
        }

        log.debug("initialising data - " + xtra);
    }

    // DataDeltaListener
    public void applyDataDelta(DataDelta delta) {
        log.trace("applying data delta - " + delta);
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() {
        GeronimoMBeanInfo mbeanInfo = AbstractCluster.getGeronimoMBeanInfo();
        mbeanInfo.setTargetClass(LocalCluster.class);
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Members", true, false, "List of cluster members"));
        return mbeanInfo;
    }
}
