package com.example.trareco.ui.theme

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import com.example.trareco.data.AppDatabase
import com.example.trareco.viewmodel.MainViewModel
import com.example.trareco.data.TaskInfo
import com.example.trareco.data.DailyRecord
import com.example.trareco.utils.DateUtils
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.BottomAppBar
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.trareco.R
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.HorizontalDivider

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
            val navController = rememberNavController()

            val records by viewModel.allRecords.collectAsState(initial = emptyList())
            val currentDate = DateUtils.formatToKey(0)

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            Scaffold(
                bottomBar = {
                    BottomAppBar(
                        modifier = Modifier.height(90.dp),
                        containerColor = White
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically){
                            Box(modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },//ボタンの状態を渡す
                                    indication = null//ボタンを押したときのアニメーションを失くす
                                )
                                {
                                    navController.navigate(DailyToDo.Home.name)
                                }
                                .weight(1f), contentAlignment = Alignment.Center)
                            {
                                DisplayIconAndText("ホーム", icon = R.drawable.house,
                                    isClicked =
                                    if (currentRoute == DailyToDo.Home.name) {
                                        true
                                    } else {
                                        false
                                    }
                                )
                            }
                            Box(modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },//ボタンの状態を渡す
                                    indication = null//ボタンを押したときのアニメーションを失くす
                                )
                                {
                                    navController.navigate(DailyToDo.Calendar.name)
                                }
                                .weight(1f), contentAlignment = Alignment.Center)
                            {
                                DisplayIconAndText("カレンダー", icon = R.drawable.calendar_1,
                                    isClicked =
                                    if (currentRoute == DailyToDo.Calendar.name) {
                                        true
                                    } else {
                                        false
                                    }
                                )
                            }
                        }
                    }
                }
            )
            { innerPadding ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    //color = MaterialTheme.colorScheme.background
                    color = Gainsboro// <----動的にしたい
                ){
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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
                                if (records.isNotEmpty()){
                                    viewModel.TakeOverToDo(currentDate, viewModel, records)
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 20.dp, end = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                )
                                {
                                    Box(
                                        //contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(4.2f)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(color = DarkOrange)
                                    ){
                                        Column(
                                            modifier = Modifier,
                                            horizontalAlignment = Alignment.CenterHorizontally)
                                        {
                                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center){
                                                DisplayTodayDate(fontSize = 35, White)
                                            }
                                            Box(modifier = Modifier
                                                .padding(10.dp)
                                                .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                                                .weight(3f)
                                                .background(color = White),
                                                contentAlignment = Alignment.Center
                                            ){
                                                CheckToDo(currentDate, viewModel, records)
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(4.8f)
                                            .padding(top = 20.dp)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(color = White)
                                    ){
                                        Calendar(viewModel, records)
                                    }
                                    Box(modifier = Modifier.weight(1f).padding(top = 20.dp)) {
                                        NewRecord(onNavigate = { navController.navigate(DailyToDo.NewRecord.name) })
                                    }
                                }
                            }

                            //新しい記録
                            composable(route = DailyToDo.NewRecord.name)
                            {
                                Column()
                                {
                                    InputNewRecord(onSaveClick = { inputedtask1, inputedtask2, inputedtask3 ->
                                        val date = DateUtils.formatToKey(0)

                                        //データベースに保存
                                        viewModel.saveRecord(date, todo1 = inputedtask1, todo2 = inputedtask2, todo3 = inputedtask3)

                                        //保存したらホーム画面に戻る
                                        navController.popBackStack()
                                    }, viewModel, records, currentDate)
                                }
                            }

                            // カレンダー
                            composable(route = DailyToDo.Calendar.name)
                            {
                                //カレンダーを実装しよう
                                OneMonth()
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class DailyToDo() {
    Home,
    NewRecord,
    Calendar
}

@Composable
fun NewRecord(onNavigate: () -> Unit) {
    Button(
        onClick = { onNavigate() },
        colors = ButtonDefaults.buttonColors(containerColor = DarkOrange, contentColor = Color.White)
    ) {
        Text("目標を設定", fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@SuppressLint("NewApi")
@Composable
fun Calendar(viewModel: MainViewModel, records:List<DailyRecord>) {
    val currentTime = LocalDateTime.now()
    val recordsMapList = viewModel.convertToMap(records)

    Column(
        modifier = Modifier
            .padding(20.dp)
            .size(width = 400.dp, height = 350.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "記録", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 5.dp).fillMaxWidth(), textAlign = TextAlign.Start)
        for (i in 0..10) {
            val nextTime = currentTime.minusDays(i.toLong())
            val nextDate = DateUtils.formatToKey(i)

            val oneDayRecord1: TaskInfo = recordsMapList[0][nextDate]?: TaskInfo()
            val oneDayRecord2: TaskInfo = recordsMapList[1][nextDate]?: TaskInfo()
            val oneDayRecord3: TaskInfo = recordsMapList[2][nextDate]?: TaskInfo()

            DayCalendar(nextTime, oneDayRecord1, oneDayRecord2, oneDayRecord3)
            HorizontalDivider(thickness = 1.dp, color = Color(0xFF000000))
        }
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
    val dayOfWeek = nextTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.JAPANESE)
    val day = nextTime.dayOfMonth
    val fontSize = 18

    Row(
        modifier = Modifier.fillMaxWidth()
    ){
        Text(text = day.toString() + "日" + "(" + dayOfWeek + ")", modifier = Modifier.padding(2.dp).width(110.dp), fontSize = fontSize.sp)
        Column(
            modifier = Modifier.size(width = 200.dp, height = 90.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(modifier = Modifier.padding(0.5.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                SingleToDo(record1)
            }
            Row(modifier = Modifier.padding(0.5.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                SingleToDo(record2)
            }
            Row(modifier = Modifier.padding(0.5.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                SingleToDo(record3)
            }
        }
    }
}

@Composable
fun SingleToDo(record: TaskInfo){
    val taskName = record.taskName.take(5)
//    val chunkedTaskNames = taskName.take(5)
    val isDone = record.isDone
    val count = record.count
    val checkInfo: String = if (isDone == true){
        "☑"
    } else {
        "☐"
    }

    val fontSize = 18

    Row(
        horizontalArrangement = Arrangement.End
    ){
        Text(text = checkInfo, modifier = Modifier.width(30.dp)
            , textAlign = TextAlign.Center, fontSize = fontSize.sp)
        Text(text = taskName, modifier = Modifier.width(90.dp)
            , textAlign = TextAlign.Start , fontSize = fontSize.sp)
        Text(text = count.toString() + "回", modifier = Modifier.padding(start = 10.dp).width(60.dp)
            , textAlign = TextAlign.End, fontSize = fontSize.sp)
    }
}

@Composable
fun InputNewRecord(onSaveClick: (TaskInfo, TaskInfo, TaskInfo) -> Unit, viewModel: MainViewModel, records:List<DailyRecord>, date: String) {

    val recordsMapList = viewModel.convertToMap(records)

    val taskInfo1 = recordsMapList[0][date] ?: TaskInfo()
    val taskInfo2 = recordsMapList[1][date] ?: TaskInfo()
    val taskInfo3 = recordsMapList[2][date] ?: TaskInfo()

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

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Row(horizontalArrangement = Arrangement.Center){
            TextField(
                value = text1,
                onValueChange = { text1 = it },
                label = { Text("タスク") },
                modifier = Modifier
                    .padding(8.dp)
                    .width(200.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF000000),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = DarkOrange,
                    unfocusedIndicatorColor = DarkOrange
                )
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
                modifier = Modifier
                    .padding(8.dp)
                    .width(100.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF000000),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = DarkOrange,
                    unfocusedIndicatorColor = DarkOrange
                )
            )
        }

        Row(horizontalArrangement = Arrangement.Center){
            TextField(
                value = text2,
                onValueChange = { text2 = it },
                label = { Text("タスク") },
                modifier = Modifier
                    .padding(8.dp)
                    .width(200.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF000000),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = DarkOrange,
                    unfocusedIndicatorColor = DarkOrange
                )
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
                modifier = Modifier
                    .padding(8.dp)
                    .width(100.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF000000),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = DarkOrange,
                    unfocusedIndicatorColor = DarkOrange
                )
            )
        }

        Row(horizontalArrangement = Arrangement.Center){
            TextField(
                value = text3,
                onValueChange = { text3 = it },
                label = { Text("タスク") },
                modifier = Modifier
                    .padding(8.dp)
                    .width(200.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF000000),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = DarkOrange,
                    unfocusedIndicatorColor = DarkOrange
                )
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
                modifier = Modifier
                    .padding(8.dp)
                    .width(100.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF000000),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = DarkOrange,
                    unfocusedIndicatorColor = DarkOrange
                )
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
            },
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkOrange, contentColor = Color.White)
        ) {
            Text("保存", fontSize = 20.sp)
        }
    }
}

@Composable
fun CheckToDo(date: String, viewModel: MainViewModel, records:List<DailyRecord>){
    val recordsMapList = viewModel.convertToMap(records)

    Column ()
    {
        for (i in 0..2) {
            val taskInfo = recordsMapList[i][date] ?: TaskInfo()

            Row(modifier = Modifier.padding(start = 40.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ){
                DisplayCheckBox(date,viewModel, taskInfo, i)
                Text(text = taskInfo.taskName.take(7), fontSize = 20.sp, modifier = Modifier.weight(1f))
                Text(text = taskInfo.count.toString() + " 回", modifier = Modifier.padding(end = 40.dp), fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun DisplayCheckBox(date: String, viewModel: MainViewModel, taskInfo: TaskInfo, taskIndex: Int){
    Checkbox(
        checked = taskInfo.isDone,
        onCheckedChange = { isChecked ->
            viewModel.checkToDo(date, isChecked, taskIndex)
        },
        colors = CheckboxDefaults.colors(
            checkedColor = DarkOrange,
            checkmarkColor = White
        )
    )
}

@SuppressLint("NewApi")
@Composable
fun DisplayTodayDate(fontSize : Int, color: androidx.compose.ui.graphics.Color) {
    val currentTime = LocalDateTime.now()
    val currentMonth = currentTime.monthValue
    val currentDay = currentTime.dayOfMonth

    Text(text = "$currentMonth 月 $currentDay 日", fontSize = fontSize.sp, color = color, fontWeight = FontWeight.Bold)
}

@Composable
fun DisplayIconAndText(text : String, icon : Int, isClicked : Boolean){
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        // Image(painter = painterResource(icon), contentDescription = text)
        var color = Color(0xFF000000)
        if (isClicked == true) {
            color = DarkOrange
        }
        Icon(painter = painterResource(icon), contentDescription = text, tint = color)
        Text(text = text, textAlign = TextAlign.Center, fontSize = 8.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

