package services

import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProviderChain,
  EnvironmentVariableCredentialsProvider,
  InstanceProfileCredentialsProvider,
  ProfileCredentialsProvider
}
import software.amazon.awssdk.regions.Region.EU_WEST_1
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.{
  Dimension,
  MetricDatum,
  PutMetricDataRequest,
  StandardUnit,
  GetMetricDataRequest
}

import scala.jdk.CollectionConverters._
import scala.util.Try
import java.time.Instant;
import software.amazon.awssdk.services.cloudwatch.model.Metric
import software.amazon.awssdk.services.cloudwatch.model.MetricStat
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery

object Aws {
  val ProfileName = "mobile"

  lazy val CredentialsProvider: AwsCredentialsProviderChain =
    AwsCredentialsProviderChain.builder
      .credentialsProviders(
        ProfileCredentialsProvider.builder.profileName(ProfileName).build(),
        EnvironmentVariableCredentialsProvider.create()
      )
      .build()
}

object AwsCloudWatch {
  val client: CloudWatchClient = CloudWatchClient.builder
    .region(EU_WEST_1)
    .credentialsProvider(Aws.CredentialsProvider)
    .build()

  case class MetricRequest(
      namespace: String,
      name: String,
      dimensions: Map[String, String],
      value: Double = 1.0,
      stat: String,
      startTime: Instant,
      endTime: Instant,
      period: Int
  )

  def metricGet(request: MetricRequest): Try[Unit] = {
    buildMetricRequest(request, None)
  }

  private def buildMetricRequest(
      request: MetricRequest,
      nextToken: Option[String]
  ): Try[Unit] = {
    val metric = Metric
      .builder()
      .metricName(request.name)
      .namespace(request.namespace)
      .build();

    val metStat = MetricStat
      .builder()
      .stat(request.stat)
      .period(request.period)
      .metric(metric)
      .build();

    val dataQUery = MetricDataQuery
      .builder()
      .metricStat(metStat)
      .id("foo2")
      .returnData(true)
      .build();

    val dq = List(dataQUery)

    val getMetricDataRequestBuilder = GetMetricDataRequest.builder
      .maxDatapoints(100)
      .startTime(request.startTime)
      .endTime(request.endTime)
      .metricDataQueries(dq.asJava)

    nextToken match {
      case Some(x) => getMetricDataRequestBuilder.nextToken(x)
      case None    => getMetricDataRequestBuilder
    }

    val getMetricDataRequest = getMetricDataRequestBuilder.build()

    Try(client.getMetricData(getMetricDataRequest)).map(response => {
      if (response.nextToken() == null || response.nextToken().isEmpty)
        println(response.metricDataResults())
      else {
        println(response.metricDataResults())
        buildMetricRequest(request, Some(response.nextToken()))
      }
    })
  }

//  private def buildMetricDatum(request: MetricRequest) = {
//    val dimensions = request.dimensions
//      .map { case (name, value) =>
//        Dimension.builder.name(name).value(value).build()
//      }
//      .toList
//      .asJava
//    MetricDatum.builder
//      .metricName(request.name)
//      .dimensions(dimensions)
//      .value(request.value)
//      .unit(StandardUnit.COUNT)
//      .build()
//  }

  def test = println(client.listMetrics())
}
