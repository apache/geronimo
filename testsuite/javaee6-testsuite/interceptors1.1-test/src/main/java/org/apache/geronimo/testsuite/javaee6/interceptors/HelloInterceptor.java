/**
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.apache.geronimo.testsuite.javaee6.interceptors;

import javax.interceptor.Interceptor;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ejb.EJB;

@Hello @Interceptor
public class HelloInterceptor {
    @EJB
    private org.apache.geronimo.testsuite.javaee6.beans.msgBean msgb;

    @AroundInvoke
    public Object sayHello(InvocationContext ic) throws Exception {
        msgb.setOutput("invoke @Interceptor, @InterceptorBinding!");
        //System.out.println("Hello from:" + ic.getMethod().getName());
        return ic.proceed();
    }
}