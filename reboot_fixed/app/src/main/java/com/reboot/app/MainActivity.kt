package com.reboot.app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val Bg=Color(0xFF0B0D1A)
private val Card=Color(0xFF171A2D)
private val Purple=Color(0xFF8B5CF6)
private val Cyan=Color(0xFF22D3EE)
private val Pink=Color(0xFFF472B6)
private val Text=Color(0xFFF8FAFC)
private val Muted=Color(0xFFA5B4CC)

data class UserState(
    val name:String="Новый пользователь", val level:Int=1, val xp:Int=0,
    val streak:Int=0, val habits:List<String> = emptyList(),
    val problems:List<String> = emptyList(), val goals:List<String> = emptyList()
)

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{RebootApp()}}
}

@Composable
fun RebootApp(){
    var page by remember { mutableStateOf("home") }
    var user by remember { mutableStateOf(UserState()) }
    var logged by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme=darkColorScheme(background=Bg,surface=Card,primary=Purple,onBackground=Text,onSurface=Text)){
        if(!logged) AuthScreen({ logged=true }, user.name){ n->user=user.copy(name=n) }
        else Scaffold(
            containerColor=Bg,
            bottomBar={ NavigationBar(containerColor=Color(0xFF101225)){
                listOf("home" to "Дом","plans" to "Планы","ai" to "ИИ","progress" to "Прогресс","profile" to "Я").forEach{(id,label)->
                    NavigationBarItem(selected=page==id,onClick={page=id},icon={Text(label.take(1),fontWeight=FontWeight.Bold)},label={Text(label)})
                }
            }}
        ){pad->Box(Modifier.padding(pad)){when(page){
            "home"->Home(user){user=it}
            "plans"->Plans(user)
            "ai"->Ai()
            "progress"->Progress(user)
            "profile"->Profile(user){user=it}
        }}}
    }
}

@Composable
fun AuthScreen(onLogin:()->Unit,name:String,onName:(String)->Unit){
    var register by remember{mutableStateOf(true)}
    var n by remember{mutableStateOf(name)}
    var email by remember{mutableStateOf("")}
    var pass by remember{mutableStateOf("")}
    var step by remember{mutableStateOf(0)}
    val habits=listOf("Соцсети","Игры","Прокрастинация","Нерегулярный сон","Сладкое","Отсутствие движения","Другое")
    val problems=listOf("Тревожность","Неуверенность","Стресс","Прокрастинация","Страх отказа","Сложности с режимом")
    val selectedH=remember{mutableStateListOf<String>()}
    val selectedP=remember{mutableStateListOf<String>()}

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF24123D),Bg))).padding(22.dp)){
        if(step==0){
            Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
                Spacer(Modifier.height(40.dp));Text("REBOOT",fontSize=42.sp,fontWeight=FontWeight.Black,color=Text)
                Text("Создай новую версию себя.",color=Muted)
                if(register){
                    Field("Имя",n){n=it}
                    Field("Email",email){email=it}
                } else Field("Email",email){email=it}
                Field("Пароль",pass){pass=it}
                Button(onClick={if(register)step=1 else onLogin()},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(Purple)){
                    Text(if(register)"Создать аккаунт" else "Войти")
                }
                Text(if(register)"Уже есть аккаунт? Войти" else "Нет аккаунта? Регистрация",
                    Modifier.clickable{register=!register}.padding(8.dp),color=Cyan)
                Text("В production эта форма отправляется на /api/v1/auth; локальный экран нужен для сборки и UX.",fontSize=11.sp,color=Muted)
            }
        } else {
            LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){
                item{Text("Настроим REBOOT",fontSize=30.sp,fontWeight=FontWeight.Bold);Text("Выбери то, над чем хочешь работать.",color=Muted)}
                item{Text("Привычки",fontSize=20.sp,fontWeight=FontWeight.Bold)}
                items(habits){x->Choice(x,selectedH.contains(x)){if(selectedH.contains(x))selectedH.remove(x) else selectedH.add(x)}}
                item{Text("Проблемы",fontSize=20.sp,fontWeight=FontWeight.Bold)}
                items(problems){x->Choice(x,selectedP.contains(x)){if(selectedP.contains(x))selectedP.remove(x) else selectedP.add(x)}}
                item{Button(onClick={onName(n);onLogin()},modifier=Modifier.fillMaxWidth()){Text("Запустить мой REBOOT")}}
            }
        }
    }
}

@Composable fun Field(label:String,value:String,onValue:(String)->Unit)=OutlinedTextField(value,onValue,label={Text(label)},modifier=Modifier.fillMaxWidth())
@Composable fun Choice(text:String,selected:Boolean,onClick:()->Unit){
    Row(Modifier.fillMaxWidth().background(if(selected)Color(0xFF30244F) else Card,RoundedCornerShape(16.dp)).clickable{onClick()}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
        Checkbox(selected,onClick);Text(text,color=Text)
    }
}

@Composable fun Header(title:String,sub:String){Column(Modifier.padding(18.dp)){Text(title,fontSize=30.sp,fontWeight=FontWeight.Black);Text(sub,color=Muted)}}
@Composable fun Card(title:String,body:String,button:String?=null,onClick:(()->Unit)?=null){
    Column(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=6.dp).background(Card,RoundedCornerShape(22.dp)).padding(18.dp)){
        Text(title,fontSize=19.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(6.dp));Text(body,color=Muted)
        if(button!=null){Spacer(Modifier.height(10.dp));Button(onClick={onClick?.invoke()},colors=ButtonDefaults.buttonColors(Purple)){Text(button)}}
    }
}
@Composable fun Home(user:UserState,onUser:(UserState)->Unit){
    LazyColumn{item{Header("Привет, ${user.name}","Твой REBOOT сегодня • уровень ${user.level}")}}
    // Replaced below by a compact column for stability
    Column(Modifier.fillMaxSize().background(Bg)){
        Spacer(Modifier.height(110.dp))
        Card("Уровень ${user.level}","${user.xp}/100 XP • Серия ${user.streak} дней")
        Card("Задача дня","Одно маленькое действие на 1–5 минут. Выполни его, чтобы получить XP.","Выполнено"){
            val nx=user.xp+50;onUser(user.copy(xp=nx%100,level=user.level+if(nx>=100)1 else 0,streak=user.streak+1))
        }
        Card("Твои направления","Тренировки • саморазвитие • карьера • психология • здоровье")
        Card("Стоп. Сейчас","Открой спокойный экран заземления и выбери безопасный следующий шаг.","Открыть")
    }
}
@Composable fun Plans(user:UserState){
    LazyColumn{item{Header("Планы","Персональные квесты")}
        items(listOf(
            "Тренировки" to "Уровень и безопасная нагрузка.",
            "Саморазвитие" to "Навыки и обучение.",
            "Карьера" to "Цели → этапы → задачи.",
            "Психология" to "Саморегуляция и уверенность.",
            "Здоровье" to "Сон, движение, режим.",
            "Свободная цель" to "Свой квест."
        )){(a,b)->Card(a,b,"Сгенерировать AI"){}}
    }
}
@Composable fun Ai(){
    val context=LocalContext.current
    var text by remember{mutableStateOf("")}
    var mode by remember{mutableStateOf("Друг")}
    var answer by remember{mutableStateOf("AI готов. Подключи backend Groq и отправь первое сообщение.")}

    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){res->
        val r=res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if(r!=null)text=r
    }
    LazyColumn{item{Header("ИИ","Персональный голосовой наставник")}
        item{Row(Modifier.padding(18.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Друг","Тренер","Провокатор","Мудрец","Турбо").forEach{m->FilterChip(selected=mode==m,onClick={mode=m},label={Text(m)})}}}
        item{Card("Режим $mode","Ответы адаптируются к выбранному стилю, но не должны унижать или предлагать опасные действия.")}}
        item{Card("Диалог",answer)}
        item{OutlinedTextField(text,{text=it},label={Text("Сообщение")},modifier=Modifier.fillMaxWidth().padding(18.dp))}
        item{Row(Modifier.padding(horizontal=18.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            Button(onClick={if(SpeechRecognizer.isRecognitionAvailable(context)){launcher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault().toLanguageTag());putExtra(RecognizerIntent.EXTRA_PROMPT,"Говори…")})}}){Text("Голос")}
            Button(onClick={answer="Запрос готов для /api/v1/ai/chat • режим: $mode";}){Text("Отправить")}
        }}
    }
}
@Composable fun Progress(user:UserState){
    LazyColumn{item{Header("Прогресс","Твоя статистика")}
        item{Card("XP","${user.xp}/100 • уровень ${user.level}")}
        item{Card("Серия","${user.streak} дней")}
        item{Card("Ачивки","Первый квест • Первый уровень • 7 дней • 10 дней • 100 задач")}
        item{Card("Аналитика","Тепловая карта, выполнение планов, привычки и история AI подключаются к backend.")}}
}
@Composable fun Profile(user:UserState,onUser:(UserState)->Unit){
    LazyColumn{item{Header("Я","Профиль")}
        item{Card("Аккаунт",user.name)}
        item{Card("Привычки","Выбранные направления: ${user.habits.joinToString().ifBlank{"пока не синхронизированы"}}")}
        item{Card("AI","Язык • голос • скорость • интенсивность")}
        item{Card("Подписка","Free / Premium • история платежей через backend")}
        item{Card("Реферал","Пригласи друга — бонус выдаётся сервером после проверки.")}}
}
