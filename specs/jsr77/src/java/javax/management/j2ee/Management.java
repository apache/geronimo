package javax.management.j2ee;

import javax.ejb.EJBObject;
import javax.management.*;
import java.rmi.RemoteException;
import java.util.Set;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface Management extends EJBObject {
    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, RemoteException;

    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException, RemoteException;

    public String getDefaultDomain() throws RemoteException;

    public Integer getMBeanCount() throws RemoteException;

    public MBeanInfo getMBeanInfo(ObjectName name) throws IntrospectionException, InstanceNotFoundException, ReflectionException, RemoteException;

    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException, RemoteException;

    public boolean isRegistered(ObjectName name) throws RemoteException;

    public Set quertyNames(ObjectName name, QueryExp query) throws RemoteException;

    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, RemoteException;

    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException, RemoteException;

    public ListenerRegistration getListenerRegistration() throws RemoteException;
}