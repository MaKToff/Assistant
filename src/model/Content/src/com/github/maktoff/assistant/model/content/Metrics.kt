package com.github.maktoff.assistant.model.content

import com.github.maktoff.assistant.common.Helpers

internal object Metrics {
    private fun tf(document: String, term: String): Double {
        var result = 0.0
        val list = Helpers.splitToKeywords(document)

        for (word in list) {
            if (term.equals(word, true)) {
                result++
            }
        }

        return 1 + Math.log(result + 1)
    }

    private fun idf(documents: List<String>, term: String): Double {
        var n = 0.0

        for (doc in documents) {
            val list = Helpers.splitToKeywords(doc)

            for (word in list) {
                if (term.equals(word, true)) {
                    n++
                    break
                }
            }
        }

        return if (n > 0) Math.log(documents.size / n) else 0.0
    }

    fun getVector(document: String, documents: List<String>, terms: List<String>): ArrayList<Double> {
        val result = ArrayList<Double>()

        for (term in terms) {
            result.add(tf(document, term) * idf(documents, term))
        }

        return result
    }

    fun cosineSimilarity(vectorA: ArrayList<Double>, vectorB: ArrayList<Double>): Double {
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in 0 until vectorA.size) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += Math.pow(vectorA[i], 2.0)
            normB += Math.pow(vectorB[i], 2.0)
        }

        val sqrt = Math.sqrt(normA) * Math.sqrt(normB)

        return if (sqrt > 0) dotProduct / sqrt else 0.0
    }
}