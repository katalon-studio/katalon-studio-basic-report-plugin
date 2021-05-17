package com.kms.katalon.core.reporting.basic.reporting;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.kms.katalon.core.logging.model.ILogRecord;
import com.kms.katalon.core.logging.model.TestCaseLogRecord;
import com.kms.katalon.core.logging.model.TestStatus;
import com.kms.katalon.core.logging.model.TestStatus.TestStatusValue;
import com.kms.katalon.core.logging.model.TestStepLogRecord;

public class JsTestModel extends JsModel {

	private List<String> listStrings;

	private TestCaseLogRecord testLog;

	private JsModel tags;

	private JsModel status;

	private JsModel values;

	private JsModel names;

	private List<JsStepModel> steps;

	// This Test could be called by a Call step
	private TestStepLogRecord caller;

	public JsTestModel(TestCaseLogRecord testLog, List<String> listStrings) {
		this.testLog = testLog;
		this.listStrings = listStrings;

		init();
	}

	public JsTestModel(TestCaseLogRecord testLog, List<String> listStrings, TestStepLogRecord caller) {
		this(testLog, listStrings);
		this.caller = caller;
	}

	private void init() {
		// test name
		props.add(new JsModelProperty("name", testLog.getName(), listStrings));
		// timeout
		props.add(new JsModelProperty("timeout", EMPTY_STRING_INDEX, null));
		// isCritical, skip this
		props.add(new JsModelProperty("isCritical", EMPTY_STRING_INDEX, null));
		// document, skip this
		props.add(new JsModelProperty("document", StringUtils.defaultString(testLog.getDescription()), listStrings));

		props.add(new JsModelProperty("tag", StringUtils.defaultString(testLog.getTag()), listStrings));
		// tags, skip this
		tags = new JsModel();

		// Status
		initStatus();

		// Steps
		steps = new ArrayList<JsStepModel>();
		for (ILogRecord logRecord : testLog.getChildRecords()) {
			if (logRecord instanceof TestStepLogRecord) {
				steps.add(new JsStepModel((TestStepLogRecord) logRecord, listStrings,
						caller == null ? "" : caller.getName()));
			}
		}
		// Test Data
		initVariables();
	}

	private void initVariables() {
		names = new JsModel();
		values = new JsModel();
		for (ILogRecord logRecord : testLog.getTestData()) {
			String[] data = logRecord.getMessage().split(" = ");
			if (data.length == 1) {
				values.props.add(new JsModelProperty(data[0], "", listStrings));
			} else {
				values.props.add(new JsModelProperty(data[0], data[1], listStrings));
			}
			names.props.add(new JsModelProperty(data[0], data[0], listStrings));

		}
	}

	private void initStatus() {
		// The test status
		status = new JsModel();
		TestStatus testStatusEntity = testLog.getStatus();
		TestStatusValue testStat = testStatusEntity.getStatusValue();
		String statVal = testStat.ordinal() + "";
		String errMsg = testLog.getMessage() == null ? "" : testLog.getMessage();
		long startTime = testLog.getStartTime();
		long elapsedTime = testLog.getEndTime() - startTime;

		status.props.add(new JsModelProperty("status", statVal, null));
		status.props.add(new JsModelProperty("startTime", String.valueOf(startTime), null));
		status.props.add(new JsModelProperty("elapsedTime", String.valueOf(elapsedTime), null));
		if (testStat == TestStatusValue.FAILED || testStat == TestStatusValue.ERROR) {
			status.props.add(errMsg.equals("") ? new JsModelProperty("errMessage", EMPTY_STRING_INDEX, null)
					: new JsModelProperty("errMessage", errMsg, listStrings));
		}
	}

	public StringBuilder toArrayString() {
		StringBuilder sb = new StringBuilder();
		// Start test
		sb.append(ARRAY_OPEN);

		// Properties
		for (JsModelProperty prop : props) {
			sb.append(prop.getPropertyValue());
			sb.append(ARRAY_DLMT);
		}
		// tags
		sb.append(tags.toArrayString());
		sb.append(ARRAY_DLMT);
		// Status
		sb.append(status.toArrayString());
		sb.append(ARRAY_DLMT);
		// Steps
		sb.append(ARRAY_OPEN);
		for (int i = 0; i < steps.size(); i++) {
			sb.append(steps.get(i).toArrayString());
			if (i < steps.size() - 1) {
				sb.append(ARRAY_DLMT);
			}
		}
		sb.append(ARRAY_CLOSE);
		sb.append(ARRAY_DLMT);

		// Test Data
		// Variable's Names
		sb.append(ARRAY_OPEN);
		for (int i = 0; i < names.props.size(); i++) {
			sb.append(names.props.get(i).getPropertyValue());
			if (i < names.props.size() - 1) {
				sb.append(ARRAY_DLMT);
			}
		}
		sb.append(ARRAY_CLOSE);
		sb.append(ARRAY_DLMT);

		// Variable's values
		sb.append(ARRAY_OPEN);
		for (int i = 0; i < values.props.size(); i++) {
			sb.append(values.props.get(i).getPropertyValue());
			if (i < values.props.size() - 1) {
				sb.append(ARRAY_DLMT);
			}
		}
		sb.append(ARRAY_CLOSE);

		// End test
		sb.append(ARRAY_CLOSE);
		return sb;
	}

	public List<JsStepModel> getSteps() {
		return steps;
	}
}
