package io.i4tech.odata.test.model.employee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataEntitySet;
import lombok.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ODataEntitySet(name = "EmployeeCollection")
public class Employee extends ODataEntity {

    @XmlElement(name = "ObjectID", required = true)
    protected String objectID;

    @XmlElement(name = "FirstName", required = false)
    protected String firstName;

    @XmlElement(name = "LastName", required = false)
    protected String lastName;

    @XmlElement(name = "LanguageCode", required = false)
    @XmlSchemaType(name = "string")
    protected EmployeeLanguageCode languageCode;

    @Singular
    @XmlElement(name = "EmployeeSkills", required = true)
    @JsonIgnore
    protected List<EmployeeSkills> employeeSkills;

    @Singular("workingHours")
    @XmlElement(name = "EmployeeWorkingHours", required = true)
    @JsonIgnore
    protected List<EmployeeWorkingHours> employeeWorkingHours;


    @XmlAttribute(name = "_ObjectID")
    public final static EmployeeKeyFields _ObjectID = EmployeeKeyFields.OBJECTID;

    @XmlAttribute(name = "_FirstName")
    public final static EmployeeFields _FirstName = EmployeeFields.FIRSTNAME;

    @XmlAttribute(name = "_LastName")
    public final static EmployeeFields _LastName = EmployeeFields.LASTNAME;

    @XmlAttribute(name = "_LanguageCode")
    public final static EmployeeFields _LanguageCode = EmployeeFields.LANGUAGECODE;

    @XmlAttribute(name = "_EmployeeSkills")
    public final static EmployeeNavigations _EmployeeSkills = EmployeeNavigations.EMPLOYEESKILLS;

    @XmlAttribute(name = "_EmployeeWorkingHours")
    public final static EmployeeNavigations _EmployeeWorkingHours = EmployeeNavigations.EMPLOYEEWORKINGHOURS;

}
