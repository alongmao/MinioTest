package minio

import io.minio.{BucketExistsArgs, MakeBucketArgs, MinioClient, UploadObjectArgs}
import org.apache.log4j.Logger

import java.io.{File, FileOutputStream}
import java.time.LocalDateTime
import java.util.concurrent.locks.ReentrantLock
import scala.util.Random

/**
 * @desc: TODO
 * @author: along
 * @date: 2022/10/22
 * @version: 1.0
 */
class UploadRunnable(n: Long, minioClient: MinioClient, fileNames: Array[String], bucketName: String, locks: Array[ReentrantLock],
                     countSize: Array[Long], countNum: Array[Long], filenameList: Array[File]) extends Runnable {
  val logger = Logger.getLogger(this.getClass)

  override def run(): Unit = {

    val suffix = new Random().nextInt(locks.length)
    val newBucketName = bucketName + suffix
    locks(suffix).lock()

    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(newBucketName).build()))
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(newBucketName).build());

    val fout = new FileOutputStream(filenameList(suffix), true)
    var j = 0L
    while (j < n/locks.length/100) {
      fileNames.foreach(fileName => {
        val randFilename = System.currentTimeMillis() + fileName.split("/").last
        minioClient.uploadObject(
          UploadObjectArgs.builder()
            .bucket(newBucketName)
            .`object`(randFilename)
            .filename(fileName).build())
        countSize.update(suffix, countSize(suffix) + new File(fileName).length())
        countNum.update(suffix, countNum(suffix) + 1)
        fout.write((randFilename + "\n").getBytes())
      })
      j += 1
    }
    fout.close()
    locks(suffix).unlock()
  }


}
