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
 * An interface implemented by components that wish to have their
 * state initialised from the Cluster.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:56:55 $
 */
public interface
  DataListener
{
  /**
   * Called by Cluster to retrieve current Cluster state.
   *
   */
  public Data getData();
  /**
   * Called by Cluster to initialise the state of a [new] node.
   *
   * @param state <code>Data</code> to be used as the node's initial
   * state.
   */
  public void setData(Data state);
}
