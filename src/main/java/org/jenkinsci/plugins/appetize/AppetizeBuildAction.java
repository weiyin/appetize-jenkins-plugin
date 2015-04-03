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

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;

/**
 * Developers: Weiyin He and John Snyder
 */
public class AppetizeBuildAction extends AppetizeApp implements EnvironmentContributingAction {
    private int buildNumber;

    public AppetizeBuildAction(String platform, String privateKey, String publicKey, String publicUrl, String manageUrl, int buildNumber) {
        super(platform, privateKey, publicKey, publicUrl, manageUrl);
        this.buildNumber = buildNumber;
    }

    public void buildEnvVars(AbstractBuild<?, ?> abstractBuild, EnvVars envVars) {
        if (envVars == null) return;

        envVars.put("APPETIZEIO_PUBLIC_KEY", getPublicKey());
        envVars.put("APPETIZEIO_PRIVATE_KEY", getPrivateKey());
        envVars.put("APPETIZEIO_PUBLIC_URL", getPublicUrl());
        envVars.put("APPETIZEIO_MANAGE_URL", getManageUrl());
    }

    @Override
    public String getIconFileName() {
        // don't show anything on left sidebar
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    public int getBuildNumber() {
        return buildNumber;
    }
}
