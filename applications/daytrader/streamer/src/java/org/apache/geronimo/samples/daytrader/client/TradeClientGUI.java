/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable 
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

package org.apache.geronimo.samples.daytrader.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.apache.geronimo.samples.daytrader.client.table.*;

public class TradeClientGUI extends JFrame implements ActionListener, WindowListener {
	private JMenuItem resetItem;
	private JMenuItem exitItem;
	private JMenuItem propItem;
	private TradeClient client;
	private JTextField statusMsg;
	private JLabel webLabel, ejbLabel;
	private TradeClientGUIProperties props;

	private static final String TRADELOGO_FILENAME = "/images/tradeLogoSmall.gif";
	private static final String WEBSPHERELOGO_FILENAME = "/images/WEBSPHERE_18P_UNIX.GIF";

	public TradeClientGUI(TradeClient client) {
		this.client = client;
		
		JTabbedPane overallPanel = new JTabbedPane();

		// Panel 1 - Streaming quotes
		JPanel streamerPanel = new JPanel();
		streamerPanel.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
		JPanel topBarPanel = new JPanel();
		topBarPanel.setLayout(new BoxLayout(topBarPanel, BoxLayout.X_AXIS));
		JPanel topImagePanel = new JPanel();
		topImagePanel.setLayout(new BorderLayout());

		statusMsg = new JTextField("");

		SortableTable auditTable = new SortableTable(client.getAuditStats());
		auditTable.setDefaultRenderer(ChangeModel.class, new ChangeRenderer());
		auditTable.setDefaultRenderer(AuditModel.class, new AuditRenderer());
		auditTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane scrollpane1 = new JScrollPane(auditTable);

		ImageIcon iconTrade = new ImageIcon(this.getClass().getResource(TRADELOGO_FILENAME));
		ImageIcon iconWS = new ImageIcon(this.getClass().getResource(WEBSPHERELOGO_FILENAME));

		topImagePanel.add(new JLabel(iconTrade), BorderLayout.WEST);
		topImagePanel.add(new JLabel(iconWS), BorderLayout.EAST);

		streamerPanel.setLayout(new BorderLayout());
		streamerPanel.add(topImagePanel, BorderLayout.NORTH);
		streamerPanel.add(scrollpane1, BorderLayout.CENTER);
		streamerPanel.add(statusMsg, BorderLayout.SOUTH);
        
		// Overall Frame
		setTitle("Trade Client Application");
		addWindowListener(this);

		JMenuBar jmb = new JMenuBar();
		JMenu file = new JMenu ("File");
		exitItem = new JMenuItem("Exit");
		resetItem = new JMenuItem("Reset");
		propItem = new JMenuItem("Properties");
    file.add(exitItem);
		file.add(resetItem);
		file.add(propItem);
    exitItem.addActionListener(this);
		resetItem.addActionListener(this);
    propItem.addActionListener(this);
		jmb.add(file);
		setJMenuBar(jmb);

		overallPanel.addTab("Streamer", streamerPanel);
		getContentPane().add(overallPanel, java.awt.BorderLayout.CENTER);
		setSize(800, 600);

		props = new TradeClientGUIProperties(client, this);
		props.pack();

		setVisible(true);
	}

	public void updateStatusMessage(String message) {
		statusMsg.setText(message);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == resetItem) {
			try {
				client.reset();
			}
			catch (Exception ex)	{
				System.err.println("Caught an unexpected exception!");
				ex.printStackTrace();
			}
		}
		if (e.getSource() == propItem) {
			props.setVisible(true);
		}
		if (e.getSource() == exitItem) {
			client.closeClient();
		}
	}

	public void windowClosing(WindowEvent e) {
		client.closeClient();
	}

	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
}
