package yumo.achat.app.ui.imagevideo

internal data class GenerationModeOption(val quality: String, val price: Int)

internal fun generationModes(template: SelectedGenerationTemplate?): List<GenerationModeOption> = listOfNotNull(
    template?.fastPrice?.takeIf { it >= 0 }?.let { GenerationModeOption("fast", it) },
    template?.qualityPrice?.takeIf { it >= 0 }?.let { GenerationModeOption("quality", it) },
)

internal fun defaultGenerationMode(modes: List<GenerationModeOption>): String? =
    modes.firstOrNull { it.quality == "quality" }?.quality ?: modes.firstOrNull()?.quality

internal fun generationSelectionForMode(template: SelectedGenerationTemplate?, quality: String?): SelectedGenerationTemplate? {
    val mode = generationModes(template).firstOrNull { it.quality == quality } ?: return null
    return template?.copy(quality = mode.quality, quotedDiamondCost = mode.price)
}

internal fun shouldShowGenerationMode(resourceId: String?, template: SelectedGenerationTemplate?): Boolean =
    !resourceId.isNullOrBlank() && template != null
