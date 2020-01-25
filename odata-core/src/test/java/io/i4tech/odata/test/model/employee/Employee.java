package io.i4tech.odata.test.model.employee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.i4tech.odata.adapter.DateAdapter;
import io.i4tech.odata.adapter.TimestampAdapter;
import io.i4tech.odata.adapter.UuidAdapter;
import io.i4tech.odata.common.model.ODataEdmType;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataEntitySet;
import lombok.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ODataEntitySet(name = "EmployeeCollection")
public class Employee extends ODataEntity {

    @XmlElement(name = "ObjectID", required = true)
    @ODataEdmType("Edm.String")
    protected String objectID;

    @XmlElement(name = "EmployeeUUID", required = true)
    @XmlJavaTypeAdapter(UuidAdapter.class)
    @ODataEdmType("Edm.Guid")
    protected UUID employeeUUID;

    @XmlElement(name = "FirstName", required = false)
    @ODataEdmType("Edm.String")
    protected String firstName;

    @XmlElement(name = "LastName", required = false)
    @ODataEdmType("Edm.String")
    protected String lastName;

    @XmlElement(name = "LanguageCode", required = false)
    @XmlSchemaType(name = "string")
    @ODataEdmType("Edm.String")
    protected EmployeeLanguageCode languageCode;

    @XmlElement(name = "BirthDate", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlSchemaType(name = "date")
    @ODataEdmType("Edm.DateTime")
    protected Date birthDate;

    @XmlElement(name = "EntityLastChangedOn", required = true, type = String.class)
    @XmlJavaTypeAdapter(TimestampAdapter.class)
    @XmlSchemaType(name = "dateTime")
    @ODataEdmType("Edm.DateTimeOffset")
    protected Timestamp entityLastChangedOn;

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

    @XmlAttribute(name = "_EmployeeUUID")
    public final static EmployeeFields _EmployeeUUID = EmployeeFields.EMPLOYEEUUID;

    @XmlAttribute(name = "_FirstName")
    public final static EmployeeFields _FirstName = EmployeeFields.FIRSTNAME;

    @XmlAttribute(name = "_LastName")
    public final static EmployeeFields _LastName = EmployeeFields.LASTNAME;

    @XmlAttribute(name = "_LanguageCode")
    public final static EmployeeFields _LanguageCode = EmployeeFields.LANGUAGECODE;

    @XmlAttribute(name = "_BirthDate")
    public final static EmployeeFields _BirthDate = EmployeeFields.BIRTHDATE;

    @XmlAttribute(name = "_EntityLastChangedOn")
    public final static EmployeeFields _EntityLastChangedOn = EmployeeFields.ENTITYLASTCHANGEDON;

    @XmlAttribute(name = "_EmployeeSkills")
    public final static EmployeeNavigations _EmployeeSkills = EmployeeNavigations.EMPLOYEESKILLS;

    @XmlAttribute(name = "_EmployeeWorkingHours")
    public final static EmployeeNavigations _EmployeeWorkingHours = EmployeeNavigations.EMPLOYEEWORKINGHOURS;

}
