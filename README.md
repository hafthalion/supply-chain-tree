# Objective

Your task is to implement a backend service using Spring Boot with Kotlin to manage a tree data
structure. The tree will be represented using a set of edges, where each edge connects two
nodes. Your implementation should support functionalities to add and delete edges, as well as
retrieve the entire tree starting from a given node.

# Requirements

## Data Model

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

## Endpoints

### Create an Edge

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

# Technical Specifications

- Language: Kotlin
- Framework: Spring Boot
- Database: PostgreSQL
- ORM/Database Interaction: Use JOOQ for interacting with the PostgreSQL database.
- Data Format: JSON for all request and response payloads.
- Error Handling: The application should handle errors gracefully, including invalid inputs,
  duplicate entries, and database errors.

# Deliverables

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