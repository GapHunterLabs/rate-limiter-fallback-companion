package dev.gaphunter.ratelimiterfallbackcompanion.detect

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.ratelimiterfallbackcompanion.model.FallbackHit
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaFallbackFinder]. */
object KotlinFallbackFinder {

    fun findAll(file: PsiFile): List<FallbackHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<FallbackHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                val containingClass = PsiTreeUtil.getParentOfType(function, KtClassOrObject::class.java) ?: return
                val functionNamesInClass = containingClass.declarations
                    .filterIsInstance<KtNamedFunction>()
                    .mapNotNull { it.name }
                    .toSet()

                for (entry in function.annotationEntries) {
                    val simpleName = entry.shortName?.asString() ?: continue
                    if (simpleName !in Resilience4jAnnotations.WITH_FALLBACK) continue
                    val fallbackName = fallbackMethodNameOf(entry) ?: continue
                    if (fallbackName in functionNamesInClass) continue

                    val nameIdentifier = function.nameIdentifier ?: continue
                    hits += FallbackHit(nameIdentifier, fallbackName, simpleName)
                }
            }
        })
        return hits
    }

    private fun fallbackMethodNameOf(annotation: KtAnnotationEntry): String? {
        val named = annotation.valueArguments.firstOrNull { it.getArgumentName()?.asName?.asString() == "fallbackMethod" }
        val text = named?.getArgumentExpression()?.text ?: return null
        return text.trim('"')
    }
}
