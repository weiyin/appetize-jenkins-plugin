/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Appetize.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.appetize;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Developers: Weiyin He and John Snyder
 */
public class AppetizeRecorder extends Recorder {
    private static final String PLACEHOLDER_API_TOKEN = "tok_7vkmr5quwwjjxy4rv1q1h0rn08";
    private static final String PLACEHOLDER_ID = "placeholder";

    private final String platform;
    private final String appPath;
    private final String apiTokenId;

    @DataBoundConstructor
    public AppetizeRecorder(String platform, String appPath, String apiTokenId) {
        this.platform = platform;
        this.appPath = appPath;
        this.apiTokenId = apiTokenId;
    }

    public String getPlatform() {
        return platform;
    }

    public String getApiTokenId() {
        return apiTokenId;
    }

    public String getAppPath() {
        return appPath;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        ArrayList<AppetizeProjectAction> collection = new ArrayList<AppetizeProjectAction>(1);
        AppetizeProjectAction action = new AppetizeProjectAction(project);
        if (action != null) collection.add(action);
        return collection;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        // only run on SUCCESS
        if (!build.getResult().isBetterOrEqualTo(Result.SUCCESS)) return false;

        PrintStream logger = listener.getLogger();

        // check platform
        if (!platform.equalsIgnoreCase("ios") && !platform.equalsIgnoreCase("android")) {
            logger.println("Error: Invalid platform " + platform);
            return false;
        }

        // check appPath exists
        if (appPath == null || appPath.isEmpty()) {
            logger.println("Error: Empty appPath");
            return false;
        }
        FilePath appLocation = new FilePath(build.getWorkspace(), appPath);
        if ((platform.equalsIgnoreCase("ios") && !appLocation.isDirectory()) ||
                (platform.equalsIgnoreCase("android") && !appLocation.exists())) {
            logger.println("Error: could not find app in " + appLocation.getRemote());
            return false;
        }

        // get api token
        String apiToken = null;
        if (apiTokenId == null || apiTokenId.isEmpty() || apiTokenId.equals(PLACEHOLDER_ID)) {
            apiToken = PLACEHOLDER_API_TOKEN;
        } else {
            List<AppetizeCredentials> credentialsList = CredentialsProvider.lookupCredentials(
                    AppetizeCredentials.class, (Item)null,
                    ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
            for(AppetizeCredentials credentials : credentialsList) {
                if (apiTokenId.equals(credentials.getId())) {
                    apiToken = credentials.getApiToken().getPlainText();
                    break;
                }
            }

            if (apiToken == null) {
                logger.println("Error looking up appetize.io credentials. Please reconfigure the appetize.io post-build action");
                return false;
            }
        }

        // get pre-signed url
        AppetizeApiService appetize = new AppetizeApiService(logger);
        AppetizeApiService.AppetizePresignedUrls urls = appetize.getPresignedUrls();
        if (urls == null) {
            logger.println("Error getting appetize.io upload URL");
            return false;
        }

        // prepare upload
        FilePath uploadFile;
        File zipFile = null;
        String uploadUrl = null;
        if (platform.equalsIgnoreCase("ios")) {
            try {
                zipFile = File.createTempFile("appetize", ".zip");
                uploadFile = new FilePath(zipFile);
                appLocation.zip(uploadFile);
            } catch (Exception e) {
                logger.println("Error creating zip file in " + zipFile.toString());
                return false;
            }
            uploadUrl = urls.iosUrl;
        }
        else {
            uploadFile = appLocation;
            uploadUrl = urls.androidUrl;
        }

        // upload file
        if (!appetize.uploadData(uploadFile.read(), uploadUrl)) {
            return false;
        }

        String jobUUID;
        try {
            String projectName = build.getProject().getName();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String jenkinsUUID = getDescriptor().getJenkinsUUID();
            if (jenkinsUUID != null) digest.update(jenkinsUUID.getBytes());
            if (projectName != null) digest.update(projectName.getBytes());
            if (jenkinsUUID != null) digest.update(jenkinsUUID.getBytes());

            byte[] hash = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            jobUUID = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return false;
        }

        // hit api to create app
        AppetizeApiService.AppetizeUpdateParams params = new AppetizeApiService.AppetizeUpdateParams();
        params.url = uploadUrl;
        params.platform = platform;
        params.token = apiToken;
        params.source = "appetize-jenkins-plugin";
        params.jenkinsUUID = getDescriptor().getJenkinsUUID();
        params.jobUUID = jobUUID;
        params.buildNumber = build.getNumber();
        AppetizeApiService.AppetizeUpdateResult result = appetize.updateApp(params);
        if (result == null) {
            logger.println("Error calling Appetize.io API");
            return false;
        }

        logger.println("Success uploading to Appetize.io");
        logger.println("You can view your app at " + result.publicURL);
        logger.println("You can manage your app at " + result.manageURL);

        // add action to build
        build.addAction(new AppetizeBuildAction(platform, result.privateKey, result.publicKey,
                result.publicURL, result.manageURL, build.getNumber()));

        return true;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String jenkinsUUID;

        public DescriptorImpl() {
            load();
            if (jenkinsUUID == null || jenkinsUUID.isEmpty()) {
                jenkinsUUID = UUID.randomUUID().toString();
                save();
            }
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Upload to Appetize.io";
        }

        public String getJenkinsUUID() {
            return jenkinsUUID;
        }

        public ListBoxModel doFillApiTokenIdItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Placeholder API Token", PLACEHOLDER_ID);

            List<AppetizeCredentials> credentialsList = CredentialsProvider.lookupCredentials(
                    AppetizeCredentials.class, (Item)null,
                    ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
            for (AppetizeCredentials credentials : credentialsList) {
                items.add(credentials.toString(), credentials.getId());
            }

            return items;
        }
    }
}
