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

package io.i4tech.odata.generator;

import lombok.*;
import net.sf.saxon.trans.XPathException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.xml.XMLConstants;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Xsd Generator Mojo.
 *
 * @goal generate
 * @phase generate-sources
 *
 * @author Gabor Ivan
 * @version 1.0.0
 * @since 1.0.0
 */
@Setter(AccessLevel.PACKAGE)
public class XsdGeneratorMojo extends AbstractMojo {

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class ImportedEntity {
        protected String name;
        protected String packageNamespace;
        protected String schema;
    }

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Log log = getLog();

    /**
     * The enclosing project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;


    /**
     * Service url of OData service
     *
     * @parameter
     * @required
     */
    private String rootServiceUrl;

    /**
     * Root target namespace (will be extended with package namespace)
     *
     * @parameter
     * @required
     */
    private String rootTargetNamespace;

    /**
     * Package  namespace
     *
     * @parameter
     */
    private String packageNamespace;

    /**
     * Header entity name
     *
     * @parameter
     */
    private String headerEntities;

    /**
     * Path to edmx file
     *
     * @parameter
     * @required
     */
    private String inputMetadata;

    /**
     * Path and filename of output XSD
     *
     * @parameter
     * @required
     */
    private String outputSchema;

    /**
     * Root directory for dowloaded collections. Each
     *
     * @parameter
     * @required
     */
    private String rootCollectionPath;

    /**
     * Package name to scan as collections.
     *
     * @parameter
     */
    private String transformerStylesheet;

    /**
     * Basic authentication username.
     *
     * @parameter
     */
    private String basicAuthUser;

    /**
     * Basic authentication password.
     *
     * @parameter
     */
    private String basicAuthPassword;

    /**
     * Entity prefix.
     *
     * @parameter
     * @required
     */
    private String entityPrefix;

    /**
     * Base interface for field metadata.
     *
     * @parameter
     * @required
     */
    private String fieldsMetaInterface;

    /**
     * Base interface for navigation metadata.
     *
     * @parameter
     * @required
     */
    private String navigationMetaInterface;

    /**
     * Base interface for key metadata.
     *
     * @parameter
     * @required
     */
    private String keyMetaInterface;

    /**
     * Base interface for codelist enums.
     *
     * @parameter
     * @required
     */
    private String codelistWrapperMetaInterface;

    /**
     * Base interface for codelist enums.
     *
     * @parameter
     * @required
     */
    private String contextualCodelistWrapperMetaInterface;

    /**
     * Entity base class.
     *
     * @parameter
     */
    private String entityBaseClass;

    /**
     * Function Import base class.
     *
     * @parameter
     */
    private String functionImportBaseClass;

    /**
     * Function imports to process
     *
     * @parameter
     */
    private String functionImports;

    /**
     * Generate wrapper enum for code lists.
     *
     * @parameter
     */
    private String generateCodelistWrapper;

    /**
     * Generate wrapper enum for code lists.
     *
     * @parameter
     */
    private String generateContextualCodelistWrapper;

    /**
     * Base interface for codelist enums.
     *
     * @parameter
     * @required
     */
    private String enumMetaInterface;

    /**
     * Entities to be imported from other schemas.
     *
     * @parameter
     */
    private List<ImportedEntity> importedEntities;

    /**
     * Codelists to be excluded from generation.
     *
     * @parameter
     */
    private List<String> excludedCodelists;

    private final Log logger = getLog();

    private HttpEntity executeGet(String absoluteUrl, String contentType) throws IOException {
        final HttpGet get = new HttpGet(absoluteUrl);
        if (StringUtils.isNotBlank(basicAuthUser) && StringUtils.isNotBlank(basicAuthPassword)) {
            get.setHeader(HttpHeaders.AUTHORIZATION, getAuthorizationHeader());
        }
        get.setHeader(HttpHeaders.ACCEPT, contentType);
        get.setHeader(HttpHeaders.ACCEPT_LANGUAGE, "en");
        get.setHeader(HttpHeaders.ACCEPT_ENCODING, "UTF-8");

        HttpResponse response = HttpClientBuilder.create().build().execute(get);
        return response.getEntity();
    }

    private String getAuthorizationHeader() {
        String temp = basicAuthUser + ":" + basicAuthPassword;
        return "Basic " + new String(Base64.encodeBase64(temp.getBytes()));
    }

    private String getCollectionContent(final String href) throws IOException {
        final HttpEntity response = executeGet(href, ContentType.APPLICATION_ATOM_XML.getMimeType());
        final String content = IOUtils.toString(response.getContent(), StandardCharsets.UTF_8);
        return content;
    }

    private String readCodeList(String href) throws IOException {
        final StringBuilder codelist = new StringBuilder();
        boolean hasNext = false;
        String uri = href;

        do {
            String content = getCollectionContent(uri);
            if (hasNext) {
                content = content.substring(content.indexOf("<entry"));
            }
            int idx = content.indexOf("<link rel=");
            if (idx > -1) {
                codelist.append(content.substring(0, idx));
                final String hrefHolder = content.substring(idx);
                int hrefStart = hrefHolder.indexOf("href=") + 6;
                int hrefEnd = hrefHolder.indexOf("\"/>");
                uri = hrefHolder.substring(hrefStart, hrefEnd).replace("&amp;", "");
                hasNext = true;
            } else {
                codelist.append(content);
                hasNext = false;
            }
        } while (hasNext);

        return codelist.toString();
    }

    private StreamSource resolveCollection(String href) {
        if (href.startsWith("http")) {
            final String outDir = rootCollectionPath + (StringUtils.isNotBlank(packageNamespace) ?
                    packageNamespace : "");
            final File directory = new File(outDir);
            if (!directory.exists()){
                directory.mkdirs();
            }
            final String outfile = outDir + "/" + href.substring(href.lastIndexOf('/') + 1) + ".xml";

            try (FileWriter writer = new FileWriter(outfile)) {
                final String codelist = readCodeList(href);

                writer.write(codelist);

                return new StreamSource(new ByteArrayInputStream(codelist.getBytes()));
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private String createPrefix(String name) {
        return name.replaceAll("[a-z]","").toLowerCase();
    }

    private void handleException(TransformerException e) {
        if (e instanceof XPathException) {
            final String errorCode = ((XPathException)e).getErrorCodeLocalPart();
            final Pattern pattern = Pattern.compile("([A-Za-z]+\\.xml)");
            final Matcher matcher = pattern.matcher(e.getMessage());
            if ("FODC0002".equals(errorCode) && matcher.find()) {
                log.info(String.format("Local copy of collection '%s' not found. Fetching.", matcher.group(1)));
            } else {
                log.error(e);
            }
        } else {
            log.error(e);
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            //Set saxon as transformer.
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

            StreamSource xsl = null;
            if (StringUtils.isBlank(transformerStylesheet)) {
                xsl = new StreamSource(this.getClass().getResourceAsStream("/xsl/edmx2xsd.xsl"));
            } else {
                xsl = new StreamSource(new File(transformerStylesheet));
            }

            final TransformerFactory tFactory = TransformerFactory.newInstance();
            tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            tFactory.setErrorListener(new ErrorListener() {
                @Override
                public void warning(TransformerException e) throws TransformerException {
                    handleException(e);
                }

                @Override
                public void error(TransformerException e) throws TransformerException {
                    handleException(e);
                }

                @Override
                public void fatalError(TransformerException e) throws TransformerException {
                    handleException(e);
                }
            });
            final String entities = Arrays.stream(StringUtils.defaultString(headerEntities).split(","))
                    .map(s -> entityPrefix + "." + s).collect(Collectors.joining(","));

            Transformer transformer = tFactory.newTransformer(xsl);
            transformer.setParameter("rootTargetNamespace", rootTargetNamespace);
            transformer.setParameter("packageNamespace", StringUtils.defaultString(packageNamespace));
            transformer.setParameter("collectionsUrl", rootServiceUrl);
            transformer.setParameter("collectionsPath", rootCollectionPath +
                    (StringUtils.isNotBlank(packageNamespace) ?  packageNamespace + "/" : ""));
            transformer.setParameter("headerEntities", entities);
            transformer.setParameter("entityBaseClass", StringUtils.defaultString(entityBaseClass));
            transformer.setParameter("fieldsMetaInterface", fieldsMetaInterface);
            transformer.setParameter("keyMetaInterface", keyMetaInterface);
            transformer.setParameter("navigationMetaInterface", navigationMetaInterface);
            transformer.setParameter("functionImports", StringUtils.defaultString(functionImports));
            transformer.setParameter("functionImportBaseClass", functionImportBaseClass);
            transformer.setParameter("codelistWrapperMetaInterface", codelistWrapperMetaInterface);
            transformer.setParameter("generateCodelistWrapper", StringUtils.isNotBlank(generateCodelistWrapper) ?
                    generateCodelistWrapper : StringUtils.EMPTY);
            transformer.setParameter("contextualCodelistWrapperMetaInterface", contextualCodelistWrapperMetaInterface);
            transformer.setParameter("generateContextualCodelistWrapper", StringUtils.isNotBlank(generateContextualCodelistWrapper) ?
                    generateContextualCodelistWrapper : StringUtils.EMPTY);
            transformer.setParameter("enumMetaInterface", enumMetaInterface);

            if (importedEntities != null) {
                transformer.setParameter("importedEntities", importedEntities.stream()
                        .map(i -> i.getName() + " " + createPrefix(i.getName()) + " " + rootTargetNamespace + i.getPackageNamespace() + " " + i.getSchema())
                        .collect(Collectors.joining(",")));
            }

            if (excludedCodelists != null && !excludedCodelists.isEmpty()) {
                transformer.setParameter("excludedCodelists", String.join(",", excludedCodelists));
            }
            transformer.setURIResolver((href, base) -> resolveCollection(href));
            transformer.transform(new StreamSource(new File(inputMetadata)), new StreamResult(new File(outputSchema)));

        } catch (Exception e) {
            throw new MojoExecutionException("Could not generate xsd.", e);
        }
    }


}
