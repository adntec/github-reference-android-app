package com.envyserve.githubreference;

import java.util.ArrayList;
import java.util.List;

import com.envyserve.githubreference.R;
import com.envyserve.githubreference.facebook.FacebookFragment;
import com.envyserve.githubreference.fav.ui.FavFragment;
import com.envyserve.githubreference.instagram.InstagramFragment;
import com.envyserve.githubreference.maps.MapsFragment;
import com.envyserve.githubreference.media.ui.MediaFragment;
import com.envyserve.githubreference.rss.ui.RssFragment;
import com.envyserve.githubreference.soundcloud.ui.SoundCloudFragment;
import com.envyserve.githubreference.tumblr.ui.TumblrFragment;
import com.envyserve.githubreference.twi.ui.TweetsFragment;
import com.envyserve.githubreference.web.WebviewFragment;
import com.envyserve.githubreference.wordpress.ui.WordpressFragment;
import com.envyserve.githubreference.yt.ui.VideosFragment;

public class Config {

    //To open links in the WebView or outside the WebView.
    public static final boolean OPEN_EXPLICIT_EXTERNAL = true;
    public static final boolean OPEN_INLINE_EXTERNAL = false;

    //To open videos in our Local player or outside the local player
    public static final boolean PLAY_EXTERNAL = false;

    //To use the new drawer (overlaying toolbar)
    public static final boolean USE_NEW_DRAWER = true;

    //Wordpress perma-friendly API requests
    public static final boolean USE_WP_FRIENDLY = true;

	public static List<NavItem> configuration() {

		List<NavItem> i = new ArrayList<NavItem>();

		//DONT MODIFY ABOVE THIS LINE

        //Some sample content is added below, please refer to your documentation for more information about configuring this file properly
		i.add(new NavItem("Section", NavItem.SECTION));

        i.add(new NavItem("Youtube Channel", R.drawable.ic_details, NavItem.ITEM, VideosFragment.class,
                new String[]{"UU7V6hW6xqPAiUfataAZZtWA","UC7V6hW6xqPAiUfataAZZtWA"}));
        i.add(new NavItem("Youtube Playlist", R.drawable.ic_details, NavItem.ITEM, VideosFragment.class,
                new String[]{"LL7V6hW6xqPAiUfataAZZtWA"}));

        i.add(new NavItem("RSS", R.drawable.ic_details, NavItem.ITEM, RssFragment.class,
                new String[]{"https://googleblog.blogspot.nl/atom.xml"}));
        i.add(new NavItem("Rss Podcast", R.drawable.ic_details, NavItem.ITEM, RssFragment.class,
                new String[]{"http://feeds.nature.com/nature/podcast/current"}));
        i.add(new NavItem("Webview", R.drawable.ic_details, NavItem.ITEM, WebviewFragment.class,
                new String[]{"http://www.google.com"}));

        i.add(new NavItem("Wordpress Recent", R.drawable.ic_details, NavItem.ITEM, WordpressFragment.class,
                new String[]{"http:/envyserve.com/githubref", "", "http://www.androidpolice.com/?p=%d"}));
        i.add(new NavItem("Wordpress Category", R.drawable.ic_details, NavItem.ITEM, WordpressFragment.class,
                new String[]{"http://moma.org/wp/inside_out", "conservation"}));

        i.add(new NavItem("Tumblr", R.drawable.ic_details, NavItem.ITEM, TumblrFragment.class,
                new String[]{"androidbackgrounds"}, true));
        i.add(new NavItem("SoundCloud", R.drawable.ic_details, NavItem.ITEM, SoundCloudFragment.class,
                new String[]{"13568105"}));
        i.add(new NavItem("Radio Stream", R.drawable.ic_details, NavItem.ITEM, MediaFragment.class,
                new String[]{"http://yp.shoutcast.com/sbin/tunein-station.m3u?id=709809"}));

        i.add(new NavItem("Twitter", R.drawable.ic_details, NavItem.ITEM, TweetsFragment.class,
                new String[]{"Android"}));
        i.add(new NavItem("Instagram", R.drawable.ic_details, NavItem.ITEM, InstagramFragment.class,
                new String[]{"1067259270"}));
        i.add(new NavItem("Facebook", R.drawable.ic_details, NavItem.ITEM, FacebookFragment.class,
                new String[]{"104958162837"}));

        i.add(new NavItem("Maps Query", R.drawable.ic_details, NavItem.ITEM, MapsFragment.class,
                new String[]{"pharmacy"}));
        i.add(new NavItem("Maps Query", R.drawable.ic_details, NavItem.ITEM, MapsFragment.class,
                new String[]{"<b>Adress:</b><br>SomeStreet 5<br>Sydney, Australia<br><br><i>Email: Mail@Company.com</i>",
                        "Company",
                        "This is where our office is.",
                        "-33.864",
                        "151.206",
                        "13"}));

        //It's suggested to not change the content below this line

        i.add(new NavItem("Device", NavItem.SECTION));
        i.add(new NavItem("Favorites", R.drawable.ic_action_favorite, NavItem.EXTRA, FavFragment.class, null));
        i.add(new NavItem("Settings", R.drawable.ic_action_settings, NavItem.EXTRA, SettingsFragment.class, null));

        //DONT MODIFY BELOW THIS LINE

        return i;

	}

}