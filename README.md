# Supply chain tree API

A simple supply chain tree management API.

## Content
- [Objective](#objective)
- [Running the service](#running-the-service)
- [Design decisions](#design-decisions)
- [Requirements](#requirements)
- [Technical Specifications](#technical-specifications)
- [Deliverables](#deliverables)

## Objective

Your task is to implement a backend service using Spring Boot with Kotlin to manage a tree data
structure. The tree will be represented using a set of edges, where each edge connects two
nodes. Your implementation should support functionalities to add and delete edges, as well as
retrieve the entire tree starting from a given node.

## Running the service

The service is built as Spring Boot application using Gradle which gets all dependencies automatically. The database is an external dependency, Docker compose setup is provided for this. Database schema is automatically created using Liquibase during runtime.

Following system components are required to be installed:
1. JDK 21
2. Docker

To run and test the service you can follow these steps:
1. Go to the project directory
2. Run the start script: `run`
5. Open the service API UI: [Open API docs](http://localhost:8080/swagger-ui/index.html)
6. Optionally create a large test tree using: [/test/tree/from/{fromNodeId}](http://localhost:8080/swagger-ui/index.html#/Supply%20chain%20tree%20API%20for%20testing/generateLargeTestTree)

For large trees use command line instead of UI to avoid browser freezes:
```shell
curl -X 'GET' 'http://localhost:8080/api/tree/from/1' -H 'accept: application/json'
```

You can also use HTTP Client of IntelliJ Idea: [SupplyChainTreeApi.http](src/test/http/SupplyChainTreeApi.http).

## Design decisions

[REST API endpoints](src/main/kotlin/com/prewave/supplychaintree/api/SupplyChainTreeApi.kt) adhere to the restful principles where appropriate HTTP methods are used, incl. GET, POST, DELETE. For edge creation the POST method was chosen instead of PUT because of the requirement on the error response, which makes the endpoint not idempotent.

Some mandatory API parameters were placed directly into the path for the edges and tree to be able to act as an HTTP resource and easier handling.

For the GET tree endpoint a flat array structure of edges from a given node was chosen because of the requirement to handle large tree structures. A flat array structure can be streamed each element at a time in an effective way end to end. A true tree hierarchy would be too heavy on memory both on the server (generation) and client (parsing) side. The node elements are streamed in the tree hierarchy order, meaning that node ID references are forward only, allowing effective processing on the client side, i.e. processed elements can be forgotten.

The fetching of all reachable edges from the database a recursive SQL query with streaming is used at the moment as an easy and also surprisingly effective solution to avoid building complex read-models unless necessary.

Error handling is done using exceptions and leveraging the Spring framework to convert them into standard meaningful error responses.

A test API was added to easily generate a large tree structure for performance testing. This test API is meant to be behind an authorization check. For this Spring Security can be used.

## Requirements

### Data Model

1. Create a table named edge in a PostgreSQL database with the following columns:

- from_id: Represents the starting node ID of the edge (integer).
- to_id: Represents the ending node ID of the edge (integer).

Example table

| from_id | to_id |
|---------|-------|
| 1       | 2     |
| 1       | 3     |
| 2       | 4     |
| 2       | 5     |
| 3       | 6     |

In this example

- Node 1 is the root with two children: nodes 2 and 3
- Node 2 has two children: nodes 4 and 5
- Node 3 has one child: node 6

### Endpoints

#### Create an Edge

This endpoint should allow adding a new edge to the database. If an edge already exists the
operation should return an appropriate error response.

### Delete an Edge

This endpoint should allow deleting an existing edge based on the provided from_id and to_id.
If the specified edge does not exist, the operation should return an appropriate error response.

### Get Tree by Node ID

This endpoint should take a node ID as input and return the entire tree structure with the
specified node as the root.

### Design considerations

Please come up with an endpoint-design that allows to fetch the data
in a usable format. The response should be a JSON payload that contains the information of the
requested tree with the root-node given by the parameter. Also let the performance
considerations (see below) be part of your endpoint design considerations.

### Performance considerations

Since trees can be very large, the implementation should be
optimised for performance to handle large trees without performance degradation or memory
overflow.

## Technical Specifications

- Language: Kotlin
- Framework: Spring Boot
- Database: PostgreSQL
- ORM/Database Interaction: Use JOOQ for interacting with the PostgreSQL database.
- Data Format: JSON for all request and response payloads.
- Error Handling: The application should handle errors gracefully, including invalid inputs,
  duplicate entries, and database errors.

## Deliverables

1. A GitHub repository or ZIP file containing the complete source code.
2. Instructions for setting up and running the application locally, including any necessary
   database setup and dependencies.
3. Documentation or a Postman collection for testing the endpoints.

Please keep in mind that this task should reflect your approach on designing and implementing
a specific requirement. Even though we know about the benefits of supporting AI as part of the
daily development process, we want to evaluate your work with this task. Therefore we ask you
not to use any kind of AI tools like Copilot, Cursor, Claude... If we come to the conclusion that
your submission was done with such tools, we will end the interview process.

We look forward to reviewing your submission and assessing your approach to managing data
trees using Spring Boot and JOOQ!