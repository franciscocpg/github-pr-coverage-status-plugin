package com.github.terma.jenkins.githubprcoveragestatus;

import java.util.Map;

interface MasterCoverageByProjectPackageRepository {

    Map<String, Map<String, PackageCoverage>> get(final String gitHubRepoUrl);

}
