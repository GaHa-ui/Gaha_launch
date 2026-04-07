package com.mcoder.presentation.files

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * File manager tab.
 */
@Composable
fun FilesScreen(viewModel: FilesViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val query by viewModel.query.collectAsState()
    val isEditorOpen by viewModel.isEditorOpen.collectAsState()
    val fileContent by viewModel.fileContent.collectAsState()
    val selectedFilePath by viewModel.selectedFilePath.collectAsState()
    var newFileName by remember { mutableStateOf("") }
    var newFolderName by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<FileEntry?>(null) }
    var renameValue by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Файлы", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Текущая папка: $currentPath")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Поиск") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::refresh) {
                Text("Обновить")
            }
            Button(onClick = viewModel::exportWorkspace) {
                Text("Экспорт ZIP")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newFileName,
            onValueChange = { newFileName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Новый файл (имя)") }
        )
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = {
                viewModel.createFile(newFileName)
                newFileName = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать файл")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newFolderName,
            onValueChange = { newFolderName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Новая папка (имя)") }
        )
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = {
                viewModel.createFolder(newFolderName)
                newFolderName = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать папку")
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn {
            items(entries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .clickable {
                            if (entry.isDirectory) {
                                viewModel.openFolder(entry.path)
                            } else {
                                viewModel.openFile(entry.path)
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val tag = when {
                            entry.isDirectory -> "[DIR]"
                            entry.name.endsWith(".kt") -> "[KT]"
                            entry.name.endsWith(".java") -> "[JAVA]"
                            entry.name.endsWith(".py") -> "[PY]"
                            entry.name.endsWith(".js") -> "[JS]"
                            entry.name.endsWith(".json") -> "[JSON]"
                            entry.name.endsWith(".txt") -> "[TXT]"
                            else -> "[FILE]"
                        }
                        Text(text = "$tag ${entry.name}", style = MaterialTheme.typography.titleMedium)
                        Text(text = if (entry.isDirectory) "Папка" else "Файл")
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { renameTarget = entry; renameValue = entry.name }) {
                                Text("Переименовать")
                            }
                            Button(onClick = { viewModel.delete(entry.path) }) {
                                Text("Удалить")
                            }
                        }
                    }
                }
            }
        }
    }

    if (isEditorOpen && selectedFilePath != null) {
        AlertDialog(
            onDismissRequest = viewModel::closeEditor,
            confirmButton = {
                Button(onClick = viewModel::saveFile) { Text("Сохранить") }
            },
            dismissButton = {
                Button(onClick = viewModel::closeEditor) { Text("Закрыть") }
            },
            title = { Text("Редактор: ${selectedFilePath!!.substringAfterLast("/")}") },
            text = {
                OutlinedTextField(
                    value = fileContent,
                    onValueChange = viewModel::updateFileContent,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Содержимое") }
                )
            }
        )
    }

    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            confirmButton = {
                Button(onClick = {
                    viewModel.rename(renameTarget!!.path, renameValue)
                    renameTarget = null
                }) { Text("ОК") }
            },
            dismissButton = {
                Button(onClick = { renameTarget = null }) { Text("Отмена") }
            },
            title = { Text("Переименовать") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Новое имя") }
                )
            }
        )
    }
}
