package com.kms.katalon.core.reporting.basic.reporting;

import org.apache.commons.lang3.StringEscapeUtils;

public class XmlStringUtil {

    public static String escapeXml(String text) {
        return StringEscapeUtils.escapeXml10(text);
    }
}
