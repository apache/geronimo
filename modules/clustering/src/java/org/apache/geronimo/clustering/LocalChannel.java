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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A uniquely identifiable n->n intra-vm event-raising communications channel...
 *
 * @version $Revision: 1.2 $ $Date: 2003/12/30 14:54:38 $
 */
public class
  LocalChannel
{
  // class
  protected static Log _log=LogFactory.getLog(LocalChannel.class);
  protected static Map _map=new HashMap();

  public static LocalChannel
    find(String name)
  {
    synchronized (_map)
    {
      LocalChannel channel=(LocalChannel)_map.get(name);

      if (channel==null)
      {
	channel=new LocalChannel(name);
	_map.put(name, channel);

	_log.trace("created channel: "+name);
      }
      else
      {
	_log.trace("found channel: "+name);
      }

      return channel;
    }
  }

  // instance
  protected String _name;
  protected List   _members=new Vector();

  protected LocalChannel(String name) {_name=name;}

  public List getMembers(){synchronized (_members){return Collections.unmodifiableList(_members);}}

  // MetaData

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

    // last one out could turn out the lights...
  }

  // Data

  public synchronized Object
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
}
