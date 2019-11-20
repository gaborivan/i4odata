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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
@Getter(AccessLevel.PACKAGE)
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


    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String HTTP_HEADER_ACCEPT = "Accept";
    private static final String HTTP_HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String APPLICATION_ATOM_XML = "application/atom+xml";

    private final Log logger = getLog();

    private InputStream executeGet(String absoluteUrl, String contentType) throws IllegalStateException, IOException {
        final HttpGet get = new HttpGet(absoluteUrl);
        if (StringUtils.isNotBlank(basicAuthUser) && StringUtils.isNotBlank(basicAuthPassword)) {
            get.setHeader(AUTHORIZATION_HEADER, getAuthorizationHeader());
        }
        get.setHeader(HTTP_HEADER_ACCEPT, contentType);
        get.setHeader(HTTP_HEADER_ACCEPT_LANGUAGE, "en");

        HttpResponse response = HttpClientBuilder.create().build().execute(get);
        return response.getEntity().getContent();
    }

    private String getAuthorizationHeader() {
        // Note: This example uses Basic Authentication
        // Preferred option is to use OAuth SAML bearer flow.
        String temp = new StringBuilder(basicAuthUser).append(":").append(basicAuthPassword).toString();
        String result = "Basic " + new String(Base64.encodeBase64(temp.getBytes()));
        return result;
    }

    private String getCollectionContent(final String href) throws IOException {
        final InputStream is = executeGet(href, APPLICATION_ATOM_XML);
        final byte[] data = IOUtils.toByteArray(is);
        final String content = new String(data);
        is.close();
        return content;
    }

    private void setClassLoader() {
        try {
            Set<URL> urls = new HashSet<>();
            List<Resource> elements = project.getResources();
            for (Resource element : elements) {
                urls.add(new File(element.getDirectory()).toURI().toURL());
            }

            ClassLoader contextClassLoader = URLClassLoader.newInstance(
                    urls.toArray(new URL[0]),
                    Thread.currentThread().getContextClassLoader());

            Thread.currentThread().setContextClassLoader(contextClassLoader);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    public void execute() throws MojoExecutionException, MojoFailureException {
        //Set saxon as transformer.
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

        StreamSource xsl = null;
        if (StringUtils.isBlank(transformerStylesheet)) {
            xsl = new StreamSource(this.getClass().getResourceAsStream("/xsl/edmx2xsd.xsl"));
        } else {
            xsl = new StreamSource(new File(transformerStylesheet));
        }
        TransformerFactory tFactory = TransformerFactory.newInstance();
        try {
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
                transformer.setParameter("excludedCodelists", excludedCodelists.stream()
                        .collect(Collectors.joining(",")));
            }
            transformer.setURIResolver((href, base) -> {

                try {
                    if (href.startsWith("http")) {
                        String outDir = rootCollectionPath + (StringUtils.isNotBlank(packageNamespace) ?
                                packageNamespace : "");
                        File directory = new File(outDir);
                        if (! directory.exists()){
                            directory.mkdirs();
                        }
                        String outfile = outDir + "/" + href.substring(href.lastIndexOf('/') + 1) + ".xml";

                        boolean hasNext = false;
                        final StringBuffer codelist = new StringBuffer();
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

                        FileWriter writer = new FileWriter(outfile);
                        writer.write(codelist.toString());
                        writer.flush();
                        writer.close();
                        ByteArrayInputStream bais = new ByteArrayInputStream(codelist.toString().getBytes());
                        return new StreamSource(bais);
                    } else {
                        return null;
                    }
                } catch (IOException e) {
                    return null;
                }
            });
            transformer.transform(new StreamSource(new File(inputMetadata)),
                    //new StreamResult(System.out));
                    new StreamResult(new File(outputSchema)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createPrefix(String name) {
        return name.replaceAll("[a-z]","").toLowerCase();
    }
}
