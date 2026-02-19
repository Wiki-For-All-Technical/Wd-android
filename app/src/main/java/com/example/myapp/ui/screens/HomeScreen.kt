package com.example.myapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapp.data.AppView
import com.example.myapp.ui.components.SidebarNavigationDrawer
import com.example.myapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val homePageData by viewModel.homePageData.collectAsState()
    val isHomeLoading by viewModel.isHomeLoading.collectAsState()
    val stats by viewModel.wikidataStats.collectAsState()
    val context = LocalContext.current

    SidebarNavigationDrawer(viewModel = viewModel) {
        if (isHomeLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HomeHeroSection(viewModel = viewModel)
                WelcomeSection(stats = stats, context = context)
                GetInvolvedSection(context = context)
                LearnAboutDataSection(context = context)
                CurrentHighlightsSection(context = context)
                DiscoverSection(context = context)
                ContactSection(context = context)
                FooterSection(context = context)
            }
        }
    }
}

@Composable
fun HomeHeroSection(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        WikidataBarLogo(modifier = Modifier.size(80.dp, 56.dp))
        Text(
            text = "Wikidata",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "The free knowledge base with 115M+ items that anyone can edit.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF5F6368),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(AppView.SEARCH) },
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, Color(0xFFDADCE0)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF9AA0A6)
                )
                Text(
                    "Search Wikidata",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF9AA0A6)
                )
            }
        }
        Text(
            text = "↓ Scroll down for more",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9AA0A6)
        )
    }
}

@Composable
private fun WikidataBarLogo(modifier: Modifier = Modifier) {
    val barWidth = 4.dp
    val maxBarHeight = 56.dp
    val colors = listOf(
        Color(0xFFE4432B),
        Color(0xFF00AF89),
        Color(0xFF3366CC),
        Color(0xFFE4432B),
        Color(0xFF00AF89),
        Color(0xFF3366CC),
        Color(0xFFE4432B),
        Color(0xFF00AF89),
        Color(0xFF3366CC)
    )
    val heightFractions = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.55f, 0.75f, 0.45f)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = androidx.compose.ui.Alignment.Bottom
    ) {
        heightFractions.forEachIndexed { index, frac ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(maxBarHeight * frac)
                    .background(colors[index % colors.size])
            )
        }
    }
}

@Composable
fun WelcomeSection(stats: com.example.myapp.data.WikidataStats?, context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00AF89))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BarcodeIcon()
                    Text(
                        text = "Welcome!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Wikidata is the free and open knowledge base that can be read and edited by both humans and machines.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "It acts as central storage for the structured data of its Wikimedia sister projects including Wikipedia, Wikivoyage, Wiktionary, Wikisource, and others.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Wikidata also provides support to many other sites and services beyond just Wikimedia projects! The content of Wikidata is available under a free license, exported using standard formats, and can be interlinked to other open data sets on the linked data web.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    ClickableText("free license", "https://www.wikidata.org/wiki/Wikidata:Licensing", context)
                    Text("•", style = MaterialTheme.typography.bodySmall)
                    ClickableText("other open data sets", "https://www.wikidata.org/wiki/Wikidata:Linked_data", context)
                }
                stats?.let {
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Total Items",
                            value = it.totalItems,
                            icon = Icons.Rounded.DataUsage
                        )
                        StatItem(
                            label = "Active Users",
                            value = it.activeUsers,
                            icon = Icons.Rounded.People
                        )
                        StatItem(
                            label = "Edits Today",
                            value = it.editsToday,
                            icon = Icons.Rounded.Edit
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GetInvolvedSection(context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFCC0000))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BarcodeIcon()
                    Text(
                        text = "Get involved",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Learn about Wikidata",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    ClickableText("Wikidata introduction", "https://www.wikidata.org/wiki/Wikidata:Introduction", context)
                    ClickableText("Explore Wikidata by looking at a featured showcase item", "https://www.wikidata.org/wiki/Q42", context)
                    ClickableText("Get started with Wikidata's SPARQL query service", "https://query.wikidata.org/", context)
                }
                Divider()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Contribute to Wikidata",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    ClickableText("• Tutorials", "https://www.wikidata.org/wiki/Wikidata:Tutorials", context)
                    ClickableText("• Join a WikiProject", "https://www.wikidata.org/wiki/Wikidata:WikiProjects", context)
                    ClickableText("• Donate data", "https://www.wikidata.org/wiki/Wikidata:Data_donation", context)
                }
                Divider()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Meet the Wikidata community",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    ClickableText("Visit the community portal", "https://www.wikidata.org/wiki/Wikidata:Community_portal", context)
                    ClickableText("• Attend a Wikidata event", "https://www.wikidata.org/wiki/Wikidata:Events", context)
                    ClickableText("• Create a user account", "https://www.wikidata.org/wiki/Special:CreateAccount", context)
                    ClickableText("• Project chat, Telegram groups, or the live IRC chat", "https://www.wikidata.org/wiki/Wikidata:Project_chat", context)
                }
                Divider()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Use data from Wikidata",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    ClickableText("• Learn how you can retrieve and use data from Wikidata", "https://www.wikidata.org/wiki/Wikidata:Data_access", context)
                    ClickableText("More...", "https://www.wikidata.org/wiki/Wikidata:Data_access", context)
                }
            }
        }
    }
}

@Composable
fun LearnAboutDataSection(context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3366CC))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BarcodeIcon()
                    Text(
                        text = "Learn about data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "New to the wonderful world of data? Develop and improve your data literacy through content designed to get you up to speed and feeling comfortable with the fundamentals in no time.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.wikidata.org/wiki/Q2"))
                                context.startActivity(intent)
                            },
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.Public,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF3366CC)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Item: Earth (Q2)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFF3366CC)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.wikidata.org/wiki/Property:P610"))
                                context.startActivity(intent)
                            },
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.Terrain,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF3366CC)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Property: highest point (P610)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFF3366CC)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.wikidata.org/wiki/Q573"))
                                context.startActivity(intent)
                            },
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.Landscape,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF3366CC)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "custom value: Mount Everest (Q573)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFF3366CC)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentHighlightsSection(context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00AF89))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BarcodeIcon()
                    Text(
                        text = "Current highlights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClickableText("• Scott Buckley (Q61759294)", "https://www.wikidata.org/wiki/Q61759294", context)
                ClickableText("• Fátima Bosch (Q136265985)", "https://www.wikidata.org/wiki/Q136265985", context)
                ClickableText("• Stephen Hero (Q136265986)", "https://www.wikidata.org/wiki/Q136265986", context)
                ClickableText("• Françoise Quintin Ryszowska (Q136265987)", "https://www.wikidata.org/wiki/Q136265987", context)
                ClickableText("• Haytham Ali Tabatabai (Q136265988)", "https://www.wikidata.org/wiki/Q136265988", context)
                ClickableText("• Wanindara rail (Q136265989)", "https://www.wikidata.org/wiki/Q136265989", context)
                ClickableText("• Jonas Hallberg (pictured)", "https://www.wikidata.org/wiki/Q136265990", context)
            }
        }
    }
}

@Composable
fun DiscoverSection(context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFCC0000))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BarcodeIcon()
                    Text(
                        text = "Discover",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Innovative applications and contributions from the Wikidata community",
                    style = MaterialTheme.typography.bodyMedium
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.wikidata.org/wiki/Wikidata:WikiProject_Women"))
                            context.startActivity(intent)
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE5F1))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Featured WikiProject",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = "WikiProject Women",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFF3366CC)
                        )
                        Text(
                            text = "Passionate about highlighting women's achievements?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "WikiProject Women is dedicated to improving data about women globally.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    ClickableText("Check out Wikidata Tools for some of our best tools and gadgets for using and exploring Wikidata.", "https://www.wikidata.org/wiki/Wikidata:Tools", context)
                    ClickableText("Try building SPARQL queries and inquiring data using natural language using the Spinach Wikidata assistant.", "https://www.wikidata.org/wiki/Wikidata:Spinach", context)
                    ClickableText("See the category for user-made bot-updated lists of Wikidata items.", "https://www.wikidata.org/wiki/Category:Bot-updated_lists", context)
                }
                ClickableText(
                    "Know of an interesting project or research conducted using Wikidata? You can nominate content to be featured on the Main page here!",
                    "https://www.wikidata.org/wiki/Wikidata:Main_Page/Featured_content",
                    context
                )
            }
        }
    }
}

@Composable
fun ContactSection(context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3366CC))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BarcodeIcon()
                    Text(
                        text = "Contact",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClickableText("Wikidata mailing list", "https://lists.wikimedia.org/postorius/lists/wikidata.lists.wikimedia.org/", context)
                ClickableText("Wikidata technical mailing list", "https://lists.wikimedia.org/postorius/lists/wikidata-tech.lists.wikimedia.org/", context)
                ClickableText("• Discussion requests for specific topics", "https://www.wikidata.org/wiki/Wikidata:Project_chat", context)
                ClickableText("• Facebook, Mastodon, Bluesky", "https://www.wikidata.org/wiki/Wikidata:Contact", context)
                ClickableText("• Leave a message at project chat", "https://www.wikidata.org/wiki/Wikidata:Project_chat", context)
                ClickableText("• Telegram General Chat, Telegram Help or on IRC connect", "https://www.wikidata.org/wiki/Wikidata:Contact", context)
                ClickableText("• Report a technical problem", "https://www.wikidata.org/wiki/Wikidata:Contact", context)
                ClickableText("• Keep up-to-date: Weekly summaries", "https://www.wikidata.org/wiki/Wikidata:News", context)
            }
        }
    }
}

@Composable
fun FooterSection(context: android.content.Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "WIKIDATA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(24.dp))
            Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Text(
            text = "Wikidata is part of the non-profit, multilingual, free-content Wikimedia family.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Divider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClickableText("Privacy policy", "https://foundation.wikimedia.org/wiki/Privacy_policy", context)
            Text("•", style = MaterialTheme.typography.bodySmall)
            ClickableText("Code of Conduct", "https://www.wikidata.org/wiki/Wikidata:Code_of_Conduct", context)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClickableText("Developers", "https://www.wikidata.org/wiki/Wikidata:Developers", context)
            Text("•", style = MaterialTheme.typography.bodySmall)
            ClickableText("Statistics", "https://www.wikidata.org/wiki/Special:Statistics", context)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClickableText("Cookie statement", "https://foundation.wikimedia.org/wiki/Cookie_statement", context)
            Text("•", style = MaterialTheme.typography.bodySmall)
            ClickableText("Terms of Use", "https://foundation.wikimedia.org/wiki/Terms_of_Use", context)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClickableText("Desktop", "https://www.wikidata.org/", context)
            Text("•", style = MaterialTheme.typography.bodySmall)
            ClickableText("Data access", "https://www.wikidata.org/wiki/Wikidata:Data_access", context)
        }
    }
}

@Composable
fun BarcodeIcon() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
    }
}

@Composable
fun ClickableText(text: String, url: String, context: android.content.Context) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF3366CC),
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    )
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
