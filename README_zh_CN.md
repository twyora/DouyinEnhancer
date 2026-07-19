# 抖柚

[![GitHub](https://img.shields.io/github/license/twyora/DouyinEnhancer)](https://github.com/twyora/DouyinEnhancer/blob/main/LICENSE)
![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/twyora/DouyinEnhancer)

[English](README.md) | [简体中文](README_zh_CN.md)

为抖音添加一些优化体验的小功能

> 项目仍处于早期阶段，功能还在逐步完善中，欢迎反馈和建议

## 测试环境

> 抖音 37.6.0
---
> 其它版本未经验证，不保证功能可用性，若存在问题欢迎提交 Issue 或 PR

## 功能

- 评论区图片无水印下载
- 评论区表情包保存到相册
- ~~视频过滤~~ 推荐流过滤
- 播放内容保存到相册（暂不支持受限制内容下载）
- 保存评论区语音评论

---
后续功能开发详见：[PM.md](PM.md)

## 使用方法

1. 在 Xposed 管理器（如 LSPosed）中激活模块
2. 作用域勾选抖音（`com.ss.android.ugc.aweme`）
3. 重启抖音

## 免责声明

- 本项目仅供个人学习与技术交流使用，请勿用于商业用途
- 使用本模块产生的一切后果均由使用者自行承担

## 致谢

> 排名不分先后

- [BiliRoaming](https://github.com/yujincheng08/BiliRoaming)
- [DexKit](https://github.com/LuckyPray/DexKit)
- [gif.kt](https://github.com/shaksternano/gif.kt)
- [Gropify](https://github.com/HighCapable/Gropify)
- [KavaRef](https://github.com/HighCapable/KavaRef)
- [RestoreSplashScreen](https://github.com/GSWXXN/RestoreSplashScreen)
- [YukiHookAPI](https://github.com/HighCapable/YukiHookAPI)
