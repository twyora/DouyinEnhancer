# 抖柚

[![GitHub](https://img.shields.io/github/license/twyora/DouyinEnhancer)](https://github.com/twyora/DouyinEnhancer/blob/main/LICENSE)
![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/twyora/DouyinEnhancer)

[English](README.md) | [简体中文](README_zh_CN.md)

为抖音添加一些优化体验的小功能。

> 项目仍处于早期阶段，功能还在逐步完善中，欢迎反馈和建议。

## 模块功能

- 评论区图片无水印下载
- 评论区表情包保存到相册

## 计划

- 视频过滤
- 播放内容保存到相册
- 已收藏表情保存到相册
- 模块设置页面
- 缓存被混淆方法的查找结果，提升宿主启动速度

## 使用方法

1. 在 Xposed 管理器（如 LSPosed）中激活模块
2. 作用域勾选抖音（`com.ss.android.ugc.aweme`）
3. 重启抖音

## 免责声明

- 本项目仅供个人学习与技术交流使用，请勿用于商业用途。
- 使用本模块产生的一切后果（如账号异常、数据丢失等）均由使用者自行承担。

## 致谢

- 使用 [YukiHookAPI](https://github.com/HighCapable/YukiHookAPI) 作为 Xposed 模块开发框架
- 使用 [DexKit](https://github.com/LuckyPray/DexKit) 进行混淆方法的动态搜索
- 使用 [KavaRef](https://github.com/HighCapable/KavaLib) 提供反射 API 支持
- 使用 [Gropify](https://github.com/HighCapable/Gropify) 管理项目构建配置
- 使用 [gif.kt](https://github.com/shaksternano/gif.kt) 处理 GIF 动图的编码与解码
- [RestoreSplashScreen](https://github.com/GSWXXN/RestoreSplashScreen) 是本人初次接触 Xposed 时的启蒙模块，第一次让我领略到 Xposed 的强大，也因此成为后来开发本项目的“白月光”与灵感源泉。本项目在构建配置、Hook 代码组织、Android CI 搭建等方面大量借鉴了[迷璐](https://github.com/GSWXXN)的设计思路。