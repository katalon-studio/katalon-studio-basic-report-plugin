# katalon-studio-report-plugin
Katalon Studio Report Plugin is a Custom Keyword Plugin that replaces for the current Report feature of Katalon Studio. Starting from v6.1.5, the Report feature is no longer available on Katalon Studio, users need to download this plugin to continue using.

**Katalon Studio Report Plugin provides:**
- Generates automatically report from Test Suite report after every test execution with various formats: HTML, CSV, JUnit, and PDF.
- Allows users to export manually Test Suite and Test Suite collection report to HTML, CSV, JUnit, and PDF. Right click on the report and choose Export as.

**Limitation:**
- Katalon Studio Report Plugin only automatically generates reports for Test Suite, not for Test Suite Collection.
- Katalon Studio Report Plugin only supports HTML format for Test Suite Collection.

## Setup project
1. Run: 
```sh
gradle copyMyDependencies
```
2. Open Katalon Studio and open this project

## Build project
1. Run:
```sh
gradle jar
```
2. Copy *build/libs/katalon-studio-report-plugin.jar* after running command ```gradle jar``` and paste into Plugins folder of another Katalon Studio project or upload to Katalon Store

## Usage
[Usage guide](docs/tutorials/usage.md)
