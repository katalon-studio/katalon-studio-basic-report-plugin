package com.kms.katalon.core.reporting.basic.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.kms.katalon.core.constants.StringConstants;
import com.kms.katalon.core.logging.TestSuiteXMLLogParser;
import com.kms.katalon.core.logging.XMLLoggerParser;
import com.kms.katalon.core.logging.XMLParserException;
import com.kms.katalon.core.logging.XmlLogRecord;
import com.kms.katalon.core.logging.model.ILogRecord;
import com.kms.katalon.core.logging.model.MessageLogRecord;
import com.kms.katalon.core.logging.model.TestStatus;
import com.kms.katalon.core.logging.model.TestStatus.TestStatusValue;
import com.kms.katalon.core.logging.model.TestSuiteLogRecord;
import com.kms.katalon.core.pdf.TestSuitePdfGenerator;
import com.kms.katalon.core.pdf.exception.JasperReportException;
import com.kms.katalon.core.reporting.basic.reporting.template.ResourceLoader;
import com.kms.katalon.core.testdata.reader.CsvWriter;
import com.kms.katalon.core.util.internal.DateUtil;

public class ReportWriterUtil {

    private static void appendReportConstantValues(List<String> constantValues, StringBuilder stringBuilder) {
        for (String value : constantValues) {
            stringBuilder.append(value);
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    }

    private static void appendReportConstantValuesWithWriter(List<String> constantValues, Writer writer)
            throws IOException {
        int size = constantValues.size();
        for (int i = 0; i < size; i++) {
            writer.write(constantValues.get(i));
            if (i < size - 1) {
                writer.write(",");
            }
        }
    }

    private static String generateVars(List<String> strings, TestSuiteLogRecord suiteLogEntity, StringBuilder model)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        List<String> lines = IOUtils
                .readLines(ResourceLoader.class.getResourceAsStream(ResourceLoader.HTML_TEMPLATE_VARS));
        for (String line : lines) {
            if (line.equals(ResourceLoader.HTML_TEMPLATE_SUITE_MODEL_TOKEN)) {
                sb.append(model);
            } else if (line.equals(ResourceLoader.HTML_TEMPLATE_STRINGS_CONSTANT_TOKEN)) {
                appendReportConstantValues(strings, sb);
            } else if (line.equals(ResourceLoader.HTML_TEMPLATE_EXEC_ENV_TOKEN)) {
                StringBuilder envInfoSb = new StringBuilder();
                envInfoSb.append("{");
                envInfoSb.append(String.format("\"host\" : \"%s\", ", suiteLogEntity.getHostName()));
                envInfoSb.append(String.format("\"os\" : \"%s\", ", suiteLogEntity.getOs()));
                envInfoSb.append(String.format("\"" + StringConstants.APP_VERSION + "\" : \"%s\", ",
                        suiteLogEntity.getAppVersion()));

                String mobileDeviceName = suiteLogEntity.getMobileDeviceName();
                envInfoSb.append(String.format("\"deviceName\" : \"%s\", ", mobileDeviceName));

                String browserName = suiteLogEntity.getBrowser();
                envInfoSb.append(String.format("\"browserName\" : \"%s\", ", browserName));
                envInfoSb.append("\"\" : \"\"");
                envInfoSb.append("}");
                sb.append(envInfoSb);
            } else {
                sb.append(line);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static void generateVarsWithWriter(List<String> strings, TestSuiteLogRecord suiteLogEntity,
            StringBuilder model, Writer writer) throws IOException {
        List<String> lines = IOUtils
                .readLines(ResourceLoader.class.getResourceAsStream(ResourceLoader.HTML_TEMPLATE_VARS));
        for (String line : lines) {
            if (line.equals(ResourceLoader.HTML_TEMPLATE_SUITE_MODEL_TOKEN)) {
                writer.write(model.toString());
            } else if (line.equals(ResourceLoader.HTML_TEMPLATE_STRINGS_CONSTANT_TOKEN)) {
                appendReportConstantValuesWithWriter(strings, writer);
            } else if (line.equals(ResourceLoader.HTML_TEMPLATE_EXEC_ENV_TOKEN)) {
                StringBuilder envInfoSb = new StringBuilder();
                envInfoSb.append("{");
                envInfoSb.append(String.format("\"host\" : \"%s\", ", suiteLogEntity.getHostName()));
                envInfoSb.append(String.format("\"os\" : \"%s\", ", suiteLogEntity.getOs()));
                envInfoSb.append(String.format("\"" + StringConstants.APP_VERSION + "\" : \"%s\", ",
                        suiteLogEntity.getAppVersion()));
                String mobileDeviceName = suiteLogEntity.getMobileDeviceName();
                envInfoSb.append(String.format("\"deviceName\" : \"%s\", ", mobileDeviceName));

                String browserName = suiteLogEntity.getBrowser();
                envInfoSb.append(String.format("\"browserName\" : \"%s\", ", browserName));
                envInfoSb.append("\"\" : \"\"");
                envInfoSb.append("}");
                writer.write(envInfoSb.toString());
            } else {
                writer.write(line);
                writer.write("\n");
            }
        }
    }

    public static String getOs() {
        return System.getProperty("os.name") + " " + System.getProperty("sun.arch.data.model") + "bit";
    }

    public static String getHostName() {
        String hostName = "Unknown";
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostName = addr.getCanonicalHostName();
        } catch (UnknownHostException ex) {}
        return hostName;
    }

    private static void collectInfoLines(ILogRecord logRecord, List<ILogRecord> rmvLogs) {
        if (logRecord instanceof MessageLogRecord) {
            if (logRecord.getStatus().getStatusValue() == TestStatusValue.INCOMPLETE
                    || logRecord.getStatus().getStatusValue() == TestStatusValue.INFO) {
                rmvLogs.add(logRecord);
            }
        }
        for (ILogRecord childLogRecord : logRecord.getChildRecords()) {
            collectInfoLines(childLogRecord, rmvLogs);
        }
    }

    public static void writeLogRecordToFiles(String logFolder) throws Exception {
        TestSuiteLogRecord testSuiteLogRecord = generate(logFolder);
        if (testSuiteLogRecord != null) {
            writeLogRecordToFiles(testSuiteLogRecord, new File(logFolder));
        }
    }

    public static void writeLogRecordToCSVFile(TestSuiteLogRecord suiteLogEntity, File destFile,
            List<ILogRecord> filteredTestCases) throws IOException {
        writeLogRecordToCSVFile(suiteLogEntity, destFile, filteredTestCases, true);
    }

    public static void writeLogRecordToCSVFile(TestSuiteLogRecord suiteLogEntity, File destFile,
            List<ILogRecord> filteredTestCases, boolean stepsIncluded) throws IOException {
        CsvWriter.writeCsvReport(suiteLogEntity, destFile, filteredTestCases, stepsIncluded);
    }

    public static void writeLogRecordToFiles(TestSuiteLogRecord suiteLogEntity, File logFolder) throws Exception {
        writeHtmlReport(suiteLogEntity, logFolder);

        writeCSVReport(suiteLogEntity, logFolder);

        writeSimpleHTMLReport(suiteLogEntity, logFolder);

        writeJUnitReport(suiteLogEntity, logFolder);

        writePdfReport(suiteLogEntity, logFolder);
    }

    public static File writePdfReport(TestSuiteLogRecord suiteLogEntity, File logFolder)
            throws JasperReportException, IOException {
        TestSuitePdfGenerator pdfGenerator = new TestSuitePdfGenerator(suiteLogEntity);
        File exportLocation = new File(logFolder, logFolder.getName() + ".pdf");
        pdfGenerator.exportToPDF(exportLocation.getAbsolutePath());
        return exportLocation;
    }

    public static void writeLogRecordToJUnitFile(String logFolder) throws Exception {
        TestSuiteLogRecord testSuiteLogRecord = generate(logFolder);
        if (testSuiteLogRecord != null) {
            writeJUnitReport(testSuiteLogRecord, new File(logFolder));
        }
    }

    public static void writeJUnitReport(TestSuiteLogRecord suiteLogEntity, File logFolder)
            throws JAXBException, IOException {
        JUnitReportObjectFactory factory = new JUnitReportObjectFactory();

        String testSuiteName = suiteLogEntity.getName();
        String totalPass = suiteLogEntity.getTotalPassedTestCases() + "";
        String totalError = suiteLogEntity.getTotalErrorTestCases() + "";
        String totalFailure = suiteLogEntity.getTotalFailedTestCases() + "";
        String duration = ((suiteLogEntity.getEndTime() - suiteLogEntity.getStartTime()) / 1000) + "";

        JUnitProperties properties = factory.createProperties();
        List<JUnitProperty> propertyList = properties.getProperty();
        propertyList.add(new JUnitProperty("deviceName", suiteLogEntity.getDeviceName()));
        propertyList.add(new JUnitProperty("devicePlatform", suiteLogEntity.getDevicePlatform()));
        propertyList.add(new JUnitProperty("logFolder", StringEscapeUtils.escapeJava(suiteLogEntity.getLogFolder())));
        propertyList.add(new JUnitProperty("logFiles", factory.sanitizeReportLogs(suiteLogEntity)));
        propertyList.add(new JUnitProperty("attachments", factory.sanitizeReportAttachments(suiteLogEntity)));
        suiteLogEntity.getRunData().forEach((name, value) -> propertyList.add(new JUnitProperty(name, value)));

        JUnitTestSuite ts = factory.createTestSuite();
        ts.setProperties(properties);
        ts.setId(suiteLogEntity.getId());
        ts.setName(testSuiteName);
        ts.setHostname(suiteLogEntity.getHostName());
        ts.setTime(duration);
        ts.setTimestamp(DateUtil.getDateTimeFormatted(suiteLogEntity.getStartTime()));
        ts.setSystemOut(suiteLogEntity.getSystemOutMsg().trim());
        ts.setSystemErr(suiteLogEntity.getSystemErrorMsg().trim());

        // tests: The total number of tests in the suite, required
        ts.setTests(totalPass);
        // errors: The total number of tests in the suite that error
        ts.setErrors(totalError);
        // failures: The total number of tests in the suite that failed
        ts.setFailures(totalFailure);

        Arrays.asList(suiteLogEntity.getChildRecords()).stream().forEach(item -> {
            JUnitTestCase tc = factory.createTestCase();
            tc.setClassname(item.getId());
            tc.setName(item.getName());

            TestStatus status = item.getStatus();
            TestStatusValue statusValue = status.getStatusValue();
            String statusName = statusValue.name();
            String message = StringUtils.removeStart(item.getMessage(),
                    item.getName() + " " + statusName + " because (of) ");
            tc.setStatus(statusName);
            if (TestStatusValue.ERROR == statusValue) {
                JUnitError error = factory.createError();
                error.setType(statusName);
                error.setMessage(message);
                tc.getError().add(error);
            }
            if (TestStatusValue.FAILED == statusValue) {
                JUnitFailure failure = factory.createFailure();
                failure.setType(statusName);
                failure.setMessage(message);
                tc.getFailure().add(failure);
            }

            tc.getSystemOut().add(item.getSystemOutMsg().trim());
            tc.getSystemErr().add(item.getSystemErrorMsg().trim());
            ts.getTestcase().add(tc);
        });

        // This is a single test suite. Thus, the info for the test suites is the same as test suite
        JUnitTestSuites tss = factory.createTestSuites();
        // errors: total number of tests with error result from all test suite
        tss.setErrors(totalError);
        // failures: total number of failed tests from all test suite
        tss.setFailures(totalFailure);
        // tests: total number of successful tests from all test suite
        tss.setTests(totalPass);
        // time: in seconds to execute all test suites
        tss.setTime(duration);
        // name
        tss.setName(testSuiteName);

        tss.getTestsuite().add(ts);

        JAXBContext context = JAXBContext
                .newInstance(new Class[] { JUnitError.class, JUnitFailure.class, JUnitProperties.class,
                        JUnitProperty.class, JUnitTestCase.class, JUnitTestSuites.class, JUnitTestSuite.class });
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(tss, new File(logFolder, "JUnit_Report.xml"));
    }

    public static File writeTSCollectionHTMLReport(String reportTitle, String tsReportsJson, File destDir)
            throws IOException, URISyntaxException {
        String template = readFileToStringBuilder(ResourceLoader.HTML_COLLECTION_INDEX_TEMPLATE);
        template = StringUtils.replace(template, "REPORT_TITLE", reportTitle);
        template = StringUtils.replace(template, "TEST_SUITE_REPORT_LIST", tsReportsJson);
        File indexFile = new File(destDir, "index.html");
        FileUtils.writeStringToFile(indexFile, template, StringConstants.DF_CHARSET);

        template = readFileToStringBuilder(ResourceLoader.HTML_COLLECTION_FRAME_TEMPLATE);
        template = StringUtils.replace(template, "REPORT_TITLE", reportTitle);
        template = StringUtils.replace(template, "TEST_SUITE_REPORT_LIST", tsReportsJson);
        FileUtils.writeStringToFile(new File(destDir, "index-frame-view.html"), template, StringConstants.DF_CHARSET);
        return indexFile;
    }

    public static File writeHtmlReport(TestSuiteLogRecord suiteLogEntity, File logFolder)
            throws IOException, URISyntaxException {
        File destFile = new File(logFolder, logFolder.getName() + ".html");
        prepareHtmlContent(suiteLogEntity, destFile);
        return destFile;
    }

    private static void prepareHtmlContent(TestSuiteLogRecord suiteLogEntity, File destFile)
            throws IOException, URISyntaxException {
        List<String> strings = new ArrayList<String>();

        JsSuiteModel jsSuiteModel = new JsSuiteModel(suiteLogEntity, strings);
        StringBuilder sbModel = jsSuiteModel.toArrayString();
        OutputStream outputStream = null;
        Writer writer = null;
        try {
            outputStream = new FileOutputStream(destFile);
            writer = new OutputStreamWriter(outputStream, StringConstants.DF_CHARSET);
            writer.write(readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_FILE));
            generateVarsWithWriter(strings, suiteLogEntity, sbModel, writer);
            writer.write(readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_CONTENT));
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public static void writeHtmlReportAppendHashCodeToName(TestSuiteLogRecord suiteLogEntity, File logFolder,
            int reportDirLocationHashCode) throws IOException, URISyntaxException {
        File destFile = new File(logFolder, logFolder.getName() + reportDirLocationHashCode + ".html");
        prepareHtmlContent(suiteLogEntity, destFile);
    }

    public static void writeExecutionUUIDToFile(String UUID, File logFolder) throws IOException, URISyntaxException {
        FileUtils.writeStringToFile(new File(logFolder, "execution.uuid"), UUID, StringConstants.DF_CHARSET);
    }

    public static File writeCSVReport(TestSuiteLogRecord suiteLogEntity, File folder) throws IOException {
        File file = new File(folder, folder.getName() + ".csv");
        if (!file.exists()) {
            CsvWriter.writeCsvReport(suiteLogEntity, file, Arrays.asList(suiteLogEntity.filterFinalTestCasesResult()));
        }
        return file;
    }

    public static void writeSimpleHTMLReport(TestSuiteLogRecord suiteLogEntity, File logFolder)
            throws IOException, URISyntaxException {

        List<String> simpleStrings = new LinkedList<String>();
        JsSuiteModel simpleJsSuiteModel = new JsSuiteModel(suiteLogEntity, simpleStrings);
        StringBuilder simpleSbModel = simpleJsSuiteModel.toArrayString();

        File destSimpleFile = new File(logFolder, "Report.html");
        FileUtils.writeStringToFile(destSimpleFile, readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_FILE),
                StringConstants.DF_CHARSET);
        FileUtils.writeStringToFile(destSimpleFile, generateVars(simpleStrings, suiteLogEntity, simpleSbModel),
                StringConstants.DF_CHARSET, true);
        FileUtils.writeStringToFile(destSimpleFile, readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_CONTENT),
                StringConstants.DF_CHARSET, true);
    }

    public static void writeLogRecordToHTMLFile(TestSuiteLogRecord suiteLogEntity, File destFile,
            List<ILogRecord> filteredTestCases) throws IOException, URISyntaxException {

        List<String> strings = new LinkedList<String>();
        JsSuiteModel jsSuiteModel = new JsSuiteModel(suiteLogEntity, strings, filteredTestCases);
        StringBuilder sbModel = jsSuiteModel.toArrayString();

        FileUtils.writeStringToFile(destFile, readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_FILE),
                StringConstants.DF_CHARSET);
        FileUtils.writeStringToFile(destFile, generateVars(strings, suiteLogEntity, sbModel),
                StringConstants.DF_CHARSET, true);
        FileUtils.writeStringToFile(destFile, readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_CONTENT),
                StringConstants.DF_CHARSET, true);
    }

    public static List<XmlLogRecord> getAllLogRecords(String logFolder)
            throws XMLParserException, IOException, XMLStreamException {
        return XMLLoggerParser.readFromLogFolder(logFolder);
    }

    public static TestSuiteLogRecord generate(String logFolder, IProgressMonitor progressMonitor)
            throws XMLParserException, IOException, XMLStreamException {
        return new TestSuiteXMLLogParser().readTestSuiteLogFromXMLFiles(logFolder, progressMonitor);
    }

    public static TestSuiteLogRecord generate(String logFolder)
            throws XMLParserException, IOException, XMLStreamException {
        return generate(logFolder, new NullProgressMonitor());
    }

    private static String readFileToStringBuilder(String fileName) throws IOException, URISyntaxException {
        StringBuilder sb = new StringBuilder();
        String path = ResourceLoader.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path = URLDecoder.decode(path, "utf-8");
        File jarFile = new File(path);
        if (jarFile.isFile()) {
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if (name.endsWith(fileName)) {
                    StringBuilderWriter sbWriter = new StringBuilderWriter(new StringBuilder());
                    IOUtils.copy(jar.getInputStream(jarEntry), sbWriter);
                    sbWriter.flush();
                    sbWriter.close();
                    sb.append(sbWriter.getBuilder());
                    break;
                }
            }
            jar.close();
        } else { // Run with IDE
                 // sb.append(FileUtils.readFileToString(new
                 // File(ResourceLoader.class.getResource(fileName).getContent()
                 // )));
            InputStream is = (InputStream) ResourceLoader.class.getResource(fileName).getContent();
            sb.append(IOUtils.toString(is, "UTF-8"));
        }
        return sb.toString();
    }
}
