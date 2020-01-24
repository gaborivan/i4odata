<?xml version="1.0" encoding="UTF-8"?>
<!--

MIT License

Copyright (c) 2019 i4tech Kft.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:edm="http://schemas.microsoft.com/ado/2008/09/edm"
                xmlns:c4c="http://www.sap.com/Protocols/C4CData"
                xmlns:sap="http://www.sap.com/Protocols/SAPData"
                xmlns:jxb="http://java.sun.com/xml/ns/jaxb"
                xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"
                xmlns:inheritance="http://jaxb2-commons.dev.java.net/basic/inheritance"
                xmlns:annox="http://annox.dev.java.net"
                xmlns:ci="http://jaxb.dev.java.net/plugin/code-injector"
                xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata"
                xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices"
                xmlns:atom="http://www.w3.org/2005/Atom"
>

    <xsl:strip-space elements="*"/>
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:param name="rootTargetNamespace"/>
    <xsl:param name="packageNamespace"/>
    <xsl:param name="targetNamespace" select="concat($rootTargetNamespace, $packageNamespace)"/>

    <xsl:param name="collectionsPath"/>
    <xsl:param name="collectionsUrl"/>
    <xsl:param name="headerEntities"/>
    <xsl:param name="entityBaseClass"/>
    <xsl:param name="functionImportBaseClass"/>
    <xsl:param name="fieldsMetaInterface"/>
    <xsl:param name="keyMetaInterface"/>
    <xsl:param name="navigationMetaInterface"/>
    <xsl:param name="functionImports"/>
    <xsl:param name="codelistWrapperMetaInterface"/>
    <xsl:param name="generateCodelistWrapper" />
    <xsl:param name="contextualCodelistWrapperMetaInterface"/>
    <xsl:param name="generateContextualCodelistWrapper" />
    <xsl:param name="enumMetaInterface" />
    <xsl:param name="importedEntities"/>
    <xsl:param name="excludedCodelists"/>

    <xsl:variable name="imports">
        <xsl:for-each select="tokenize($importedEntities, ',')">
            <xsl:variable name="import" select="tokenize(., ' ')"/>
            <import type="{$import[1]}"><xsl:value-of select="concat($import[2],':',$import[1])"/></import>
        </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="types">
        <type edmType="Edm.String" xsdType="xs:string"/>
        <type edmType="Edm.DateTimeOffset" xsdType="xs:dateTime"/>
        <type edmType="Edm.DateTime" xsdType="xs:dateTime"/>
        <type edmType="Edm.Guid" xsdType="GUID"/>
        <type edmType="Edm.Int32" xsdType="xs:int"/>
        <type edmType="Edm.Int16" xsdType="xs:int"/>
        <type edmType="Edm.Boolean" xsdType="xs:boolean"/>
        <type edmType="Edm.Time" xsdType="xs:time"/>
        <type edmType="Edm.Decimal" xsdType="xs:double"/>
        <type edmType="Edm.Binary" xsdType="xs:base64Binary"/>
    </xsl:variable>

    <xsl:variable name="badEnums">
        <xsl:for-each select="tokenize($excludedCodelists, ',')">
            <badEnum enumName="{.}"/>
        </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="bom">
        <xsl:variable name="docRoot" select="/" />
        <bom>
            <xsl:for-each select="tokenize($headerEntities, ',')">
                <xsl:variable name="headerEntity" select="."/>
                <xsl:apply-templates mode="collect" select="$docRoot//edm:EntitySet[@EntityType=$headerEntity]">
                    <xsl:with-param name="generatedEntities" select="''"/>
                    <xsl:with-param name="generatedEnums" select="''"/>
                </xsl:apply-templates>
            </xsl:for-each>
            <xsl:for-each select="tokenize($functionImports, ',')">
                <xsl:variable name="functionImport" select="."/>
                <xsl:apply-templates mode="collect" select="$docRoot//edm:FunctionImport[@Name=$functionImport]">
                </xsl:apply-templates>
            </xsl:for-each>
        </bom>
    </xsl:variable>

    <xsl:template mode="collect" match="edm:EntitySet[not(ends-with(@EntityType,'CodeList'))]">
        <xsl:param name="generatedEntities"/>
        <xsl:variable name="entityName" select="substring-after(@EntityType, '.')"/>
        <xsl:variable name="entity" select="//edm:EntityType[@Name=$entityName]"/>

        <xsl:if test="count($imports/*[@type=$entityName]) = 0">
            <xsl:apply-templates mode="collect" select="$entity">
                <xsl:with-param name="entitySetName" select="@Name"/>
            </xsl:apply-templates>

            <xsl:for-each select="$entity/edm:NavigationProperty">
                <xsl:variable name="associationName" select="substring-after(@Relationship, '.')"/>
                <xsl:apply-templates mode="collect" select="//edm:Association[@Name = $associationName]">
                    <xsl:with-param name="fromRole" select="@FromRole"/>
                    <xsl:with-param name="toRole" select="@ToRole"/>
                    <xsl:with-param name="generatedEntities"
                                    select="concat($generatedEntities, ',', concat('[',$entity/@Name,']'))"/>
                </xsl:apply-templates>
            </xsl:for-each>

            <xsl:for-each select="$entity/edm:Property[@c4c:value-help]">
                <xsl:variable name="enumName" select="@c4c:value-help"/>
                <xsl:if test="not($badEnums/*[@enumName=$enumName]) and //edm:EntitySet[@Name=$enumName and ends-with(@EntityType, '.CodeList')]">
                    <enum><xsl:value-of select="$enumName"/></enum>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>

    </xsl:template>

    <xsl:template mode="collect" match="edm:EntitySet[ends-with(@EntityType,'.CodeList')]">
    </xsl:template>

    <xsl:template mode="collect" match="edm:EntitySet[ends-with(@EntityType,'.ContextualCodeList')]">
        <contextual><xsl:value-of select="@Name" /></contextual>
    </xsl:template>

    <xsl:template mode="collect" match="edm:EntityType">
        <xsl:param name="entitySetName" />
        <entity><xsl:value-of select="$entitySetName" /></entity>
    </xsl:template>

    <xsl:template mode="collect" match="edm:Association">
        <xsl:param name="generatedEntities"/>
        <xsl:param name="toRole"/>
        <xsl:if test="not(contains($generatedEntities, concat('[', $toRole, ']')))">
            <xsl:apply-templates mode="collect" select="//edm:EntitySet[ends-with(@EntityType,concat('.', $toRole))]">
                <xsl:with-param name="generatedEntities"
                                select="concat($generatedEntities, ',', concat('[', $toRole, ']'))"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="collect" match="edm:FunctionImport">
        <xsl:variable name="functionName" select="@Name"/>
        <function><xsl:value-of select="$functionName" /></function>
        <xsl:variable name="returnType">
            <xsl:choose>
                <xsl:when test="starts-with(@ReturnType, 'Collection(')">
                    <xsl:value-of select="substring-before(substring-after(@ReturnType, 'Collection('), ')')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@ReturnType" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:apply-templates mode="collect" select="//edm:EntitySet[@EntityType=$returnType]" >
            <xsl:with-param name="generatedEntities" select="''"/>
            <xsl:with-param name="generatedEnums" select="''"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template name="generate-enum">
        <xsl:param name="enumCollectionName"/>
        <xsl:variable name="enumName"
                      select="substring($enumCollectionName, 1, string-length($enumCollectionName) - 10)"/>
        <xsl:variable name="offlineEnumData" select="document(concat($collectionsPath,$enumCollectionName,'.xml'))"/>
        <xsl:variable name="enumData">
            <xsl:choose>
                <xsl:when test="$offlineEnumData">
                    <xsl:copy-of select="$offlineEnumData"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="onlineEnumData"
                                  select="document(concat($collectionsUrl,$enumCollectionName,''))"/>
                    <xsl:copy-of select="$onlineEnumData"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="enumType">'<xsl:value-of select="$enumName"/>'</xsl:variable>

        <xsl:if test="not($badEnums/*[@enumName=$enumName])">
            <xs:simpleType name="{$enumName}">
                <xsl:if test="count($enumData//atom:entry//m:properties) > 0">
                    <xs:annotation>
                        <xs:appinfo>
                            <inheritance:implements><xsl:value-of select="$enumMetaInterface"/></inheritance:implements>
                            <ci:code>
    public static <xsl:value-of select="$enumName"/> fromNullable(String v) {
        try {
            return fromValue(v);
        } catch (Exception e) {
            return null;
        }
    }
                            </ci:code>
                            <annox:annotateEnumValueMethod>@com.fasterxml.jackson.annotation.JsonValue</annox:annotateEnumValueMethod>
                        </xs:appinfo>
                    </xs:annotation>
                </xsl:if>
                <xs:restriction base="xs:string">
                    <xsl:for-each select="$enumData//atom:entry//m:properties">
                        <xsl:variable name="description" select="replace(d:Description, '[&lt;&gt;]', '')"/>
                        <xsl:variable name="code" select="d:Code"/>
                        <xsl:variable name="enumMember">
                            <xsl:choose>
                                <xsl:when test="string-length($description) = 0">
                                    <xsl:value-of select="concat('C_', d:Code)"/>
                                </xsl:when>
                                <xsl:when test="substring($description, 1, 1) castable as xs:double">
                                    <xsl:value-of select="concat('C_', $description)"/>
                                </xsl:when>
                                <xsl:when test="count($enumData//atom:entry//m:properties[replace(lower-case(d:Description), '[\\''.]', '') = replace(lower-case($description), '[\\''.]', '')]) = 1">
                                    <xsl:value-of select="$description"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="concat($description, '_', d:Code)"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="enumCode">
                            <xsl:choose>
                                <xsl:when test="count($enumData//atom:entry//m:properties[d:Code = $code]) = 1">
                                    <xsl:value-of select="$code"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="concat('**_', $code)"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="enumDesc" select="replace(replace(replace(replace(replace(replace(replace(translate(upper-case($enumMember),' /.-– ()[]:;,\''', '_______________'), '^_+', ''), '\+', '_PLUS_'), '&amp;', '_AND_'), '%', '_PERCENT_' ), '[&gt;&lt;&#xA;]', ''), '_+', '_'), '_+$', '')"/>
                        <xs:enumeration value="{$enumCode}">
                            <xs:annotation>
                                <xs:appinfo>
                                    <jxb:typesafeEnumMember name="{$enumDesc}"/>
                                </xs:appinfo>
                            </xs:annotation>
                        </xs:enumeration>
                    </xsl:for-each>
                </xs:restriction>
            </xs:simpleType>
        </xsl:if>
    </xsl:template>

    <xsl:template name="camelToSnake">
        <xsl:param name="text"/>
        <xsl:variable name="Upper">ABCDEFGHIJKLMNOPQRSTUVQXYZ ,</xsl:variable>
        <xsl:variable name="Lower">abcdefghijklmnopqrstuvwxyz</xsl:variable>

        <xsl:for-each select="tokenize(replace($text, '[.]', '$1,'),',')">
            <xsl:choose>
                <xsl:when test="contains($Upper,.)">
                    <xsl:if test="position()>1">
                        <xsl:text>_</xsl:text>
                    </xsl:if>
                    <xsl:value-of select="translate(.,$Lower,$Upper)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="generate-codelist-wrapper">
        <xs:simpleType name="CodeLists">
            <xs:annotation>
                <xs:appinfo>
                    <annox:annotateEnumValueMethod>@com.fasterxml.jackson.annotation.JsonValue</annox:annotateEnumValueMethod>
                    <inheritance:implements><xsl:value-of select="$codelistWrapperMetaInterface"/></inheritance:implements>
                </xs:appinfo>
            </xs:annotation>
            <xs:restriction base="xs:string">
                <xsl:choose>
                    <xsl:when test="$generateCodelistWrapper = 'all'">
                        <xsl:for-each select="//edm:EntitySet[ends-with(@EntityType,'CodeList')]" >
                            <xsl:variable name="enumName" select="@Name" />
                            <xs:enumeration value="{$enumName}">
                                <xs:annotation>
                                    <xs:appinfo>
                                        <jxb:typesafeEnumMember name="{$enumName}"/>
                                    </xs:appinfo>
                                </xs:annotation>
                            </xs:enumeration>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="$generateCodelistWrapper = 'used'">
                        <xsl:for-each select="$bom//enum" >
                            <xsl:variable name="enumName" select="." />
                            <xs:enumeration value="{$enumName}">
                                <xs:annotation>
                                    <xs:appinfo>
                                        <jxb:typesafeEnumMember name="{$enumName}"/>
                                    </xs:appinfo>
                                </xs:annotation>
                            </xs:enumeration>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                    </xsl:otherwise>
                </xsl:choose>
            </xs:restriction>
        </xs:simpleType>
    </xsl:template>

    <xsl:template name="generate-contextual-codelist-wrapper">
        <xs:simpleType name="ContextualCodeLists">
            <xs:annotation>
                <xs:appinfo>
                    <annox:annotateEnumValueMethod>@com.fasterxml.jackson.annotation.JsonValue</annox:annotateEnumValueMethod>
                    <inheritance:implements><xsl:value-of select="$contextualCodelistWrapperMetaInterface"/></inheritance:implements>
                </xs:appinfo>
            </xs:annotation>
            <xs:restriction base="xs:string">
                <xsl:choose>
                    <xsl:when test="$generateContextualCodelistWrapper = 'all'">
                        <xsl:for-each select="//edm:EntitySet[ends-with(@EntityType,'ContextualCodeList')]" >
                            <xsl:variable name="enumName" select="@Name" />
                            <xs:enumeration value="{$enumName}">
                                <xs:annotation>
                                    <xs:appinfo>
                                        <jxb:typesafeEnumMember name="{$enumName}"/>
                                    </xs:appinfo>
                                </xs:annotation>
                            </xs:enumeration>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="$generateContextualCodelistWrapper = 'used'">
                        <xsl:for-each select="$bom//contextual" >
                            <xsl:variable name="enumName" select="." />
                            <xs:enumeration value="{$enumName}">
                                <xs:annotation>
                                    <xs:appinfo>
                                        <jxb:typesafeEnumMember name="{$enumName}"/>
                                    </xs:appinfo>
                                </xs:annotation>
                            </xs:enumeration>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                    </xsl:otherwise>
                </xsl:choose>
            </xs:restriction>
        </xs:simpleType>
    </xsl:template>


    <xsl:template match="edm:Schema">
        <xsl:variable name="docRoot" select="/" />

        <xs:schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   jxb:version="2.1" jxb:extensionBindingPrefixes="annox inheritance ci xjc"
                   targetNamespace="{$targetNamespace}">
            <xsl:namespace name="" select="$targetNamespace"/>
            
            <xsl:for-each select="tokenize($importedEntities, ',')">
                <xsl:variable name="import" select="tokenize(., ' ')"/>
                <xsl:namespace name="{$import[2]}" select="$import[3]" />
            </xsl:for-each>
            <xsl:for-each select="tokenize($importedEntities, ',')">
                <xsl:variable name="import" select="tokenize(., ' ')"/>
                <xs:import namespace="{$import[3]}" schemaLocation="{$import[4]}"/>
            </xsl:for-each>

            <xs:simpleType name="GUID">
                <xs:annotation>
                    <xs:appinfo>
                        <xjc:javaType adapter="io.i4tech.odata.adapter.UuidAdapter"
                                      name="java.util.UUID"/>
                    </xs:appinfo>
                </xs:annotation>
                <xs:restriction base="xs:string">
                </xs:restriction>
            </xs:simpleType>

            <xsl:for-each select="distinct-values($bom//entity)">
                <xsl:variable name="entitySetName" select="."/>
                <xsl:apply-templates select="$docRoot//edm:EntitySet[@Name=$entitySetName]">
                    <xsl:with-param name="generatedEntities" select="''"/>
                </xsl:apply-templates>
            </xsl:for-each>

            <xsl:for-each select="distinct-values($bom//function)">
                <xsl:variable name="functionImportName" select="."/>
                <xsl:apply-templates select="$docRoot//edm:FunctionImport[@Name=$functionImportName]"/>
            </xsl:for-each>

            <xsl:for-each select="distinct-values($bom//enum)">
                <xsl:variable name="enumName" select="."/>
                <xsl:call-template name="generate-enum">
                    <xsl:with-param name="enumCollectionName" select="$enumName"/>
                </xsl:call-template>
            </xsl:for-each>

            <xsl:if test="$generateCodelistWrapper = 'all' or ($generateCodelistWrapper = 'used' and count($bom//enum) > 0)">
                <xsl:call-template name="generate-codelist-wrapper" />
            </xsl:if>

            <xsl:if test="$generateContextualCodelistWrapper = 'all' or ($generateContextualCodelistWrapper = 'used' and count($bom//enum) > 0)">
                <xsl:call-template name="generate-contextual-codelist-wrapper" />
            </xsl:if>

        </xs:schema>
    </xsl:template>


    <xsl:template match="edm:EntitySet[not(ends-with(@EntityType,'CodeList'))]">
        <xsl:variable name="entityName" select="substring-after(@EntityType, '.')"/>
        <xsl:variable name="entity" select="//edm:EntityType[@Name=$entityName]"/>
        <xsl:apply-templates select="$entity">
            <xsl:with-param name="entitySetName" select="@Name"/>
        </xsl:apply-templates>
        <xsl:for-each select="$entity/edm:NavigationProperty">
            <xsl:variable name="associationName" select="substring-after(@Relationship, '.')"/>
            <xsl:apply-templates select="//edm:Association[@Name = $associationName]">
                <xsl:with-param name="fromRole" select="@FromRole"/>
                <xsl:with-param name="toRole" select="@ToRole"/>
            </xsl:apply-templates>
        </xsl:for-each>
    </xsl:template>


    <xsl:template match="edm:EntitySet[ends-with(@EntityType,'.CodeList')]">
    </xsl:template>


    <xsl:template match="edm:EntityType">
        <xsl:param name="entitySetName" />
        <xsl:variable name="entityName" select="@Name" />
        <xs:complexType name="{@Name}">
            <xs:annotation>
                <xs:appinfo>
                    <xsl:if test="string-length($entityBaseClass) > 0">
                        <inheritance:extends><xsl:value-of select="$entityBaseClass"></xsl:value-of> </inheritance:extends>
                    </xsl:if>
                    <annox:annotate><xsl:value-of select="concat('@io.i4tech.odata.common.model.ODataEntitySet(name = &quot;',$entitySetName,'&quot;)')"/></annox:annotate>
                </xs:appinfo>
            </xs:annotation>
            <xs:sequence>
                <xsl:apply-templates/>
            </xs:sequence>
            <xsl:for-each select="edm:Key/edm:PropertyRef">
                <xsl:variable name="keyName" select="@Name"/>
                <xs:attribute name="{concat('_',$keyName)}" type="{concat($entityName,'KeyFields')}" fixed="{$keyName}">
                    <xs:annotation>
                        <xs:appinfo>
                            <jxb:property name="{concat('_',$keyName)}" fixedAttributeAsConstantProperty="true" />
                        </xs:appinfo>
                    </xs:annotation>
                </xs:attribute>
            </xsl:for-each>
            <xsl:for-each select="edm:Property[@sap:filterable='true' and not(@Name = parent::node()/edm:Key/edm:PropertyRef/@Name)]">
                <xsl:variable name="fieldName" select="@Name"/>
                <xs:attribute name="{concat('_',$fieldName)}" type="{concat($entityName,'Fields')}" fixed="{$fieldName}">
                    <xs:annotation>
                        <xs:appinfo>
                            <jxb:property name="{concat('_',$fieldName)}" fixedAttributeAsConstantProperty="true" />
                        </xs:appinfo>
                    </xs:annotation>
                </xs:attribute>
            </xsl:for-each>
            <xsl:for-each select="edm:NavigationProperty">
                <xsl:variable name="navigationName" select="@Name"/>
                <xs:attribute name="{concat('_',$navigationName)}" type="{concat($entityName,'Navigations')}" fixed="{$navigationName}">
                    <xs:annotation>
                        <xs:appinfo>
                            <jxb:property name="{concat('_',$navigationName)}" fixedAttributeAsConstantProperty="true" />
                        </xs:appinfo>
                    </xs:annotation>
                </xs:attribute>
            </xsl:for-each>
        </xs:complexType>
        <xsl:if test="count(edm:Property[@sap:filterable='true' and not(@Name = parent::node()/edm:Key/edm:PropertyRef/@Name)]) > 0">
            <xs:simpleType name="{concat($entityName,'Fields')}">
                <xs:annotation>
                    <xs:appinfo>
                        <jxb:typesafeEnumClass />
                        <inheritance:implements><xsl:value-of select="$fieldsMetaInterface"/>&lt;<xsl:value-of select="$entityName"/>&gt;</inheritance:implements>
                    </xs:appinfo>
                </xs:annotation>
                <xs:restriction base="xs:string">
                    <xsl:for-each select="edm:Property[@sap:filterable='true']">
                    <xs:enumeration value="{@Name}">
                        <xs:annotation>
                            <xs:appinfo>
                                <jxb:typesafeEnumMember name="{upper-case(@Name)}"/>
                            </xs:appinfo>
                        </xs:annotation>
                    </xs:enumeration>
                    </xsl:for-each>
                </xs:restriction>
            </xs:simpleType>
        </xsl:if>
        <xsl:if test="count(edm:NavigationProperty) > 0">
            <xs:simpleType name="{concat($entityName,'Navigations')}">
                <xs:annotation>
                    <xs:appinfo>
                        <jxb:typesafeEnumClass />
                        <inheritance:implements><xsl:value-of select="$navigationMetaInterface"/>&lt;<xsl:value-of select="$entityName"/>&gt;</inheritance:implements>
                    </xs:appinfo>
                </xs:annotation>
                <xs:restriction base="xs:string">
                    <xsl:for-each select="edm:NavigationProperty">
                        <xs:enumeration value="{@Name}">
                            <xs:annotation>
                                <xs:appinfo>
                                    <jxb:typesafeEnumMember name="{upper-case(@Name)}"/>
                                </xs:appinfo>
                            </xs:annotation>
                        </xs:enumeration>
                    </xsl:for-each>
                </xs:restriction>
            </xs:simpleType>
        </xsl:if>
        <xsl:if test="count(edm:Key/edm:PropertyRef) > 0">
            <xs:simpleType name="{concat($entityName,'KeyFields')}">
                <xs:annotation>
                    <xs:appinfo>
                        <jxb:typesafeEnumClass />
                        <inheritance:implements><xsl:value-of select="$keyMetaInterface"/>&lt;<xsl:value-of select="$entityName"/>&gt;</inheritance:implements>
                    </xs:appinfo>
                </xs:annotation>
                <xs:restriction base="xs:string">
                    <xsl:for-each select="edm:Key/edm:PropertyRef">
                        <xs:enumeration value="{@Name}">
                            <xs:annotation>
                                <xs:appinfo>
                                    <jxb:typesafeEnumMember name="{upper-case(@Name)}"/>
                                </xs:appinfo>
                            </xs:annotation>
                        </xs:enumeration>
                    </xsl:for-each>
                </xs:restriction>
            </xs:simpleType>
        </xsl:if>
    </xsl:template>


    <xsl:template match="edm:FunctionImport">
        <xsl:variable name="functionName" select="@Name"/>
        <xsl:variable name="returnType">
            <xsl:choose>
                <xsl:when test="starts-with(@ReturnType, 'Collection(')">
                    <xsl:value-of select="substring-after(substring-before(substring-after(@ReturnType, 'Collection('), ')'), '.')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring-after(@ReturnType, '.')" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xs:complexType name="{@Name}">
            <xs:annotation>
                <xs:appinfo>
                    <xsl:if test="string-length($functionImportBaseClass) > 0">
                        <inheritance:extends><xsl:value-of select="$functionImportBaseClass"></xsl:value-of>&lt;<xsl:value-of select="$returnType"/>&gt;</inheritance:extends>
                    </xsl:if>
                </xs:appinfo>
            </xs:annotation>

            <xs:sequence>
                <xsl:apply-templates/>
            </xs:sequence>

            <xsl:if test="@m:HttpMethod">
                <xs:attribute name="HTTP_METHOD" type="xs:string" fixed="{@m:HttpMethod}">
                    <xs:annotation>
                        <xs:appinfo>
                            <jxb:property name="HTTP_METHOD" fixedAttributeAsConstantProperty="true" />
                        </xs:appinfo>
                    </xs:annotation>
                </xs:attribute>
            </xsl:if>
        </xs:complexType>
    </xsl:template>


    <xsl:template match="edm:ComplexType">
        <xs:complexType name="{@Name}">
            <xs:sequence>
                <xsl:apply-templates/>
            </xs:sequence>
        </xs:complexType>
    </xsl:template>


    <xsl:template match="edm:NavigationProperty">
        <xsl:variable name="associationName" select="substring-after(@Relationship, '.')"/>
        <xsl:variable name="association" select="//edm:Association[@Name = $associationName]"/>
        <xsl:variable name="toRole" select="@ToRole"/>
        <xs:element name="{@Name}">
            <xsl:variable name="typeName" select="substring-after($association/edm:End[@Role=$toRole]/@Type, '.')"/>
            <xsl:attribute name="type">
                <xsl:choose>
                    <xsl:when test="$imports/*[@type=$typeName]">
                        <xsl:value-of select="$imports/*[@type=$typeName]" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$typeName" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:if test="$association/edm:End[@Role=$toRole]/@Multiplicity = '*'">
                <xsl:attribute name="maxOccurs" select="'unbounded'"/>
                <xs:annotation>
                    <xs:appinfo>
                        <annox:annotate target="field">@com.fasterxml.jackson.annotation.JsonIgnore</annox:annotate>
                    </xs:appinfo>
                </xs:annotation>
            </xsl:if>
        </xs:element>
    </xsl:template>


    <xsl:template match="edm:Parameter">
        <xsl:variable name="typeName" select="@Type"/>
        <xs:element name="{@Name}">
            <xsl:attribute name="type">
                <xsl:value-of select="$types/*[@edmType=$typeName]/@xsdType"/>
            </xsl:attribute>
        </xs:element>
    </xsl:template>


    <xsl:template match="edm:Property">
        <xsl:variable name="typeName" select="@Type"/>
        <xs:element name="{@Name}">
            <xsl:attribute name="type">
                <xsl:variable name="valueHelp" select="@c4c:value-help"/>
                <xsl:variable name="valueHelpEnumName"
                              select="substring($valueHelp, 1, string-length($valueHelp) - 10)"/>
                <xsl:choose>
                    <xsl:when test="$valueHelp">
                        <xsl:choose>
                            <xsl:when test="$badEnums/*[@enumName=$valueHelpEnumName]">
                                <xsl:value-of select="'xs:string'"/>
                            </xsl:when>
                            <xsl:when test="//edm:EntitySet[@Name=$valueHelp and ends-with(@EntityType, '.CodeList')]">
                                <xsl:value-of select="$valueHelpEnumName"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="'xs:string'"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when test="//edm:ComplexType[@Name = substring-after($typeName,'.')]">
                        <xsl:value-of select="substring-after($typeName,'.')"/>
                    </xsl:when>
                    <xsl:when test="//edm:EntityType[@Name = $typeName]">
                        <xsl:value-of select="$typeName"/>
                    </xsl:when>
                    <xsl:when test="starts-with($typeName, 'Edm.Date') and @Precision = '0'">
                        <xsl:value-of select="'xs:date'"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$types/*[@edmType=$typeName]/@xsdType"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xs:annotation>
                <xs:appinfo>
                    <annox:annotate target="field"><xsl:value-of select="concat('@io.i4tech.odata.common.model.ODataEdmType(&quot;',$typeName,'&quot;)')"/></annox:annotate>
                </xs:appinfo>
            </xs:annotation>
        </xs:element>
    </xsl:template>


</xsl:stylesheet>
