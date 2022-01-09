package org.laolittle.plugin

import com.huaban.analysis.jieba.JiebaSegmenter

object JiebaSegmenter : JiebaSegmenter() {

    object SegMode {
        val SEARCH get() = JiebaSegmenter.SegMode.SEARCH
        val INDEX get() = JiebaSegmenter.SegMode.INDEX
    }
}