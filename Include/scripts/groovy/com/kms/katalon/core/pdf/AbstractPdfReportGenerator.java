package com.kms.katalon.core.pdf;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.kms.katalon.core.logging.model.ILogRecord;
import com.kms.katalon.core.pdf.exception.JasperReportException;
import com.kms.katalon.core.util.internal.JarUtil;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRAbstractJavaCompiler;

public abstract class AbstractPdfReportGenerator {
    protected static final String TEST_SUITE_TPL = "test_suite_tpl.jrxml";
    protected static final String TEST_CASE_TPL = "test_case_tpl.jrxml";
    protected static final String TEST_CASE_SUMMARY_TPL = "test_case_summary.jrxml";

    /**
     * @return Returns an array of {@link ILogRecord} that system will use as data source when generating report.
     * @see {@link #exportToPDF(String)}
     */
    protected abstract ILogRecord[] getLogRecords();

    /**
     * @return Returns relative path of main template of current generator. System will compile the report template at
     *         this location before generating report.
     *         <ul>
     *         <li>{@link #TEST_CASE_TPL}</li>
     *         <li>{@link #TEST_SUITE_TPL}</li>
     *         </ul>
     * 
     * @see {@link #exportToPDF(String)}
     */
    protected abstract String getPrimaryTemplateLocation();

    /**
     * @return Returns a map of Jasper Report's parameters that system will use and the returned of
     *         {@link #getReportParams()} as parameters when calling
     *         {@link JasperFillManager#fillReport(String, Map, net.sf.jasperreports.engine.JRDataSource)}
     */
    protected abstract Map<String, Object> getAdditionalParams();

    /**
     * @return Returns a map of Jasper Report's parameters
     * @see {@link JasperFillManager#fillReport(String, Map, net.sf.jasperreports.engine.JRDataSource)}
     * @see {@link #exportToPDF(String)}
     */
    private Map<String, Object> getReportParams() {
        Map<String, Object> jasperParams = new HashMap<String, Object>();

        jasperParams.put("TEST_CASE_TPL", TEST_CASE_TPL);
        jasperParams.put("TEST_CASE_SUMMARY_TPL", TEST_CASE_SUMMARY_TPL);
        jasperParams.putAll(getAdditionalParams());

        return jasperParams;
    }

    /**
     * Exports the current PDF format to the given <code>fileLocation</code>
     * @param fileLocation destination of the exported file.
     * @return a PDF file
     * @throws JasperReportException if there is any error occurs when generating report.
     * @throws IOException if the exported file is not valid.
     * @see {@link JRAbstractJavaCompiler#compileReport(net.sf.jasperreports.engine.design.JasperDesign)}
     */
    public File exportToPDF(String fileLocation) throws JasperReportException, IOException {
        File pdfFile = new File(fileLocation);

        Thread thread = Thread.currentThread();
        ClassLoader loader = thread.getContextClassLoader();
        thread.setContextClassLoader(this.getClass().getClassLoader());

        try {
            JasperClasspathManager.getInstance().modifySystemProperties();
            JasperReport jasperReport = JasperCompileManager.compileReport(JarUtil.getResourceAsInputStream(
                    getClass(), getPrimaryTemplateLocation()));

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, getReportParams(),
                    new JRBeanCollectionDataSource(Arrays.asList(getLogRecords())));

            JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFile.getAbsolutePath());
            return pdfFile;
        } catch (JRException | JRRuntimeException | URISyntaxException e) {
            throw new JasperReportException("Unable to export pdf to location: "+ fileLocation, e);
        } finally {
            thread.setContextClassLoader(loader);
        }
    }
}
