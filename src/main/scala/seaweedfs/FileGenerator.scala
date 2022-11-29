package seaweedfs

import java.io.{File, PrintWriter}
import scala.util.Random

/**
 * @desc: TODO
 * @author: along
 * @date: 2022/11/21
 * @version: 1.0
 */
object FileGenerator {
  val FILE_NUMS = 100
  val FILE_SIZE = 50
  val STR_LENGTH = 70


  def main(args: Array[String]): Unit = {
    val rand = new Random()
    val st = System.currentTimeMillis()
    println(s"starting generate ${FILE_NUMS} files... ")
    for(i<- 0 until(FILE_NUMS)){
      val file = new File("/Users/along/Downloads/files/test_file%02d.txt".format(i))
      val writer = new PrintWriter(file)
      for(j<-0 to FILE_SIZE){
        writer.write(rand.nextString(STR_LENGTH)+"\n")
      }
      writer.close()
      println("wrote file /Users/along/Downloads/MinioTest/src/main/resource/files/test_file%02d.txt".format(i))
    }
    println(s"finished! cost ${System.currentTimeMillis()-st}ms")
  }
}
