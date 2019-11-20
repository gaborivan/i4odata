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

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.i4tech.odata.common.authorization.ODataAuthorization;
import io.i4tech.odata.common.mapper.ODataEntityMapper;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataErrorResponse;
import io.i4tech.odata.common.util.ODataEntityUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;

import javax.xml.bind.annotation.XmlElement;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ODataClient {

    protected static final String DEFAULT_CONTENT_TYPE = HttpContentType.APPLICATION_XML;
    private static final String X_CSRF_TOKEN = "x-csrf-token";
    private static final String X_CSRF_TOKEN_FETCH = "Fetch";
    private static final String X_CSRF_TOKEN_REQUIRED = "Required";

    @Getter
    protected final String serviceUrl;

    @Getter
    protected ODataAuthorization authorization;

    protected final Map<String, String> metadataArguments;

    protected final Proxy proxy;

    protected final Locale defaultLocale;

    protected final Map<String, String> headers;

    protected Edm edm;

    protected HttpClient httpClient;

    protected String csrfToken;

    protected final ODataEntityMapper mapper;

    public Locale getRequestLocale() {
        return requestLocale.get();
    }

    public void setRequestLocale(Locale requestLocale) {
        this.requestLocale.set(requestLocale);
    }

    protected final ThreadLocal<Locale> requestLocale;


    protected ODataClient(String serviceUrl, ODataAuthorization authorization, Map<String, String> headers,
                          Map<String, String> metatataArgs, Proxy proxy, Locale locale, HttpClient httpClient, ODataEntityMapper mapper) {
        this.serviceUrl = serviceUrl;
        this.headers = headers;
        this.proxy = proxy;
        this.metadataArguments = metatataArgs;
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.defaultLocale = (locale == null ? Locale.getDefault() : locale);
        this.requestLocale = new ThreadLocal();
        this.requestLocale.set(this.defaultLocale);
        this.authorization = authorization;
    }

    protected String getRequestUri(String requestPath) {
        final String url = getServiceUrl();
        StringWriter uri = new StringWriter().append(url);
        if (!url.endsWith("/") && !requestPath.startsWith("/")) {
            uri.append("/");
        }
        return uri.append(requestPath).toString()
                .replace(" ", "%20")
                .replace("'", "%27");
    }

    protected void setReadRequestHeaders(HttpUriRequest request, Map<String, String> extraHeaders) {
        headers.forEach(request::setHeader);
        getAuthorization().getHeaders().forEach(request::setHeader);
        if (extraHeaders != null) {
            extraHeaders.forEach(request::setHeader);
        }
        request.setHeader(HttpHeaders.ACCEPT_LANGUAGE, requestLocale.get().getLanguage());
        request.setHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
        request.setHeader(HttpHeaders.ACCEPT, DEFAULT_CONTENT_TYPE);
    }

    protected void setWriteRequestHeaders(HttpUriRequest request, Map<String, String> extraHeaders) {
        setReadRequestHeaders(request, extraHeaders);
        if (csrfToken == null) {
            fetchCsrfToken();
        }
        request.setHeader(X_CSRF_TOKEN, csrfToken);
    }

    protected HttpHead headRequest(String requestPath) {
        return headRequest(requestPath, null);
    }

    protected HttpHead headRequest(String requestPath, Map<String, String> extraHeaders) {
        final HttpHead request = new HttpHead(getRequestUri(requestPath));
        setReadRequestHeaders(request, extraHeaders);
        return request;
    }

    protected HttpGet getRequest(String requestPath) {
        return getRequest(requestPath, null);
    }

    protected HttpGet getRequest(String requestPath, Map<String, String> extraHeaders) {
        final HttpGet request = new HttpGet(getRequestUri(requestPath));
        setReadRequestHeaders(request, extraHeaders);
        return request;
    }

    protected HttpPost postRequest(String requestPath) {
        return postRequest(requestPath, null);
    }

    protected HttpPost postRequest(String requestPath, Map<String, String> extraHeaders) {
        final HttpPost request = new HttpPost(getRequestUri(requestPath));
        setWriteRequestHeaders(request, extraHeaders);
        return request;
    }

    protected HttpPatch patchRequest(String requestPath) {
        return patchRequest(requestPath, null);
    }

    protected HttpPatch patchRequest(String requestPath, Map<String, String> extraHeaders) {
        final HttpPatch request = new HttpPatch(getRequestUri(requestPath));
        setWriteRequestHeaders(request, extraHeaders);
        return request;
    }

    protected HttpDelete deleteRequest(String requestPath) {
        return deleteRequest(requestPath, null);
    }

    protected HttpDelete deleteRequest(String requestPath, Map<String, String> extraHeaders) {
        final HttpDelete request = new HttpDelete(getRequestUri(requestPath));
        setWriteRequestHeaders(request, extraHeaders);
        return request;
    }

    protected ODataErrorResponse getErrorResponse(HttpResponse response) {
        ODataErrorResponse error = null;
        try {
            error = new XmlMapper().readValue(response.getEntity().getContent(), ODataErrorResponse.class);
        } catch (Exception e) {
            // Error cannot be mapped
        }
        return error;
    }

    protected HttpResponse executeRequest(HttpUriRequest request) throws IOException {
        HttpResponse response = httpClient.execute(request);
        HttpStatusCodes httpStatusCode = HttpStatusCodes.fromStatusCode(response.getStatusLine().getStatusCode());
        log.debug("Executed request '{}' -> Status {}", request, httpStatusCode);
        if (httpStatusCode.getStatusCode() == 403 && Arrays.stream(response.getHeaders(X_CSRF_TOKEN)).anyMatch(h -> X_CSRF_TOKEN_REQUIRED.equals(h.getValue()))) {
            csrfToken = null;
            throw new CsrfTokenValidationFailedException();
        }
        if (400 <= httpStatusCode.getStatusCode() && httpStatusCode.getStatusCode() <= 599) {
            ODataErrorResponse error = getErrorResponse(response);
            if (error != null) {
                throw new ODataException(String.format("[%s]: %s", StringUtils.defaultString(error.getCode()), StringUtils.defaultString(error.getMessage())));
            } else {
                throw new ODataException("Http Connection to '" + request.getURI() + "' failed with status " + httpStatusCode.getStatusCode() + " " + httpStatusCode.toString());
            }
        }
        return response;
    }

    private void fetchCsrfToken() {
        final HttpGet request = getRequest("", Collections.singletonMap(X_CSRF_TOKEN, X_CSRF_TOKEN_FETCH));
        HttpResponse response = null;
        try {
            response = executeRequest(request);
            csrfToken = Arrays.stream(response.getHeaders(X_CSRF_TOKEN))
                    .findFirst()
                    .map(h -> h.getValue())
                    .orElse(null);
        } catch (ODataException  e) {
            throw e;
        } catch (Exception e) {
            throw new ODataException(e);
        } finally {
            request.releaseConnection();
        }
    }

    private String toQueryString(Map<String, String> args) {
        final StringBuilder queryString = new StringBuilder();
        final Iterator<Map.Entry<String, String>> arguments = args.entrySet().iterator();
        for (boolean isFirst = true; arguments.hasNext(); isFirst = false) {
            final Map.Entry<String, String> entry = arguments.next();
            queryString.append(isFirst ? "?" : "&");
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return queryString.toString();
    }

    public Edm loadEdm() {
        final String metadataUri = "$metadata" + toQueryString(metadataArguments);
        final HttpGet request = getRequest(metadataUri);
        HttpResponse response = null;
        try {
            response = executeRequest(request);
            edm = EntityProvider.readMetadata(response.getEntity().getContent(), false);
        } catch (ODataException  e) {
            throw e;
        } catch (Exception  e) {
            throw new ODataException(e);
        } finally {
            request.releaseConnection();
        }
        return edm;
    }

    protected Edm getEdm() {
        if (edm == null) {
            loadEdm();
        }
        return edm;
    }

    protected <E extends ODataEntity> EdmEntitySet getEntitySet(String entitySetName) throws EdmException {
        return getEdm().getDefaultEntityContainer().getEntitySet(entitySetName);
    }

    private Map<String, Object> getTimestampFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.getType().isAssignableFrom(Timestamp.class))
                .collect(Collectors.toMap(f -> f.getAnnotation(XmlElement.class).name(), Field::getType));
    }

    private Map<String, Object> getTypeMap(Class<?> entityClass) {
        final Map<String, Object> typeMap = getTimestampFields(entityClass);
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> ODataEntity.class.isAssignableFrom(ODataEntityUtils.getGenericType(f)))
                .map(f -> getTimestampFields(ODataEntityUtils.getGenericType(f)))
                .forEach(typeMap::putAll);
        return typeMap;
    }

    protected <E extends ODataEntity> E readContentEntry(InputStream content, Class<E> entityClass) throws EdmException, EntityProviderException {
        ODataEntry entry = EntityProvider.readEntry(DEFAULT_CONTENT_TYPE,
                getEntitySet(ODataEntityUtils.getEntitySetName(entityClass)),
                content,
                EntityProviderReadProperties.init()
                        .addTypeMappings(getTypeMap(entityClass))
                        .build());
        return mapper.mapPropertiesToEntity(entry.getProperties(), entityClass);
    }

    protected EntityProviderReadProperties getReadProperties(Class<?> entityClass) {
        return EntityProviderReadProperties.init()
                .addTypeMappings(getTypeMap(entityClass))
                .build();
    }


    protected <E extends ODataEntity> ODataResponse<E> readContentStream(InputStream content, String entitySetName, Class<E> entityClass) throws EdmException, EntityProviderException, IOException {
        final ODataFeed feed = EntityProvider.readFeed(DEFAULT_CONTENT_TYPE, getEntitySet(entitySetName), content, getReadProperties(entityClass));
        final ODataResponse<E> response = new ODataResponse<>(feed.getEntries().stream()
                .map(e -> mapper.mapPropertiesToEntity(e.getProperties(), entityClass))
                .collect(Collectors.toList()),
                feed.getFeedMetadata().getInlineCount() != null ? feed.getFeedMetadata().getInlineCount() : 0);

        // if server-side paging is present, fetch next page
        if (StringUtils.isNotBlank(feed.getFeedMetadata().getNextLink())) {
            final String linkPath = feed.getFeedMetadata().getNextLink().replace(getServiceUrl(), StringUtils.EMPTY);
            response.merge(read(entitySetName, entityClass, linkPath));
        }
        return response;
    }

    protected EntityProviderWriteProperties getWriteProperties(EdmEntitySet entitySet, Map<String, Object> dataMap) throws EdmException, URISyntaxException {
        // identify only properties for which the data were provided
        final ExpandSelectTreeNode expandSelectTree = ExpandSelectTreeNode.entitySet(entitySet)
                .selectedProperties(dataMap.keySet().stream()
                        .filter(k -> !"etag".equals(k))
                        .collect(Collectors.toList()))
                .build();

        return EntityProviderWriteProperties
                .serviceRoot(new URI(entitySet.getName()))
                .omitETag(true)
                .contentOnly(true)
                .expandSelectTree(expandSelectTree)
                .build();
    }

    protected InputStream writeContentStream(String entitySetName, Map<String, Object> dataMap) throws EdmException, URISyntaxException, EntityProviderException {
        final EdmEntitySet entitySet = getEntitySet(entitySetName);
        final EntityProviderWriteProperties properties = getWriteProperties(entitySet, dataMap);

        // serialize data into ODataResponse object
        return (InputStream) EntityProvider.writeEntry(DEFAULT_CONTENT_TYPE, entitySet, dataMap, properties).getEntity();
    }

    protected <E extends ODataEntity> InputStream writeContentStream(E data, Class<E> entityClass) throws EdmException, URISyntaxException, EntityProviderException {
        final Map<String, Object> dataMap = mapper.mapEntityToProperties(data, entityClass);

        return writeContentStream(ODataEntityUtils.getEntitySetName(entityClass), dataMap);
    }


    public <E extends ODataEntity> ODataResponse<E> get(Class<E> entityClass, String requestPath) {
        final HttpGet request = getRequest(requestPath);
        HttpResponse response = null;
        try {
            response = executeRequest(request);
            final InputStream content = response.getEntity().getContent();
            final E entry = readContentEntry(content, entityClass);

            return new ODataResponse<>(entry);
        } catch (Exception e) {
            throw new ODataException(e);
        } finally {
            request.releaseConnection();
        }
    }

    public <E extends ODataEntity> ODataResponse<E> read(String entitySet, Class<E> entityClass, String requestPath) {
        final HttpGet request = getRequest(requestPath);
        HttpResponse response = null;
        try {
            response = executeRequest(request);
            final byte[] content = IOUtils.toByteArray(response.getEntity().getContent());
            request.releaseConnection();

            try {
                return readContentStream(new ByteArrayInputStream(content), entitySet, entityClass);
            } catch (EntityProviderException e) {
                try {
                    return new ODataResponse<>(Collections.singletonList(
                            readContentEntry(new ByteArrayInputStream(content), entityClass)));
                } catch (EntityProviderException e1) {
                    return new ODataResponse<>(new String(content));
                }
            }
        } catch (Exception e) {
            throw new ODataException(e);
        } finally {
            request.releaseConnection();
        }
    }


    public <E extends ODataEntity> ODataResponse<E> create(Class<E> entityClass, String requestPath, E data) {
        final HttpPost request = postRequest(requestPath);
        HttpResponse response = null;
        try {
            final InputStream contentStream = writeContentStream(data, entityClass);
            request.setEntity(new InputStreamEntity(contentStream));

            response = executeRequest(request);

            final E result = readContentEntry(response.getEntity().getContent(), entityClass);
            return new ODataResponse<>(result);
        } catch (CsrfTokenValidationFailedException e) {
            // retry with new token
            return create(entityClass, requestPath, data);
        } catch (ODataException e) {
            throw e;
        } catch (Exception e) {
            throw new ODataException(e);
        } finally {
            request.releaseConnection();
        }
    }

    public <E extends ODataEntity> ODataResponse<E> update(Class<E> entityClass, String requestPath, E data) {
        final HttpPatch request = patchRequest(requestPath);
        HttpResponse response = null;
        try {
            final InputStream contentStream = writeContentStream(data, entityClass);
            request.setEntity(new InputStreamEntity(contentStream));

            response = executeRequest(request);

            ODataResponse<E> result = null;
            if (ODataEntityUtils.allKeyFieldsSet(data)) {
                result = new ODataResponse<>(data);
            } else {
                result = this.get(entityClass, requestPath);
            }
            return result;
        } catch (CsrfTokenValidationFailedException e) {
            // retry with new token
            return create(entityClass, requestPath, data);
        } catch (ODataException e) {
            throw e;
        } catch (Exception e) {
            throw new ODataException(e);
        } finally {
            request.releaseConnection();
        }
    }

    public <E extends ODataEntity> ODataResponse<E> delete(Class<E> entityClass, String requestPath) {
        final HttpDelete request = deleteRequest(requestPath);
        HttpResponse response = null;
        try {
            response = executeRequest(request);
            return new ODataResponse<E>();
        } catch (CsrfTokenValidationFailedException e) {
            // retry with new token
            return delete(entityClass, requestPath);
        } catch (ODataException e) {
            throw e;
        } catch (Exception e) {
            throw new ODataException(e);
        } finally {
            request.releaseConnection();
        }
    }


    public <E extends ODataEntity> ODataResponse<E> function(Class<E> entityClass, String requestPath, Map<String, String> postParameters) {
        HttpRequestBase request = null;
        if (postParameters.keySet().isEmpty()) {
            request = getRequest(requestPath);
        } else {
            throw new NotImplementedException("OData POST function calls are not implemented.");
        }
        try {
            HttpResponse response = executeRequest(request);
            final byte[] content = IOUtils.toByteArray(response.getEntity().getContent());
            request.releaseConnection();

            try {
                final String entitySetName = ODataEntityUtils.getEntitySetName(entityClass);
                return readContentStream(new ByteArrayInputStream(content), entitySetName, entityClass);
            } catch (EntityProviderException e) {
                try {
                    return new ODataResponse<>(Collections.singletonList(
                            readContentEntry(new ByteArrayInputStream(content), entityClass)));
                } catch (EntityProviderException e1) {
                    return new ODataResponse<>(new String(content));
                }
            }
        } catch (Exception e) {
            throw new ODataException(e);
        } finally {
            request.releaseConnection();
        }
    }


    public boolean testConnection() {
        HttpRequestBase request = getRequest(StringUtils.EMPTY);
        try {
            executeRequest(request);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            request.releaseConnection();
        }
    }


    public static ODataClientBuilder builder() {
        return new ODataClientBuilder();
    }

}
