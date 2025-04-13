package com.example.strack

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.strack.ui.theme.LocalExtraColors
import com.example.strack.ui.theme.StrackTheme
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalAdjusters
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

fun encryptAES(data: ByteArray): ByteArray {
    val key = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    return cipher.doFinal(data)
}

fun decryptAES(data: ByteArray): ByteArray {
    val key = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, key)
    return cipher.doFinal(data)
}

// Lang.get(0, context)
object Lang {
    private val keys: Map<Int, Int> by lazy {
        val fields = R.string::class.java.fields
        fields
            .filter { it.name.startsWith("lang_") }
            .associate { it.name.removePrefix("lang_").toInt() to it.getInt(null) }
    }

    fun get(key: Int, context: Context): String {
        return keys[key]?.let { context.getString(it) } ?: "Empty"
    }
}

object OperationKeys {
    const val GAIN = "gain"
    const val KM = "km"
    const val GASOLINE = "gasoline"
    const val OIL = "oil"
    const val MAINTENANCE = "maintenance"
    const val CALIBRATE_TIRES = "calibrate_tires"
    const val TIGHTEN_CHAIN = "tighten_chain"
    const val CLEAN_CHAIN = "clean_chain"
    const val CHECK_LIGHTS = "check_lights"
    const val CHECK_RADIATOR = "check_radiator"
    const val CLEAN_FILTER = "clean_filter"
}

fun translateType(context: Context, type: String): String {
    return when (type) {
        "gain" -> Lang.get(10, context)
        "km" -> Lang.get(11, context)
        "gasoline" -> Lang.get(12, context)
        "oil" -> Lang.get(13, context)
        "maintenance" -> Lang.get(14, context)
        "calibrate_tires" -> Lang.get(15, context)
        "tighten_chain" -> Lang.get(16, context)
        "clean_chain" -> Lang.get(17, context)
        "check_lights" -> Lang.get(18, context)
        "check_radiator" -> Lang.get(19, context)
        "clean_filter" -> Lang.get(20, context)
        else -> type
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this) // Inicializa a biblioteca ThreeTenABP

        WindowCompat.setDecorFitsSystemWindows(window, false) // Permite desenhar sob a status bar
        setStatusBarAppearance()

        setContent {
            StrackTheme {
                AppNavigation()
            }
        }

        readOrCreateFile() // Carregar ou criar o arquivo XML
    }

    private fun setStatusBarAppearance() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false // Ícones brancos na status bar
    }

    private fun readOrCreateFile() {
        val strackDir = File(filesDir, "strack").apply {
            if (!exists()) mkdir()
        }

        val filePath = File(strackDir, "strack.xml")

        if (!filePath.exists()) {
            Log.w("Strack", "strack.xml não existe! Criando novo arquivo.")
            try {
                filePath.writeText(
                    "<strack><settings><vehType>vehType1</vehType>" +
                            "<reminder kmGasoline=\"200\" kmOil=\"900\" kmCalibrateTires=\"200\" " +
                            "kmTightenChain=\"900\" kmCleanChain=\"900\" kmCheckLights=\"900\" kmCheckRadiator=\"900\" kmCleanFilter=\"900\"/></settings>" +
                            "<operations></operations></strack>"
                )
                Log.d("Strack", "strack.xml foi criado")
            } catch (e: Exception) {
                Log.e("Strack", "Erro ao criar o arquivo: ${e.message}")
            }
        }
    }
}

fun saveStrackToXML(settings: Map<String, String>, operations: List<Triple<String, String, String>>, context: Context) {
    val strackDir = File(context.filesDir, "strack")
    if (!strackDir.exists()) strackDir.mkdir()

    val byteStream = ByteArrayOutputStream()
    val xml = Xml.newSerializer()
    xml.setOutput(byteStream, "UTF-8")
    xml.startDocument("UTF-8", true)
    xml.startTag("", "strack")

    xml.startTag("", "settings")
    xml.startTag("", "userName")
    xml.text(settings["userName"] ?: Lang.get(7, context))
    xml.endTag("", "userName")
    xml.startTag("", "vehType")
    xml.text(settings["vehType"] ?: "vehType1")
    xml.endTag("", "vehType")

    xml.startTag("", "reminder")
    xml.attribute("", "kmGasoline", settings["kmGasoline"] ?: "200")
    xml.attribute("", "kmOil", settings["kmOil"] ?: "900")
    xml.attribute("", "kmCalibrateTires", settings["kmCalibrateTires"] ?: "200")
    xml.attribute("", "kmTightenChain", settings["kmTightenChain"] ?: "900")
    xml.attribute("", "kmCleanChain", settings["kmCleanChain"] ?: "900")
    xml.attribute("", "kmCheckLights", settings["kmCheckLights"] ?: "900")
    xml.attribute("", "kmCheckRadiator", settings["kmCheckRadiator"] ?: "900")
    xml.attribute("", "kmCleanFilter", settings["kmCleanFilter"] ?: "900")
    xml.endTag("", "reminder")
    xml.endTag("", "settings")

    xml.startTag("", "operations")
    for ((type, value, date) in operations) {
        xml.startTag("", "operation")
        xml.attribute("", "type", type)
        xml.attribute("", "value", value)
        xml.attribute("", "date", date)
        xml.endTag("", "operation")
    }
    xml.endTag("", "operations")

    xml.endTag("", "strack")
    xml.endDocument()
    xml.flush()

    val encryptedData = encryptAES(byteStream.toByteArray())
    File(strackDir, "strack.xml").writeBytes(encryptedData)
}

fun loadStrackFromXML(context: Context): Pair<Map<String, String>, List<Triple<String, String, String>>> {
    val settings = mutableMapOf<String, String>()
    val operations = mutableListOf<Triple<String, String, String>>()

    try {
        val file = File(File(context.filesDir, "strack"), "strack.xml")
        val encryptedBytes = file.readBytes()
        val decryptedBytes = decryptAES(encryptedBytes)

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(ByteArrayInputStream(decryptedBytes), "UTF-8")

        var eventType = parser.eventType
        var currentTag = ""
        var type = ""
        var value = ""
        var date = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    if (currentTag == "userName") {
                        settings["userName"] = parser.nextText()
                    } else if (currentTag == "vehType") {
                        settings["vehType"] = parser.nextText()
                    } else if (currentTag == "reminder") {
                        settings["kmGasoline"] = parser.getAttributeValue(null, "kmGasoline")
                        settings["kmOil"] = parser.getAttributeValue(null, "kmOil")
                        settings["kmCalibrateTires"] = parser.getAttributeValue(null, "kmCalibrateTires")
                        settings["kmTightenChain"] = parser.getAttributeValue(null, "kmTightenChain")
                        settings["kmCleanChain"] = parser.getAttributeValue(null, "kmCleanChain")
                        settings["kmCheckLights"] = parser.getAttributeValue(null, "kmCheckLights")
                        settings["kmCheckRadiator"] = parser.getAttributeValue(null, "kmCheckRadiator")
                        settings["kmCleanFilter"] = parser.getAttributeValue(null, "kmCleanFilter")
                    } else if (currentTag == "operation") {
                        type = parser.getAttributeValue(null, "type")
                        value = parser.getAttributeValue(null, "value")
                        date = parser.getAttributeValue(null, "date")
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "operation") {
                        operations.add(Triple(type, value, date))
                    }
                }
            }
            eventType = parser.next()
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return Pair(settings, operations)
}

fun salvarBackup(context: Context, uri: Uri) {
    try {
        val inputFile = File(context.filesDir, "strack/strack.xml")
        if (!inputFile.exists()) {
            Toast.makeText(context, Lang.get(1, context), Toast.LENGTH_SHORT).show()
            return
        }

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(inputFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        Toast.makeText(context, Lang.get(2, context), Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, Lang.get(3, context), Toast.LENGTH_SHORT).show()
    }
}

fun importarBackup(context: Context, uri: Uri) {
    try {
        val strackDir = File(context.filesDir, "strack")
        if (!strackDir.exists()) {
            strackDir.mkdir()
        }

        val tempFile = File(strackDir, "strack_temp.xml")
        val outputFile = File(strackDir, "strack.xml")

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // Testa se o arquivo importado pode ser lido antes de sobrescrever
        val testLoad = loadStrackFromXML(context)
        if (testLoad.second.isNotEmpty() || testLoad.first.isNotEmpty()) {
            tempFile.renameTo(outputFile)
            Toast.makeText(context, Lang.get(4, context), Toast.LENGTH_SHORT).show()
        } else {
            tempFile.delete()
            Toast.makeText(context, Lang.get(5, context), Toast.LENGTH_SHORT).show()
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, Lang.get(6, context), Toast.LENGTH_SHORT).show()
    }
}

// Obtém e salva a última página acessada
fun getLastPage(context: Context) =
    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getString("last_page", "Home") ?: "Home"

fun saveLastPage(context: Context, page: String) =
    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit {
        putString("last_page", page)
    }

// Função para gerar o nome do arquivo com a data atual
fun getBackupFileName(context: Context): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = dateFormat.format(Date())
    return "${getAppName(context).lowercase(Locale.getDefault())} $date"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(getLastPage(context)) }
    var settings by remember { mutableStateOf(emptyMap<String, String>()) }
    var operations by remember { mutableStateOf(emptyList<Triple<String, String, String>>()) }

    LaunchedEffect(Unit) {
        val (loadedSettings, loadedOperations) = loadStrackFromXML(context)
        settings = loadedSettings
        operations = loadedOperations
    }

    // Função para atualizar as operações
    fun updateOperations(newOperations: List<Triple<String, String, String>>) {
        operations = newOperations
        saveStrackToXML(settings, operations, context)
    }

    // Função para atualizar as configurações
    fun updateSettings(newSettings: Map<String, String>) {
        settings = newSettings
        saveStrackToXML(settings, operations, context)
    }

    // Função para resetar as configurações para o estado inicial
    fun resetSettings() {
        // Defina os valores padrão para as configurações
        settings = mutableMapOf(
            "userName" to Lang.get(7, context),
            "vehType" to "vehType1",
            // Defina outras configurações padrão aqui
            "kmGasoline" to "200",
            "kmOil" to "900",
            "kmCalibrateTires" to "200",
            "kmTightenChain" to "900",
            "kmCleanChain" to "900",
            "kmCheckLights" to "900",
            "kmCheckRadiator" to "900",
            "kmCleanFilter" to "900"
        )
        operations = emptyList()
        saveStrackToXML(settings, operations, context)
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val backPressedCallback = rememberUpdatedState {
        currentPage = when (currentPage) {
            "Home" -> { (context as Activity).finish(); "Home" }
            "WorkingPage" -> "Home"
            else -> getLastPage(context)
        }
    }

    DisposableEffect(backDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressedCallback.value()
            }
        }
        backDispatcher?.addCallback(callback)
        onDispose { callback.remove() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentPage) {
            "Home" -> HomeScreen(
                operations = operations,
                settings = settings.toMutableMap(),
                userName = settings["userName"] ?: Lang.get(7, context),
                onStart = { currentPage = "WorkingPage" },
                onSettingsClick = { currentPage = "Settings" }
            )

            "WorkingPage" -> WorkingPage(
                operations = operations,
                settings = settings.toMutableMap(),
                onBack = { currentPage = "Home" },
                onAddOperation = { type, value, date -> updateOperations(listOf(Triple(type, value, date)) + operations) },
                onEditOperation = { index, type, value, date ->
                    updateOperations(operations.toMutableList().apply { this[index] = Triple(type, value, date) })
                },
                onDeleteOperation = { index -> updateOperations(operations.toMutableList().apply { removeAt(index) }) },
                onSettingsClick = { currentPage = "Settings" }
            )

            "Settings" -> SettingsPage(
                settings = settings.toMutableMap(),
                onBack = { currentPage = getLastPage(context) },
                onSave = { updatedSettings -> updateSettings(updatedSettings) },
                onBackupImported = { newSettings, newOperations ->
                    settings = newSettings
                    operations = newOperations
                    currentPage = "Home"
                },
                onReset = { resetSettings() }  // Passando a função onReset para SettingsPage
            )
        }
    }
}

fun getTypeColor(context: Context, type: String): Color {
    return when (type) {
        "gain" -> Color(0xFF43A047)            // Verde grama
        "km" -> Color(0xFF484848)              // Cinza escuro
        "gasoline" -> Color(0xFFD35600)        // Laranja claro
        "oil" -> Color(0xFF5500B7)             // Roxo escuro
        "maintenance" -> Color(0xFF4B2C2C)     // Marrom escuro
        "calibrate_tires" -> Color(0xFFFF812D) // Laranja escuro
        "tighten_chain" -> Color(0xFF9133FF)   // Roxo claro
        "clean_chain" -> Color(0xFF001AFF)     // Azul
        "check_lights" -> Color(0xFFE0C10D)     // Ajuste conforme necessário
        "check_radiator" -> Color(0xFF9B465A)   // Ajuste conforme necessário
        "clean_filter" -> Color(0xFF42E57C)     // Ajuste conforme necessário
        else -> Color.Gray
    }
}


fun getAppVersion(context: Context): String {
    return try {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: Lang.get(21, context) // Caso versionName seja nulo, retorna "Desconhecida"
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        Lang.get(21, context) // Caso não consiga encontrar a versão
    }
}

fun getAppName(context: Context): String {
    val packageManager = context.packageManager
    val applicationInfo = context.applicationInfo
    return packageManager.getApplicationLabel(applicationInfo).toString()
}

@Composable
fun Header(
    title: String,
    onSettingsClick: () -> Unit,
    headerBackgroundColor: Color
) {
    val foreColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(headerBackgroundColor), // Usando a cor passada como parâmetro
        contentAlignment = Alignment.Center
    ) {
        // Row para garantir que o título ocupe toda a largura disponível e o botão de configurações fique à direita
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Título clicável
            Text(
                text = title,
                color = foreColor,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f) // Garante que o título ocupe a largura disponível
                    .padding(vertical = 15.dp)
                    .padding(start = 50.dp)
            )

            // Botão de configurações clicável na área inteira
            Box(
                modifier = Modifier
                    .clickable { onSettingsClick() }
                    .align(Alignment.CenterVertically) // Alinha verticalmente no centro
                    .padding(14.dp) // Espaçamento para a área clicável
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = foreColor
                )
            }
        }
    }
}

@Composable
fun Footer(
    title: String,
    onClick: () -> Unit,
    footerBackgroundColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(footerBackgroundColor.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        // Row para garantir que o título ocupe toda a largura disponível e o botão de configurações fique à direita
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Título clicável
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f) // Garante que o título ocupe a largura disponível
                    .clickable { onClick() }
                    .padding(vertical = 15.dp)
                    //.padding(start = 50.dp)
            )
        }
    }
}


@Composable
fun SettingsPage(
    settings: MutableMap<String, String>,
    onBack: () -> Unit,
    onSave: (MutableMap<String, String>) -> Unit,
    onBackupImported: (Map<String, String>, List<Triple<String, String, String>>) -> Unit,
    onReset: () -> Unit  // Função para resetar as configurações
) {
    val backColor = MaterialTheme.colorScheme.background
    val foreColor = MaterialTheme.colorScheme.onBackground
    val lightBackColor = LocalExtraColors.current.lightBackground

    val context = LocalContext.current
    val appVersion = getAppVersion(context)
    val focusManager = LocalFocusManager.current

    // Cria o estado a partir do mapa, usando o context para obter os defaults traduzidos se necessário
    var settingsState by remember { mutableStateOf(SettingsState.fromMap(settings, context)) }

    // Mapeamento: use as chaves internas fixas para os lembretes
    val fieldMapping = mapOf(
        "gasoline" to "kmGasoline",
        "oil" to "kmOil",
        "calibrate_tires" to "kmCalibrateTires",
        "tighten_chain" to "kmTightenChain",
        "clean_chain" to "kmCleanChain",
        "check_lights" to "kmCheckLights",
        "check_radiator" to "kmCheckRadiator",
        "clean_filter" to "kmCleanFilter"
    )

    // Backup Launchers
    val saveBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/xml")) { uri ->
        uri?.let { salvarBackup(context, it) }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            importarBackup(context, it)
            val (newSettings, newOperations) = loadStrackFromXML(context)
            onBackupImported(newSettings, newOperations)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor)
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Header(title = Lang.get(22, context), onSettingsClick = {}, headerBackgroundColor = lightBackColor)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Conta (usuário e tipo de veículo)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(lightBackColor, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingsTextField(
                            label = Lang.get(23, context),
                            value = settingsState.userName,
                            onValueChange = { newName ->
                                settingsState = settingsState.copy(userName = newName.filter { it.isLetter() })
                            },
                            onFocusLost = {
                                settings["userName"] = settingsState.userName.ifEmpty { Lang.get(7, context) }
                                onSave(settings)
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Aqui usamos chaves internas para vehType: "vehType1" e "vehType2"
                        VehicleTypeSelector(
                            label = Lang.get(24, context),
                            selectedVehicleType = settingsState.vehType,  // Valor interno, por exemplo, "vehType1" ou "vehType2"
                            onValueChange = { newType ->
                                settingsState = settingsState.copy(vehType = newType)
                                settings["vehType"] = newType
                                onSave(settings)
                            }
                        )
                    }
                }
            }
            // Lembretes de KM
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(lightBackColor, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(Lang.get(25, context), color = foreColor, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        listOf(
                            "gasoline" to settingsState.kmGasoline,
                            "oil" to settingsState.kmOil,
                            "calibrate_tires" to settingsState.kmCalibrateTires,
                            "tighten_chain" to settingsState.kmTightenChain,
                            "clean_chain" to settingsState.kmCleanChain,
                            "check_lights" to settingsState.kmCheckLights,
                            "check_radiator" to settingsState.kmCheckRadiator,
                            "clean_filter" to settingsState.kmCleanFilter
                        ).forEach { (key, value) ->
                            // Aqui usamos o valor interno (key) para atualizar os settings,
                            // mas exibimos a tradução usando Lang.get()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SettingsNumberField(
                                    label = Lang.get(
                                        when (key) {
                                            "gasoline" -> 12
                                            "oil" -> 13
                                            "calibrate_tires" -> 15
                                            "tighten_chain" -> 16
                                            "clean_chain" -> 17
                                            "check_lights" -> 18
                                            "check_radiator" -> 19
                                            "clean_filter" -> 20
                                            else -> 0
                                        }, context
                                    ),
                                    value = value,
                                    onValueChange = { newValue ->
                                        settingsState = settingsState.updateField(key, newValue)
                                    },
                                    onFocusLost = {
                                        settings[fieldMapping[key] ?: ""] = value
                                        onSave(settings)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            // Backup
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(lightBackColor, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(Lang.get(26, context), color = foreColor, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { saveBackupLauncher.launch(getBackupFileName(context)) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = Color.DarkGray
                                )
                            ) {
                                Text(
                                    text = Lang.get(27, context),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = { importBackupLauncher.launch(arrayOf("*/*")) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = Color.DarkGray
                                )
                            ) {
                                Text(
                                    text = Lang.get(28, context),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = { onReset() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = Color.DarkGray
                                )
                            ) {
                                Text(
                                    text = Lang.get(29, context),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Versão do app
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(lightBackColor, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        val appName = getAppName(context)
                        Text(appName, color = foreColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("${Lang.get(30, context)} $appVersion", color = foreColor, fontSize = 18.sp)
                        Text("© ${Calendar.getInstance().get(Calendar.YEAR)} Asen Lab Corporation\n${Lang.get(31, context)}", color = foreColor, fontSize = 16.sp)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
        Footer(title = Lang.get(32, context), onClick = onBack, footerBackgroundColor = LocalExtraColors.current.lightBackground)
    }
}


// Modelo para armazenar as configurações
data class SettingsState(
    val userName: String,
    val vehType: String,
    val kmGasoline: String,
    val kmOil: String,
    val kmCalibrateTires: String,
    val kmTightenChain: String,
    val kmCleanChain: String,
    val kmCheckLights: String,
    val kmCheckRadiator: String,
    val kmCleanFilter: String,
) {
    companion object {
        fun fromMap(map: Map<String, String>, context: Context) = SettingsState(
            userName = map["userName"] ?: Lang.get(7, context),
            vehType = map["vehType"] ?: "vehType1",
            kmGasoline = map["kmGasoline"] ?: "200",
            kmOil = map["kmOil"] ?: "900",
            kmCalibrateTires = map["kmCalibrateTires"] ?: "200",
            kmTightenChain = map["kmTightenChain"] ?: "200",
            kmCleanChain = map["kmCleanChain"] ?: "900",
            kmCheckLights = map["kmCheckLights"] ?: "900",
            kmCheckRadiator = map["kmCheckRadiator"] ?: "900",
            kmCleanFilter = map["kmCleanFilter"] ?: "900",
        )
    }

    fun updateField(label: String, value: String) = when (label) {
        "gasoline" -> copy(kmGasoline = value)
        "oil" -> copy(kmOil = value)
        "calibrate_tires" -> copy(kmCalibrateTires = value)
        "tighten_chain" -> copy(kmTightenChain = value)
        "clean_chain" -> copy(kmCleanChain = value)
        "check_lights" -> copy(kmCheckLights = value)
        "check_radiator" -> copy(kmCheckRadiator = value)
        "clean_filter" -> copy(kmCleanFilter = value)
        else -> this
    }
}

// Composable para campos de texto
@Composable
fun SettingsTextField(label: String, value: String, onValueChange: (String) -> Unit, onFocusLost: () -> Unit) {
    val foreColor = MaterialTheme.colorScheme.onBackground
    val fieldBackColor = MaterialTheme.colorScheme.surface
    val fieldForeColor = MaterialTheme.colorScheme.onSurface

    Column {
        Text(label, color = foreColor, fontSize = 18.sp)
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                // Limitar a 20 caracteres
                if (newValue.length <= 20) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused) onFocusLost() },
            textStyle = TextStyle(color = fieldForeColor, fontSize = 18.sp),
            decorationBox = { innerTextField ->
                Box(Modifier
                    .background(fieldBackColor, RoundedCornerShape(4.dp))
                    .padding(8.dp)) {
                    innerTextField()
                }
            }
        )
    }
}

// Composable para campos numéricos
@Composable
fun SettingsNumberField(label: String, value: String, onValueChange: (String) -> Unit, onFocusLost: () -> Unit) {
    val foreColor = MaterialTheme.colorScheme.onBackground
    val fieldBackColor = MaterialTheme.colorScheme.surface
    val fieldForeColor = MaterialTheme.colorScheme.onSurface

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = foreColor, fontSize = 18.sp)
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                // Permitir apenas números e até 4 caracteres
                if (newValue.length <= 4 && newValue.all { char -> char.isDigit() }) {
                    onValueChange(newValue)
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .width(100.dp)
                .onFocusChanged { if (!it.isFocused) onFocusLost() },
            textStyle = TextStyle(color = fieldForeColor, fontSize = 18.sp),
            decorationBox = { innerTextField ->
                Box(Modifier
                    .background(fieldBackColor, RoundedCornerShape(4.dp))
                    .padding(8.dp)) {
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun VehicleTypeSelector(
    label: String,
    selectedVehicleType: String,
    onValueChange: (String) -> Unit
) {
    val vehicleTypes = listOf("vehType1", "vehType2") // Valores internos

    Column {
        Text(label, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Row(verticalAlignment = Alignment.CenterVertically) {
            vehicleTypes.forEach { vehicleType ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(0.dp)
                ) {
                    RadioButton(
                        selected = selectedVehicleType == vehicleType,
                        onClick = { onValueChange(vehicleType) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.tertiary,
                            unselectedColor = MaterialTheme.colorScheme.onSurface,
                            disabledSelectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            disabledUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    )
                    // Exibição: usamos Lang.get() para obter a tradução
                    val displayText = when(vehicleType) {
                        "vehType1" -> Lang.get(8, LocalContext.current)
                        "vehType2" -> Lang.get(9, LocalContext.current)
                        else -> vehicleType
                    }
                    Text(displayText, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}


@Composable
fun HomeScreen(
    onStart: () -> Unit,
    onSettingsClick: () -> Unit,
    settings: MutableMap<String, String>,
    operations: List<Triple<String, String, String>>,
    userName: String
) {
    val backColor = MaterialTheme.colorScheme.background
    val foreColor = MaterialTheme.colorScheme.onBackground
    val lightBackColor = LocalExtraColors.current.lightBackground
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(true) { saveLastPage(context, "Home") }

    // Calcula os valores semanais e mensais
    val weeklySums = calculateWeeklySums(operations)
    val maxValue = weeklySums.maxOrNull() ?: 1
    val monthlySums = calculateMonthlySums(operations)
    val maxMonthValue = monthlySums.maxOrNull() ?: 1
    val currentWeekDays = getCurrentWeekDays()
    val lastFiveMonths = getLastFiveMonths(context)
    val lembretes = getLembretes(context, operations, settings)

    ClickableBackgroundColumn(
        focusManager = focusManager,
        backColor = backColor
    ) {
        Header(title = Lang.get(33, context), onSettingsClick = onSettingsClick, headerBackgroundColor = lightBackColor)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            item { WelcomeBox(userName, foreColor, lightBackColor) }
                item { ChartBox(Lang.get(34, context), weeklySums, maxValue, currentWeekDays.map { it.dayOfMonth.toString() }, listOf(Lang.get(45, context), Lang.get(46, context), Lang.get(47, context), Lang.get(48, context), Lang.get(49, context), Lang.get(50, context), Lang.get(51, context)), Color(0xFF0035FF), lightBackColor, foreColor) }
                item { ChartBox(Lang.get(35, context), monthlySums, maxMonthValue, lastFiveMonths, emptyList(), Color(0xFFE72222), lightBackColor, foreColor) }
                item { ReminderBox(lembretes, foreColor, lightBackColor) }
            item { Spacer(modifier = Modifier.height(8.dp))}
        }

        Footer(title = Lang.get(36, context), onClick = onStart, footerBackgroundColor = LocalExtraColors.current.lightBackground)
    }
}

// ================== COMPONENTES AUXILIARES ==================

@Composable
fun ClickableBackgroundColumn(
    focusManager: FocusManager,
    backColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor)
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
        content = content
    )
}

@Composable
fun WelcomeBox(userName: String, foreColor: Color, backgroundColor: Color) {
    val context = LocalContext.current
    val userLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale
    }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val fullDateFormatter = DateTimeFormatter.ofPattern("EEEE dd, MMMM yyyy", userLocale)

    val currentTime = LocalTime.now().format(timeFormatter)
    val currentFullDate = LocalDate.now()
        .format(fullDateFormatter)
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(userLocale) else it.toString() }
        }



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${Lang.get(37, context)} $userName, ${Lang.get(68, context)} $currentFullDate. ${Lang.get(67, context)} $currentTime.",
            color = foreColor,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }

}


@Composable
fun BarChart(value: Int, maxValue: Int, barColor: Color) {
    val foreColor = MaterialTheme.colorScheme.onBackground
    val scaledHeight = if (maxValue > 0) (value.toFloat() / maxValue.toFloat()) * 130 else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.height(130.dp)
    ) {
        Text(
            text = value.toString(),
            color = foreColor,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .width(30.dp)
                .height(scaledHeight.dp)
                .background(barColor)
        )
    }
}
@Composable
fun ChartBox(
    title: String,
    values: List<Int>,
    maxValue: Int,
    labels: List<String>,
    secondaryLabels: List<String>,
    barColor: Color,
    backgroundColor: Color,
    foreColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text(text = title, fontSize = 16.sp, color = foreColor, modifier = Modifier.padding(bottom = 8.dp))

            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                values.forEach { value -> BarChart(value = value, maxValue = maxValue, barColor = barColor) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                labels.forEach { label -> Text(text = label, fontSize = 12.sp, color = foreColor) }
            }

            if (secondaryLabels.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    secondaryLabels.forEach { label -> Text(text = label, fontSize = 12.sp, color = foreColor) }
                }
            }
        }
    }
}

@Composable
fun ReminderBox(lembretes: List<String>, foreColor: Color, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column {
            lembretes.forEach { lembrete ->
                Text(lembrete, style = MaterialTheme.typography.bodyMedium, color = foreColor)
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ================== FUNÇÕES AUXILIARES ==================

fun getCurrentWeekDays(): List<LocalDate> {
    val today = LocalDate.now()
    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    return (0..6).map { startOfWeek.plusDays(it.toLong()) }
}

fun getLastFiveMonths(context: Context): List<String> {
    val currentMonth = LocalDate.now()
    val monthNames = listOf(Lang.get(52, context), Lang.get(53, context), Lang.get(54, context), Lang.get(55, context), Lang.get(56, context), Lang.get(57, context), Lang.get(58, context), Lang.get(59, context), Lang.get(60, context), Lang.get(61, context), Lang.get(62, context), Lang.get(63, context))
    return (0..4).map {
        val monthIndex = currentMonth.minusMonths(4 - it.toLong()).monthValue - 1
        monthNames[monthIndex]
    }
}

fun calculateWeeklySums(operations: List<Triple<String, String, String>>): List<Int> {
    val today = LocalDate.now()
    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    val sums = MutableList(7) { 0 }

    operations.forEach { operation ->
        val (type, value, date) = operation

        try {
            // Usando ZonedDateTime para garantir que o fuso horário local seja considerado
            val zonedDateTime = ZonedDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()))
            val localDate = zonedDateTime.toLocalDate() // Pega apenas a data

            if (!localDate.isBefore(startOfWeek) && !localDate.isAfter(today)) { // Apenas datas da semana atual
                val dayIndex = (localDate.dayOfWeek.value % 7)
                val numericValue = value.replace(",", ".").toDouble().toInt()

                // Define se o valor será positivo ou negativo
                val adjustedValue = when (type.lowercase()) {
                    "gasoline", "oil", "maintenance" -> -numericValue
                    "gain" -> numericValue
                    else -> 0 // Ignora outros tipos
                }

                sums[dayIndex] += adjustedValue

                // Log para verificação
                Log.d(
                    "DEBUG",
                    "Data: $date | Tipo: $type | Dia da semana: $dayIndex | Valor: $adjustedValue"
                )
            }
        } catch (e: Exception) {
            Log.e("ERROR", "Erro ao processar data: $date", e)
        }
    }

    Log.d("DEBUG", "Somas Semanais: $sums")

    return sums
}

fun calculateMonthlySums(operations: List<Triple<String, String, String>>): List<Int> {
    val today = LocalDate.now()
    val startMonth = today.minusMonths(4).withDayOfMonth(1) // Começa 4 meses atrás

    val sums = MutableList(5) { 0 }

    operations.forEach { operation ->
        val (type, value, date) = operation

        try {
            val zonedDateTime = ZonedDateTime.parse(
                date,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
            )
            val localDate = zonedDateTime.toLocalDate()

            if (!localDate.isBefore(startMonth) && !localDate.isAfter(today)) {
                val monthIndex = ChronoUnit.MONTHS.between(startMonth, localDate).toInt()
                val numericValue = value.replace(",", ".").toDouble().toInt()

                val adjustedValue = when (type.lowercase()) {
                    "gasoline", "oil", "maintenance" -> -numericValue
                    "gain" -> numericValue
                    else -> 0
                }

                sums[monthIndex] += adjustedValue
            }
        } catch (e: Exception) {
            Log.e("ERROR", "Erro ao processar data: $date", e)
        }
    }

    return sums
}


fun getLembretes(
    context: Context,
    operations: List<Triple<String, String, String>>,
    settings: MutableMap<String, String>
): List<String> {
    val lembretes = mutableListOf<String>()

    // Cria uma lista das operações em ordem cronológica (mais antigas primeiro)
    val opsChrono = operations.reversed()

    // Função auxiliar: calcula a soma dos KM após a última ocorrência de uma operação específica.
    fun calcularKmDepoisDe(operacao: String): Int {
        val lastIndex = opsChrono.indexOfLast { it.first == operacao }
        return if (lastIndex != -1) {
            opsChrono.subList(lastIndex + 1, opsChrono.size)
                .filter { it.first == "km" } // "km" é a chave interna para os valores de quilometragem
                .sumOf { it.second.replace(",", ".").toDouble().toInt() }
        } else 0
    }

    // Limites definidos nas configurações: use chaves internas fixas
    val operacoesLimites = mapOf(
        "oil" to (settings["kmOil"]?.toIntOrNull() ?: 900),
        "gasoline" to (settings["kmGasoline"]?.toIntOrNull() ?: 200),
        "calibrate_tires" to (settings["kmCalibrateTires"]?.toIntOrNull() ?: 200),
        "tighten_chain" to (settings["kmTightenChain"]?.toIntOrNull() ?: 900),
        "clean_chain" to (settings["kmCleanChain"]?.toIntOrNull() ?: 900),
        "check_lights" to (settings["kmCheckLights"]?.toIntOrNull() ?: 900),
        "check_radiator" to (settings["kmCheckRadiator"]?.toIntOrNull() ?: 900),
        "clean_filter" to (settings["kmCleanFilter"]?.toIntOrNull() ?: 900)
    )

    // Para cada operação limite, verifica se os KM depois da operação excedem o limite.
    operacoesLimites.forEach { (operacao, limite) ->
        val kmDepoisDeOperacao = calcularKmDepoisDe(operacao)
        if (kmDepoisDeOperacao >= limite) {
            // Converte a chave interna para o texto traduzido para exibição.
            val operacaoExibicao = when (operacao) {
                "oil" -> Lang.get(13, context)             // "Oleo"
                "gasoline" -> Lang.get(12, context)          // "Gasolina"
                "calibrate_tires" -> Lang.get(15, context)     // "Calibrar Pneus"
                "tighten_chain" -> Lang.get(16, context)       // "Apertar Corrente"
                "clean_chain" -> Lang.get(17, context)         // "Limpar Corrente"
                "check_lights" -> Lang.get(18, context)        // "Checar Lampadas"
                "check_radiator" -> Lang.get(19, context)      // "Checar Radiador"
                "clean_filter" -> Lang.get(20, context)        // "Limpar Filtro"
                else -> operacao
            }
            lembretes.add("${Lang.get(38, context)} $operacaoExibicao | ($kmDepoisDeOperacao ${Lang.get(39, context)})")
        }
    }

    return lembretes
}



// ========================================= paginas ========================================= //

@Composable
fun WorkingPage(
    operations: List<Triple<String, String, String>>,
    settings: MutableMap<String, String>,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddOperation: (String, String, String) -> Unit,
    onEditOperation: (Int, String, String, String) -> Unit,
    onDeleteOperation: (Int) -> Unit,
) {
    val isPremiumVersion = false
    val mensalOperationsLimit = 5

    // Cores e estilos
    val backColor = MaterialTheme.colorScheme.background
    val foreColor = MaterialTheme.colorScheme.onBackground
    val lightBackColor = LocalExtraColors.current.lightBackground

    val context = LocalContext.current
    LaunchedEffect(true) { saveLastPage(context, "WorkingPage") }

    // Formatação de datas
    val sdfInput = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val sdfOutput = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
    }
    val todayStr = remember { sdfOutput.format(Date()) }

    // Agrupa operações por data (usando o formato "dd/MM/yyyy")
    val groupedOperations = remember(operations) {
        operations.groupBy { op ->
            try {
                val parsedDate = sdfInput.parse(op.third)
                parsedDate?.let { sdfOutput.format(it) } ?: Lang.get(21, context)
            } catch (e: Exception) { Lang.get(21, context) }
        }
    }

    // Estados para controlar o modal e a expansão da operação
    var expandedOperationKey by remember { mutableStateOf<String?>(null) }
    var isOperationScreenVisible by remember { mutableStateOf(false) }
    var operationToEdit by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backColor)
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(
                title = Lang.get(40, context),
                onSettingsClick = onSettingsClick,
                headerBackgroundColor = lightBackColor
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                groupedOperations.forEach { (date, operationsList) ->
                    item {
                        DateSection(
                            date = date,
                            operationsList = operationsList,
                            todayStr = todayStr,
                            expandedOperationKey = expandedOperationKey,
                            setExpandedOperationKey = { expandedOperationKey = it },
                            // Ao iniciar a edição, atualiza o estado centralizado:
                            onStartEditOperation = { op ->
                                operationToEdit = op
                                isOperationScreenVisible = true
                            },
                            onDeleteOperation = { localIndex ->
                                // Converte o índice local (no grupo) para o índice global
                                val globalIndex = operations.indexOf(operationsList[localIndex])
                                if (globalIndex >= 0) onDeleteOperation(globalIndex)
                            }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(146.dp)) }
            }

            Footer(
                title = Lang.get(41, context),
                onClick = onBack,
                footerBackgroundColor = LocalExtraColors.current.lightBackground
            )
        }

        // Botão flutuante para adicionar operação
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
                .padding(32.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            val now = remember { Calendar.getInstance() }

            FloatingActionButton(
                onClick = {
                    val currentMonthOperations = operations.count { op ->
                        try {
                            val date = sdfInput.parse(op.third)
                            if (date != null) {
                                val cal = Calendar.getInstance().apply { time = date }
                                cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                            } else false
                        } catch (e: Exception) {
                            false
                        }
                    }

                    if (!isPremiumVersion && currentMonthOperations >= mensalOperationsLimit) {
                        Toast.makeText(context, "${Lang.get(65, context)} (${currentMonthOperations}/${mensalOperationsLimit})\n${Lang.get(64, context)}", Toast.LENGTH_SHORT).show()
                    } else {
                        isOperationScreenVisible = true
                        operationToEdit = null
                        expandedOperationKey = null
                    }
                },
                modifier = Modifier.size(80.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = foreColor
            ) {
                Text(
                    "+",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 30.sp),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    // Modal unificado para adicionar ou editar operação
    if (isOperationScreenVisible) {
        OperationScreen(
            operationToEdit = operationToEdit,
            onDismiss = {
                isOperationScreenVisible = false
                operationToEdit = null
                expandedOperationKey = null
            },
            onSave = { newType, newValue, newDate ->
                if (operationToEdit == null) {
                    // Modo adicionar
                    onAddOperation(newType, newValue, newDate)
                } else {
                    // Modo editar: encontra o índice global da operação em edição
                    val globalIndex = operations.indexOf(operationToEdit)
                    if (globalIndex >= 0) onEditOperation(globalIndex, newType, newValue, newDate)
                }
                isOperationScreenVisible = false
                operationToEdit = null
                expandedOperationKey = null
            },
            settings = settings
        )
    }
}

@Composable
fun DateSection(
    date: String,
    operationsList: List<Triple<String, String, String>>,
    todayStr: String,
    expandedOperationKey: String?,
    setExpandedOperationKey: (String?) -> Unit,
    onStartEditOperation: (Triple<String, String, String>) -> Unit,
    onDeleteOperation: (Int) -> Unit
) {
    val context = LocalContext.current
    // Cabeçalho da data
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentSize(Alignment.Center)
    ) {
        val backgroundColor = if (date == todayStr) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                Box(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (date == todayStr) Lang.get(42, context) else date,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                color = if (date == todayStr) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    // Lista de operações para essa data
    operationsList.forEachIndexed { index, operation ->
        val (type, value, opDate) = operation
        val cleanedValue = value.replace(',', '.')
        val formattedValue = try {
            val valueAsDouble = cleanedValue.toDouble()
            if (type != "km") {
                val locale = Locale.getDefault()
                NumberFormat.getCurrencyInstance(locale).format(valueAsDouble)
            } else {
                valueAsDouble.toInt().toString()
            }
        } catch (e: Exception) {
            value
        }

        OperationRow(
            type = type,
            formattedValue = formattedValue,
            expandedOperationKey = expandedOperationKey,
            setExpandedOperationKey = setExpandedOperationKey,
            date = opDate,
            index = index,
            onStartEditOperation = { onStartEditOperation(operation) },
            onDeleteOperation = onDeleteOperation
        )
    }
}

@Composable
fun OperationRow(
    type: String,
    formattedValue: String,
    expandedOperationKey: String?,
    setExpandedOperationKey: (String?) -> Unit,
    date: String,
    index: Int,
    onStartEditOperation: () -> Unit,
    onDeleteOperation: (Int) -> Unit
) {
    val context = LocalContext.current
    val key = "$date-$index"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { setExpandedOperationKey(if (expandedOperationKey == key) null else key) },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val roundMax = 30
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .background(getTypeColor(context, type), RoundedCornerShape(roundMax.dp))
                .border(2.dp, getTypeColor(context, type), RoundedCornerShape(roundMax.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        LocalExtraColors.current.lightBackground,
                        RoundedCornerShape(roundMax.dp)
                    )
                    .height(62.dp)
                    .padding(12.dp)
            ) {
                Text(
                    text = formattedValue,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(getTypeColor(context, type), RoundedCornerShape(roundMax.dp))
                    .height(62.dp)
                    .padding(12.dp)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = translateType(context, type),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (expandedOperationKey == key) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(LocalExtraColors.current.lightBackground, RoundedCornerShape(8.dp))
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onStartEditOperation,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        Lang.get(43, context), // "Editar"
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )
                }

                Text(
                    text = "|",
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .align(Alignment.CenterVertically),
                    color = MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp)
                )

                Button(
                    onClick = { onDeleteOperation(index) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        Lang.get(66, context), // "Excluir"
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                    )
                }
            }
        }
    }
}




@Composable
fun OperationScreen(
    operationToEdit: Triple<String, String, String>? = null,  // (tipo, valor, data)
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
    settings: Map<String, String>  // Para saber o tipo de veículo
) {
    val foreColor = MaterialTheme.colorScheme.onBackground
    val fieldBackColor = MaterialTheme.colorScheme.surface
    val fieldForeColor = MaterialTheme.colorScheme.onSurface
    val lightBackColor = LocalExtraColors.current.lightBackground

    val context = LocalContext.current
    var amount by remember { mutableStateOf(operationToEdit?.second ?: "") }
    // Valor interno: "Ganho", "KM", etc. Texto exibido será traduzido via Lang.get()
    var selectedCategory by remember { mutableStateOf(operationToEdit?.first ?: "gain") }
    var expanded by remember { mutableStateOf(false) }

    // Lista de categorias com valor interno fixo e texto traduzido
    val allCategories = listOf(
        OperationKeys.GAIN to Lang.get(10, context),
        OperationKeys.KM to Lang.get(11, context),
        OperationKeys.GASOLINE to Lang.get(12, context),
        OperationKeys.OIL to Lang.get(13, context),
        OperationKeys.MAINTENANCE to Lang.get(14, context),
        OperationKeys.CALIBRATE_TIRES to Lang.get(15, context),
        OperationKeys.TIGHTEN_CHAIN to Lang.get(16, context),
        OperationKeys.CLEAN_CHAIN to Lang.get(17, context),
        OperationKeys.CHECK_LIGHTS to Lang.get(18, context),
        OperationKeys.CHECK_RADIATOR to Lang.get(19, context),
        OperationKeys.CLEAN_FILTER to Lang.get(20, context)
    )

    // Filtra categorias dependendo do tipo de veículo
    // Se o veículo for "vehType2", remove alguns tipos (exemplo: "Apertar Corrente" e "Limpar Corrente")
    val filteredCategories = if (settings["vehType"] == "vehType2") {
        allCategories.filterNot { it.first == OperationKeys.TIGHTEN_CHAIN || it.first == OperationKeys.CLEAN_CHAIN }
    } else {
        allCategories
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    val currentDate = dateFormat.format(Date())

    val symbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
        decimalSeparator = ','
    }
    val decimalFormat = DecimalFormat("0.00", symbols)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fundo semi-transparente para fechar o modal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        )
        // Modal de operação
        Box(
            modifier = Modifier
                .width(350.dp)
                .background(lightBackColor, RoundedCornerShape(8.dp))
                .zIndex(1f)
                .clickable(enabled = false, onClick = {})
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Dropdown para selecionar o tipo (exibindo o texto traduzido)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .background(fieldBackColor, RoundedCornerShape(4.dp))
                        .clickable { expanded = !expanded }
                ) {
                    // Para exibir, buscamos o par cujo valor interno seja o selecionado
                    val displayText = allCategories.find { it.first == selectedCategory }?.second ?: selectedCategory
                    Text(
                        text = displayText,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(12.dp),
                        color = fieldForeColor,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 26.sp)
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .width(320.dp)
                            .background(lightBackColor)
                    ) {
                        filteredCategories.forEach { (internalValue, displayValue) ->
                            DropdownMenuItem(
                                modifier = Modifier
                                    .height(68.dp)
                                    .background(lightBackColor),
                                text = {
                                    Text(
                                        text = displayValue,
                                        color = fieldForeColor,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 26.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                },
                                onClick = {
                                    selectedCategory = internalValue  // Salva o valor interno
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Campo para valor
                BasicTextField(
                    value = amount,
                    onValueChange = { newAmount ->
                        if (newAmount.length <= 7) {
                            val cleanedAmount = newAmount.replace("[^\\d,]".toRegex(), "")
                                .replace(",", ".")
                            try {
                                val number = cleanedAmount.toDouble()
                                if (number <= 9999) {
                                    amount = decimalFormat.format(number).replace(".", ",")
                                }
                            } catch (e: Exception) {
                                amount = cleanedAmount
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .background(fieldBackColor, RoundedCornerShape(4.dp))
                        .padding(16.dp),
                    textStyle = TextStyle(
                        color = foreColor,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Start
                    ),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            innerTextField()
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (amount.isNotBlank()) {
                            val finalDate = operationToEdit?.third ?: currentDate
                            // Usa o valor interno de selectedCategory para salvar a operação
                            onSave(selectedCategory, amount, finalDate)
                            onDismiss() // Fecha o modal
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = foreColor
                    )
                ) {
                    Text(
                        text = if (operationToEdit != null) Lang.get(43, context) else Lang.get(44, context),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 26.sp)
                    )
                }
            }
        }
    }
}
