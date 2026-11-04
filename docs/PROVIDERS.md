# Media providers

This guide records the supported provider paths and their boundaries.

## PeerTube

- Search uses the public PeerTube REST endpoint `GET /api/v1/search/videos`.
- Current default instance: [TILvids](https://tilvids.com), an independent PeerTube instance.
- Results open the instance's official embedded player using its `embedPath`.
- This first version does not download videos or attempt to resolve media stream URLs.
- PeerTube is federated: results depend on the selected instance and its search/federation configuration. The default instance can be replaced in `PeerTubeProvider.DEFAULT_INSTANCE`.

References:
- [PeerTube REST API](https://docs.joinpeertube.org/api-rest-reference.html)
- [PeerTube embed player](https://docs.joinpeertube.org/api/embed-player)
- [PeerTube public instances](https://instances.joinpeertube.org/)

## Internet Archive

- Search uses the Internet Archive Advanced Search API and requests movie metadata only.
- Results open the official Archive.org embedded player.
- The result may show a rights/license label returned by the catalog, but the app does not infer a download permission from the presence or absence of that text.
- No Archive.org download URL is exposed in this version. Playback is delegated to the item's own player and availability rules.

References:
- [Internet Archive developers](https://archive.org/developers/)
- [Advanced Search API](https://archive.org/advancedsearch.php#raw)
- [Metadata API](https://archive.org/developers/md-read.html)

## YouTube

YouTube is optional and disabled until a Data API key is configured.

1. Create a project and enable **YouTube Data API v3** in Google Cloud.
2. Create an API key and restrict it to the Android application ID `io.github.mauriciogamedev.youvideo` and the signing certificate fingerprints used by the builds.
3. Put the key in the ignored local `local.properties` file:

   ```properties
   YOUTUBE_API_KEY=YOUR_RESTRICTED_API_KEY
   ```

   Alternatively, pass it as a Gradle property: `-PYOUTUBE_API_KEY=YOUR_RESTRICTED_API_KEY`.

4. Rebuild locally. Never commit the key or attach a key-bearing APK to a public workflow/artifact.

The app uses YouTube Data API search and then checks each candidate with `videos.list(part=status)`. Only videos with a known `madeForKids=false` status and `embeddable=true` are displayed. Videos with unknown status are excluded conservatively.

Playback is performed by the official YouTube embedded player in Android's WebView. The app supplies its registered app identity through the required Referer header. The app does not extract stream URLs, bypass the official player, block ads, or download YouTube content.

**API-key note:** a key packaged in an Android app can be extracted from the APK. Restrict it in Google Cloud, monitor quota, and do not treat BuildConfig as secret storage. The CI workflow intentionally builds without the YouTube key unless a future secure release process is deliberately configured.

References:
- [YouTube Data API](https://developers.google.com/youtube/v3)
- [YouTube embedded player and minimum functionality](https://developers.google.com/youtube/terms/required-minimum-functionality)
- [Made for Kids status](https://developers.google.com/youtube/v3/guides/made_for_kids_status)
- [YouTube API developer policies](https://developers.google.com/youtube/terms/developer-policies)

## Shared implementation

All providers implement `MediaProvider` and declare their capabilities. Search runs on the IO dispatcher through a bounded JSON HTTP client with explicit connect/read timeouts and response-size limits. The Explore screen fans out searches to the selected providers and keeps partial results when one provider fails.

Downloads remain deliberately unsupported until a source-specific permission model, file selection rules, and cancellation/resume handling are implemented.
