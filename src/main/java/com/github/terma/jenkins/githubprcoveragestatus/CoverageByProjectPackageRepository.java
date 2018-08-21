package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.FilePath;
import java.io.IOException;
import java.util.Map;

interface CoverageByProjectPackageRepository {

    Map<String, Map<String, PackageCoverage>> get(FilePath workspace) throws IOException, InterruptedException;

}
