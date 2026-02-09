package com.example.trareco


//@SuppressLint("NewApi")
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.light(
//                android.graphics.Color.TRANSPARENT, // 背景を透明に
//                android.graphics.Color.TRANSPARENT  // ダークモード時の色
//            )
//        )
//
//        super.onCreate(savedInstanceState)
//
//        // データベースのインスタンス作成
//        val db = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java, "daily_database"
//        ).fallbackToDestructiveMigration().build()
//        val dao = db.userDao()
//        val viewModel = MainViewModel(dao)
//
//        setContent {
//            val navController = androidx.navigation.compose.rememberNavController()
//
//            val records by viewModel.allRecords.collectAsState(initial = emptyList())
//
//            // Map形式に変換しておくとカレンダーで使いやすい
//            val recordsMap1 = records.associate { it.date to it.todo1 }
//            val recordsMap2 = records.associate { it.date to it.todo2 }
//            val recordsMap3 = records.associate { it.date to it.todo3 }
//
//            val currentDate = DateUtils.formatToKey(0)
//
//            Surface(
//                modifier = Modifier.fillMaxSize(),
//                //color = MaterialTheme.colorScheme.background
//                color = Gainsboro// <----動的にしたい
//            ){
//                Column(modifier = Modifier
//                    .fillMaxSize()
//                    .windowInsetsPadding(WindowInsets.safeDrawing)
//                )
//                {
//                    NavHost(navController = navController,
//                        startDestination = DailyToDo.Home.name,
//                        modifier = Modifier.padding())
//                    {
//                        //ホーム画面
//                        composable(route = DailyToDo.Home.name)
//                        {
//                            if (records.isNotEmpty()){
//                                TakeOverToDo(currentDate, viewModel, recordsMap1, recordsMap2, recordsMap3)
//                            }
//
//                            Column(
//                                modifier = Modifier,
//                                horizontalAlignment = Alignment.CenterHorizontally)
//                            {
//                                DisplayTodayDate(fontSize = 35)
//                                //Box(modifier = Modifier.size(width = 400.dp, height = 100.dp).background(color = Color(0xFFFF0000))){}
//                                CheckToDo(currentDate, viewModel, records, recordsMap1,recordsMap2, recordsMap3)
//                                FiveDaysCalendar(recordsMap1 = recordsMap1, recordsMap2 = recordsMap2, recordsMap3 = recordsMap3)
//                                NewRecord(onNavigate = {
//                                    navController.navigate(DailyToDo.NewRecord.name)
//                                })
//                            }
//                        }
//                        //新しい記録
//                        composable(route = DailyToDo.NewRecord.name)
//                        {
//                            Column(
//                                modifier = Modifier,
//                                horizontalAlignment = Alignment.CenterHorizontally)
//                            {
//                                //DisplayTodayDate()
//                                InputNewRecord(onSaveClick = { inputedtask1, inputedtask2, inputedtask3 ->
//                                    val date = DateUtils.formatToKey(0)
//
//                                    //データベースに保存
//                                    viewModel.saveRecord(date, todo1 = inputedtask1, todo2 = inputedtask2, todo3 = inputedtask3)
//
//                                    //保存したらホーム画面に戻る
//                                    navController.popBackStack()
//                                }, recordsMap1[currentDate] ?: TaskInfo(), recordsMap2[currentDate] ?: TaskInfo(), recordsMap3[currentDate] ?: TaskInfo())
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

//enum class DailyToDo() {
//    Home,
//    NewRecord
//}

//data class TaskInfo(
//    val isDone: Boolean = false,
//    val taskName: String = "",
//    val count: Int = 0
//)
//
////データエンティティ
//@Entity(tableName = "daily_records")
//data class DailyRecord(
//    @PrimaryKey val date: String,
//    @Embedded(prefix = "todo1_") val todo1: TaskInfo,
//    @Embedded(prefix = "todo2_") val todo2: TaskInfo,
//    @Embedded(prefix = "todo3_") val todo3: TaskInfo,
//){
//    fun toList() = listOf(todo1, todo2, todo3)
//}
//
////データアクセスオブジェクト（DAO）
//@Dao
//interface DailyRecordDao {
//    //データの取得
//    @Query("SELECT * FROM daily_records")
//    fun loadAllUsers(): Flow<List<DailyRecord>>
//
//    //挿入
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun addNewRecord(dailyRecord: DailyRecord)
//
//    //削除
//    @Delete
//    suspend fun deleteRecord(dailyRecord: DailyRecord)
//
//    // 【修正】特定の日付のデータを取得するクエリを追加（チェック更新用）
//    @Query("SELECT * FROM daily_records WHERE date = :date")
//    suspend fun getRecordByDate(date: String): DailyRecord?
//
//    //最新の記録を1件だけ取得する
//    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT 1")
//    suspend fun getLastRecord(): DailyRecord?
//}
//
////データベースオブジェクト
//@Database(entities = [DailyRecord::class], version = 1)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun userDao(): DailyRecordDao
//}

//class MainViewModel(private val dao: DailyRecordDao) : ViewModel() {
//
//    // 全データを取得するFlow（自動更新対応）
//    val allRecords: Flow<List<DailyRecord>> = dao.loadAllUsers()
//
//    //データを保存する関数
//    fun saveRecord(date: String, todo1: TaskInfo, todo2: TaskInfo, todo3: TaskInfo) {
//        viewModelScope.launch {
//            val newRecord = DailyRecord(date = date, todo1 = todo1, todo2 = todo2, todo3 = todo3)
//            dao.addNewRecord(newRecord)
//        }
//    }
//
//    // 最新の1件を取得する関数（suspend）
//    suspend fun getLastRecord(): DailyRecord? {
//        return dao.getLastRecord()
//    }
//
//    // todoのチェックを更新する
//    fun checkToDo(date: String, isDone: Boolean, taskIndex: Int){
//        viewModelScope.launch {
//            val record = dao.getRecordByDate(date) ?: return@launch
//            val tasks = record.toList().toMutableList()
//
//            tasks[taskIndex] = tasks[taskIndex].copy(isDone = isDone)
//            saveRecord(date, tasks[0], tasks[1], tasks[2])
//        }
//    }
//}

////日時の情報を取得
//object DateUtils {
//    @SuppressLint("NewApi")
//    fun formatToKey(num: Int): String {
//        val date = LocalDateTime.now().minusDays(num.toLong())
//
//        return "${date.year}_${date.monthValue}_${date.dayOfMonth}"
//    }
//}

//@Composable
//fun NewRecord(onNavigate: () -> Unit) {
//    Button(onClick = {
//            onNavigate()
//        }, modifier = Modifier.padding(10.dp)) {
//        Text("目標を設定")
//    }
//}
//
//// FiveDaysCalendar 本体の修正
//@SuppressLint("NewApi")
//@Composable
//fun FiveDaysCalendar(recordsMap1: Map<String, TaskInfo>, recordsMap2: Map<String, TaskInfo>, recordsMap3: Map<String, TaskInfo>) { // 引数を追加
//    val currentTime = LocalDateTime.now()
//
//    Column(
//        modifier = Modifier.padding(20.dp).size(width = 400.dp, height = 350.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ){
//        for (i in 1..4) {
//            val nextTime = currentTime.minusDays(i.toLong())
//            val nextDate = DateUtils.formatToKey(i)
//
//            val oneDayRecord1: TaskInfo = recordsMap1[nextDate]?: TaskInfo()
//            val oneDayRecord2: TaskInfo = recordsMap2[nextDate]?: TaskInfo()
//            val oneDayRecord3: TaskInfo = recordsMap3[nextDate]?: TaskInfo()
//
//            DayCalendar(nextTime, oneDayRecord1, oneDayRecord2, oneDayRecord3)
//        }
//    }
//}
//
//@SuppressLint("NewApi")
//@Composable
//fun DayCalendar(
//    nextTime: LocalDateTime,
//    record1: TaskInfo,
//    record2: TaskInfo,
//    record3: TaskInfo,
//    )
//{
//    val dayOfWeek = nextTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.JAPANESE)
//    val day = nextTime.dayOfMonth
//    val fontSize = 18
//
//    Row(){
//        Text(text = day.toString() + "日" + "(" + dayOfWeek + ")", modifier = Modifier.padding(2.dp), fontSize = fontSize.sp)
//        Column(
//            modifier = Modifier.size(width = 100.dp, height = 90.dp),
//            horizontalAlignment = Alignment.End
//        ) {
//            Row(modifier = Modifier.padding(0.5.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ){
//                SingleToDo(record1)
//            }
//            Row(modifier = Modifier.padding(0.5.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ){
//                SingleToDo(record2)
//            }
//            Row(modifier = Modifier.padding(0.5.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ){
//                SingleToDo(record3)
//            }
//        }
//    }
//}
//
//@Composable
//fun SingleToDo(record: TaskInfo){
//    val taskName = record.taskName
//    val isDone = record.isDone
//    val count = record.count
//    val checkInfo: String = if (isDone == true){
//        "☑"
//    } else {
//        "☐"
//    }
//
//    val fontSize = 18
//
//    Row(modifier = Modifier.padding(0.5.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ){
//        Text(text = checkInfo, modifier = Modifier.weight(0.1f), fontSize = fontSize.sp)
//        Text(text = taskName + "  " + count + " 回", fontSize = fontSize.sp)
//    }
//}
//
//@Composable
//fun InputNewRecord(onSaveClick: (TaskInfo, TaskInfo, TaskInfo) -> Unit, taskInfo1: TaskInfo, taskInfo2: TaskInfo, taskInfo3: TaskInfo) {
//    // 一つ目のタスク
//    var text1 by remember { mutableStateOf(taskInfo1.taskName) }
//    var count1 by remember { mutableStateOf(taskInfo1.count.toString()) }
//    var countInt1 = 0
//
//    val isDone1: Boolean = taskInfo1.isDone
//    var oneDayRecord1 = TaskInfo()
//
//    // 二つ目のタスク
//    var text2 by remember { mutableStateOf(taskInfo2.taskName) }
//    var count2 by remember { mutableStateOf(taskInfo2.count.toString()) }
//    var countInt2 = 0
//
//    val isDone2: Boolean = taskInfo2.isDone
//    var oneDayRecord2 = TaskInfo()
//
//    // 三つ目のタスク
//    var text3 by remember { mutableStateOf(taskInfo3.taskName) }
//    var count3 by remember { mutableStateOf(taskInfo3.count.toString()) }
//    var countInt3 = 0
//
//    val isDone3: Boolean = taskInfo3.isDone
//    var oneDayRecord3 = TaskInfo()
//
//    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//
//        Row(horizontalArrangement = Arrangement.Center){
//            TextField(
//                value = text1,
//                onValueChange = { text1 = it },
//                label = { Text("タスク") },
//                modifier = Modifier.padding(8.dp).width(200.dp)
//            )
//            TextField(
//                value = count1,
//                onValueChange = { inputedValue: String ->
//                    if (inputedValue.all { it.isDigit() } || inputedValue.isEmpty()) {
//                        count1 = inputedValue
//                    }
//                },
//                label = { Text("回数/時間") },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                modifier = Modifier.padding(8.dp).width(100.dp)
//            )
//        }
//
//        Row(horizontalArrangement = Arrangement.Center){
//            TextField(
//                value = text2,
//                onValueChange = { text2 = it },
//                label = { Text("タスク") },
//                modifier = Modifier.padding(8.dp).width(200.dp)
//            )
//            TextField(
//                value = count2,
//                onValueChange = { inputedValue: String ->
//                    if (inputedValue.all { it.isDigit() } || inputedValue.isEmpty()) {
//                        count2 = inputedValue
//                    }
//                },
//                label = { Text("回数/時間") },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                modifier = Modifier.padding(8.dp).width(100.dp)
//            )
//        }
//
//        Row(horizontalArrangement = Arrangement.Center){
//            TextField(
//                value = text3,
//                onValueChange = { text3 = it },
//                label = { Text("タスク") },
//                modifier = Modifier.padding(8.dp).width(200.dp)
//            )
//            TextField(
//                value = count3,
//                onValueChange = { inputedValue: String ->
//                    if (inputedValue.all { it.isDigit() } || inputedValue.isEmpty()) {
//                        count3 = inputedValue
//                    }
//                },
//                label = { Text("回数/時間") },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                modifier = Modifier.padding(8.dp).width(100.dp)
//            )
//        }
//
//        if (count1 == "") {
//            countInt1 = 0
//        } else {
//            countInt1 = count1.toInt()
//        }
//
//        if (count2 == "") {
//            countInt2 = 0
//        } else {
//            countInt2 = count2.toInt()
//        }
//
//        if (count3 == "") {
//            countInt3 = 0
//        } else {
//            countInt3 = count3.toInt()
//        }
//
//        oneDayRecord1 = TaskInfo(isDone1, text1, countInt1)
//        oneDayRecord2 = TaskInfo(isDone2, text2, countInt2)
//        oneDayRecord3 = TaskInfo(isDone3, text3, countInt3)
//
//
//        // 保存ボタン
//        Button(
//            onClick = {
//                if (text1.isNotBlank()){
//                    onSaveClick(oneDayRecord1, oneDayRecord2, oneDayRecord3)
//                }
//            }
//        ) {
//            Text("保存")
//        }
//    }
//}
//
//@Composable
//fun CheckToDo(date: String, viewModel: MainViewModel, records:List<DailyRecord>, recordsMap1: Map<String, TaskInfo>, recordsMap2: Map<String, TaskInfo>, recordsMap3: Map<String, TaskInfo>){
//
//    val taskInfo1 = recordsMap1[date] ?: TaskInfo()
//    val taskInfo2 = recordsMap2[date] ?: TaskInfo()
//    val taskInfo3 = recordsMap3[date] ?: TaskInfo()
//
//    Column (
//        modifier = Modifier.size(width = 380.dp, height = 200.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    )
//    {
//        Row(modifier = Modifier.padding(8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ){
//            DisplayCheckBox(date,viewModel, taskInfo1, 0)
//            Text(text = taskInfo1.taskName + "  " + taskInfo1.count + " 回", fontSize = 20.sp)
//        }
//        Row(modifier = Modifier.padding(8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ){
//            DisplayCheckBox(date,viewModel, taskInfo2, 1)
//            Text(text = taskInfo2.taskName + "  " + taskInfo1.count + " 回", fontSize = 20.sp)
//        }
//        Row(modifier = Modifier.padding(8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ){
//            DisplayCheckBox(date,viewModel, taskInfo3, 2)
//            Text(text = taskInfo3.taskName + "  " + taskInfo1.count + " 回", fontSize = 20.sp)
//        }
//    }
//}
//
//@Composable
//fun DisplayCheckBox(date: String, viewModel: MainViewModel, taskInfo: TaskInfo, taskIndex: Int){
//    Checkbox(
//        checked = taskInfo.isDone,
//        onCheckedChange = { isChecked ->
//            viewModel.checkToDo(date, isChecked, taskIndex)
//        }
//    )
//}
//
//@Composable
//@SuppressLint("NewApi")
//fun TakeOverToDo(date: String, viewModel: MainViewModel, recordsMap1: Map<String, TaskInfo>, recordsMap2: Map<String, TaskInfo>, recordsMap3: Map<String, TaskInfo>){
//
//    val taskInfo1 = recordsMap1[date] ?: TaskInfo()
//    val taskInfo2 = recordsMap2[date] ?: TaskInfo()
//    val taskInfo3 = recordsMap3[date] ?: TaskInfo()
//
//    LaunchedEffect(date) {
//        val lastRecord = viewModel.getLastRecord()
//
//        if ((taskInfo1.taskName == "") && (taskInfo2.taskName == "") && (taskInfo3.taskName == "")) {
//            if (lastRecord != null) {
//                val newTaskInfo1 = lastRecord.todo1.copy(isDone = false)
//                val newTaskInfo2 = lastRecord.todo2.copy(isDone = false)
//                val newTaskInfo3 = lastRecord.todo3.copy(isDone = false)
//
//                viewModel.saveRecord(date, todo1 = newTaskInfo1, todo2 = newTaskInfo2, todo3 = newTaskInfo3)
//            }
//        }
//    }
//}
//
//@SuppressLint("NewApi")
//@Composable
//fun DisplayTodayDate(fontSize : Int) {
//    val currentTime = LocalDateTime.now()
//    val currentMonth = currentTime.monthValue
//    val currentDay = currentTime.dayOfMonth
//
//    Text(text = "$currentMonth 月 $currentDay 日", fontSize = fontSize.sp, modifier = Modifier.padding(40.dp))
//}
