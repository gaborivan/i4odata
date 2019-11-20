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

package io.i4tech.odata.common.client;


import io.i4tech.odata.common.mapper.ODataEntityMapper;
import io.i4tech.odata.common.authorization.ODataAuthorization;
import org.apache.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ODataClientBuilder {

    protected Map<String, String> headers = new HashMap<>();
    protected Map<String, String> metadataArguments = new HashMap<>();
    protected Proxy proxy;
    protected String serviceUrl;
    protected Locale locale;
    protected ODataAuthorization authorization;
    protected HttpClient httpClient;
    protected ODataEntityMapper mapper;
    protected ODataClientBuilder() {
        // protected access only
    }

    public ODataClient build() {
        return new ODataClient(serviceUrl, authorization, headers, metadataArguments, proxy, locale, httpClient, mapper);
    }

    public final ODataClientBuilder serviceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        return this;
    }

    public final ODataClientBuilder metadataArgument(String key, String value) {
        metadataArguments.put(key, value);
        return this;
    }

    public final ODataClientBuilder authorization(ODataAuthorization authorization) {
        this.authorization = authorization;
        return this;
    }

    public final ODataClientBuilder mapper(ODataEntityMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public final ODataClientBuilder proxy(String host, int port) {
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        return this;
    }

    public final ODataClientBuilder language(Locale locale) {
        this.locale = locale;
        return this;
    }

    public final ODataClientBuilder header(String header, String value) {
        headers.put(header, value);
        return this;
    }

    public final ODataClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }
}
