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
package org.apache.geronimo.console.navigation;

import java.util.Comparator;

/*
 * NodeId is in format like this: 1-2-3
 *
 * @version $Rev$ $Date$
 */
class TreeNodeIdComparator implements Comparator<String>{
    
    public int compare(String idString1, String idString2) {
        
        String[] ids1 = idString1.split(TreeNode.ID_DELIMIT);
        String[] ids2 = idString2.split(TreeNode.ID_DELIMIT);

        int ids1_length = ids1.length;
        int ids2_length = ids2.length;
        
        // Sample: for 1-2-3 and 1-3, only compare 1-2 and 1-3.
        int shorterLength = ids1_length <= ids2_length ? ids1_length : ids2.length;

        for (int i = 0; i < shorterLength; i++) {

            int id1 = Integer.parseInt(ids1[i]);
            int id2 = Integer.parseInt(ids2[i]);

            if (id1 == id2) {
                continue;
            }

            if (id1 > id2) {
                return 1;
            } else {
                return -1;
            }

        }
     // Sample: 1-2 >1-2-2
        if (ids1_length < ids2_length) {
            return -1;
        }

        if (ids1_length > ids2_length) {
            return 1;
        }

        return 0;
    }
    
}
