# api-template

Template for an API written in scala with Spray.io and Slick. It can be used to bootstrap a project that needs a RESTful API. It provides support for easily implementing CRUD methods on resources that are backed on a relational database (eg. MySQL).

You will need to define the classes that represent your resources by extending the base domain trait [Resource](./src/main/scala/ro/mihneabaia/api/domain/base/Resource.scala).

You will also need to implement the resource operations supported by your resources. See [ResourceOperations.scala](./src/main/scala/ro/mihneabaia/api/domain/base/ResourceOperations.scala) for the traits that you need to provide implementation for depending on the operations your resources must support. You can also define optional validation logic for your resources here.

You will have to provide the json serialization and deserialization logic for your resources based on the spray-json support.

You should use the [RouteBuilder](./src/main/scala/ro/mihneabaia/api/router/base/RouteBuilder.scala) to build the routes that your API must provide for your resources.

For the persistence layer, you need to implement [ModelEntityRepository](./src/main/scala/ro/mihneabaia/api/repository/base/ModelEntityRepository.scala) for each of your tables. You should use the provided support for most common queries (CRUD operations). You must provide the Slick table row class for each table (you can write it by hand or get it using the code generation tool from Slick).

There are already two resources defined in this template: [Arena](./src/main/scala/ro/mihneabaia/api/domain/Arena.scala) and  [Venue](./src/main/scala/ro/mihneabaia/api/domain/Venue.scala). Use them as examples of how to implement simple resources (Venue) mapped to a single table or more complex resources (Arena) that is involved data from multiple tables.

The template provides support of optimistic locking, validation of resources and soft deletion and enabling/disabling of resources (using a status field).

Under development:
  * Unit tests :)
  * Make it available as a Typesafe Activator template
  * Add authentication/authorization support with [JSON Web Token](https://jwt.io/)
  * Add support for documenting the API with [Swagger](http://swagger.io/)
  * A lot more useful stuff...