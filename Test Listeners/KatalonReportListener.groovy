import java.io.File;

import com.kms.katalon.core.annotation.AfterTestSuite;
import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.logging.model.TestSuiteLogRecord;
import com.kms.katalon.core.reporting.basic.reporting.ReportWriterUtil;
import com.kms.katalon.core.setting.BundleSettingStore;
import com.kms.katalon.core.util.KeywordUtil;
import com.kms.katalon.core.util.internal.ExceptionsUtil;
import java.nio.file.Files
import org.apache.commons.io.FileUtils

public class KatalonReportListener {

    @AfterTestSuite
    public void exportKatalonReports() {
        try {
            String reportFolder = RunConfiguration.getReportFolder();
            String projectDir = RunConfiguration.getProjectDir();

            BundleSettingStore bundleSettingStore = new BundleSettingStore(projectDir, "com.katalon.plugin.report",
                    true);

            boolean genereteHTML = bundleSettingStore.getBoolean("generateHTML", true);
            boolean genereteCSV = bundleSettingStore.getBoolean("generateCSV", true);
            // boolean genereteJUnit = bundleSettingStore.getBoolean("generateJUnit", true);
            boolean generetePDF = bundleSettingStore.getBoolean("generatePDF", false);
            if (!genereteHTML && !genereteCSV && !generetePDF) {
                return;
            }

            File reportFolderFile = new File(reportFolder);
            File folderTemp = Files.createTempDirectory(reportFolderFile.getName() + "_").toFile();
            // rename temp folder to match with report folder         
            folderTemp = Files.move(folderTemp.toPath(), folderTemp.toPath().resolveSibling(reportFolderFile.getName())).toFile();

            FileUtils.copyDirectory(reportFolderFile, folderTemp);
            String folderTempString = folderTemp.getAbsolutePath();           
            
            TestSuiteLogRecord suiteLogEntity = ReportWriterUtil.generate(folderTempString);

            if (genereteHTML) {
                KeywordUtil.logInfo("Start generating HTML report folder at: " + reportFolder + "...");
                ReportWriterUtil.writeHtmlReport(suiteLogEntity, folderTemp);
                KeywordUtil.logInfo("HTML report generated");
            }

            if (genereteCSV) {
                KeywordUtil.logInfo("Start generating CSV report folder at: " + reportFolder + "...");
                ReportWriterUtil.writeCSVReport(suiteLogEntity, folderTemp);
                KeywordUtil.logInfo("CSV report generated");
            }

//            if (genereteJUnit) {
//                KeywordUtil.logInfo("Start generating JUnit report folder at: " + reportFolder + "...");
//                ReportWriterUtil.writeJUnitReport(suiteLogEntity, reportFolderFile);
//                KeywordUtil.logInfo("JUnit report generated");
//            }

            if (generetePDF) {
                KeywordUtil.logInfo("Start generating PDF report folder at: " + reportFolder + "...");
                ReportWriterUtil.writePdfReport(suiteLogEntity, folderTemp);
                KeywordUtil.logInfo("PDF report generated");
            }
            
            FileUtils.copyDirectory(folderTemp, reportFolderFile, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String path = pathname.getAbsolutePath().toLowerCase();
                    return path.contains(".csv") || path.contains(".html") || path.contains(".pdf");
                }
            });
            FileUtils.deleteQuietly(folderTemp);       
            FileUtils.forceDeleteOnExit(folderTemp);
        } catch (Exception e) {
            KeywordUtil.markWarning(ExceptionsUtil.getStackTraceForThrowable(e));
        }
    }
}
