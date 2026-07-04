# DouyinEnhancer

[![GitHub](https://img.shields.io/github/license/twyora/DouyinEnhancer)](https://github.com/twyora/DouyinEnhancer/blob/main/LICENSE)
![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/twyora/DouyinEnhancer)

[English](README.md) | [简体中文](README_zh_CN.md)

A small Xposed module that adds quality-of-life features to Douyin.

> This project is still in its early stages. Features are being added gradually — feedback and suggestions are welcome.

## Tested Environment

> Douyin 37.6.0

## Features

- Download comment images without watermark
- Save comment emojis to album

## TODO

- ~~Video filtering~~
- [x] Recommended feed filtering
- Save playing content to album
- Save favorited emojis to album
- [x] Module settings UI
- Clear-screen playback without hiding danmaku
- [x] Cache obfuscated method lookup results for faster host app startup
- Save audio comments from comment section
- Remove copyright restriction of Douyin Audio Mode
- Backup and restore module settings

## Usage

1. Activate the module in your Xposed manager (e.g. LSPosed)
2. Set the scope to Douyin (`com.ss.android.ugc.aweme`)
3. Restart Douyin

## Disclaimer

- This project is for personal learning and technical exchange only. Please do not use it for commercial purposes.
- Any consequences arising from the use of this module (e.g., account suspension, data loss) are at the user's own risk.

## Credits

- [YukiHookAPI](https://github.com/HighCapable/YukiHookAPI) as the Xposed module development framework
- [DexKit](https://github.com/LuckyPray/DexKit) for dynamic obfuscated method lookup
- [KavaRef](https://github.com/HighCapable/KavaRef) for reflection API support
- [Gropify](https://github.com/HighCapable/Gropify) for managing build configuration
- [gif.kt](https://github.com/shaksternano/gif.kt) for GIF encoding and decoding
- [RestoreSplashScreen](https://github.com/GSWXXN/RestoreSplashScreen) for being the core inspiration for this project, with deep references in build configurations, hook logic organization, Android CI setups, and so on.
- Thanks to [BiliRoaming](https://github.com/yujincheng08/BiliRoaming) for providing the approach to caching obfuscated method lookup results