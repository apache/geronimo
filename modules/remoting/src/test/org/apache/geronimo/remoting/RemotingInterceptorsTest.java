/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http:www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.remoting;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.apache.geronimo.proxy.ProxyContainer;
import org.apache.geronimo.proxy.ReflexiveInterceptor;
import org.apache.geronimo.remoting.router.InterceptorRegistryRouter;
import org.apache.geronimo.remoting.transport.BytesMarshalledObject;
import org.apache.geronimo.remoting.transport.RemoteTransportInterceptor;
import org.apache.geronimo.remoting.transport.TransportFactory;
import org.apache.geronimo.remoting.transport.TransportServer;
import org.apache.geronimo.remoting.transport.URISupport;

/**
 * Unit test for the Marshaling/DeMarshaling Interceptors
 *
 * This test uses 2 classloaders to mock 2 seperate
 * application classloaders.
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/19 11:15:03 $
 */

public class RemotingInterceptorsTest extends TestCase {

    private static final String PERSON_CLASS = "org.apache.geronimo.remoting.Person";
    private static final String TRANSIENT_CLASS = "org.apache.geronimo.remoting.TransientValue";
    URLClassLoader cl1, cl2;
    
    TransportServer server;
    URI connectURI;    

    /**
     * creates the two classloaders that will be used during the tests.
     */
    public void setUp() throws Exception {
        URL url = new File("./target/mock-app").toURL();
        System.out.println("Setting up the CP to: " + url);

        cl1 = new URLClassLoader(new URL[] { url });
        cl2 = new URLClassLoader(new URL[] { url });

        System.out.println("================================================");
        URI bindURI = new URI("async://0.0.0.0:0");
        TransportFactory tf = TransportFactory.getTransportFactory(bindURI);
        server = tf.createSever();
        InterceptorRegistryRouter irr = new InterceptorRegistryRouter();
        irr.doStart();
        server.bind(bindURI,irr);
        connectURI = server.getClientConnectURI();
        server.start();

    }
    
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        server.dispose();
    }    

    /**
     * Verify that the classpath/classloader structure is
     * as expected.  If the application classes are in
     * the current classloader then that will throw all the 
     * tests off.
     * 
     * @throws Exception
     */
    public void testCheckClassLoaders() throws Exception {
        Class class1 = cl1.loadClass(PERSON_CLASS);
        Class class2 = cl2.loadClass(PERSON_CLASS);
        assertFalse("Classpath for this test was incorrect.  The '"+PERSON_CLASS+"' cannot be in the test's classpath.", class1.equals(class2));
    }

    /**
     * Verify that the helper methods used by this test work
     * as expected.  Everything should be ok when working in the
     * same classloader.
     * 
     * @throws Throwable
     */
    public void testSetSpouseSameCL() throws Throwable {
        Class class1 = cl1.loadClass(PERSON_CLASS);
        Class class2 = cl1.loadClass(PERSON_CLASS);

        Object object1 = class1.newInstance();
        Object object2 = class2.newInstance();

        call(object1, "setSpouse", new Object[] { object2 });
    }

    /**
     * Verify that the helper methods used by this test work
     * as expected.  We should see problems when you start mixing
     * Objects from different classloaders.
     * 
     * @throws Throwable
     */
    public void testSetSpouseOtherCL() throws Throwable {
        Class class1 = cl1.loadClass(PERSON_CLASS);
        Class class2 = cl2.loadClass(PERSON_CLASS);

        Object object1 = class1.newInstance();
        Object object2 = class2.newInstance();

        try {
            call(object1, "setSpouse", new Object[] { object2 });
            fail("Call should fail due to argument type mismatch.");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verify that a proxy using working in a single app context works ok.
     * 
     * @throws Throwable
     */
    public void testSetSpouseWithProxy() throws Throwable {
        Class class1 = cl1.loadClass(PERSON_CLASS);
        Object object1 = class1.newInstance();

        // Simulate App1 creating a proxy.
        Thread.currentThread().setContextClassLoader(cl1);
        Object proxy1 = createProxy(object1, true);
        call(proxy1, "setSpouse", new Object[] { object1 });
    }

    /**
     * App2 creates a proxy and serializes.  App1 context deserializes
     * the proxy and trys to use it.  Method calls should be getting
     * marshalled/demarshaled by the proxy to avoid getting an 
     * IllegalArgumentException.
     * 
     * @throws Throwable
     */
    public void testSetSpouseOtherCLWithSerializedProxy() throws Throwable {
        Class class1 = cl1.loadClass(PERSON_CLASS);
        Class class2 = cl2.loadClass(PERSON_CLASS);

        Object object1 = class1.newInstance();
        Object object2 = class2.newInstance();

        // Simulate App2 creating a proxy.
        Thread.currentThread().setContextClassLoader(cl2);
        Object proxy2 = createProxy(object2, true);
        MarshalledObject mo = new BytesMarshalledObject(proxy2);

        // Simulate App1 using the serialized proxy.    
        Thread.currentThread().setContextClassLoader(cl1);
        Object proxy1 = mo.get();
        call(proxy1, "setSpouse", new Object[] { object1 });
    }

    /**
     * App1 creates a proxy.  It then creates a Transient calls that holds
     * a "value" property that is transient when serialized.  This allows to 
     * to see if the Transient object was serialized by getting the value back
     * and seeing if the "value" is null.  All this work is done in one classloader
     * so no serialization should occur.
     * 
     * @throws Throwable
     */
    public void testSetTransientWithOptimizedProxy() throws Throwable {
        Thread.currentThread().setContextClassLoader(cl1);
        Class class1 = cl1.loadClass(PERSON_CLASS);
        Object object1 = class1.newInstance();

        Class class2 = cl1.loadClass(TRANSIENT_CLASS);
        Object object2 = class2.newInstance();
        call(object2, "setValue", new Object[] { "foo" });

        Object proxy1 = createProxy(object1, false);
        call(proxy1, "setValue", new Object[] { object2 });
        Object rc = call(proxy1, "getValue", new Object[] {
        });
        rc = call(rc, "getValue", new Object[] {
        });

        assertSame(rc, "foo");
    }
    
    /**
     * Same as testSetTransientWithOptimizedProxy() but, the proxy is serialized before it is used.
     * It should still result in the method call not being serialized.
     * 
     * @throws Throwable
     */
    public void testSetTransientWithSerializedOptimizedProxy() throws Throwable {
        Thread.currentThread().setContextClassLoader(cl1);
        Class class1 = cl1.loadClass(PERSON_CLASS);
        Object object1 = class1.newInstance();

        Class class2 = cl1.loadClass(TRANSIENT_CLASS);
        Object object2 = class2.newInstance();
        call(object2, "setValue", new Object[] { "foo" });

        // First test to see waht happens if we mock it to be a remote object.
        Object proxy1 = createProxy(object1,true);
        proxy1 = new BytesMarshalledObject(proxy1).get();
        call(proxy1, "setValue", new Object[] { object2 });
        Object rc = call(proxy1, "getValue", new Object[] {
        });
        rc = call(rc, "getValue", new Object[] {
        });
        assertSame(rc, null); // The value was marsalled cause it's remote.

        // Second test to see what happens if we mock it to be a local object.
        proxy1 = createProxy(object1,false);
        proxy1 = new BytesMarshalledObject(proxy1).get();
        call(proxy1, "setValue", new Object[] { object2 });
        rc = call(proxy1, "getValue", new Object[] {
        });
        rc = call(rc, "getValue", new Object[] {
        });
        assertSame(rc, "foo"); // No serialization occured.
    }
    
    /**
     * Same as testSetTransientWithOptimizedProxy() but, the proxy is serialized before it is
     * by App2.  Since a different classloader is using the proxy, the metod call should
     * be serialized and we should see that the set "value" is null.
     * 
     * @throws Throwable
     */
    public void testSetTransientWithSerializedNonOptimizedProxy() throws Throwable {
        Thread.currentThread().setContextClassLoader(cl1);
        Class class1 = cl1.loadClass(PERSON_CLASS);
        Object object1 = class1.newInstance();

        Object proxy1 = createProxy(object1, true);
        Thread.currentThread().setContextClassLoader(cl2);
        proxy1 = new BytesMarshalledObject(proxy1).get();

        Class class2 = cl2.loadClass(TRANSIENT_CLASS);
        Object object2 = class2.newInstance();
        call(object2, "setValue", new Object[] { "foo" });

        call(proxy1, "setValue", new Object[] { object2 });
        Object rc = call(proxy1, "getValue", new Object[] {
        });
        rc = call(rc, "getValue", new Object[] {
        });

        assertSame(rc, null);
    }

    /**
     * Does a reflexive call on object1 my calling method with the provided args.
     * 
     * @param object1
     * @param string
     * @param objects
     */
    private Object call(Object object1, String method, Object[] args) throws Throwable {
        try {
            Class argTypes[] = getArgTypes(object1.getClass().getClassLoader(), args);
            Method m = findMethod(object1.getClass(), method);
            return m.invoke(object1, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * Gets the Class[] for a given Object[] using the provided loader.
     * 
     * @param object1
     * @param args
     * @return
     */
    private Class[] getArgTypes(ClassLoader loader, Object[] args) throws Exception {
        Class rc[] = new Class[args.length];
        for (int i = 0; i < rc.length; i++) {
            rc[i] = loader.loadClass(args[i].getClass().getName());
        }
        return rc;
    }

    /**
     * Finds the first method in class c whose name is name.
     * @param c
     * @param name
     * @return
     */
    private Method findMethod(Class c, String name) {
        Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(name))
                return methods[i];
        }
        return null;
    }

    /**
      * @param object1
      * @return
      */
    private Object createProxy(Object target, boolean mockFromRemoteMV ) throws Exception {

        // Setup the server side contianer..        
        ProxyContainer serverContainer = new ProxyContainer();        
        DeMarshalingInterceptor demarshaller = new DeMarshalingInterceptor();
        serverContainer.addInterceptor(demarshaller);      
        serverContainer.addInterceptor(new ReflexiveInterceptor(target));
        
        // Configure the server side interceptors.
        demarshaller.setClassloader(target.getClass().getClassLoader());
        Long dmiid = InterceptorRegistry.instance.register(demarshaller);
        
        // Setup the client side container..        
        ProxyContainer clientContainer = new ProxyContainer();
        InterVMRoutingInterceptor remoteRouter = new InterVMRoutingInterceptor();
        clientContainer.addInterceptor(remoteRouter);
        IntraVMRoutingInterceptor localRouter = new IntraVMRoutingInterceptor();
        clientContainer.addInterceptor(localRouter);

        // Configure the client side interceptors.
        localRouter.setDeMarshalingInterceptorID(dmiid);
        localRouter.setNext(demarshaller.getNext());
        remoteRouter.setLocalInterceptor(localRouter);
        RemoteTransportInterceptor transport = new RemoteTransportInterceptor();
        transport.setRemoteURI(URISupport.setFragment(connectURI, ""+dmiid));
        remoteRouter.setTransportInterceptor(transport);
        if(mockFromRemoteMV)
            remoteRouter.setTargetVMID("THIS CRAP WILL NOT MATCH THE LOCAL VM ID");
        
        return clientContainer.createProxy(target.getClass().getClassLoader(), target.getClass().getInterfaces());
    }

}
