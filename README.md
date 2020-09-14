[![Build Status](https://img.shields.io/endpoint?url=https%3A%2F%2Fstatusbadge-jx.apps.serv.run%2Fentando%2Fentando-plugin-jacms)](https://github.com/entando/devops-results/tree/logs/jenkins-x/logs/entando/entando-plugin-jacms/master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=entando_entando-plugin-jacms&metric=alert_status)](https://sonarcloud.io/dashboard?id=entando_entando-plugin-jacms)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=entando_entando-plugin-jacms&metric=coverage)](https://entando.github.io/devops-results/entando-plugin-jacms/master/jacoco/index.html)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=entando_entando-plugin-jacms&metric=vulnerabilities)](https://entando.github.io/devops-results/entando-plugin-jacms/master/dependency-check-report.html)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=entando_entando-plugin-jacms&metric=code_smells)](https://sonarcloud.io/dashboard?id=entando_entando-plugin-jacms)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=entando_entando-plugin-jacms&metric=security_rating)](https://sonarcloud.io/dashboard?id=entando_entando-plugin-jacms)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=entando_entando-plugin-jacms&metric=sqale_index)](https://sonarcloud.io/dashboard?id=entando_entando-plugin-jacms)

entando-plugin-jacms
============

**CMS**

**Code**: ```jacms```

**Description**

CMS is a plugin that allows to registered users to manage in the Back Office dynamic contents and digital assets.

**Installation**

In order to install the CMS plugin, you must insert the following dependency in the pom.xml file of your project:

```
<dependency>
       <groupId>org.entando.entando.bundles.app-view</groupId>
       <artifactId>entando-app-view-cms-default</artifactId>
       <version>${entando.version}</version>
       <type>war</type>
</dependency>
```

# Developing against local versions of upstream projects (e.g. admin-console,  entando-engine).

Full instructions on how to develop against local versions of upstream projects are available in the
[entando-parent-bom](https://github.com/entando/entando-core-bom) project. 
