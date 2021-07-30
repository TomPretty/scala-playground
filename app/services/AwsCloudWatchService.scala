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
  val ProfileName = "membership"

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
      value: Double = 1.0
  )

  def metricGet(request: MetricRequest): Try[Unit] = {

    val start = Instant.parse("2021-07-28T10:12:35Z");
    val endDate = Instant.now();

    val met = Metric
      .builder()
      .metricName("2XX-response-code")
      .namespace("members-data-api")
      .build();

    val metStat = MetricStat
      .builder()
      .stat("Minimum")
      .period(60)
      .metric(met)
      .build();

    val dataQUery = MetricDataQuery
      .builder()
      .metricStat(metStat)
      .id("foo2")
      .returnData(true)
      .build();

		val dq = List(dataQUery)

    val getMetricDataRequest = GetMetricDataRequest.builder
      .maxDatapoints(100)
      .startTime(start)
      .endTime(endDate)
      .metricDataQueries(dq.asJava)
      .build()

    Try(client.getMetricData(getMetricDataRequest)).map(response => println(response.metricDataResults()))
  }

  private def buildMetricDatum(request: MetricRequest) = {
    val dimensions = request.dimensions
      .map { case (name, value) =>
        Dimension.builder.name(name).value(value).build()
      }
      .toList
      .asJava
    MetricDatum.builder
      .metricName(request.name)
      .dimensions(dimensions)
      .value(request.value)
      .unit(StandardUnit.COUNT)
      .build()
  }

  def test = println(client.listMetrics())
}
