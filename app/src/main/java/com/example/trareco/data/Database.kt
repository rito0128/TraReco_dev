package com.example.trareco.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

data class TaskInfo(
    val isDone: Boolean = false,
    val taskName: String = "",
    val count: Int = 0
)

//データエンティティ
@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey val date: String,
    @Embedded(prefix = "todo1_") val todo1: TaskInfo,
    @Embedded(prefix = "todo2_") val todo2: TaskInfo,
    @Embedded(prefix = "todo3_") val todo3: TaskInfo,
){
    fun toList() = listOf(todo1, todo2, todo3)
}

//データアクセスオブジェクト（DAO）
@Dao
interface DailyRecordDao {
    //データの取得
    @Query("SELECT * FROM daily_records")
    fun loadAllUsers(): Flow<List<DailyRecord>>

    //挿入
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNewRecord(dailyRecord: DailyRecord)

    //削除
    @Delete
    suspend fun deleteRecord(dailyRecord: DailyRecord)

    // 【修正】特定の日付のデータを取得するクエリを追加（チェック更新用）
    @Query("SELECT * FROM daily_records WHERE date = :date")
    suspend fun getRecordByDate(date: String): DailyRecord?

    //最新の記録を1件だけ取得する
    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT 1")
    suspend fun getLastRecord(): DailyRecord?
}

//データベースオブジェクト
@Database(entities = [DailyRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): DailyRecordDao
}