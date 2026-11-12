# Let You Despawn

NeoForge 1.21.1 · `LGPL-3.0-or-later` · by **LvDriver** · mod id `letyoudespawn`

---

## 中文

### 这是什么

**Let You Despawn** 是 **Let Me Despawn 1.26.11.1**（作者 frikinjay）的 NeoForge 1.21.1 **独立重建版**。

上游把配置与部分工具方法放在 **Almanac** 库里（另需 **DungeonConfig**）。本版本为减少前置依赖，把这些能力内联进本体，行为逻辑沿用原作者的成果。

需要说明的是：Almanac 为了处理「物品带着空 NBT 组件」这类边界情况，在 `ItemStack.getCount()`、`Slot.getItem()` 等高频方法上做了注入，每次调用都会复制一份 NBT。物品栏同步、掉落物合并与界面渲染都会走到这些方法，在大型整合包里这部分开销是可观测的。本版本没有沿用该实现。

本版本把它们全部收进本体：

- **不需要 Almanac**，也不需要 DungeonConfig
- **Fzzy Config 是可选前置**：装了就有游戏内配置界面，不装也能正常跑，改文件即可

### 功能

- 生物捡起物品后**不再被钉成永久存在**，仍会按原版规则消失
- 捡过物品的生物消失时，把它**自己捡到的那几件**装备丢出来（按装备槽位记录，不是笼统一个标记）
- 自定义名的生物保持永久；`mobNames` 名单里的生物同样豁免本 Mod
- `persistenceEnablers` 里的物品被生物捡起并穿上后，该生物消失时会被拦住
- **普通刷怪笼**：即使有东西想让它永久存在，来自刷怪笼的怪仍可消失（可开关）
- **试炼刷怪笼**：只要刷怪笼还在统计这只生物，它就不会消失——挂机等怪消失拿奖励的路子被堵死（可开关）
- 抹掉末影人"搬着方块就永久存在"的原版行为，且它消失时方块会掉落
- 发射器给怪穿装备也走同一套规则
- 生物来源、试炼刷怪笼坐标、捡到的槽位都写进 NBT，跨存档与重启有效

### 命令

全部需要权限等级 **2（op）**，修改后立即写盘，并同步到 Fzzy Config 的 TOML。

| 命令 | 作用 |
|---|---|
| `/letyoudespawn add <实体id>` | 把生物加入「保持永久」名单，例如 `/letyoudespawn add minecraft:zombie` |
| `/letyoudespawn remove <实体id>` | 从名单里移出 |
| `/letyoudespawn addPersistenceEnabler <物品id>` | 把物品加入「阻止消失」名单，例如 `/letyoudespawn addPersistenceEnabler minecraft:name_tag` |
| `/letyoudespawn removePersistenceEnabler <物品id>` | 从名单里移出 |
| `/letyoudespawn standardSpawnerDespawn <true\|false>` | 开关：刷怪笼生成的生物能否消失 |
| `/letyoudespawn trialSpawnerDespawn <true\|false>` | 开关：试炼刷怪笼生成的生物能否消失 |
| `/letyoudespawn reload` | 重新读取配置文件并立即生效 |

- 实体/物品 id 参数带**自动补全**（输入时按 Tab），并按注册表校验，写错会被拒绝
- 控制台执行同样有效

### 配置

四个选项，两种载体，按是否安装 Fzzy Config 自动选择：

| 装了 Fzzy Config | 没装 |
|---|---|
| `config/letyoudespawn/config.toml` 为权威，可在游戏内编辑 | `config/letyoudespawn.json` 为权威，手改即可 |
| 每次保存同时镜像一份 `config/letyoudespawn.json` | — |
| 首次启动若 TOML 不存在，会用 JSON 里的现有值初始化 | — |

| 键 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `mobNames` | 字符串列表 | `["corpse:corpse"]` | 这些生物捡东西后**照旧**变成永久存在 |
| `persistenceEnablers` | 字符串列表 | `[]` | 捡到并穿上这些物品的生物不会被消失 |
| `enableStandardSpawnerDespawn` | 布尔 | `true` | 刷怪笼生成的生物可消失 |
| `enableTrialSpawnerDespawn` | 布尔 | `true` | 试炼刷怪笼生成的生物可消失 |

游戏内路径：**Mods → Let You Despawn → Config**。列表项可以用界面里的「浏览并选择...」按钮打开可搜索的勾选列表，不必手打 id。

### 环境要求

| | |
|---|---|
| Minecraft | **1.21.1** |
| NeoForge | **21.1.218+**（21.1.x 全部适用） |
| 可选前置 | **Fzzy Config 0.7.0+**（仅用于配置界面） |
| Java | 21 |

### 安装

把 jar 放进 `mods/`。装上 Fzzy Config 就会多出游戏内配置界面，不装也能正常使用。

### 从源码构建

```bash
./gradlew build
```

产物：`build/libs/letyoudespawn-1.0.0.jar`

`libs/` 里的三个 jar 只用于编译和开发运行，不会打进产物：

| 文件 | 用途 |
|---|---|
| `fzzy_config-0.7.7+fix1+1.21+neoforge.jar` | `compileOnly` + `localRuntime` |
| `kotlinforforge-5.12.0-all.jar` | 仅 `localRuntime`（Fzzy Config 的运行期依赖） |
| `kotlin-stdlib-2.0.21.jar` | `compileOnly`（javac 读 Kotlin 签名） |

开发运行：`./gradlew runClient` / `./gradlew runServer`

### 与上游 1.26.11.1 的差异

| 项目 | 说明 |
|---|---|
| 前置 | 不再需要 Almanac、DungeonConfig；Fzzy Config 可选 |
| 配置框架 | DungeonConfig → 自建 JSON + 可选 Fzzy Config（键名与语义一致） |
| 配置文件 | `config/letyoudespawn.json` / `config/letyoudespawn/config.toml` |
| 命令 | 前缀 `/letyoudespawn`；新增 `reload`；权限 3 → 2 |
| 访问 `Mob#persistenceRequired` | 用 `@Accessor`，不再需要 access transformer |
| 打包 | 纯 NeoForge，移除 Architectury 多加载器壳 |
| 依赖声明 | NeoForge `[21.1.218,)`，Minecraft `[1.21.1,1.21.2)` |

### 致谢与许可

- 原模组：**Let Me Despawn**，作者 **frikinjay** — <https://github.com/frikinjay/let-me-despawn>（LGPL-3.0）
- 配置界面：**Fzzy Config**，作者 **fzzyhmstrs**
- 许可：**GNU Lesser General Public License v3.0 or later**，见 [LICENSE](LICENSE)

本作品是上游 LGPL-3.0 代码的衍生作品，仓库内附完整源码。

---

## English

**Let You Despawn** is a **standalone NeoForge 1.21.1 rebuild** of **Let Me Despawn 1.26.11.1** by frikinjay.

Upstream keeps its config and a few helpers in the **Almanac** library (and **DungeonConfig**). This build folds them into the mod itself so the pack needs one dependency fewer; the behaviour is the original author's work.

For the record, that dependency has a cost: to handle an edge case where an item carries an empty NBT component, Almanac injects into hot methods such as `ItemStack.getCount()` and `Slot.getItem()` and copies NBT on each call. Inventory sync, item merging and rendering all pass through them. This build does not carry that implementation.

This build folds everything back into one mod:

- **No Almanac**, no DungeonConfig
- **Fzzy Config is optional**: with it you get an in-game config screen, without it the files in `config/` still work

### Features

- Mobs that pick items up are **no longer pinned as persistent**
- They drop **only the gear they picked up** (tracked per equipment slot)
- Custom-named mobs stay persistent; `mobNames` opts mobs out of the mod
- `persistenceEnablers` items keep a mob alive once picked up and worn
- **Monster spawners**: their mobs can despawn even if something tries to make them persistent (toggle)
- **Trial spawners**: a mob still counted by its spawner never despawns, which stops idle reward farming (toggle)
- Endermen no longer become persistent for carrying a block, and drop it when they despawn
- Dispensers equipping mobs follow the same rules
- Spawn origin, trial-spawner position and picked slots are stored in NBT

### Commands

Permission level **2** (op). Every change is written to disk immediately and mirrored into the Fzzy TOML.

| Command | Effect |
|---|---|
| `/letyoudespawn add <entity id>` | Add a mob to the persistent list |
| `/letyoudespawn remove <entity id>` | Remove it |
| `/letyoudespawn addPersistenceEnabler <item id>` | Add an item to the "prevents despawning" list |
| `/letyoudespawn removePersistenceEnabler <item id>` | Remove it |
| `/letyoudespawn standardSpawnerDespawn <true\|false>` | Allow spawner mobs to despawn |
| `/letyoudespawn trialSpawnerDespawn <true\|false>` | Allow trial spawner mobs to despawn |
| `/letyoudespawn reload` | Re-read the config file and apply it |

Id arguments are tab-completed and validated against the registry; unknown ids are rejected.

### Configuration

| Key | Type | Default | Meaning |
|---|---|---|---|
| `mobNames` | string list | `["corpse:corpse"]` | Mobs that still become persistent after a pickup |
| `persistenceEnablers` | string list | `[]` | Items that keep a mob alive once picked up and worn |
| `enableStandardSpawnerDespawn` | boolean | `true` | Spawner mobs may despawn |
| `enableTrialSpawnerDespawn` | boolean | `true` | Trial spawner mobs may despawn |

With Fzzy Config installed, `config/letyoudespawn/config.toml` is authoritative and a **「浏览并选择...」/「Browse and pick...」** button opens a searchable picker for both lists. Without it, edit `config/letyoudespawn.json`.

### Requirements

Minecraft **1.21.1**, NeoForge **21.1.218+**, Java 21. Fzzy Config 0.7.0+ is optional.

### Building

```bash
./gradlew build
```

### Licence and credits

Upstream: **Let Me Despawn** by **frikinjay** (LGPL-3.0) — <https://github.com/frikinjay/let-me-despawn>. Config screen: **Fzzy Config** by **fzzyhmstrs**.
Released under **LGPL-3.0-or-later**; full source is in this repository.