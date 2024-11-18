package config

object EnvConfig {
  def getKafkaBroker: String = sys.env.getOrElse("KAFKA_BROKERS", "localhost:9092")
}