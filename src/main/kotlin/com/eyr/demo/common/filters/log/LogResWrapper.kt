package com.eyr.demo.common.filters.log

import com.eyr.demo.common.servlets.streams.HttpBodyServletOutputStream
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import org.slf4j.LoggerFactory
import java.io.*


class LogResWrapper(
    private val response: HttpServletResponse
) : HttpServletResponseWrapper(response) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LogResWrapper::class.java)
    }

    private val byteArrayOutputStream = ByteArrayOutputStream()

    fun log(request: HttpServletRequest) {
        val mapper = ObjectMapper()
        val resData = mapper.readValue(
            this.byteArrayOutputStream.toString(),
            object : TypeReference<HashMap<String, Any>>() {}
        )
        val prettied = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
            mapOf(
                "status" to "${response.status}",
                "code" to resData["code"],
                "payload" to resData["payload"],
            )
        )

        LOGGER.info("[${request.method}] <-- ${request.requestURI} $prettied")

        response.outputStream.write(byteArrayOutputStream.toByteArray())
    }

    override fun getOutputStream(): ServletOutputStream {
        return HttpBodyServletOutputStream(
            outputStream = this.byteArrayOutputStream,
        )
    }

    override fun getWriter(): PrintWriter {
        return PrintWriter(
            OutputStreamWriter(
                this.byteArrayOutputStream,
                this.response.characterEncoding
            )
        )
    }

    override fun flushBuffer() = writer.flush()

    override fun toString(): String = writer.toString()
}