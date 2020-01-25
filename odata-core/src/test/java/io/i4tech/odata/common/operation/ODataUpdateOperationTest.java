package io.i4tech.odata.common.operation;


import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.model.ODataKey;
import io.i4tech.odata.common.operation.update.ODataUpdateOperation;
import io.i4tech.odata.test.model.employee.Employee;
import io.i4tech.odata.test.model.employee.EmployeeSkills;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ODataUpdateOperationTest {

    @Mock
    private ODataClient client;

    @Test
    public void testSinglePathSingleKey() {
        ODataUpdateOperation<Employee> update = ODataUpdateOperation.builder()
                .client(client)
                .path(Employee.class, Employee._ObjectID, "1")
                .data(Employee.builder().build())
                .build();
        String path = update.getRequestPath();
        Assert.assertEquals("/EmployeeCollection('1')", path);
    }

    @Test
    public void testSinglePathMultiKey() {
        ODataUpdateOperation<EmployeeSkills> update = ODataUpdateOperation.builder()
                .client(client)
                .path(EmployeeSkills.class, ODataKey.<EmployeeSkills>builder()
                        .add(EmployeeSkills._SkillID, "1")
                        .add(EmployeeSkills._EmployeeID, "2")
                        .build())
                .data(EmployeeSkills.builder().build())
                .build();
        String path = update.getRequestPath();
        Assert.assertEquals("/EmployeeSkillsCollection(SkillID='1',EmployeeID='2')", path);
    }

    @Test
    public void testMultiPathMultiKey() {
        ODataUpdateOperation<EmployeeSkills> update = ODataUpdateOperation.builder()
                .client(client)
                .path(Employee.class, Employee._ObjectID, "1")
                .path(EmployeeSkills.class, ODataKey.<EmployeeSkills>builder()
                        .add(EmployeeSkills._SkillID, "1")
                        .add(EmployeeSkills._EmployeeID, "2")
                        .build())
                .data(EmployeeSkills.builder().build())
                .build();
        String path = update.getRequestPath();
        Assert.assertEquals("/EmployeeCollection('1')/EmployeeSkillsCollection(SkillID='1',EmployeeID='2')", path);
    }




}
