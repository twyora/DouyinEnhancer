# DouyinEnhancer

[![GitHub](https://img.shields.io/github/license/twyora/DouyinEnhancer)](https://github.com/twyora/DouyinEnhancer/blob/main/LICENSE)
![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/twyora/DouyinEnhancer)

[English](README.md) | [简体中文](README_zh_CN.md)

A small Xposed module that adds quality-of-life features to Douyin

> This project is still in its early stages. Features are being added gradually — feedback and
> suggestions are welcome

## Tested Environment

> Douyin v37.6.0 (last tested on module v0.6.0)
>
> Douyin v38.8.0
>
> Douyin v40.2.0 (Not tested by maintainer)
---
> Other versions are untested with no guaranteed functionality. Please report issues or submit PRs
> for any
> problems

## Features

- Download comment images without watermark
- Save comment emojis to album
- ~~Video filtering~~ Recommended feed filtering
- Save playing content to album
- Save audio comments from comment section
- Bypass Listen Aweme mode copyright restrictions
- Disable double-tap to like in feed
- Open comment panel by double-tap in feed
- Keep danmaku visible when entering clean mode (After enabling this, some scenarios in clear‑screen
  mode may unexpectedly make unrelated components visible)
- Automatically pause video on playback completion
- Block auto video resumption on foreground return
- Block specific playback‑page components

---
See more about future development: [PM.md](PM.md)

## Usage

1. Activate the module in your Xposed manager (e.g. LSPosed)
2. Set the scope to Douyin (`com.ss.android.ugc.aweme`)
3. Restart Douyin

## Disclaimer

- This project is for personal learning and technical exchange only. Please do not use it for
  commercial purposes.
- Any consequences arising from the use of this module are at
  the user's own risk.

## Credits

> In no particular order

- [BiliRoaming](https://github.com/yujincheng08/BiliRoaming)
- [DexKit](https://github.com/LuckyPray/DexKit)
- [gif.kt](https://github.com/shaksternano/gif.kt)
- [Gropify](https://github.com/HighCapable/Gropify)
- [KavaRef](https://github.com/HighCapable/KavaRef)
- [RestoreSplashScreen](https://github.com/GSWXXN/RestoreSplashScreen)
- [YukiHookAPI](https://github.com/HighCapable/YukiHookAPI)
