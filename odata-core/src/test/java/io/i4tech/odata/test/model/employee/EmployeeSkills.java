package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.adapter.DateAdapter;
import io.i4tech.odata.common.model.ODataEdmType;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataEntitySet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ODataEntitySet(name = "EmployeeSkillsCollection")
public class EmployeeSkills extends ODataEntity {

    @XmlElement(name = "EmployeeID", required = true)
    @ODataEdmType("Edm.String")
    protected String employeeID;

    @XmlElement(name = "SkillID", required = true)
    @ODataEdmType("Edm.String")
    protected String skillID;

    @XmlElement(name = "ValidFrom", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlSchemaType(name = "date")
    @ODataEdmType("Edm.DateTime")
    protected Date validFrom;

    @XmlElement(name = "ValidTo", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlSchemaType(name = "date")
    @ODataEdmType("Edm.DateTime")
    protected Date validTo;

    @XmlAttribute(name = "_EmployeeID")
    public final static EmployeeSkillsKeyFields _EmployeeID = EmployeeSkillsKeyFields.EMPLOYEEID;

    @XmlAttribute(name = "_SkillID")
    public final static EmployeeSkillsKeyFields _SkillID = EmployeeSkillsKeyFields.SKILLID;

    @XmlAttribute(name = "_ValidFrom")
    public final static EmployeeSkillsFields _ValidFrom = EmployeeSkillsFields.VALIDFROM;

    @XmlAttribute(name = "_ValidTo")
    public final static EmployeeSkillsFields _ValidTo = EmployeeSkillsFields.VALIDTO;

}
