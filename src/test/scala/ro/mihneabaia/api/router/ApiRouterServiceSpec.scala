package ro.mihneabaia.api.router

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

abstract class ApiRouterServiceSpec extends Specification with Specs2RouteTest with ApiRouterService {
  def actorRefFactory = system
  
//  "MyService" should {
//
//    "return a greeting for GET requests to the root path" in {
//      Get() ~> apiRoutes ~> check {
//        responseAs[String] must contain("Say hello")
//      }
//    }
//
//    "leave GET requests to other paths unhandled" in {
//      Get("/kermit") ~> apiRoutes ~> check {
//        handled must beFalse
//      }
//    }
//
//    "return a MethodNotAllowed error for PUT requests to the root path" in {
//      Put() ~> sealRoute(apiRoutes) ~> check {
//        status === MethodNotAllowed
//        responseAs[String] === "HTTP method not allowed, supported methods: GET"
//      }
//    }
//  }
}
