/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.javaee6.asynejb.ejb;

import org.apache.geronimo.javaee6.asynejb.entity.Log;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.AsyncResult;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.Asynchronous;
import javax.ejb.Timer;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Stateless
public class LogSessionBean {

    @PersistenceContext(unitName = "EJBTimerPU")
    private EntityManager em;

    @Asynchronous
    public void WriteIntoDB() {
        int count = 10;
        int current = 0;
        while (current < count) {
            try {
                Log log = new Log();
                Date dt = new Date();
                DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String nowTime = df.format(dt);
                log.setCreateTime(nowTime);
                log.setEvent("event" + new Random().nextInt());
//                System.out.println("count is :"+current);
                create(log);
               Thread.sleep(2000);
                current++;
          }
           catch (InterruptedException ex) {
            }
        }
    }

    @Asynchronous
    public Future<Integer> notifyCustomers() {
        int count = 10;
        int current = 0;
        while (current < count) {
            try {
//                System.out.println("Notify Time is: " + new Date());
//                System.out.println("The current id is: " + current);
                Thread.sleep(1000);
                current++;
            } catch (InterruptedException ex) {
                Logger.getLogger(LogSessionBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Future<Integer> future = new AsyncResult(current);
        return future;
    }

    public void create(Log log) {

        em.persist(log);
    }

    public int count() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Log> rt = cq.from(Log.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
//        System.out.println("in LogSessionBean.java, the count res is:"+((Long) q.getSingleResult()).intValue());
       return ((Long) q.getSingleResult()).intValue();
    }
}
