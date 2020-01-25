package io.i4tech.odata.test.model.employee;

import com.fasterxml.jackson.annotation.JsonValue;
import io.i4tech.odata.common.model.ODataEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EmployeeLanguageCode")
@XmlEnum
public enum EmployeeLanguageCode implements ODataEnum {

    @XmlEnumValue("DE")
    GERMAN("DE"),
    @XmlEnumValue("EN")
    ENGLISH("EN"),
    @XmlEnumValue("ES")
    SPANISH("ES"),
    @XmlEnumValue("FR")
    FRENCH("FR"),
    @XmlEnumValue("IT")
    ITALIAN("IT"),
    @XmlEnumValue("JA")
    JAPANESE("JA"),
    @XmlEnumValue("NL")
    DUTCH("NL"),
    @XmlEnumValue("PT")
    PORTUGUESE("PT"),
    @XmlEnumValue("ZH")
    CHINESE("ZH");
    private final String value;

    EmployeeLanguageCode(String v) {
        value = v;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public static EmployeeLanguageCode fromValue(String v) {
        for (EmployeeLanguageCode c: EmployeeLanguageCode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static EmployeeLanguageCode fromNullable(String v) {
        try {
            return fromValue(v);
        } catch (Exception e) {
            return null;
        }
    }

}
