/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.clustering;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A uniquely identifiable n->n intra-vm event-raising communications
 * channel. A number of nodes which are part of the same cluster and
 * reside in the same VM should share a single Cluster object.
 *
 * @version $Revision: 1.15 $ $Date: 2004/03/10 09:58:21 $
 */
public class
  LocalCluster
  extends Cluster
{
  protected Log  _log=LogFactory.getLog(LocalCluster.class);
  protected List _members=new Vector();

  public List getMembers(){synchronized (_members){return Collections.unmodifiableList(_members);}}

  // MetaData

  /**
   * Notify interested Cluster members of a change in membership,
   * including the node which generated it.
   *
   * @param members a <code>List</code> value
   */
  protected void
    notifyMembershipChanged(List members)
  {
    for (Iterator i=members.iterator(); i.hasNext();)
      try
      {
	Object member=i.next();
	if (member instanceof MetaDataListener)
	  ((MetaDataListener)member).setMetaData(members);
      }
      catch (Exception e)
      {
	_log.warn("problem notifying membership changed", e);
      }
  }

  public void
    join(Object member)
  {
    // first one in could turn on the lights...
    synchronized (_members)
    {
      _members.add(member);
      notifyMembershipChanged(_members);
    }
  }

  public void
    leave(Object member)
  {
    synchronized (_members)
    {
      _members.remove(member);
      notifyMembershipChanged(_members);
    }

    // last one out could turn off the lights...
  }

  // Data

  /**
   * Get the Cluster's Data - uses an election policy (currently
   * hardwired) to decide which node to get it from.
   *
   * @return a <code>Data</code> value - The data
   */
  public synchronized Data
    getData()
  {
    // TODO - we need a pluggable election policy to decide who will
    // be asked for state...

    synchronized (_members)
    {
      if (_members.isEmpty())
	return null;
      else
      {
	for (Iterator i=_members.iterator(); i.hasNext();)
	{
	  Object member=i.next();
	  // TODO - we need to do a deep copy of the state here -
	  // serialise and deserialise...
	  if (member instanceof DataListener)
	    return ((DataListener)member).getData();
	}
	return null;
      }
    }
  }

  /**
   * Apply the given delta to all interested members of the cluster,
   * excluding the member which generated it.
   *
   * @param l a <code>DataDeltaListener</code> value - The node that generated the delta
   * @param delta a <code>DataDelta</code> value - The delta
   */
  public void
    notifyDataDelta(DataDeltaListener l, DataDelta delta)
  {
    synchronized (_members)
    {
      for (Iterator i=_members.iterator(); i.hasNext();)
      {
	Object member=i.next();
	if (member != l && member instanceof DataDeltaListener)
	  ((DataDeltaListener)member).applyDataDelta(delta);
      }
    }
  }
  /*
  public static GeronimoMBeanInfo
    getGeronimoMBeanInfo()
  {
    GeronimoMBeanInfo mbeanInfo=Cluster.getGeronimoMBeanInfo();
    mbeanInfo.setTargetClass(LocalCluster.class);
    return mbeanInfo;
  }
  */
}
