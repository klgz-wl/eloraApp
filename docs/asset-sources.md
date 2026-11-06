# Asset Sources

## `hero_portrait.webp`

- Location: `app/src/main/res/drawable-nodpi/hero_portrait.webp`
- Source: [Facee AI portrait](https://facee.ru/look/zhenschina-derzhit-pered-soboy-buket-roz-raznyh-ottenkov-rozovogo-ee-glaza-siyayut-ot-radosti-pozadi-legkie-pastelnye-to-se4cl/)
- Purpose: Figma prototype placeholder.
- Release status: The redistribution and model-release rights have not been verified. Replace this file with an owned or explicitly licensed production asset before any public or commercial release.

## Elora Logo and launcher icons

- Original: `app/src/main/res/drawable-nodpi/elora_logo.png` (1280 × 1280 PNG, original Figma image bytes).
- Source: [万理 elora, selected layer 22:23](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=22-23).
- Authorization: the project owner selected this layer as Elora's logo. No independent third-party license or ownership evidence was supplied; this record does not establish broader redistribution rights.
- Usage: login choice/account brand image; launcher icons in `mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}`. The adaptive foreground fits the full square image into the central 72 dp of a 108 dp layer to protect the artwork and wordmark under launcher masks; legacy icons use 48 dp and round icons apply a circular mask.
- The original includes its dark background and Elora wordmark; preserve these when deriving assets. The historical `zorv_logo_wordmark.png` is unused and is not the Elora brand source.

## Elora login welcome page

- Design: [selected welcome page 1:2040](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-2040).
- `drawable/elora_login_device.xml` and `drawable/elora_login_account.xml`: the exact SVG path/stroke geometry exported for nodes `1:2103` and `1:2108`, mechanically converted to Android VectorDrawable. No redrawn glyphs or runtime Figma URLs.
- The hero reuses the original Elora logo above with the design's circular clip and glass overlay. Device status/navigation bars are provided by Android.
- These assets have the same owner-selected design provenance and release-rights evidence limitations as the logo.

## Elora account/password login page

- Design: [selected account page 1:2119](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-2119).
- `drawable/elora_account_{back,user,lock,eye,arrow}.xml`: original exported SVG path/stroke geometry for nodes `1:2146`, `1:2180`, `1:2194`, `1:2199`, `1:2208`, mechanically converted to VectorDrawable; source color names converted to Android hex colors and back stroke opacity retained.
- The circular glass logo is shared with the welcome page. Native Android status bars and safe/IME insets replace mock iOS device chrome.
- Same owner-selected Figma design provenance and release-rights evidence limitations as the original logo. Caption uses Elora instead of the design's stale `nexor` brand.

## Elora video template catalog

- Design: [Figma video template page 1:1404](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-1404).
- Original exported icons: `docs/design-assets/elora-video/*.svg`. The corresponding `drawable-nodpi/elora_catalog_*.png` files are rasterizations of those exact exports at 72 px width, retaining original shape, gradients and filters. No custom icon redraws.
- Six original design images in `app/src/androidTest/assets/elora-video/` are local deterministic test fixtures only, excluded from the production app. They cover male editorial, fragrance, fire VFX, roses, portrait editing and cyberpunk visuals. As owner-selected Figma assets, they have no independently verified third-party/model/redistribution license evidence; do not treat them as licensed production catalog content.
- Production cover images, titles, durations, prices, categories and balance come from existing backend data. Missing covers show a title placeholder; VOICE and NEW badges are omitted because the current model has no corresponding audio/publication evidence. HOT uses the actual positive hot score.

## Elora image template catalog

- Design: [Figma image template layout 59:924](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=59-924).
- Shares the original catalog icons/background with the video page. `docs/design-assets/elora-video/{video-inactive,image-active}.svg` and the corresponding `drawable-nodpi/elora_catalog_{video_inactive,image_active}.png` come from selected nodes `59:1176` and `59:1171`; PNGs preserve exported SVG rendering at 72 px width.
- Production uses real image catalog covers/categories. The screenshot test reuses the Figma image fixtures documented above, excluded from production, and does not establish production content rights. Design placeholder names/ages and NEW badges are not fabricated where backend fields do not exist.

## Elora featured home feed

- Design: [Figma home video feed 1:2](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-2).
- `docs/design-assets/elora-video/{home-active,use-template}.svg` are the original exported active-home glyph and use-template glyph; corresponding `drawable-nodpi/elora_catalog_{home_active,use_template}.png` retain their rendering at 72 px width. Existing owner-selected Figma provenance and licensing evidence limitations apply.
- Production home videos are queried using the backend's `home_featured=true`, with no static design video used as production data. UI swipe screenshots reuse existing local image fixtures; they verify layout and paging, not remote video download/decoding.

## Elora template preview before local photo selection

- Design: [Figma preview stage 1:93](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-93).
- `docs/design-assets/elora-preview/{info,upload}.svg` preserve the original exported information and cloud-upload icons (`1:123`, `1:129`); `drawable-nodpi/elora_preview_{info,upload}.png` are exact rasterizations at 72 px width. The matching existing exported back glyph is reused.
- `app/src/androidTest/assets/elora-preview/portrait.png` is the design's original preview image, used only for deterministic UI screenshots and tests, excluded from production. Same owner-selected Figma provenance applies; independent third-party/model/redistribution release evidence has not been supplied.
- Production media and the page title come from the selected template. Preserve the existing Fit image/video behavior so full template content remains visible; do not use the temporary Figma URLs at runtime.

## Elora photo selection bottom sheet

- Design: [Figma 78:1828](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=78-1828).
- Original exports: `docs/design-assets/elora-picker/{close,options,add,check}.svg`, from sheet nodes `78:1878`, `78:1887`, `78:1898`, `78:1907`. `drawable-nodpi/elora_picker_*.png` rasterize the exact SVGs at 72 px width. The add-tile dashed border is ordinary Compose decoration.
- Production thumbnails use only URIs actually selected in the current upload workflow; no design screenshot is inserted into the user's Recent photos. Screenshot tests reuse the already documented Figma fixtures, excluded from production.
- The existing owner-selected Figma provenance and independent release-rights limitations apply to these design exports.

## Elora rendering mode selection

- Design: [Figma uploaded-photo mode page 1:505](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-505).
- `docs/design-assets/elora-mode/{check,sparkle,token,fast,quality,selected}.svg` preserve original exported icons. `drawable-nodpi/elora_mode_*.png` are their exact 72 px-width rasterizations; existing Figma-derived back glyph is reused.
- Production comparison cards show the selected local photo and the chosen template media, labelled accordingly. They are not pre-generated enhanced results. UI screenshots reuse the documented portrait fixture, excluded from production. The earlier Figma ownership/release-rights evidence limitations apply.
- Prices and supported fast/quality modes are read from backend template prices; the fixed design price is never used as a production quote.

## Elora report confirmation sheet

- Design: [Figma report prototype 1:257](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-257).
- `docs/design-assets/elora-report/{close,flag}.svg` preserve exported close and report-flag icons (`1:309`, `1:325`); `drawable-nodpi/elora_report_{close,flag}.png` rasterize them at 72 px width without redrawing.
- Screenshot background reuses the documented preview fixture, excluded from production. The same owner-selected Figma provenance and independent release-rights limitations apply.

## Elora Me creations page

- Design: [Figma Me 1:1766](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-1766). The user separately supplied the exact empty-state text and Create Now appearance.
- `docs/design-assets/elora-me/{settings,purchase,download,play,profile-active}.svg` are original exported glyphs; corresponding `drawable-nodpi/elora_me_*.png` preserve their rendering at 72 px width. Existing catalog star icon is reused where it matches the design.
- Production profile, balance and generated-resource URLs come from existing APIs. Design avatars and sample creations are not substituted for real user data. UI screenshots reuse previously documented test fixtures only, excluded from production; ownership/release evidence limitations remain unchanged.
- The user later requested the 72-hour notice to be displayed. `docs/design-assets/elora-me/privacy-clock.svg` preserves the exported clock from node 1:1842 (banner 1:1839); `drawable-nodpi/elora_me_privacy_clock.png` rasterizes it at 72 px width. This is a display-only notice; no client deletion operation is introduced.

## Elora credits purchase page

- Design: [Figma recharge 59:410](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=59-410).
- `docs/design-assets/elora-credits/{back,token,history,check,selected-token,standard-token,premium-token,arrow}.svg` preserve the original exports. Corresponding `drawable-nodpi/elora_credits_*.png` rasterize them at 72 px width; colors and layout are native Compose UI.
- Package counts, prices, bonuses, discounts and recommendation tags in production use StoreCatalog data. The six-package screenshot is test fixture data, excluded from production; it does not demonstrate real Google Play/provider prices or a completed charge. The existing Figma ownership and independent release-rights evidence limitations apply.

## Elora settings page

- Design: [Figma Settings 1:1662](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-1662).
- `docs/design-assets/elora-settings/{back,privacy,chevron,terms,about,contact,signout}.svg` preserve exact Figma exports. `drawable-nodpi/elora_settings_*.png` rasterize these exports at 72 px width; the Android system supplies status/navigation bars. The existing Figma ownership and independent release-rights limitations apply.
- The stale design brand name is replaced with Elora. Privacy/terms URLs, version and contact email come from existing per-flavor BuildConfig values; the design's sample email and version are not production data. No new photographic assets are introduced.

## Elora wallet Record page

- Design: [Figma Record and help sheet 1:827](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=1-827).
- `docs/design-assets/elora-record/{video,credit,back,help,close,check}.svg` preserve exact exported glyphs; corresponding `drawable-nodpi/elora_record_*.png` rasterize them at 72 px width. The existing catalog image glyph is reused for image-specific records. Existing owner-selected Figma provenance and independent release-rights limitations apply.
- Transactions in production are returned by the wallet API. Screenshots use deterministic local fixtures, not evidence of actual payment/consumption/refund. The help text is the requested prototype notice with spacing/grammar normalized; no automated refund feature is added. No photographic assets are introduced.

## Elora Me creation viewer

- Design: [Figma Me media viewer 59:1670](https://www.figma.com/design/CpV8PTHxU293dAGBeENzJD?node-id=59-1670). `docs/design-assets/elora-viewer/{back,play-circle,play}.svg` preserve exact exports; corresponding `drawable-nodpi/elora_viewer_*.png` are 160 px-wide rasterizations. Existing Figma provenance/independent release-rights limitations apply.
- Production media URLs come from the selected generated resource. The design's sample portrait and iOS status artwork are not inserted as user content; Android renders system bars.
- Image UI tests reuse the already documented Figma male fixture. `app/src/androidTest/assets/elora-viewer/pattern.mp4` is an original synthetic 360×640, 24fps, 3-second color test pattern, generated using FFmpeg `-f lavfi -i testsrc2=size=360x640:rate=24 -t 3 -c:v libx264 -pix_fmt yuv420p -an`. It is included only in the test APK. The temporary FFmpeg runtime is outside the repository; no application dependency was added.

- 2026-11-05 举报入口按用户明确指定复用已记录的 `elora_preview_info.png` 圆形i资产；`elora_report_flag.png`仅用于举报弹层内的Report按钮，不作为媒体入口图标。
