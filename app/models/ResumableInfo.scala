package models

import java.io.File

import org.apache.commons.lang3.StringUtils

case class ResumableInfo(resumableChunkSize: Int,
                         resumableTotalSize: Long,
                         resumableIdentifier: String,
                         resumableFilename: String,
                         resumableRelativePath: String,
                         resumableFilePath: String
                          ) {

  private var uploadedChunks = List[Int]()

  def notValid: Boolean = {
    resumableChunkSize < 0 ||
      resumableTotalSize < 0 ||
      StringUtils.isEmpty(resumableIdentifier) ||
      StringUtils.isEmpty(resumableFilename) ||
      StringUtils.isEmpty(resumableRelativePath)
  }

  def checkIfUploadFinished: Boolean = {
    val count = Math.ceil(resumableTotalSize.toDouble / resumableChunkSize.toDouble).toInt
    1 until count foreach { i =>
      if (!uploadedChunks.contains(i)) return false
    }

    val file = new File(resumableFilePath)
    val newPath = file.getAbsolutePath.substring(0, file.getAbsolutePath.length - ".temp".length)
    file.renameTo(new File(newPath))
    true
  }

  def addUploadedChunk(chunkNumber: Int) = {
    uploadedChunks = chunkNumber :: uploadedChunks
  }

  def containsChunk(chunkNumber: Int) = {
    uploadedChunks.contains(chunkNumber)
  }
}
