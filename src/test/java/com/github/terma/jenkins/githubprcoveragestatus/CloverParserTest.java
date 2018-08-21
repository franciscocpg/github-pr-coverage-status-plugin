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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class CloverParserTest {

    @Test
    public void extractCoverageFromCloverReport() throws IOException {
        String filePath = CloverParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CloverParserTest/clover.xml").getFile();

        Assert.assertEquals(0.5, new CloverParser().get(filePath), 0.1);
    }

    @Test
    public void extractZeroCoverageIfNoCoveredStatements() throws IOException {
        String filePath = CloverParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CloverParserTest/clover-zero-statements-coverage.xml").getFile();

        Assert.assertEquals(0, new CloverParser().get(filePath), 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionIfReportDoesntHaveStatementsAttribute() throws IOException {
        String filePath = CloverParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CloverParserTest/clover-invalid-no-statements.xml").getFile();

        Assert.assertEquals(0, new CloverParser().get(filePath), 0.1);
    }

    @Test
    public void extractZeroCoverageIfZeroStatements() throws IOException {
        String filePath = CloverParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CloverParserTest/clover-zero-statements.xml").getFile();

        Assert.assertEquals(0, new CloverParser().get(filePath), 0.1);
    }

    @Test
    public void extractCoverageByPackageFromCloverReport() throws IOException {
        String filePath = CloverParserTest.class.getResource(
                "/com/github/terma/jenkins/githubprcoveragestatus/CloverParserTest/clover-package.xml").getFile();

        Map<String, Map<String, PackageCoverage>> coverageByProjectPackage = new CloverParser().getByProjectPackage(filePath);

        Assert.assertEquals(1, coverageByProjectPackage.size());
        Assert.assertEquals("BankAccount", coverageByProjectPackage.keySet().iterator().next());

        Map<String, PackageCoverage> coverageByPackage = coverageByProjectPackage.get("BankAccount");
        Assert.assertEquals(2, coverageByPackage.size());
        Iterator<String> keys = coverageByPackage.keySet().iterator();

        Assert.assertEquals("bank.controller", keys.next());

        PackageCoverage packageCoverage = coverageByPackage.get("bank.controller");
        Assert.assertEquals(0.5494506f, packageCoverage.getCoverage(), 0.0f);

        Map<String, Float> filesCoverage = packageCoverage.getFilesCoverage();
        Assert.assertEquals(2, filesCoverage.size());

        Set<Map.Entry<String, Float>> entries = filesCoverage.entrySet();

        Iterator<Map.Entry<String, Float>> it = entries.iterator();
        Map.Entry<String, Float> entry = it.next();
        Assert.assertEquals("account.js", entry.getKey());
        Assert.assertEquals(0.375f, entry.getValue(), 0.0f);

        entry = it.next();
        Assert.assertEquals("bank.js", entry.getKey());
        Assert.assertEquals(0.6f, entry.getValue(), 0.0f);

        Assert.assertEquals("bank.service", keys.next());

        packageCoverage = coverageByPackage.get("bank.service");
        Assert.assertEquals(1f, packageCoverage.getCoverage(), 0.1f);

        filesCoverage = packageCoverage.getFilesCoverage();
        Assert.assertEquals(1, filesCoverage.size());

        entries = filesCoverage.entrySet();

        entry = entries.iterator().next();
        Assert.assertEquals("bank.js", entry.getKey());
        Assert.assertEquals(1f, entry.getValue(), 0.1f);
    }

}
