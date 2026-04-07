package com.mcoder.presentation.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcoder.domain.model.FileEntry
import com.mcoder.domain.usecase.FileUseCases
import com.mcoder.util.Constants
import com.mcoder.util.ZipExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for file browser.
 */
@HiltViewModel
class FilesViewModel @Inject constructor(
    private val fileUseCases: FileUseCases
) : ViewModel() {

    private val _currentPath = MutableStateFlow(Constants.WORKSPACE_ROOT)
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _entries = MutableStateFlow<List<FileEntry>>(emptyList())
    val entries: StateFlow<List<FileEntry>> = _entries.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedFilePath = MutableStateFlow<String?>(null)
    val selectedFilePath: StateFlow<String?> = _selectedFilePath.asStateFlow()

    private val _fileContent = MutableStateFlow("")
    val fileContent: StateFlow<String> = _fileContent.asStateFlow()

    private val _isEditorOpen = MutableStateFlow(false)
    val isEditorOpen: StateFlow<Boolean> = _isEditorOpen.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val raw = fileUseCases.list(_currentPath.value)
            val q = _query.value.trim().lowercase()
            _entries.value = if (q.isBlank()) raw else raw.filter { it.name.lowercase().contains(q) }
        }
    }

    fun openFolder(path: String) {
        _currentPath.value = path
        refresh()
    }

    fun onQueryChange(value: String) {
        _query.value = value
        refresh()
    }

    fun openFile(path: String) {
        viewModelScope.launch {
            _selectedFilePath.value = path
            _fileContent.value = fileUseCases.readText(path)
            _isEditorOpen.value = true
        }
    }

    fun updateFileContent(value: String) {
        _fileContent.value = value
    }

    fun saveFile() {
        val path = _selectedFilePath.value ?: return
        viewModelScope.launch {
            fileUseCases.writeText(path, _fileContent.value)
            _isEditorOpen.value = false
        }
    }

    fun closeEditor() {
        _isEditorOpen.value = false
    }

    fun createFile(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val path = "${_currentPath.value}/$name"
            fileUseCases.writeText(path, "")
            refresh()
        }
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val path = "${_currentPath.value}/$name"
            fileUseCases.createFolder(path)
            refresh()
        }
    }

    fun delete(path: String) {
        viewModelScope.launch {
            fileUseCases.delete(path)
            refresh()
        }
    }

    fun rename(path: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            fileUseCases.rename(path, newName)
            refresh()
        }
    }

    fun exportWorkspace() {
        viewModelScope.launch {
            ZipExporter.exportWorkspace()
        }
    }
}
