package io.i4tech.odata.common.operation;


import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.model.ODataKey;
import io.i4tech.odata.common.operation.query.ODataQueryOperation;
import io.i4tech.odata.common.util.EncoderUtils;
import io.i4tech.odata.test.model.employee.Employee;
import io.i4tech.odata.test.model.employee.EmployeeLanguageCode;
import io.i4tech.odata.test.model.employee.EmployeeSkills;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ODataQueryOperationTest {

    @Mock
    private ODataClient client;

    @Test
    public void testSinglePath() {
        ODataQueryOperation<Employee> query = (ODataQueryOperation<Employee>) ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection", path);
    }

    @Test
    public void testSinglePathSingleKey() {
        ODataQueryOperation<Employee> query = (ODataQueryOperation<Employee>) ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class, Employee._ObjectID, "1")
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection('1')", path);
    }

    @Test
    public void testSinglePathMultiKey() {
        ODataQueryOperation<EmployeeSkills> query = ODataQueryOperation.builder()
                .client(client)
                .path(EmployeeSkills.class, ODataKey.<EmployeeSkills>builder()
                        .add(EmployeeSkills._SkillID, "1")
                        .add(EmployeeSkills._EmployeeID, "2")
                        .build())
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeSkillsCollection(SkillID='1',EmployeeID='2')", path);
    }

    @Test
    public void testMultiPathSingleKey() {
        ODataQueryOperation<EmployeeSkills> query = (ODataQueryOperation<EmployeeSkills>) ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class, Employee._ObjectID, "1")
                .path(EmployeeSkills.class)
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection('1')/EmployeeSkillsCollection", path);
    }

    @Test
    public void testMultiPathMultiKey() {
        ODataQueryOperation<EmployeeSkills> query = (ODataQueryOperation<EmployeeSkills>) ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class, Employee._ObjectID, "1")
                .path(EmployeeSkills.class, ODataKey.<EmployeeSkills>builder()
                        .add(EmployeeSkills._SkillID, "1")
                        .add(EmployeeSkills._EmployeeID, "2")
                        .build())
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection('1')/EmployeeSkillsCollection(SkillID='1',EmployeeID='2')", path);
    }

    @Test
    public void testSearch() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .search("searchTerm")
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$search='searchTerm'", path);
    }

    @Test
    public void testSingleFilter() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .filter(ODataFilter
                        .builder(Employee._FirstName, ODataFilter.Option.EQUALS, "value1's")
                        .build())
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$filter=FirstName+eq+%27value1%27%27s%27", path);
    }

    @Test
    public void testUUIDFilter() {
        final String uuid = UUID.randomUUID().toString();
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
            .client(client)
            .path(Employee.class)
            .filter(ODataFilter
                .builder(Employee._EmployeeUUID, ODataFilter.Option.EQUALS, uuid)
                .build())
            .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$filter=EmployeeUUID+eq+guid%27" + uuid + "%27", path);
    }

    @Test
    public void testDateTimeFilter() {
        final Date birthDate = new Date();
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
            .client(client)
            .path(Employee.class)
            .filter(ODataFilter
                .builder(Employee._BirthDate, ODataFilter.Option.GREATER_OR_EQUALS, birthDate)
                .build())
            .build();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String formattedDate = EncoderUtils.encode(dateFormat.format(birthDate));
        final String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$filter=BirthDate+ge+datetime%27" + formattedDate + "%27", path);
    }

    @Test
    public void testDateTimeOffsetFilter() {
        final Date lastChangedDate = new Date();
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .filter(ODataFilter
                        .builder(Employee._EntityLastChangedOn, ODataFilter.Option.LESS_THAN, lastChangedDate)
                        .build())
                .build();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String formattedDate = EncoderUtils.encode(dateFormat.format(lastChangedDate));
        final String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$filter=EntityLastChangedOn+lt+datetimeoffset%27" + formattedDate + "%27", path);
    }

    @Test
    public void testMultipleAndFilter() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .filter(ODataFilter
                        .builder(Employee._FirstName, ODataFilter.Option.EQUALS, "value1")
                        .and(Employee._LanguageCode, ODataFilter.Option.EQUALS, EmployeeLanguageCode.ENGLISH.value())
                        .build())
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$filter=FirstName+eq+%27value1%27+and+LanguageCode+eq+%27EN%27", path);
    }

    @Test
    public void testSingleExpand() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .expand(Employee._EmployeeSkills)
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$expand=EmployeeSkills", path);
    }

    @Test
    public void testMultipleExpand() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .expand(Employee._EmployeeSkills)
                .expand(Employee._EmployeeWorkingHours)
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$expand=EmployeeSkills,EmployeeWorkingHours", path);
    }

    @Test
    public void testSingleSelect() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .select(Employee._FirstName)
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$select=FirstName", path);
    }

    @Test
    public void testMultipleSelect() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .select(Employee._FirstName)
                .select(Employee._LastName)
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$select=FirstName,LastName", path);
    }

    @Test
    public void testExpandSelect() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .select(Employee._LastName)
                .expandSelect(Employee._EmployeeSkills, EmployeeSkills._SkillID)
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$expand=EmployeeSkills&$select=LastName,EmployeeSkills/SkillID", path);
    }

    @Test
    public void testSingleOrderBy() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .orderBy(Employee._FirstName, OrderByDirection.DESC)
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$orderby=FirstName desc", path);
    }

    @Test
    public void testMultipleOrderBy() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .orderBy(Employee._FirstName, OrderByDirection.DESC)
                .orderBy(Employee._LastName)
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$orderby=FirstName desc,LastName asc", path);
    }

    @Test
    public void testSkipTopOrderBy() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .skip(3)
                .top(1)
                .orderBy(Employee._FirstName)
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$orderby=FirstName asc&$skip=3&$top=1", path);
    }

    @Test
    public void testInlineCount() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .inlineCount()
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$inlinecount=allpages", path);
    }

    @Test
    public void testCount() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .count()
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection/$count", path);
    }

    @Test
    public void testCountWithFilter() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .filter(ODataFilter.builder(Employee._FirstName, ODataFilter.Option.EQUALS, "value1")
                        .build())
                .count()
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection/$count?$filter=FirstName+eq+%27value1%27", path);
    }

    @Test
    public void testManualQuery() {
        ODataQueryOperation<Employee> query = ODataQueryOperation.builder()
                .client(client)
                .collection("EmployeeCollection", Employee.class)
                .queryString("$filter=FirstName+eq+%27value1%27")
                .build();
        String path = query.getRequestPath();
        Assert.assertEquals("/EmployeeCollection?$filter=FirstName+eq+%27value1%27", path);
    }

    /*@Test
    public void testFunction() {
        ODataFunctionOperation<ContactDuplicateCheckResult> function = ODataFunctionOperation.builder()
                .function(new CheckForDuplicates()
                        .withFirstName("first & co")
                        .withLastName("last")
                        .withEmail("first.last@domain.com")
                        .withPhone("phone")
                        .withMobile("mobile")
                        .withFax("fax")
                )
                .build();
        String path = function.getRequestPath();
        Assert.assertEquals("/CheckForDuplicates?FirstName='first+%26+co'&LastName='last'&Phone='phone'&Mobile='mobile'&Fax='fax'&Email='first.last%40domain.com'", path);
    }*/
}
