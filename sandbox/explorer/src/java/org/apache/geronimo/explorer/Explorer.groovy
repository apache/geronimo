package org.apache.geronimo.explorer

import groovy.swing.SwingBuilder

import java.awt.BorderLayout
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.swing.BorderFactory

class Explorer {

    property frame
    property swing
    property treeModel
    property viewManager
    property objectName
    
    static void main(args) { 
	    explorer = new Explorer()
	    explorer.run()   
	}
	
    void run() {
        swing = new SwingBuilder()
        viewManager = new ViewManager(this)
        
        frame = swing.frame(title:'Geronimo Explorer', location:[200,200], size:[800,400]) {
            menuBar {
		        menu(text:'Help') {
		            menuItem() {
		                action(name:'About', closure:{ showAbout() })
		            }
		        }
		    }
			split = splitPane() {
                scrollPane() {
					tree(model:owner.getTreeModel(), valueChanged:{event| onTreeSelection(event)})
                }
                p = panel()
                owner.viewManager.setPanel(p)
		    }
		    split.dividerLocation = 0.5
		}        
		frame.show()
    }
    
    createMBeanView(name) {
		objectName = name
        return swing.scrollPane(constraints:BorderLayout.CENTER) {
            vbox() {
                panel(border:BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), 'MBean Properties')) {
                    name = owner.getObjectName()
                    label(text:'domain')
                    textField(text:name.domain, editable:false)
                    
                    for (e in name.keyPropertyList) {
                        valueText = ""
                        if (e.value != null) {
                            valueText = e.value.toString()
                        }

                        label(text:e.key)
                        textField(text:valueText, editable:false)                    
                    }
                }
                panel(border:BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), 'MBean Attributes')) {
                    name = owner.getObjectName()
                    server = owner.getMBeanServer()
                    beanInfo = owner.getMBeanInfo()
                    attributes = beanInfo.attributes
                    list = []
                    for (attrInfo in attributes) {
                        attrName = attrInfo.getName()
                        description = attrInfo.getDescription()
                        value = server.getAttribute(name, attrName)
    
                        valueText = ""
                        if (value != null) {
                            valueText = value.toString()
                        }
                        label(text:attrName)
                        textField(text:valueText)                    
                    }
                }
                panel(border:BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), 'MBean Operations')) {
                    beanInfo = owner.getMBeanInfo()
                    operations = beanInfo.operations
                    list = []
                    for (opInfo in operations) {
                        opName = opInfo.getName()
                        description = opInfo.getDescription()

                        label(text:opName)
                        
                        /** @todo this will only work when local variables are passed into closures */
                        //button(text:'Call', actionListener:{ owner.callMBeanOperation(opName) })                    
                        button(text:'Call')                    
                    }
                }
            }
        }
    }
    
	onTreeSelection(event) {
	    path = event.path
	    System.out.println("Selected: " + path)
	    
	    node = path.lastPathComponent
	    
        System.out.println("node: " + node)

		viewManager.setSelectedTreeNode(node)
	}
	
	getMBeanServer() {
	    return getTreeModel().getMBeanServer()
	}
	
	getMBeanInfo() {
        server = getMBeanServer()
        return server.getMBeanInfo(objectName)
	}

    showAbout() {
 		pane = swing.optionPane(message:'This program is a Swing user interface for introspecting Geronimo servers')
 		dialog = pane.createDialog(frame, 'About Geronimo Explorer')
 		dialog.show()
    }
}
