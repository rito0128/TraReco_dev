package com.example.trareco

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import androidx.navigation.compose.NavHost
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.composable
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.room.Room
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextField
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue
import androidx.room.Embedded
import androidx.room.OnConflictStrategy
import androidx.compose.material3.Checkbox
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.LaunchedEffect


@SuppressLint("NewApi")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT, // 背景を透明に
                android.graphics.Color.TRANSPARENT  // ダークモード時の色
            )
        )

        super.onCreate(savedInstanceState)

        // データベースのインスタンス作成
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "daily_database"
        ).fallbackToDestructiveMigration().build()
        val dao = db.userDao()
        val viewModel = MainViewModel(dao)

        setContent {
            val navController = androidx.navigation.compose.rememberNavController()

            val records by viewModel.allRecords.collectAsState(initial = emptyList())

            // Map形式に変換しておくとカレンダーで使いやすい
            val recordsMap1 = records.associate { it.date to it.todo1 }
            val recordsMap2 = records.associate { it.date to it.todo2 }
            val recordsMap3 = records.associate { it.date to it.todo3 }

            val currentTime = LocalDateTime.now()
            val currentDate: String = currentTime.year.toString() + "_" + currentTime.monthValue.toString() + "_" + currentTime.dayOfMonth.toString()

            Column(modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
            )
            {
                NavHost(navController = navController,
                    startDestination = DailyToDo.Home.name,
                    modifier = Modifier.padding())
                {
                    //ホーム画面
                    composable(route = DailyToDo.Home.name)
                    {
                        Column(
                            modifier = Modifier,
                            horizontalAlignment = Alignment.CenterHorizontally)
                        {
                            DisplayTodayDate()

                            CheckToDo(currentDate, viewModel, records, recordsMap1,recordsMap2, recordsMap3)

                            FiveDaysCalendar(recordsMap1 = recordsMap1, recordsMap2 = recordsMap2, recordsMap3 = recordsMap3)
                            NewRecord(onNavigate = {
                                navController.navigate(DailyToDo.NewRecord.name)
                            })
                        }
                    }
                    //新しい記録
                    composable(route = DailyToDo.NewRecord.name)
                    {
                        Column(
                            modifier = Modifier,
                            horizontalAlignment = Alignment.CenterHorizontally)
                        {
                            //DisplayTodayDate()
                            InputNewRecord(onSaveClick = { inputedtask1, inputedtask2, inputedtask3 ->
                                val currentTime = LocalDateTime.now()
                                val date: String = currentTime.year.toString() + "_" + currentTime.monthValue.toString() + "_" + currentTime.dayOfMonth.toString()

                                //データベースに保存
                                viewModel.saveRecord(date, todo1 = inputedtask1, todo2 = inputedtask2, todo3 = inputedtask3)

                                //保存したらホーム画面に戻る
                                navController.popBackStack()
                            }, recordsMap1[currentDate] ?: TaskInfo(), recordsMap2[currentDate] ?: TaskInfo(), recordsMap3[currentDate] ?: TaskInfo())
                        }
                    }
                }
            }
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun DailyGoals(goals: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("毎日の目標")
        Text(goals)
    }
}

enum class DailyToDo() {
    Home,
    NewRecord
}

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
)

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

    //最新の記録を1件だけ取得する
    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT 1")
    suspend fun getLastRecord(): DailyRecord?
}

//データベースオブジェクト
@Database(entities = [DailyRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): DailyRecordDao
}

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

    // 最新の1件を取得する関数（suspend）
    suspend fun getLastRecord(): DailyRecord? {
        return dao.getLastRecord()
    }
}

@Composable
fun NewRecord(onNavigate: () -> Unit) {
    Button(onClick = {
            onNavigate()
        }, modifier = Modifier.padding(60.dp)) {
        Text("新しい記録")
    }
}

// FiveDaysCalendar 本体の修正
@SuppressLint("NewApi")
@Composable
fun FiveDaysCalendar(recordsMap1: Map<String, TaskInfo>, recordsMap2: Map<String, TaskInfo>, recordsMap3: Map<String, TaskInfo>) { // 引数を追加
    val currentTime = LocalDateTime.now()

    for (i in 0..4) {
        val nextTime = currentTime.minusDays(i.toLong())
        val nextDate: String = nextTime.year.toString() + "_" + nextTime.monthValue.toString() + "_" + nextTime.dayOfMonth.toString()

        val oneDayRecord1: TaskInfo = recordsMap1[nextDate]?: TaskInfo()
        val oneDayRecord2: TaskInfo = recordsMap2[nextDate]?: TaskInfo()
        val oneDayRecord3: TaskInfo = recordsMap3[nextDate]?: TaskInfo()

        DayCalendar(nextTime, oneDayRecord1, oneDayRecord2, oneDayRecord3)
    }
}

@SuppressLint("NewApi")
@Composable
fun DayCalendar(
    nextTime: LocalDateTime,
    record1: TaskInfo,
    record2: TaskInfo,
    record3: TaskInfo,
    )
{

    val nextDate: String = nextTime.year.toString() + "_" + nextTime.monthValue.toString() + "_" + nextTime.dayOfMonth.toString()
    val dayOfWeek = nextTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.JAPANESE)
    val day = nextTime.dayOfMonth


    val taskName1 = record1.taskName
    val isDone1 = record1.isDone
    val count1 = record1.count

    val taskName2 = record2.taskName
    val isDone2 = record2.isDone
    val count2 = record2.count

    val taskName3 = record3.taskName
    val isDone3 = record3.isDone
    val count3 = record3.count


    val checkInfo1: String = if (isDone1 == true){
        "☑"
    } else {
        "☐"
    }

    val checkInfo2: String = if (isDone2 == true){
        "☑"
    } else {
        "☐"
    }
    val checkInfo3: String = if (isDone3 == true){
        "☑"
    } else {
        "☐"
    }

    Row(
        horizontalArrangement = Arrangement.Center
    ){
        Text(text = day.toString() + "日", modifier = Modifier.weight(0.1f))
        Text(text = "( $dayOfWeek )", modifier = Modifier.weight(0.1f))
        Text(text = "①", modifier = Modifier.weight(0.05f), fontSize = 20.sp)
        Text(text = checkInfo1, modifier = Modifier.weight(0.1f), fontSize = 20.sp)
        Text(text = "②", modifier = Modifier.weight(0.05f), fontSize = 20.sp)
        Text(text = checkInfo2, modifier = Modifier.weight(0.1f), fontSize = 20.sp)
        Text(text = "③", modifier = Modifier.weight(0.05f), fontSize = 20.sp)
        Text(text = checkInfo3, modifier = Modifier.weight(0.1f), fontSize = 20.sp)
    }
}

@Composable
fun InputNewRecord(onSaveClick: (TaskInfo, TaskInfo, TaskInfo) -> Unit, taskInfo1: TaskInfo, taskInfo2: TaskInfo, taskInfo3: TaskInfo) {
    // 一つ目のタスク
    var text1 by remember { mutableStateOf(taskInfo1.taskName) }
    var count1 by remember { mutableStateOf(taskInfo1.count.toString()) }
    var countInt1 = 0

    val isDone1: Boolean = taskInfo1.isDone
    var oneDayRecord1 = TaskInfo()

    // 二つ目のタスク
    var text2 by remember { mutableStateOf(taskInfo2.taskName) }
    var count2 by remember { mutableStateOf(taskInfo2.count.toString()) }
    var countInt2 = 0

    val isDone2: Boolean = taskInfo2.isDone
    var oneDayRecord2 = TaskInfo()

    // 三つ目のタスク
    var text3 by remember { mutableStateOf(taskInfo3.taskName) }
    var count3 by remember { mutableStateOf(taskInfo3.count.toString()) }
    var countInt3 = 0

    val isDone3: Boolean = taskInfo3.isDone
    var oneDayRecord3 = TaskInfo()

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        Row(horizontalArrangement = Arrangement.Center){
            TextField(
                value = text1,
                onValueChange = { text1 = it },
                label = { Text("タスク") },
                modifier = Modifier.padding(8.dp).width(200.dp)
            )
            TextField(
                value = count1,
                onValueChange = { inputedValue: String ->
                    if (inputedValue.all { it.isDigit() } || inputedValue.isEmpty()) {
                        count1 = inputedValue
                    }
                },
                label = { Text("回数/時間") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.padding(8.dp).width(100.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.Center){
            TextField(
                value = text2,
                onValueChange = { text2 = it },
                label = { Text("タスク") },
                modifier = Modifier.padding(8.dp).width(200.dp)
            )
            TextField(
                value = count2,
                onValueChange = { inputedValue: String ->
                    if (inputedValue.all { it.isDigit() } || inputedValue.isEmpty()) {
                        count2 = inputedValue
                    }
                },
                label = { Text("回数/時間") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.padding(8.dp).width(100.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.Center){
            TextField(
                value = text3,
                onValueChange = { text3 = it },
                label = { Text("タスク") },
                modifier = Modifier.padding(8.dp).width(200.dp)
            )
            TextField(
                value = count3,
                onValueChange = { inputedValue: String ->
                    if (inputedValue.all { it.isDigit() } || inputedValue.isEmpty()) {
                        count3 = inputedValue
                    }
                },
                label = { Text("回数/時間") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.padding(8.dp).width(100.dp)
            )
        }

        if (count1 == "") {
            countInt1 = 0
        } else {
            countInt1 = count1.toInt()
        }

        if (count2 == "") {
            countInt2 = 0
        } else {
            countInt2 = count2.toInt()
        }

        if (count3 == "") {
            countInt3 = 0
        } else {
            countInt3 = count3.toInt()
        }

        oneDayRecord1 = TaskInfo(isDone1, text1, countInt1)
        oneDayRecord2 = TaskInfo(isDone2, text2, countInt2)
        oneDayRecord3 = TaskInfo(isDone3, text3, countInt3)


        // 保存ボタン
        Button(
            onClick = {
                if (text1.isNotBlank()){
                    onSaveClick(oneDayRecord1, oneDayRecord2, oneDayRecord3)
                }
            }
        ) {
            Text("保存")
        }
    }
}

@Composable
fun CheckToDo(date: String, viewModel: MainViewModel, records:List<DailyRecord>, recordsMap1: Map<String, TaskInfo>, recordsMap2: Map<String, TaskInfo>, recordsMap3: Map<String, TaskInfo>){

    if (records.isNotEmpty()){
        TakeOverToDo(date, viewModel, recordsMap1, recordsMap2, recordsMap3)
    }

    val taskInfo1 = recordsMap1[date] ?: TaskInfo()
    val taskInfo2 = recordsMap2[date] ?: TaskInfo()
    val taskInfo3 = recordsMap3[date] ?: TaskInfo()

    Column (modifier = Modifier.padding(24.dp)) {
        Row(modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            displayCheckBoxAndContent1(date,viewModel, taskInfo1, taskInfo2, taskInfo3)
            Text(text = "①", fontSize = 20.sp)
            Text(text = taskInfo1.taskName + "  " + taskInfo1.count + " 回", fontSize = 20.sp)
        }
        Row(modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            displayCheckBoxAndContent2(date,viewModel, taskInfo1, taskInfo2, taskInfo3)
            Text(text = "②", fontSize = 20.sp)
            Text(text = taskInfo2.taskName + "  " + taskInfo2.count + " 回", fontSize = 20.sp)
        }
        Row(modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            displayCheckBoxAndContent3(date,viewModel, taskInfo1, taskInfo2, taskInfo3)
            Text(text = "③", fontSize = 20.sp)
            Text(text = taskInfo3.taskName + "  " + taskInfo3.count + " 回", fontSize = 20.sp)
        }
    }
}

//@Composable
//@SuppressLint("NewApi")
//fun TakeOverToDo(date: String, viewModel: MainViewModel, recordsMap1: Map<String, TaskInfo>, recordsMap2: Map<String, TaskInfo>, recordsMap3: Map<String, TaskInfo>){
//
//    val taskInfo1 = recordsMap1[date] ?: TaskInfo()
//    val taskInfo2 = recordsMap2[date] ?: TaskInfo()
//    val taskInfo3 = recordsMap3[date] ?: TaskInfo()
//
//    val currentTime = LocalDateTime.now()
//    val previousTime = currentTime.minusDays(1)
//    val previousDate: String = previousTime.year.toString() + "_" + previousTime.monthValue.toString() + "_" + previousTime.dayOfMonth.toString()
//
//    if ((taskInfo1.taskName == "") && (taskInfo2.taskName == "") && (taskInfo3.taskName == "")) {
//        val previousTaskInfo1 = recordsMap1[previousDate] ?: TaskInfo()
//        val previousTaskInfo2 = recordsMap2[previousDate] ?: TaskInfo()
//        val previousTaskInfo3 = recordsMap3[previousDate] ?: TaskInfo()
//
//        val newTaskInfo1 = previousTaskInfo1.copy(isDone = false)
//        val newTaskInfo2 = previousTaskInfo2.copy(isDone = false)
//        val newTaskInfo3 = previousTaskInfo3.copy(isDone = false)
//
//        viewModel.saveRecord(date, todo1 = newTaskInfo1, todo2 = newTaskInfo2, todo3 = newTaskInfo3)
//    }
//}

@Composable
@SuppressLint("NewApi")
fun TakeOverToDo(date: String, viewModel: MainViewModel, recordsMap1: Map<String, TaskInfo>, recordsMap2: Map<String, TaskInfo>, recordsMap3: Map<String, TaskInfo>){

    val taskInfo1 = recordsMap1[date] ?: TaskInfo()
    val taskInfo2 = recordsMap2[date] ?: TaskInfo()
    val taskInfo3 = recordsMap3[date] ?: TaskInfo()

    LaunchedEffect(date) {
        val lastRecord = viewModel.getLastRecord()

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

@Composable
fun displayCheckBoxAndContent1(date: String, viewModel: MainViewModel, taskInfo1: TaskInfo, taskInfo2: TaskInfo, taskInfo3: TaskInfo): Boolean{
    var checkedState: Boolean by remember { mutableStateOf(taskInfo1.isDone) }
    var changedToDo = taskInfo1

    Checkbox(
        checked = taskInfo1.isDone,
        onCheckedChange = { isChecked ->
            checkedState = isChecked
            changedToDo = taskInfo1.copy(isDone = isChecked)
            viewModel.saveRecord(date, todo1 = changedToDo, todo2 = taskInfo2, todo3 = taskInfo3)
        }
    )

    return checkedState
}

@Composable
fun displayCheckBoxAndContent2(date: String, viewModel: MainViewModel, taskInfo1: TaskInfo, taskInfo2: TaskInfo, taskInfo3: TaskInfo): Boolean{
    var checkedState: Boolean by remember { mutableStateOf(taskInfo2.isDone) }
    var changedToDo = taskInfo2

    Checkbox(
        checked = taskInfo2.isDone,
        onCheckedChange = { isChecked ->
            checkedState = isChecked
            changedToDo = taskInfo2.copy(isDone = isChecked)
            viewModel.saveRecord(date, todo1 = taskInfo1, todo2 = changedToDo, todo3 = taskInfo3)
        }
    )

    return checkedState
}

@Composable
fun displayCheckBoxAndContent3(date: String, viewModel: MainViewModel, taskInfo1: TaskInfo, taskInfo2: TaskInfo, taskInfo3: TaskInfo): Boolean{
    var checkedState: Boolean by remember { mutableStateOf(taskInfo3.isDone) }
    var changedToDo = taskInfo3

    Checkbox(
        checked = taskInfo3.isDone,
        onCheckedChange = { isChecked ->
            checkedState = isChecked
            changedToDo = taskInfo3.copy(isDone = isChecked)
            viewModel.saveRecord(date, todo1 = taskInfo1, todo2 = taskInfo2, todo3 = changedToDo)
        }
    )

    return checkedState
}

@SuppressLint("NewApi")
@Composable
fun DisplayTodayDate() {
    val currentTime = LocalDateTime.now()
    val currentMonth = currentTime.monthValue
    val currentDay = currentTime.dayOfMonth
    val dayOfWeek = currentTime.dayOfWeek.getDisplayName(
        java.time.format.TextStyle.SHORT, // 「月曜」ならFULL、「月」ならSHORT
        java.util.Locale.JAPANESE// 日本語を指定
    )

    Text(text = "$currentMonth 月 $currentDay 日 ($dayOfWeek)", fontSize = 40.sp, modifier = Modifier.padding(40.dp))
}

////プレビュー
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun DisplayBoxPreview() {
//    Column(modifier = Modifier
//        .fillMaxSize()
//        .windowInsetsPadding(WindowInsets.safeDrawing),
//        horizontalAlignment = Alignment.CenterHorizontally
//    )
//    {
//        DailyGoals(goals = "腹筋を100回やる")
//        FiveDaysCalendar()
//        NewRecord(onNavigate = {})
//    }
//}
