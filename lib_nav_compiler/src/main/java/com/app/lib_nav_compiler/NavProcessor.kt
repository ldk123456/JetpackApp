package com.app.lib_nav_compiler

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.app.lib_base.DESTINATION_OUTPUT_FILE_NAME
import com.app.lib_base.safeClose
import com.app.lib_nav_annotation.ACTIVITY_DESTINATION
import com.app.lib_nav_annotation.FRAGMENT_DESTINATION
import com.app.lib_nav_annotation.annotation.ActivityDestination
import com.app.lib_nav_annotation.annotation.FragmentDestination
import com.google.auto.service.AutoService
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.math.absoluteValue

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes(ACTIVITY_DESTINATION, FRAGMENT_DESTINATION)
class NavProcessor : AbstractProcessor() {
    private var messanger: Messager? = null
    private var filer: Filer? = null

    override fun init(environment: ProcessingEnvironment?) {
        super.init(environment)
        messanger = environment?.messager
        filer = environment?.filer
    }

    override fun process(annotations: MutableSet<out TypeElement>?, environment: RoundEnvironment?): Boolean {
        if (annotations.isNullOrEmpty() || environment == null) {
            return true
        }

        val activityElements = environment.getElementsAnnotatedWith(ActivityDestination::class.java)
        val fragmentElements = environment.getElementsAnnotatedWith(FragmentDestination::class.java)

        val destMap = HashMap<String, JSONObject>()
        handleDestination(activityElements, ActivityDestination::class.java, destMap)
        handleDestination(fragmentElements, FragmentDestination::class.java, destMap)

        if (destMap.isEmpty()) {
            return true
        }

        var fos: FileOutputStream? = null
        var writer: OutputStreamWriter? = null
        try {
            val resource = filer?.createResource(StandardLocation.CLASS_OUTPUT, "", DESTINATION_OUTPUT_FILE_NAME)
            val resourcePath = resource?.toUri()?.path
            messanger?.printMessage(Diagnostic.Kind.NOTE, "resourcePath: $resourcePath")
            if (resourcePath.isNullOrEmpty()) {
                return true
            }

            val appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4)
            val assetsPath = appPath + "src/main/assets"
            val file = File(assetsPath)
            if (!file.exists()) {
                file.mkdirs()
            }

            val outputFile = File(file, DESTINATION_OUTPUT_FILE_NAME)
            if (outputFile.exists()) {
                outputFile.delete()
            }
            outputFile.createNewFile()

            val content = JSON.toJSONString(destMap)
            fos = FileOutputStream(outputFile)
            writer = OutputStreamWriter(fos, "UTF-8")
            writer.write(content)
            writer.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fos.safeClose()
            writer.safeClose()
        }
        return true
    }

    private fun handleDestination(
        elements: Set<Element>?,
        annotationClass: Class<out Annotation>,
        destMap: HashMap<String, JSONObject>
    ) {
        elements?.asSequence()
            ?.filter {
                it is TypeElement
            }?.forEach {
                val element = it as TypeElement
                val className = element.qualifiedName.toString()
                val id = className.hashCode().absoluteValue
                var pageUrl = ""
                var needLogin = false
                var asStarter = false
                var isFragment = false
                when (val annotation = element.getAnnotation(annotationClass)) {
                    is ActivityDestination -> {
                        pageUrl = annotation.pageUrl
                        needLogin = annotation.needLogin
                        asStarter = annotation.asStarter
                        isFragment = false
                    }

                    is FragmentDestination -> {
                        pageUrl = annotation.pageUrl
                        needLogin = annotation.needLogin
                        asStarter = annotation.asStarter
                        isFragment = true
                    }
                }
                if (destMap.containsKey(pageUrl)) {
                    messanger?.printMessage(
                        Diagnostic.Kind.ERROR,
                        "不同的页面不允许使用相同的pageUrl, pageUrl: $pageUrl, class: $className"
                    )
                } else if (pageUrl.isNotEmpty()) {
                    destMap[pageUrl] = JSONObject().apply {
                        put("className", className)
                        put("id", id)
                        put("pageUrl", pageUrl)
                        put("needLogin", needLogin)
                        put("asStarter", asStarter)
                        put("isFragment", isFragment)
                    }
                }
            }
    }
}