package seaweedfs

import com.google.common.io.Files
import org.apache.log4j.Logger
import seaweedfs.client.{FilerClient, SeaweedInputStream, SeaweedOutputStream}

import java.io.{File, FileInputStream, FileOutputStream, IOException, InputStream}
import java.util.concurrent.{ArrayBlockingQueue, Executors, ScheduledExecutorService, ThreadPoolExecutor, TimeUnit}
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


/**
 * @desc: TODO
 * @author: along
 * @date: 2022/11/21
 * @version: 1.0
 */
object SwTest {
  val logger = Logger.getLogger("Seaweedfs Test")
  val CORE_POOL_SIZE = 2
  val MAX_POOL_SIZE = 50
  val KEEP_ALIVE_TIME = 1L
  val QUEUE_CAPACITY = 200
  var countSize = new Array[Long](CORE_POOL_SIZE)
  val ZIP_PATH = "/Users/along/Downloads/test.zip"

  def main(args: Array[String]): Unit = {

    val filerClients:Array[FilerClient] = Array(
      new FilerClient("10.0.82.146", 18888),
      new FilerClient("10.0.82.147", 18888)
    )

    val service = new ThreadPoolExecutor(CORE_POOL_SIZE + 1, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
      new ArrayBlockingQueue[Runnable](QUEUE_CAPACITY),
      new ThreadPoolExecutor.DiscardPolicy()
    )

    val st = System.currentTimeMillis()

    val relCountProgress: Runnable = () => {
      while (true) {
        if ((System.currentTimeMillis() - st) % 10000 == 0) {
          logger.info(s"thread upload details:${countSize.mkString(" ")}")
          logger.info(s"upload ${countSize.sum} files cost ${System.currentTimeMillis() - st}ms")
        }
      }
    }

    service.execute(relCountProgress)
    for (i <- 0 until (CORE_POOL_SIZE)) {
      service.execute(new Runnable {
        override def run(): Unit = {
          while (true) {
            val count = writeZip(filerClients(i), ZIP_PATH, "/test%02d".format(i))
            countSize.update(i, countSize(i) + count)
          }
        }
      })
    }
  }


  def writeSingleFile(filerClient: FilerClient, localFilePath: String, remoteParentDir: String): Unit = {
    val filename = localFilePath.substring(localFilePath.lastIndexOf("/") + 1)
    val seaweedOutputStream = new SeaweedOutputStream(filerClient, s"${remoteParentDir}/${filename}")
    Files.copy(new File(localFilePath), seaweedOutputStream)
    seaweedOutputStream.close()
  }

  def readSingleFile(filerClient: FilerClient, localFilePath: String, remoteFilePath: String): Unit = {
    val filename = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1)
    val file = new File(s"${localFilePath}/${filename}")
    if (!file.exists())
      file.createNewFile()

    val t1 = System.currentTimeMillis()
    val sis = new SeaweedInputStream(filerClient, remoteFilePath)
    val fout = new FileOutputStream(file)
    val bytesIn = new Array[Byte](16 * 1024)
    var read = 0
    try {
      while ( {
        read = sis.read(bytesIn, 0, 16 * 1024)
        read
      } != -1) {
        fout.write(bytesIn, 0, read)
      }
      logger.info(s"seaweedfs read ${remoteFilePath} cost ${System.currentTimeMillis() - t1}ms")
    } catch {
      case exception: Exception => logger.error(exception)
    } finally {
      fout.close()
      sis.close()
    }
  }

  def writeZip(filerClient: FilerClient, zipFilePath: String, remoteParentDir: String): Long = {
    val fis = new FileInputStream(zipFilePath)
    unZipFiles(filerClient, fis, remoteParentDir)
  }

  def readZip(filerClient: FilerClient, remoteFilePath: String): Unit = {
    val st = System.currentTimeMillis()
    val sis = new SeaweedInputStream(filerClient, remoteFilePath)
    parseZip(sis)
    val swProcessTime = System.currentTimeMillis() - st
    logger.info(s"seaweedfs read ${remoteFilePath} cost ${swProcessTime}ms\n")
  }

  def parseZip(is: InputStream): Unit = {
    val zin = new ZipInputStream(is)
    var ze: ZipEntry = null
    while ( {
      ze = zin.getNextEntry
      ze
    } != null
    ) println(ze.getName)
  }

  def unZipFiles(filerClient: FilerClient, is: InputStream, remoteParentDir: String): Long = {
    val zin = new ZipInputStream(is)
    var ze: ZipEntry = null
    var count: Long = 0
    while ( {
      ze = zin.getNextEntry
      ze
    } != null) {
      var filename = ze.getName
      if (filename.indexOf("/") >= 0) filename = filename.substring(filename.lastIndexOf("/") + 1)
      if (filename.length != 0) {
        val seaweedOutputStream = new SeaweedOutputStream(filerClient, s"$remoteParentDir/${System.currentTimeMillis()}$filename")
        val bytesIn = new Array[Byte](16 * 1024)
        var read = 0
        while ( {
          read = zin.read(bytesIn)
          read
        } != -1) {
          seaweedOutputStream.write(bytesIn, 0, read)
        }
        seaweedOutputStream.close()
        count += 1
      }
    }
    count
  }

}
