package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataFields;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EmployeeWorkingHoursFields")
@XmlEnum
public enum EmployeeWorkingHoursFields implements ODataFields<EmployeeWorkingHours> {

    @XmlEnumValue("ObjectID")
    OBJECTID("ObjectID");

    private final String value;

    EmployeeWorkingHoursFields(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmployeeWorkingHoursFields fromValue(String v) {
        for (EmployeeWorkingHoursFields c: EmployeeWorkingHoursFields.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
