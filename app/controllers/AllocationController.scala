package controllers

import models.request.{AllocationRequest, ReturnEquipment}
import models.response.ApiResponse
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import services.{AllocationService, EquipmentService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._

class AllocationController @Inject()(
     val cc: ControllerComponents,
     allocationService: AllocationService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // request equipment allocation
  def requestAllocation(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[AllocationRequest] match {
      case JsSuccess(allocation, _) =>
        allocationService.requestEquipment(allocation).map{created =>
          ApiResponse.successResult(201, Json.toJson(created))
        }.recover {
          case ex: Exception =>
            ApiResponse.errorResult(s"Error creating allocationRequest: ${ex.getMessage}", 500)
        }
      case JsError(errors) =>
        Future.successful(
          ApiResponse.errorResult(
            "Invalid Allocation Request data",
            400
          ))
    }
  }

  // get allocation details by allocation id
  def getAllocationDetails(allocationId: Long): Action[AnyContent] = Action.async {
    allocationService.getAllocationDetails(allocationId).map(created =>
      ApiResponse.successResult(200, Json.toJson(created))
    )
  }

  // process the allocation request
  def processAllocation(allocationId: Long): Action[AnyContent] = Action.async {
    allocationService.processAllocation(allocationId).map{updated =>
      ApiResponse.successResult(200, Json.toJson(updated))
    }.recover{
      case ex: Exception =>
        ApiResponse.errorResult(s"Error processing allocationRequest: ${ex.getMessage}", 500)
    }
  }

  // process the equipment returned
  def processReturnEquipment(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[ReturnEquipment] match {
      case JsSuccess(req, _) =>
        allocationService.returnEquipment(req).map{returned =>
          ApiResponse.successResult(200, Json.toJson(returned))
        }.recover {
          case ex: Exception =>
            ApiResponse.errorResult(s"Error processing allocationRequest: ${ex.getMessage}", 500)
        }
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj(
          "message" -> "Invalid Return Equipment Request data",
          "errors" -> JsError.toJson(errors))))
    }
  }
}
