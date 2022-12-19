package day01

import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.api.scala._

object WordCount {
  def main(args: Array[String]): Unit = {

    //1.创建一个执行环境
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment

    //2. 读取文本
    val lineDs: DataSet[String] = env.readTextFile("input/word.txt")

    //3.转换数据
    lineDs.flatMap(line=>(line.split(" ")))


    lineDs
      .flatMap(_.split(" "))
      .map((_,1))
      .groupBy(0)
      .sum(1)
      .print()

  }
}
