package org.apache.geronimo.console.navigation;

import java.util.Comparator;

/*
 * NodeId is in format like this: 1-2-3
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
