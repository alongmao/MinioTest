package minio

import io.minio.{BucketExistsArgs, MakeBucketArgs, MinioClient, UploadObjectArgs}
import org.apache.log4j.Logger

import java.io.File
import java.time.LocalDateTime
import java.util.Date
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.{ArrayBlockingQueue, ThreadPoolExecutor, TimeUnit}
import scala.util.Random

/**
 * @desc: TODO
 * @author: along
 * @date: 2022/10/22
 * @version: 1.0
 */
object multiThreadMinioOpt {

  val accessKey: String = "minioadmin"
  val securityKey: String = "minioadmin"
  val CORE_POOL_SIZE = 20
  val MAX_POOL_SIZE = 300
  val QUEUE_CAPACITY = 10000
  val KEEP_ALIVE_TIME = 10L
  val BUCKET_NUM = 100
  val logger = Logger.getLogger(this.getClass)


  def main(args: Array[String]): Unit = {

    val path: String = args(0)
    val minioHostPath: String = args(1)
    val bucketName: String = args(2)
    val list: String = args(3)
    val n: Long = args(4).toLong
    val filesNames: Array[String] = new File(path).listFiles().map(file => file.getAbsolutePath)
    val countSize: Array[Long] = new Array[Long](CORE_POOL_SIZE)
    val countNum: Array[Long] = new Array[Long](CORE_POOL_SIZE)
    val filenameList: Array[File] = new Array[File](CORE_POOL_SIZE)


    /*每一个bucket有一把锁，防止线程争用同一个bucket导致抛出异常*/
    val locks: Array[ReentrantLock] = new Array[ReentrantLock](CORE_POOL_SIZE)
    for (i <- locks.indices) {
      locks(i) = new ReentrantLock()
      countSize(i) = 0
      countNum(i) = 0
      filenameList(i) = new File(s"$list/file${i}_list.txt")
      if (!filenameList(i).exists()) {
        filenameList(i).createNewFile()
      }
    }

    val minioClient: MinioClient = MinioClient.builder().endpoint(minioHostPath, 9000, false).credentials(accessKey, securityKey).build()

    val executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
      new ArrayBlockingQueue[Runnable](QUEUE_CAPACITY),
      new ThreadPoolExecutor.DiscardPolicy()
    )

    var flag = false

    new Thread(() => {
      val t1 = System.currentTimeMillis()
      while (!flag) {
        val elapse = System.currentTimeMillis() - t1
        if (elapse % 5000 == 0) {
          println(s"transfer:${countSize.mkString(" ")}")
          println(s"obj numbers: ${countNum.mkString(" ")}")
          println(f"finish ${countNum.sum * 1.0 / n * 100}%.2f%% cost${System.currentTimeMillis() - t1}ms")
        }
        flag = countNum.sum >= n
      }
      println(s"transfer:${countSize.mkString(" ")}")
      println(s"obj numbers: ${countNum.mkString(" ")}")
      println(f"finish ${countNum.sum * 1.0 / n * 100}%.2f%% cost${System.currentTimeMillis() - t1}ms")

    }).start()

    while (!flag) {
      val work = new UploadRunnable(n, minioClient, filesNames, bucketName, locks, countSize, countNum, filenameList)
      executor.execute(work)
    }

    executor.shutdown()
    while (!executor.isTerminated) {

    }
  }
}


