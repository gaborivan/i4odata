package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataKeyFields;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EmployeeDuplicateCheckResultKeyFields")
@XmlEnum
public enum EmployeeDuplicateCheckResultKeyFields implements ODataKeyFields<Employee> {

    @XmlEnumValue("ObjectID")
    OBJECTID("ObjectID");
    private final String value;

    EmployeeDuplicateCheckResultKeyFields(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmployeeDuplicateCheckResultKeyFields fromValue(String v) {
        for (EmployeeDuplicateCheckResultKeyFields c: EmployeeDuplicateCheckResultKeyFields.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
