package org.apache.geronimo.naming.deployment;

import java.util.Set;

import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;
import javax.management.Attribute;

import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 */
public class GBeanResourceEnvironmentBuilder implements ResourceEnvironmentBuilder {

    private final GBeanMBean gbean;

    public GBeanResourceEnvironmentBuilder(GBeanMBean gbean) {
        this.gbean = gbean;
    }

    public Set getUnshareableResources() {
        try {
            return (Set)gbean.getAttribute("unshareableResources");
        } catch (ReflectionException e) {
            throw (IllegalStateException)new IllegalStateException().initCause(e);
        } catch (AttributeNotFoundException e) {
            throw (IllegalStateException)new IllegalStateException().initCause(e);
        }
    }

    public void setUnshareableResources(Set unshareableResources) {
        try {
            gbean.setAttribute(new Attribute("unshareableResources", unshareableResources));
         } catch (ReflectionException e) {
             throw (IllegalStateException)new IllegalStateException().initCause(e);
         } catch (AttributeNotFoundException e) {
             throw (IllegalStateException)new IllegalStateException().initCause(e);
         }

    }

    public Set getApplicationManagedSecurityResources() {
        try {
             return (Set)gbean.getAttribute("applicationManagedSecurityResources");
         } catch (ReflectionException e) {
             throw (IllegalStateException)new IllegalStateException().initCause(e);
         } catch (AttributeNotFoundException e) {
             throw (IllegalStateException)new IllegalStateException().initCause(e);
         }
    }

    public void setApplicationManagedSecurityResources(Set applicationManagedSecurityResources) {
        try {
             gbean.setAttribute(new Attribute("applicationManagedSecurityResources", applicationManagedSecurityResources));
         } catch (ReflectionException e) {
             throw (IllegalStateException)new IllegalStateException().initCause(e);
         } catch (AttributeNotFoundException e) {
             throw (IllegalStateException)new IllegalStateException().initCause(e);
         }
    }
}
