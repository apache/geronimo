/**
 *
 * Copyright 2004 The Apache Software Foundation
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.jmxdebug.web.velocity;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.jmxdebug.web.beanlib.MBeanInfoHelper;
import org.apache.geronimo.jmxdebug.web.beanlib.MBeanServerHelper;
import org.apache.velocity.VelocityContext;

/**
 * Simple servlet for looking at mbeans
 *
 * @version $Rev$ $Date$
 */
public class DebugServlet extends BasicVelocityActionServlet {
    public static String OBJECT_NAME_FILTER_KEY = "ObjectNameFilter";

    protected String getActionVerb() {
        return "action";
    }

    /**
     * The only real action - just puts the mbean server helper in the
     * context, and if there was a mbean specified for details, shoves
     * a MBeanINfoHelper in the context
     */
    public void defaultAction(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String beanName = req.getParameter("MBeanName");
        String filterKey = req.getParameter(OBJECT_NAME_FILTER_KEY);

        if (filterKey == null || "".equals(filterKey)) {
            filterKey = "*:*";
        }

        VelocityContext vc = new VelocityContext();

        MBeanServerHelper kernelHelper = new MBeanServerHelper();
        vc.put("mbctx", kernelHelper);
        vc.put("encoder", new KickSunInHead());
        vc.put(OBJECT_NAME_FILTER_KEY, filterKey);

        if (beanName == null) {
            vc.put("template", "nobean.vm");
        } else {
            try {
                vc.put("beanInfo", new MBeanInfoHelper(kernelHelper, beanName));
            } catch (Exception e) {
                e.printStackTrace();
            }
            vc.put("template", "mbeaninfo.vm");
        }

        renderTemplate(req, res, vc, "index.vm");
    }

    public void unknownAction(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        defaultAction(req, res);
    }


    /**
     * Why oh why couldn't this be one class...
     */
    public class KickSunInHead {
        public String decode(String string) {
            return decode(string, "UTF-8");
        }

        public String decode(String string, String encoding) {
            if (string != null) {
                try {
                    return URLDecoder.decode(string, encoding);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        public String encode(String string) {
            return encode(string, "UTF-8");
        }

        public String encode(String string, String encoding) {
            if (string != null) {
                try {
                    return URLEncoder.encode(string, encoding);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

    }
}
