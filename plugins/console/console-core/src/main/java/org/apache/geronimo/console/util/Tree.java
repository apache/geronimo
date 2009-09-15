/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.util;

import java.util.ArrayList;
import java.util.List;
import org.directwebremoting.annotations.DataTransferObject;

/*
 * DWR data transfer object used by debugviews portlets and openejb portlets
 */
@DataTransferObject
public class Tree {

    private String label;

    private String identifier;

    private List<TreeEntry> items;

    public Tree(String identifier, String label) {
        this.label = label;
        this.identifier = identifier;
        this.items = new ArrayList<TreeEntry>();
    }
    
    /*
     * methods
     */
    public void addItem(TreeEntry item){
        this.items.add(item);
    }
    
    /*
     * getters and setters
     */
    public List<TreeEntry> getItems() {
        return items;
    }
    
    public void setItems(List<TreeEntry> items) {
        this.items = items;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setId(String identifier) {
        this.identifier = identifier;
    }
}
