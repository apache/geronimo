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

import java.util.List;

/**
 * An interface implemented by components that wish to be notified
 * upon membership of a Cluster changing.
 *
 * @version $Rev$ $Date$
 */
public interface
  MetaDataListener
{
  /**
   * Called by Cluster when a change in membership occurs. This is
   * better than a memberLeft/memberJoined notification as it can
   * handle multiple concurrent leave/joins, this may occur in the
   * case of event elysion...
   *
   * @param members a <code>List</code> of members.
   */
  public void setMetaData(List members);
}
