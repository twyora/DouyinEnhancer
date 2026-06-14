# DouyinEnhancer

[![GitHub](https://img.shields.io/github/license/twyora/DouyinEnhancer)](https://github.com/twyora/DouyinEnhancer/blob/main/LICENSE)
![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/twyora/DouyinEnhancer)

[English](README.md) | [简体中文](README_zh_CN.md)

A small Xposed module that adds quality-of-life features to Douyin.

> This project is still in its early stages. Features are being added gradually — feedback and suggestions are welcome.

## Features

1. Download comment images without watermark
2. Save comment emojis to album

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