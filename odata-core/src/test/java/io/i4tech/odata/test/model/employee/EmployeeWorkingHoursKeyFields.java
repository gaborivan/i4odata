package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataKeyFields;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EmployeeWorkingHoursKeyFields")
@XmlEnum
public enum EmployeeWorkingHoursKeyFields implements ODataKeyFields<EmployeeWorkingHours> {

    @XmlEnumValue("ObjectID")
    OBJECTID("ObjectID");

    private final String value;

    EmployeeWorkingHoursKeyFields(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmployeeWorkingHoursKeyFields fromValue(String v) {
        for (EmployeeWorkingHoursKeyFields c: EmployeeWorkingHoursKeyFields.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
