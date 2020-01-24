
package io.i4tech.odata.test.model.employee;

import io.i4tech.odata.common.model.ODataFunction;
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
public class CheckForDuplicates extends ODataFunction<EmployeeDuplicateCheckResult> {

    @XmlAttribute(name = "HTTP_METHOD")
    public final static String HTTP_METHOD = "GET";

    @XmlElement(name = "FirstName", required = true)
    protected String firstName;

    @XmlElement(name = "LastName", required = true)
    protected String lastName;


}
