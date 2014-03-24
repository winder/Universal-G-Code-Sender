package com.willwinder.universalgcodesender.pendantui;

public class PendantURLBean {
	private String urlString;
	private byte[] qrCodeJpg;
	
	public PendantURLBean() {
	}

	public PendantURLBean(String urlString, byte[] qrCodeJpg) {
		super();
		this.urlString = urlString;
		this.qrCodeJpg = qrCodeJpg;
	}

	public String getUrlString() {
		return urlString;
	}

	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}

	public byte[] getQrCodeJpg() {
		return qrCodeJpg;
	}

	public void setQrCodeJpg(byte[] qrCodeJpg) {
		this.qrCodeJpg = qrCodeJpg;
	}

	@Override
	public String toString() {
		return urlString;
	}

}
