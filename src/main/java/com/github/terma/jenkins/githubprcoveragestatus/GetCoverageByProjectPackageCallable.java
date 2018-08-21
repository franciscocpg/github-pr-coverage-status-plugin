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

import hudson.FilePath;
import hudson.Util;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jenkins.MasterToSlaveFileCallable;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

@SuppressWarnings("WeakerAccess")
final class GetCoverageByProjectPackageCallable
        extends MasterToSlaveFileCallable<Map<String, Map<String, PackageCoverage>>>
        implements CoverageByProjectPackageRepository {

    private static List<Map<String, Map<String, PackageCoverage>>> getCoveragesByPackage(File ws, String path, CoverageByProjectPackageReportParser parser) {
        FileSet fs = Util.createFileSet(ws, path);
        DirectoryScanner ds = fs.getDirectoryScanner();
        String[] files = ds.getIncludedFiles();
        List<Map<String, Map<String, PackageCoverage>>> cov = new ArrayList<Map<String, Map<String, PackageCoverage>>>();
        for (String file : files) {
            cov.add(parser.getByProjectPackage(new File(ds.getBasedir(), file).getAbsolutePath()));
        }
        return cov;
    }

    @Override
    public Map<String, Map<String, PackageCoverage>> get(final FilePath workspace) throws IOException, InterruptedException {
        if (workspace == null) {
            throw new IllegalArgumentException("Workspace should not be null!");
        }
        return workspace.act(new GetCoverageByProjectPackageCallable());
    }

    @Override
    public Map<String, Map<String, PackageCoverage>> invoke(final File ws, final VirtualChannel channel) throws IOException {
        List<Map<String, Map<String, PackageCoverage>>> cov = new ArrayList<Map<String, Map<String, PackageCoverage>>>();
        cov.addAll(getCoveragesByPackage(ws, "**/clover.xml", new CloverParser()));

        Map<String, Map<String, PackageCoverage>> coverageByProjectPackage = new TreeMap<String, Map<String, PackageCoverage>>();
        if (cov.isEmpty()) {
            return coverageByProjectPackage;
        }

        for (Map<String, Map<String, PackageCoverage>> map : cov) {
            for (Map.Entry<String, Map<String, PackageCoverage>> entry : map.entrySet()) {
                String project = entry.getKey();
                Map<String, PackageCoverage> packageCoverage = entry.getValue();

                coverageByProjectPackage.put(project, packageCoverage);
            }
        }

        return coverageByProjectPackage;
    }

}
