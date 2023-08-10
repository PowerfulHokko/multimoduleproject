package com.hokko.alpha.models.database

import java.sql.Date

case class UserKeys(userId: Long, algo: String, hash: Array[Byte], salt: Array[Byte], lastUpdate: Date)
