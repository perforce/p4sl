package com.perforce.p4simulink.connection;

import java.util.ResourceBundle;

public class P4Identifier {
	private String product;
	private String version;
	private static final ResourceBundle resources = ResourceBundle.getBundle("Labels");

	public P4Identifier() {
		Package p = this.getClass().getPackage();
		version = p.getSpecificationVersion();
		String title = p.getSpecificationTitle();
		if (title != null) {
			product = resources.getString("productName.label") + " " + title.toUpperCase();
		}
	}

	public String getVersion() {
		if(version == null) {
			return "undefined";
		}
		return version;
	}

	public String getProduct() {
		if(product == null) {
			return "undefined";
		}
		return product;
	}
}
