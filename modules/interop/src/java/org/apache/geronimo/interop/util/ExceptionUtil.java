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
package org.apache.geronimo.interop.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.interop.CheckedException;
import org.apache.geronimo.interop.SystemException;


public abstract class ExceptionUtil {
    public static List addException(List exceptions, Throwable ex) {
        if (exceptions == null) {
            exceptions = new ArrayList(1);
        }
        exceptions.add(ex);
        return exceptions;
    }

    public static void checkExceptions(List exceptions) {
        if (exceptions != null) {
            int n = exceptions.size();
            if (n == 1) {
                Throwable ex = (Throwable) exceptions.get(0);
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new SystemException(ex);
            } else {
                StringBuffer buffer = new StringBuffer();
                for (Iterator i = exceptions.iterator(); i.hasNext();) {
                    Exception ex = (Exception) i.next();
                    if (buffer.length() > 0) {
                        buffer.append("\n______________________________________________________________\n\n");
                    }
                    buffer.append(ExceptionUtil.getStackTrace(ex));
                }
                throw new SystemException(buffer.toString());
            }
        }
    }

    public static String getDivider() {
        return "\n        ______________________________________________________________\n";
    }

    /**
     * * Construct a detail message for an exception which doesn't take a
     * * cause parameter in its constructor.
     */
    public static String causedBy(Throwable ex) {
        return "\nCaused by: " + getStackTrace(ex) + getDivider();
    }

    public static String causedBy(String stackTrace)
    {
        return "\nCaused by: " + getTraceLines(stackTrace);
    }

    public static Throwable getCause(Throwable ex)
    {
        for (;;)
        {
            if (ex instanceof SystemException)
            {
                SystemException se = (SystemException)ex;
                if (se.getCause() != null && se.getMessage() == null)
                {
                    ex = se.getCause();
                }
                else
                {
                    break;
                }
            }
            else
            {
                break;
            }
        }
        return ex;
    }

    public static String getCauseChain(Throwable ex) {
        String stackTrace = getStackTrace(ex);
        return getCauseChain(stackTrace);
    }

    public static String getCauseChain(String stackTrace) {
        try {
            BufferedReader input = new BufferedReader(new StringReader(stackTrace));
            StringBuffer output = new StringBuffer(100);
            String line;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("at ") && !line.startsWith("... ")) {
                    output.append(line);
                    output.append('\n');
                }
            }
            return output.toString();
        } catch (Exception ex2) {
            ex2.printStackTrace();
            return stackTrace;
        }
    }

    public static String getStackTrace(Throwable ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString().trim();
    }

    public static String getTraceLines(String stackTrace)
    {
        try
        {
            BufferedReader input = new BufferedReader(new StringReader(stackTrace));
            StringBuffer output = new StringBuffer(100);
            String line;
            boolean first = true;
            while ((line = input.readLine()) != null)
            {
                line = line.trim();
                if (line.length() != 0)
                {
                    if (! first)
                    {
                        output.append("| ");
                    }
                    first = false;
                    output.append(line);
                    output.append('\n');
                }
            }
            return output.toString();
        }
        catch (Exception ex2)
        {
            ex2.printStackTrace();
            return stackTrace;
        }
    }

    public static String getCurrentStackTrace() {
        return StringUtil.removePrefix(getStackTrace(new Exception()), "java.lang.Exception:");
    }

    public static String indentLines(String lines) {
        return "    " + StringUtil.replace(lines.trim(), "\n", "\n    ");
    }

    public static boolean isApplicationException(Throwable ex) {
        return !isSystemException(ex);
    }

    public static boolean isSystemException(Throwable ex) {
        Class exClass = ex.getClass();
        return Error.class.isAssignableFrom(exClass)
               || RuntimeException.class.isAssignableFrom(exClass);
    }

    public static boolean isUserException(Class exClass) {
        if (RuntimeException.class.isAssignableFrom(exClass)
            || Error.class.isAssignableFrom(exClass)) {
            return false;
        }
        return true;
    }

    public static RuntimeException getRuntimeException(Exception ex) {
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        } else {
            return new SystemException(ex);
        }
    }

    public static RuntimeException rethrow(Throwable ex) {
        if (ex instanceof Error) {
            throw (Error) ex;
        } else if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        } else {
            return new CheckedException(ex);
        }
    }
}
