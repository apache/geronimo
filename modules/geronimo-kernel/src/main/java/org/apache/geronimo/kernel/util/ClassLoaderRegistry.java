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
package org.apache.geronimo.kernel.util;

import java.util.List;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class ClassLoaderRegistry {
    private static List<WeakReference> list = new ArrayList<WeakReference>();
    public static List getList(){
        List<ClassLoader> ret = new ArrayList<ClassLoader>();
        for(int i=0;i<list.size();i++)
            if(list.get(i) != null)ret.add((ClassLoader)list.get(i).get());
            else
                list.remove(i);
        return ret;
    }
    public static boolean add(ClassLoader cloader){
        if(contains(cloader))
            return false;
        return list.add(new WeakReference<ClassLoader>(cloader));
    } 
    public static boolean contains(ClassLoader cloader){
        for(int i=0;i<list.size();i++){
            WeakReference wk = list.get(i);
            if(wk.get() == null)list.remove(i);
            else if(wk.get().equals(cloader))
                return true;            
        }
        return false;
    }
    public static boolean remove(ClassLoader cloader){
        boolean result = false;
        for(int i=0;i<list.size();i++){
            WeakReference wk = list.get(i);
            if(wk.get() == null)list.remove(i);
            else if(wk.get().equals(cloader)){
                list.remove(i);
                result = true;
            }
        }
        return result;
    } 
}
