package com.kms.katalon.core.pdf;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;

import net.sf.jasperreports.engine.util.JRProperties;

public class JasperClasspathManager {
    private static JasperClasspathManager _instance;

    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "Katalon";

    public static final String CLASSPATH_PROPERTY = "net.sf.jasperreports.compiler.classpath";

    public static final String REPORT_TEMP_DIR_PROPERTY = "user.dir";

    private boolean resolveClasspath;

    private JasperClasspathManager() {
        resolveClasspath = false;
    }

    public static JasperClasspathManager getInstance() {
        if (_instance == null) {
            _instance = new JasperClasspathManager();
        }
        return _instance;
    }

    public synchronized void modifySystemProperties() throws IOException, URISyntaxException {
        if (!resolveClasspath) {
            JRProperties.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            modifyClasspathProperty();
            modifyTempDirProperty();
        }
        resolveClasspath = true;
    }

    private void modifyTempDirProperty() {
        JRProperties.setProperty(JRProperties.COMPILER_TEMP_DIR, TEMP_DIR + "/generated/pdf");
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
        JRProperties.setProperty(JRProperties.COMPILER_CLASSPATH, classPathBuilder.toString());
    }
}
