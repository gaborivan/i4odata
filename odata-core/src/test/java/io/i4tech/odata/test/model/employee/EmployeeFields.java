package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataFields;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EmployeeFields")
@XmlEnum
public enum EmployeeFields implements ODataFields<Employee> {

    @XmlEnumValue("ObjectID")
    OBJECTID("ObjectID"),

    @XmlEnumValue("EmployeeUUID")
    EMPLOYEEUUID("EmployeeUUID"),

    @XmlEnumValue("FirstName")
    FIRSTNAME("FirstName"),

    @XmlEnumValue("LastName")
    LASTNAME("LastName"),

    @XmlEnumValue("BirthDate")
    BIRTHDATE("BirthDate"),

    @XmlEnumValue("LanguageCode")
    LANGUAGECODE("LanguageCode"),

    @XmlEnumValue("EntityLastChangedOn")
    ENTITYLASTCHANGEDON("EntityLastChangedOn");

    private final String value;

    EmployeeFields(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmployeeFields fromValue(String v) {
        for (EmployeeFields c: EmployeeFields.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
