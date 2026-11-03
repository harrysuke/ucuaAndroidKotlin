package com.ucua.app.core.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _reportDao: Lazy<ReportDao> = lazy {
    ReportDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "323f78a487ca7b5c1d88b27209ea66d1", "e6803dfe9a80b3d89fbec1a6ef8b3819") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `cached_reports` (`id` INTEGER NOT NULL, `staffId` TEXT, `formType` TEXT, `locationId` TEXT, `conditionDetails` TEXT, `offenceCode` TEXT, `violatorName` TEXT, `violatorCompany` TEXT, `submitDate` TEXT, `status` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '323f78a487ca7b5c1d88b27209ea66d1')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `cached_reports`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsCachedReports: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCachedReports.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedReports.put("staffId", TableInfo.Column("staffId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedReports.put("formType", TableInfo.Column("formType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedReports.put("locationId", TableInfo.Column("locationId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedReports.put("conditionDetails", TableInfo.Column("conditionDetails", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedReports.put("offenceCode", TableInfo.Column("offenceCode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedReports.put("violatorName", TableInfo.Column("violatorName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedReports.put("violatorCompany", TableInfo.Column("violatorCompany", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedReports.put("submitDate", TableInfo.Column("submitDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCachedReports.put("status", TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCachedReports: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCachedReports: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCachedReports: TableInfo = TableInfo("cached_reports", _columnsCachedReports, _foreignKeysCachedReports, _indicesCachedReports)
        val _existingCachedReports: TableInfo = read(connection, "cached_reports")
        if (!_infoCachedReports.equals(_existingCachedReports)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |cached_reports(com.ucua.app.core.database.ReportEntity).
              | Expected:
              |""".trimMargin() + _infoCachedReports + """
              |
              | Found:
              |""".trimMargin() + _existingCachedReports)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "cached_reports")
  }

  public override fun clearAllTables() {
    super.performClear(false, "cached_reports")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ReportDao::class, ReportDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun reportDao(): ReportDao = _reportDao.value
}
