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
        StringBuffer stb = new StringBuffer();
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
                    stb.append(((StringTree)obj).toJSONObject(prependId+"."+i)); 
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
    public static String classIcon = "../images/ico_C.gif";
    public static String interfaceIcon = "../images/ico_I.gif";
    public static String folderIcon = "../images/ico_folder_16x16.gif";
    public static String clIcon = "../images/ico_filetree_16x16.gif";
    public String toHTMLNode(int depth)
    {
        //if(depth > 2)return "";
        StringBuffer stb = new StringBuffer();
        
        if(childs == null || childs.size() == 0){
            stb.append("<div dojoType='TreeNode' title='"+name+"'></div>");
        }
        else
        {
            if(name.equals("Classes") || name.equals("Interfaces"))
                stb.append("<div dojoType='TreeNode' title='"+name+"' childIconSrc='"+folderIcon+"'>");
            else
                stb.append("<div dojoType='TreeNode' title='"+name+"' childIconSrc='"+clIcon+"'>");
            Iterator iter = childs.iterator();
            while(iter.hasNext()){
                Object obj = iter.next();
                if(obj instanceof StringTree)
                    stb.append(((StringTree)obj).toHTMLNode(depth+1));
                else
                {/*
                    String curr = (String)obj;
                    if(curr.startsWith("class"))
                        stb.append("<div dojoType='TreeNode' title='"+curr.substring(6)+"' childIconSrc='"+classIcon+"'></div>");
                    else if(curr.startsWith("interface"))
                        stb.append("<div dojoType='TreeNode' title='"+curr.substring(10)+"' childIconSrc='"+interfaceIcon+"'></div>");
                    else
                        stb.append("<div dojoType='TreeNode' title='"+curr+"'></div>");
                        */
                }                
            }
            stb.append("</div>");            
        }
        return stb.toString();
    }
}
