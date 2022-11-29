import MinIOMultiThreadMinioOpt.minioClient
import io.minio.{BucketExistsArgs, MakeBucketArgs, MinioClient, UploadObjectArgs}
import org.junit.{BeforeClass, Test}

/**
 * @Author: Airzihao
 * @Description:
 * @Date: Created at 12:32 2022/7/28
 * @Modified By:
 */

object MinIOMultiThreadMinioOpt {
  val minioClient: MinioClient = MinioClient.builder()
      .endpoint("http://10.0.82.146:9000")
      .credentials("minioadmin", "minioadmin")
      .build();

  @BeforeClass
  def init(): Unit = {
    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket("test-bucket").build()))
    minioClient.makeBucket(MakeBucketArgs.builder().bucket("test-bucket").build());
  }

}

class MinIOMultiThreadMinioOpt {

  @Test
  def uploadTest(): Unit = {
    minioClient.uploadObject(
      UploadObjectArgs.builder()
        .bucket("test-bucket").`object`("car1.jpg").filename("./src/test/testInput/car1.jpg").build());
  }

  def downloadTest(): Unit = {

  }

}
