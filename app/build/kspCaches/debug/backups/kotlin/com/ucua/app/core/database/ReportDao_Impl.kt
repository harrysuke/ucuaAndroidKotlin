package com.ucua.app.core.database

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ReportDao_Impl(
  __db: RoomDatabase,
) : ReportDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfReportEntity: EntityInsertAdapter<ReportEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfReportEntity = object : EntityInsertAdapter<ReportEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `cached_reports` (`id`,`staffId`,`formType`,`locationId`,`conditionDetails`,`offenceCode`,`violatorName`,`violatorCompany`,`submitDate`,`status`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ReportEntity) {
        statement.bindLong(1, entity.id)
        val _tmpStaffId: String? = entity.staffId
        if (_tmpStaffId == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpStaffId)
        }
        val _tmpFormType: String? = entity.formType
        if (_tmpFormType == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpFormType)
        }
        val _tmpLocationId: String? = entity.locationId
        if (_tmpLocationId == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpLocationId)
        }
        val _tmpConditionDetails: String? = entity.conditionDetails
        if (_tmpConditionDetails == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpConditionDetails)
        }
        val _tmpOffenceCode: String? = entity.offenceCode
        if (_tmpOffenceCode == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpOffenceCode)
        }
        val _tmpViolatorName: String? = entity.violatorName
        if (_tmpViolatorName == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpViolatorName)
        }
        val _tmpViolatorCompany: String? = entity.violatorCompany
        if (_tmpViolatorCompany == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpViolatorCompany)
        }
        val _tmpSubmitDate: String? = entity.submitDate
        if (_tmpSubmitDate == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpSubmitDate)
        }
        val _tmpStatus: String? = entity.status
        if (_tmpStatus == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpStatus)
        }
      }
    }
  }

  public override suspend fun upsertAll(reports: List<ReportEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfReportEntity.insert(_connection, reports)
  }

  public override suspend fun upsert(report: ReportEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfReportEntity.insert(_connection, report)
  }

  public override fun observeAll(): Flow<List<ReportEntity>> {
    val _sql: String = "SELECT * FROM cached_reports ORDER BY id DESC"
    return createFlow(__db, false, arrayOf("cached_reports")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStaffId: Int = getColumnIndexOrThrow(_stmt, "staffId")
        val _columnIndexOfFormType: Int = getColumnIndexOrThrow(_stmt, "formType")
        val _columnIndexOfLocationId: Int = getColumnIndexOrThrow(_stmt, "locationId")
        val _columnIndexOfConditionDetails: Int = getColumnIndexOrThrow(_stmt, "conditionDetails")
        val _columnIndexOfOffenceCode: Int = getColumnIndexOrThrow(_stmt, "offenceCode")
        val _columnIndexOfViolatorName: Int = getColumnIndexOrThrow(_stmt, "violatorName")
        val _columnIndexOfViolatorCompany: Int = getColumnIndexOrThrow(_stmt, "violatorCompany")
        val _columnIndexOfSubmitDate: Int = getColumnIndexOrThrow(_stmt, "submitDate")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: MutableList<ReportEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ReportEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStaffId: String?
          if (_stmt.isNull(_columnIndexOfStaffId)) {
            _tmpStaffId = null
          } else {
            _tmpStaffId = _stmt.getText(_columnIndexOfStaffId)
          }
          val _tmpFormType: String?
          if (_stmt.isNull(_columnIndexOfFormType)) {
            _tmpFormType = null
          } else {
            _tmpFormType = _stmt.getText(_columnIndexOfFormType)
          }
          val _tmpLocationId: String?
          if (_stmt.isNull(_columnIndexOfLocationId)) {
            _tmpLocationId = null
          } else {
            _tmpLocationId = _stmt.getText(_columnIndexOfLocationId)
          }
          val _tmpConditionDetails: String?
          if (_stmt.isNull(_columnIndexOfConditionDetails)) {
            _tmpConditionDetails = null
          } else {
            _tmpConditionDetails = _stmt.getText(_columnIndexOfConditionDetails)
          }
          val _tmpOffenceCode: String?
          if (_stmt.isNull(_columnIndexOfOffenceCode)) {
            _tmpOffenceCode = null
          } else {
            _tmpOffenceCode = _stmt.getText(_columnIndexOfOffenceCode)
          }
          val _tmpViolatorName: String?
          if (_stmt.isNull(_columnIndexOfViolatorName)) {
            _tmpViolatorName = null
          } else {
            _tmpViolatorName = _stmt.getText(_columnIndexOfViolatorName)
          }
          val _tmpViolatorCompany: String?
          if (_stmt.isNull(_columnIndexOfViolatorCompany)) {
            _tmpViolatorCompany = null
          } else {
            _tmpViolatorCompany = _stmt.getText(_columnIndexOfViolatorCompany)
          }
          val _tmpSubmitDate: String?
          if (_stmt.isNull(_columnIndexOfSubmitDate)) {
            _tmpSubmitDate = null
          } else {
            _tmpSubmitDate = _stmt.getText(_columnIndexOfSubmitDate)
          }
          val _tmpStatus: String?
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null
          } else {
            _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          }
          _item = ReportEntity(_tmpId,_tmpStaffId,_tmpFormType,_tmpLocationId,_tmpConditionDetails,_tmpOffenceCode,_tmpViolatorName,_tmpViolatorCompany,_tmpSubmitDate,_tmpStatus)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: Long): ReportEntity? {
    val _sql: String = "SELECT * FROM cached_reports WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStaffId: Int = getColumnIndexOrThrow(_stmt, "staffId")
        val _columnIndexOfFormType: Int = getColumnIndexOrThrow(_stmt, "formType")
        val _columnIndexOfLocationId: Int = getColumnIndexOrThrow(_stmt, "locationId")
        val _columnIndexOfConditionDetails: Int = getColumnIndexOrThrow(_stmt, "conditionDetails")
        val _columnIndexOfOffenceCode: Int = getColumnIndexOrThrow(_stmt, "offenceCode")
        val _columnIndexOfViolatorName: Int = getColumnIndexOrThrow(_stmt, "violatorName")
        val _columnIndexOfViolatorCompany: Int = getColumnIndexOrThrow(_stmt, "violatorCompany")
        val _columnIndexOfSubmitDate: Int = getColumnIndexOrThrow(_stmt, "submitDate")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: ReportEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStaffId: String?
          if (_stmt.isNull(_columnIndexOfStaffId)) {
            _tmpStaffId = null
          } else {
            _tmpStaffId = _stmt.getText(_columnIndexOfStaffId)
          }
          val _tmpFormType: String?
          if (_stmt.isNull(_columnIndexOfFormType)) {
            _tmpFormType = null
          } else {
            _tmpFormType = _stmt.getText(_columnIndexOfFormType)
          }
          val _tmpLocationId: String?
          if (_stmt.isNull(_columnIndexOfLocationId)) {
            _tmpLocationId = null
          } else {
            _tmpLocationId = _stmt.getText(_columnIndexOfLocationId)
          }
          val _tmpConditionDetails: String?
          if (_stmt.isNull(_columnIndexOfConditionDetails)) {
            _tmpConditionDetails = null
          } else {
            _tmpConditionDetails = _stmt.getText(_columnIndexOfConditionDetails)
          }
          val _tmpOffenceCode: String?
          if (_stmt.isNull(_columnIndexOfOffenceCode)) {
            _tmpOffenceCode = null
          } else {
            _tmpOffenceCode = _stmt.getText(_columnIndexOfOffenceCode)
          }
          val _tmpViolatorName: String?
          if (_stmt.isNull(_columnIndexOfViolatorName)) {
            _tmpViolatorName = null
          } else {
            _tmpViolatorName = _stmt.getText(_columnIndexOfViolatorName)
          }
          val _tmpViolatorCompany: String?
          if (_stmt.isNull(_columnIndexOfViolatorCompany)) {
            _tmpViolatorCompany = null
          } else {
            _tmpViolatorCompany = _stmt.getText(_columnIndexOfViolatorCompany)
          }
          val _tmpSubmitDate: String?
          if (_stmt.isNull(_columnIndexOfSubmitDate)) {
            _tmpSubmitDate = null
          } else {
            _tmpSubmitDate = _stmt.getText(_columnIndexOfSubmitDate)
          }
          val _tmpStatus: String?
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null
          } else {
            _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          }
          _result = ReportEntity(_tmpId,_tmpStaffId,_tmpFormType,_tmpLocationId,_tmpConditionDetails,_tmpOffenceCode,_tmpViolatorName,_tmpViolatorCompany,_tmpSubmitDate,_tmpStatus)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: Long) {
    val _sql: String = "DELETE FROM cached_reports WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM cached_reports"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
