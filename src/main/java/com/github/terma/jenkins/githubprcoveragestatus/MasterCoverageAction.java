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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Record coverage of Jenkins Build and assume it as master coverage. Master
 * coverage will be used to compare Pull Request coverage and provide status
 * message in Pull Request. Optional step as coverage could be taken from Sonar.
 * Take a look on {@link Configuration}
 *
 * @see CompareCoverageAction
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class MasterCoverageAction extends Recorder implements SimpleBuildStep {

    public static final String DISPLAY_NAME = "Record Master Coverage";
    private static final long serialVersionUID = 1L;

    private Map<String, String> scmVars;

    @DataBoundConstructor
    public MasterCoverageAction() {

    }

    // TODO why is this needed for no public field ‘scmVars’ (or getter method) found in class ....
    public Map<String, String> getScmVars() {
        return scmVars;
    }

    @DataBoundSetter
    public void setScmVars(Map<String, String> scmVars) {
        this.scmVars = scmVars;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void perform(final Run build, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        if (build.getResult() != Result.SUCCESS) {
            return;
        }

        final PrintStream buildLog = listener.getLogger();
        final String gitUrl = PrIdAndUrlUtils.getGitUrl(scmVars, build, listener);
        final SettingsRepository settingsRepository = ServiceRegistry.getSettingsRepository();

        final boolean disableSimpleCov = settingsRepository.isDisableSimpleCov();
        final float masterCoverage = ServiceRegistry.getCoverageRepository(disableSimpleCov).get(workspace);
        buildLog.println("Master coverage " + Percent.toWholeString(masterCoverage));
        Configuration.setMasterCoverage(gitUrl, masterCoverage);

        final boolean showPackageDiff = settingsRepository.isShowPackageDiff();
        if (showPackageDiff) {
            Map<String, Map<String, PackageCoverage>> coverageByProjectPackage
                    = ServiceRegistry.getCoverageByProjectPackageRepository().get(workspace);
            Configuration.setMasterCoverage(gitUrl, coverageByProjectPackage);
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        @Nonnull
        public String getDisplayName() {
            return DISPLAY_NAME;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }

}
