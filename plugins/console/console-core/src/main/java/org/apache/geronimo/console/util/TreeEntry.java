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
import java.util.Iterator;
import java.util.List;
import org.directwebremoting.annotations.DataTransferObject;

/*
 * DWR data transfer object used by debugviews portlets and openejb portlets
 */
@DataTransferObject
public class TreeEntry {
    

    // Always Tree's label = "name"
    private String name; 
    
    // We always specify the Tree's identifier = null, because we hope the id can be auto-generated so that keeping unique
    // But in ClassLoaderViewHelper.java, we have to assign the id of node explicitly
    private String id;
        
    // type
    private String type;
    
    // values is used to record assistant information, 
    private String[] values;
    
    // children
    private List<TreeEntry> children;
    
    /*
     * constructors
     */
    public TreeEntry(){
        // Not recommended.
    } 
    
    public TreeEntry(String name){
        this(name, null, null);
    }
    
    public TreeEntry(String name, String type){
        this(name, type, null);
    }
    
    public TreeEntry(String name, String type, String[] values){
        this.name = name;
        this.type = type;
        this.values = values;
        this.children = new ArrayList<TreeEntry>();
    }

    /*
     * getters and setters
     */
    public List<TreeEntry> getChildren() {
        return children;
    }

    public void setChildren(List<TreeEntry> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getId()
    {
        return this.id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }
    
    /*
     * methods
     */
    public void addChild(TreeEntry child){
        this.children.add(child);
    }

    public TreeEntry findEntry(String name) {
        if (name == null)
            return null;
        if (this.name != null && this.name.equals(name))
            return this;
        Iterator<TreeEntry> iter = children.iterator();
        while (iter.hasNext()) {
            TreeEntry entry = iter.next().findEntry(name);
            if (entry != null)
                return entry;
        }
        return null;
    }
}
