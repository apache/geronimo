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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Vector;
import java.util.List;
import java.util.Collections;

/**
 * An initial Cluster impl, which only clusters within a single
 * VM. Thus development on Clustering can start before an inter-vm
 * transport layer has been put in place...
 *
 * @jmx:mbean extends="org.apache.geronimo.clustering.AbstractClusterMBean"
 * @version $Revision: 1.5 $ $Date: 2003/12/30 15:32:20 $
 */
public class
  LocalCluster
  extends AbstractCluster
  implements LocalClusterMBean, MetaDataListener, DataListener, DataDeltaListener
{
  protected Log _log=LogFactory.getLog(LocalCluster.class);

  // LocalCluster

  protected LocalChannel _channel;

  /**
   * @jmx.managed-attribute
   */
  public List getMembers(){return _channel.getMembers();}

  public void join()  {_channel.join(this);}
  public void leave() {_channel.leave(this);}

  // StateManageable
  public boolean canStart() {return true;}
  public boolean canStop()  {return true;}

  public void
    doStart()
  {
    _log=LogFactory.getLog(getClass().getName()+"#"+getName()+"/"+getNode());
    _log.info("starting");
    _channel=LocalChannel.find(getName());
    synchronized (_channel)
    {
      setData(_channel.getData());
      join();
    }
  }

  public void
    doStop()
  {
    _log.info("stopping");
    leave();
  }

  public void
    doFail()
  {
    _log.info("failing");
    leave();			// ??
  }

  // MetaDataListener
  public void
    setMetaData(List members)
  {
    _log.info("membership changed: "+members);
  }

  // DataListener
  protected Data _data;

  // TODO - should probably return byte[] - needs renaming
  public Data getData() {return _data;}

  // TODO - should probably expect byte[] - needs renaming
  public void
    setData(Data data)
  {
    String xtra="we must be the first node up";

    if (data!=null)
    {
      xtra="we are joining an extant cluster";
      _data=data;
    }
    else
    {
      _data=new Data();
    }

    _log.debug("initialising data - "+xtra);
  }

  // DataDeltaListener
  public void
    applyDataDelta(DataDelta delta)
  {
    _log.trace("applying data delta - "+delta);
  }
}
