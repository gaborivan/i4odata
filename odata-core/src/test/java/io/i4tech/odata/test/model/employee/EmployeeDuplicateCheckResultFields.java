package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataKeyFields;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EmployeeDuplicateCheckResultKeyFields")
@XmlEnum
public enum EmployeeDuplicateCheckResultFields implements ODataKeyFields<Employee> {

    @XmlEnumValue("ObjectID")
    OBJECTID("ObjectID"),

    @XmlEnumValue("EmployeeUUID")
    EMPLOYEEUUID("EmployeeUUID");

    private final String value;

    EmployeeDuplicateCheckResultFields(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmployeeDuplicateCheckResultFields fromValue(String v) {
        for (EmployeeDuplicateCheckResultFields c: EmployeeDuplicateCheckResultFields.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
