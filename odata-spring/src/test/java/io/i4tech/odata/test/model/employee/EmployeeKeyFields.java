package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataKeyFields;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EmployeeKeyFields")
@XmlEnum
public enum EmployeeKeyFields implements ODataKeyFields<Employee> {

    @XmlEnumValue("ObjectID")
    OBJECTID("ObjectID");
    private final String value;

    EmployeeKeyFields(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmployeeKeyFields fromValue(String v) {
        for (EmployeeKeyFields c: EmployeeKeyFields.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
