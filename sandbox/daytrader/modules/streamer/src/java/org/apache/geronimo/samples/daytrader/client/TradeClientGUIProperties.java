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

import java.awt.event.*;
import javax.swing.*;

public class TradeClientGUIProperties extends JDialog implements ActionListener, WindowListener {
	private JTextField updateInterval, maxPerSecond;
	private JButton okButton;
	private TradeClient client;

	public TradeClientGUIProperties(TradeClient client, TradeClientGUI gui) {
		super(gui, true);
		this.client = client;

		JPanel buttonPanel = new JPanel();
		okButton = new JButton("OK");
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(okButton);
		okButton.addActionListener(this);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JLabel label1 = new JLabel("Update Interval:");
		updateInterval = new JTextField(String.valueOf(client.getUpdateInterval()), 3);
		panel.add(label1);
		panel.add(updateInterval);

		mainPanel.add(panel);

		mainPanel.add(buttonPanel);

		getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		addWindowListener(this);
		setTitle("Trade Streamer Client Configuration Properties");
	}

	private void updateUpdateInterval() {
		String udi = updateInterval.getText().trim();
		int udii = TradeClient.DEFAULT_UPDATE_INTERVAL;
		try {
			udii = Integer.parseInt(udi);
		}
		catch (NumberFormatException nfe) {
			updateInterval.setText(String.valueOf(udii));
		}
		client.setUpdateInterval(udii);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			updateUpdateInterval();
			setVisible(false);
		}
	}

	public void windowClosing(WindowEvent e) {
		updateUpdateInterval();
	}

	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

}
