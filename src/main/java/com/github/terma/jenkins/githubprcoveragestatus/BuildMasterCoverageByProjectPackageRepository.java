package com.github.terma.jenkins.githubprcoveragestatus;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;

public class BuildMasterCoverageByProjectPackageRepository implements MasterCoverageByProjectPackageRepository {

    private final PrintStream buildLog;

    public BuildMasterCoverageByProjectPackageRepository(final PrintStream buildLog) {
        this.buildLog = buildLog;
    }

    @Override
    public Map<String, Map<String, PackageCoverage>> get(String gitHubRepoUrl) {
        Map<String, Map<String, PackageCoverage>> cov = Collections.EMPTY_MAP;
        if (gitHubRepoUrl == null) {
            return cov;
        }

        cov = Configuration.DESCRIPTOR.getCoverageAndProjectPackage().get(gitHubRepoUrl);
        if (cov == null || cov.isEmpty()) {
            buildLog.println("Can't find master coverage repository: " + gitHubRepoUrl
                    + "Make sure that you have run build with step: " + MasterCoverageAction.DISPLAY_NAME);
        }
        return cov;
    }

}
