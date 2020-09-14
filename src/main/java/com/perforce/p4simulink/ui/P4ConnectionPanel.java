package com.perforce.p4simulink.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import com.perforce.p4simulink.P4CMException;
import com.perforce.p4simulink.connection.P4Config;
import com.perforce.p4simulink.connection.P4ConfigType;
import com.perforce.p4simulink.connection.P4ConnectionFactory;
import com.perforce.p4simulink.connection.P4Uri;
import com.perforce.p4simulink.connection.P4UriType;

public class P4ConnectionPanel extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final ResourceBundle resources = ResourceBundle.getBundle("Labels");

	private JTextField pathText;
	private JTextField userText;
	private JTextField portText;
	private JTextField workText;
	private JTextField charsetText;
	private JTextField browseText;

	private JLabel pathLabel = new JLabel("URI: ");
	private JLabel userLabel = new JLabel("Username: ");
	private JLabel portLabel = new JLabel("Server: ");
	private JLabel workLabel = new JLabel("Workspace: ");
	private JLabel charsetLabel = new JLabel("Charset: ");

	private JButton genButton;

	private JFileChooser browseFC;
	private JButton browseButton;

	private JButton validateButton;
	private JTextField validateText;

	public P4ConnectionPanel(String currentUri) {

		pathText = new JTextField(currentUri);
		userText = new JTextField();
		portText = new JTextField();
		workText = new JTextField();
		charsetText = new JTextField();

		genButton = new JButton("Connect");
		URL connUrl = getClass().getResource("/icons/connection_icon_wide.png");
		ImageIcon connIcon = new ImageIcon(connUrl);
		genButton.setIcon(connIcon);

		genButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Properties cfg = new Properties();
				cfg.setProperty(P4ConfigType.P4PORT.name(), portText.getText());
				cfg.setProperty(P4ConfigType.P4USER.name(), userText.getText());
				cfg.setProperty(P4ConfigType.P4CLIENT.name(),
						workText.getText());
				cfg.setProperty(P4ConfigType.P4CHARSET.name(), charsetText.getText());

				try {
					P4Uri uri = new P4Uri(cfg);
					pathText.setText(uri.getUri(P4UriType.P4));
					validateConnection();
				} catch (P4CMException e) {
					// no update
				}
			}
		});

		browseFC = new JFileChooser();
		browseFC.setFileFilter(new P4ConfigFileFilter());
		browseFC.setFileHidingEnabled(false);

		browseText = new JTextField();
		browseText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				File file = new File(browseText.getText());
				browseFC.setSelectedFile(file);
				try {
					P4Config p4 = new P4Config(file.toPath());
					pathText.setText(p4.toUri());
					validateConnection();
				} catch (P4CMException e) {
					// no update
				}
			}
		});

		browseButton = new JButton("Browse");
		URL dirUrl = getClass().getResource("/icons/depot_tree_tab_icon.png");
		ImageIcon dirIcon = new ImageIcon(dirUrl);
		browseButton.setIcon(dirIcon);

		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int ret = browseFC.showOpenDialog(P4ConnectionPanel.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = browseFC.getSelectedFile();
					browseText.setText(file.getAbsolutePath());
					try {
						P4Config p4 = new P4Config(file.toPath());
						pathText.setText(p4.toUri());
						validateConnection();
					} catch (P4CMException e) {
						// no update
					}

				}
			}
		});

		validateText = new JTextField("Unverified connection");
		validateText.setBorder(BorderFactory.createEmptyBorder());
		validateText.setEditable(false);
		validateText.setBackground(getBackground());
		validateText.setForeground(Color.RED);

		validateButton = new JButton("Validate");
		URL verifyUrl = getClass().getResource("/icons/goto_icon.png");
		ImageIcon verifyIcon = new ImageIcon(verifyUrl);
		validateButton.setIcon(verifyIcon);

		validateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				validateConnection();
			}
		});
	}

	public String getPath() {
		return pathText.getText();
	}

	public JPanel createLayout() {
		JPanel panel = new JPanel();

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		JPanel connect = connectionLayout();
		JPanel browse = browseLayout();
		JPanel uri = uriLayout();

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(connect);
		h1.addComponent(browse);
		h1.addComponent(uri);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(connect);
		ParallelGroup v2 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(browse);
		ParallelGroup v3 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v3.addComponent(uri);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		vGroup.addGroup(v2);
		vGroup.addGroup(v3);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	private JPanel connectionLayout() {
		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(500, 170));

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		Border blackline = BorderFactory
				.createTitledBorder("Provide Connection Details");
		panel.setBorder(blackline);

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(portLabel);
		h1.addComponent(userLabel);
		h1.addComponent(workLabel);
		h1.addComponent(charsetLabel);
		ParallelGroup h2 = group
				.createParallelGroup(GroupLayout.Alignment.TRAILING);
		h2.addComponent(portText);
		h2.addComponent(userText);
		h2.addComponent(workText);
		h2.addComponent(charsetText);
		h2.addComponent(genButton);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		hGroup.addGroup(h2);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(portLabel);
		v1.addComponent(portText);
		ParallelGroup v2 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(userLabel);
		v2.addComponent(userText);
		ParallelGroup v3 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v3.addComponent(workLabel);
		v3.addComponent(workText);
		ParallelGroup v4 = group.
				createParallelGroup(GroupLayout.Alignment.BASELINE);
		v4.addComponent(charsetLabel);
		v4.addComponent(charsetText);

		ParallelGroup v5 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v5.addComponent(genButton);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		vGroup.addGroup(v2);
		vGroup.addGroup(v3);
		vGroup.addGroup(v4);
		vGroup.addGroup(v5);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	private JPanel browseLayout() {
		JPanel panel = new JPanel();

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		Border blackline = BorderFactory
				.createTitledBorder("(or) Browse for a P4CONFIG");
		panel.setBorder(blackline);

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(browseText);
		ParallelGroup h2 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h2.addComponent(browseButton);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		hGroup.addGroup(h2);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(browseText);
		v1.addComponent(browseButton);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	private JPanel uriLayout() {
		JPanel panel = new JPanel();

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		JPanel validate = validateLayout();
		JPanel path = pathLayout();

		Border blackline = BorderFactory
				.createTitledBorder(resources.getString("validate.uri.label"));
		panel.setBorder(blackline);

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.LEADING);
		h1.addComponent(path);
		h1.addComponent(validate);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(path);
		ParallelGroup v2 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(validate);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		vGroup.addGroup(v2);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	private JPanel validateLayout() {
		JPanel panel = new JPanel();

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.TRAILING);
		h1.addComponent(validateText);
		ParallelGroup h2 = group
				.createParallelGroup(GroupLayout.Alignment.TRAILING);
		h2.addComponent(validateButton);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		hGroup.addGroup(h2);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(validateText);
		v1.addComponent(validateButton);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	private JPanel pathLayout() {
		JPanel panel = new JPanel();

		GroupLayout group = new GroupLayout(panel);
		panel.setLayout(group);

		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);

		ParallelGroup h1 = group
				.createParallelGroup(GroupLayout.Alignment.TRAILING);
		h1.addComponent(pathLabel);
		ParallelGroup h2 = group
				.createParallelGroup(GroupLayout.Alignment.TRAILING);
		h2.addComponent(pathText);

		SequentialGroup hGroup = group.createSequentialGroup();
		hGroup.addGroup(h1);
		hGroup.addGroup(h2);
		group.setHorizontalGroup(hGroup);

		ParallelGroup v1 = group
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v1.addComponent(pathLabel);
		v1.addComponent(pathText);

		SequentialGroup vGroup = group.createSequentialGroup();
		vGroup.addGroup(v1);
		group.setVerticalGroup(vGroup);

		return panel;
	}

	private void validateConnection() {
		try {
			String path = pathText.getText();
			P4Uri uri = new P4Uri(path);
			String msg = P4ConnectionFactory.validate(uri);

			if ("OK".equals(msg)) {
				validateText.setText(resources.getString("connection.verified.label"));
				validateText.setForeground(new Color(0, 128, 0));
			} else {
				validateText.setText(msg);
				validateText.setForeground(Color.RED);
			}

		} catch (P4CMException e) {
			validateText.setText(e.getMessage());
			validateText.setForeground(Color.RED);
		}
	}

	public static void main(String[] args) throws Exception {
		P4ConnectionPanel s = new P4ConnectionPanel("");
		JOptionPane.showConfirmDialog(null, s.createLayout(),
				resources.getString("connection.label"), JOptionPane.OK_CANCEL_OPTION);
	}
}
