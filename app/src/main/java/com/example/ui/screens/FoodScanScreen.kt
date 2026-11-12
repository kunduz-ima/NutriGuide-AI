package com.example.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.viewmodel.MainAppViewModel
import com.example.ui.viewmodel.ResultState
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScanScreen(
    mainAppViewModel: MainAppViewModel,
    onNavigateBack: () -> Unit,
    lang: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe scanning states
    val scanState by mainAppViewModel.scanState.collectAsState()
    val scanResult by mainAppViewModel.scanResult.collectAsState()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    // Launcher for critical camera hardware permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "Camera permission is required to analyze food photos.", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for Gallery Picking
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    capturedBitmap = bitmap
                    mainAppViewModel.scanFoodPhoto(bitmap)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image from gallery.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Modal or input states for editing analyzed results
    var editableFoodName by remember { mutableStateOf("") }
    var editableCalories by remember { mutableStateOf("") }
    var editableProtein by remember { mutableStateOf("") }
    var editableCarbs by remember { mutableStateOf("") }
    var editableFat by remember { mutableStateOf("") }
    var editableWeight by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var mealExpanded by remember { mutableStateOf(false) }

    // Update form states when Scan Results arrive from backend
    LaunchedEffect(scanResult) {
        scanResult?.let { res ->
            editableFoodName = res.foodName
            editableCalories = "%.0f".format(res.calories)
            editableProtein = "%.1f".format(res.protein)
            editableCarbs = "%.1f".format(res.carbs)
            editableFat = "%.1f".format(res.fat)
            editableWeight = "%.0f".format(res.estimatedWeight)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lang == "ru") "AI Распознавание Фото" else "AI Photo Scanner", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NutriColors.DeepForest)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NutriColors.LightPastelGreen)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera input block
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (capturedBitmap != null) {
                            // Show captured freeze frame
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Captured Photo",
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { capturedBitmap = null; mainAppViewModel.clearScan() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Retake", tint = NutriColors.DeepForest)
                            }
                        } else if (hasCameraPermission) {
                            // Authentic live CameraX feed
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx)
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        try {
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.setSurfaceProvider(previewView.surfaceProvider)
                                            }
                                            imageCapture = ImageCapture.Builder().build()
                                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                imageCapture
                                            )
                                        } catch (exc: Exception) {
                                            Log.e("CameraX", "Camera initialization or binding failed", exc)
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Permission fallback view
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.CameraEnhance, contentDescription = "Lock", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Camera Hardware Locked", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Action capture triggers
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (imageCapture != null && capturedBitmap == null) {
                                    // Real CameraX capture execution
                                    val executor = ContextCompat.getMainExecutor(context)
                                    imageCapture!!.takePicture(
                                        executor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                                val buffer = imageProxy.planes[0].buffer
                                                val bytes = ByteArray(buffer.remaining())
                                                buffer.get(bytes)
                                                val rawBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                
                                                // Rotate if needed
                                                val matrix = Matrix().apply { postRotate(imageProxy.imageInfo.rotationDegrees.toFloat()) }
                                                val rotatedBmp = Bitmap.createBitmap(rawBmp, 0, 0, rawBmp.width, rawBmp.height, matrix, true)
                                                
                                                capturedBitmap = rotatedBmp
                                                mainAppViewModel.scanFoodPhoto(rotatedBmp)
                                                imageProxy.close()
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                Toast.makeText(context, "Capture Failed: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, "Live feed inactive or photo already taken.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Camera, contentDescription = "Shutter")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == "ru") "Сделать фото" else "Take Photo")
                            }
                        }

                        Button(
                            onClick = {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PhotoLibrary, contentDescription = "Gallery")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == "ru") "Галерея" else "Gallery")
                            }
                        }
                    }
                }
            }

            // Results Form loader or Error display
            when (val state = scanState) {
                is ResultState.Loading -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = NutriColors.DeepForest)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (lang == "ru") "ИИ распознает блюдо и нарезает макросы..." else "Analyzing vision payload via Groq APIs...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
                is ResultState.Error -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.Error, contentDescription = "Error", tint = Color.Red)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    color = Color.Red,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                )
                                Button(
                                    onClick = { capturedBitmap?.let { mainAppViewModel.scanFoodPhoto(it) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Retry / Повторить", color = Color.White)
                                }
                            }
                        }
                    }
                }
                else -> {}
            }

            // Interactive results editor before final database enqueing!
            if (scanResult != null && scanState is ResultState.Success) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Scanned", tint = NutriColors.LightEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ru") "Редактирование результатов" else "Confirm & Edit Nutrient Details",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = NutriColors.DeepForest
                                )
                            }
                            
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))

                            // Editable input form
                            OutlinedTextField(
                                value = editableFoodName,
                                onValueChange = { editableFoodName = it },
                                label = { Text(if (lang == "ru") "Название блюда" else "Food Item Name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = editableWeight,
                                    onValueChange = { editableWeight = it },
                                    label = { Text(if (lang == "ru") "Вес (г)" else "Portion (g)") },
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = editableCalories,
                                    onValueChange = { editableCalories = it },
                                    label = { Text(if (lang == "ru") "Калории" else "Calories") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = editableProtein,
                                    onValueChange = { editableProtein = it },
                                    label = { Text(if (lang == "ru") "Белки" else "Protein (g)") },
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = editableCarbs,
                                    onValueChange = { editableCarbs = it },
                                    label = { Text(if (lang == "ru") "Углеводы" else "Carbs (g)") },
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = editableFat,
                                    onValueChange = { editableFat = it },
                                    label = { Text(if (lang == "ru") "Жиры" else "Fat (g)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Select Meal type selection
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { mealExpanded = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NutriColors.LightPastelGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("${if (lang == "ru") "Прием пищи:" else "Meal Target:"} $selectedMealType", color = NutriColors.DeepForest)
                                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Down", tint = NutriColors.DeepForest)
                                    }
                                }
                                DropdownMenu(
                                    expanded = mealExpanded,
                                    onDismissRequest = { mealExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type) },
                                            onClick = { selectedMealType = type; mealExpanded = false }
                                        )
                                    }
                                }
                            }

                            // Confirm Add to database logs!
                            Button(
                                onClick = {
                                    mainAppViewModel.saveScannedFood(
                                        mealType = selectedMealType,
                                        foodName = editableFoodName,
                                        calories = editableCalories.toFloatOrNull() ?: 0f,
                                        protein = editableProtein.toFloatOrNull() ?: 0f,
                                        carbs = editableCarbs.toFloatOrNull() ?: 0f,
                                        fat = editableFat.toFloatOrNull() ?: 0f,
                                        weightGrams = editableWeight.toFloatOrNull() ?: 0f
                                    )
                                    Toast.makeText(context, "Scanned nutrition logged!", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(if (lang == "ru") "Добавить в Дневник" else "Commit to Clinical Food Log", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
