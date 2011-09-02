package org.apache.webbeans.samples.conversation;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIData;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class ShoppingBeanHelper {

    private UIData uiTable;

    private @Inject ShoppingBean shopingBean;

    /**
     * @return the uiTable
     */
    public UIData getUiTable() {
        return uiTable;
    }

    /**
     * @param uiTable
     *            the uiTable to set
     */
    public void setUiTable(UIData uiTable) {
        this.uiTable = uiTable;
    }

    public String buy() {
        Item item = (Item) uiTable.getRowData();
        this.shopingBean.getItems().add(item);

        return null;
    }

}
