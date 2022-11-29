package minio

import io.minio._

import java.io.{File, InputStream}

/**
 * @Author: Airzihao
 * @Description:
 * @Date: Created at 12:09 2022/7/28
 * @Modified By:
 */

object MinIOOperator {

  val accessKey: String = "admin"
  val securityKey: String = "admin123"

  //  val bucketName: String = "performance-bucket"

  def main(args: Array[String]): Unit = {
    val path: String = args(0)
    val minioHostPath: String = args(1)
    val bucketName: String = args(2)
    val filesNames: Array[String] = new File(path).listFiles().map(file => file.getAbsolutePath)

    val minioClient: MinioClient = MinioClient.builder().endpoint(minioHostPath, 9000, false).credentials(accessKey, securityKey).build()


    var totalSize = 0l
    val threshold = 1024 * 1024 * 1024l
    val t1 = System.currentTimeMillis()
    while (true) {
      totalSize += write(minioClient, filesNames, minioHostPath, bucketName)
      if (totalSize >= threshold) {
        totalSize /= 1024 * 1024 * 1024l
        val elapse = (System.currentTimeMillis() - t1) / (1000 * 60)
        println(s"upload file size ${totalSize} GB cost ${elapse} minutes")
        totalSize = 0l
      }
      //      TestUtils.timing(s"Get ${filesNames.length} files",
      //        filesNames.foreach(fileName => {
      //          val name: String = fileName.split("/").last
      //          get(minioClient, name, bucketName + cur)
      //        }
      //        )
      //      )
    }


  }

  def write(minioClient: MinioClient, filesNames: Array[String], minioHostPath: String, bucketName: String): Long = {

    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()))
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

    //    println(s"Trying to upload ${filesNames.length} to $minioHostPath.")

    var size = 0l
    TestUtils.timing(s"Upload ${filesNames.length}",
      filesNames.foreach(fileName => {
        minioClient.uploadObject(
          UploadObjectArgs.builder()
            .bucket(bucketName)
            .`object`(System.currentTimeMillis() + fileName.split("/").last)
            .filename(fileName).build())
        size += new File(fileName).length()
      }
      )
    )
    size
  }

  def get(minioClient: MinioClient, objectName: String, bucketName: String): Unit = {
    val stream: InputStream = minioClient.getObject(
      GetObjectArgs.builder()
        .bucket(bucketName)
        .`object`(objectName)
        .build())
    stream.read(new Array[Byte](1024 * 1024))
    stream.close()
  }
}
