package com.kms.katalon.core.pdf.exception;

public class JasperReportException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 503067984587299059L;

    public JasperReportException(String message) {
        super(message);
    }
    
    public JasperReportException(String message, Throwable e) {
        super(message, e);
    }
}
