package com.perforce.p4simulink.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.impl.generic.core.ChangelistSummary;

public class P4PendingPanel extends JFrame {

	private static final long serialVersionUID = 1L;

	private JLabel selectLabel;
	private JScrollPane tablePane;
	private String[][] tableData;

	private int change;

	public P4PendingPanel(int change, List<IChangelistSummary> pending) {
		this.change = change;

		selectLabel = new JLabel();
		selectLabel.setText("Select active pending changelist:");

		String[] columnNames = { "Change", "State", "Description" };
		tableData = getData(pending);

		JTable table = new JTable(tableData, columnNames);
		ListSelectionModel listSelectionModel = table.getSelectionModel();
		listSelectionModel
				.addListSelectionListener(new SharedListSelectionHandler());
		table.setSelectionModel(listSelectionModel);
		table.setAutoCreateRowSorter(true);

		// http://stackoverflow.com/questions/17627431/auto-resizing-the-jtable-column-widths
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 80; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width, width);
			}
			columnModel.getColumn(column).setPreferredWidth(width);
		}

		tablePane = new JScrollPane(table);
	}

	public int getChange() {
		return change;
	}

	private String[][] getData(List<IChangelistSummary> pending) {
		String[][] data = new String[pending.size() + 1][3];

		// add default change to top of the table
		data[0][0] = "0";
		data[0][1] = "pending";
		data[0][2] = "Default Changelist";

		int col = 1;
		for (IChangelistSummary p : pending) {
			data[col][0] = Integer.toString(p.getId());
			data[col][1] = p.getStatus().name().toLowerCase();
			data[col][2] = p.getDescription();
			col++;
		}
		return data;
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
		h1.addComponent(selectLabel);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(selectLabel);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	public JPanel createLayout() {
		JPanel panel = new JPanel();

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		JPanel basic = basicLayout();

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(basic);
		h1.addComponent(tablePane);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(basic);
		ParallelGroup v2 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(tablePane);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		vGroup.addGroup(v2);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	public static void main(String[] args) throws Exception {
		List<IChangelistSummary> pending = new ArrayList<IChangelistSummary>();

		IChangelistSummary c = new ChangelistSummary();
		c.setId(123);
		c.setStatus(ChangelistStatus.PENDING);
		c.setDescription("A description of at lease 80 character long padding padding and lots more padding");
		pending.add(c);

		P4PendingPanel s = new P4PendingPanel(0, pending);
		JOptionPane.showConfirmDialog(null, s.createLayout(),
				"Select Pending change", JOptionPane.OK_CANCEL_OPTION);
	}

	class SharedListSelectionHandler implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int pos = e.getFirstIndex();
			change = Integer.parseInt(tableData[pos][0]);
		}

	}

}
