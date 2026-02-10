package com.github.kamiiroawase.android.invoiceholder.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.github.kamiiroawase.android.invoiceholder.entity.InvoiceEntity

object InvoiceUtil {
    // 发票号码正则
    private val fapiaodaimaRegex = Regex(
        "\\s*发.*票.*[代伐].*码.*[:：]\\s*(\\d+)"
    )

    // 发票号码正则
    private val fapiaoNoRegex = Regex(
        "\\s*发.*票.*号.*码.*[:：]\\s*(\\d+)"
    )

    // 开票日期正则
    private val kaipiaoriqiRegex = Regex(
        "\\s*开.*票.*[日曰].*期.*[:：]\\s*(.+)"
    )

    // 名称正则
    private val mingchengRegex = Regex(
        "\\s*名\\s*称\\s*[:：]\\s*(.+?)" +
            "(?=\\s*名\\s*称\\s*[:：]|$)" +
            "(?:\\s*名\\s*称\\s*[:：](.+))?"
    )

    // 税号正则
    private val shuihaoNoRegex = Regex(
        ".*纳.*[税稅務].*人.*识.*别.*号.*[:：](.*)"
    )

    // 价税合计正则
    private val jiashuihejiRegex = Regex(
        "\\s*价\\s*[税稅務]\\s*[含合会]\\s*计"
    )

    // 税额正则
    private val shuieRegex = Regex(
        "\\s*[¥Y](\\s*\\d+\\s*\\.\\s*\\d+)\\s*"
    )

    fun recognize(
        bitmap: Bitmap,
        callback: ((data: InvoiceEntity?) -> Unit)? = null
    ) {
        TextRecognition
            .getClient(ChineseTextRecognizerOptions.Builder().build())
            .process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { visionText ->
                val lines = mutableListOf<com.google.mlkit.vision.text.Text.Line>()

                for (block in visionText.textBlocks) {
                    lines.addAll(block.lines)
                }

                val sortedLines = lines.sortedWith { l1, l2 ->
                    val box1 = l1.boundingBox
                    val box2 = l2.boundingBox
                    if (box1 == null || box2 == null) {
                        0
                    } else {
                        val topDiff = box1.top - box2.top
                        if (kotlin.math.abs(topDiff) > box1.height() / 2) {
                            topDiff
                        } else {
                            box1.left - box2.left
                        }
                    }
                }

                var nameSkip = 0
                var shuieSkip = 0
                var shuihaoSkip = 0

                var shuie: String? = null
                var fapiaoNum: String? = null
                var buyerName: String? = null
                var sellerName: String? = null
                var fapiaodaima: String? = null
                var kaipiaoriqi: String? = null
                var shuijiaheji: String? = null
                var buyerShuihaoNum: String? = null
                var sellerShuihaoNum: String? = null

                for ((index, line) in sortedLines.withIndex()) {
                    if (shuijiaheji != null) {
                        break
                    }

                    val text = if (index > 0) {
                        sortedLines[index - 1].text + line.text
                    } else {
                        line.text
                    }

                    if (fapiaodaima == null) {
                        val fapiaodaimaMatch = fapiaodaimaRegex.find(text)

                        if (fapiaodaimaMatch != null) {
                            fapiaodaima = fapiaodaimaMatch.groupValues[1]
                            continue
                        }
                    }

                    if (fapiaoNum == null) {
                        val fapiaoNumMatch = fapiaoNoRegex.find(text)

                        if (fapiaoNumMatch != null) {
                            fapiaoNum = fapiaoNumMatch.groupValues[1]
//                            continue
                        }
                    }

                    if (kaipiaoriqi == null) {
                        val kaipiaoriqiMatch = kaipiaoriqiRegex.find(text)

                        if (kaipiaoriqiMatch != null) {
                            kaipiaoriqi = kaipiaoriqiMatch.groupValues[1]
                                .replace("o", "0")
                                .replace("O", "0")
                                .replace("Z", "2")
                                .replace("I", "1")
                                .replace("l", "1")
                                .replace("年", "-")
                                .replace("月", "-")
                                .replace("日", "")
                                .replace("曰", "")
                            continue
                        }
                    }

                    if (buyerName == null) {
                        val buyerNameMatch = mingchengRegex.find(text)

                        if (buyerNameMatch != null) {
                            buyerName = buyerNameMatch.groupValues[1]
                            continue
                        }
                    } else if (sellerName == null) {
                        val sellerNameMatch = mingchengRegex.find(text)

                        if (sellerNameMatch != null) {
                            val matchName2 = sellerNameMatch.groupValues[2]

                            if (matchName2 == "") {
                                if (nameSkip > 0) {
                                    sellerName = sellerNameMatch.groupValues[1]
                                    continue
                                } else {
                                    nameSkip++
                                }
                            } else {
                                sellerName = matchName2
                                continue
                            }
                        }
                    }

                    if (buyerShuihaoNum == null) {
                        val shuihaoNumMatch = shuihaoNoRegex.find(text)

                        if (shuihaoNumMatch != null) {
                            buyerShuihaoNum = shuihaoNumMatch.groupValues[1]
                            val match = Regex("([0-9A-Z]*)").find(buyerShuihaoNum)
                            buyerShuihaoNum = if (match != null) {
                                match.groupValues[1]
                            } else {
                                ""
                            }
                            continue
                        }
                    } else if (sellerShuihaoNum == null) {
                        val shuihaoNumMatch = shuihaoNoRegex.find(text)

                        if (shuihaoNumMatch != null) {
                            if (shuihaoSkip > 0) {
                                sellerShuihaoNum = shuihaoNumMatch.groupValues[1]
                                val match = Regex("([0-9A-Z]*)").find(sellerShuihaoNum)
                                sellerShuihaoNum = if (match != null) {
                                    match.groupValues[1]
                                } else {
                                    ""
                                }
                                continue
                            } else {
                                shuihaoSkip++
                            }
                        }
                    }

                    if (shuieSkip <= 0) {
                        if (jiashuihejiRegex.containsMatchIn(text)) {
                            shuieSkip++
//                            continue
                        }
                    }

                    if (shuie == null && shuieSkip > 0) {
                        val shuieMatch = shuieRegex.find(text)

                        if (shuieMatch != null) {
                            shuie = shuieMatch
                                .groupValues[1]
                                .replace("\\s+".toRegex(), "")
                            continue
                        }
                    }

                    @Suppress("SENSELESS_COMPARISON")
                    if (shuijiaheji == null && shuieSkip > 0) {
                        val shuijiahejiMatch = shuieRegex.find(text)

                        if (shuijiahejiMatch != null) {
                            shuijiaheji = shuijiahejiMatch
                                .groupValues[1]
                                .replace("\\s+".toRegex(), "")
                            continue
                        }
                    }
                }

                callback?.invoke(
                    InvoiceEntity(
                        fapiaodaima = (fapiaodaima ?: ""),
                        fapiaoNum = (fapiaoNum ?: ""),
                        kaipiaoriqi = (kaipiaoriqi ?: ""),
                        buyerName = (buyerName ?: ""),
                        sellerName = (sellerName ?: ""),
                        buyerShuihaoNum = (buyerShuihaoNum ?: ""),
                        sellerShuihaoNum = (sellerShuihaoNum ?: ""),
                        shuie = (shuie ?: ""),
                        shuijiaheji = (shuijiaheji ?: ""),
                        kaipiaoriqiDate = (kaipiaoriqi ?: "").let {
                            Regex("(\\d+)")
                                .findAll(it)
                                .joinToString("") { it2 -> it2.value }
                                .toLongOrNull()
                                ?: 0L
                        }
                    )
                )
            }
            .addOnFailureListener { e ->
                callback?.invoke(null)
            }
    }
}
