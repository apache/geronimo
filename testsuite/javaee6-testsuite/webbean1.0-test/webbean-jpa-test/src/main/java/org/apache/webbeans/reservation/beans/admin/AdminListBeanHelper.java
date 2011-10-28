/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
package org.apache.webbeans.reservation.beans.admin;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.faces.component.html.HtmlDataTable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.webbeans.reservation.controller.admin.AdminController;
import org.apache.webbeans.reservation.entity.Hotel;
import org.apache.webbeans.reservation.util.JSFUtility;

@Named
@RequestScoped
public class AdminListBeanHelper {
    private HtmlDataTable model;
    
    private @Inject AdminListBean adminListBean;

    private @Inject @Default AdminController controller;

    /**
     * @return the model
     */
    public HtmlDataTable getModel()
    {
        return model;
    }
    
    /**
     * @param model the model to set
     */
    public void setModel(HtmlDataTable model)
    {
        this.model = model;
    }
    
    public String getForUpdate()
    {
        //System.out.println("enter AdminListBean.getForUpdate");
        Hotel hotel = (Hotel) model.getRowData();
        
        adminListBean.setSelected(hotel);
        
        adminListBean.setRenderedDetailPanel(true);
        
                
        return null;
    }
    
    public String delete()
    {
        Hotel selected = (Hotel)model.getRowData();
        
        if(selected == null)
        {
            JSFUtility.addErrorMessage("Pleasee select the hotel to delete", "");
            
            return null;
        }
     
        controller.deleteHotel(selected.getId());
        
        JSFUtility.addInfoMessage("Hotel with name " + selected.getName()+ " is succesfully deleted." , "");
        
        if (adminListBean.getSelected() != null) {
            adminListBean.getSelected().setCity(null);
            adminListBean.getSelected().setCountry(null);
            adminListBean.getSelected().setName(null);
            adminListBean.getSelected().setStar(0);
        }
  
        return null;
    }

}
