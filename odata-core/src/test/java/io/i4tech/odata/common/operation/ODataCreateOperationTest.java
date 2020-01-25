package io.i4tech.odata.common.operation;


import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.model.ODataKey;
import io.i4tech.odata.common.operation.create.ODataCreateOperation;
import io.i4tech.odata.test.model.employee.Employee;
import io.i4tech.odata.test.model.employee.EmployeeSkills;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ODataCreateOperationTest {

    @Mock
    private ODataClient client;


    @Test
    public void testSinglePath() {
        ODataCreateOperation<Employee> create = ODataCreateOperation.builder()
                .client(client)
                .path(Employee.class)
                .data(Employee.builder().build())
                .build();
        String path = create.getRequestPath();
        Assert.assertEquals("/EmployeeCollection", path);
    }

    @Test
    public void testMultiPathSingleKey() {
        ODataCreateOperation<EmployeeSkills> create = ODataCreateOperation.builder()
                .client(client)
                .path(Employee.class, Employee._ObjectID, "1")
                .path(EmployeeSkills.class)
                .data(EmployeeSkills.builder().build())
                .build();
        String path = create.getRequestPath();
        Assert.assertEquals("/EmployeeCollection('1')/EmployeeSkillsCollection", path);
    }

    @Test
    public void testSinglePathMultiKey() {
        ODataCreateOperation<EmployeeSkills> create = ODataCreateOperation.builder()
                .client(client)
                .path(EmployeeSkills.class, ODataKey.<EmployeeSkills>builder()
                        .add(EmployeeSkills._SkillID, "1")
                        .add(EmployeeSkills._EmployeeID, "2")
                        .build())
                .data(EmployeeSkills.builder().build())
                .build();
        String path = create.getRequestPath();
        Assert.assertEquals("/EmployeeSkillsCollection(SkillID='1',EmployeeID='2')", path);
    }


}
