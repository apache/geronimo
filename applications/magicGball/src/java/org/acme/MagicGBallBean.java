/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.acme;
/**
 * Bean implementation class for Enterprise Bean: MagicGBall
 *
 * @version $Rev: 46301 $ $Date: 2004-09-18 13:03:59 -0700 (Sat, 18 Sep 2004) $
 */
public class MagicGBallBean implements javax.ejb.SessionBean {

	private static final String[] answers = {"It is certain","Outlook not so good","You may rely on it","My Sources say no","It is decidedly so", "Rephrase and ask again","Most likely","Don't count on it","Yes definitely","Very doubtful"}; 

	public String ask(String question){
		return answers[Math.abs(question.hashCode()) % answers.length];
	}
	
	private javax.ejb.SessionContext mySessionCtx;
	/**
	 * getSessionContext
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * setSessionContext
	 */
	public void setSessionContext(javax.ejb.SessionContext ctx) {
		mySessionCtx = ctx;
	}
	/**
	 * ejbCreate
	 */
	public void ejbCreate() throws javax.ejb.CreateException {
	}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {
	}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {
	}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {
	}
}
