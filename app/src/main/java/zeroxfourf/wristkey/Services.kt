package zeroxfourf.wristkey

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.SimpleIcons
import compose.icons.simpleicons.*

data class ServiceIconItem(
    val name: String,
    val key: String,
    val icon: ImageVector
)

/**
 * Registry of supported brand icons and service mappings.
 * To add a new service, simply call [register] with the corresponding SimpleIcon and its matching issuer aliases.
 */
object Services {

    val allServiceIcons: List<ServiceIconItem> = listOf(
        ServiceIconItem("Google", "google", SimpleIcons.Google),
        ServiceIconItem("GitHub", "github", SimpleIcons.Github),
        ServiceIconItem("Microsoft", "microsoft", SimpleIcons.Microsoft),
        ServiceIconItem("Outlook", "outlook", SimpleIcons.Microsoftoutlook),
        ServiceIconItem("Azure", "azure", SimpleIcons.Microsoftazure),
        ServiceIconItem("Teams", "teams", SimpleIcons.Microsoftteams),
        ServiceIconItem("OneDrive", "onedrive", SimpleIcons.Microsoftonedrive),
        ServiceIconItem("Excel", "excel", SimpleIcons.Microsoftexcel),
        ServiceIconItem("Word", "word", SimpleIcons.Microsoftword),
        ServiceIconItem("PowerPoint", "powerpoint", SimpleIcons.Microsoftpowerpoint),
        ServiceIconItem("SharePoint", "sharepoint", SimpleIcons.Microsoftsharepoint),
        ServiceIconItem("Apple / iCloud", "apple", SimpleIcons.Apple),
        ServiceIconItem("Amazon / AWS", "amazon", SimpleIcons.Amazon),
        ServiceIconItem("Cloudflare", "cloudflare", SimpleIcons.Cloudflare),
        ServiceIconItem("DigitalOcean", "digitalocean", SimpleIcons.Digitalocean),
        ServiceIconItem("Docker", "docker", SimpleIcons.Docker),
        ServiceIconItem("GitLab", "gitlab", SimpleIcons.Gitlab),
        ServiceIconItem("Heroku", "heroku", SimpleIcons.Heroku),
        ServiceIconItem("Bitbucket", "bitbucket", SimpleIcons.Bitbucket),
        ServiceIconItem("Facebook / Meta", "facebook", SimpleIcons.Facebook),
        ServiceIconItem("Instagram", "instagram", SimpleIcons.Instagram),
        ServiceIconItem("LinkedIn", "linkedin", SimpleIcons.Linkedin),
        ServiceIconItem("Reddit", "reddit", SimpleIcons.Reddit),
        ServiceIconItem("Snapchat", "snapchat", SimpleIcons.Snapchat),
        ServiceIconItem("Twitter / X", "twitter", SimpleIcons.Twitter),
        ServiceIconItem("Discord", "discord", SimpleIcons.Discord),
        ServiceIconItem("Slack", "slack", SimpleIcons.Slack),
        ServiceIconItem("WhatsApp", "whatsapp", SimpleIcons.Whatsapp),
        ServiceIconItem("Zoom", "zoom", SimpleIcons.Zoom),
        ServiceIconItem("Netflix", "netflix", SimpleIcons.Netflix),
        ServiceIconItem("Nintendo", "nintendo", SimpleIcons.Nintendo),
        ServiceIconItem("Nvidia", "nvidia", SimpleIcons.Nvidia),
        ServiceIconItem("PlayStation", "playstation", SimpleIcons.Playstation),
        ServiceIconItem("Spotify", "spotify", SimpleIcons.Spotify),
        ServiceIconItem("Steam", "steam", SimpleIcons.Steam),
        ServiceIconItem("Twitch", "twitch", SimpleIcons.Twitch),
        ServiceIconItem("Xbox", "xbox", SimpleIcons.Xbox),
        ServiceIconItem("Adobe", "adobe", SimpleIcons.Adobe),
        ServiceIconItem("Atlassian", "atlassian", SimpleIcons.Atlassian),
        ServiceIconItem("Confluence", "confluence", SimpleIcons.Confluence),
        ServiceIconItem("Dropbox", "dropbox", SimpleIcons.Dropbox),
        ServiceIconItem("Figma", "figma", SimpleIcons.Figma),
        ServiceIconItem("Jira", "jira", SimpleIcons.Jira),
        ServiceIconItem("Salesforce", "salesforce", SimpleIcons.Salesforce),
        ServiceIconItem("Trello", "trello", SimpleIcons.Trello),
        ServiceIconItem("Coinbase", "coinbase", SimpleIcons.Coinbase),
        ServiceIconItem("eBay", "ebay", SimpleIcons.Ebay),
        ServiceIconItem("PayPal", "paypal", SimpleIcons.Paypal),
        ServiceIconItem("Airbnb", "airbnb", SimpleIcons.Airbnb),
        ServiceIconItem("Lyft", "lyft", SimpleIcons.Lyft),
        ServiceIconItem("Uber", "uber", SimpleIcons.Uber)
    )

    private val serviceIconMap: Map<String, ImageVector> = buildMap {
        fun register(icon: ImageVector, vararg aliases: String) {
            aliases.forEach { put(it.lowercase().trim(), icon) }
        }

        // Microsoft Services
        register(SimpleIcons.Microsoft, "microsoft", "live", "hotmail")
        register(SimpleIcons.Microsoftazure, "azure", "microsoftazure", "microsoft azure")
        register(SimpleIcons.Microsoftoutlook, "outlook", "microsoftoutlook", "microsoft outlook")
        register(SimpleIcons.Microsoftteams, "teams", "microsoftteams", "microsoft teams")
        register(SimpleIcons.Microsoftonedrive, "onedrive", "microsoftonedrive", "microsoft onedrive")
        register(SimpleIcons.Microsoftexcel, "excel", "microsoftexcel", "microsoft excel")
        register(SimpleIcons.Microsoftword, "word", "microsoftword", "microsoft word")
        register(SimpleIcons.Microsoftpowerpoint, "powerpoint", "microsoftpowerpoint", "microsoft powerpoint")
        register(SimpleIcons.Microsoftsharepoint, "sharepoint", "microsoftsharepoint", "microsoft sharepoint")

        // Tech & Cloud
        register(SimpleIcons.Google, "google")
        register(SimpleIcons.Github, "github")
        register(SimpleIcons.Apple, "apple", "icloud")
        register(SimpleIcons.Amazon, "amazon", "aws")
        register(SimpleIcons.Cloudflare, "cloudflare")
        register(SimpleIcons.Digitalocean, "digitalocean")
        register(SimpleIcons.Docker, "docker")
        register(SimpleIcons.Gitlab, "gitlab")
        register(SimpleIcons.Heroku, "heroku")
        register(SimpleIcons.Bitbucket, "bitbucket")

        // Social & Communication
        register(SimpleIcons.Facebook, "facebook", "meta")
        register(SimpleIcons.Instagram, "instagram")
        register(SimpleIcons.Linkedin, "linkedin")
        register(SimpleIcons.Reddit, "reddit")
        register(SimpleIcons.Snapchat, "snapchat")
        register(SimpleIcons.Twitter, "twitter", "x")
        register(SimpleIcons.Discord, "discord")
        register(SimpleIcons.Slack, "slack")
        register(SimpleIcons.Whatsapp, "whatsapp")
        register(SimpleIcons.Zoom, "zoom")

        // Entertainment & Gaming
        register(SimpleIcons.Netflix, "netflix")
        register(SimpleIcons.Nintendo, "nintendo")
        register(SimpleIcons.Nvidia, "nvidia")
        register(SimpleIcons.Playstation, "playstation", "psn")
        register(SimpleIcons.Spotify, "spotify")
        register(SimpleIcons.Steam, "steam")
        register(SimpleIcons.Twitch, "twitch")
        register(SimpleIcons.Xbox, "xbox")

        // Productivity & Business
        register(SimpleIcons.Adobe, "adobe")
        register(SimpleIcons.Atlassian, "atlassian")
        register(SimpleIcons.Confluence, "confluence")
        register(SimpleIcons.Dropbox, "dropbox")
        register(SimpleIcons.Figma, "figma")
        register(SimpleIcons.Jira, "jira")
        register(SimpleIcons.Salesforce, "salesforce")
        register(SimpleIcons.Trello, "trello")

        // Finance & E-Commerce
        register(SimpleIcons.Coinbase, "coinbase")
        register(SimpleIcons.Ebay, "ebay")
        register(SimpleIcons.Paypal, "paypal")

        // Travel & Services
        register(SimpleIcons.Airbnb, "airbnb")
        register(SimpleIcons.Lyft, "lyft")
        register(SimpleIcons.Uber, "uber")
    }

    /**
     * Looks up and returns the [ImageVector] brand icon corresponding to the provided [issuer] name or custom key.
     * Returns `null` if no matching brand icon is found.
     */
    fun getIcon(issuer: String, secret: String? = null, utilities: Utilities? = null): ImageVector? {
        if (secret != null && utilities != null) {
            val customKey = utilities.db.getString("CUSTOM_ICON_$secret", null)
            if (!customKey.isNullOrBlank()) {
                val icon = serviceIconMap[customKey.lowercase().trim()]
                if (icon != null) return icon
            }
        }
        return serviceIconMap[issuer.lowercase().trim()]
    }
}

/**
 * Top-level helper function for retrieving a brand icon by issuer name or custom key.
 */
fun getBrandIcon(issuer: String, secret: String? = null, utilities: Utilities? = null): ImageVector? =
    Services.getIcon(issuer, secret, utilities)
