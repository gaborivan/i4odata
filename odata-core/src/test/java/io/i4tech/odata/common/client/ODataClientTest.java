package io.i4tech.odata.common.client;


import io.i4tech.odata.common.authorization.ODataBasicAuthorization;
import io.i4tech.odata.common.mapper.ODataEntityMapper;
import io.i4tech.odata.common.operation.ODataOperation;
import io.i4tech.odata.common.operation.create.ODataCreateOperation;
import io.i4tech.odata.common.operation.delete.ODataDeleteOperation;
import io.i4tech.odata.common.operation.function.ODataFunctionOperation;
import io.i4tech.odata.common.operation.query.ODataQueryOperation;
import io.i4tech.odata.common.operation.update.ODataUpdateOperation;
import io.i4tech.odata.test.model.employee.*;
import io.i4tech.odata.test.util.ODataTestUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ODataClientTest {

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

    @Mock
    private HttpClient httpClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpResponse httpResponse;

    @Mock
    private Header httpResponseHeader;

    @Mock
    private Header csrfRequiredHeader;

    @Mock
    private EdmEntitySet entitySet;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Edm edm;

    private ODataClient client;


    private void setupMockCsrfToken() {
        when(httpResponseHeader.getName()).thenReturn(ODataClient.X_CSRF_TOKEN);
        when(httpResponseHeader.getValue()).thenReturn("CSRF_TOKEN_VALUE");
        when(csrfRequiredHeader.getName()).thenReturn(ODataClient.X_CSRF_TOKEN);
        when(csrfRequiredHeader.getValue()).thenReturn(ODataClient.X_CSRF_TOKEN_REQUIRED);
        when(httpResponse.getStatusLine().getStatusCode())
                .thenAnswer(ODataTestUtils.countedAnswer(cnt -> cnt == 2 ? 403 : 200));
        when(httpResponse.getHeaders(eq(ODataClient.X_CSRF_TOKEN)))
                .thenAnswer(ODataTestUtils.countedAnswer(cnt -> cnt == 1 ? new Header[]{csrfRequiredHeader} : new Header[]{httpResponseHeader}));
    }

    @Before
    public void setup() throws IOException, EntityProviderException, EdmException {
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);
        when(httpResponse.getEntity().getContent()).thenReturn(new ByteArrayInputStream(new byte[]{}));
        when(httpResponse.getStatusLine().getStatusCode()).thenReturn(200);
        EdmEntityType edmEntityType = mock(EdmEntityType.class, RETURNS_DEEP_STUBS);
        when(edmEntityType.getName()).thenReturn("Employee");
        when(edmEntityType.getProperty(anyString())).thenReturn(mock(EdmProperty.class));
        when(entitySet.getEntityType()).thenReturn(edmEntityType);
        when(entitySet.getName()).thenReturn("EmployeeCollection");
        when(edm.getDefaultEntityContainer().getEntitySet(eq("EmployeeCollection"))).thenReturn(entitySet);

        client = spy(ODataClient.builder()
                .httpClient(httpClient)
                .serviceUrl("https://myservice.domain.com/")
                .metadataArgument("labels", "true")
                .authorization(ODataBasicAuthorization.builder()
                        .username("testuser")
                        .password("testpass")
                        .build())
                .mapper(new ODataEntityMapper())
                .build());

        doReturn(edm).when(client).olingoReadMetaData(any(InputStream.class), eq(false));
        doReturn(mock(InputStream.class)).when(client).olingoWriteEntry(any(), any(), any(), any());
    }

    @Test
    public void testQuerySuccess() throws EdmException, IOException, IllegalAccessException, EntityProviderException {

        doReturn(ODataTestUtils.createFeedFrom(Collections.singletonList(employee1)))
                .when(client)
                .olingoReadFeed(anyString(), any(EdmEntitySet.class), any(InputStream.class), any(EntityProviderReadProperties.class));

        ODataOperation<Employee> operation = ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .search("text")
                .expand(Employee._EmployeeSkills)
                .expand(Employee._EmployeeWorkingHours)
                .build();

        ODataResponse<Employee> response = operation.execute();

        Assert.assertTrue(response.getResultList().size() > 0);
        Assert.assertEquals(employee1, response.getSingleResult());
    }

    @Test(expected = ODataException.class)
    public void testQueryEdmException() throws EdmException, IOException, IllegalAccessException, EntityProviderException {
        doThrow(new IllegalStateException()).when(client).olingoReadMetaData(any(InputStream.class), eq(false));

        ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .build()
                .execute();
    }

    @Test(expected = ODataException.class)
    public void testQueryEdmHttpError() throws EdmException, IOException, IllegalAccessException, EntityProviderException {
        when(httpResponse.getStatusLine().getStatusCode())
                .thenAnswer(ODataTestUtils.countedAnswer(cnt -> cnt == 1 ? 401 : 200));

        ODataQueryOperation.builder()
                .client(client)
                .path(Employee.class)
                .build()
                .execute();
    }

    @Test
    public void testCreateSuccess() throws EntityProviderException {
        setupMockCsrfToken();
        doReturn(ODataTestUtils.createEntryFrom(employee1))
                .when(client)
                .olingoReadEntry(anyString(), any(EdmEntitySet.class), any(InputStream.class), any(EntityProviderReadProperties.class));

        ODataResponse<Employee> response = ODataCreateOperation.builder()
                .client(client)
                .path(Employee.class)
                .data(employee1)
                .build()
                .execute();
        Assert.assertNotNull(response.getSingleResult());
    }

    @Test
    public void testUpdateFullSuccess() throws EntityProviderException {
        setupMockCsrfToken();
        doReturn(ODataTestUtils.createEntryFrom(employee1))
                .when(client)
                .olingoReadEntry(anyString(), any(EdmEntitySet.class), any(InputStream.class), any(EntityProviderReadProperties.class));

        ODataResponse<Employee> response = ODataUpdateOperation.builder()
                .client(client)
                .path(Employee.class, Employee._ObjectID, employee1.getObjectID())
                .data(employee1)
                .build()
                .execute();

        Assert.assertNotNull(response.getSingleResult());
    }

    @Test
    public void testUpdatePartialSuccess() throws EntityProviderException {
        setupMockCsrfToken();
        doReturn(ODataTestUtils.createEntryFrom(employee1))
                .when(client)
                .olingoReadEntry(anyString(), any(EdmEntitySet.class), any(InputStream.class), any(EntityProviderReadProperties.class));

        ODataResponse<Employee> response = ODataUpdateOperation.builder()
                .client(client)
                .path(Employee.class, Employee._ObjectID, employee1.getObjectID())
                .data(Employee.builder()
                        .languageCode(EmployeeLanguageCode.DUTCH)
                        .build())
                .build()
                .execute();

        Assert.assertNotNull(response.getSingleResult());
    }

    @Test
    public void testDeleteSuccess() throws EntityProviderException {
        setupMockCsrfToken();
        doReturn(ODataTestUtils.createEntryFrom(employee1))
                .when(client)
                .olingoReadEntry(anyString(), any(EdmEntitySet.class), any(InputStream.class), any(EntityProviderReadProperties.class));

        ODataResponse<Employee> response = ODataDeleteOperation.builder()
                .client(client)
                .path(Employee.class, Employee._ObjectID, employee1.getObjectID())
                .build()
                .execute();

        Assert.assertNull(response.getSingleResult());
    }

    @Test
    public void testFunctionSuccess() throws EntityProviderException {
        EmployeeDuplicateCheckResult duplicate = EmployeeDuplicateCheckResult.builder()
                .objectID("DUPLICATE_ID")
                .employeeUUID(UUID.randomUUID())
                .build();
        doReturn(ODataTestUtils.createFeedFrom(Collections.singletonList(duplicate)))
                .when(client)
                .olingoReadFeed(anyString(), any(EdmEntitySet.class), any(InputStream.class), any(EntityProviderReadProperties.class));

        ODataResponse<EmployeeDuplicateCheckResult> response = ODataFunctionOperation.builder()
                .client(client)
                .function(CheckForDuplicates.builder()
                        .firstName("first & co")
                        .lastName("last")
                        .build())
                .build()
                .execute();

        Assert.assertEquals(duplicate, response.getSingleResult());
    }

    @Test
    public void testConnection() {
        final boolean isConnected = client.testConnection();
        Assert.assertTrue(isConnected);
    }

}
