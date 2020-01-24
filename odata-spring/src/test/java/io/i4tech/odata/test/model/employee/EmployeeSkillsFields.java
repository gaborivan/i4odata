package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataFields;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EmployeeSkillsFields")
@XmlEnum
public enum EmployeeSkillsFields implements ODataFields<EmployeeSkills> {

    @XmlEnumValue("EmployeeID")
    EMPLOYEEID("EmployeeID"),

    @XmlEnumValue("SkillID")
    SKILLID("SkillID"),

    @XmlEnumValue("ValidFrom")
    VALIDFROM("ValidFrom"),

    @XmlEnumValue("ValidTo")
    VALIDTO("ValidTo");

    private final String value;

    EmployeeSkillsFields(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmployeeSkillsFields fromValue(String v) {
        for (EmployeeSkillsFields c: EmployeeSkillsFields.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
