package com.example.kanban

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import org.json.JSONArray
import org.json.JSONObject

data class Task(
    var title: String,
    var isDone: Boolean = false,
    var color: Color = Color.White
)

data class ColumnData(
    var name: String,
    var color: Color = Color.LightGray,
    var tasks: MutableList<Task> = mutableListOf()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KanbanApp()
        }
    }
}

@Composable
fun KanbanApp(viewModel: KanbanViewModel = viewModel()) {
    val columns by viewModel.columns.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.addColumn() }) {
                Text("Добавить колонку")
            }
            Button(onClick = { viewModel.saveState(context) }) {
                Text("💾 Сохранить")
            }
            Button(onClick = { viewModel.loadState(context) }) {
                Text("📥 Загрузить")
            }
        }

        Row(modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())) {
            for ((columnIndex, column) in columns.withIndex()) {
                ColumnCard(
                    column = column,
                    onColumnNameChange = { viewModel.updateColumnName(columnIndex, it) },
                    onAddTask = { viewModel.addTask(columnIndex) },
                    onRemoveColumn = { viewModel.removeColumn(columnIndex) },
                    onTaskChecked = { taskIndex -> viewModel.toggleTask(columnIndex, taskIndex) },
                    onTaskTitleChange = { taskIndex, newTitle -> viewModel.updateTaskTitle(columnIndex, taskIndex, newTitle) },
                    onColorPick = { color -> viewModel.updateColumnColor(columnIndex, color) },
                    onTaskColorPick = { taskIndex, color -> viewModel.updateTaskColor(columnIndex, taskIndex, color) },
                    onMoveTask = { fromIndex, toColumnIndex -> viewModel.moveTask(columnIndex, fromIndex, toColumnIndex) }
                )
            }
        }
    }
}

@Composable
fun ColumnCard(
    column: ColumnData,
    onColumnNameChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onRemoveColumn: () -> Unit,
    onTaskChecked: (Int) -> Unit,
    onTaskTitleChange: (Int, String) -> Unit,
    onColorPick: (Color) -> Unit,
    onTaskColorPick: (Int, Color) -> Unit,
    onMoveTask: (Int, Int) -> Unit
) {
    var editingName by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(250.dp)
            .background(column.color),
        elevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editingName) {
                    BasicTextField(
                        value = column.name,
                        onValueChange = onColumnNameChange,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { editingName = false }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                } else {
                    Text(column.name, style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
                    IconButton(onClick = { editingName = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
                IconButton(onClick = onRemoveColumn) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            Button(onClick = { onColorPick(randomColor()) }) {
                Text("Цвет колонки")
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                itemsIndexed(column.tasks) { index, task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(task.color)
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress { change, _ ->
                                    onMoveTask(index, (0..2).random()) // заглушка
                                }
                            }
                    ) {
                        Checkbox(checked = task.isDone, onCheckedChange = { onTaskChecked(index) })
                        BasicTextField(
                            value = task.title,
                            onValueChange = { onTaskTitleChange(index, it) },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onTaskColorPick(index, randomColor()) }) {
                            Icon(Icons.Default.ColorLens, contentDescription = "Pick color")
                        }
                    }
                }
            }

            Button(onClick = onAddTask, modifier = Modifier.fillMaxWidth()) {
                Text("Добавить задачу")
            }
        }
    }
}

fun randomColor(): Color {
    val colors = listOf(Color.Yellow, Color.Green, Color.Cyan, Color.Magenta, Color.Red, Color.Gray, Color.LightGray)
    return colors.random()
}
