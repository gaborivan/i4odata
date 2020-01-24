package io.i4tech.odata.common.operation;


import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.operation.function.ODataFunctionOperation;
import io.i4tech.odata.test.model.employee.CheckForDuplicates;
import io.i4tech.odata.test.model.employee.EmployeeDuplicateCheckResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ODataFunctionOperationTest {

    @Mock
    private ODataClient client;

    @Test
    public void testFunction() {
        ODataFunctionOperation<EmployeeDuplicateCheckResult> function = ODataFunctionOperation.builder()
                .client(client)
                .function(CheckForDuplicates.builder()
                        .firstName("first & co")
                        .lastName("last")
                        .build())
                .build();
        String path = function.getRequestPath();
        Assert.assertEquals("/CheckForDuplicates?FirstName='first+%26+co'&LastName='last'", path);
    }

}
