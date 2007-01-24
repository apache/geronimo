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
package org.apache.geronimo.console.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;

public class StringTree {
    public String name = null;
    public ArrayList childs = new ArrayList();    
    public StringTree(String nm, ArrayList elements){
        name = nm;
        childs = elements;
    }
    public StringTree(String nm){
        name = nm;        
    }
    public String getName(){
        return name;
    }
    public void setName(String nm){
        name = nm;;
    }
    public ArrayList getChilds(){
        return childs;
    }
    public void setChilds(ArrayList elements){
        childs = elements;
    }
    public void addChild(Object obj){
        childs.add(obj);
    }
    public StringTree findNode(String id){
        if(id == null)return null;
        if(name != null && name.equals(id))
            return this;
        Iterator iter = childs.iterator();
        while(iter.hasNext()){
            Object obj = iter.next();
            if(obj instanceof StringTree){
                StringTree tree = ((StringTree)obj).findNode(id);
                if(tree != null)return tree;
            }                
        }
        return null;
    }
           
    public String toJSONObject(String prependId){
        return toJSONObject(prependId, null);
    }
           
    public String toJSONObject(String prependId, Hashtable htLinks){
        StringBuffer stb = new StringBuffer();
        if(htLinks != null){
            if(!name.startsWith("class ") && !name.startsWith("interface ") && !name.equals("Classes") && !name.equals("Interfaces") && htLinks.containsKey(name)){
                stb.append("{title:'link::");
                stb.append(htLinks.get(name));
                stb.append("',widgetId:'");
                stb.append(prependId);
                stb.append("'}");
                return stb.toString();
            }
            else {
                htLinks.put(name, prependId);
            }       
        }        
        stb.append("{title:'");
        if(name != null)
            stb.append(name);
        stb.append("',widgetId:'");
        stb.append(prependId);
        if(childs == null || childs.size() == 0){
            stb.append("',children:[]}");
        }
        else
        {
            stb.append("',children:[");
            for(int i=0;i<childs.size();i++){
                Object obj = childs.get(i);
                if(i !=0 )stb.append(",");
                if(obj instanceof StringTree)
                    stb.append(((StringTree)obj).toJSONObject(prependId+"."+i, htLinks)); 
                else
                {
                    stb.append("{title:'");
                    stb.append((String)obj);
                    stb.append("',widgetId:'");
                    stb.append(prependId+"."+i);
                    stb.append("'}");
                }
            }
            stb.append("]}");
        }
        return stb.toString();
    }
}
