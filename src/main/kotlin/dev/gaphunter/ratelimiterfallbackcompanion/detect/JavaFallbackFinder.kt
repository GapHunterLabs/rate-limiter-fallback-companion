package dev.gaphunter.ratelimiterfallbackcompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import dev.gaphunter.ratelimiterfallbackcompanion.model.FallbackHit

/**
 * Finds Java methods annotated with a Resilience4j resilience
 * annotation ([Resilience4jAnnotations.WITH_FALLBACK]) whose
 * `fallbackMethod = "name"` string doesn't match any method name in
 * the enclosing class -- a real, silent footgun: nothing fails at
 * compile time, but Resilience4j throws at runtime the first time the
 * fallback is actually triggered (rate limited, circuit open, retry
 * exhausted, etc.), which can be a rare code path that isn't exercised
 * until production.
 *
 * **v0.1 scope, stated honestly:** only checks that a method with the
 * given *name* exists somewhere in the class -- it doesn't validate
 * the fallback's parameter/return-type signature actually matches
 * what Resilience4j requires (same parameters as the original method,
 * optionally plus a trailing Throwable, and a matching return type).
 * A wrong-signature fallback with the right name isn't covered (yet).
 */
object JavaFallbackFinder {

    fun findAll(file: PsiFile): List<FallbackHit> {
        val hits = mutableListOf<FallbackHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethod(method: PsiMethod) {
                super.visitMethod(method)
                val containingClass = method.containingClass ?: return
                val methodNamesInClass = containingClass.methods.mapNotNull { it.name }.toSet()

                for (annotation in method.modifierList?.annotations.orEmpty()) {
                    val simpleName = annotation.nameReferenceElement?.referenceName ?: continue
                    if (simpleName !in Resilience4jAnnotations.WITH_FALLBACK) continue
                    val fallbackName = fallbackMethodNameOf(annotation) ?: continue
                    if (fallbackName in methodNamesInClass) continue

                    val nameIdentifier = method.nameIdentifier ?: continue
                    hits += FallbackHit(nameIdentifier, fallbackName, simpleName)
                }
            }
        })
        return hits
    }

    private fun fallbackMethodNameOf(annotation: PsiAnnotation): String? {
        val value = annotation.findAttributeValue("fallbackMethod") as? PsiLiteralExpression ?: return null
        return value.value as? String
    }
}
