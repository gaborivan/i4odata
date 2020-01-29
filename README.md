
# i4odata  
OData in Java made simple. For those, who wished for a Spring Repository API on an OData v2 Service. With paging and sorting. Like [PagingAndSortingRepository](https://docs.spring.io/spring-data/data-commons/docs/current/api/org/springframework/data/repository/PagingAndSortingRepository.html). 

## Table of Contents
- [Goals](#goals)
	- [Simple, type-safe querying](#simple-type-safe-querying)
	- [Codelists as enums](#codelists-as-enums)
	- [Simple, type-safe builder for OData operations](#simple-type-safe-builder-for-odata-operations)
	- [Simple builder for OData Clients](#simple-builder-for-odata-clients)
	- [Java model for Edm entities](#java-model-for-edm-entities)
	- [Synchronize Edm changes to Java model](#sync-edm-changes-to-java-model)
- [Getting started](#getting-started)
	- [Prerequisites](#prerequisites)
	- [Building and installing](#building-and-installing)
	- Running the tests
	- [Generating a Java model](#configuring-code-generation)
		- [Configuring XSD generation](#)
		- [Configuring Java code generation ](#configuring-java-code-generation )
	- [](#)
	- [](#)	
	- [](#)
	- [](#)
	- [](#)
	- [](#) 
### Generating the Java model(s)
### Creating a client

### Executing an operation
### Getting the result
## Goals

### Simple, type-safe querying 

As a developer, I would like to write paged OData queries as Spring Repository methods:
```
public class EmployeeRepository extends ODataEntityRepository<Employee> {
	
	/**  
	 * Find employees with matching language code. 
	 * 
	 * @param lang language code  
	 * @param pager paging state object  
	 * 
	 * @return next page of employees with the specified language code  
	 */
	public Page<Employee> findByLanguage(EmployeeLanguageCode lang, Pageable pager) {  
	    ...
	}
}
```

### Codelists as enums 
As a developer, I want to use codelist-type OData Entities of my choice as Java enums, so that I would not have to rely on String constants in my code:
```
public enum EmployeeLanguageCode implements ODataEnum {  
    GERMAN("DE"),  
    ENGLISH("EN"),  
    SPANISH("ES"),  
	...
}   
```

### Simple, type-safe builder for OData operations
As a developer, I would would like to have a query builder to craft the the OData request URI. The builder has to be type-safe, so that broken request URI-s would be reported at compile time.
```
ODataQueryOperation<Employee> query = ODataQueryOperation.builder()  
        .client(client)  
        .path(Employee.class)  
        .filter(ODataFilter.builder(Employee._FirstName, ODataFilter.Option.EQUALS, "value1")  
                .build())  
        .count()  
        .build();
```
### Simple builder for OData Clients 
As a developer, I would also like to have a client builder to access an OData Service. 
```
ODataClient client = ODataClient.builder()  
        .httpClient(httpClient)  
        .serviceUrl("https://myservice.domain.com/")  
        .authorization(ODataBasicAuthorization.builder()  
                .username("testuser")  
                .password("testpass")  
                .build())  
        .mapper(new ODataEntityMapper())  
        .build());
```
### Java model for Edm entities
As a developer, I want OData entities mapped to Java POJO-s. Just as I have POJO-s to represent relational entities when using JPA.
I want to have something like this:
```
@Data
public class Employee extends ODataEntity {  		// <-- Name of the Edm Entity
    private String objectID;						// <-- Key property of the Edm Entity  
    private String someProperty;					// <-- Just some boring property of the Edm Entity
    private EmployeeLanguageCode languageCode; 	// <-- Edm codelist type stuff as enums 
    private List<EmployeeSkills> employeeSkills;  // <-- Edm Navigation Property, relationship cardinality honored, of course  
} 
```
from this:
```
<EntityType Name="Employee">  
    <Key>  
        <PropertyRef Name="ObjectID"/>  
    </Key>  
    <Property Name="ObjectID" Type="Edm.String" ... />  
    <Property Name="SomeProperty" Type="Edm.String" ... />  
    <NavigationProperty Name="EmployeeSkills" ... />
</EntityType>    
```

### Sync Edm changes to Java model 
As a developer, I want Edm changes to be synchronized to the Java model during my build, so that i do not have to manually update the entity classes. A maven plugin will do just fine:
```
<plugin>  
    <groupId>io.i4tech.i4odata</groupId>  
    <artifactId>odata-generator</artifactId>  
	...
</plugin>
```
## Getting started

### Prerequisites
In order to build **i4odata**, you need the following tools on your system:
- Java JDK 8 or newer
- Maven 3

### Building and installing
To build and install i4odata maven artifacts you need to do the following simple steps:
1. Clone the git repository into a local folder (``<i4odata_home>``).
2. Execute a maven build:
```
$ cd <i4odata_home>
$ mvn clean install
```

## Using i4odata in your Java applications

### Prerequisites
#### Know your service Edm-s
#### Have a working knowledge of OData v2
 
### Configuring code generation
Generating the Java data model is a two-step process:
1. **EDMX to XSD**: Convert the Edmx of a service to an XML Schema (XSD) using the odata-generator maven plugin.
2. **XSD to Java**: Generate Java source code from the XSD using jaxb2-maven-plugin's xjc goal.

This means that we will need to configure two maven plugins to do the full job.

#### Configuring XSD generation
XSD-s are generated from the configured edmx file. You can select In order to generate an XSD successfully, you will need to configure a number a plugin parameters:

##### Input
- inputMetadata
##### Output
- outputSchema
- rootCollectionPath

##### Service
- rootServiceUrl
- entityPrefix
- basicAuthUser
- basicAuthPassword

##### Java package and superclasses of generated
- rootTargetNamespace
- entityBaseClass
- functionImportBaseClass
- fieldsMetaInterface
- navigationMetaInterface
- keyMetaInterface
- enumMetaInterface
- codelistWrapperMetaInterface
- contextualCodelistWrapperMetaInterface

##### Header entities
##### Function imports
##### Codelists

#### Configuring Java code generation 

### Generating the Java model(s)
### Creating a client

### Executing an operation
### Getting the result

### 
## Under the hood 


|Name|Value|Definition|House
|--|--|--|--
|Georgeiescu|thomaz||good
|eeee|rt|de|


  
### Mappable  
  
Each *Edm Entity* (defined as an *EntityType* in the OData v2 service's *Entity Metadata Document* , i.e. *edm*) should be mapped to a Java *Entity Class*, i.e.  
  
```  
<EntityType Name="Employee">  
   <Property Name="ObjectID" Type="Edm.String" ... />  
   ...  
</EntityType>  
  
```  
  
should be mapped to  
  
```  
public class Employee extends ODataEntity {  
   protected String objectID;  
   ...  
}  
```  
  
### Navigable  
  
Each *NavigationProperty* of an *Edm Entity* should be mapped as a member field of the corresponding *Entity Class*, i.e  
  
```  
<EntityType Name="Employee">  
   ...  
   <NavigationProperty Name="EmployeeSkills" ... />  
</EntityType>  
  
```  
  
should be mapped to  
  
  
```  
public class Employee extends ODataEntity {  
   ...  
   protected List<EmployeeSkills> employeeSkills;  
};  
```  
  
  
### Type-safe  
The i4odata object interface should be strongly typed, and leverage Edm type information to enable the following:
- Entity key, field and navigation names should be defined as Java constants so that compile-time verification that only relevant keys, fields and navigations are used in OData operations
```
ODataOperation<Employee> operation = ODataQueryOperation.builder()  
		
		// Allow only Employee key fields to be used in key predicates
        .path(Employee.class, Employee._ObjectID, "<key value>")  
        
        // Allow only Employee navigation fields to be used in expand clauses			    		
        .expand(Employee._EmployeeSkills)  
        
        // Allow only Employee fields to be used in filter clauses
		.filter(ODataFilter
				.builder(Employee._FirstName, ODataFilter.Option.EQUALS, "value1")  
				.build())
				
		... etc ...
		
        .build();
```
- Enums ...
  
### Generated  

  
  
## Components  
### OData Entity Mapper  
  
The object mapping is performed by the ``ODataEntityMapper``, that implements the following operations:
- convert an *Edm Entity* to an ``ODataEntity``:
``<E extends ODataEntity> E mapPropertiesToEntity(Map<String, Object> properties, Class<E> entityClass)`` 
 
- convert an ``ODataEntity``  to an *Edm Entity*:
``<E extends ODataEntity> Map<String, Object> mapEntityToProperties(E data, Class<E> entityClass)`` 
> Third-party dependency: com.fasterxml.jackson  
  
### OData Client  
Every *OData Service* is represented by an ``ODataClient`` object, responsible for the low-level plumbing and physical access to *OData v2* endpoints.  
You can create an ``ODataClient`` using its ``builder()``:  
```  
ODataClient client = ODataClient.builder()  
			.httpClient(HttpClients.custom().build())
			.serviceUrl("<Service URL>")  
	        .authorization(ODataBasicAuthorization.builder()  
                        .username("<user>")  
                        .password("<pass>")  
                        .build())  
            .mapper(new ODataEntityMapper())  
            .build());  
```  
### Client Operations  
Operations to *OData Services* are accessed via the command objects  
- ``ODataQueryOperation``  
- ``ODataCreateOperation``  
- ``ODataUpdateOperation``  
- ``ODataDeleteOperation``  
- ``ODataFunctionOperation``  
  
which - when executed - return their results as an ``ODataResponse`` object.  
Both **ODataClient**-s and **ODataQueryOperation** provide their own *Builder* objects.  
  
#### Example: Invoke OData Operation  
1. Create an **ODataClient** to access the *OData Service*  
```  
ODataClient client = ODataClient.builder()  
         .httpClient(HttpClients.custom().build())  
           .serviceUrl("<Service URL>")  
         .authorization(ODataBasicAuthorization.builder()  
                        .username("<user>")  
                        .password("<pass>")  
                        .build())  
                        .mapper(new ODataEntityMapper())  
            .build());  
```  
2. Create and execute the operation  
```  
List<Employee> employees = ODataQueryOperation.builder()  
        .client(client)  
        .path(Employee.class)  
        .build()  
        .execute()  
        .getResultList();  
 ```  
  
### Type safety  
**i4odata** is type safe. What is means is that code that compiles should produce a syntactically valid query.  
  
#### Metadata fields  
Metadata fields are defined for each OData *Property*, *NavigationProperty* and *Key*.  
  
##### Simple properties  
1. For every *OData Property* there are two Java fields in the  *OData Entity Class*, a **data field** and a **metadata field** (starting with an underscore).  
```  
public final static EmployeeFields _LastName = EmployeeFields.LASTNAME;  
```  
2. An **field metadata enum** exists for every *Entity Class*, containing the definition of its fields. The **field metadata enum** is tied to the *EntityClass* by supplying the class generic type parameter.  
```  
public enum EmployeeFields implements ODataFields<Employee> {  
    ...  
   LASTNAME("LastName"),  
   ...  
}  
```  
##### Key properties  
*Key Properties* of an *OData Entity* are represented similar to simple properties:  
  
1.   The *Entity Class* contains a **data field** and a **metadata key field** (starting with an underscore)  
```  
public final static EmployeeKeyFields _ObjectID = EmployeeKeyFields.OBJECTID;  
```  
2. An **key field metadata enum** exists for every *Entity Class*, containing the definition of its fields. The **key field metadata enum** is tied to the *EntityClass* by supplying the class generic type parameter.  
```  
public enum EmployeeKeyFields implements ODataKeyFields<Employee> {  
    ...  
   OBJECTID("ObjectID"),  
   ...  
}  
```  
##### Navigation properties  
1.   The *Entity Class* contains a **data field** and a **metadata navigation field** (starting with an underscore)  
```  
public final static EmployeeNavigations _EmployeeSkills = EmployeeNavigations.EMPLOYEESKILLS;  
```  
2. An **navigation field metadata enum** exists for every *Entity Class*, containing the definition of its fields. The **navigation field metadata enum** is tied to the *EntityClass* by supplying the class generic type parameter.  
```  
public enum EmployeeNavigations implements ODataNavigations<Employee> {  
    ...  
   EMPLOYEESKILLS("EmployeeSkills"),  
   ...  
}  
```  
#### Codelists  
  
#### Operations  
 
  
  
  
## Features  
  
  
  
## Getting Started  
  
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.  
  
### Prerequisites  
  
What things you need to install the software and how to install them  
  
```  
Give examples  
```  
  
### Installing  
  
A step by step series of examples that tell you how to get a development env running  
  
Say what the step will be  
  
```  
Give the example  
```  
  
And repeat  
  
```  
until finished  
```  
  
End with an example of getting some data out of the system or using it for a little demo  
  
## Running the tests  
  
Explain how to run the automated tests for this system  
  
### Break down into end to end tests  
  
Explain what these tests test and why  
  
```  
Give an example  
```  
  
### And coding style tests  
  
Explain what these tests test and why  
  
```  
Give an example  
```  
  
## Deployment  
  
Add additional notes about how to deploy this on a live system  
  
## Built With  
  
* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used  
* [Maven](https://maven.apache.org/) - Dependency Management  
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds  
  
## Contributing  
  
Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.  
  
## Versioning  
  
We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags).  
  
## Authors  
  
* **Billie Thompson** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)  
  
See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.  
  
## License  
  
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details  
  
## Acknowledgments  
  
* Hat tip to anyone whose code was used  
* Inspiration  
* etc  
  
  
> Written with [StackEdit](https://stackedit.io/).
