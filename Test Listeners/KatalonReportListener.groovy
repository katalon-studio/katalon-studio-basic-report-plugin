import java.nio.file.Files
import java.nio.file.Path;

import org.apache.commons.io.FileUtils

import com.kms.katalon.core.annotation.AfterTestSuite;
import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.context.TestSuiteContext
import com.kms.katalon.core.logging.model.TestSuiteLogRecord;
import com.kms.katalon.core.reporting.basic.reporting.ReportWriterUtil;
import com.kms.katalon.core.setting.BundleSettingStore;
import com.kms.katalon.core.util.KeywordUtil;
import com.kms.katalon.core.util.internal.ExceptionsUtil;

public class KatalonReportListener {
    public void copyDirectory(Path sourcePath, Path destinationPath) {
        try {
            Files.walk(sourcePath).forEach(source -> {
                Path destination = destinationPath.resolve(sourcePath.relativize(source));
                try {
                    Files.copy(source, destination);
                } catch (IOException e) {
                }
            });
        } catch (IOException e) {
        }
    }

    @AfterTestSuite
    public void exportKatalonReports(TestSuiteContext testSuiteContext) {
        try {
            String reportFolder = RunConfiguration.getReportFolder();
            String projectDir = RunConfiguration.getProjectDir();

            BundleSettingStore bundleSettingStore = new BundleSettingStore(projectDir, "com.katalon.plugin.report",
            true);

            boolean genereteHTML = bundleSettingStore.getBoolean("generateHTML", true);
            boolean genereteCSV = bundleSettingStore.getBoolean("generateCSV", true);
            boolean generetePDF = bundleSettingStore.getBoolean("generatePDF", false);
            if (!genereteHTML && !genereteCSV && !generetePDF) {
                return;
            }

            File reportFolderFile = new File(reportFolder);
            File folderTemp = Files.createTempDirectory(reportFolderFile.getName() + "_").toFile();
            // rename temp folder to match with report folder
            folderTemp = Files.move(folderTemp.toPath(), folderTemp.toPath().resolveSibling(reportFolderFile.getName())).toFile();

            //FileUtils.copyDirectory(reportFolderFile, folderTemp);
            copyDirectory(reportFolderFile.toPath(), folderTemp.toPath());
            String folderTempString = folderTemp.getAbsolutePath();
            TestSuiteLogRecord suiteLogEntity = ReportWriterUtil.generate(folderTempString);

            if (genereteHTML) {
                KeywordUtil.logInfo("Start generating HTML report folder at: " + reportFolder + "...");
                File htmlReportFile =ReportWriterUtil.writeHtmlReport(suiteLogEntity, folderTemp);
                KeywordUtil.logInfo("HTML report generated");
                FileUtils.copyFileToDirectory(htmlReportFile, reportFolderFile);
            }

            if (genereteCSV) {
                KeywordUtil.logInfo("Start generating CSV report folder at: " + reportFolder + "...");
                ReportWriterUtil.writeCSVReport(suiteLogEntity, folderTemp);
                KeywordUtil.logInfo("CSV report generated");
            }

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
