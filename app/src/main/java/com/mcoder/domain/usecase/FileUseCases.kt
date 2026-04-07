package com.mcoder.domain.usecase

import com.mcoder.domain.FileRepository

/**
 * File manager use cases.
 */
class FileUseCases(private val repository: FileRepository) {
    val list = repository::list
    val readText = repository::readText
    val writeText = repository::writeText
    val createFolder = repository::createFolder
    val delete = repository::delete
    val rename = repository::rename
}
