package com.fanfan.utils;

public class UpdataInfo {
	public String changelog;
	public int version;
	public String installUrl;

	public UpdataInfo(String changelog, int version, String installUrl) {
		super();
		this.changelog = changelog;
		this.version = version;
		this.installUrl = installUrl;
	}

	public UpdataInfo() {
	}

}
