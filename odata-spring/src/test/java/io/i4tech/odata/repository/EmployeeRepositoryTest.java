package io.i4tech.odata.repository;

import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.client.ODataResponse;
import io.i4tech.odata.test.model.employee.*;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeRepositoryTest {

    private static final Employee employee1 = Employee.builder()
            .objectID("id1")
            .firstName("firstName1")
            .lastName("lastName1")
            .languageCode(EmployeeLanguageCode.ENGLISH)
            .employeeSkill(EmployeeSkills.builder()
                    .employeeID("id1")
                    .skillID("skill1")
                    .validFrom(Date.valueOf(LocalDate.now()))
                    .build())
            .workingHours(EmployeeWorkingHours.builder()
                    .objectID("workinghours1")
                    .build())
            .build();

    private static final Employee employee2 = Employee.builder()
            .objectID("id2")
            .firstName("firstName2")
            .lastName("lastName2")
            .languageCode(EmployeeLanguageCode.GERMAN)
            .build();

    @Mock
    private ODataClient client;

    @Mock
    private ODataResponse<Employee> employeeResponse;

    @InjectMocks
    private EmployeeRepository employeeRepository;

    @Test
    public void testFindAll() {
        when(employeeResponse.getResultList()).thenReturn(Arrays.asList(employee1, employee2));
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection")))
                .thenReturn(employeeResponse);

        List<Employee> employees = StreamSupport.stream(employeeRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        Assert.assertEquals(2, employees.size());
        Assert.assertEquals(employee1, employees.get(0));
        Assert.assertEquals(employee2, employees.get(1));
    }

    @Test
    public void testFindAllSort() {
        when(employeeResponse.getResultList()).thenReturn(Arrays.asList(employee1, employee2));
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection?$orderby=FirstName asc")))
                .thenReturn(employeeResponse);
        
        Iterable<Employee> employees = employeeRepository.findAll(ODataSort.by(Employee._FirstName));
        
        Assert.assertEquals(2, ((List<Employee>)employees).size());
    }

    @Test
    public void testFindAllPaged() {
        final ODataResponse<Employee> page1Response = mock(ODataResponse.class);
        when(page1Response.getResultList()).thenReturn(Collections.singletonList(employee1));
        when(page1Response.getTotalCount()).thenReturn(2L);
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection?$skip=0&$top=1&$inlinecount=allpages")))
                .thenReturn(page1Response);

        final ODataResponse<Employee> page2Response = mock(ODataResponse.class);
        when(page2Response.getResultList()).thenReturn(Collections.singletonList(employee2));
        when(page2Response.getTotalCount()).thenReturn(2L);
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection?$skip=1&$top=1&$inlinecount=allpages")))
                .thenReturn(page2Response);

        Page<Employee> employees = employeeRepository.findAll(PageRequest.of(0,1));
        Assert.assertEquals(1, employees.getNumberOfElements());

        employees = employeeRepository.findAll(employees.getPageable().next());
        Assert.assertEquals(1, employees.getNumberOfElements());

    }

    @Test
    public void testFindAllPagedAndSorted() {
        final ODataResponse<Employee> page1Response = mock(ODataResponse.class);
        when(page1Response.getResultList()).thenReturn(Collections.singletonList(employee1));
        when(page1Response.getTotalCount()).thenReturn(2L);
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection?$orderby=FirstName desc&$skip=0&$top=1&$inlinecount=allpages")))
                .thenReturn(page1Response);

        final ODataResponse<Employee> page2Response = mock(ODataResponse.class);
        when(page2Response.getResultList()).thenReturn(Collections.singletonList(employee2));
        when(page2Response.getTotalCount()).thenReturn(2L);
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection?$orderby=FirstName desc&$skip=1&$top=1&$inlinecount=allpages")))
                .thenReturn(page2Response);

        Page<Employee> employees = employeeRepository.findAll(PageRequest.of(0,1, ODataSort.by(Sort.Direction.DESC, Employee._FirstName)));
        Assert.assertEquals(1, employees.getNumberOfElements());

        employees = employeeRepository.findAll(employees.getPageable().next());
        Assert.assertEquals(1, employees.getNumberOfElements());
    }


    @Test
    public void testSaveCreate() {
        final Employee newEmployee = Employee.builder().build();
        when(employeeResponse.getSingleResult()).thenReturn(employee1);
        when(client.create(eq(Employee.class), eq("/EmployeeCollection"), eq(newEmployee)))
                .thenReturn(employeeResponse);

        Employee employee = employeeRepository.save(newEmployee);

        Assert.assertEquals(employee1, employee);
    }

    @Test
    public void testSaveUpdate() {
        when(employeeResponse.getSingleResult()).thenReturn(employee1);
        when(client.update(eq(Employee.class), eq("/EmployeeCollection('id1')"), eq(employee1)))
                .thenReturn(employeeResponse);

        Employee employee = employeeRepository.save(employee1);

        Assert.assertEquals(employee1, employee);
    }

    @Test
    public void testSaveAllOneCreateOneUpdate() {
        final Employee newEmployee = Employee.builder().build();
        final ODataResponse<Employee> createResponse = mock(ODataResponse.class);
        when(createResponse.getSingleResult()).thenReturn(employee1);
        when(client.create(eq(Employee.class), eq("/EmployeeCollection"), eq(newEmployee)))
                .thenReturn(createResponse);

        final ODataResponse<Employee> updateResponse = mock(ODataResponse.class);
        when(updateResponse.getSingleResult()).thenReturn(employee2);
        when(client.update(eq(Employee.class), eq("/EmployeeCollection('id2')"), eq(employee2)))
                .thenReturn(updateResponse);

        List<Employee> employees = (List<Employee>) employeeRepository.saveAll(Arrays.asList(newEmployee, employee2));

        Assert.assertEquals(2,employees.size());
        Assert.assertTrue(employees.contains(employee1));
        Assert.assertTrue(employees.contains(employee2));
    }

    @Test
    public void testExistsByIdTrue() {
        when(employeeResponse.getResultList()).thenReturn(Collections.singletonList(employee1));
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection('id1')?$select=ObjectID")))
                .thenReturn(employeeResponse);

        final boolean exists = employeeRepository.existsById("id1");

        Assert.assertTrue(exists);
    }

    @Test
    public void testExistsByIdFalse() {
        when(employeeResponse.getResultList()).thenReturn(Collections.emptyList());
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection('NO_SUCH_ID')?$select=ObjectID")))
                .thenReturn(employeeResponse);

        final boolean exists = employeeRepository.existsById("NO_SUCH_ID");

        Assert.assertFalse(exists);
    }

    @Test
    public void testFindByIdSuccess() {
        when(employeeResponse.getResultList()).thenReturn(Collections.singletonList(employee1));
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection('id1')")))
                .thenReturn(employeeResponse);

        final Optional<Employee> employee = employeeRepository.findById("id1");

        Assert.assertTrue(employee.isPresent());
        Assert.assertEquals(employee1, employee.get());
    }

    @Test
    public void testFindByIdFail() {
        when(employeeResponse.getResultList()).thenReturn(Collections.emptyList());
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection('NO_SUCH_ID')")))
                .thenReturn(employeeResponse);

        final Optional<Employee> employee = employeeRepository.findById("NO_SUCH_ID");

        Assert.assertFalse(employee.isPresent());
    }

    @Test
    public void testFindAllById() {
        final ODataResponse<Employee> id1Response = mock(ODataResponse.class);
        when(id1Response.getResultList()).thenReturn(Collections.singletonList(employee1));
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection('id1')")))
                .thenReturn(id1Response);

        final ODataResponse<Employee> id2Response = mock(ODataResponse.class);
        when(id2Response.getResultList()).thenReturn(Collections.singletonList(employee2));
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection('id2')")))
                .thenReturn(id2Response);

        final ODataResponse<Employee> noSuchIdResponse = mock(ODataResponse.class);
        when(noSuchIdResponse.getResultList()).thenReturn(Collections.emptyList());
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection('NO_SUCH_ID')")))
                .thenReturn(noSuchIdResponse);

        final List<Employee> employees = (List<Employee>) employeeRepository.findAllById( Arrays.asList(
                "id1",
                "NO_SUCH_ID",
                "id2"));

        Assert.assertEquals(2, employees.size());
        Assert.assertTrue(employees.contains(employee1));
        Assert.assertTrue(employees.contains(employee2));
    }

    @Test
    public void testCount() {
        when(employeeResponse.getResultValue()).thenReturn("2");
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection/$count")))
                .thenReturn(employeeResponse);

        final long numEmployees = employeeRepository.count();

        Assert.assertEquals( 2, numEmployees);
    }

    @Test
    public void testDeleteById() {
        employeeRepository.deleteById("id1");

        verify(client, times(1)).delete(eq(Employee.class), eq("/EmployeeCollection('id1')"));
    }

    @Test
    public void testDelete() {
        employeeRepository.delete(employee1);

        verify(client, times(1)).delete(eq(Employee.class), eq("/EmployeeCollection('id1')"));
    }

    @Test
    public void testDeleteAllEntities() {
        employeeRepository.deleteAll(Arrays.asList(employee1, employee2));

        verify(client, times(1)).delete(eq(Employee.class), eq("/EmployeeCollection('id1')"));
        verify(client, times(1)).delete(eq(Employee.class), eq("/EmployeeCollection('id2')"));
    }

    @Test(expected = NotImplementedException.class)
    public void testDeleteAll() {
        employeeRepository.deleteAll();
    }

    @Test
    public void testSearchPaged() {
        when(employeeResponse.getResultList()).thenReturn(Collections.singletonList(employee1));
        when(employeeResponse.getTotalCount()).thenReturn(1L);
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection?$search='term'&$skip=0&$top=1&$inlinecount=allpages")))
                .thenReturn(employeeResponse);

        Page<Employee> employees = employeeRepository.search("term", PageRequest.of(0,1));
        Assert.assertEquals(1, employees.getNumberOfElements());
    }

    @Test
    public void testFindByLanguage() {
        when(employeeResponse.getResultList()).thenReturn(Collections.singletonList(employee1));
        when(employeeResponse.getTotalCount()).thenReturn(1L);
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection?$filter=LanguageCode+eq+%27NL%27&$skip=0&$top=1&$inlinecount=allpages")))
                .thenReturn(employeeResponse);

        Page<Employee> employees = employeeRepository.findByLanguage(EmployeeLanguageCode.DUTCH, PageRequest.of(0,1));
        Assert.assertEquals(1, employees.getNumberOfElements());
    }

    @Test
    public void testCountByLanguage() {
        when(employeeResponse.getResultValue()).thenReturn("2");
        when(client.read(eq("EmployeeCollection"), eq(Employee.class), eq("/EmployeeCollection/$count?$filter=LanguageCode+eq+%27DE%27")))
                .thenReturn(employeeResponse);

        final long numEmployees = employeeRepository.countByLanguage(EmployeeLanguageCode.GERMAN);
        Assert.assertEquals( 2, numEmployees);
    }

    @Test
    public void testCheckForDuplicateEmployees() {
        EmployeeDuplicateCheckResult duplicate = EmployeeDuplicateCheckResult.builder()
                .objectID("id1")
                .build();

        ODataResponse<EmployeeDuplicateCheckResult> response = mock(ODataResponse.class);
        when(response.getResultList()).thenReturn(Collections.singletonList(duplicate));
        when(client.function(eq(EmployeeDuplicateCheckResult.class), eq("/CheckForDuplicates?FirstName='first'&LastName='last'"), any()))
                .thenReturn(response);

        final List<EmployeeDuplicateCheckResult> duplicates = employeeRepository.checkForDuplicateEmployees(
                CheckForDuplicates.builder()
                        .firstName("first")
                        .lastName("last")
                .build()
        );
        Assert.assertEquals(1, duplicates.size());
        Assert.assertEquals(duplicate, duplicates.get(0));
    }

}
