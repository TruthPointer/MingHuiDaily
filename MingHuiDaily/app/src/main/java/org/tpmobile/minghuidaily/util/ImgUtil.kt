package org.tpmobile.minghuidaily.util

import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tpmobile.minghuidaily.data.ImgSize
import java.io.File

object ImgUtil {

    suspend fun getImgInfo(imgs: List<String>): List<ImgSize>  = withContext(Dispatchers.IO){
        val imgSizeList: MutableList<ImgSize> = mutableListOf()
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        imgs.forEach { img ->
            val decodeBmp = BitmapFactory.decodeFile(img, options)
            //此时，decode出来的decodeBmp宽高并不是原始图的宽高。
            //options里面的宽高才是原始图片的宽高
            val bmpWidth = options.outWidth
            val bmpHeight = options.outHeight
            imgSizeList.add(ImgSize(bmpWidth, bmpHeight, bmpHeight / bmpWidth.toFloat()))
            //Log.d("ImageInfo", "原始图片宽高 $bmpWidth $bmpHeight")
        }
        return@withContext imgSizeList
    }

    suspend fun getImgFileNamesInDir(dirPath: String, withoutExtension: Boolean = true): List<String> = withContext(Dispatchers.IO)  {
        val fileNames: MutableList<String> = mutableListOf()
        val fileTree: FileTreeWalk = File(dirPath).walk()
        fileTree.maxDepth(1) //需遍历的目录层级为1，即无需检查子目录
            .filter { it.isFile } //只挑选文件，不处理文件夹
            .filter { it.extension in listOf("png", "jpg") } //选择扩展名为png和jpg的图片文件
            .forEach { fileNames.add(if (withoutExtension) it.nameWithoutExtension else it.name) } //循环处理符合条件的文件

        return@withContext fileNames
    }

    suspend fun getImgFilePathsInDir(dirPath: String): List<String> =  withContext(Dispatchers.IO)  {
        val filePaths: MutableList<String> = mutableListOf()
        val fileTree: FileTreeWalk = File(dirPath).walk()
        fileTree.maxDepth(1) //需遍历的目录层级为1，即无需检查子目录
            .filter { it.isFile } //只挑选文件，不处理文件夹
            .filter { it.extension in listOf("png", "jpg") } //选择扩展名为png和jpg的图片文件
            .forEach { filePaths.add(it.path) } //循环处理符合条件的文件

        return@withContext filePaths
    }
}