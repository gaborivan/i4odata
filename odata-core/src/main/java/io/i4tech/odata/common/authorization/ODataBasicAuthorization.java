/*
 * MIT License
 *
 * Copyright (c) 2019 i4tech Kft.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.i4tech.odata.common.authorization;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ODataBasicAuthorization implements ODataAuthorization {

    public static class ODataBasicAuthorizationBuilder {

        private String username;
        private String password;

        public ODataBasicAuthorizationBuilder username(String username) {
            this.username = username;
            return this;
        }

        public ODataBasicAuthorizationBuilder password(String password) {
            this.password = password;
            return this;
        }

        public ODataBasicAuthorization build() {
            return new ODataBasicAuthorization(username, password);
        }
    }

    private final String username;
    private final String password;

    private ODataBasicAuthorization(String username, String password) {
        this.username = username;
        this.password = password;
    }

    private String encodeBasicAuthenticationString() {
        if (StringUtils.isNoneBlank(username) && StringUtils.isNoneBlank(password)) {
            final String credentialsPair = username + ":" + password;
            final String encoded = Base64.getEncoder().encodeToString(credentialsPair.getBytes(StandardCharsets.UTF_8));
            return "Basic " + encoded;
        }
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, encodeBasicAuthenticationString());
        return headers;
    }


    public static ODataBasicAuthorizationBuilder builder() {
        return new ODataBasicAuthorizationBuilder();
    }
}
