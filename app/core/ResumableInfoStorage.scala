package core

import models.ResumableInfo
import play.api.Play
import scala.util.Try
import java.io.File


object ResumableInfoStorage {

  val uploadDir = Try {
    Play.current.configuration.getString("local.upload.dir").get
  }.getOrElse("")

  private var mMap = Map[String, ResumableInfo]()

  def get(resumableInfo: ResumableInfo): ResumableInfo = {
    mMap.getOrElse(resumableInfo.resumableIdentifier, {
      mMap += (resumableInfo.resumableIdentifier -> resumableInfo)
      resumableInfo
    })
  }

  def remove(info: ResumableInfo) {
    mMap = mMap - info.resumableIdentifier
  }

  def getResumableInfo(resumableParams: Map[String, String]): Option[ResumableInfo] = {
    new File(uploadDir).mkdir
    val info = get(
      ResumableInfo(
        resumableChunkSize = resumableParams("resumableChunkSize").toInt,
        resumableTotalSize = resumableParams("resumableTotalSize").toLong,
        resumableIdentifier = resumableParams("resumableIdentifier"),
        resumableFilename = resumableParams("resumableFilename"),
        resumableRelativePath = resumableParams("resumableRelativePath"),
        resumableFilePath = new File(uploadDir, resumableParams("resumableFilename")).getAbsolutePath + ".temp"
      )
    )
    if (info.notValid) {
      remove(info)
      None
    } else Some(info)
  }
}
