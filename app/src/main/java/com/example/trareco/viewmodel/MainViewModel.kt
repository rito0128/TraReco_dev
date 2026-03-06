package com.example.trareco.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trareco.data.DailyRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.example.trareco.data.DailyRecordDao
import com.example.trareco.data.TaskInfo

class MainViewModel(private val dao: DailyRecordDao) : ViewModel() {

    // 全データを取得するFlow（自動更新対応）
    val allRecords: Flow<List<DailyRecord>> = dao.loadAllUsers()

    //データを保存する関数
    fun saveRecord(date: String, todo1: TaskInfo, todo2: TaskInfo, todo3: TaskInfo) {
        viewModelScope.launch {
            val newRecord = DailyRecord(date = date, todo1 = todo1, todo2 = todo2, todo3 = todo3)
            dao.addNewRecord(newRecord)
        }
    }

//    // 最新の1件を取得する関数（suspend）
//    suspend fun getLastRecord(): DailyRecord? {
//        return dao.getLastRecord()
//    }

    // todoのチェックを更新する
    fun checkToDo(date: String, isDone: Boolean, taskIndex: Int){
        viewModelScope.launch {
            val record = dao.getRecordByDate(date) ?: return@launch
            val tasks = record.toList().toMutableList()

            tasks[taskIndex] = tasks[taskIndex].copy(isDone = isDone)
            saveRecord(date, tasks[0], tasks[1], tasks[2])
        }
    }

    // Map形式に変換
    fun convertToMap(records: List<DailyRecord>): List<Map<String, TaskInfo>>{
        val recordsMap1 = records.associate { it.date to it.todo1 }
        val recordsMap2 = records.associate { it.date to it.todo2 }
        val recordsMap3 = records.associate { it.date to it.todo3 }
        val recordsMapList: List<Map<String, TaskInfo>> = listOf(recordsMap1, recordsMap2, recordsMap3)

        return recordsMapList
    }

    @Composable
    fun TakeOverToDo(date: String, viewModel: MainViewModel, records: List<DailyRecord>){

        val recordsMapList = viewModel.convertToMap(records)

        val taskInfo1 = recordsMapList[0][date] ?: TaskInfo()
        val taskInfo2 = recordsMapList[1][date] ?: TaskInfo()
        val taskInfo3 = recordsMapList[2][date] ?: TaskInfo()

        LaunchedEffect(date){
            val lastRecord = dao.getLastRecord()

            if ((taskInfo1.taskName == "") && (taskInfo2.taskName == "") && (taskInfo3.taskName == "")) {
                if (lastRecord != null) {
                    val newTaskInfo1 = lastRecord.todo1.copy(isDone = false)
                    val newTaskInfo2 = lastRecord.todo2.copy(isDone = false)
                    val newTaskInfo3 = lastRecord.todo3.copy(isDone = false)

                    viewModel.saveRecord(date, todo1 = newTaskInfo1, todo2 = newTaskInfo2, todo3 = newTaskInfo3)
                }
            }
        }
    }
}