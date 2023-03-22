package com.kms.katalon.core.pdf;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import org.apache.commons.io.FileUtils;

public class JasperClasspathManager {
    private static JasperClasspathManager _instance;

    private final JRPropertiesUtil jrPropertiesUtil;

    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "Katalon";

    public static final String JASPER_REPORTS_COMPILER_CLASSPATH = "net.sf.jasperreports.compiler.classpath";

    public static final String JASPER_REPORTS_COMPILER_TEMP_DIR = "net.sf.jasperreports.compiler.temp.dir";

    private boolean resolveClasspath;

    private JasperClasspathManager() {
        resolveClasspath = false;
        jrPropertiesUtil = JRPropertiesUtil.getInstance(DefaultJasperReportsContext.getInstance());
    }

    public static JasperClasspathManager getInstance() {
        if (_instance == null) {
            _instance = new JasperClasspathManager();
        }
        return _instance;
    }

    public synchronized void modifySystemProperties() throws IOException, URISyntaxException {
        if (!resolveClasspath) {
            jrPropertiesUtil.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            modifyClasspathProperty();
            modifyTempDirProperty();
        }
        resolveClasspath = true;
    }

    private void modifyTempDirProperty() {
        jrPropertiesUtil.setProperty(JASPER_REPORTS_COMPILER_TEMP_DIR, TEMP_DIR + "/generated/pdf");
    }

    private File getTempClasspathDir() {
        String nonRemovableSystemDir = new File(TEMP_DIR, "non-removable").getAbsolutePath();

        return new File(nonRemovableSystemDir, "com.kms.katalon.jasper");
    }

    private void modifyClasspathProperty() throws IOException, URISyntaxException {
        StringBuilder classPathBuilder = new StringBuilder();

        File tempClasspathDir = getTempClasspathDir();
        if (tempClasspathDir.exists()) {
            FileUtils.cleanDirectory(tempClasspathDir);
        }
        tempClasspathDir.mkdirs();

        for (File libFile : ResourceUtil.getFiles(getClass(), "lib", tempClasspathDir, true)) {
            classPathBuilder.append(libFile.getAbsolutePath()).append(";");
        }
        jrPropertiesUtil.setProperty(JASPER_REPORTS_COMPILER_CLASSPATH, classPathBuilder.toString());
    }
}
