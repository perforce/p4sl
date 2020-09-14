package com.perforce.p4simulink.ui;

import java.awt.Dimension;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary.ClientLineEnd;
import com.perforce.p4java.client.IClientSummary.IClientOptions;
import com.perforce.p4java.client.IClientSummary.IClientSubmitOptions;
import com.perforce.p4java.client.IClientViewMapping;
import com.perforce.p4java.impl.generic.client.ClientOptions;
import com.perforce.p4java.impl.generic.client.ClientSubmitOptions;
import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.impl.generic.client.ClientView.ClientViewMapping;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4simulink.connection.P4ConfigType;
import com.perforce.p4simulink.connection.P4Uri;

public class P4ClientPanel extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final ResourceBundle resources = ResourceBundle.getBundle("Labels");

	private JLabel nameLabel = new JLabel("Workspace Name: ");
	private JLabel rootLabel = new JLabel("Workspace Root: ");
	private JLabel ownerLabel = new JLabel("Owner: ");
	private JLabel hostLabel = new JLabel("Host: ");
	private JLabel streamLabel = new JLabel("Stream: ");
	private JLabel changeLabel = new JLabel("Stream at change: ");
	private JLabel viewLabel = new JLabel("View Mapping: ");

	private JTextField nameText;
	private JTextField rootText;
	private JTextField ownerText;
	private JTextField hostText;
	private JTextField streamText;
	private JTextField changeText;

	private JTextArea viewArea;
	private JScrollPane scrollArea;

	private JCheckBox allwrite = new JCheckBox("AllWrite");
	private JCheckBox clobber = new JCheckBox("Clobber");
	private JCheckBox compress = new JCheckBox("Compress");
	private JCheckBox locked = new JCheckBox("Locked");
	private JCheckBox modtime = new JCheckBox("Modtime");
	private JCheckBox rmdir = new JCheckBox("RmDir");

	private JComboBox<String> submitCB;
	private JComboBox<ClientLineEnd> lineendCB;

	public P4ClientPanel(P4Uri uri, File sandboxRoot) {
		initUI();

		String clientName = uri.get(P4ConfigType.P4CLIENT);
		nameText.setText(clientName);

		String owner = uri.get(P4ConfigType.P4USER);
		ownerText.setText(owner);

		String depotPath = uri.getDepotPath();
		setView(depotPath, clientName);

		String root = sandboxRoot.getAbsolutePath();
		rootText.setText(root);
	}

	public P4ClientPanel(IClient client) {
		initUI();

		nameText.setText(client.getName());
		rootText.setText(client.getRoot());
		ownerText.setText(client.getOwnerName());
		hostText.setText(client.getHostName());

		IClientOptions o = client.getOptions();
		allwrite.setSelected(o.isAllWrite());
		clobber.setSelected(o.isClobber());
		compress.setSelected(o.isCompress());
		locked.setSelected(o.isLocked());
		modtime.setSelected(o.isModtime());
		rmdir.setSelected(o.isRmdir());

		IClientSubmitOptions s = client.getSubmitOptions();
		submitCB.setSelectedItem(s.toString());
		lineendCB.setSelectedItem(client.getLineEnd());

		streamText.setText(client.getStream());

		int c = client.getStreamAtChange();
		if (c > 0) {
			changeText.setText(String.valueOf(c));
		} else {
			changeText.setText("");
		}

		ClientView view = client.getClientView();
		StringBuffer sb = new StringBuffer();
		for (IClientViewMapping v : view.getEntryList()) {
			sb.append(v.getLeft());
			sb.append("\t");
			sb.append(v.getRight());
			sb.append("\n");
		}
		viewArea.setText(sb.toString());
	}

	private void initUI() {
		nameText = new JTextField();
		nameText.setEnabled(false);
		rootText = new JTextField();
		ownerText = new JTextField();
		ownerText.setEnabled(false);
		hostText = new JTextField();

		streamText = new JTextField();
		changeText = new JTextField();

		viewArea = new JTextArea(8, 30);
		viewArea.setLineWrap(false);

		scrollArea = new JScrollPane(viewArea);
		scrollArea
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		submitCB = new JComboBox<String>();
		submitCB.addItem(ClientSubmitOptions.LEAVE_UNCHANGED);
		submitCB.addItem(ClientSubmitOptions.REVERT_UNCHANGED);
		submitCB.addItem(ClientSubmitOptions.SUBMIT_UNCHANGED);
		submitCB.addItem(ClientSubmitOptions.LEAVE_UNCHANGED_REOPEN);
		submitCB.addItem(ClientSubmitOptions.REVERT_UNCHANGED_REOPEN);
		submitCB.addItem(ClientSubmitOptions.SUBMIT_UNCHANGED_REOPEN);

		lineendCB = new JComboBox<ClientLineEnd>();
		for (ClientLineEnd item : ClientLineEnd.values()) {
			lineendCB.addItem(item);
		}
	}

	private void setView(String depotPath, String clientName) {
		if (depotPath.isEmpty()) {
			depotPath = "depot";
		}

		StringBuffer sb = new StringBuffer();
		sb.append("//" + depotPath + "/...");
		sb.append("\t");
		sb.append("//" + clientName + "/...");

		viewArea.setText(sb.toString());
	}

	private JPanel basicLayout() {
		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(500, 20));

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(nameLabel);
		h1.addComponent(rootLabel);
		h1.addComponent(ownerLabel);
		h1.addComponent(hostLabel);
		ParallelGroup h2 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h2.addComponent(nameText);
		h2.addComponent(rootText);
		h2.addComponent(ownerText);
		h2.addComponent(hostText);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		hGroup.addGroup(h2);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(nameLabel);
		v1.addComponent(nameText);
		ParallelGroup v2 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(rootLabel);
		v2.addComponent(rootText);
		ParallelGroup v3 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v3.addComponent(ownerLabel);
		v3.addComponent(ownerText);
		ParallelGroup v4 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v4.addComponent(hostLabel);
		v4.addComponent(hostText);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		vGroup.addGroup(v2);
		vGroup.addGroup(v3);
		vGroup.addGroup(v4);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	private JPanel advancedLayout() {
		JPanel panel = new JPanel();

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(allwrite);
		ParallelGroup h2 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h2.addComponent(clobber);
		ParallelGroup h3 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h3.addComponent(compress);
		ParallelGroup h4 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h4.addComponent(locked);
		ParallelGroup h5 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h5.addComponent(modtime);
		ParallelGroup h6 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h6.addComponent(rmdir);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		hGroup.addGroup(h2);
		hGroup.addGroup(h3);
		hGroup.addGroup(h4);
		hGroup.addGroup(h5);
		hGroup.addGroup(h6);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(allwrite);
		v1.addComponent(clobber);
		v1.addComponent(compress);
		v1.addComponent(locked);
		v1.addComponent(modtime);
		v1.addComponent(rmdir);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	private JPanel optionsLayout() {
		JPanel panel = new JPanel();

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(submitCB);
		ParallelGroup h2 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h2.addComponent(lineendCB);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		hGroup.addGroup(h2);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(submitCB);
		v1.addComponent(lineendCB);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	private JPanel streamLayout() {
		JPanel panel = new JPanel();

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(streamLabel);
		h1.addComponent(changeLabel);
		ParallelGroup h2 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h2.addComponent(streamText);
		h2.addComponent(changeText);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		hGroup.addGroup(h2);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(streamLabel);
		v1.addComponent(streamText);
		ParallelGroup v2 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(changeLabel);
		v2.addComponent(changeText);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		vGroup.addGroup(v2);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	public JPanel createLayout() {
		JPanel panel = new JPanel();

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		JPanel basic = basicLayout();
		JPanel advanced = advancedLayout();
		JPanel options = optionsLayout();
		JPanel stream = streamLayout();

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(basic);
		h1.addComponent(advanced);
		h1.addComponent(options);
		h1.addComponent(stream);
		h1.addComponent(viewLabel);
		h1.addComponent(scrollArea);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(basic);
		ParallelGroup v2 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(advanced);
		ParallelGroup v3 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v3.addComponent(options);
		ParallelGroup v4 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v4.addComponent(stream);
		ParallelGroup v5 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v5.addComponent(viewLabel);
		ParallelGroup v6 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v6.addComponent(scrollArea);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		vGroup.addGroup(v2);
		vGroup.addGroup(v3);
		vGroup.addGroup(v4);
		vGroup.addGroup(v5);
		vGroup.addGroup(v6);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	public Client getClient() {
		Client client = new Client();

		client.setName(nameText.getText());
		client.setRoot(rootText.getText());
		client.setOwnerName(ownerText.getText());
		client.setHostName(hostText.getText());

		ClientOptions options = new ClientOptions();
		options.setAllWrite(allwrite.isSelected());
		options.setClobber(clobber.isSelected());
		options.setCompress(compress.isSelected());
		options.setLocked(locked.isSelected());
		options.setModtime(modtime.isSelected());
		options.setRmdir(rmdir.isSelected());
		client.setOptions(options);

		String submitOpt = (String) submitCB.getSelectedItem();
		client.setSubmitOptions(new ClientSubmitOptions(submitOpt));
		client.setLineEnd((ClientLineEnd) lineendCB.getSelectedItem());

		client.setStream(streamText.getText());

		try {
			int change = Integer.parseInt(changeText.getText());
			client.setStreamAtChange(change);
		} catch (NumberFormatException e) {
		}

		ClientView clientView = new ClientView();
		int order = 0;
		for (String line : viewArea.getText().split("\\n")) {
			try {
				ClientViewMapping entry = new ClientViewMapping(order, line);
				order++;
				clientView.addEntry(entry);
			} catch (Exception e) {
			}
		}
		client.setClientView(clientView);

		return client;
	}

	public static void main(String[] args) throws Exception {
		String uriStr = "p4://P4CLIENT=myClient;pallen@localhost:1666/files/depot/main";
		P4Uri uri = new P4Uri(uriStr);
		File cwd = new File("");
		P4ClientPanel s = new P4ClientPanel(uri, cwd);
		JOptionPane.showConfirmDialog(null, s.createLayout(), resources.getString("workspace.label"), JOptionPane.OK_CANCEL_OPTION);
	}

}
