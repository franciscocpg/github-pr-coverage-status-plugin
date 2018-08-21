/*

    Copyright 2015-2016 Artem Stasiuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */
package com.github.terma.jenkins.githubprcoveragestatus;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * For more info about Clover see:
 * <a href="https://answers.atlassian.com/questions/203628/clover-xml-number-of-covered-lines>
 * https://answers.atlassian.com/questions/203628/clover-xml-number-of-covered-lines</a>
 * <a href="https://wiki.jenkins-ci.org/display/JENKINS/Clover+Plugin>
 * https://wiki.jenkins-ci.org/display/JENKINS/Clover+Plugin</a>
 * <a
 * href="https://phpunit.de/manual/current/en/logging.html#logging.codecoverage.xml"
 * https://phpunit.de/manual/current/en/logging.html#logging.codecoverage.xml</a>
 */
class CloverParser implements CoverageReportParser, CoverageByProjectPackageReportParser {

    private static final String PROJECT_NAME_XPATH = "/coverage/project/@name";
    private static final String TOTAL_STATEMENTS_XPATH = "/coverage/project/metrics/@statements";
    private static final String COVER_STATEMENTS_XPATH = "/coverage/project/metrics/@coveredstatements";
    private static final String COVER_PACKAGES_XPATH = "/coverage/project/metrics/package";

    private int getByXpath(final String filePath, final String content, final String xpath) {
        try {
            return Integer.parseInt(XmlUtils.findInXml(content, xpath));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Strange Clover report!\n"
                    + "File path: " + filePath + "\n"
                    + "Can't extract float value by XPath: " + xpath + "\n"
                    + "from:\n" + content, e);
        }
    }

    private static String readContent(String path) {
        try {
            return FileUtils.readFileToString(new File(path));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Can't read Clover report by path: " + path);
        }
    }

    private static Float getCoverage(final float statements, final float coveredStatements) {
        if (statements == 0) {
            return 0f;
        } else {
            return coveredStatements / statements;
        }
    }

    @Override
    public float get(final String cloverFilePath) {
        final String content = readContent(cloverFilePath);

        final float statements = getByXpath(cloverFilePath, content, TOTAL_STATEMENTS_XPATH);
        final float coveredStatements = getByXpath(cloverFilePath, content, COVER_STATEMENTS_XPATH);

        return getCoverage(statements, coveredStatements);
    }

    private static Float getCoverageFromNodeMetrics(Node node) {
        final float statements = Float.parseFloat(XmlUtils.getNodeAttributeValue(node, "statements"));
        final float coveredstatements = Float.parseFloat(XmlUtils.getNodeAttributeValue(node, "coveredstatements"));

        return getCoverage(statements, coveredstatements);
    }

    private static void addFilesCoverage(Map<String, Float> filesCoverage, Node node) {
        NodeList childNodeList = node.getChildNodes();
        String fileName = XmlUtils.getNodeAttributeValue(node, "name");
        for (int i = 0; i < childNodeList.getLength(); i++) {
            Node childNode = childNodeList.item(i);
            if (childNode.getNodeName().equals("metrics")) {
                filesCoverage.put(fileName, getCoverageFromNodeMetrics(childNode));
                break;
            }
        }
    }

    @Override
    public Map<String, Map<String, PackageCoverage>> getByProjectPackage(final String cloverFilePath) {
        final Map<String, PackageCoverage> packageCoverage = new TreeMap<String, PackageCoverage>();
        final String content = readContent(cloverFilePath);

        NodeList nodeList = XmlUtils.findNodeInXml(content, COVER_PACKAGES_XPATH);
        for (int i = 0; i < nodeList.getLength(); i++) {
            float coverage = 0f;
            Map<String, Float> filesCoverage = new TreeMap<String, Float>();

            Node node = nodeList.item(i);
            String packageName = XmlUtils.getNodeAttributeValue(node, "name");
            NodeList childNodeList = node.getChildNodes();

            for (int j = 0; j < childNodeList.getLength(); j++) {
                Node childNode = childNodeList.item(j);

                if (childNode.getNodeName().equals("metrics")) {
                    coverage = getCoverageFromNodeMetrics(childNode);
                } else if (childNode.getNodeName().equals("file")) {
                    addFilesCoverage(filesCoverage, childNode);
                }
            }

            packageCoverage.put(packageName, new PackageCoverage(coverage, filesCoverage));
        }

        final Map<String, Map<String, PackageCoverage>> projectPackageCoverage = new TreeMap<String, Map<String, PackageCoverage>>();
        String projectName = XmlUtils.findInXml(content, PROJECT_NAME_XPATH);
        projectPackageCoverage.put(projectName, packageCoverage);

        return projectPackageCoverage;
    }

}
