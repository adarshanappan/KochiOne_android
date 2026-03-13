package com.kochione.kochi_one.models

sealed class ExploreContentBlock {
    data class Title(val text: String) : ExploreContentBlock()
    data class SectionHeading(val text: String) : ExploreContentBlock()
    data class Subheading(val text: String) : ExploreContentBlock()
    data class Paragraph(val text: String) : ExploreContentBlock()
    data class BulletPoint(val text: String) : ExploreContentBlock()
}

object ExploreContentParser {
    // Regex to match (N)Content(N) where N is a digit from 1 to 5
    private val markerRegex = Regex("""\((\d)\)(?s:(.*?))\(\1\)""")

    fun parse(body: String): List<ExploreContentBlock> {
        return markerRegex.findAll(body).mapNotNull { matchResult ->
            val markerType = matchResult.groupValues[1]
            val content = matchResult.groupValues[2].trim()
            
            when (markerType) {
                "1" -> ExploreContentBlock.Title(content)
                "2" -> ExploreContentBlock.SectionHeading(content)
                "3" -> ExploreContentBlock.Subheading(content)
                "4" -> ExploreContentBlock.Paragraph(content)
                "5" -> ExploreContentBlock.BulletPoint(content)
                else -> null
            }
        }.toList()
    }
}
