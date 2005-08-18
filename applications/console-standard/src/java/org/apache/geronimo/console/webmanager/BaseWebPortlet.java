package org.apache.geronimo.console.webmanager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.portlet.GenericPortlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 1.0$
 */
public class BaseWebPortlet extends GenericPortlet {
    private final static Log log = LogFactory.getLog(BaseWebPortlet.class);
    protected final static String SERVER_JETTY = "jetty";
    protected final static String SERVER_TOMCAT = "tomcat";
    protected final static String SERVER_GENERIC = "generic";

    protected final static String getServerType(Class cls) {
        Class[] intfs = cls.getInterfaces();
        for (int i = 0; i < intfs.length; i++) {
            Class intf = intfs[i];
            if(intf.getName().indexOf("Jetty") > -1) {
                return SERVER_JETTY;
            } else if(intf.getName().indexOf("Tomcat") > -1) {
                return SERVER_TOMCAT;
            }
        }
        return SERVER_GENERIC;
    }

    public final static void setProperty(Object target, String name, Object value) {
        boolean found = false;
        Class cls = target.getClass();
        String setter = "set"+Character.toUpperCase(name.charAt(0))+name.substring(1);
        Method[] list = cls.getMethods();
        for (int i = 0; i < list.length; i++) {
            Method method = list[i];
            if(method.getName().equals(setter) && method.getParameterTypes().length == 1 && Modifier.isPublic(method.getModifiers()) &&
                    !Modifier.isStatic(method.getModifiers())) {
                found = true;
                try {
                    method.invoke(target, new Object[]{value});
                } catch (Exception e) {
                    log.error("Unable to set property "+name+" on "+target.getClass().getName());
                }
                break;
            }
        }
        if(!found) {
            throw new IllegalArgumentException("No such method found ("+setter+" on "+target.getClass().getName()+")");
        }
    }

    public final static Object getProperty(Object target, String name) {
        Class cls = target.getClass();
        String getter = "get"+Character.toUpperCase(name.charAt(0))+name.substring(1);
        Method[] list = cls.getMethods();
        for (int i = 0; i < list.length; i++) {
            Method method = list[i];
            if(method.getName().equals(getter) && method.getParameterTypes().length == 0 && Modifier.isPublic(method.getModifiers()) &&
                    !Modifier.isStatic(method.getModifiers())) {
                try {
                    return method.invoke(target, new Object[0]);
                } catch (Exception e) {
                    log.error("Unable to get property "+name+" on "+target.getClass().getName());
                }
                break;
            }
        }
        throw new IllegalArgumentException("No such method found ("+getter+" on "+target.getClass().getName()+")");
    }

    public final static Object callOperation(Object target, String operation, Object[] args) {
        Class cls = target.getClass();
        Method[] list = cls.getMethods();
        for (int i = 0; i < list.length; i++) {
            Method method = list[i];
            if(method.getName().equals(operation) && ((args == null && method.getParameterTypes().length == 0) || (args != null && args.length == method.getParameterTypes().length))
                    && Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
                try {
                    return method.invoke(target, args);
                } catch (Exception e) {
                    log.error("Unable to invoke "+operation+" on "+target.getClass().getName());
                }
                break;
            }
        }
        throw new IllegalArgumentException("No such method found ("+operation+" on "+target.getClass().getName()+")");
    }
}
