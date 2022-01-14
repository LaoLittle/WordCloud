package org.laolittle.plugin

import com.huaban.analysis.jieba.JiebaSegmenter

object JiebaSegmenter : JiebaSegmenter() {

    @Suppress("unused")
    object SegMode {
        val SEARCH = JiebaSegmenter.SegMode.SEARCH
        val INDEX = JiebaSegmenter.SegMode.INDEX
    }
}