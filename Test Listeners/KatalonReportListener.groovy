import java.nio.file.FileAlreadyExistsException
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
    private void copyDirectory(Path sourcePath, Path destinationPath) {
        try {
            Files.walk(sourcePath).forEach{ source ->
				Path destination = destinationPath.resolve(sourcePath.relativize(source));
				try {
					Files.copy(source, destination);
				} catch (IOException e) {
					if(!(e instanceof FileAlreadyExistsException)) {
						KeywordUtil.markWarning(ExceptionsUtil.getStackTraceForThrowable(e));
					}
				}
			};
        } catch (IOException e) {
            KeywordUtil.markWarning(ExceptionsUtil.getStackTraceForThrowable(e));
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

            copyDirectory(reportFolderFile.toPath(), folderTemp.toPath());
            String folderTempString = folderTemp.getAbsolutePath();
            TestSuiteLogRecord suiteLogEntity = ReportWriterUtil.generate(folderTempString);

            if (genereteHTML) {
                KeywordUtil.logInfo("Start generating HTML report folder at: " + reportFolder + "...");
                File htmlReportFile = ReportWriterUtil.writeHtmlReport(suiteLogEntity, folderTemp);
                FileUtils.copyFileToDirectory(htmlReportFile, reportFolderFile);
                KeywordUtil.logInfo("HTML report generated");
            }

            if (genereteCSV) {
                KeywordUtil.logInfo("Start generating CSV report folder at: " + reportFolder + "...");
                File csvReportFile = ReportWriterUtil.writeCSVReport(suiteLogEntity, folderTemp);
                FileUtils.copyFileToDirectory(csvReportFile, reportFolderFile);
                KeywordUtil.logInfo("CSV report generated");
            }

            if (generetePDF) {
                KeywordUtil.logInfo("Start generating PDF report folder at: " + reportFolder + "...");
                File pdfReportFile = ReportWriterUtil.writePdfReport(suiteLogEntity, folderTemp);
                FileUtils.copyFileToDirectory(pdfReportFile, reportFolderFile);
                KeywordUtil.logInfo("PDF report generated");
            }
            FileUtils.deleteQuietly(folderTemp);
            FileUtils.forceDeleteOnExit(folderTemp);
        } catch (Exception e) {
            KeywordUtil.markWarning(ExceptionsUtil.getStackTraceForThrowable(e));
        }
    }
}
