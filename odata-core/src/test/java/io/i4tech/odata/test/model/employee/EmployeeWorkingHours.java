package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataEdmType;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataEntitySet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ODataEntitySet(name = "EmployeeWorkingHoursCollection")
public class EmployeeWorkingHours extends ODataEntity {

    @XmlElement(name = "ObjectID", required = true)
    @ODataEdmType("Edm.String")
    protected String objectID;

    @XmlAttribute(name = "_ObjectID")
    public final static EmployeeKeyFields _ObjectID = EmployeeKeyFields.OBJECTID;

}
