package com.perforce.p4simulink.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class P4PopulatePanel implements ActionListener {

	public enum SyncType {
		LATEST, REVISION;
	}

	private SyncType type;
	private JLabel pathLabel;
	private JTextField pathText;
	private JTextField revisionText;

	JRadioButton latestRadio;
	JRadioButton revisionRadio;
	ButtonGroup group;

	private JCheckBox forceCheck = new JCheckBox("Force Operation (p4 sync -f)");
	private JCheckBox safeCheck = new JCheckBox("Safe Update (p4 sync -s)");

	public P4PopulatePanel(String path) {
		pathLabel = new JLabel("Update the following path:");
		pathText = new JTextField(path);

		latestRadio = new JRadioButton("Get latest revision.");
		latestRadio.addActionListener(this);
		latestRadio.setActionCommand(SyncType.LATEST.name());
		latestRadio.setSelected(true);

		revisionRadio = new JRadioButton("Specify a revision:");
		revisionRadio.addActionListener(this);
		revisionRadio.setActionCommand(SyncType.REVISION.name());

		group = new ButtonGroup();
		group.add(latestRadio);
		group.add(revisionRadio);

		revisionText = new JTextField();
		revisionText.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		type = SyncType.valueOf(action);

		switch (type) {
		case LATEST:
		default:
			revisionText.setEnabled(false);
			break;

		case REVISION:
			revisionText.setEnabled(true);
			break;
		}

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
		h1.addComponent(latestRadio);
		h1.addComponent(revisionRadio);
		ParallelGroup h2 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h2.addComponent(revisionText);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		hGroup.addGroup(h2);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(latestRadio);
		ParallelGroup v2 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(revisionRadio);
		v2.addComponent(revisionText);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		vGroup.addGroup(v2);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	private JPanel advancedLayout() {
		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(500, 20));

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		Border blackline = BorderFactory
				.createTitledBorder("Advanced Sync Options");
		panel.setBorder(blackline);

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(forceCheck);
		h1.addComponent(safeCheck);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(forceCheck);
		ParallelGroup v2 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(safeCheck);

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

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(pathLabel);
		h1.addComponent(pathText);
		h1.addComponent(basic);
		h1.addComponent(advanced);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(pathLabel);
		ParallelGroup v2 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(pathText);
		ParallelGroup v3 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v3.addComponent(basic);
		ParallelGroup v4 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v4.addComponent(advanced);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		vGroup.addGroup(v2);
		vGroup.addGroup(v3);
		vGroup.addGroup(v4);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	public String getPath() {
		return pathText.getText();
	}

	public String getRev() {
		StringBuffer sb = new StringBuffer();
		if (type == SyncType.REVISION) {
			String rev = revisionText.getText();
			if (!rev.startsWith("@")) {
				sb.append("@");
			}
			sb.append(rev);
		}
		return sb.toString();
	}

	public boolean isForce() {
		return forceCheck.isSelected();
	}

	public boolean isSafe() {
		return safeCheck.isSelected();
	}

	public static void main(String[] args) throws Exception {
		P4PopulatePanel s = new P4PopulatePanel("//...");
		JOptionPane.showConfirmDialog(null, s.createLayout(), "Get Revisons",
				JOptionPane.OK_CANCEL_OPTION);
	}

}
