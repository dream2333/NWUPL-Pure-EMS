# NWUPL_Pure_EMS 政法轻教务

[![License: GPLv3+](https://img.shields.io/badge/License-GPLv3%2B-blue)](https://www.gnu.org/licenses/gpl-3.0.html)  ![GitHub Release Date](https://img.shields.io/github/release-date/dream2333/NWUPL_Pure_EMS) 

[蓝奏云下载地址](https://www.lanzouw.com/iB61xo1uhah "下载地址") / [github下载地址](https://github.com/dream2333/NWUPL_Pure_EMS/releases/download/v0.2.5/app-release.apk "下载地址") / [release](https://github.com/dream2333/NWUPL_Pure_EMS/releases "下载地址")

个人开发的一款西北政法大学教务平台客户端，遵循Material Design标准，使用kotlin+jetpack+mvvm架构，查询流量消耗为网页端的1/100。可以使用西北政法大学教务端学号及密码进行登录，能够替代西北政法大学网页端教务平台的部分功能，实现教务的便捷查询。

使用学号+身份证后六位登录

## 已实现功能

- cas单点登录
- 学年课程表查询删除
- 学年成绩、绩点查询查询
- 学籍信息查询
- 桌面课程表部件

<img src="https://www.hualigs.cn/image/6076c0de0981d.jpg" height="330" />   <img src="https://www.hualigs.cn/image/6076c0de96773.jpg" height="330" />  <img src="https://www.hualigs.cn/image/6076c0de52283.jpg" height="330"  />   <img src="https://www.hualigs.cn/image/6076c0dcb5c37.jpg" height="330"  />  <img src="https://www.hualigs.cn/image/6076bca38517b.jpg" height="330"  /> 

## 数维登录请求流程

[教务接口说明](https://github.com/dream2333/NWUPL_Pure_EMS/blob/master/%E6%8E%A5%E5%8F%A3.md "接口文档") 

- 获取jessionid

- 获取真实登陆url

- 从真实登陆url处获取response

- 从response中获取cookies和form

- 使用cookies获取验证码图片

- 从图片构建真实登录表单

- 使用真实登录表单和cookies登陆

- 请求接口/eams/homeExt.action

- 需要返回内容：1.cookies 2.form 3.bitmap（验证码）

## 相关仓库

- [Jetpack-MVVM-Best-Practice](https://github.com/KunMinX/Jetpack-MVVM-Best-Practice) — 难得一见的 Jetpack MVVM 最佳实践
- [ScheduleX](https://github.com/Surine/ScheduleX) — 遵循MaterialDesign的开源课程表
- [Art of Readme](https://github.com/noffle/art-of-readme) — 💌 写高质量 README 的艺术。

## 未来功能
- 情侣课程表
- 选课系统
- 教师评价
- 学分认定
- 教学计划查看

## 维护者

[@dream2333](https://github.com/dream2333)

## 如何贡献

非常欢迎你的加入！[提一个 Issue](https://github.com/dream2333/NWUPL_Pure_EMS/issues/new) 或者提交一个 Pull Request。


标准 Readme 遵循 [Contributor Covenant](http://contributor-covenant.org/version/1/3/0/) 行为规范。


## 使用许可

[GPL3.0](LICENSE) © dream2333
