// 
// CORBA RMI-IIOP Skeleton Generator
//   Interface: NameServiceOperations_Skeleton
//   Date: Wed Dec 08 15:22:39 EST 2004

/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop;

import java.util.HashMap;


public class NameServiceOperations_Skeleton
        extends RemoteObject
        implements RemoteInterface {
    // 
    // Fields
    // 
    public java.lang.String[] _ids = {"org.apache.geronimo.interop.rmi.iiop.NameServiceOperations", "RMI:org.apache.geronimo.interop.rmi.iiop.NameServiceOperations:0000000000000000", "NameService"};
    public java.util.HashMap _methods = new HashMap(10);
    public org.apache.geronimo.interop.rmi.iiop.NameServiceOperations _servant = null;
    public java.lang.String _f3 = "f1";
    public java.lang.String[] _f4 = {"f1", "f2"};
    public org.apache.geronimo.interop.rmi.iiop.ValueType vt$0 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(java.lang.String.class);
    public org.apache.geronimo.interop.rmi.iiop.ValueType vt$1 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(org.apache.geronimo.interop.CosNaming.NameComponent[].class);
    public org.apache.geronimo.interop.rmi.iiop.ValueType vt$2 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(org.omg.CORBA.Object.class);
    public org.apache.geronimo.interop.rmi.iiop.ValueType vt$3 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(org.apache.geronimo.interop.CosNaming.BindingListHolder.class);
    public org.apache.geronimo.interop.rmi.iiop.ValueType vt$4 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(org.apache.geronimo.interop.CosNaming.BindingIteratorHolder.class);
    public org.apache.geronimo.interop.rmi.iiop.ValueType vt$5 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(org.apache.geronimo.interop.CosNaming.NamingContext.class);

    //
    // Constructors
    // 
    public NameServiceOperations_Skeleton() {
        super();

        registerMethods();
    }

    protected void registerMethods() {
        super.registerMethods();

        registerMethod("resolve_host", 0);
        registerMethod("to_string", 1);
        registerMethod("to_name", 2);
        registerMethod("to_url", 3);
        registerMethod("resolve_str", 4);
        registerMethod("list", 5);
        registerMethod("resolve", 6);
        registerMethod("bind", 7);
        registerMethod("bind_context", 8);
        registerMethod("rebind", 9);
        registerMethod("rebind_context", 10);
        registerMethod("unbind", 11);
        registerMethod("new_context", 12);
        registerMethod("bind_new_context", 13);
    }
    
    // 
    // Methods
    // 
    
    public void registerMethod(java.lang.String name, int id) {
        _methods.put(name, new Integer(id));
    }

    public java.lang.String[] getIds() {
        return _ids;
    }

    public RemoteInterface $getSkeleton() {
        return this;
    }

    public ObjectRef $getObjectRef() {
        org.apache.geronimo.interop.rmi.iiop.ObjectRef or = new ObjectRef();
        or.$setID("RMI:org.apache.geronimo.interop.rmi.iiop.NameServiceOperations:0000000000000000");
        or.$setObjectKey("org.apache.geronimo.interop.rmi.iiop.NameServiceOperations");
        return or;
    }

    public void $invoke(java.lang.String methodName, byte[] objectKey, java.lang.Object instance, org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        java.lang.Integer m = (Integer) _methods.get(methodName);
        if (m == null) {
            throw new org.omg.CORBA.BAD_OPERATION(methodName);
        }

        _servant = (org.apache.geronimo.interop.rmi.iiop.NameServiceOperations) instance;

        if (m.intValue() < 0) {
            super.invoke(m.intValue(), objectKey, instance, input, output);
        }


        switch (m.intValue()) {
            case 0:
                {
                    resolve_host(input, output);
                }
                break;
            case 1:
                {
                    to_string(input, output);
                }
                break;
            case 2:
                {
                    to_name(input, output);
                }
                break;
            case 3:
                {
                    to_url(input, output);
                }
                break;
            case 4:
                {
                    resolve_str(input, output);
                }
                break;
            case 5:
                {
                    list(input, output);
                }
                break;
            case 6:
                {
                    resolve(input, output);
                }
                break;
            case 7:
                {
                    bind(input, output);
                }
                break;
            case 8:
                {
                    bind_context(input, output);
                }
                break;
            case 9:
                {
                    rebind(input, output);
                }
                break;
            case 10:
                {
                    rebind_context(input, output);
                }
                break;
            case 11:
                {
                    unbind(input, output);
                }
                break;
            case 12:
                {
                    new_context(input, output);
                }
                break;
            case 13:
                {
                    bind_new_context(input, output);
                }
                break;
        }
    }

    public void resolve_host(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        java.lang.String rc;

        try {
            java.lang.String p0 = (java.lang.String) input.readObject(vt$0);
            rc = _servant.resolve_host(p0);
            output.writeObject(vt$0, rc);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void to_string(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        java.lang.String rc;

        try {
            org.apache.geronimo.interop.CosNaming.NameComponent[] p0 = (org.apache.geronimo.interop.CosNaming.NameComponent[]) input.readObject(vt$1);
            rc = _servant.to_string(p0);
            output.writeObject(vt$0, rc);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void to_name(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        org.apache.geronimo.interop.CosNaming.NameComponent[] rc;

        try {
            java.lang.String p0 = (java.lang.String) input.readObject(vt$0);
            rc = _servant.to_name(p0);
            output.writeObject(vt$1, rc);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void to_url(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        java.lang.String rc;

        try {
            java.lang.String p0 = (java.lang.String) input.readObject(vt$0);
            java.lang.String p1 = (java.lang.String) input.readObject(vt$0);
            rc = _servant.to_url(p0, p1);
            output.writeObject(vt$0, rc);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void resolve_str(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        org.omg.CORBA.Object rc;

        try {
            java.lang.String p0 = (java.lang.String) input.readObject(vt$0);
            rc = _servant.resolve_str(p0);
            output.writeObject(vt$2, rc);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void list(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {

        try {
            int p0 = input.readInt();
            org.apache.geronimo.interop.CosNaming.BindingListHolder p1 = (org.apache.geronimo.interop.CosNaming.BindingListHolder) input.readObject(vt$3);
            org.apache.geronimo.interop.CosNaming.BindingIteratorHolder p2 = (org.apache.geronimo.interop.CosNaming.BindingIteratorHolder) input.readObject(vt$4);
            _servant.list(p0, p1, p2);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void resolve(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        org.omg.CORBA.Object rc;

        try {
            org.apache.geronimo.interop.CosNaming.NameComponent[] p0 = (org.apache.geronimo.interop.CosNaming.NameComponent[]) input.readObject(vt$1);
            rc = _servant.resolve(p0);
            output.writeObject(vt$2, rc);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void bind(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {

        try {
            org.apache.geronimo.interop.CosNaming.NameComponent[] p0 = (org.apache.geronimo.interop.CosNaming.NameComponent[]) input.readObject(vt$1);
            org.omg.CORBA.Object p1 = (org.omg.CORBA.Object) input.readObject(vt$2);
            _servant.bind(p0, p1);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void bind_context(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {

        try {
            org.apache.geronimo.interop.CosNaming.NameComponent[] p0 = (org.apache.geronimo.interop.CosNaming.NameComponent[]) input.readObject(vt$1);
            org.apache.geronimo.interop.CosNaming.NamingContext p1 = (org.apache.geronimo.interop.CosNaming.NamingContext) input.readObject(vt$5);
            _servant.bind_context(p0, p1);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void rebind(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {

        try {
            org.apache.geronimo.interop.CosNaming.NameComponent[] p0 = (org.apache.geronimo.interop.CosNaming.NameComponent[]) input.readObject(vt$1);
            org.omg.CORBA.Object p1 = (org.omg.CORBA.Object) input.readObject(vt$2);
            _servant.rebind(p0, p1);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void rebind_context(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {

        try {
            org.apache.geronimo.interop.CosNaming.NameComponent[] p0 = (org.apache.geronimo.interop.CosNaming.NameComponent[]) input.readObject(vt$1);
            org.apache.geronimo.interop.CosNaming.NamingContext p1 = (org.apache.geronimo.interop.CosNaming.NamingContext) input.readObject(vt$5);
            _servant.rebind_context(p0, p1);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void unbind(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {

        try {
            org.apache.geronimo.interop.CosNaming.NameComponent[] p0 = (org.apache.geronimo.interop.CosNaming.NameComponent[]) input.readObject(vt$1);
            _servant.unbind(p0);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void new_context(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        org.apache.geronimo.interop.CosNaming.NamingContext rc;

        try {
            rc = _servant.new_context();
            output.writeObject(vt$5, rc);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }

    public void bind_new_context(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        org.apache.geronimo.interop.CosNaming.NamingContext rc;

        try {
            org.apache.geronimo.interop.CosNaming.NameComponent[] p0 = (org.apache.geronimo.interop.CosNaming.NameComponent[]) input.readObject(vt$1);
            rc = _servant.bind_new_context(p0);
            output.writeObject(vt$5, rc);
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        } catch (java.lang.Error er) {
            er.printStackTrace();
        }
    }
}
