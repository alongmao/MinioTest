package minio

import io.minio._

import java.io.{File, InputStream}

/**
 * @desc: TODO
 * @author: along
 * @date: 2022/10/25
 * @version: 1.0
 */
object SingleThreadMinioOpt {
  //  val logger = Logger.getLogger(this.getClass)
  val accessKey: String = "minioadmin"
  val securityKey: String = "minioadmin"

  //    val bucketName: String = "performance-bucket"

  def main(args: Array[String]): Unit = {
        val path: String = args(0)
        val minioHostPath: String = args(1)
        val bucketName: String = args(2)
        val filesNames: Array[String] = new File(path).listFiles().map(file => file.getAbsolutePath)

    val minioClient: MinioClient = MinioClient.builder().endpoint(minioHostPath, 9000, false).credentials(accessKey, securityKey).build()


        var totalSize = BigInt(0)
        val reportSize = BigInt(1024 * 1024 * 1024)
        val t1 = System.currentTimeMillis()
        var cardinality = BigInt(0)

//        while (true) {
          totalSize += write(minioClient, filesNames, minioHostPath, bucketName)
//          val tmp = totalSize / reportSize
//          if (tmp > cardinality) {
//            cardinality = tmp
//            val elapse = (System.currentTimeMillis() - t1).toDouble / (1.0 * 1000 * 60)
//            println(s"upload file size ${tmp} GB cost ${elapse} minutes")
//          }
//          TestUtils.timing(s"Get ${filesNames.length} files",
//            filesNames.foreach(fileName => {
//              val name: String = fileName.split("/").last
//              get(minioClient, name, bucketName)
//            }
//            )
//          )
//        }
//    val t1 = System.currentTimeMillis()
//    get(minioClient, "1666683311172b70d74f8968f493fb3d57e415fca2b8e.jpg", "performance-bucket")
//    println(s"read cost ${System.currentTimeMillis() - t1}ms")
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
