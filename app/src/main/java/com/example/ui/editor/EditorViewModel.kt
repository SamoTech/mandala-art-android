package com.example.ui.editor

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MandalaRepository
import com.example.generator.MandalaLibrary
import com.example.model.ColorStep
import com.example.model.MandalaDesign
import com.example.model.MandalaPalettes
import com.example.util.AppSettings
import com.example.util.SoundAndHaptic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditorViewModel(
    val mandalaId: String,
    private val repository: MandalaRepository,
    private val appSettings: AppSettings,
    private val ownerUid: String = repository.currentOwnerUid,
    private val persistProgress: suspend (String, Map<String, Long>) -> Unit = { uid, snapshot ->
        repository.saveProgress(
            mandalaId = mandalaId,
            coloredRegions = snapshot,
            totalRegions = design.regionCount,
            ownerUid = uid
        )
    }
) : ViewModel() {

    val design: MandalaDesign = MandalaLibrary.getDesignById(mandalaId)

    private val _coloredRegions = MutableStateFlow<Map<String, Long>>(emptyMap())
    val coloredRegions: StateFlow<Map<String, Long>> = _coloredRegions.asStateFlow()

    private val _selectedColor = MutableStateFlow(MandalaPalettes.defaultColor)
    val selectedColor: StateFlow<Long> = _selectedColor.asStateFlow()

    private val _isEraserActive = MutableStateFlow(false)
    val isEraserActive: StateFlow<Boolean> = _isEraserActive.asStateFlow()

    private val _isZoomPanMode = MutableStateFlow(false)
    val isZoomPanMode: StateFlow<Boolean> = _isZoomPanMode.asStateFlow()

    private val undoStack = mutableListOf<ColorStep>()
    // Prevent a late initial database read from replacing the user's first edit.
    private var hasUserEdited = false
    private val redoStack = mutableListOf<ColorStep>()

    private val autosaveWorker = EditorAutosaveWorker(
        scope = viewModelScope,
        save = { snapshot -> persistProgress(ownerUid, snapshot) },
        onSaveError = { error ->
            Log.e("EditorViewModel", "Autosave failed for $mandalaId", error)
        }
    )

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    val percentCompleted: StateFlow<Float> = _coloredRegions.map { map ->
        val coloredCount = map.values.count { it != MandalaPalettes.CLEAR_COLOR }
        if (design.regionCount > 0) {
            (coloredCount.toFloat() / design.regionCount).coerceIn(0f, 1f)
        } else 0f
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    init {
        loadSavedProgress()
    }

    private fun loadSavedProgress() {
        viewModelScope.launch {
            val saved = repository.getProgressOnce(mandalaId, ownerUid)
            if (!hasUserEdited && saved != null && saved.coloredRegions.isNotEmpty()) {
                _coloredRegions.value = saved.coloredRegions
            }
        }
    }

    fun onRegionTapped(regionId: String, context: Context) {
        val targetColor = if (_isEraserActive.value) {
            MandalaPalettes.CLEAR_COLOR
        } else {
            _selectedColor.value
        }

        val previousColor = _coloredRegions.value[regionId] ?: MandalaPalettes.CLEAR_COLOR
        if (previousColor == targetColor) return // No change needed

        // Push to undo stack
        undoStack.add(ColorStep(regionId, previousColor, targetColor))
        redoStack.clear()
        _canUndo.value = true
        _canRedo.value = false

        // Update map
        val updated = _coloredRegions.value.toMutableMap()
        if (targetColor == MandalaPalettes.CLEAR_COLOR) {
            updated.remove(regionId)
        } else {
            updated[regionId] = targetColor
        }
        _coloredRegions.value = updated

        // Haptic feedback & click sound
        SoundAndHaptic.performColorTap(
            context,
            appSettings.soundEnabled.value,
            appSettings.hapticEnabled.value
        )

        // Auto-save
        saveProgressInternal()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val step = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(step)
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = true

        val updated = _coloredRegions.value.toMutableMap()
        if (step.previousColor == null || step.previousColor == MandalaPalettes.CLEAR_COLOR) {
            updated.remove(step.regionId)
        } else {
            updated[step.regionId] = step.previousColor
        }
        _coloredRegions.value = updated
        saveProgressInternal()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val step = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(step)
        _canUndo.value = true
        _canRedo.value = redoStack.isNotEmpty()

        val updated = _coloredRegions.value.toMutableMap()
        if (step.newColor == null || step.newColor == MandalaPalettes.CLEAR_COLOR) {
            updated.remove(step.regionId)
        } else {
            updated[step.regionId] = step.newColor
        }
        _coloredRegions.value = updated
        saveProgressInternal()
    }

    suspend fun reset() {
        val current = _coloredRegions.value
        if (current.isEmpty()) return

        // Push clear step as multiple or clear all
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _coloredRegions.value = emptyMap()
        hasUserEdited = true

        autosaveWorker.flush()
        repository.resetProgress(mandalaId, ownerUid)
    }

    fun setSelectedColor(color: Long) {
        _selectedColor.value = color
        _isEraserActive.value = false
    }

    fun toggleEraser() {
        _isEraserActive.value = !_isEraserActive.value
    }

    fun toggleZoomPanMode() {
        _isZoomPanMode.value = !_isZoomPanMode.value
    }

    /**
     * Persist the latest editor snapshot before navigation or when the app
     * leaves the foreground. Safe to call repeatedly.
     */
    suspend fun flushPendingSave() {
        autosaveWorker.flush()
    }

    private fun saveProgressInternal() {
        hasUserEdited = true
        autosaveWorker.submit(_coloredRegions.value)
    }

    class Factory(
        private val mandalaId: String,
        private val repository: MandalaRepository,
        private val appSettings: AppSettings,
        private val ownerUid: String = repository.currentOwnerUid,
        private val persistProgress: suspend (String, Map<String, Long>) -> Unit = { uid, snapshot ->
            repository.saveProgress(
                mandalaId = mandalaId,
                coloredRegions = snapshot,
                totalRegions = MandalaLibrary.getDesignById(mandalaId).regionCount,
                ownerUid = uid
            )
        }
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditorViewModel(mandalaId, repository, appSettings, ownerUid, persistProgress) as T
        }
    }
}
