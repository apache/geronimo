/**
 *
 * Copyright 2004 The Apache Software Foundation
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
 * about changes to Cluster state.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:56:55 $
 */
public interface
  DataDeltaListener
{
  /**
   * Called by Cluster to notify node of a change to Cluster state.
   *
   * @param delta a <code>DataDelta</code> to be applied to the node's
   * current state.
   */
  public void applyDataDelta(DataDelta delta);
}
