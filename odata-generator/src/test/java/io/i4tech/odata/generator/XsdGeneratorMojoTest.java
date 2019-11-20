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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XsdGeneratorMojoTest {

    static class ListBuilder<T> {
        private final ArrayList<T> list;

        public ListBuilder() {
            list = new ArrayList<>();
        }

        public ListBuilder<T> add(T element) {
            list.add(element);
            return this;
        }

        public List<T> build() {
            return list;
        }
    }

    private XsdGeneratorMojo xsdGenerator = new XsdGeneratorMojo();

    @Before
    public void setup() {
        xsdGenerator.setInputMetadata("src/test/resources/c4codataapi.edmx");
        xsdGenerator.setRootServiceUrl("https://my330751.crm.ondemand.com/sap/c4c/odata/v1/c4codataapi/");
        xsdGenerator.setRootTargetNamespace("http://maihiro.com/odata/c4c/");
        xsdGenerator.setRootCollectionPath("target/generated-collections/");
        xsdGenerator.setEntityPrefix("c4codata");
        xsdGenerator.setEntityBaseClass("io.i4tech.odata.c4c.model.C4cODataEntity");
        xsdGenerator.setFunctionImportBaseClass("io.i4tech.odata.common.model.ODataFunction");
        xsdGenerator.setFieldsMetaInterface("io.i4tech.odata.common.model.ODataFields");
        xsdGenerator.setNavigationMetaInterface("io.i4tech.odata.common.model.ODataNavigations");
        xsdGenerator.setKeyMetaInterface("io.i4tech.odata.common.model.ODataKeyFields");
        xsdGenerator.setEnumMetaInterface("io.i4tech.odata.common.model.ODataEnum");
        xsdGenerator.setCodelistWrapperMetaInterface("io.i4tech.odata.common.model.ODataCodeList");
        xsdGenerator.setContextualCodelistWrapperMetaInterface("io.i4tech.odata.common.model.ODataContextualCodeList");
        xsdGenerator.setBasicAuthUser("gabori");
        xsdGenerator.setBasicAuthPassword("Tehenke10");
        xsdGenerator.setOutputSchema("target/test-output.xsd");

        new File("target/test-output.xsd").delete();
    }

    @Test
    public void testSingleHeaderEntity() throws MojoFailureException, MojoExecutionException {
        xsdGenerator.setPackageNamespace("test");
        xsdGenerator.setHeaderEntities("Employee");

        xsdGenerator.execute();
        Assert.assertTrue(new File("target/test-output.xsd").exists());
    }

    @Test
    public void testSingleHeaderEntityWithExcludedCodelists() throws MojoFailureException, MojoExecutionException {
        xsdGenerator.setPackageNamespace("test");
        xsdGenerator.setHeaderEntities("Employee");
        xsdGenerator.setExcludedCodelists(new ListBuilder<String>()
                .add("LeadSalesAndMarketingTeamPartyTypeCode")
                .add("LeadPartyPartyTypeCode")
                .add("LeadBusinessTransactionDocumentReferenceTypeCode")
                .add("LeadExternalPartyPartyTypeCode")
                .add("CorporateAccountInternationalVersionPOBoxDeviatingRegionCode")
                .add("LeadAccountState")
                .add("ContractItemPartyStateCode")
                .add("ServiceRequestServicePointLocationAddressState")
                .add("ContractItemPartyPOBoxDeviatingStateCode")
                .add("CorporateAccountInternationalVersionStateCode")
                .add("EmployeeUserSubscriptionAssignmentUserSubscriptionTypeCode")
                .add("BusinessUserDecimalFormatCode")
                .add("BusinessUserDateFormatCode")
                .build());
        xsdGenerator.execute();
        Assert.assertTrue(new File("target/test-output.xsd").exists());
    }

    @Test
    public void testSingleHeaderEntitySingleEntityImport() throws MojoFailureException, MojoExecutionException {
        xsdGenerator.setPackageNamespace("test");
        xsdGenerator.setHeaderEntities("Employee");
        xsdGenerator.setImportedEntities(Collections.singletonList(XsdGeneratorMojo.ImportedEntity.builder()
                .name("BusinessUser")
                .packageNamespace("businessuser")
                .schema("c4codataapi-businessuser.xsd")
                .build()));
        xsdGenerator.execute();
        Assert.assertTrue(new File("target/test-output.xsd").exists());
    }

    @Test
    public void testSingleHeaderEntitySingleFunctionImport() throws MojoFailureException, MojoExecutionException {
        xsdGenerator.setPackageNamespace("test");
        xsdGenerator.setHeaderEntities("Contact");
        xsdGenerator.setFunctionImports("CheckForDuplicates");

        xsdGenerator.execute();
        Assert.assertTrue(new File("target/test-output.xsd").exists());
    }

    @Test
    public void testCodelistWrapperForAll() throws MojoFailureException, MojoExecutionException {
        xsdGenerator.setGenerateCodelistWrapper("all");

        xsdGenerator.execute();
        Assert.assertTrue(new File("target/test-output.xsd").exists());
    }

    @Test
    public void testCodelistWrapperForUsed() throws MojoFailureException, MojoExecutionException {
        xsdGenerator.setGenerateCodelistWrapper("used");
        xsdGenerator.setHeaderEntities("Contact");

        xsdGenerator.execute();
        Assert.assertTrue(new File("target/test-output.xsd").exists());
    }

    @Test
    public void testContextualCodelistWrapperForAll() throws MojoFailureException, MojoExecutionException {
        xsdGenerator.setGenerateContextualCodelistWrapper("all");

        xsdGenerator.execute();
        Assert.assertTrue(new File("target/test-output.xsd").exists());
    }

    @Test
    public void testContextualCodelistWrapperForUsed() throws MojoFailureException, MojoExecutionException {
        xsdGenerator.setGenerateContextualCodelistWrapper("used");
        xsdGenerator.setHeaderEntities("Contact");

        xsdGenerator.execute();
        Assert.assertTrue(new File("target/test-output.xsd").exists());
    }
}
