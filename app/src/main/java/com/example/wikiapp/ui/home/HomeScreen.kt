package com.example.wikiapp.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wikiapp.navigation.Screen
import com.example.wikiapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    onNavigationItemClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            WikidataTopBar(
                onSearchClick = {
                    navController?.navigate(Screen.Search.route)
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Section with Logo and Search
            item {
                HeroSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            navController?.navigate(Screen.Search.createRouteWithQuery(searchQuery))
                        }
                        focusManager.clearFocus()
                    },
                    onSearchClick = {
                        navController?.navigate(Screen.Search.route)
                    }
                )
            }

            // Introduction Section
            item {
                IntroductionSection()
            }

            // Featured Items Section
            item {
                FeaturedItemsSection(navController)
            }

            // Statistics Section
            item {
                StatisticsSection()
            }

            // Footer
            item {
                FooterSection()
            }
            
            // Extra content to ensure scrolling works
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
            
            item {
                Text(
                    text = "— End of page —",
                    style = MaterialTheme.typography.bodySmall,
                    color = Base50,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WikidataTopBar(
    onSearchClick: () -> Unit = {}
) {
    Surface(
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Wikidata Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WikidataBarcodeIcon(size = 28)
                Text(
                    text = "WIKIDATA",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Base10,
                    letterSpacing = 1.sp
                )
            }
            
            // Search icon
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = WikidataTeal
                )
            }
        }
    }
}

@Composable
private fun WikidataBarcodeIcon(size: Int = 40) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barData = listOf(
            Pair(0.6f, AccentRed),
            Pair(0.8f, WikidataItemGreen),
            Pair(0.5f, WikidataTeal),
            Pair(1f, AccentRed),
            Pair(0.7f, WikidataItemGreen),
            Pair(0.55f, WikidataTeal),
            Pair(0.85f, AccentRed),
            Pair(0.5f, WikidataItemGreen),
            Pair(0.7f, WikidataTeal)
        )
        
        barData.forEach { (heightFraction, color) ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((size * heightFraction).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun HeroSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Large Wikidata Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WikidataBarcodeIcon(size = 56)
            
            Text(
                text = "Wikidata",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Normal,
                color = Base10
            )
            
            Text(
                text = "The free knowledge base with 115M+ items\nthat anyone can edit.",
                style = MaterialTheme.typography.bodyLarge,
                color = Base30,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
        
        // Search Bar
        SearchBarComponent(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onSearch = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        
        // Scroll hint
        Text(
            text = "↓ Scroll down for more",
            style = MaterialTheme.typography.labelMedium,
            color = Base50,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarComponent(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp)),
        placeholder = {
            Text(
                "Search Wikidata",
                color = Base50
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = WikidataTeal
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Base50
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WikidataTeal,
            unfocusedBorderColor = Base70,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp)
    )
}

@Composable
private fun IntroductionSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Base90)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Introduction",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Base10
        )
        
        Text(
            text = "Wikidata is a free and open knowledge base that can be read and edited by both humans and machines.\n\n" +
                   "Wikidata acts as central storage for the structured data of its Wikimedia sister projects including Wikipedia, Wikivoyage, Wiktionary, Wikisource, and others.",
            style = MaterialTheme.typography.bodyMedium,
            color = Base20,
            lineHeight = 22.sp
        )
        
        // Quick links
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            QuickLinkChip(
                text = "Items",
                badge = "Q",
                color = WikidataItemGreen
            )
            QuickLinkChip(
                text = "Properties",
                badge = "P",
                color = WikidataPropertyOrange
            )
            QuickLinkChip(
                text = "Lexemes",
                badge = "L",
                color = WikidataLexemePurple
            )
        }
    }
}

@Composable
private fun QuickLinkChip(
    text: String,
    badge: String,
    color: Color
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Base70)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = AccentBlue
            )
        }
    }
}

@Composable
private fun FeaturedItemsSection(navController: NavController?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Explore Wikidata",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Base10
        )
        
        Text(
            text = "Discover items from various domains",
            style = MaterialTheme.typography.bodyMedium,
            color = Base30
        )
        
        // Featured items
        val featuredItems = listOf(
            FeaturedItem("Q42", "Douglas Adams", "British author and humourist"),
            FeaturedItem("Q1", "Universe", "totality of space and all contents"),
            FeaturedItem("Q2", "Earth", "third planet from the Sun"),
            FeaturedItem("Q5", "human", "any member of Homo sapiens"),
            FeaturedItem("Q146", "house cat", "domesticated feline"),
            FeaturedItem("Q7251", "Alan Turing", "English mathematician")
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(featuredItems) { item ->
                FeaturedItemCard(
                    item = item,
                    onClick = {
                        navController?.navigate(Screen.EntityDetail.createRoute(item.id))
                    }
                )
            }
        }
    }
}

private data class FeaturedItem(
    val id: String,
    val label: String,
    val description: String
)

@Composable
private fun FeaturedItemCard(
    item: FeaturedItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Base80)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Entity ID
            Text(
                text = item.id,
                style = MaterialTheme.typography.labelMedium,
                color = WikidataItemGreen,
                fontWeight = FontWeight.Medium
            )
            
            // Label
            Text(
                text = item.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AccentBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Description
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = Base30,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatisticsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WikidataTeal.copy(alpha = 0.05f))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Wikidata contains:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Base10
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                number = "115M+",
                label = "data items",
                color = WikidataItemGreen
            )
            StatItem(
                number = "12K+",
                label = "properties",
                color = WikidataPropertyOrange
            )
            StatItem(
                number = "1.6B+",
                label = "statements",
                color = WikidataTeal
            )
        }
    }
}

@Composable
private fun StatItem(
    number: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Base30
        )
    }
}

@Composable
private fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Base90)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FooterLink("About Wikidata")
            FooterLink("Help")
            FooterLink("Data access")
        }
        
        HorizontalDivider(color = Base70)
        
        Text(
            text = "Wikidata is a project of the Wikimedia Foundation",
            style = MaterialTheme.typography.bodySmall,
            color = Base50,
            textAlign = TextAlign.Center
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Privacy policy",
                style = MaterialTheme.typography.labelSmall,
                color = AccentBlue
            )
            Text(
                text = "Terms of Use",
                style = MaterialTheme.typography.labelSmall,
                color = AccentBlue
            )
        }
    }
}

@Composable
private fun FooterLink(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = AccentBlue
    )
}
