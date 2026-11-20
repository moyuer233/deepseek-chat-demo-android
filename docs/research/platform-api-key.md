# platform.deepseek.com 创建 API Key 的内部接口调研

> 调研时间：2026-11-20（北京时间）  
> 调研对象：https://platform.deepseek.com（首页 commit-id 64e05285；主 JS main.01930e1575.js；业务 chunk 5697.092cb1c1d0.js）  
> 方法：只读抓取 platform 前端 bundle（main + 322 个 chunk 映射，321 个可下载）并静态分析；匿名 HTTP 探测错误结构；用公开第三方实现交叉验证。**未登录任何账号，未发送带有效凭证的创建请求。**  
> 结论分级：**【确定】**＝代码或实测直接证明；**【推测】**＝由证据推断；**【未知】**＝本次无法验证。

---

## 0. 结论摘要

**一句话：只靠 chat.deepseek.com 的登录态，不能自动创建 API Key。** platform 是独立登录态；把 chat 的 userToken 发到 platform，平台会判为无效（顶层 `code` 40003）。如果允许用户在 App 内**再登录一次 platform**（同一账号），自动创建 Key 可行：创建接口是一个带 Bearer 的普通 POST，前端创建路径没有验证码/短信/实名弹窗，成功响应直接返回完整 Key。

四个问题的短答：

1. **接口**：列表 `GET /api/v0/users/get_api_keys`；创建/删除/改名共用 `POST /api/v0/users/edit_api_keys`，用请求体里的 `action` 区分（create / delete / update）。都在 platform 同源 `/api/v0/users/*` 下。
2. **登录态**：platform 把 token 存在 localStorage 的 `userToken`，值是 JSON 信封 `{"value":"<JWT>","__version":"0"}`；与 chat 的 key 名/信封相同，但**不是同一套凭证**，不能跨域直接用（证据见 §2.3）。
3. **关卡**：创建 Key 的前端路径没有验证码、手机验证、PoW；但有三类关卡：实名认证（仅特定 `registerClientType=DSH` 账号，后端也可能返回 biz_code 8）、Key 数量上限（biz_code 1）、通用风控挑战（40303 Shumei/hCaptcha、AWS WAF 405/202）。
4. **结论**：不能"只登录 chat 就自动建"。最接近的替代是"chat 登录后引导用户在同一 WebView 登录一次 platform，然后自动建 Key"；再退一步是深链到 https://platform.deepseek.com/api_keys 让用户点一下，或手动粘 Key。

---

## 1. 接口清单

### 1.0 基础信息

- Base URL：`https://platform.deepseek.com`。platform 前端的 http 配置是 `baseUrl: undefined`，所有 API 请求都走当前 origin（同源），不是单独的 api 子域。
- 响应形态：**HTTP 200 + JSON 信封**；鉴权失败也是 200（实测见 §1.7）。业务错误看顶层 `code` 和内层 `data.biz_code`。
- 版本信息：首页 `<meta name="commit-id" content="64e05285">`；主 JS `main.01930e1575.js`；业务代码在 `5697.092cb1c1d0.js`（约 1.11 MB）。
- 前端路由：`/api_keys` 是 API Keys 页面（`lk.API_KEYS="/api_keys"`）。

### 1.1 四个操作

| 操作 | 方法 | 路径 | 请求体 | 成功时读取 |
|---|---|---|---|---|
| 列表 | GET | `/api/v0/users/get_api_keys` | 无 | `data.biz_data.api_keys[]`、`data.biz_data.key_quota` |
| 创建 | POST | `/api/v0/users/edit_api_keys` | `action="create"` + name + 空字段 | `data.biz_data.api_key`（含完整 `sensitive_id`） |
| 删除 | POST | `/api/v0/users/edit_api_keys` | `action="delete"` + redacted_key + created_at + tracking_id | `data.biz_data`（可为 null） |
| 改名/切换 exp | POST | `/api/v0/users/edit_api_keys` | `action="update"` + 新 name + redacted_key + created_at + tracking_id（可选 is_exp） | `data.biz_data.api_key` |

**没有独立的 DELETE 方法，也没有单独的 create/delete 路径；三个写操作都复用 `edit_api_keys`。**

#### 1.1.1 列表（GET get_api_keys）

前端调用（`5697.092cb1c1d0.js`，函数 `uj`）：

```js
function uj(e){
  return s$.http.http.get("/api/v0/users/get_api_keys",{json:e}).then(e=>{
    let {json:t,context:{logId:a}}=e, {dshKeyName:n}=s2.getTranslate();
    return {
      apiKeys: t.data.biz_data.api_keys.map(e=>({
        createdAt: 1e3*e.created_at,
        lastUsed: e.last_use ? 1e3*e.last_use : null,
        name: "DSH"===e.key_type ? n : e.name,
        sensitiveId: e.sensitive_id,
        trackingId: e.tracking_id,
        keyType: e.key_type,
        isExp: e.is_exp
      })),
      keyQuota: t.data.biz_data.key_quota,
      logId: a
    }
  })
}
```

由此可确定列表返回的字段名：

- `api_keys[]`：每项含 `created_at`（秒）、`last_use`（秒，可空）、`name`、`sensitive_id`、`tracking_id`、`key_type`、`is_exp`。
- `key_quota`：页面用 `keyQuota.total.used`、`keyQuota.total.limit`、`keyQuota.exp`。即总量配额和 exp Key 配额。
- **列表里的 `sensitive_id` 是打码的**（页面显示时把连续星号替换成 `*****`；文案明确"API key 仅在创建时可见可复制"）。完整 Key 只在创建响应里出现一次。

#### 1.1.2 创建（POST edit_api_keys，action=create）

前端调用（函数 `hY` 内的 `R`）：

```js
hV({action:"create", name:m, createdAt:void 0, redactedKey:void 0, trackingId:null, isExp:t})
```

经 `hV` 序列化后的实际请求体：

```json
{
  "action": "create",
  "name": "DeepseekChatApp",
  "redacted_key": null,
  "created_at": null,
  "tracking_id": null,
  "is_exp": false
}
```

说明：
- `name` 是用户输入的 Key 名称（必填，不能为空白；前端只校验 `trim()` 非空）。
- `is_exp` 对普通 Key 恒为 `false`；只有账号被批准使用 exp Key 时 UI 才显示开关。
- 成功时前端从 `data.biz_data.api_key.sensitive_id` 取完整 Key，弹窗展示一次，之后列表只能看到打码值。

#### 1.1.3 删除（POST edit_api_keys，action=delete）

前端调用（函数 `hq`）：

```js
hV({
  action:"delete",
  name:void 0,
  redactedKey:t.sensitiveId,
  createdAt:t.createdAt,
  trackingId:t.trackingId
})
```

序列化后：

```json
{
  "action": "delete",
  "name": null,
  "redacted_key": "<列表里的 sensitive_id（打码值）>",
  "created_at": 1690000000,
  "tracking_id": "<tracking_id，或 null>"
}
```

说明：
- `created_at` 的单位是**秒**（前端把列表返回的毫秒值 `/1000` 后再发）。
- `tracking_id` 如果是字符串 `"null"`，前端会转成 JSON null。
- 删除请求不发送 `is_exp`（`hV` 里 `action==="delete"` 时省略）。

#### 1.1.4 改名/更新（POST edit_api_keys，action=update）

前端调用（函数 `hZ`）：

```js
hV({
  action:"update",
  createdAt:n.createdAt,
  name:d,
  redactedKey:n.sensitiveId,
  trackingId:n.trackingId,
  isExp: E ? m : void 0
})
```

序列化后：

```json
{
  "action": "update",
  "name": "new name",
  "redacted_key": "<sensitive_id>",
  "created_at": 1690000000,
  "tracking_id": "<tracking_id>",
  "is_exp": false
}
```

说明：只有 exp 状态发生变化时才带 `is_exp`；不改 exp 时该字段省略。

### 1.2 请求体构造的公共函数

```js
function hV(e){
  let {action:t, name:a, redactedKey:n, createdAt:r, trackingId:i, isExp:o}=e;
  return s$.http.http.post("/api/v0/users/edit_api_keys",{json:{
    action: t,
    name: null!=a ? a : null,
    redacted_key: null!=n ? n : null,
    created_at: void 0===r ? null : r/1e3,
    tracking_id: "null"===i ? null : i,
    ...("delete"!==t && void 0!==o ? {is_exp:o} : {})
  }}).then(e=>{
    let {json:t,context:a}=e;
    return {
      context:a,
      data:{
        apiKey: t.data.biz_data ? {
          createdAt: t.data.biz_data.api_key.created_at,
          lastUsed: t.data.biz_data.api_key.last_use,
          name: t.data.biz_data.api_key.name,
          sensitiveId: t.data.biz_data.api_key.sensitive_id,
          trackingId: t.data.biz_data.api_key.tracking_id,
          keyType: t.data.biz_data.api_key.key_type,
          isExp: t.data.biz_data.api_key.is_exp
        } : null,
        code: t.data.biz_code
      }
    }
  })
}
```

### 1.3 必需请求头

platform 前端对 `/api/`、`/auth-api/` 请求统一加以下头（`5697.092cb1c1d0.js` 的 `onBeforeRequestInit`）：

| Header | 值 | 说明 |
|---|---|---|
| `Authorization` | `Bearer <platform userToken>` | **鉴权必需**。缺失返回 40002，无效返回 40003（实测）。 |
| `x-client-bundle-id` | `com.deepseek.chat` | 品牌为 nov 时是 `com.deepseek.chat.nov`；第三方实现也常发空串，平台可接受。 |
| `x-client-platform` | `web` | platform Web 端固定 web。 |
| `x-client-version` | `1.0.1` | platform 的 `staticEnv.appVersion`。 |
| `x-client-locale` | `zh_CN` / `en_US` | 下划线格式。 |
| `x-client-timezone-offset` | 如 `28800` | 秒；**东为正**（UTC+8 = 28800）。 |
| `x-device-id` | UUID v4 | 来自 localStorage `deepseek-device-id:platform`；请求路径以 `/api/` 或 `/auth-api/` 开头时自动加。 |
| `x-device-model` | `""`（Web） | Web 端是空串；DSH 端是 `darwin-arm64` 这类值。 |
| `Content-Type` | `application/json` | POST 必需。 |
| `Accept` | `application/json` | 建议带，未验证是否强制。 |

另有 `x-dsh-auth-token`：平台也把它识别为鉴权头（实测无效值返回 40003 而不是 40002），用于 DSH grant，**不适用于本方案的 platform Web token**。

### 1.4 返回结构

成功时统一是两层信封（来自前端解析逻辑和第三方 DSH 官方包 `readEnvelope` 的校验流程）：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "biz_code": 0,
    "biz_msg": "",
    "biz_data": { }
  }
}
```

- 列表的 `biz_data`：`{ api_keys:[...], key_quota:{ total:{used,limit}, exp:{used,limit} } }`。
- 创建/更新的 `biz_data`：`{ api_key:{ created_at,last_use,name,sensitive_id,tracking_id,key_type,is_exp } }`。
- 删除的 `biz_data`：可为 null（前端删除逻辑不读返回内容）。

**注意**：本次没有真实账号，无法抓到成功响应样本；以上字段名来自前端映射代码（【确定】字段名），具体数值和是否所有字段都始终存在（【未知】）。

### 1.5 业务错误码（`data.biz_code`）

```js
0   OK
1   EXCEED_MAXIMUM_KEY_NUM        // Key 数量达到上限
2   API_KEY_NAME_TOO_LONG         // 名称过长
8   IDENTITY_VERIFICATION_REQUIRED// 需要实名认证（前端创建 switch 未专门处理）
9   ExpAuthorizationRequired      // exp Key 未获授权
10  ExceedMaximumExpKeyNum        // exp Key 数量超限
```

### 1.6 顶层错误码（`code`，HTTP 200）

```js
40002 MISSING_TOKEN
40003 INVALID_TOKEN
40006 REQUEST_DECRYPT_FAILED
40007 REQUEST_DECRYPT_NOT_ALLOWED
40029 IP_ACCESS_RESTRICTED
40012 USER_IS_BANNED
50006 MUTED
40300 POW_HEADER_ERROR
40301 INVALID_POW_RESPONSE
40303 CHALLENGE_REQUIRED
40304 CHALLENGE_FAILED
```

### 1.7 匿名探测（本次实测）

用 Node fetch（不登录、不带有效 token）请求：

```text
GET https://platform.deepseek.com/api/v0/users/get_api_keys
-> HTTP 200  {"code":40002,"msg":"Missing Token","data":null}

GET .../get_api_keys  Authorization: Bearer invalid-token-for-probe
-> HTTP 200  {"code":40003,"msg":"Authorization Failed (invalid token)","data":null}

POST .../edit_api_keys  Authorization: Bearer invalid-token-for-probe
body: {"action":"create","name":"probe","redacted_key":null,"created_at":null,"tracking_id":null,"is_exp":false}
-> HTTP 200  {"code":40003,"msg":"Authorization Failed (invalid token)","data":null}

OPTIONS .../edit_api_keys
-> HTTP 405  {"detail":"Method Not Allowed"}（无 Access-Control-Allow-Origin）

GET .../get_api_keys  header x-dsh-auth-token: invalid-grant
-> HTTP 200  {"code":40003,...}   // 说明平台也识别 x-dsh-auth-token 这个鉴权头
```

补充：**响应里没有 `Access-Control-Allow-Origin`**（带 platform 或 chat 的 Origin 都一样）。这意味着浏览器里跨域 XHR/fetch 会被 CORS 拦；App 必须走原生 HTTP（或同源 WebView 内 fetch）。

---

## 2. platform 的登录态

### 2.1 存储方式【确定】

platform 前端用的是同一套 AppKit 认证 SDK（与 chat 同源代码），token 存储句柄这样创建：

```js
c = l.create("userToken", null, null)
d = l.create("__appKit_userInfo", {id:null}, null)
```

底层写入（`class iG`）：

```js
n = { value: t, __version: i }
localStorage.setItem(e, JSON.stringify(n))
```

即 localStorage key = `userToken`，值是 JSON 字符串 `{"value":"<token>","__version":"0"}`（`__version` 是存储 schema 版本；社区抓包里也见过 `1`，读的时候只取 `.value` 即可）。另有 `__appKit_userInfo` = `{"id":"<user id>"}`。

token 取用链：登录成功后 `setPersistedCurrentUser(token,id)` → `D.set(token)`（写入 `userToken`）；HTTP 拦截器取 token 后加 `Authorization: Bearer <token>`：

```js
if(!e.context.withToken) return e;
let {token:a, source:n} = u.getToken();
return a && (e.headers.Authorization = "Bearer ".concat(a), ...)
```

设备身份另存：localStorage `deepseek-device-id:platform`（UUID v4），请求时加 `x-device-id` / `x-device-model`。

### 2.2 token 形态

- 存储层是"字符串 + JSON 信封"，**不是**把用户信息塞进 token 对象。
- platform 的 token 是 JWT：社区插件文档明确说从 platform 的 `userToken` 复制出的长串"以 eyJ 开头"（JWT 的 base64 header）。本次未解码验证 payload 字段（【未知】具体 claims/有效期）。
- 发送方式只有 `Authorization: Bearer`（或 DSH 的 `x-dsh-auth-token`）。

### 2.3 与 chat.deepseek.com 的 userToken 是否同一套

**存储机制相同，凭证不是同一套。**【确定（结构）/ 高置信（第三方实测）】

| 项 | chat.deepseek.com | platform.deepseek.com |
|---|---|---|
| localStorage key | `userToken` | `userToken` |
| 值形态 | `{"value":"<JWT>","__version":"0/1"}` | 同左（同一套 AppKit 代码） |
| 登录接口 | `/api/v0/users/login`（chat 源） | `/auth-api/v0/users/login`（platform 源） |
| 会话 cookie | `ds_session_id`：HostOnly、HttpOnly、Secure、SameSite=Strict | 首页只设 WAF cookie，无跨站 SSO cookie |
| 能否互相直接用 | **不能**：platform 拒绝 chat token（顶层 40003） | **不能**：chat 源不挂 platform 的 get_api_keys（返回 SPA HTML） |

第三方实测证据（dsh-account-switcher README）：

> `chat.deepseek.com` 的网页会话 token **不能直接导入**：它是另一套服务的凭证，平台接口会返回 `code 40003`。……只支持 **platform.deepseek.com** 的登录 token；`chat.deepseek.com` 的网页 token 会被平台拒绝（`40003`）。

本次未用真实账号复测这一条（任务要求不登录），但结构证据（两套登录接口、两个 origin、chat 源没有 platform API）与第三方实测一致，按**高置信**处理。

### 2.4 跨域/跨站结论

- **浏览器层**：localStorage 按 origin 隔离；platform 的 JS 读不到 chat 的 `userToken`。chat 的 `ds_session_id` 是 HostOnly + SameSite=Strict，也不会发给 platform。
- **原生层**：App 可以在 chat WebView 里读到 chat token，再原生发给 platform；但服务端会按 §2.3 拒绝（40003）。所以"跨域直接用"不成立。
- **CORS**：platform API 不回 `Access-Control-Allow-Origin`，浏览器里跨域 fetch 会被拦；原生 HTTP 不受 CORS 限制。

---

## 3. 创建 Key 的关卡

### 3.1 前端创建路径：没有验证码/短信/实名弹窗【确定】

创建弹窗（函数 `hY`）只有：

1. 一个名称输入框（`createApiKeyModalInputPlaceholder`）；
2. 可选 exp Key 开关（只对已获批账号显示）；
3. "取消 / 创建"按钮。

创建按钮的处理函数直接调 `hV({action:"create",...})`；**没有** captcha 组件、没有短信验证码、没有实名认证弹窗、没有 PoW 头。成功后直接把 `sensitive_id` 显示到复制框。

### 3.2 但有三类关卡

| 关卡 | 触发条件 | 前端表现 | 证据 |
|---|---|---|---|
| **实名认证（前端）** | 账号 `registerClientType==="DSH"`、`currency==="CNY"`、且 `identityVerificationId` 与 `businessVerificationId` 都为空 | 页面顶部黄色警告；"创建"按钮被禁用，tooltip 提示"你还未完成实名认证，请认证后再创建 API Key" | 页面组件：`v=!!(c&&h1(c)&&null===c.identityVerificationId&&null===c.businessVerificationId&&"CNY"===c.currency)`；`h1(e)=>(e?.registerClientType)==="DSH"` |
| **实名认证（后端）** | 服务端判定需要实名 | 返回 `biz_code=8`（`IDENTITY_VERIFICATION_REQUIRED`）；前端创建 switch **未专门处理 8**，会落到"未知错误"toast | 错误码枚举 + 创建 switch（只处理 0/1/2/9/10） |
| **Key 数量上限** | `keyQuota.total.used >= keyQuota.total.limit` | 返回 `biz_code=1`；页面按钮也提前禁用 | 错误码枚举、页面 `g=!!f&&f.used>=f.limit` |
| **名称过长** | 名称超过服务端限制 | `biz_code=2` | 错误码枚举 |
| **exp Key 权限/配额** | 未获 exp 授权 / exp Key 超限 | `biz_code=9/10` | 错误码枚举 |
| **通用风控挑战** | 服务端认为请求可疑 | 顶层 `code=40303`；响应 `data` 里有 `challenge_id`、`provider`（shumei/hcaptcha）；客户端弹验证码，成功后带 `x-ds-challenge-response` 重放原请求 | 挑战处理代码 |
| **AWS WAF** | 边缘 WAF 拦截 | HTTP 405 + `x-amzn-waf-action: captcha`，或 202 + `challenge` | `rv=(e,t)=>405===e&&t["x-amzn-waf-action"]==="captcha"?"captcha":202===e&&t["x-amzn-waf-action"]==="challenge"&&"challenge"` |

> 对普通 chat 注册账号，`registerClientType` 是否等于 `"DSH"` 本次无法确认（【未知】）。代码逻辑上，只有 DSH 来源的 CNY 账号会被前端实名门槛挡住；但后端始终可能返回 biz_code 8。

### 3.3 创建路径没有 PoW【确定】

platform 的 PoW（guest challenge）只挂在注册/短信/邮箱验证码上，路径清单：

```js
["/v0/users/register",
 "/v0/users/register_by_mobile",
 "/v0/users/login_by_mobile_sms",
 "/api/v0/users/create_sms_verification_code",
 "/api/v0/users/create_email_verification_code"]
```

创建/列表/删除 API Key 都不在这个清单里；platform bundle 里也**没有** `x-ds-pow-response` 这个头；平台自己的 guest PoW 头是 `X-DS-Guest-PoW-Response`，且只用于上述注册/验证码请求。所以"建 Key 要过 PoW"不成立。

### 3.4 容易误判的字段

| 字段 | 实际用途 | 与建 Key 的关系 |
|---|---|---|
| `sso_id` | chat 的 `/api/v0/client/settings/report` 里传 `user.id`（"unauthenticated: sso_id missing"） | 不是建 Key 的参数/关卡 |
| `did` | 远程 feature store 的设备 id（`__ds_remote_feature_did`），用于 settings 上报 | 不是建 Key 的关卡 |
| `shumei` | 登录/注册/支付的验证码（`/api/v1/payments` 里还有 `shumei_device_id`） | 建 Key 前端路径不触发；但通用挑战可能用 shumei provider |
| `x-ds-challenge-response` | 通用风控挑战成功后重放请求的头 | 只在 40303 时出现 |
| `x-request-encryption-key` | 登录/注册等 auth 请求的 RSA+AES 包体加密头（`rc.platform` 集合） | 建 Key 不加密 |
| `x-dsh-auth-token` | DSH grant 的鉴权头 | 平台识别，但不是 platform Web token |

---

## 4. 结论：能不能自动建 Key

### 4.1 只登录 chat：不能【确定】

- chat token 在 platform 上无效（40003，第三方实测；结构上也支持）。
- 本次没有发现从 chat 登录态静默换 platform token 的入口：没有共享 cookie、没有 token 查询参数、没有公开的 SSO/OAuth 客户端注册。platform 自己的登录在 `/auth-api/v0/users/*`，需要账号密码/短信/微信/Google 等真实登录动作。
- 因此"用户登录 chat → App 自动建 Key"这条链路**走不通**。

### 4.2 方案 A（推荐）：platform 再登录一次 + 自动建 Key

流程：

1. 用户先在 App WebView 登录 chat.deepseek.com（现有计划）。
2. App 提示"为了自动创建 API Key，需要再登录一次 DeepSeek 开放平台（同一账号）"，打开 `https://platform.deepseek.com/sign_in`（同一个或第二个 WebView）。
3. 用户完成 platform 登录（手机号/邮箱+密码/短信；微信/Google 也可，但 WebView 里 Google OAuth 可能被拒，见 §6）。
4. App 在 platform origin 注入 JS 读取：
   - `JSON.parse(localStorage.getItem("userToken")).value` → Bearer token；
   - `localStorage.getItem("deepseek-device-id:platform")` → 设备 id。
5. App 原生 HTTP（或同源 WebView fetch）调用 §1 的接口：
   - 先 `GET /api/v0/users/get_api_keys` 看是否已有同名 Key；
   - 再 `POST /api/v0/users/edit_api_keys`（action=create）创建名为 `DeepseekChatApp` 的 Key；
   - 把返回的 `sensitive_id` 存进 Android Keystore/EncryptedSharedPreferences，之后不再展示。
6. 错误处理：
   - 40002/40003 → platform 登录失效，重新登录；
   - biz_code 1 → 列出现有 Key，提示用户删一个或复用（复用得先让用户粘原 Key，因为列表只返回打码值）；
   - biz_code 8 → 引导实名认证（platform 个人资料页）；
   - 40303/405/202 → 风控挑战，回退到 platform 页面让用户手动创建（原生端很难稳定解 Shumei/hCaptcha）。

这条路的可行性依据：创建是一个普通的带 Bearer 的 POST；前端创建路径没有验证码/短信/PoW；只要 platform token 有效，服务端没有额外的交互式验证（除风控挑战）。

### 4.3 方案 B（最稳的降级）：深链到 platform 让用户点一下

- 打开 `https://platform.deepseek.com/api_keys`（已登录则直接到列表）。
- 用户点"Create new API key" → 输入名称 → 创建 → 复制 Key。
- 注意：**没有 URL 参数能直接打开创建弹窗**（`createExpKey` 是 React Router 的 location state，不是 query）；深链只能到列表页。
- 如果 App 用系统浏览器/Custom Tabs 打开，App 读不到结果，只能让用户回来粘贴；如果用自己的 WebView 打开，可以注入 JS 读页面上的 Key，但实现更脆。

### 4.4 方案 C：手动粘贴 Key（M1 兜底）

- 用户自己在 platform 创建，回到 App 粘贴。
- 与现有 M1 计划一致，不阻塞其他功能。

### 4.5 不建议的路线

- **拿 chat token 直接打 platform**：40003，且属于跨服务混用。
- **模拟 DSH 的 `auth_init → auth_approve → auth_exchange` 流程**：这是 platform 给 DeepSeek Harness 的专用授权；第三方 App 冒充 Harness 获取 grant 属于滥用未公开接口，且 grant 不是 `sk-` API Key（是 `x-dsh-auth-token` 凭证），不能直接当 API Key 用。合规和稳定性都不可取。
- **自动填充用户密码/验证码登录 platform**：需要存储密码、破解 PoW/RSA/验证码，风险高且违反产品安全底线。
- **从用户浏览器 LevelDB 读 token**：Android 上不可行/隐私问题；DSH 桌面端这么做是桌面特权，不适用于 App。

补充：platform 的 Key 列表里已经存在 `key_type="DSH"` 的 app-managed Key（"DeepSeek Harness"，不能编辑/删除），说明平台有"应用托管 Key"机制；但 web bundle 里没有第三方应用注册/创建这种 Key 的入口，本次不认为它能被普通 App 复用。

---

## 5. 证据与复现

### 5.1 抓取物

| 文件 | URL | 用途 |
|---|---|---|
| platform 首页 | https://platform.deepseek.com/ | 确认 commit-id 64e05285、静态资源域名 |
| platform 主 JS | https://fe-static.deepseek.com/platform/static/main.01930e1575.js | chunk 映射、运行时 |
| platform 业务 chunk | https://fe-static.deepseek.com/platform/static/5697.092cb1c1d0.js | API Key 页面、接口、登录态、风控 |
| chat 首页 | https://chat.deepseek.com/ | 对比 chat 的 cookie / 资源 |
| chat 主 JS | https://fe-static.deepseek.com/chat/static/main.18fe4d5b7a.js | chat 的 `userToken` / header / 登录接口 |
| deepseek-exporter | https://github.com/pionxzh/deepseek-exporter/blob/main/src/api.ts | chat token 解析与请求头参考 |
| dsh-account-switcher | https://github.com/hyqweasd5-png/dsh-account-switcher | 第三方实测：chat token 在 platform 返回 40003；platform token 用法 |
| DeepSeek Harness | https://github.com/deepseek-ai/deepseek-harness/blob/master/packages/account/deepseek-account/README.zh.md | 官方 DSH 账号包：auth_init/approve/exchange、x-dsh-auth-token、五个客户端头 |
| CodexBar | https://github.com/steipete/CodexBar/blob/main/docs/deepseek.md | platform userToken 作为 Bearer 调 platform 私有接口 |
| dsh-cost-widget | https://dshplugin.online/plugins/zhangshaobo608--dsh-cost-widget | 说明 platform userToken 是 eyJ 开头的 JWT |

### 5.2 关键代码片段（platform chunk）

**登录 token 存储：**

```js
c = l.create("userToken", null, null)
d = l.create("__appKit_userInfo", {id:null}, null)
// 写入：
n = { value: t, __version: i }
localStorage.setItem(e, JSON.stringify(n))
```

**请求头：**

```js
{
  "x-client-bundle-id": t===aN ? "com.deepseek.chat.nov" : "com.deepseek.chat",
  "x-client-platform": a,
  "x-client-version": n,
  "x-client-locale": r,
  "x-client-timezone-offset": i
}
// 调用处：
({brand:s$.getBrand(), platform:sj.clientPlatform, version:"1.0.1",
  locale:s2.getLocale(), timezoneOffsetSeconds:60*ov()().utcOffset()})
```

**设备头：**

```js
nK = e => ({"x-device-id": e.deviceId, "x-device-model": e.deviceModel})
// 只对 /api/ 或 /auth-api/ 路径注入
if (n.origin !== location.origin ||
    (!n.pathname.startsWith("/api/") && !n.pathname.startsWith("/auth-api/"))) return;
e.headers = {...e.headers, ...nK(r)}
```

**创建调用：**

```js
hV({action:"create", name:m, createdAt:void 0, redactedKey:void 0, trackingId:null, isExp:t})
```

**删除调用：**

```js
hV({action:"delete", name:void 0, redactedKey:t.sensitiveId, createdAt:t.createdAt, trackingId:t.trackingId})
```

**实名门槛：**

```js
function h1(e){ return (e?.registerClientType) === "DSH" }
// API Keys 页面：
v = !!(c && h1(c)
      && null === c.identityVerificationId
      && null === c.businessVerificationId
      && "CNY" === c.currency)
```

**挑战重试：**

```js
// 40303 -> 响应 data 里取 provider/challenge_id，弹验证码
// 成功后：
{action:"retry", value:()=>({...t, headers:{...t.headers, "x-ds-challenge-response": o}})}
```

**DSH 托管 Key 的展示（说明平台存在 app-managed key，但 web bundle 没有创建入口）：**

```js
name: "DSH" === e.key_type ? dshKeyName : e.name
// 文案：dshKeyCantEdit / dshKeyCantDelete / dshKeyCantDisplay
```

### 5.3 匿名探测复现

```bash
# Node 的 fetch 可直连；curl 可能被 TLS 风控挡
node -e '
const h={authorization:"Bearer invalid-token-for-probe",accept:"application/json"};
fetch("https://platform.deepseek.com/api/v0/users/get_api_keys",{headers:h})
  .then(r=>r.text().then(t=>console.log(r.status,t)));
'
# -> 200 {"code":40003,"msg":"Authorization Failed (invalid token)","data":null}
```

不带 Authorization：

```bash
node -e '
fetch("https://platform.deepseek.com/api/v0/users/get_api_keys",{headers:{accept:"application/json"}})
  .then(r=>r.text().then(t=>console.log(r.status,t)));
'
# -> 200 {"code":40002,"msg":"Missing Token","data":null}
```

**没有发送任何带有效凭证的请求，因此没有创建任何 Key。**

### 5.4 证据等级汇总

| 结论 | 等级 | 依据 |
|---|---|---|
| 四个接口的路径/方法/请求体 | 确定 | chunk 5697 的 `uj` / `hV` / `hY` / `hq` 代码 |
| 成功信封两层 code/biz_code | 确定（结构）/ 未知（真实样本） | 前端解析 + 第三方 dsh-account-switcher 的 `readEnvelope` |
| 匿名 40002/40003、HTTP 200 | 确定 | 本次实测 |
| 无 CORS 头 | 确定 | 本次实测（OPTIONS 405，无 ACAO） |
| platform token 存 localStorage userToken | 确定 | chunk 5697 AppKit 代码 + 社区文档 |
| platform token 是 JWT | 高置信 | 第三方插件文档"以 eyJ 开头"；未解码 |
| chat token 不能用于 platform | 高置信 | dsh-account-switcher README 实测 + 结构证据；本次未登录复测 |
| 创建路径无验证码/短信/PoW | 确定（前端） | `hY` 代码、PoW 路径清单 |
| 实名门槛只命中 DSH+CNY 账号 | 确定（前端逻辑）/ 未知（普通账号值） | 页面组件 + `h1` |
| 后端可能返回 biz_code 8 | 确定（枚举存在）/ 未知（触发条件） | 错误码枚举 |
| 40303/WAF 风控存在 | 确定（代码） / 未知（触发频率） | 挑战处理 + `rv` |
| DSH app-managed key 存在 | 确定 | 列表映射 + 文案；DSH 官方包 |

---

## 6. 未知项与真机验证清单

1. **真实成功响应样本**：`api_keys` / `key_quota` 的字段完整性、默认 limit、Key 字符串格式（是否 `sk-` 开头）。需要一次带测试账号的只读/创建测试。
2. **创建是否实际触发 40303/WAF**：代码上可能，频率/阈值未知；需要真机多次测试。
3. **x-client-* / x-device-* 是否服务端强制**：匿名阶段不带也返回 40003，但成功阶段是否校验未知。
4. **platform token 有效期/刷新**：JWT exp 未解码；失效表现为 40003/401，需要重新登录。
5. **普通 chat 注册账号的 `registerClientType`**：是否为 `"DSH"`；若不是，前端实名门槛不会命中，但后端仍可能返回 8。
6. **Capacitor WebView 里的 platform 登录**：手机号/邮箱密码/SMS 应可用；Google OAuth 在嵌入式 WebView 常被 Google 拒绝（disallowed_useragent），需要真机验证；WeChat 登录也可能有 UA 限制。
7. **platform 登录是否有"记住我"**：token 能存多久；App 重启后是否还要再登录。
8. **DSH app-managed key 的创建端点**：web bundle 里没有；可能在 DSH 服务端内部。第三方 App 不应冒用。
9. **Key 名称/数量/频率的隐藏限制**：除 biz_code 1/2 外是否还有别的规则。
10. **多设备/风控**：用 App 自己生成的 `x-device-id` 与 platform 登录时的设备 id 不一致，是否会被判风险；建议直接用 WebView 里的 `deepseek-device-id:platform`。

**建议的验证步骤（用测试账号，不用主账号）：**

1. 在 App WebView 登录 platform，读 `userToken` + `deepseek-device-id:platform`。
2. `GET /api/v0/users/get_api_keys`：确认 `code=0`、`data.biz_code=0`，记录 `key_quota`。
3. `POST edit_api_keys` 创建 `name="DeepseekChatApp Test-<timestamp>"` 的 Key；确认返回 `api_key.sensitive_id` 是完整 Key。
4. 再 `GET` 列表，确认新 Key 的 `sensitive_id` 已打码。
5. `POST edit_api_keys` action=delete 删除这把测试 Key，确认列表恢复。
6. 用 chat token 重复第 2 步，确认返回 40003（验证第三方结论）。
7. 清空 WebView 存储后重试，观察是否触发风控/实名/数量限制。

---

## 7. 给 App 的落地建议（一句话版）

- **M1**：手动填 Key，保持现有计划。
- **M2**：把"自动建 Key"改成"chat 登录 + platform 再登录一次 + 自动建 Key"；UI 上明确告诉用户这是同一账号的第二次登录，避免被当成重复登录。
- **M2 失败兜底**：深链 `https://platform.deepseek.com/api_keys`，或让用户手动粘 Key。
- **安全**：Key 存 Android Keystore/EncryptedSharedPreferences，不写日志、不进 WebView localStorage；创建后只展示一次。
- **合规**：这是 platform 的未公开内部接口，可能随版本变化；不要自动化登录/注册/验证码，不要冒用 DSH 授权。
