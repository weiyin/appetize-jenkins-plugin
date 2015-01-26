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

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Developers: Weiyin He and John Snyder
 */
@NameWith(value=AppetizeCredentials.NameProvider.class)
public class AppetizeCredentials extends BaseStandardCredentials {
    private static final long serialVersionUID = 1L;

    // Appetize.io API token
    private final Secret apiToken;

    @DataBoundConstructor
    public AppetizeCredentials(CredentialsScope scope, String id, String description, String apiToken) {
        super(scope, id, description);
        this.apiToken = Secret.fromString(apiToken);
    }

    public Secret getApiToken() {
        return apiToken;
    }

    public String toString() {
        String description = Util.fixEmptyAndTrim(getDescription());
        return "API Token" + (description != null ? " (" + description + ")" : "");
    }

    @Extension
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        @Override
        public String getDisplayName() {
            return "Appetize.io Credentials";
        }
    }

    public static class NameProvider extends CredentialsNameProvider<StandardCredentials> {
        @NonNull
        @Override
        public String getName(StandardCredentials appetizeCredentials) {
            return appetizeCredentials.toString();
        }
    }
}
