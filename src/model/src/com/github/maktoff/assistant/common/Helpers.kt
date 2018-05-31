package com.github.maktoff.assistant.common

import org.apache.lucene.analysis.StopFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.snowball.SnowballFilter
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.tokenattributes.TermAttribute
import org.apache.lucene.util.Version
import org.json.JSONException
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

object Helpers {
    private val stopWords = HashSet<String>()

    init {
        val words = readFromFile(this::class.java.getResource("/stopwords.txt").path)
        stopWords.addAll(words.split(System.lineSeparator()))
    }

    fun readFromFile(path: String): String {
        var result = ""

        try {
            val bufferedReader = File(path).bufferedReader()
            result = bufferedReader.use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }

    fun writeToFile(path: String, text: String, append: Boolean = false) {
        try {
            val file = File(path)

            if (append) {
                file.appendText(text)
            } else {
                file.writeText(text)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getJSON(query: String, zipped: Boolean = false): String {
        var json = ""

        try {
            val url = URL(query)
            val connection = url.openConnection() as HttpURLConnection

            connection.doOutput = true
            connection.instanceFollowRedirects = false
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("charset", "utf-8")
            connection.connect()

            if (connection.responseCode == 200) {
                val inputStream = if (zipped)
                    GZIPInputStream(connection.inputStream) else connection.inputStream

                json = inputStreamToString(inputStream)
            }
        } catch (e: Exception) {
            when (e) {
                is JSONException, is IOException -> e.printStackTrace()
            }
        }

        return json
    }

    fun splitCamelCase(sequence: String): ArrayList<String> {
        val stringBuilder = StringBuilder()

        for (character in sequence) {
            if (character.isLetter()) {
                stringBuilder.append(character)
            } else {
                stringBuilder.append(" ")
            }
        }

        val text = stringBuilder.toString().trim()
        val temp = text.split(" ")
        val result = ArrayList<String>()

        for (item in temp) {
            stringBuilder.setLength(0)

            if (item == item.toUpperCase()) {
                if (item.isNotEmpty()) {
                    result.add(item.toLowerCase())
                }
            } else {
                for (character in item) {
                    if (character.isUpperCase()) {
                        val element = stringBuilder.toString()

                        if (element.isNotEmpty()) {
                            result.add(element)
                        }

                        stringBuilder.setLength(0)
                        stringBuilder.append(character.toLowerCase())
                    } else {
                        stringBuilder.append(character)
                    }
                }

                val element = stringBuilder.toString()

                if (element.isNotEmpty()) {
                    result.add(element)
                }
            }
        }

        return result
    }

    fun preProcessKeyWords(list: List<String>): ArrayList<String> {
        val stringBuilder = StringBuilder()

        for (item in list) {
            stringBuilder.append(item)
            stringBuilder.append(" ")
        }

        val text = stringBuilder.toString()
        var tokenStream: TokenStream = StandardTokenizer(Version.LUCENE_30, StringReader(text))

        tokenStream = StopFilter(true, tokenStream, stopWords)
        tokenStream = SnowballFilter(tokenStream, "English")

        val result = ArrayList<String>()
        val termAttr = tokenStream.getAttribute(TermAttribute::class.java)

        while (tokenStream.incrementToken()) {
            result.add(termAttr.term())
        }

        return result
    }

    fun splitToKeywords(sequence: String): ArrayList<String> = preProcessKeyWords(splitCamelCase(sequence))

    private fun inputStreamToString(inputStream: InputStream): String {
        var str = ""

        try {
            val result = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var length = 0

            while (length != -1) {
                length = inputStream.read(buffer)

                if (length == -1) {
                    break
                }

                result.write(buffer, 0, length)
            }

            str = result.toString("UTF-8")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream.close()
        }

        return str
    }
}