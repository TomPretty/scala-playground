package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import services.AwsCloudWatch
import services.AwsCloudWatch.MetricRequest

import java.time.Instant

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method will be
    * called when the application receives a `GET` request with a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    val request = MetricRequest(
      namespace = "Mapi/PROD/mobile-fronts",
      name = "cricket-api-success",
      dimensions = Map[String, String](),
      value = 1.0,
      stat = "Sum",
      startTime = Instant.parse("2021-09-03T10:12:35Z"),
      endTime = Instant.parse("2021-09-03T09:12:35Z"),
      period = 60
    )
    AwsCloudWatch.metricGet(request)

    Ok(views.html.index())
  }
}
