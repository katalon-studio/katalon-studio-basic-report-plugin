# katalon-studio-report-plugin
Katalon Studio Report Plugin is a Custom Keyword Plugin that replaces for the current Report feature of Katalon Studio. Starting from v6.1.5, the Report feature is no longer available on Katalon Studio, users need to download this plugin to continue using.

## Companion products

### Katalon TestOps

[Katalon TestOps](https://analytics.katalon.com) is a web-based application that provides dynamic perspectives and an insightful look at your automation testing data. You can leverage your automation testing data by transforming and visualizing your data; analyzing test results; seamlessly integrating with such tools as Katalon Studio and Jira; maximizing the testing capacity with remote execution.

* Read our [documentation](https://docs.katalon.com/katalon-analytics/docs/overview.html).
* Ask a question on [Forum](https://forum.katalon.com/categories/katalon-analytics).
* Request a new feature on [GitHub](CONTRIBUTING.md).
* Vote for [Popular Feature Requests](https://github.com/katalon-analytics/katalon-analytics/issues?q=is%3Aopen+is%3Aissue+label%3Afeature-request+sort%3Areactions-%2B1-desc).
* File a bug in [GitHub Issues](https://github.com/katalon-analytics/katalon-analytics/issues).

### Katalon Studio
[Katalon Studio](https://www.katalon.com) is a free and complete automation testing solution for Web, Mobile, and API testing with modern methodologies (Data-Driven Testing, TDD/BDD, Page Object Model, etc.) as well as advanced integration (JIRA, qTest, Slack, CI, Katalon TestOps, etc.). Learn more about [Katalon Studio features](https://www.katalon.com/features/).

### How to use
* Download project.
* Install gradle [here](https://gradle.org/releases/).
> :warning: Gradle version must be greater than 6.1.0 .
> :warning: Java version 17.
* Run command to install all dependencies.
> gradle katalonCopyDependencies
* Open project with Katalon Studio and work normally.

### How to build plugin
* Run command to build.
> gradle katalonPluginPackage