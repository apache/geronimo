package org.apache.geronimo.security.jaas;

import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.ContextManager;

import javax.security.auth.Subject;
import java.util.*;
import java.security.Principal;

/**
 * Tracks security information about a single user.  This is used before,
 * during, and after the login.
 *
 * @version $Revision: 1.0$
 */
public class JaasSecurityContext {
    private String realmName;
    private Subject subject;
    private long created;
    private boolean done;
    private JaasLoginModuleConfiguration[] modules;
    private DecouplingCallbackHandler handler;
    private Set processedPrincipals = new HashSet();

    public JaasSecurityContext(String realmName, JaasLoginModuleConfiguration[] modules) {
        this.realmName = realmName;
        this.created = System.currentTimeMillis();
        this.done = false;
        this.modules = modules;
        subject = new Subject();
    }

    public Subject getSubject() {
        return subject;
    }

    public long getCreated() {
        return created;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public JaasLoginModuleConfiguration[] getModules() {
        return modules;
    }

    public DecouplingCallbackHandler getHandler() {
        if(handler == null) { //lazy create
            handler = new DecouplingCallbackHandler();
        }
        return handler;
    }

    public void processPrincipals() {
        List list = new LinkedList();
        for (Iterator it = subject.getPrincipals().iterator(); it.hasNext();) {
            Principal p = (Principal) it.next();
            if(!processedPrincipals.contains(p)) {
                list.add(ContextManager.registerPrincipal(new RealmPrincipal(realmName, p)));
                processedPrincipals.add(p);
            }
        }
        subject.getPrincipals().addAll(list);
    }

    public void processPrincipals(Principal[] principals) {
        List list = new LinkedList();
        for (int i = 0; i < principals.length; i++) {
            Principal p = principals[i];
            list.add(p);
            list.add(ContextManager.registerPrincipal(new RealmPrincipal(realmName, p)));
        }
        subject.getPrincipals().addAll(list);
    }
}
