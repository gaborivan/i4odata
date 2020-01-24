package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataNavigations;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EmployeeNavigations")
@XmlEnum
public enum EmployeeNavigations implements ODataNavigations<Employee> {

    @XmlEnumValue("EmployeeSkills")
    EMPLOYEESKILLS("EmployeeSkills"),

    @XmlEnumValue("EmployeeWorkingHours")
    EMPLOYEEWORKINGHOURS("EmployeeWorkingHours");

    private final String value;

    EmployeeNavigations(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmployeeNavigations fromValue(String v) {
        for (EmployeeNavigations c: EmployeeNavigations.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
