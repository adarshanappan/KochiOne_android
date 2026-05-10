package com.kochione.kochi_one.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kochione.kochi_one.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatView(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    initialMessage: String? = null,
    onFocus: () -> Unit = {}
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryText = textColor.copy(alpha = 0.6f)
    val inputBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF0F0F0)
    val userBubbleColor = Color(0xFF007AFF)
    val aiBubbleColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF0F0F0)
    val chipBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
    val chipBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)

    val messages = remember { SnapshotStateList<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Staggered entrance animations
    val headerAlpha = remember { Animatable(0f) }
    val headerOffsetY = remember { Animatable(-40f) }
    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(60f) }
    val contentScale = remember { Animatable(0.95f) }
    val inputAlpha = remember { Animatable(0f) }
    val inputOffsetY = remember { Animatable(80f) }

    LaunchedEffect(Unit) {
        // Header morphs in first (from the search bar position)
        launch {
            headerAlpha.animateTo(1f, tween(350, easing = EaseOutCubic))
        }
        launch {
            headerOffsetY.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow))
        }

        delay(120)

        // Content fades + scales in
        launch {
            contentAlpha.animateTo(1f, tween(450, easing = EaseOutCubic))
        }
        launch {
            contentOffsetY.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow))
        }
        launch {
            contentScale.animateTo(1f, tween(400, easing = EaseOutCubic))
        }

        delay(100)

        // Input slides up last
        launch {
            inputAlpha.animateTo(1f, tween(350, easing = EaseOutCubic))
        }
        launch {
            inputOffsetY.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow))
        }
    }

    val suggestions = listOf(
        "🍽️ Best restaurants nearby",
        "🚇 Metro timings",
        "🏖️ Things to do in Kochi",
        "💪 Gyms near me",
        "🏏 Where to play cricket",
        "🌴 Hidden gems"
    )

    fun getResponse(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("restaurant") || lower.contains("food") || lower.contains("eat") ||
            lower.contains("dinner") || lower.contains("dessert") || lower.contains("coffee") ||
            lower.contains("café") || lower.contains("cafe") || lower.contains("bakery") ->
                "Kochi is a foodie paradise! 🍽️ Check out the Eats tab for curated restaurant recommendations with ratings, menus, and directions."

            lower.contains("metro") || lower.contains("train") || lower.contains("transit") ||
            lower.contains("timing") ->
                "🚇 The Kochi Metro runs daily from 6:00 AM to 10:00 PM with trains every 5–10 minutes. Head to the Transit tab for live train tracking!"

            lower.contains("play") || lower.contains("cricket") || lower.contains("badminton") ||
            lower.contains("sport") || lower.contains("game") ->
                "Looking for some action? 🏏 The Play tab has the best sports venues — cricket grounds, badminton courts, and fun activity centers!"

            lower.contains("gym") || lower.contains("fitness") || lower.contains("yoga") ||
            lower.contains("workout") ->
                "Stay fit in Kochi! 💪 The Fitness tab has gyms, yoga studios, and wellness centers near you."

            lower.contains("thing") || lower.contains("explore") || lower.contains("visit") ||
            lower.contains("hidden") || lower.contains("gem") ->
                "Kochi has so much to explore! 🌴 From Fort Kochi and Chinese fishing nets to art galleries and backwaters. Check the Explore tab!"

            lower.contains("your name") || lower.contains("who are you") || lower.contains("what's your name") ||
            lower.contains("whats your name") || lower.contains("name?") ->
                "I'm Lilly! 🌸 Your personal Kochi guide. I can help you find restaurants, activities, transit info, and hidden gems around the city!"

            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hey there! 👋 I'm your Kochi guide. Ask me about restaurants, transit, activities, or anything about the city!"

            else ->
                "Great question! 🤔 Explore the app tabs for restaurants, activities, fitness venues, and transit info. Try asking about food or metro!"
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        messages.add(ChatMessage(text = text.trim(), isUser = true))
        inputText = ""
        isTyping = true
        coroutineScope.launch {
            if (messages.size > 0) {
                listState.animateScrollToItem(messages.size - 1)
            }
            delay(1000L + (500..1500).random().toLong())
            isTyping = false
            messages.add(ChatMessage(text = getResponse(text), isUser = false))
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-send initial message if provided
    LaunchedEffect(initialMessage) {
        if (!initialMessage.isNullOrBlank()) {
            delay(500) // Wait for entrance animation
            sendMessage(initialMessage)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header — morphs in from search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .graphicsLayer {
                    alpha = headerAlpha.value
                    translationY = headerOffsetY.value
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(inputBgColor)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "Back",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Lilly",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CD964))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Online",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText
                    )
                }
            }
        }

        // Content — scales + slides in
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = contentAlpha.value
                    translationY = contentOffsetY.value
                    scaleX = contentScale.value
                    scaleY = contentScale.value
                }
        ) {
            if (messages.isEmpty()) {
                // Welcome State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_forum_outline),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = textColor.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ask me anything about Kochi",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Restaurants, metro, activities, and more",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEachIndexed { index, suggestion ->
                            // Each chip animates in with its own staggered delay
                            val chipAlpha = remember { Animatable(0f) }
                            val chipScale = remember { Animatable(0.8f) }
                            LaunchedEffect(Unit) {
                                delay(350L + index * 60L)
                                launch { chipAlpha.animateTo(1f, tween(300, easing = EaseOutCubic)) }
                                launch { chipScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)) }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .graphicsLayer {
                                        alpha = chipAlpha.value
                                        scaleX = chipScale.value
                                        scaleY = chipScale.value
                                    }
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(chipBgColor)
                                    .border(1.dp, chipBorderColor, RoundedCornerShape(20.dp))
                                    .clickable { sendMessage(suggestion) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            } else {
                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .imeNestedScroll(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(message, userBubbleColor, aiBubbleColor, textColor)
                    }
                    if (isTyping) {
                        item {
                            TypingIndicator(aiBubbleColor, textColor)
                        }
                    }
                }
            }
        }

        // Input — slides up from bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .graphicsLayer {
                    alpha = inputAlpha.value
                    translationY = inputOffsetY.value
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(inputBgColor)
                    .onFocusChanged { if (it.isFocused) onFocus() },
                singleLine = true,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                ),
                keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Ask Lilly about Kochi...",
                                color = secondaryText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank()) Color(0xFF007AFF) else inputBgColor
                    )
                    .clickable { sendMessage(inputText) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_near_me),
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) Color.White else secondaryText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    userBubbleColor: Color,
    aiBubbleColor: Color,
    textColor: Color
) {
    val isUser = message.isUser
    val bubbleColor = if (isUser) userBubbleColor else aiBubbleColor
    val bubbleTextColor = if (isUser) Color.White else textColor
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    // Each bubble animates in
    val bubbleAlpha = remember { Animatable(0f) }
    val bubbleOffsetY = remember { Animatable(20f) }
    val bubbleScale = remember { Animatable(0.92f) }
    LaunchedEffect(Unit) {
        launch { bubbleAlpha.animateTo(1f, tween(300, easing = EaseOutCubic)) }
        launch { bubbleOffsetY.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)) }
        launch { bubbleScale.animateTo(1f, tween(250, easing = EaseOutCubic)) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = bubbleAlpha.value
                translationY = bubbleOffsetY.value
                scaleX = bubbleScale.value
                scaleY = bubbleScale.value
                transformOrigin = if (isUser) {
                    androidx.compose.ui.graphics.TransformOrigin(1f, 1f)
                } else {
                    androidx.compose.ui.graphics.TransformOrigin(0f, 1f)
                }
            },
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = bubbleTextColor
            )
        }
    }
}

@Composable
private fun TypingIndicator(bgColor: Color, dotColor: Color) {
    val transition = rememberInfiniteTransition(label = "typing")

    // Animate the indicator in
    val indicatorAlpha = remember { Animatable(0f) }
    val indicatorScale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        launch { indicatorAlpha.animateTo(1f, tween(300, easing = EaseOutCubic)) }
        launch { indicatorScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = indicatorAlpha.value
                scaleX = indicatorScale.value
                scaleY = indicatorScale.value
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 1f)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                .background(bgColor)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                val alpha by transition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_$index"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(alpha)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = 0.5f))
                )
            }
        }
    }
}
