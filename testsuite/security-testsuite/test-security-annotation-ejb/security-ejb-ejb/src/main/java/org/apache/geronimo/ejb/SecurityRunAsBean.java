/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.ejb;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
@Stateless
@DeclareRoles({"user","admin"})
@RunAs("user")
public class SecurityRunAsBean implements SecurityRunAsRemote{
    @EJB
    private SecurityRemote securityBean;
	@PermitAll
	public String permitAllMethod() {
		String temp;
		try{
			temp=securityBean.permitAllMethod();
		}catch(Throwable t){
			temp="SecurityBean.permitAllMethod:false";
		}
	    return "SecurityRunAsBean.permitAllMethod:true"+"::"+temp;
	}
	@RolesAllowed({"user"})
	public String rolesAllowedUserMethod() {
		String temp;
		try{
			temp=securityBean.rolesAllowedUserMethod();
		}catch(Throwable t){
			temp="SecurityBean.rolesAllowedUserMethod:false";
		}
	    return "SecurityRunAsBean.rolesAllowedUserMethod:true"+"::"+temp;
	}
	@RolesAllowed({"admin"})
	public String rolesAllowedAdminMethod() {
		String temp;
		try{
			temp=securityBean.rolesAllowedAdminMethod();
		}catch(Throwable t){
			temp="SecurityBean.rolesAllowedAdminMethod:false";
		}
	    return "SecurityRunAsBean.rolesAllowedAdminMethod:true"+"::"+temp;
	}
    @DenyAll
	public String denyAllMethod() {
    	String temp;
		try{
			temp=securityBean.denyAllMethod();
		}catch(Throwable t){
			temp="SecurityBean.rolesAllowedAdminMethod:false";
		}
	    return "SecurityRunAsBean.denyAllMethod:false"+"::"+temp;
	}
}
