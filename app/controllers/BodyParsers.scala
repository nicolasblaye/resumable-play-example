package controllers

import java.io.ByteArrayOutputStream

import akka.util.ByteString
import play.api.libs.iteratee.Iteratee
import play.api.libs.streams.Streams
import play.api.mvc.BodyParsers.parse._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{BodyParser, MultipartFormData}
import play.core.parsers.Multipart.{FileInfo, FilePartHandler}

import scala.concurrent.ExecutionContext.Implicits.global

trait BodyParsers {
  // handle file part as Array[Byte]
  def handleFilePartAsByteArray: FilePartHandler[Array[Byte]] = {
    val os = new ByteArrayOutputStream()

    def handleFilePartAsBytes: FilePartHandler[Array[Byte]] = {
      case FileInfo(partName, filename, contentType) =>
        // simply write the data to the a ByteArrayOutputStream

        //we first build an iteratee that add data to the OutputStream
        val iteratee: Iteratee[ByteString, Array[Byte]] = Iteratee.foreach[ByteString] { data =>
          os.write(data.asByteBuffer.array())
        } map { _ =>
          os.close()
          os.toByteArray
        }
        // we create an accumulator out of it (because this is the new akka stream API)
        // thanks play 2.5 :/
        val accumulator = Streams.iterateeToAccumulator(iteratee)

        //once that accumulator, is built we take each byte of arrays from the accumulator and we build fileparts that contain the bytes
        accumulator.map((bytes: Array[Byte]) =>
          FilePart(partName, filename, contentType, bytes))
    }

    handleFilePartAsBytes
  }

  // custom body parser to handle file part as Array[Byte]
  def multipartFormDataAsBytes: BodyParser[MultipartFormData[Array[Byte]]] =
    multipartFormData(handleFilePartAsByteArray)
}
