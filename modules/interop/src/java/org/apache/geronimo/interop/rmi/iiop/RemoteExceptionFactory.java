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

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.util.ExceptionUtil;


public abstract class RemoteExceptionFactory {
    public static java.rmi.RemoteException getException(Exception ex) {
        if (ex instanceof SystemException) {
            Throwable cause = ((SystemException) ex).getCause();
            if (cause instanceof org.omg.CORBA.SystemException) {
                org.omg.CORBA.SystemException corbaException = (org.omg.CORBA.SystemException) cause;
                if (ex.getMessage() == null) {
                    return new java.rmi.ServerException(corbaException.getClass().getName(), corbaException);
                } else {
                    return new java.rmi.ServerException(ex.getMessage(), corbaException);
                }
            }
        }
        return new java.rmi.RemoteException(ExceptionUtil.getStackTrace(ex));
    }
}
