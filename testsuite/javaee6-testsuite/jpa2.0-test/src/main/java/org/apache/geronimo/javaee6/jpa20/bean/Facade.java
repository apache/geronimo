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

package org.apache.geronimo.javaee6.jpa20.bean;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.geronimo.javaee6.jpa20.entities.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.ListJoin;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionAttribute;

import javax.ejb.Stateless;
import javax.ejb.Stateful;

@Stateful
//@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class Facade {

    @PersistenceContext(unitName = "CourseSelectPU")
    private EntityManager em;

    public void createCourse(Course course) {
//    System.out.println("in Facade.createCourse, cname is:"+course.getCname());
    if (em==null || em.equals(null))
    {
    System.out.println("em is null");
    return;
    }
//        em.getTransaction().begin();
        em.persist(course);
//        em.getTransaction().commit();

    }

    public void editCourse(Course course) {
//        em.getTransaction().begin();
        em.merge(course);
//        em.getTransaction().commit();
    }

    public void removeCourse(Course course) {
//        em.getTransaction().begin();
        em.remove(em.merge(course));
//        em.getTransaction().commit();
    }


    public void addComment(int cid, String comment) {
        Course course=findCourse(cid);
//        System.out.println("course_ID is :"+cid);
        List<String> evaluations=course.getEvaluation();
        
        evaluations.add(comment);
        course.setEvaluation(evaluations);
        editCourse(course);
    }

        public void selectCourse(Student student, Course course) {
            List<Course> map = new ArrayList<Course>();
			if(student.getCourses()!=null){
			    map = student.getCourses();
			}
            map.add(course);
            student.setCourses(map);
//            double totalScore = student.getTotalScore() + score;
//            student.setTotalScore(totalScore);
//            em.getTransaction().begin();
            em.merge(student);
//            em.getTransaction().commit();
//            System.out.println("Student " + student.getInfo().getName() + " has Selected course:" + course.getCname());
        }


    public void unselectCourse(Student student, Course course) {
        List<Course> map = student.getCourses();
//        double totalScore = student.getTotalScore();
//        totalScore -= 3;
        map.remove(course);
        student.setCourses(map);
//        student.setTotalScore(totalScore);
//        em.getTransaction().begin();
        em.merge(student);
//        em.getTransaction().commit();
//        System.out.println("Student " + student.getInfo().getName() + " has Canceled course:" + course.getCname());
    }
	
    public void createStudent(Student student) {
//        System.out.println("in createStudent.");
//        em.getTransaction().begin();
        em.persist(student);
//        em.getTransaction().commit();
    }

    public void editStudent(Student student) {
//        em.getTransaction().begin();
        em.merge(student);
//        em.getTransaction().commit();
    }

    public void removeStudent(Student student) {
//        em.getTransaction().begin();
        em.remove(em.merge(student));
//        em.getTransaction().commit();
    }

    public Student findStudent(int id) {
        return em.find(Student.class, id);
    }

    public List<Student> findAllStudent() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Student.class));
        return em.createQuery(cq).getResultList();
    }

    public int countStudent() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Student> rt = cq.from(Student.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }


    public Course findCourse(int id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Course> cq = cb.createQuery(Course.class);
        Root<Course> course = cq.from(Course.class);
        cq.select(course).where(cb.equal(course.get("cid"), id));
        Query query = em.createQuery(cq);
        Course result = (Course) query.getSingleResult();
        if (result != null) {
//            System.out.println("Cousre with id: " + cid + " is found");
        }
        return result;
    }

    public List<Course> findAllCourse() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Course.class));
        return em.createQuery(cq).getResultList();
    }

    public Course findCourseByName(String cname) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Course> cq = cb.createQuery(Course.class);
        Root<Course> course = cq.from(Course.class);
        cq.select(course).where(cb.equal(course.get("cname"), cname));
        Query query = em.createQuery(cq);
        Course result = (Course) query.getSingleResult();
        if (result != null) {
//            System.out.println("Cousre with name: " + cname + " is found");
        }
        return result;
    }
    
    public String nullIf(int studentId){
        String jpql = "SELECT NULLIF(0,0) from Student s where s.id="+studentId ;
        Query query = em.createQuery(jpql);
        String result = new String();
        
        if( query.getSingleResult()==null){
        	result = "sucess";
        }
        else {
        	result = "fail, and query return value is:"+query.getSingleResult().toString();
        }
        return result;
    }


}
