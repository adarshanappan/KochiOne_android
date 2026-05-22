package com.kochione.kochi_one.utils

object MessageSuggestionSets {
    fun default(): List<String> = listOf(
        "Best Cafe's in Kochi", "Best Dessert Spot?", "Cafe's Near Me?", 
        "Places with a view", "Quick bite nearby", "Where to eat?", 
        "Best breakfast", "Coffee nearby"
    )

    fun forHelp(): List<String> = listOf(
        "How do I use Kochi One?", "How do I find nearby spots?", 
        "How do likes and saves work?", "How do I use transit?", 
        "How do I open profile?", "How do I explore cards?"
    )

    fun forPlayTab(): List<String> = listOf(
        "Soccer turf nearby?", "Cricket grounds?", "Badminton courts?", 
        "Game centres?", "Snooker clubs?", "Fun activities?", 
        "Play near me?", "Indoor games?"
    )

    fun forFitnessTab(): List<String> = listOf(
        "Gym near me?", "Yoga classes?", "MMA gyms?", "Fitness centres?", 
        "Best gyms?", "Yoga studios?", "Combat sports?", "Health clubs?"
    )

    fun forPlayCategory(category: String): List<String> {
        return when (category) {
            "Soccer Turf" -> listOf("Soccer turf near me?", "Best soccer grounds", "5-a-side nearby?", "Turf booking?")
            "Cricket Turf" -> listOf("Cricket nets nearby?", "Best cricket grounds", "Practice nets?", "Turf near me?")
            "Badminton Turf" -> listOf("Badminton courts?", "Courts near me?", "Shuttle time?", "Indoor courts?")
            "Game Centre" -> listOf("Game centres?", "Arcade nearby?", "Indoor games?", "Fun zone near me?")
            "Snooker Centre" -> listOf("Snooker clubs?", "Pool tables nearby?", "Cue sports?", "Snooker near me?")
            "Fun Activities" -> listOf("Fun activities?", "Adventure spots?", "Family fun nearby?", "Weekend fun?")
            else -> forPlayTab()
        }
    }

    fun forFitnessCategory(category: String): List<String> {
        return when (category) {
            "Gym" -> listOf("Gym nearby?", "Best gyms?", "Weights & cardio?", "Gym with trainer?")
            "Yoga" -> listOf("Yoga classes?", "Yoga studios?", "Meditation?", "Yoga near me?")
            "MMA" -> listOf("MMA gyms nearby?", "Combat sports?", "Fight training?", "MMA classes?")
            "Fitness Centres" -> listOf("Fitness centres?", "Health clubs?", "Trainers?", "Fitness near me?")
            else -> forFitnessTab()
        }
    }

    fun forTab(tabName: String): List<String> {
        return when (tabName) {
            "Food" -> default()
            "Play" -> forPlayTab()
            "Fitness" -> forFitnessTab()
            "Help" -> forHelp()
            else -> default()
        }
    }
}
