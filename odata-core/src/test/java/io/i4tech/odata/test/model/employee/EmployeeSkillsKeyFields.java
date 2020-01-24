package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataKeyFields;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EmployeeSkillsKeyFields")
@XmlEnum
public enum EmployeeSkillsKeyFields implements ODataKeyFields<EmployeeSkills> {

    @XmlEnumValue("EmployeeID")
    EMPLOYEEID("EmployeeID"),

    @XmlEnumValue("SkillID")
    SKILLID("SkillID");

    private final String value;

    EmployeeSkillsKeyFields(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmployeeSkillsKeyFields fromValue(String v) {
        for (EmployeeSkillsKeyFields c: EmployeeSkillsKeyFields.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
