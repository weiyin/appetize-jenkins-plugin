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

/**
 * Developers: Weiyin He and John Snyder
 */
public class AppetizeApp {
    private String platform;
    private String privateKey;
    private String publicKey;
    private String publicUrl;
    private String manageUrl;

    public AppetizeApp(String platform, String privateKey, String publicKey, String publicUrl, String manageUrl) {
        this.platform = platform;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.publicUrl = publicUrl;
        this.manageUrl = manageUrl;
    }

    public String getEmbedHtml() {
        String device = null;
        String width  = null;
        String height = null;

        if (platform.equalsIgnoreCase("ios")) {
            device = "iphone";
            width = "284px";
            height = "600px";
        }
        else if (platform.equalsIgnoreCase("android")) {
            device = "nexus5";
            width = "300px";
            height = "597px";
        }

        if (device != null && privateKey != null) {
            String sourceUrl = "https://appetize.io/embed/" + publicKey +
                    "?device=" + device + "&scale=75&autoplay=false&orientation=portrait&deviceColor=black";
            String iframe = String.format("<iframe src=\"%s\" width=\"%s\" height=\"%s\" frameborder=\"0\" scrolling=\"no\"></iframe>",
                    sourceUrl, width, height);
            return iframe;
        } else {
            return "";
        }
    }

    public String getPlatform() {
        return platform;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public String getManageUrl() {
        return manageUrl;
    }
}
