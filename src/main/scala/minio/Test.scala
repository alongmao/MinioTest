package minio

import io.minio.MinioClient
import org.apache.log4j.Logger

import java.io.File
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.{ArrayBlockingQueue, ThreadPoolExecutor, TimeUnit}

/**
 * @desc: TODO
 * @author: along
 * @date: 2022/10/22
 * @version: 1.0
 */
object Test {

  val accessKey: String = "admin"
  val securityKey: String = "admin123"
  val CORE_POOL_SIZE = 50
  val MAX_POOL_SIZE = 300
  val QUEUE_CAPACITY = 10000
  val KEEP_ALIVE_TIME = 1L
  val BUCKET_NUM = 100
  val logger = Logger.getLogger(this.getClass)


  def main(args: Array[String]): Unit = {

    val path: String = args(0)
    val minioHostPath: String = args(1)
    val bucketName: String = args(2)
    val filesNames: Array[String] = new File(path).listFiles().map(file => file.getAbsolutePath)

    /*每一个bucket有一把锁，防止线程争用同一个bucket导致抛出异常*/
    val locks: Array[ReentrantLock] = new Array[ReentrantLock](BUCKET_NUM)
    for (i <- locks.indices) {
      locks(i) = new ReentrantLock()
    }

    val minioClient: MinioClient = MinioClient.builder().endpoint(minioHostPath, 9000, false).credentials(accessKey, securityKey).build()

    val executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
      new ArrayBlockingQueue[Runnable](QUEUE_CAPACITY),
      new ThreadPoolExecutor.DiscardPolicy()
    )

//    while (true) {
//      val work = new UploadRunnable(10,minioClient, filesNames, bucketName, locks)
//      executor.execute(work)
//    }

  }
}
