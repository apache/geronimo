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

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.util.Date;
import javax.interceptor.Interceptor;

//@Value1 @Interceptor
public class ValueIntcpt1 {

    @AroundInvoke
    public Object checkValid1(InvocationContext ctx)throws Exception {
        //System.out.println("Value Interceptor 1 invoked!");
        long start = System.currentTimeMillis();
        Object param[] = ctx.getParameters();
        if(param[0]==null)
           throw new Exception("!!!!input Value is null");
        else
        {
           double dv = Double.valueOf(param[0].toString());
           //System.out.println("in interceptor 1 , class obtained is:"+ ctx.getMethod().getName());
           String[] tmp=(String[])param[1];
           if(dv>=0.0)
           {
               tmp[0]="Valid";
           }
           else
           {
               tmp[0]="Invalid";
           }
            
            long tm=System.currentTimeMillis();
            tmp[2]=Long.toString(tm);
            tmp[1]=new Date(tm).toString();
            param[1]=tmp;
            
            ctx.setParameters(param);
            try{
            Thread.sleep(1500);//1.5 seconds
            }catch(Exception e)
            {
            }
          }
            return ctx.proceed();
    }
}
