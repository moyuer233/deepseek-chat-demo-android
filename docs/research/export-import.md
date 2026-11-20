# DeepSeek 官方导出包 → chat-demo 历史导入兼容性调研

> 调研日期：2026-11-20 ｜ 范围：只读代码 + 公开网络证据 ｜ 未改 `src/`、未跑构建
> 目标：判断 chat.deepseek.com「导出数据」产出的 ZIP 能否直接交给本仓库 `src/services/chat/history/importArchive.ts` 导入。

## 0. 结论（先看这里）

| 层面 | 结论 | 依据 |
|---|---|---|
| **格式层** | **直接兼容，零字段映射。** 官方导出与 chat-demo 导入器吃的是同一套 `export_format_version: 3` 格式：同样的条目名、同样的会话/消息/片段字段。 | DeepSeek 官方文档写明 chat-demo 可导入 DeepSeek Chat 导出的聊天记录；DeepSeek 自家 DSH 导入插件读取同版本、同条目名、同字段（§2）。 |
| **实现层** | **不能“零改动直接上线”，建议先做 6 处防御性适配**（放宽校验/给默认值，**不是字段转换**）。当前 schema 比官方格式更严，且官方 v3 存在“不升版本号加字段/减字段”的漂移。 | `schema.ts` 对消息级字段、`current_message_id`、`FILE` 字段是硬要求；DSH 的 zod 已把其中若干字段标为可选（§3.2）。 |
| **证据缺口** | **缺一份真实导出 ZIP 的字节级实证。** 公开网络只找到结构级证据，没有找到可下载的真实 `chat_sessions/*.json` 样本。 | §2.5、§4。 |

一句话：**格式同源，所以不需要写字段映射；但现有导入器是“严格解析器”，官方包是“会漂移的格式”，上线前应把它改造成“兼容解析器”，并用一份真实包做回归。**

---

## 1. 导入器对归档的字段要求

### 1.1 归档（ZIP）层

官方顺序与本地期望（[README.md](../../src/services/chat/history/README.md#L7-L16)、[paths.ts](../../src/services/chat/history/paths.ts#L1-L14)）：

```text
manifest.json                                       必须是 ZIP 的第一个“文件条目”
user.json                                           可选；只能在 manifest 之后、任何会话/索引之前
chat_sessions/{session_id}.json                     全部会话 JSON
chat_session_index.json                             索引；在所有会话之后（也可在图片之后）
chat_sessions/{session_id}_files/{file_id}_{file_name}   图片原件（可紧跟各自会话，也可在 index 之后）
```

| 规则 | 要求 | 代码 |
|---|---|---|
| 首条目 | `checkArchiveFormat` 只读第一个产出条目，路径必须**精确等于** `manifest.json`；否则 `backupUnrecognized`（“无法识别该文件”）。目录条目在 `readArchive` 中被跳过，所以“第一个文件条目”才是判定对象。 | [importArchive.ts:57-73](../../src/services/chat/history/importArchive.ts#L57-L73) |
| 顺序容忍 | 支持 `manifest → [user] → 会话 → index`，也支持“会话与其图片交错、index 在末尾”。**不支持**会话出现在 index 之后；index 之后只接受已导入会话的图片条目。 | [importArchive.ts:104-141](../../src/services/chat/history/importArchive.ts#L104-L141) |
| user.json | 只在“尚未见到任何会话、尚未见到 index”时跳过；位置不对会 `backupIncomplete`。 | [importArchive.ts:114-116](../../src/services/chat/history/importArchive.ts#L114-L116) |
| ZIP 特性 | 只接受单卷 ZIP32、STORE/DEFLATE；拒绝 ZIP64、加密、分卷、data descriptor 之外的扩展字段、非 UTF-8 的非 ASCII 文件名。 | [zipReader.ts:46-75](../../src/services/chat/history/zipReader.ts#L46-L75)、[zipReader.ts:139-152](../../src/services/chat/history/zipReader.ts#L139-L152) |
| 路径 | 统一 NFC；禁止反斜杠、`..`、绝对路径/盘符；同名条目不能重复。 | [zipReader.ts:61-75](../../src/services/chat/history/zipReader.ts#L61-L75)、[zipReader.ts:155-160](../../src/services/chat/history/zipReader.ts#L155-L160) |
| 会话文件名 | 只认 `chat_sessions/([^/]+).json` 这一层；文件内 `id` 必须与路径中的 id 相同，且同 id 不能出现两次。 | [paths.ts:13-14](../../src/services/chat/history/paths.ts#L13-L14)、[importArchive.ts:117-122](../../src/services/chat/history/importArchive.ts#L117-L122) |
| 索引一致性 | 必须存在 `chat_session_index.json`；索引 id 不能重复，且必须与会话文件**一一对应**（少一个、多一个都失败）。 | [importArchive.ts:145-157](../../src/services/chat/history/importArchive.ts#L145-L157) |

### 1.2 `manifest.json`

|字段|类型|要求|
|---|---|---|
| `export_format_version` | number | **必须精确等于 3**（[format.ts:12](../../src/services/chat/history/format.ts#L12)）：`> 3` 报 `backupNewerVersion`，其它（缺失/字符串/2）报 `backupInvalidVersion`（[schema.ts:229-238](../../src/services/chat/history/schema.ts#L229-L238)）。 |

### 1.3 `chat_session_index.json`

数组；每个元素（[format.ts:18-25](../../src/services/chat/history/format.ts#L18-L25)、[schema.ts:240-250](../../src/services/chat/history/schema.ts#L240-L250)）：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | string | ✅ | 非空，不能含 `/`、`\`，不能是 `.`/`..` |
| `title` | string \| null | ✅（可为 null） | 键必须存在 |
| `pinned` | — | ❌ | 导入端**不读取**，固定写 `false`（schema.ts:245） |
| `message_count` | 非负安全整数 | ✅ | — |
| `inserted_at` | ISO 时间 | ✅ | 见 §1.8 |
| `updated_at` | ISO 时间 | ✅ | 见 §1.8 |

> 注意：索引里的 title/时间**不会**写进本地会话；本地会话的标题/时间来自会话 JSON 本身（`toLocalSession` 用 file 的字段，[toLocal.ts:274-282](../../src/services/chat/history/toLocal.ts#L274-L282)）。索引只用于一致性校验。

### 1.4 `chat_sessions/{session_id}.json`（会话对象）

| 字段 | 类型 | 必填 | 可空 | 说明 |
|---|---|---|---|---|
| `id` | string | ✅ | ❌ | 必须等于文件名中的 id，且不含路径分隔符 |
| `title` | string | ✅ | ✅ null | null 表示空标题；本地显示为 `''` |
| `pinned` | boolean | ❌ | — | 导出端会写，导入端不读，固定 false |
| `current_message_id` | 非负整数 | ✅ | ❌ | **必须在 `messages` 中存在**；决定当前分支（[toLocal.ts:262](../../src/services/chat/history/toLocal.ts#L262)） |
| `inserted_at` | ISO 时间 | ✅ | ❌ | → `Session.createdAt` |
| `updated_at` | ISO 时间 | ✅ | ❌ | → `Session.updatedAt` |
| `messages` | Message[] | ✅ | ❌ | 见 §1.5 |

代码：[format.ts:28-38](../../src/services/chat/history/format.ts#L28-L38)、[schema.ts:252-263](../../src/services/chat/history/schema.ts#L252-L263)。

### 1.5 消息树规则（最容易卡住的地方）

`buildTree` 按 `id` 升序逐条建树，**每一条消息都要过校验**（不只是当前分支），任一不满足整包报 `backupInvalidBranches`：

1. `id` 必须唯一（重复 → 失败）。
2. `parent_id === null` 的根消息**必须是 `USER`**。
3. 非根消息的 `parent_id` 必须能在“已处理集合”中找到 ⇒ **父消息 id 必须小于子消息 id**；父不存在 → 失败。
4. **角色必须交替**：子消息角色不能与父消息相同（USER↔ASSISTANT）。
5. `current_message_id` 必须能查到；查不到 → 失败（官方若给 null/缺省，这里直接挂）。
6. 从 `current_message_id` 向根回溯，路径上的 `selectedChildId` 被显式设置；当前消息自身的 `selectedChildId` 清空（“用户停留位置之后即使还有消息也不展开”）。

代码：[toLocal.ts:234-271](../../src/services/chat/history/toLocal.ts#L234-L271)；文档：[README.md:26](../../src/services/chat/history/README.md#L26)。

> 与 DSH 的差异：DSH 只解析`current_message_id` 那条链，缺省/null/无效时回退到“id 最大的叶子”，且不要求角色交替（DeepSeek 官方笔记明确写了这一点）。chat-demo 则是**全树严格校验**。

### 1.6 消息对象字段

| 字段 | 类型 | 必填 | 可空 | 说明 |
|---|---|---|---|---|
| `id` | 非负安全整数 | ✅ | ❌ | 会话内自增；本地转成 string |
| `parent_id` | 非负整数 | ✅ | ✅ null | null = 会话第一条 |
| `role` | `USER` \| `ASSISTANT` | ✅ | ❌ | 只认这两个值 |
| `status` | 枚举（见下） | ✅ | ❌ | 只认 7 个值 |
| `thinking_enabled` | boolean | ✅ | ❌ | **官方是否必带？未实证**（风险 B） |
| `search_enabled` | boolean | ✅ | ❌ | 同上 |
| `accumulated_token_usage` | 非负整数 | ✅ | ❌ | 同上 |
| `inserted_at` | ISO 时间 | ✅ | ❌ | — |
| `fragments` | Fragment[] | ✅ | ❌ | 未知类型片段会被**丢弃但不会失败** |

代码：[format.ts:51-65](../../src/services/chat/history/format.ts#L51-L65)、[schema.ts:200-221](../../src/services/chat/history/schema.ts#L200-L221)。

**状态枚举（7 个，精确匹配）**：`WIP`、`FINISHED`、`CONTENT_FILTER`、`FAILED`、`INCOMPLETE`、`CONTEXT_LENGTH_EXCEEDED`、`TIMEOUT`（[format.ts:42-49](../../src/services/chat/history/format.ts#L42-L49)、[schema.ts:206-214](../../src/services/chat/history/schema.ts#L206-L214)）。任何新状态值都会让**整条消息**校验失败 → 该会话失败。

本地映射（[toLocal.ts:200-231](../../src/services/chat/history/toLocal.ts#L200-L231)）：

| 归档 status | 本地 ReplyStatus | 备注 |
|---|---|---|
| `FINISHED` | `done` | — |
| `CONTENT_FILTER` | `done` + `stopReason: 'refusal'` | 丢弃模型正文和思考，保留模板提示/工具步骤 |
| `INCOMPLETE` / `WIP` | `stopped` | — |
| `FAILED` / `CONTEXT_LENGTH_EXCEEDED` / `TIMEOUT` | `error` | 不进入后续上下文 |

### 1.7 片段（fragment）字段

片段级校验失败**不会**让整包失败：`fragment()` 捕获 `backupIncomplete` 并返回 null，只丢掉这一个片段（[schema.ts:188-198](../../src/services/chat/history/schema.ts#L188-L198)）。未知 `type` 同样整段丢弃（schema.ts:183-184）。

| type | 所在消息 | 必填字段 | 可空/可选 | 本地处理 |
|---|---|---|---|---|
| `REQUEST` | USER | `content` string | — | 拼成用户正文 |
| `RESPONSE` | ASSISTANT | `content` | — | text block |
| `TEMPLATE_RESPONSE` | ASSISTANT | `content` | — | text block（`isTemplate`） |
| `THINK` | ASSISTANT | `content`、`elapsed_secs` | 可空（键必须在） | thinking block；`elapsed_secs` 缺失会整段丢弃 |
| `TIP` | ASSISTANT | `content`、`style` | `style∈{INFO,WARNING,CAUTION}` | 丢弃（无展示） |
| `CONTENT_RISK` | ASSISTANT | `content`、`style` | 同上 | 丢弃 |
| `PARTIAL_FILE_READ` | ASSISTANT | `ratio` 0–100 | — | 丢弃 |
| `TOOL_SEARCH` | ASSISTANT | `queries`、`results` | `status` 可选；`stage_id`/`group_id` 可选 | 转 web_search 工具块；`WIP` 无结果 |
| `TOOL_OPEN` | ASSISTANT | `status`、`url`、`title`、`published_at` | `snippet`/`site_name` 可选；`published_at` 可空 | url 为 null/空 → 丢弃 |
| `TOOL_FIND` | ASSISTANT | `status` | `pattern` 可选 | 丢弃（仅展示占位） |
| `READ_LINK` | ASSISTANT | `results[].signature` | `url`/`title` 可选 | 保留签名 + 链接 |
| `FILE` | USER | `id`、`file_name`、`file_size`、`token_usage`、`inserted_at`、`export_path`、`is_image`、`width`、`height` | 除 id/file_name/file_size/is_image/inserted_at 外均可为 null（键必须在） | 附件/图片 |
| `TOOL_IMAGE_SEARCH` | ASSISTANT | `status`、`results` | `image_indexes` 可选；`stage_id`/`group_id` 可选 | 丢弃（无对应展示） |

代码：[schema.ts:102-186](../../src/services/chat/history/schema.ts#L102-L186)；类型定义：[format.ts:67-193](../../src/services/chat/history/format.ts#L67-L193)。转换规则见 [toLocal.ts:101-198](../../src/services/chat/history/toLocal.ts#L101-L198)。

### 1.8 时间格式

- 正则：`^(YYYY-MM-DDTHH:MM:SS)(\.digits)?Z$`，即 **UTC、必须 Z 结尾、小数秒可有可无**；不允许时区偏移（[schema.ts:37-51](../../src/services/chat/history/schema.ts#L37-L51)）。
- 校验时把小数秒补齐/截断到 3 位再交给 `Date`，但**返回值保留原文**，不丢精度。
- 本地存储用 `Date.parse` 转毫秒（[toLocal.ts:26](../../src/services/chat/history/toLocal.ts#L26)）。

### 1.9 图片匹配

- 只有 `FILE` 片段里 `is_image: true` 且 `export_path` 非 null 的条目才会被当作图片引用（[toLocal.ts:283-307](../../src/services/chat/history/toLocal.ts#L283-L307)）。
- `export_path` 必须位于本会话目录 `chat_sessions/{sessionId}_files/` 下；路径按 **NFC** 归一化；同一条目名不能对应不同 file id（否则 `backupInvalidFiles`）。
- 导入时**按条目名精确匹配**：ZIP 里没被任何 `export_path` 引用的图片直接跳过；引用了但缺失的图片保留文件名/大小，但不会进入后续请求（[importArchive.ts:124-137](../../src/services/chat/history/importArchive.ts#L124-L137)、[README.md:26](../../src/services/chat/history/README.md#L26)）。
- 字节识别 MIME（jpeg/png/gif/webp/bmp/tiff/avif），识别不出给空字符串，图片身份仍由 `is_image` 决定（[importArchive.ts:16-42](../../src/services/chat/history/importArchive.ts#L16-L42)）。
- 非图片附件只有元数据，本体不在导出包里。

### 1.10 官方导出端写出的字段（对称证据）

chat-demo 自己的导出端（[toArchive.ts](../../src/services/chat/history/toArchive.ts)）写出的正是上面这套字段：`file_size/token_usage/export_path/width/height`（L32-38）、`thinking_enabled/search_enabled/accumulated_token_usage`（L250-252）、`pinned/current_message_id`（L262-264）、索引的 `pinned/message_count`（L274-275）。这说明导入器的“严格”是照着自家导出端的输出写的。

---

## 2. 官方导出包结构的证据

### 2.1 第一方文档：chat-demo 就是官方指定的导入器

DeepSeek API 文档（2026-11-13 开放的全家桶应用 API）在 chat-demo 一节明确写：

> “You can also import the chat history you exported from DeepSeek Chat and continue your conversations.”
> — <https://api-docs.deepseek.com/guides/build_a_chatbot>

这是“官方导出 → chat-demo 导入”这条链路的**第一方契约声明**。

### 2.2 第一方实现：DeepSeek 自家的 DSH 导入插件读同一套格式

DeepSeek 官方仓库 `deepseek-ai/deepseek-harness` 里的实验包 `@deepseek-ai/dsh-deepseek-chat-import` 专门导入 `deepseek_data-*.zip`：

- README 的失败表写明：`unsupported-version` = “`manifest.json` does not declare `export_format_version` 3”；
  `chat_session_index.json` 用来选会话；导入 `deepseek_data-*.zip`。
  <https://github.com/deepseek-ai/deepseek-harness/blob/master/packages/experimental/deepseek-chat-import/README.md>
- 设计笔记直接写：“导出格式（`export_format_version: 3`）与 DSH Session 有四点结构差异”，并描述 `messages` 是靠 `parent_id` 连接的消息树、`fragments` 交错记录思考/搜索/正文、`current_message_id` 是导出自己的线性化依据。
  <https://github.com/deepseek-ai/deepseek-harness/blob/master/.agents/notes/implemented/feature/2026-11-11-deepseek-chat-export-import.zh.md>
- 字段级 zod 校验（[src/format.ts](https://github.com/deepseek-ai/deepseek-harness/blob/master/packages/experimental/deepseek-chat-import/src/format.ts)）读的字段与本仓库几乎逐项对应：
  - `manifest.export_format_version`
  - 索引行 `id / inserted_at / updated_at`
  - 会话文件 `id / title / current_message_id / inserted_at / messages`
  - 消息 `id / parent_id / role / status / inserted_at / fragments`
  - 片段 `REQUEST / RESPONSE / THINK / TEMPLATE_RESPONSE / TOOL_SEARCH / TOOL_OPEN / TOOL_FIND / READ_LINK / TOOL_IMAGE_SEARCH / PARTIAL_FILE_READ / TIP / FILE`
  - `FILE` 条目 `id / file_name / file_size / is_image / export_path`
- 图片路径规则也一致：`chat_sessions/{id}_files/…`（同仓库 importer/README 描述）。

**这基本排除了“官方导出是另一套格式”的可能**：DeepSeek 内部两个不同产品（chat-demo 与本家 DSH 插件）读的是同一套 v3 归档。

### 2.3 第三方实测：v3 目录结构

社区技能 `cosen1024/deepseek-chat-import-skill` 的 README 标注“DeepSeek 导出格式（v3，2026-11 实测）”：

```text
deepseek_data-YYYY-MM-DD/
├── manifest.json                 {"export_format_version": 3}
├── chat_session_index.json       会话列表
├── user.json                     账号信息（脚本不读取）
└── chat_sessions/
    ├── <会话ID>.json             一个会话
    └── <会话ID>_files/           这个会话的图片
```

并说明消息用 `parent_id` 连成树、`current_message_id` 指向最后看到的消息、片段类型包含 `REQUEST/FILE/THINK/RESPONSE/TOOL_SEARCH/TOOL_OPEN/TEMPLATE_RESPONSE/CONTENT_RISK`。
<https://github.com/cosen1024/deepseek-chat-import-skill>

### 2.4 真实导出目录的旁证（2026-11-14）

nowledge-co/community issue #1033 贴出了 2026-11-14 取得的真实导出解压目录：

```text
deepseek_data-2026-11-14/
├── chat_sessions/
├── chat_session_index.json
├── manifest.json
└── user.json
```

<https://github.com/nowledge-co/community/issues/1033>
（文件管理器按字母排序，不能据此判断 ZIP 内条目顺序；但顶层文件集合与本仓库 README 完全一致，且确认有 `user.json`。）

### 2.5 没找到什么

- **没有找到公开的真实 `chat_sessions/*.json` 原文/样本包**。GitHub 代码搜索需要登录（API 返回 401），只能搜 issue/仓库/文档；`raw.githubusercontent.com` 在本机不可达，改用 `api.github.com` contents 读取。
- 因此“官方包是否一定带 `thinking_enabled` / `search_enabled` / `accumulated_token_usage`、`current_message_id` 是否可能为 null、`FILE.file_size` 是否可能缺省”这些问题，**只能从官方实现反推，无法用真实字节证实**。
- DSH 的格式代码本身给了“会漂移”的直接证据（见 §3.2）。

---

## 3. 兼容性判定

### 3.1 字段映射：恒等映射（不需要转换）

官方导出字段与本地模型的对应关系（[toLocal.ts](../../src/services/chat/history/toLocal.ts)）：

| 官方导出 | 本地 | 处理 |
|---|---|---|
| `session.id` | `Session.id` | 直接用 |
| `session.title`（null） | `Session.title`（`''`） | null → 空串 |
| `session.inserted_at/updated_at` | `createdAt/updatedAt` | `Date.parse` |
| `current_message_id` + `messages` | `messages` / `rootMessageId` / `selectedChildId` | `buildTree` 线性化 |
| `messages[].id` | `Message.id` | number → string |
| `parent_id` | `parentId` | null 保留 |
| `role` | `role` | USER→user，ASSISTANT→assistant |
| `status` | `ReplyStatus` / `stopReason` | 见 §1.6 表 |
| `thinking_enabled/search_enabled` | `thinking/search` | 直接复制 |
| `fragments[].REQUEST` | 用户正文 | 拼接 |
| `fragments[].RESPONSE/THINK/TEMPLATE_RESPONSE` | text/thinking block | 见 toLocal |
| `TOOL_SEARCH/TOOL_OPEN/TOOL_FIND/READ_LINK` | server_tool_use + result | 生成稳定 tool id |
| `FILE.files[]` | `FileAttachment[]` | 图片另走 `export_path` |
| `FILE.export_path` | 图片映射（ZIP 条目名 → file id） | NFC + 前缀校验 |

**没有任何字段需要改名/改值/换结构。** 这与“官方导出 == chat-demo 归档格式”的结论一致。

### 3.2 风险清单（按严重度）

DSH 的 `format.ts` 里有一段关键注释：

> “Fields the 2026-11-12 exporter added are optional: `export_format_version` does not change with field-level drift, so an archive exported before that date must still parse instead of losing its whole conversation.”
> “`file_size` … an archive without it reports 0 bytes.”
> “`current_message_id` may be absent or null.”

这说明官方在**不升 v3 版本号**的情况下加过字段，也说明旧包可能缺字段。逐条对照本仓库：

| # | 风险 | 严重度 | 说明 |
|---|---|---|---|
| A | **首条目必须 `manifest.json`** | 中 | 官方格式规定 manifest 是第一个条目（[README.md:10](../../src/services/chat/history/README.md#L10)），正常下载包应满足；但任何“解压后重打包/工具改写/首条目变化”都会直接报“无法识别该文件”。同代码的第三方项目有真实用户包卡在 check 的记录（根因未定，仅作风险提示）：<https://github.com/zzy-repo/deepseek-app/issues/40>、<https://github.com/zzy-repo/deepseek-app/issues/153> |
| B | **消息级三字段硬要求** `thinking_enabled/search_enabled/accumulated_token_usage` | 中-高 | 缺任一 → 整条消息 invalid → 整个会话失败（[schema.ts:215-217](../../src/services/chat/history/schema.ts#L215-L217)）。官方是否必带**未实证**；DSH 不读它们，无法反证。 |
| C | **`current_message_id` 必须存在且命中** | 中-高 | DSH 明确允许“缺失/null/指向不存在”，chat-demo 直接 `fail`（[toLocal.ts:262](../../src/services/chat/history/toLocal.ts#L262)）。若官方某些会话（空会话/异常会话）给 null，就会整包失败。 |
| D | **`FILE` 的 `file_size` 等字段键必须存在** | 低-中 | 缺 `file_size` 等 → 整个 FILE 片段被丢弃（图片/附件丢失），**不会**整包失败（[schema.ts:87-100](../../src/services/chat/history/schema.ts#L87-L100)、[schema.ts:188-198](../../src/services/chat/history/schema.ts#L188-L198)）。DSH 把 `file_size` 设为可选并默认 0，说明旧包可能没有。 |
| E | **全树严格校验 + 角色交替 + 父 id 必须更小** | 中 | 官方树正常应满足；但 chat-demo 连被丢弃的旁支也校验，任何一条坏数据会让整个会话失败。DSH 只线性化当前分支，并明确“不要求 user/assistant 严格交替”。 |
| F | **status 只认 7 个值** | 中 | 官方未来若加状态，v3 不升号，这里会整条消息失败。DSH 接受任意字符串。 |
| G | **时间只认 UTC `Z`** | 低 | 官方 RFC3339 UTC（DSH 注释：0/3/6/9 位小数），正常满足；若出现 `+00:00` 或 epoch 数字则失败。 |
| H | **ZIP32 限制** | 低 | 超大导出（>4GB / >65535 条目）若用 ZIP64 会被拒（[zipReader.ts:46-59](../../src/services/chat/history/zipReader.ts#L46-L59)）。 |
| I | **内存/耗时** | 中（工程） | 导入器把会话 JSON 全量读进内存、图片逐张 Blob；Android WebView 上几百 MB 的包有 OOM 风险。DSH 设了 512MiB 包 / 256MiB 条目上限可参考。 |

### 3.3 建议的适配（不改变格式，只增强容错）

按优先级：

1. **check 阶段按名找 manifest**：扫描 ZIP 中央目录/条目名找 `manifest.json`，不再要求“第一个条目”；同时把 `backupUnrecognized` 拆成“非 ZIP / 无 manifest / manifest 结构不符 / 版本不支持”四类可定位错误。
2. **消息级字段给默认值**：缺失时 `thinking_enabled=false`、`search_enabled=false`、`accumulated_token_usage=0`；未知 `status` 归为 `INCOMPLETE`（而不是整包失败）。
3. **`current_message_id` 兜底**：缺失/null/无效时回退到“id 最大的叶子”，照 DSH 的 `selectBranch`。
4. **`FILE` 字段默认值**：`file_size=0`、`token_usage/width/height/export_path=null`；保留“未知片段丢弃”的现有行为。
5. **树校验改为“尽力导入”**：只沿当前分支线性化；父缺失/环/角色异常时跳过该消息或回退到最大 id，不因旁支坏数据整包失败（可参考同代码项目的 #117 改法：收集 `skipped`，不整包回滚）。
6. **时间放宽**：`Date.parse` + 范围校验，接受 RFC3339 的 `Z` 与合法偏移；保留原始字符串。
7. **大包**：下载后先看大小/条目数（可参考 DSH 的 512MiB / 256MiB 上限），必要时改为“边下边解/按需读取”，避免整包进内存。

> 这些改动都**不涉及字段映射**，因为格式本来就是同一套；改的是“解析器严格度”。

### 3.4 结论定级

- **格式层：直接兼容** —— 同一套 `export_format_version: 3`，字段映射为恒等，不需要写转换层。
- **实现层：需要适配（防御性）** —— 当前代码是“严格校验器”，面对官方包的字段漂移、null/缺省、旁支异常会整包失败。建议先落 §3.3 的 1–5 条，再用真实包回归。
- **证据层：仍缺一份真实样本** —— 没有它，B/C/E/F 是否真实命中无法 100% 判定；上线前必须补。

---

## 4. 需要一份什么样的真实样本

**样本规格（最小充分）：**

1. 2026-11-12 之后从 chat.deepseek.com「设置 → 数据管理 → 导出所有历史对话」下载的**原始** `deepseek_data-YYYY-MM-DD.zip`（不要解压重打包，不要用第三方工具改写）。
2. 内容至少包含：
   - 1 个**有分支**的会话（重新生成过回答，或编辑过提问）；
   - 1 个带**图片**的提问；
   - 1 个带**深度思考**和 1 个带**联网搜索**的回答；
   - 1 个空标题/新会话（如果官方允许导出）；
   - 总量控制在 1–20MB（便于脱敏和回归）。
3. 需要记录/提供（内容可脱敏，键名与类型保留）：
   - `unzip -l` 的**完整条目列表与顺序**（重点：第一个文件条目是不是 `manifest.json`）；
   - `manifest.json`、`chat_session_index.json`、`user.json` 原文（去掉账号信息）；
   - 任一 `chat_sessions/*.json` 中：会话对象键集合、`messages[]` 每条消息的键集合（重点确认 `thinking_enabled/search_enabled/accumulated_token_usage` 是否存在）、`current_message_id` 是否有 null、时间字符串原文；
   - 一个 `FILE` 片段的完整 JSON（确认 `file_size/token_usage/width/height/export_path` 是否存在、图片条目名格式）；
   - 一个 `TOOL_SEARCH`/`TOOL_OPEN`/`READ_LINK` 片段样本（确认 `status/stage_id/group_id/site_icon` 等可选字段）。
4. 验证方式：用本地 `checkArchiveFormat` + `importArchive`（或等价的 Node 复现脚本）直接跑该 ZIP，记录失败点；再与 `schema.ts` 必填项逐条对照。
5. 建议同时收集**一份 2026-11-12 之前导出的旧包**，验证字段漂移（尤其 `file_size`、`THINK.elapsed_secs`、`TOOL_SEARCH.status`）。

---

## 5. 证据链接汇总

| 证据 | 链接 | 证明了什么 |
|---|---|---|
| DeepSeek API 文档（chat-demo 节） | <https://api-docs.deepseek.com/guides/build_a_chatbot> | 官方声明 chat-demo 可导入 DeepSeek Chat 导出的聊天记录 |
| DSH 导入插件 README | <https://github.com/deepseek-ai/deepseek-harness/blob/master/packages/experimental/deepseek-chat-import/README.md> | 官方读 `deepseek_data-*.zip`、只支持 `export_format_version: 3`、用 `chat_session_index.json` 选会话 |
| DSH 导入插件字段 schema | <https://github.com/deepseek-ai/deepseek-harness/blob/master/packages/experimental/deepseek-chat-import/src/format.ts> | 官方字段全集；2026-11-12 字段漂移、`file_size` 可选、`current_message_id` 可缺省 |
| DSH 设计笔记（中文） | <https://github.com/deepseek-ai/deepseek-harness/blob/master/.agents/notes/implemented/feature/2026-11-11-deepseek-chat-export-import.zh.md> | 官方导出格式 v3、消息树/片段/图片路径、状态语义 |
| cosen 导入技能（v3 实测） | <https://github.com/cosen1024/deepseek-chat-import-skill> | 2026-11 真实导出目录结构、片段类型 |
| nowledge 社区 issue #1033 | <https://github.com/nowledge-co/community/issues/1033> | 2026-11-14 真实导出顶层目录（含 `user.json`） |
| chat-demo 上游仓库 | <https://github.com/deepseek-ai/chat-demo> | 历史导入导出功能属于上游官方 demo |
| 同代码项目真实包失败记录 | <https://github.com/zzy-repo/deepseek-app/issues/40>、<https://github.com/zzy-repo/deepseek-app/issues/153> | 首条目/顺序敏感确实会让真实用户包卡在 check（根因未定，仅风险提示） |

## 6. 本地代码行号索引

| 关注点 | 位置 |
|---|---|
| 归档结构说明 | [src/services/chat/history/README.md](../../src/services/chat/history/README.md#L5-L18) |
| 格式类型定义 | [src/services/chat/history/format.ts](../../src/services/chat/history/format.ts#L1-L208) |
| 字段校验 | [src/services/chat/history/schema.ts](../../src/services/chat/history/schema.ts#L37-L263) |
| 消息树/图片转换 | [src/services/chat/history/toLocal.ts](../../src/services/chat/history/toLocal.ts#L200-L310) |
| 导入主流程 | [src/services/chat/history/importArchive.ts](../../src/services/chat/history/importArchive.ts#L53-L159) |
| ZIP 读取限制 | [src/services/chat/history/zipReader.ts](../../src/services/chat/history/zipReader.ts#L19-L160) |
| 条目路径常量 | [src/services/chat/history/paths.ts](../../src/services/chat/history/paths.ts#L1-L25) |
| 导出端（对称证据） | [src/services/chat/history/toArchive.ts](../../src/services/chat/history/toArchive.ts#L28-L38) |

---

### 附：本次调研的边界

- 只读：未修改 `src/`，未跑 `pnpm build`，未在真机/WebView 上验证。
- 网络：`raw.githubusercontent.com` 与 `cdn.jsdelivr.net` 本机不可达；GitHub 内容统一走 `api.github.com/repos/…/contents` 读取，`repos/{owner}/{repo}/issues` 用于样本检索。shell 里的 6.25MB 用户包属于第三方项目，未取得原包，不能作为“官方包必失败”的证据。
- 官方导出接口（`GET /api/v0/export_all`、`GET /api/v0/download_export_history`）与 7 天有效期来自项目 `AGENTS.md` 的背景记录，本次未独立复测。
