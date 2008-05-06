/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.apache.myfaces.blank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.event.ActionEvent;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * A typical simple backing bean, that is backed to <code>helloworld.jsp</code>
 * 
 * @author <a href="mailto:matzew@apache.org">Matthias Weßendorf</a>
 */
public class HelloWorldBacking {

    private static final Logger log = LoggerFactory.getLogger(HelloWorldBacking.class);

    //properties
    private String name;
    private String greeting;
    @Resource(name="testDouble")
    private Double testDouble;
    /**
     * default empty constructor
     */
    public HelloWorldBacking(){
        greeting = "Hello";
    }

    @PostConstruct()
    public void postConstruct() {
        log.error("PostConstruct");
        log.error("Test Double " + testDouble);
    }

    //-------------------getter & setter
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getGreeting()
    {
        return greeting;
    }

    public void setGreeting(String greeting)
    {
        this.greeting = greeting;
    }

    public Double getTestDouble()
    {
        return testDouble;
    }

    public void setTestDouble(Double testDouble)
    {
        this.testDouble = testDouble;
    }

    /**
     * Method that is backed to a submit button of a form.
     */
    public String send(){
        //do real logic
        return ("success");
    }

    public void updateGreeting(ActionEvent evt)
    {
        greeting = "Bye!";
    }
}