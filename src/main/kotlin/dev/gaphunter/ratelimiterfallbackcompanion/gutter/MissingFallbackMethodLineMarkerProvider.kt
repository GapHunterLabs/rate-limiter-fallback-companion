package dev.gaphunter.ratelimiterfallbackcompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.ratelimiterfallbackcompanion.detect.JavaFallbackFinder
import dev.gaphunter.ratelimiterfallbackcompanion.detect.KotlinFallbackFinder
import dev.gaphunter.ratelimiterfallbackcompanion.model.FallbackHit
import dev.gaphunter.ratelimiterfallbackcompanion.review.ReviewPrompt

class MissingFallbackMethodLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "Resilience4j fallbackMethod doesn't exist"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val file = elements.firstOrNull()?.containingFile ?: return
        val hits = when (file.language.id) {
            "JAVA" -> JavaFallbackFinder.findAll(file)
            "kotlin" -> KotlinFallbackFinder.findAll(file)
            else -> emptyList()
        }
        if (hits.isEmpty()) return

        val hitsByElement = hits.associateBy { it.nameElement }
        for (element in elements) {
            val hit = hitsByElement[element] ?: continue
            result.add(buildMarker(hit))

            val path = file.virtualFile?.path ?: continue
            val lineNumber = file.viewProvider.document?.getLineNumber(element.textRange.startOffset) ?: -1
            ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
        }
    }

    private fun buildMarker(hit: FallbackHit): LineMarkerInfo<PsiElement> {
        val tooltip = "@${hit.annotationName}'s fallbackMethod \"${hit.fallbackMethodName}\" doesn't match any method in this class -- " +
            "Resilience4j will throw at runtime the first time this fallback is actually triggered"
        return LineMarkerInfo(
            hit.nameElement,
            hit.nameElement.textRange,
            FallbackIcons.RISK,
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }
}
