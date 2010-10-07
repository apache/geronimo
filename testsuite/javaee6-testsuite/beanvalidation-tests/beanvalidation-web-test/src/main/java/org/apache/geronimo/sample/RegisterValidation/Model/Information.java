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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.sample.RegisterValidation.Model;


import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.geronimo.sample.RegisterValidation.Constraints.ValidDay;


public class Information {
    @NotNull(message="Name can not be null!")
    @Size(min=1,max=5,message="The length of name should between {min} and {max}")
    String name;

    @Min(value=1,message="Age should be larger than {value}")
    @Max(value=100,message="Age should be less than {value}")
    int age;

    //@Pattern(regexp="^[\\w.-]+@([0-9a-zA-Z\\w-]+\\.)+[0-9a-zA-Z]{2,8}$")
    @Pattern(regexp="^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$" )
    String mail;


    @ValidDay
    String birthday;


    @NotNull
    @Valid
    Address address;
    
    @Min.List({@Min(value=1000,groups=OrdinaryPeople.class,message="Your salary should not be less than {value}"),
               @Min(value=10000,groups=VIP.class,message="As a VIP,your salary should not be less than {value}")})
    int salary;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    

}
