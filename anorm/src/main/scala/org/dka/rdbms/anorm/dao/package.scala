package org.dka.rdbms.anorm.dao

import anorm.*
import anorm.SqlParser.*
import org.dka.rdbms.common.model.fields.{ID, Version}

def getID: RowParser[ID] = get[String](ID.fieldName).map(ID.build)

def getVersion: RowParser[Version] = get[Int](Version.fieldName).map(Version.build)
