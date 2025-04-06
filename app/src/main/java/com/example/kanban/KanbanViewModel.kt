package com.example.kanban

import android.content.Context
import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class KanbanViewModel : ViewModel() {
    private val _columns = MutableStateFlow(
        mutableListOf(
            ColumnData("To Do"),
            ColumnData("In Progress"),
            ColumnData("Done")
        )
    )
    val columns = _columns.asStateFlow()

    fun addColumn() {
        _columns.value.add(ColumnData("Новая колонка"))
    }

    fun removeColumn(index: Int) {
        if (_columns.value.size > 1) {
            _columns.value.removeAt(index)
        }
    }

    fun updateColumnName(index: Int, newName: String) {
        _columns.value[index].name = newName
    }

    fun updateColumnColor(index: Int, color: Color) {
        _columns.value[index].color = color
    }

    fun addTask(columnIndex: Int) {
        _columns.value[columnIndex].tasks.add(Task("Новая задача"))
    }

    fun toggleTask(columnIndex: Int, taskIndex: Int) {
        val task = _columns.value[columnIndex].tasks[taskIndex]
        task.isDone = !task.isDone
    }

    fun updateTaskTitle(columnIndex: Int, taskIndex: Int, newTitle: String) {
        _columns.value[columnIndex].tasks[taskIndex].title = newTitle
    }

    fun updateTaskColor(columnIndex: Int, taskIndex: Int, color: Color) {
        _columns.value[columnIndex].tasks[taskIndex].color = color
    }

    fun moveTask(fromColumn: Int, fromIndex: Int, toColumn: Int) {
        if (fromColumn != toColumn && toColumn in _columns.value.indices) {
            val task = _columns.value[fromColumn].tasks.removeAt(fromIndex)
            _columns.value[toColumn].tasks.add(task)
        }
    }

    fun saveState(context: Context) {
        val json = JSONArray()
        for (col in _columns.value) {
            val colObj = JSONObject()
            colObj.put("name", col.name)
            colObj.put("color", colorToHex(col.color))
            val tasks = JSONArray()
            for (task in col.tasks) {
                val taskObj = JSONObject()
                taskObj.put("title", task.title)
                taskObj.put("isDone", task.isDone)
                taskObj.put("color", colorToHex(task.color))
                tasks.put(taskObj)
            }
            colObj.put("tasks", tasks)
            json.put(colObj)
        }
        context.getSharedPreferences("kanban", Context.MODE_PRIVATE).edit {
            putString("state", json.toString())
        }
    }

    fun loadState(context: Context) {
        val saved = context.getSharedPreferences("kanban", Context.MODE_PRIVATE).getString("state", null)
        if (saved != null) {
            val json = JSONArray(saved)
            val newCols = mutableListOf<ColumnData>()
            for (i in 0 until json.length()) {
                val colObj = json.getJSONObject(i)
                val col = ColumnData(
                    name = colObj.getString("name"),
                    color = Color(parseColor(colObj.getString("color"))),
                    tasks = mutableListOf()
                )
                val tasks = colObj.getJSONArray("tasks")
                for (j in 0 until tasks.length()) {
                    val t = tasks.getJSONObject(j)
                    col.tasks.add(
                        Task(
                            title = t.getString("title"),
                            isDone = t.getBoolean("isDone"),
                            color = Color(parseColor(t.getString("color")))
                        )
                    )
                }
                newCols.add(col)
            }
            _columns.value = newCols
        }
    }

    private fun colorToHex(color: Color): String {
        return "#%02X%02X%02X".format(
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
    }
}
