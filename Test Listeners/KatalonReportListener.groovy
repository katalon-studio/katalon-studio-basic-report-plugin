import com.kms.katalon.core.annotation.AfterTestSuite;
import com.kms.katalon.core.annotation.BeforeTestSuite
import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.logging.model.TestSuiteLogRecord;
import com.kms.katalon.core.reporting.ReportWriterUtil;
import com.kms.katalon.core.setting.BundleSettingStore;
import com.kms.katalon.core.util.KeywordUtil;
import com.kms.katalon.core.util.internal.ExceptionsUtil;

public class KatalonReportListener {

    @AfterTestSuite
    public void exportKatalonReports() {
        try {
            KeywordUtil.logInfo("This Katalon Studio version has been deprecated. Please upgrade Katalon Studio to the latest version.");
            String reportFolder = RunConfiguration.getReportFolder();
            String projectDir = RunConfiguration.getProjectDir();

            BundleSettingStore bundleSettingStore = new BundleSettingStore(projectDir, "com.katalon.plugin.report",
                    true);

            boolean genereteHTML = bundleSettingStore.getBoolean("generateHTML", true);
            boolean genereteCSV = bundleSettingStore.getBoolean("generateCSV", true);
            boolean genereteJUnit = bundleSettingStore.getBoolean("generateJUnit", true);
            boolean generetePDF = bundleSettingStore.getBoolean("generatePDF", false);
            if (!genereteHTML && !genereteCSV && !genereteJUnit && !generetePDF) {
                return;
            }

            File reportFolderFile = new File(reportFolder);

            TestSuiteLogRecord suiteLogEntity = ReportWriterUtil.generate(reportFolder);

            if (genereteHTML) {
                KeywordUtil.logInfo("Start generating HTML report folder at: " + reportFolder + "...");
                ReportWriterUtil.writeHtmlReport(suiteLogEntity, reportFolderFile);
                KeywordUtil.logInfo("HTML report generated");
            }

            if (genereteCSV) {
                KeywordUtil.logInfo("Start generating CSV report folder at: " + reportFolder + "...");
                ReportWriterUtil.writeCSVReport(suiteLogEntity, reportFolderFile);
                KeywordUtil.logInfo("CSV report generated");
            }

            if (genereteJUnit) {
                KeywordUtil.logInfo("Start generating JUnit report folder at: " + reportFolder + "...");
                ReportWriterUtil.writeJUnitReport(suiteLogEntity, reportFolderFile);
                KeywordUtil.logInfo("JUnit report generated");
            }

            if (generetePDF) {
                KeywordUtil.logInfo("Start generating PDF report folder at: " + reportFolder + "...");
                ReportWriterUtil.writePdfReport(suiteLogEntity, reportFolderFile);
                KeywordUtil.logInfo("PDF report generated");
            }
        } catch (Exception e) {
            KeywordUtil.markWarning(ExceptionsUtil.getStackTraceForThrowable(e));
        }
    }

	@BeforeTestSuite
	public void beforeExportKatalonReports() {
		KeywordUtil.logInfo("This Katalon Studio version has been deprecated. Please upgrade Katalon Studio to the latest version.");
	}
}
