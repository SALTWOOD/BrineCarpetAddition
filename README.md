<div align="center">

![BrineCarpetAddition](https://socialify.git.ci/SALTWOOD/BrineCarpetAddition/image?description=1&font=Inter&forks=1&issues=1&language=1&name=1&owner=1&pattern=Plus&pulls=1&stargazers=1&theme=Auto)

# BrineCarpetAddition
✨ **地毯模组的又一款拓展模组** ✨

<a href="https://github.com/SALTWOOD/BrineCarpetAddition/stargazers"><img src="https://img.shields.io/github/stars/SALTWOOD/BrineCarpetAddition?color=ffcb47&label=Stars&logo=github&style=flat-square"></a>
<a href="https://github.com/SALTWOOD/BrineCarpetAddition/network/members"><img src="https://img.shields.io/github/forks/SALTWOOD/BrineCarpetAddition?color=2ea44f&label=Forks&logo=github&style=flat-square"></a>
<a href="https://github.com/SALTWOOD/BrineCarpetAddition/releases"><img src="https://img.shields.io/github/v/release/SALTWOOD/BrineCarpetAddition?color=6875f5&label=Latest%20Release&logo=githubrelease&style=flat-square"></a>
<a href="https://github.com/SALTWOOD/BrineCarpetAddition/blob/master/LICENSE"><img src="https://img.shields.io/github/license/SALTWOOD/BrineCarpetAddition?color=00a3cc&label=License&style=flat-square"></a>
</div>

> [!NOTE]
> 我做这款模组是因为 [MasaGadget](https://github.com/plusls/MasaGadget) 中「同步实体数据」的功能所依赖的 [PluslsCarpetAddition](https://github.com/plusls/plusls-carpet-addition) 暂未更新至 1.21.1 版本，并且我需要它的墓碑功能。
> PluslsCarpetAddition 使用用 CC0 协议，意味着作者放弃了代码的相关著作权。
> 因此我在阅读源码后，基于其原有逻辑与部分代码，移植并实现了其中的部分功能。

> [!TIP]
> 新功能请求请前往 [Issues](https://github.com/SALTWOOD/BrineCarpetAddition/issues) 反馈

## 功能列表
本模组所有功能可通过 `/carpet` 命令开关

### 协议
- **PCA 同步协议兼容 (`pcaProtocolEnabled`)**
  启用 PCA 的实体/方块同步协议（兼容模式）。
- **玩家实体同步权限配置 (`syncPlayer`)**
  搭配同步协议使用，可自定义允许同步的玩家实体范围，可选配置：
  ✔ `NOBODY` - 不同步任何玩家实体
  ✔ `BOT` - 仅同步机器人玩家实体
  ✔ `OPS` - 仅同步管理员与机器人玩家实体
  ✔ `OPS_AND_SELF` - 同步管理员、机器人与自身玩家实体
  ✔ `EVERYONE` - 同步所有玩家实体

### 功能
- **解除铁砧等级上限 (`avoidAnvilTooExpensive`)**
  附魔/修复时不再因等级超过 40 级而过于昂贵无法进行操作。
- **死亡墓碑 (`deathskull`)**
  玩家死亡时会在原地生成头颅墓碑，保留死亡时的全部背包物品和一半经验。
- **绿宝石吸引村民 (`emeraldAttractsVillager`)**
  手持绿宝石或绿宝石块时，周围的村民会主动向你靠近；手持绿宝石块时，村民的靠近速度会更快。
- **白日梦 (`daydream`)**
  允许玩家在白天睡觉
