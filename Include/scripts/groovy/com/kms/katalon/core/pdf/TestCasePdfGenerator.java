package com.kms.katalon.core.pdf;

import java.util.HashMap;
import java.util.Map;

import com.kms.katalon.core.logging.model.ILogRecord;
import com.kms.katalon.core.logging.model.TestCaseLogRecord;

public class TestCasePdfGenerator extends AbstractPdfReportGenerator {

    private TestCaseLogRecord fTestCaseLogRecord;
    private String fLogFolderLoc;

    public TestCasePdfGenerator(TestCaseLogRecord testCaseLogRecord, String logFolderLoc) {
        fTestCaseLogRecord = testCaseLogRecord;
        fLogFolderLoc = logFolderLoc;
    }

    @Override
    protected ILogRecord[] getLogRecords() {
        return new ILogRecord[] { fTestCaseLogRecord };
    }

    @Override
    protected String getPrimaryTemplateLocation() {
        return TEST_CASE_TPL;
    }

    @Override
    protected Map<String, Object> getAdditionalParams() {
        Map<String, Object> jasperParams = new HashMap<String, Object>();
        jasperParams.put("LOG_FOLDER", fLogFolderLoc);

        return jasperParams;
    }

}
