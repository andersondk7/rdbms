package org.dka.rdbms.anorm.dao

import anorm.*
import anorm.SqlParser.*
import org.dka.rdbms.common.model.fields.{CreateDate, ID, UpdateDate, Version}

import java.time.LocalDateTime

def getID: RowParser[ID] = get[String](ID.fieldName).map(ID.build)

def getVersion: RowParser[Version] = get[Int](Version.fieldName).map(Version.build)

def getCreateDate: RowParser[CreateDate] = get[LocalDateTime](CreateDate.fieldName).map(CreateDate.build)

def getUpdateDate: RowParser[Option[UpdateDate]] = get[Option[LocalDateTime]](UpdateDate.fieldName).map(UpdateDate.build)
