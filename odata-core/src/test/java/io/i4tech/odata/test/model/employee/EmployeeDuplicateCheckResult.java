
package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.adapter.UuidAdapter;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataEntitySet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ODataEntitySet(name = "EmployeeDuplicateCheckResultCollection")
public class EmployeeDuplicateCheckResult extends ODataEntity {

    @XmlElement(name = "ObjectID", required = true)
    protected String objectID;

    @XmlElement(name = "EmployeeUUID", required = true, type = String.class)
    @XmlJavaTypeAdapter(UuidAdapter.class)
    protected UUID employeeUUID;

    @XmlAttribute(name = "_ObjectID")
    public final static EmployeeDuplicateCheckResultKeyFields _ObjectID = EmployeeDuplicateCheckResultKeyFields.OBJECTID;

    @XmlAttribute(name = "_EmployeeUUID")
    public final static EmployeeDuplicateCheckResultFields _EmployeeUUID = EmployeeDuplicateCheckResultFields.EMPLOYEEUUID;

}
