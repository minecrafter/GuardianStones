package com.imaginarycode.plugins.guardianstones;

public class GuardianStonesException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8015498583701871041L;
	private String s;
	
	public GuardianStonesException(String msg) {
		s = msg;
	}
	
	public String getMessage() {
		return s;
	}
}
