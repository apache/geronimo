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
package org.apache.geronimo.interop;


public class SystemException extends RuntimeException {
    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable cause) {
        super(cause != null && cause instanceof SystemException
              && cause.getMessage() == null
              ? cause.getCause() : cause);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause != null && cause instanceof SystemException
                       && cause.getMessage() == null
                       ? cause.getCause() : cause);
    }

    /*
Constructor Summary 
RuntimeException() 
          Constructs a new runtime exception with null as its detail message. 
RuntimeException(String message) 
          Constructs a new runtime exception with the specified detail message. 
RuntimeException(String message, Throwable cause) 
          Constructs a new runtime exception with the specified detail message and cause. 
RuntimeException(Throwable cause) 
          Constructs a new runtime exception with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause). 
          */

}
