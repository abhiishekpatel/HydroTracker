package com.example.hydrotracker.ui.screens.tips

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hydrotracker.ui.theme.Amber500
import com.example.hydrotracker.ui.theme.Blue500
import com.example.hydrotracker.ui.theme.Cyan500
import com.example.hydrotracker.ui.theme.Green500

data class TipItem(
    val title: String,
    val content: String,
    val icon: ImageVector,
    val iconColor: androidx.compose.ui.graphics.Color,
    val category: String
)

private val tips = listOf(
    TipItem(
        title = "Why 4L When Taking Creatine?",
        content = "Creatine monohydrate increases intracellular water retention in muscle cells. This means your body requires additional water intake to maintain proper hydration. Research recommends consuming an additional 1-1.5L above baseline needs, bringing the total to approximately 3.5-4.5L daily.",
        icon = Icons.Default.Science,
        iconColor = Blue500,
        category = "Science"
    ),
    TipItem(
        title = "Timing Your Water Intake",
        content = "Don't try to drink all 4L at once. Spread your intake evenly throughout the day. A good rule is to drink a glass of water every hour during waking hours. Pair a glass with each meal and have water available during workouts.",
        icon = Icons.Default.WaterDrop,
        iconColor = Cyan500,
        category = "Best Practice"
    ),
    TipItem(
        title = "Creatine & Water: The Science",
        content = "Creatine is stored as phosphocreatine in muscle cells. When creatine enters muscle cells, water follows via osmosis. This is why muscles appear fuller when supplementing, but it also means dehydration risk increases if water intake isn't adequate.",
        icon = Icons.Default.Science,
        iconColor = Blue500,
        category = "Science"
    ),
    TipItem(
        title = "Signs of Dehydration",
        content = "Watch for these warning signs: dark yellow urine, dry mouth, headaches, fatigue, dizziness, decreased workout performance, and muscle cramps. If you notice any of these, increase your water intake immediately.",
        icon = Icons.Default.Info,
        iconColor = Amber500,
        category = "Health"
    ),
    TipItem(
        title = "Pre-Workout Hydration",
        content = "Drink 500ml of water 30-60 minutes before training. During intense exercise, aim for 200-300ml every 15-20 minutes. After your workout, replenish with at least 500ml. Proper hydration directly impacts strength and endurance.",
        icon = Icons.Default.FitnessCenter,
        iconColor = Green500,
        category = "Training"
    ),
    TipItem(
        title = "Creatine Dosing & Water",
        content = "During the loading phase (20g/day for 5-7 days), water needs are even higher. During maintenance (3-5g/day), 4L is the target. Always dissolve creatine in at least 250ml of water and drink an extra glass alongside each dose.",
        icon = Icons.Default.Bolt,
        iconColor = Amber500,
        category = "Supplementation"
    ),
    TipItem(
        title = "Water & Creatine Absorption",
        content = "Adequate water intake improves creatine absorption and transport to muscle cells. Dehydration can reduce the effectiveness of your creatine supplementation, meaning you won't get the full strength and performance benefits.",
        icon = Icons.Default.Science,
        iconColor = Blue500,
        category = "Science"
    ),
    TipItem(
        title = "Myth: Creatine Causes Kidney Damage",
        content = "Multiple long-term studies have shown that creatine monohydrate at recommended doses (3-5g/day) does not cause kidney damage in healthy individuals. However, staying well-hydrated supports kidney function and is always a good practice.",
        icon = Icons.Default.Info,
        iconColor = Amber500,
        category = "Myth Busting"
    ),
    TipItem(
        title = "Beyond Water: Electrolytes",
        content = "When drinking high volumes of water, consider electrolyte balance. Adding a pinch of salt to one of your water bottles or consuming electrolyte-rich foods helps maintain sodium and potassium levels, preventing hyponatremia.",
        icon = Icons.Default.Bolt,
        iconColor = Green500,
        category = "Nutrition"
    ),
    TipItem(
        title = "Building the Hydration Habit",
        content = "Carry a marked water bottle everywhere. Set reminders. Drink a glass immediately upon waking. Pair water with existing habits (meals, coffee, gym). Track your progress. After 2-3 weeks, drinking 4L will feel natural.",
        icon = Icons.Default.WaterDrop,
        iconColor = Cyan500,
        category = "Habits"
    )
)

@Composable
fun TipsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Text(
            text = "Creatine & Hydration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Text(
            text = "Science-backed tips for optimal hydration",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(tips) { _, tip ->
                TipCard(tip = tip)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun TipCard(tip: TipItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    tip.icon,
                    contentDescription = null,
                    tint = tip.iconColor,
                    modifier = Modifier.size(24.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tip.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = tip.iconColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = tip.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tip.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}
