/**
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

package org.apache.geronimo.test.jpa.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * This class is the implementation of the blogPersistenceService
 */
public class BlogPersistenceServiceImpl implements BlogPersistenceService {

    private EntityManager em;
	
    public BlogPersistenceServiceImpl() {
    }
	
    //@PersistenceContext(unitName = "blogExample")
    public void setEntityManager(EntityManager e) {
        em = e;
    }
	
    public void createAuthor(String email, Date dob, String name,
                             String displayName, String bio) {
        Author a = new Author();
        a.setEmail(email);
        a.setName(name);
        a.setDisplayName(displayName);
        a.setBio(bio);
        a.setDob(dob);
        em.persist(a);
    }

    public void createBlogPost(String authorEmail, String title,
                               String blogText, List<String> tags) {
	
        Author a = em.find(Author.class, authorEmail);
        Entry b = new Entry();
        
        Date publishDate = new Date(System.currentTimeMillis());
        
        b.setBlogText(blogText);
        b.setAuthor(a);
        b.setTitle((title == null) ? "" : title);
        b.setPublishDate(publishDate);
        b.setTags((tags == null) ? new ArrayList<String>() : tags);
        
        a.updateEntries(b);
        em.persist(b);		
        em.merge(b.getAuthor());
        //Uncomment this line to verify that datasources have been enlisted.
        //The data base should not contain the blog post even though it has been persisted.
        //throw new RuntimeException();
    }

    public Entry findBlogEntryByTitle(String title) {
        Query q = em
            .createQuery("SELECT e FROM BLOGENTRY e WHERE e.title = ?1");
		q.setParameter(1, title);
		Entry b = (Entry) q.getSingleResult();
		return b;
    }
    
    public List<Author> getAllAuthors() {
        @SuppressWarnings("unchecked")
        List<Author> list = em.createQuery("SELECT a FROM AUTHOR a").getResultList();        
        return list;
    }

    public List<Entry> getAllBlogEntries() {
        @SuppressWarnings("unchecked")
        List<Entry> list = em.createQuery("SELECT b FROM BLOGENTRY b ORDER BY b.publishDate DESC").getResultList();
        return list;        
    }
    
    public int getNoOfBlogEntries() {
        Number n = (Number) em.createQuery(
                                           "SELECT COUNT(b) FROM BLOGENTRY b").getSingleResult();
        return n.intValue();
    }
    
    public List<Entry> getBlogEntries(int firstPostIndex, int noOfPosts) {
        Query q = em
            .createQuery("SELECT b FROM BLOGENTRY b ORDER BY b.publishDate DESC");
        q.setFirstResult(firstPostIndex);
        q.setMaxResults(noOfPosts);
        
        @SuppressWarnings("unchecked")
        List<Entry> list = q.getResultList();
        
        return list;
    }
    
    public Author getAuthor(String emailAddress) {
        Author a = em.find(Author.class, emailAddress);
        return a;
    }
    
    public List<Entry> getBlogEntriesModifiedBetween(Date start, Date end) {
        Query q = em
            .createQuery("SELECT b FROM BLOGENTRY b WHERE (b.updatedDate >= :start AND b.updatedDate <= :end) OR (b.publishDate >= :start AND b.publishDate <= :end) ORDER BY b.publishDate ASC");
        q.setParameter("start", start);
        q.setParameter("end", end);
        
        @SuppressWarnings("unchecked")
        List<Entry> list = q.getResultList();
        
        return list;
    }
	
    public List<Entry> getBlogsForAuthor(String emailAddress) {
        
        List<Entry> list = em.find(Author.class, emailAddress)
            .getEntries();
        
        return list;
        
    }

    public void updateAuthor(String email, Date dob, String name,
                             String displayName, String bio) {
        Author a = em.find(Author.class, email);
        a.setEmail(email);
        a.setName(name);
        a.setDisplayName(displayName);
        a.setBio(bio);
        a.setDob(dob);
        em.merge(a);
    }
	
    public void updateBlogEntry(long id, String email, String title,
                                String blogText, List<String> tags, Date updatedDate) {
        Entry b = em.find(Entry.class, id);
        b.setTitle(title);
        b.setBlogText(blogText);
        b.setTags(tags);
        b.setUpdatedDate(updatedDate);
        
        em.merge(b);
    }
    
    public void removeAuthor(String emailAddress) {
        em.remove(em.find(Author.class, emailAddress));
    }
    
    public void removeBlogEntry(long id) {
        Entry b = em.find(Entry.class, id);
        b = em.merge(b);
        b.getAuthor().getEntries().remove(b);
        
        em.remove(em.merge(b));
        em.merge(b.getAuthor());
    }

    public Entry getBlogEntryById(long postId) {
        Entry b =  em.find(Entry.class, postId);
        return b;
    }

    public void setPublishDate (long postId, Date date) {
        //Added for testing
        Entry b = em.find(Entry.class, postId);
        b.setPublishDate(date);	
        em.merge(b);
    }
	
    public void setUpdatedDate (long postId, Date date) {
        //Added for testing
        Entry b = em.find(Entry.class, postId);
        b.setUpdatedDate(date);	
        em.merge(b);
    }
}
