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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO - should this just inherit from HashMap ?

/**
 * An object capable of holding the state of multiple applications and
 * tiers. This will be a Map of tiers (web, ejb, ...). Each tier may
 * choose how to organise it's own Map. e.g. Web tier will use a Map
 * of webapps. Each webapp being represented by a Map of
 * HttpSessionID:HttpSession. Assuming that all IDs where GUIDs, it
 * would be posible to collapse all webapps together (and maybe all
 * tiers), thus avoiding dehashing overhead with each lookup coming
 * over the Cluster. I've considered this and decided that the extra
 * partitioning of the data that I am doing, will result in much less
 * contention on the Map used (particularly upon e.g. session
 * creation), furthermore, by retaining references into the
 * above-described structure, and distributing e.g. deltas across
 * e.g. webapp specific Channels, this dehashing overhead could be
 * avoided.<p> By using a Map of tiers, we avoid closing the set of
 * clusterable services, but incur a little more synchronisation
 * overhead on lookups, consider...
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:21 $
 */
public class
  Data
{
  protected static  Log _log=LogFactory.getLog(Data.class);
  protected         Map _tiers=new HashMap();

  public Map getTiers(){return _tiers;};

  public String toString(){return _tiers.toString();}
}
