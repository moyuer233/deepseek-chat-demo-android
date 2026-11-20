# DSH 客户端登录 → API 凭证原理（2026-11-20 调研）

> 目标：回答「DSH 登录为什么能直接用」，判断我们的 Android App 能否复用。
> 证据：`moyuer233/deepseek-harness` fork（2026-11-20 创建）+ 直接读 master 源码 + 端点实测。

## 0. 结论

**DSH 登录不创建 API Key。** 仓库里没有 `edit_api_keys` / `sensitive_id` / `createKey` 相关代码（GitHub code search 0 命中）。
它走的是 Platform 的 **DSH OAuth（PKCE）**，拿到一个 account grant，然后：

- 账号接口：`GET /auth-api/v0/users/current`、`/api/v0/users/get_user_summary`，凭证放在 **`x-dsh-auth-token`** 头里；
- 聊天接口：同样是 `x-dsh-auth-token`，直接打 `https://api.deepseek.com`（`dsh-llm-deepseek-account` README：「Listing requests carry the account token in x-dsh-auth-token」，模型列表 `GET /models`）。

也就是说，**account grant 本身就是 API 凭证，不需要 API Key**。

## 1. 登录四步（源码：packages/account/deepseek-account/src/index.ts:777-802）

1. `POST https://platform.deepseek.com/auth-api/v0/dsh/auth_init`
   body：`code_challenge`(S256)、`code_challenge_method`、`state`、`redirect_uri`、`locale`、`login_source`
   头：`x-client-bundle-id`(空)、`x-client-platform`、`x-client-version`、`x-client-locale`、`x-client-timezone-offset`
   → 返回 `authorize_url`、`authorize_id`、`expires_in`（实测 600 秒）。
2. 浏览器打开 `https://platform.deepseek.com/dsh/authorize?authorize_id=…`，用户登录/授权。
3. Platform 回调 `redirect_uri`（必须是 loopback HTTP：`http://127.0.0.1:<port>/oauth/callback`，README 明确只支持 localhost/127.0.0.1/[::1] + 显式端口）。
4. `POST /auth-api/v0/dsh/auth_exchange`
   body：`code`、`code_verifier`、`redirect_uri`、`device_id`(UUID)、`device_model`(如 win32-x64)、`os_version`
   → `{ token, authorized_url, user? }`；token 存本地即完成登录。

## 2. 实测记录（curl，走代理）

- 不带 `origin`/`referer`/`UA`：被 WAF 拦（返回「Request Blocked」HTML）。
- redirect_uri 用 `https://localhost/oauth/callback`：`{"code":50005,"msg":"PAYLOAD_SCHEMA_MISMATCH"}`。
- redirect_uri 用 `http://127.0.0.1:18765/oauth/callback` + 浏览器头：
  `{"code":0,"msg":"","data":{"biz_code":0,"biz_msg":"","biz_data":{"authorize_url":"https://platform.deepseek.com/dsh/authorize?authorize_id=i0VRZ…","authorize_id":"i0VRZ…","expires_in":600}}}`

## 3. 对我们的 App 意味着什么

- **可以不再建 API Key**：登录拿到 grant 后，聊天请求带 `x-dsh-auth-token` 即可（需改 chat-demo 的请求层）。
- **授权只需一次**：platform 的 /dsh/authorize 页；WebView 里拦截 loopback 回调即可拿 code，不必起本地 HTTP server。
- **会话导出仍需要 chat.deepseek.com 的 userToken**（`/api/v0/export_all` 在 chat 域），与 platform grant 是两套；
  chat 与 platform 是否共享登录态（SSO）**未验证**——如果共享，就能做到「一次登录，导入 + 聊天全通」。
- 未验证：grant 能否调 `POST /api/v0/users/edit_api_keys`（若能，旧方案也能继续用 Key）。

## 4. 待验证清单（需要真实账号）

1. WebView 打开 authorize_url → 登录 → 能否拦截 `http://127.0.0.1:<port>/oauth/callback?code=…&state=…`。
2. auth_exchange 拿 token。
3. token 打 `GET https://api.deepseek.com/models`（x-dsh-auth-token）是否 200。
4. token 打 platform `edit_api_keys` 是否 200。
5. chat.deepseek.com 是否因同一浏览器登录态而免登录（SSO）。
