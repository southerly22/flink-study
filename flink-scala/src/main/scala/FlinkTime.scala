import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

/**
 * ${description}
 *
 * @author lzx
 * @date 2023/04/26 16:54
 * */
object FlinkTime {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val ds1: DataStream[String] = env.socketTextStream("localhost", 9999)

    ds1.map(_.toUpperCase)
      .keyBy(_.substring(0, 2))
      .window(TumblingEventTimeWindows.of(Time.seconds(5)))
      .reduce((a,b)=>a.concat(b))
      .addSink(println(_))

    env.execute()
  }
}
