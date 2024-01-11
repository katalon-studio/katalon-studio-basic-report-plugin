package com.kms.katalon.core.reporting.basic.reporting;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class JsModelProperty {

    private int valueIndex;

    private String propertyName;

    private String propertyValue;

    List<String> strings;

    public JsModelProperty() {
    }

    public JsModelProperty(String name, String value, List<String> strings) {
        this.propertyName = name;
        this.propertyValue = value;
        this.strings = strings;

        appendAndEscapeString(strings, value);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        if (strings != null) {
            return String.valueOf(valueIndex);
        }
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    private void appendAndEscapeString(List<String> listStrings, String string) {
        if (listStrings != null) {
            String str = string;
            if (!string.startsWith("data:image/png;base64,")) {
                str = StringEscapeUtils.escapeHtml(StringEscapeUtils.escapeJava(string));
            }
            listStrings.add(convertString(str));
            valueIndex = listStrings.size() - 1;
        }
    }

    private static String convertString(String string) {
        return "\"" + StringUtils.defaultString(string) + "\"";
    }
}
