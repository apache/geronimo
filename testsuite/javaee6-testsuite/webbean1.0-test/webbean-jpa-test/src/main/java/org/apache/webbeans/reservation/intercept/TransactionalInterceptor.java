/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.reservation.intercept;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.webbeans.reservation.bindings.intercep.Transactional;

@Interceptor
@Transactional
public class TransactionalInterceptor
{
    @Inject @Resource UserTransaction tx;
   
    @AroundInvoke
    public Object invoke(InvocationContext context) throws Exception
    {
       
        //System.out.println("in TransactionInterceptor, get tx!"+context.getTarget().getClass().getName());
        try
        {
            if(tx.getStatus()==Status.STATUS_NO_TRANSACTION)
            {
                tx.begin();
                //System.out.println("in TransactionInterceptor,tx.begin!");
            }
            //System.out.println("in TransactionInterceptor,context.proceed()!");
            return context.proceed();

        }
        catch(Exception e)
        {
            //System.out.println("Exception in transactional method call:"+ e.getMessage());
            if(tx != null)
            {
                tx.rollback();
            }

            throw e;

        }
        finally
        {
            if(tx != null && tx.getStatus()==Status.STATUS_ACTIVE)
            {
                tx.commit();
                //.out.println("in TransactionInterceptor, tx.commit!");
            }
        }

    }

}
