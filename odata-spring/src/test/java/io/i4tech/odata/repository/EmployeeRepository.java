package io.i4tech.odata.repository;

import io.i4tech.odata.common.operation.ODataFilter;
import io.i4tech.odata.common.operation.function.ODataFunctionOperation;
import io.i4tech.odata.test.model.employee.CheckForDuplicates;
import io.i4tech.odata.test.model.employee.Employee;
import io.i4tech.odata.test.model.employee.EmployeeDuplicateCheckResult;
import io.i4tech.odata.test.model.employee.EmployeeLanguageCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Component;

import java.util.List;


@NoRepositoryBean
@Component
public class EmployeeRepository extends ODataEntityRepository<Employee> {

    /**
     * Find employees by language code.
     *
     * @param lang language code
     * @param pager paging state object
     * @return next page of employees with the specified language code
     *
     */
    public Page<Employee> findByLanguage(EmployeeLanguageCode lang, Pageable pager) {
        return renderPagedResponse(
                getPagedQueryBuilder(pager)
                        .path(Employee.class)
                        .filter(ODataFilter
                                .builder(Employee._LanguageCode, ODataFilter.Option.EQUALS, lang.value())
                                .build())
                        .build()
                        .execute(),
                pager);
    }


    /**
     * Get the number of employees with the specified language code
     *
     * @param lang language code
     * @return number of employees with the specified language code
     *
     */
    public long countByLanguage(EmployeeLanguageCode lang) {
        return Long.parseLong(
                getQueryBuilder()
                        .path(Employee.class)
                        .filter(ODataFilter
                                .builder(Employee._LanguageCode, ODataFilter.Option.EQUALS, lang.value())
                                .build())
                        .count()
                        .build()
                        .execute()
                        .getResultValue());
    }


    /**
     * Find duplicate employee entries by calling CheckForDuplicates OData Function.
     *
     * @param duplicateCheckParams function parameters
     * @return Employees matching the specified parameters
     *
     */
    public List<EmployeeDuplicateCheckResult> checkForDuplicateEmployees(CheckForDuplicates duplicateCheckParams) {
        return ODataFunctionOperation.builder()
                .client(client)
                .function(duplicateCheckParams)
                .build()
                .execute()
                .getResultList();
    }

}
