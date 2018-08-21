package com.github.terma.jenkins.githubprcoveragestatus;

import java.util.Map;

public class PackageCoverage {

    private final float coverage;
    private final Map<String, Float> filesCoverage;

    PackageCoverage(final float coverage, final Map<String, Float> filesCoverage) {
        this.coverage = coverage;
        this.filesCoverage = filesCoverage;
    }

    float getCoverage() {
        return coverage;
    }

    Map<String, Float> getFilesCoverage() {
        return filesCoverage;
    }

}
