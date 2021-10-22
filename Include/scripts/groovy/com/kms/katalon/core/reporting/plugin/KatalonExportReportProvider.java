package com.kms.katalon.core.reporting.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.logging.XMLParserException;
import com.kms.katalon.core.logging.model.ILogRecord;
import com.kms.katalon.core.logging.model.TestCaseLogRecord;
import com.kms.katalon.core.logging.model.TestSuiteLogRecord;
import com.kms.katalon.core.pdf.TestSuitePdfGenerator;
import com.kms.katalon.core.pdf.exception.JasperReportException;
import com.kms.katalon.core.reporting.ExportReportProvider;
import com.kms.katalon.core.reporting.ReportUtil;
import com.kms.katalon.core.reporting.basic.reporting.ReportWriterUtil;
import com.kms.katalon.core.util.internal.ExceptionsUtil;
import com.kms.katalon.core.util.internal.JsonUtil;

public class KatalonExportReportProvider implements ExportReportProvider {
    
    private static final KeywordLogger logger = KeywordLogger.getInstance(KatalonExportReportProvider.class);

    public static final String PASSED_LOG_BACKGROUND_COLOR = "#5bb135";

    public static final String FAILED_LOG_BACKGROUND_COLOR = "#ce2c2c";

    public static final String FAILED_STATUS_BACKGROUND_COLOR = "#f19696";

    public static final String INCOMPLETE_STATUS_BACKGROUND_COLOR = "#f2bc70";

    private static final String DEFAULT_COMPOSITE_BACKGROUND_COLOR = "#f0f0f0";

    @Override
    public String[] getSupportedTypeForTestSuite() {
        return new String[] { "HTML", "CSV (Summary)", "CSV (Details)", "PDF" };
    }

    @Override
    public String[] getSupportedTypeForTestSuiteCollection() {
        return new String[] { "HTML" };
    }

    @Override
    public File exportTestSuite(File exportLocation, String projectDir, String reportId, String formatType)
            throws IOException, URISyntaxException {
        try {
            File reportFolder = new File(projectDir, reportId);
            TestSuiteLogRecord testSuiteLogRecord = ReportWriterUtil.generate(reportFolder.getAbsolutePath());
            switch (formatType) {
                case "HTML":
                    List<ILogRecord> testCaseLogRecords = new ArrayList<>();
                    for (ILogRecord logRecord : testSuiteLogRecord.filterFinalTestCasesResult()) {
                        if (logRecord instanceof TestCaseLogRecord) {
                            testCaseLogRecords.add(logRecord);
                        }
                    }
                    ReportWriterUtil.writeLogRecordToHTMLFile(testSuiteLogRecord, exportLocation, testCaseLogRecords);
                    return exportLocation;
                case "CSV (Summary)":
                    ReportWriterUtil.writeLogRecordToCSVFile(testSuiteLogRecord, exportLocation,
                            Arrays.asList(testSuiteLogRecord.filterFinalTestCasesResult()), false);
                    return exportLocation;
                case "CSV (Details)":
                    ReportWriterUtil.writeLogRecordToCSVFile(testSuiteLogRecord, exportLocation,
                            Arrays.asList(testSuiteLogRecord.filterFinalTestCasesResult()), true);
                    return exportLocation;
                case "PDF":
                    TestSuitePdfGenerator pdfGenerator = new TestSuitePdfGenerator(testSuiteLogRecord);
                    try {
                        pdfGenerator.exportToPDF(exportLocation.getAbsolutePath());
                    } catch (JasperReportException e) {
                        logger.logWarning(ExceptionsUtil.getStackTraceForThrowable(e));
                        throw new IOException(e);
                    }
                    return exportLocation;
                default:
                    break;
            }
        } catch (XMLParserException | XMLStreamException ex) {
            logger.logWarning(ExceptionsUtil.getStackTraceForThrowable(ex));
            throw new IOException(ex);
        }
        return null;
    }

    @Override
    public File exportTestSuiteCollection(File exportLocation, String projectDirLocation, String reportId,
            String formatType) throws IOException, URISyntaxException {
        switch (formatType) {
            case "HTML": {
                try {
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(new File(projectDirLocation, reportId + ".rp"));
                    Element rootElement = document.getRootElement();
                    List<String> testSuiteReportIds = new ArrayList<>();
                    List<Element> reportItemDescriptionItems = rootElement.element("reportItemDescriptions")
                            .elements("ReportItemDescription");
                    reportItemDescriptionItems.stream().forEach(e -> {
                        testSuiteReportIds.add(e.elementText("reportLocation"));
                    });

                    Map<String, String> tsInfoItem;
                    List<Map<String, String>> tsInfoItems = new ArrayList<>();
                    for (String reportRelativeLocation : testSuiteReportIds) {
                        try {
                            String reportDirLocation = projectDirLocation + File.separator + reportRelativeLocation;
                            TestSuiteLogRecord testSuiteLogRecord = ReportUtil.generate(reportDirLocation);
                            int reportDirLocationHashCode = reportDirLocation.toString().hashCode();

                            String htmlFileName = StringUtils.substringAfterLast(reportRelativeLocation, "/")
                                    + reportDirLocationHashCode + ".html";

                            File htmlFile = new File(reportDirLocation + File.separator + htmlFileName);
                            if (!htmlFile.exists()) {
                                if (testSuiteLogRecord == null) {
                                    throw new FileNotFoundException(htmlFile.getPath());
                                }
                                ReportUtil.writeHtmlReportAppendHashCodeToName(testSuiteLogRecord,
                                        new File(reportDirLocation), reportDirLocationHashCode);
                            }
                            FileUtils.copyFileToDirectory(htmlFile, exportLocation);

                            tsInfoItem = new HashMap<>();
                            tsInfoItem.put("report_location", reportRelativeLocation);
                            tsInfoItem.put("report", htmlFileName);
                            tsInfoItem.put("id", getTestSuiteId(reportRelativeLocation));
                            tsInfoItem.put("environment", testSuiteLogRecord.getBrowser());
                            String status = getStatus(testSuiteLogRecord);
                            tsInfoItem.put("status", status);
                            tsInfoItem.put("fail_on_total", getFailOnTotal(testSuiteLogRecord));
                            tsInfoItem.put("status_color", getStatusColor(status));
                            tsInfoItem.put("fail_color", getFailColor(testSuiteLogRecord));
                            tsInfoItems.add(tsInfoItem);
                        } catch (XMLParserException | XMLStreamException e) {
                            throw new IOException(e);
                        }
                    }
                    return ReportWriterUtil.writeTSCollectionHTMLReport(new File(reportId).getName(),
                            JsonUtil.toJson(tsInfoItems, false), exportLocation);
                } catch (DocumentException ex) {
                    throw new IOException(ex);
                }
            }
        }

        return null;
    }
    
    private int getTotalFailedTestCases(TestSuiteLogRecord testSuiteLogRecord) {
        if (testSuiteLogRecord == null) {
            return 0;
        }
        return testSuiteLogRecord.getTotalFailedTestCases() + testSuiteLogRecord.getTotalErrorTestCases();
    }

    private String getFailOnTotal(TestSuiteLogRecord testSuiteLogRecord) {
        if (testSuiteLogRecord == null) {
            return StringUtils.EMPTY;
        }
        return getTotalFailedTestCases(testSuiteLogRecord) + " / " + testSuiteLogRecord.getTotalTestCases();
    }

    private String getFailColor(TestSuiteLogRecord testSuiteLogRecord) {
        if (testSuiteLogRecord == null) {
            return StringUtils.EMPTY;
        }
        if (getTotalFailedTestCases(testSuiteLogRecord) > 0) {
            return FAILED_STATUS_BACKGROUND_COLOR;
        }
        return StringUtils.EMPTY;
    }

    private String getTestSuiteId(String reportRelativeLocation) {
        String testSuiteId = reportRelativeLocation.replaceFirst("Reports",
                "Test Suites");
        testSuiteId = StringUtils.substringBeforeLast(testSuiteId, "/");
        return testSuiteId;
    }

    private String getStatus(TestSuiteLogRecord logRecord) {
        if (logRecord == null) {
            return "NOT_STARTED";
        }
        if (logRecord.getTotalIncompleteTestCases() > 0) {
            return "INCOMPLETE";
        }
        return "COMPLETE";
    }

    private String getStatusColor(String status) {
        if (StringUtils.equals(status, "NOT_STARTED")) {
            return DEFAULT_COMPOSITE_BACKGROUND_COLOR;
        }
        if (StringUtils.equals(status, "INCOMPLETE")) {
            return INCOMPLETE_STATUS_BACKGROUND_COLOR;
        }
        return StringUtils.EMPTY;
    }

}
