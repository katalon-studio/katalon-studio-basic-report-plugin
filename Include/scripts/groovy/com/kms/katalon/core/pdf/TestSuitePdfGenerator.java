package com.kms.katalon.core.pdf;

import java.util.HashMap;
import java.util.Map;

import com.kms.katalon.core.logging.model.ILogRecord;
import com.kms.katalon.core.logging.model.TestSuiteLogRecord;

public class TestSuitePdfGenerator extends AbstractPdfReportGenerator {
    
    private TestSuiteLogRecord fTestSuiteLogRecord;
    
    public TestSuitePdfGenerator(TestSuiteLogRecord testSuiteLogRecord) {
        fTestSuiteLogRecord = testSuiteLogRecord;
    }

    @Override
    protected ILogRecord[] getLogRecords() {
        return fTestSuiteLogRecord.filterFinalTestCasesResult();
    }

    @Override
    protected String getPrimaryTemplateLocation() {       
        return TEST_SUITE_TPL;
    }

    @Override
    protected Map<String, Object> getAdditionalParams() {
        Map<String, Object> jasperParams = new HashMap<String, Object>();
        jasperParams.put("TEST_SUITE", fTestSuiteLogRecord);
        
        return jasperParams;
    }

}
