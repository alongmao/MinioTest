package minio

/**
 * @Author: Airzihao
 * @Description:
 * @Date: Created at 14:19 2022/7/28
 * @Modified By:
 */
object TestUtils {
  def getModuleRootPath: String = System.getProperty("user.dir")

  def timing[T](msg: String, runnable: => T): T = {
    //    val t1 = System.currentTimeMillis()
    var result: T = null.asInstanceOf[T];
    result = runnable

    //    val t2 = System.currentTimeMillis()

    //    println(new Exception().getStackTrace()(1).toString)

    //    val elapsed = t2 - t1;
    //    println(s"$msg Time cost: ${elapsed}ms")

    result
  }
}
