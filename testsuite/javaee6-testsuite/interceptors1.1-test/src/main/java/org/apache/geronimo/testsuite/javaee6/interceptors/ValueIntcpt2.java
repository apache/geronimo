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
import java.util.Date;

public class ValueIntcpt2 {

    @AroundInvoke
    public Object checkValid2(InvocationContext ctx)throws Exception {
        
        long start = System.currentTimeMillis();
        Object param[] = ctx.getParameters();
        if(param[0]==null)
        	throw new Exception("!!!!input Value is null");
        else
        {
           double dv = Double.valueOf(param[0].toString());
           
           String[] tmp=(String[])param[1];
           
           if(dv<0.0)
           {
               tmp[3]="Valid";
           }
           else
           {
               tmp[3]="Invalid";
           }

            long tm=System.currentTimeMillis();
            
            tmp[5]=Long.toString(tm);
            tmp[4]=new Date(tm).toString();
            param[1]=tmp;
            //System.out.println("in interceptor 2 , class obtained is:"+ ctx.getMethod().getName());
            ctx.setParameters(param);
            }
            return ctx.proceed();



    }
}
