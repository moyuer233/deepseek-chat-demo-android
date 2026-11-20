# 充值入口与微信支付技术调研

| 项 | 值 |
| --- | --- |
| 日期 | 2026-11-20 |
| 工程 | H:/dsapp（Capacitor 8.6.2，Android minSdk 24 / targetSdk 36） |
| 目标 | App 内置充值入口，打开 https://platform.deepseek.com/top_up ，并尽量拉起微信支付 |
| 方法 | 线上前端 bundle 静态分析 + 微信支付官方文档 + 本机 Capacitor 8.6.2 源码；**未做真机验证、未发起任何支付** |
| 结论一句话 | 当前 DeepSeek 充值页在手机浏览器/WebView 里**根本不提供微信支付**，三条路线都无法在 App 内拉起微信；能做的只有把支付宝/浏览器路径做顺，并把“微信支付需 PC 扫码或在微信内打开链接”讲清楚 |

## 0. 结论（先看这段）

1. **当前 DeepSeek 充值页在手机浏览器 / Android WebView（默认 Android UA）里不提供微信支付。** 用户选“微信”后点充值，页面只弹一个提示框（正文见 1.2），并给一个“复制充值链接”按钮；它不会创建支付订单，也不会产生 weixin:// 链接。
2. 我全量扫描了该站当前线上 **321 个可下载 JS chunk**：weixin://、wap/pay、mweb_url、wechat_h5、h5_url 命中 **0 次**；唯一的微信相关代码是“微信内置浏览器里的 JSAPI”使用的 WeixinJSBridge。
3. 因此路线 (b)「内置 WebView + shouldOverrideUrlLoading 拦截 weixin://」**在当前页面没有拦截对象**。三条路线的“微信支付成功率”都是 **0%**，差别只在支付宝兜底、支付后回跳、登录态和实现成本。
4. 微信官方也明确：H5 支付“不建议在 APP 端使用”，App 内应接 **APP 支付**；H5 支付还要求 Referer/域名严格匹配、h5_url 5 分钟有效、不能在微信内打开。第三方 App **无法**用 DeepSeek 的商户号去发起 APP 支付。
5. 推荐：**Custom Tabs（@capacitor/browser）或系统浏览器打开充值页**；UI 明确写“微信支付请在电脑扫码，或在微信里打开链接”。如果未来 DeepSeek 真的开放移动端微信支付，Capacitor 8 默认就会把 weixin:// 交给系统拉起微信，**不需要写拦截代码**。

## 1. DeepSeek 充值页的线上实现（核心事实）

### 1.1 抓取对象与复现方法

- 页面：https://platform.deepseek.com/top_up
- 入口 HTML 的 <code>meta name="commit-id" content="64e05285"</code>，只有一个入口脚本：
  https://fe-static.deepseek.com/platform/static/main.01930e1575.js
- 主体逻辑在懒加载 chunk：https://fe-static.deepseek.com/platform/static/5697.092cb1c1d0.js （约 1.1 MB）
- 复现：从入口 JS 里解析 rspack 的 chunk 映射（形如 <code>f.u=e=&gt;"static/"+e+"."+({...})[e]+".js"</code>），逐个下载后 grep。
- 结果：映射表 322 个 chunk，321 个可下载（chunk 3 返回 404，估计为未部署/已下线），对这 321 个 chunk 搜索微信支付关键字，命中 **0**。
- 用 Android/iPhone 手机 UA 与桌面 UA 分别请求该页，返回的入口脚本都是同一个 main.01930e1575.js，不存在“服务端给手机下发另一套 bundle”的情况。
- 抓取日期 2026-11-20；站点随时可能更新，结论以该时点为准。

### 1.2 移动端：微信被产品层直接拦住

充值按钮的核心逻辑（chunk 5697，已格式化为可读伪代码，变量名为压缩后的原名）：

~~~js
u = "wechat" === method
p = typeof WeixinJSBridge !== "undefined"

if (isMobile && p && u) {
  // 微信内置浏览器：走公众号 OAuth，再回 /wechat-pay 做 JSAPI
  openLink("https://open.weixin.qq.com/connect/oauth2/authorize"
         + "?appid=wx9fdce37c3d31a2de"
         + "&redirect_uri=" + location.origin + "/wechat-pay"
         + "&response_type=code&scope=snsapi_base&state=" + amount)
  return
}

if (isMobile && u) {
  // 微信外手机浏览器：只弹提示，不发支付请求
  openModal(<WeChatMobileCaveat/>)
  return
}

g = isMobile ? { method: "alipay_wap" } : { method: u ? "wechat" : "alipay" }
pay(g) // POST /api/v1/payments
~~~

提示框（组件 fN）的内容：

- 标题：<code>wechatPayModalIndeterminateTitle</code> = “微信支付”
- 正文（zh_CN，原文照录）：**“暂时不支持在移动端浏览器中完成微信支付。 你可以选择在在微信中打开 https://platform.deepseek.com/top_up，或在 PC 端进行微信支付。”**（原文如此，多了一个“在”）
- 英文：<code>weChatMobilePayCaveat</code> = “Currently, completing WeChat Pay in mobile browsers is not supported. You can choose to open the link(https://platform.deepseek.com/top_up) in WeChat, or proceed with WeChat Pay on a PC.”
- 按钮：<code>copyTopUpLinkButton</code> = “复制充值链接”
- 复制成功提示：**“复制成功，将该链接作为微信消息发送，随后打开该链接以完成微信支付”**

判定用的是页面内部的 <code>s$.isMobile</code>（mobile/tablet），默认 Android WebView 会命中移动端分支；只有把 UA 改成桌面才可能走 1.4 的二维码分支（不建议这么做，见 5.3）。

也就是说：**手机浏览器里 DeepSeek 既不做扫码，也不做 H5 跳转，而是让用户去 PC 或去微信里打开链接。**

### 1.3 微信内：公众号 JSAPI

微信内置浏览器里 <code>WeixinJSBridge</code> 存在，页面会跳转公众号 OAuth：

- appid：<code>wx9fdce37c3d31a2de</code>（DeepSeek 的公众号）
- redirect_uri：<code>https://platform.deepseek.com/wechat-pay</code>
- scope：<code>snsapi_base</code>

回到 <code>/wechat-pay</code> 后，前端调用 <code>POST /api/v1/payments</code>（<code>payment_method_type = WECHAT_JSAPI</code>），拿到 <code>jsapi_request_data</code>，再执行：

~~~js
WeixinJSBridge.invoke("getBrandWCPayRequest", params, callback)
~~~

这条路径**只在微信内置浏览器里成立**；官方 FAQ 也写明“JSAPI 支付必需从微信浏览器里调起”。

### 1.4 桌面：Native 扫码

桌面端选择微信或支付宝后，页面调用下单接口，拿到 <code>url</code>，用二维码组件渲染，并轮询：

~~~js
POST /api/v1/payments
body.order_info.payment_method_type = "WECHAT" / "ALIPAY" / "CMB_UNIONPAY"
轮询 payments/:orderId/capture
~~~

二维码弹窗文案：<code>wechatPayModalIndeterminateDescription</code> = “扫一扫付款”，有效期 15 分钟。

### 1.5 支付方式矩阵（当前线上版本）

| 打开环境 | 微信支付 | 支付宝 | 实际行为 |
| --- | --- | --- | --- |
| 桌面浏览器 | 二维码（Native） | 二维码（Native） | 正常扫码支付 |
| 手机浏览器（非微信） | **不可用** | alipay_wap 跳转 | 选微信→提示框；选支付宝→跳支付宝 |
| 微信内置浏览器 | JSAPI | 通常不可用（微信内会屏蔽 Alipay 跳转） | 微信内正常调起微信收银台 |
| Android WebView（本 App，默认 UA） | **不可用** | alipay_wap 跳转 | 与手机浏览器一致；页面仍弹提示框 |

补充：chunk 里还存在 <code>wechatH5ModalIndeterminateDescription</code>（“请在微信中完成支付，支付结果将在此页面更新。”）这个文案，但**代码里没有引用**，属于未启用的死代码；同时全量扫描没有任何 wechat_h5 / mweb_url / h5_url 字符串。

### 1.6 证据强度与边界

- **高置信**：代码路径、线上文案、全量 chunk 搜索；都属于可直接复现的静态证据。
- **未验证**：真机 UI、登录后的接口返回、页面未来改版。
- **重要边界**：支付方式、域名、Referer、是否允许 H5 完全由 DeepSeek 的前端/后端决定。App 侧无论用系统浏览器、WebView 还是 Custom Tabs，都改变不了“页面不提供微信支付”这个事实。

## 2. 微信 H5 支付（MWEB）在 Android WebView 里的真实行为

> 这一节回答“假设页面真的有 weixin://wap/pay，WebView 里会发生什么”。结论对当前 DeepSeek 页面不直接适用，但可作为未来兼容性依据。

### 2.1 产品定位：给“微信外的手机浏览器”用

微信支付官方定义：

> “H5支付是指商户在微信客户端外的移动端网页展示商品或服务，用户在前述页面确认使用微信支付时，商户发起本服务呼起微信客户端进行支付。主要用于触屏版的手机浏览器请求微信支付的场景。可以方便的从外部浏览器唤起微信支付。”
>
> —— https://pay.weixin.qq.com/doc/global/v2/zh/4013636513

并且官方明确：

> “H5支付不建议在APP端使用，如需要在APP中使用微信支付，请接APP支付。”
>
> —— https://pay.weixin.qq.com/doc/global/v3/zh/4012354168

### 2.2 必要条件

| 条件 | 说明 | 证据 |
| --- | --- | --- |
| 商户开通 H5 支付并配置 H5 支付域名 | 只有配置了域名的网页才能跳转 h5_url | 开发指引 4012791831 |
| 发起页域名与配置域名**完全一致** | 不能多一级 www、不能换子域 | 常见问题 4012791845 |
| 跳转 h5_url 的请求必须带 Referer | Referer 为空会报“商家参数格式有误” | 常见问题 4012791845 |
| App/WebView 要手动设置 Referer | 官方示例：<code>extraHeaders.put("Referer", "商户申请H5时提交的授权域名")</code> | 同上 |
| h5_url 5 分钟有效 | 超时需重新下单 | 开发指引 4012791831 |
| 下单参数齐全 | scene_info.payer_client_ip 必填；scene_info.h5_info.type 必填（Wap/iOS/Android） | H5 下单 4012791834 |
| 不能被微信内打开 | 微信内会报“请在微信外打开订单，进行支付” | 常见问题 4012791845 |
| 同一 h5_url 只能被一个微信号调起 | 换号要重新下单 | 常见问题 4012791845 |

### 2.3 WebView 里的两个硬故障

**故障一：Android WebView 默认不认识 weixin://**

- 微信 H5 的收银台中间页（wx.tenpay.com）会尝试跳到 <code>weixin://wap/pay?...</code>。
- WebView 默认会把未知 scheme 当普通 URL 加载，结果通常是 <code>net::ERR_UNKNOWN_URL_SCHEME</code>，页面白屏/报错；必须在 <code>shouldOverrideUrlLoading</code> 里拦截并交给系统 Intent。
- Android 官方对 <code>shouldOverrideUrlLoading</code> 的说明就是“给宿主应用一个接管 URL 加载的机会”：
  https://developer.android.google.cn/reference/android/webkit/WebViewClient
- 社区实践（含拦截代码）：
  https://blog.soarli.top/archives/986.html
  https://ask.csdn.net/questions/8626610

**故障二：Referer 丢失**

- 微信官方 FAQ 把“调起 H5 支付报‘商家参数格式有误’”的第一条原因就写成 referer 为空：
  > “当前调起 H5 支付的 referer 为空导致，一般是因为直接访问 h5_url 调起 H5 支付导致的…… 如果是 App 里调起 H5 支付，需要在 webview 中手动设置 referer。”
- 这意味着：即使 WebView 能加载 h5_url，只要 Referer 不是商户配置的域名，微信就会在收银台前拦下。

### 2.4 风控与安全验证

- 官方开发指引写明：微信支付收银台中间页会进行 **H5 权限的校验、安全性检查**，校验通过后才可支付（4012791831）。
- 社区有“App 内 WebView 接 H5 支付，跳到安全验证/验证码，验证后仍提示系统繁忙”的案例（微信开发者社区帖子，未复现）：
  https://developers.weixin.qq.com/community/pay/doc/000820e04acbc02b5ba46a6ba66c00
- 公开文档里**没有**“WebView UA 一定被拦”的条款；但风控规则不透明，无法承诺成功率。可以确定的是：**Referer/域名不对一定失败，微信内一定失败，未装微信一定失败。**

### 2.5 支付后回跳

官方开发指引：

> “当用户在收银台完成支付后点击完成按钮，或中途取消支付，页面会从收银台返回到拉起支付前的 H5 页面（若拉起收银台时有拼接 redirect_url 参数将返回指定页面）。”
>
> —— https://pay.weixin.qq.com/doc/v3/merchant/4012791831

限制：

- redirect_url 的域名必须也是商户配置的 H5 支付域名，且要 urlencode（4012791835）。
- 所以**不能**把 redirect_url 设成 <code>myapp://...</code> 这种自定义 scheme，也就没有“支付完自动深链回 App”的官方能力。
- 实际体验：微信付完点“完成”会切回拉起支付的浏览器/WebView；如果 App 在后台，需要用户自己切回来（或者按返回键）。

## 3. 三条路线对比与打分

### 3.1 现状（2026-11-20，DeepSeek 页面）

| 维度 | (a) 系统浏览器 | (b) 内置 WebView 拦截 | (c) Chrome Custom Tabs |
| --- | --- | --- | --- |
| 微信支付（当前页面） | **0%** | **0%** | **0%** |
| 原因 | 页面弹“不支持移动端微信” | 同上，且没有 weixin:// 可拦 | 同上 |
| 支付宝（当前页面） | 高 | 中（需 allowNavigation + alipays:// 拉起） | 高 |
| 支付后回跳 App | 差（手动切回） | 好（页面留在 App，onResume 可刷新） | 中（返回键/关闭事件回 App） |
| 实现复杂度 | 极低（0 行原生代码） | 中（配置 + 登录态 + 返回键 + 可选自定义 client） | 低（1 个官方插件） |
| 登录态 | 浏览器会话（可能已登录） | 独立 WebView 会话，需要登录 | 共享浏览器 Cookie |
| 风控/兼容风险 | 低（真实浏览器） | 中高（WebView UA/Referer；若未来有 H5） | 低（Chrome 内核） |
| 能否完成充值 | 能（支付宝） | 能（支付宝，需适配） | 能（支付宝） |

### 3.2 (a) 系统浏览器

**支付成功率风险**：当前页面微信 0%；支付宝 alipay_wap 在真实浏览器里成功率最高。未来若有 H5 微信支付，系统浏览器也是微信官方最推荐的环境（真实 UA、Referer 正常）。

**回跳 App**：最差。付款完成/取消后，用户停留在浏览器；需要自己切回 App。App 侧只能在 <code>appStateChange</code> 恢复时刷新余额或订单状态。

**实现复杂度**：最低。本项目里甚至不需要原生代码：Capacitor 的 <code>Bridge.launchIntent</code> 默认会把非 allowNavigation 的 http(s) 导航交给系统浏览器（见 4.1）。前端 <code>window.location.href = "https://platform.deepseek.com/top_up"</code> 即可。

### 3.3 (b) 内置 WebView + 拦截 weixin://

**支付成功率风险**：当前页面 0%，因为没有 weixin:// 产生；拦截代码没有触发机会。未来若 DeepSeek 开放 H5 支付：WebView 仍是微信官方“不建议”的场景，且 Referer 需要手动注入，成功率低于真实浏览器。

**回跳 App**：最好。页面一直在 App 的 WebView 里，微信付完切回 App 时页面还在，监听 <code>onResume</code>/<code>appStateChange</code> 刷新即可；理论上还能继续轮询订单状态。

**实现复杂度**：中。需要：
- <code>server.allowNavigation</code> 放行 platform.deepseek.com（否则 Capacitor 默认把页面丢到系统浏览器）；
- 独立的 WebView 登录态（platform 与 chat 不同域，不共享 cookie）；
- 返回键处理；
- 若未来要拦 weixin://，其实 Capacitor 8 已内置（见 4.1），只有想加“未安装微信”提示/日志时才需要自定义 WebViewClient。

### 3.4 (c) Chrome Custom Tabs

**支付成功率风险**：当前页面微信 0%；支付宝与系统浏览器同级别（Chrome 内核，真实 UA/Referer）。未来若有 H5 微信支付，Custom Tabs 也是真实 Chrome，成功率高。

**回跳 App**：中。Custom Tab 在任务栈上层，返回键即可回 App；<code>@capacitor/browser</code> 还提供 <code>browserFinished</code> 事件，可在关闭时刷新。比系统浏览器好，但不如 WebView 那样“页面一直活着”。

**实现复杂度**：低。官方插件 <code>@capacitor/browser</code>（8.0.x）在 Android 就是用 <code>CustomTabsIntent</code> 实现（已核对 npm 包内 Browser.java）；不需要改 minSdk（项目当前 24 即可）。

### 3.5 假设 DeepSeek 未来开放 H5 微信支付，加权打分

评分 1-5，5 最好；“微信成功率”权重 30%，“支付宝兜底”20%，“回跳”20%，“复杂度（5=最简单）”15%，“登录态”10%，“风控”5%。

| 路线 | 微信成功率 | 支付宝 | 回跳 | 复杂度 | 登录态 | 风控 | 加权分 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| (a) 系统浏览器 | 5 | 5 | 2 | 5 | 4 | 5 | **4.30** |
| (b) 内置 WebView | 3 | 3 | 5 | 2 | 2 | 2 | **3.10** |
| (c) Custom Tabs | 5 | 5 | 4 | 4 | 5 | 5 | **4.65** |

注意：这个表是“假设 H5 已开放”的路线质量；**现实是三者微信都是 0%**。

### 3.6 路线结论

- 如果目标是“让用户能充值”：**(c) Custom Tabs 最平衡**，(a) 系统浏览器是最省事的兜底。
- 如果目标是“App 内直接拉起微信支付”：**三条路线都做不到**，因为页面不产生微信支付请求。做 (b) 只会增加实现和登录成本，不会提高微信成功率。
- 只有一种情况值得上 (b)：产品明确要求“充值页必须留在 App 内”（品牌/转化考虑），且接受“微信支付仍不可用、只能支付宝”。这时 (b) 的代码量也很小。

## 4. Capacitor 8 的现成做法（含 (b) 的实现）

### 4.1 框架已经内置 weixin:// 拉起（重点）

本机安装的是 @capacitor/android 8.6.2。源码：

- <code>BridgeWebViewClient.shouldOverrideUrlLoading</code> 直接调用 <code>bridge.launchIntent(url)</code>：
  <code>node_modules/@capacitor/android/capacitor/src/main/java/com/getcapacitor/BridgeWebViewClient.java:27-30</code>
- <code>Bridge.launchIntent(Uri)</code> 对“非同源且不在 allowNavigation 里的 URL”执行：
  ~~~java
  Intent openIntent = new Intent(Intent.ACTION_VIEW, url);
  getContext().startActivity(openIntent);
  return true;
  ~~~
  <code>node_modules/@capacitor/android/capacitor/src/main/java/com/getcapacitor/Bridge.java:389-427</code>
- <code>Bridge.setWebViewClient(BridgeWebViewClient)</code> 是 public：
  <code>Bridge.java:1464-1471</code>
- <code>BridgeActivity.bridge</code> 是 protected，MainActivity 直接继承即可拿到：
  <code>BridgeActivity.java:13</code>

**结论：只要页面真的跳到 weixin://，Capacitor 8 默认就会用 ACTION_VIEW 拉起微信，不需要写拦截代码。** 这比网上大多数“自定义 WebViewClient 拦 weixin://”的方案更省事。

两个前提/坑：

- 这依赖 WebView 真的回调 <code>shouldOverrideUrlLoading</code>。JS 层 <code>location.href</code> 跳转通常没问题；服务端 302 重定向、iframe 内跳转在部分 WebView 版本可能不回调（社区已知兼容性坑，真机要验证）。
- 默认实现没有处理“未安装微信”（ActivityNotFoundException 被吞掉，用户无反馈），也没有日志/统计。

### 4.2 让充值页留在 WebView（(b) 的唯一必要配置）

<code>capacitor.config.ts</code>：

~~~ts
import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.moyuer233.deepseekchatapp',
  appName: 'DeepseekChatApp',
  webDir: 'dist',
  server: {
    // 只放行充值页域名；不要用 '*'，否则所有外链都会留在 App 内
    allowNavigation: ['platform.deepseek.com', '*.deepseek.com'],
  },
}

export default config
~~~

说明：

- allowNavigation 是按 host 匹配的掩码（<code>HostMask</code>），只影响**导航**，不影响 XHR/静态资源。
- 配了之后，前端跳转 <code>https://platform.deepseek.com/top_up</code> 会在主 WebView 里打开；页面里的 weixin:///alipays:// 仍会走 <code>launchIntent</code> 交给系统。
- 不配的话，Capacitor 默认把 platform.deepseek.com 丢到系统浏览器——也就是路线 (a)。

### 4.3 自定义 WebViewClient（只在需要提示/统计时）

默认行为已经够用；如果要在“未安装微信/支付宝”时给用户提示，或只允许特定域名留在 App 内，可以子类化：

~~~java
package com.moyuer233.deepseekchatapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import com.getcapacitor.Bridge;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;

public class MainActivity extends BridgeActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bridge bridge = getBridge();
    if (bridge != null) {
      bridge.setWebViewClient(new PayWebViewClient(bridge));
    }
  }

  static class PayWebViewClient extends BridgeWebViewClient {
    PayWebViewClient(Bridge bridge) { super(bridge); }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
      Uri uri = request.getUrl();
      String scheme = uri.getScheme();
      if ("weixin".equals(scheme) || "alipays".equals(scheme)) {
        try {
          Intent intent = new Intent(Intent.ACTION_VIEW, uri);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          view.getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
          // 这里换成你的提示：未安装微信/支付宝
        }
        return true;
      }
      // 其余 URL 保持 Capacitor 默认逻辑（含 allowNavigation / 外跳浏览器）
      return super.shouldOverrideUrlLoading(view, request);
    }
  }
}
~~~

注意：

- <code>Bridge.setWebViewClient</code> 虽然 public，但不是文档化 API；升级 Capacitor 大版本时要回归。
- 只重写 <code>shouldOverrideUrlLoading(WebView, WebResourceRequest)</code> 即可覆盖 API 24+；项目 minSdk 24。
- 想判断“微信是否安装”再决定是否拉起，需要 Android 11+ 的 <code>&lt;queries&gt;</code> 声明（见 4.5）。

### 4.4 社区插件与现成证据

| 方案 | 说明 | 链接 |
| --- | --- | --- |
| Capacitor 自带 launchIntent | 默认就会 ACTION_VIEW 拉起外部 scheme；(b) 无需写拦截也能拉起微信 | 本机源码 Bridge.java:389-427 |
| 自定义 WebViewClient | 社区做法：代理/继承原 client，重写 shouldOverrideUrlLoading | https://stackoverflow.com/questions/59152563（SO 403，搜索摘要可见）；https://forum.ionicframework.com/t/how-to-access-webview-errors-on-native-android/240810/2 |
| @capacitor/browser | 官方插件，Android 用 CustomTabsIntent；路线 (c) 用这个 | https://capacitorjs.com/docs/apis/browser |
| @capacitor/inappbrowser | 官方插件，三种模式（WebView/Custom Tabs/系统浏览器）；但要求 minSdk 26 | https://capacitorjs.com/docs/apis/inappbrowser |
| @capgo/capacitor-wechat | 原生微信 SDK（登录/分享/APP 支付），需要微信开放平台 appid + 商户号；**不适用于给 DeepSeek 充值** | https://www.npmjs.com/package/@capgo/capacitor-wechat |

### 4.5 其他注意点

- **未安装微信**：Capacitor 的 launchIntent 会捕获 ActivityNotFoundException 但不做任何提示（源码里的 TODO），用户会以为按钮没反应。
- **Android 11+ 包可见性**：startActivity 隐式 Intent 一般仍可拉起；但要在拉起前用 <code>resolveActivity</code>/<code>queryIntentActivities</code> 判断微信是否安装，需要 <code>&lt;queries&gt;</code> 声明 <code>weixin</code> scheme。参考：https://developer.android.google.cn/training/package-visibility/use-cases
- **返回键**：WebView 内多级页面时，返回键应先网页后退再退出 App；需要单独处理。
- **登录态**：platform.deepseek.com 与 chat.deepseek.com 不同源，App 现有 chat 登录态不能复用；WebView 里要重新登录一次（cookie 会持久化）。
- **风控**：DeepSeek 本身有设备环境风控（登录/支付都可能触发），在 WebView 里登录 platform 的风险未验证。

## 5. 建议方案

### 5.1 推荐落地（当前页面）

**方案 C（首选）：Custom Tabs**

~~~ts
import { Browser } from '@capacitor/browser'

await Browser.open({ url: 'https://platform.deepseek.com/top_up' })

// 页面关闭时刷新余额/订单状态
const h = await Browser.addListener('browserFinished', () => {
  // refreshBalance()
})
~~~

- 依赖：<code>pnpm add @capacitor/browser</code>，然后 <code>npx cap sync android</code>。
- 优点：真实 Chrome 内核、UA/Referer 正常；共享浏览器 Cookie，登录态通常还在；返回键自然回 App；有 browserFinished 事件。
- 成本：一个官方插件；不改 minSdk。

**方案 A（兜底）：系统浏览器**

- 零原生代码：按钮执行 <code>window.location.href = "https://platform.deepseek.com/top_up"</code>，Capacitor 默认会外跳（前提：没把该域名加进 allowNavigation）。
- 或者用 <code>@capacitor/inappbrowser</code> 的 <code>openInExternalBrowser</code>（注意 minSdk 26）。
- 优点：最省事；真实浏览器。
- 缺点：回跳要靠用户自己切回来。

**不推荐：方案 B（内置 WebView）作为“拉起微信”的手段。** 可以作为“充值页留在 App 内”的体验优化，但要接受微信支付仍然不可用；并且要额外处理登录态、返回键、未安装微信提示。

### 5.2 “微信支付”该怎么向用户交代

当前页面下，用户要微信支付只有两条路：

1. **PC 端**：电脑打开 https://platform.deepseek.com/top_up ，选微信，扫码支付。
2. **微信内打开链接**：把链接发到微信（文件传输助手/自己），在微信里点开，走公众号 JSAPI 支付。

App 可做的辅助：

- 在充值入口旁明确写：“微信支付请在电脑上扫码，或点‘复制链接’后在微信中打开。”
- 提供“复制链接”“分享到微信”（<code>Intent.ACTION_SEND</code> / 系统分享）按钮，减少用户操作。
- 不要试图用手机截图再“扫一扫相册”——体验差且二维码 15 分钟过期，不建议做成产品功能。

### 5.3 明确不要做

- **不要伪造 User-Agent 冒充桌面或微信**：桌面 UA 只会得到二维码（同一台手机扫不了自己的屏幕）；微信 UA 会走 JSAPI 但 WebView 里没有 WeixinJSBridge/openid，只会报错。
- **不要注入假的 WeixinJSBridge 或篡改支付接口**：属于绕过第三方支付流程，既有合规风险，也会因签名/openid 校验失败。
- **不要自己拼 h5_url / 代下单**：需要 DeepSeek 的商户私钥，第三方拿不到；自己找商户代收也改变不了 DeepSeek 的订单归属。
- **不要用 @capgo/capacitor-wechat 之类的原生 SDK**：那是用“你自己的商户号”收钱，无法给 DeepSeek 账户充值。

### 5.4 文案建议（可直接用）

- 入口页说明：
  “充值将在 DeepSeek 开放平台完成。微信支付暂不支持在手机浏览器内直接拉起，请在电脑上扫码，或复制链接到微信中打开。”
- 微信选项被点时的提示（与 DeepSeek 原文一致，避免用户困惑）：
  “暂时不支持在移动端浏览器中完成微信支付。你可以选择在微信中打开该链接，或在 PC 端进行微信支付。”
- 按钮：“复制充值链接”“在浏览器中打开”。

## 6. 真机验证清单

> 前提：本清单只验证“页面行为/跳转/返回”，**不要实际付款**；如需端到端支付，必须由用户授权并用最小金额。本次调研受只读约束，未执行。

| # | 测什么 | 步骤 | 预期 | 判定 |
| --- | --- | --- | --- | --- |
| 1 | 手机浏览器打开充值页 | Chrome 打开 top_up | 未登录→登录页；登录后→充值页 | 页面可达 |
| 2 | 手机浏览器选微信 | 选微信→点充值 | 弹“暂时不支持在移动端浏览器中完成微信支付”，**不跳微信**，不产生订单 | 与 1.2 一致 |
| 3 | 手机浏览器选支付宝 | 选支付宝→点充值 | 跳 alipay_wap/支付宝 | 支付宝可用 |
| 4 | 桌面浏览器 | PC Chrome 打开 top_up，选微信 | 弹二维码（“扫一扫付款”，15 分钟） | 桌面扫码可用 |
| 5 | 微信内打开链接 | 把链接发到微信文件传输助手→点开→登录→充值→微信 | 公众号 OAuth→JSAPI 收银台 | 微信内可付 |
| 6 | Custom Tabs 打开 | App 内 Browser.open(top_up) | 在 App 上方打开 Chrome Custom Tab，可登录/充值 | 路线 (c) 可用 |
| 7 | Custom Tabs 返回 | 支付页按返回/关闭 | 回到 App；browserFinished 触发 | 回跳正常 |
| 8 | 系统浏览器打开 | App 内 location.href 跳 top_up | 跳出到默认浏览器，App 留在后台 | 路线 (a) 可用 |
| 9 | WebView + allowNavigation（若做 b） | 配置后打开 top_up | 页面留在 App 内，可登录 | 页面可达 |
| 10 | WebView 内选微信（若做 b） | 同上，选微信→充值 | 仍弹“不支持移动端微信”，**不拉起微信** | 验证无对象可拦 |
| 11 | 未安装微信 | 卸载微信后打开充值页/点微信 | 不崩溃；若无自定义 client 则无提示 | 无崩溃 |
| 12 | 自定义 scheme 拉起（仅测试） | 用一个测试页/链接触发 weixin:// | Capacitor 默认 ACTION_VIEW 拉起微信；无微信时静默 | 机制成立 |
| 13 | Android 11+ 包可见性 | 在自定义 client 里 resolveActivity(weixin) | 未加 queries 时可能查不到；startActivity 一般仍可 | 记录实际结果 |
| 14 | 返回键 | WebView 多级页面按返回 | 先网页后退，再退出 App | 不闪退 |
| 15 | 支付结果刷新 | 支付宝支付完成返回 App（当前微信不可用） | onResume/appStateChange 后余额或订单状态更新 | 状态正确 |

建议的观测手段：

- <code>adb logcat | grep -iE "chromium|capacitor|ActivityNotFound"</code>，确认是否有 ERR_UNKNOWN_URL_SCHEME / 拉起失败。
- Chrome <code>chrome://inspect</code> 连 WebView，观察页面 DOM 与是否弹出提示框。
- 记录 UA：在页面 console 执行 <code>navigator.userAgent</code>，区分手机/桌面 UA 分支。

## 7. 未知与待验证

1. **DeepSeek 是否会在未来开放移动端 H5 微信支付**：未知；当前代码里没有任何 MWEB 路径。
2. **微信风控对 WebView 的具体规则**：官方无公开条款；只能确定 Referer/域名/微信内外/客户端安装这些硬条件。
3. **真机 UI**：本次未跑真机（只读约束），文案与分支来自线上 bundle，建议按第 6 节验证。
4. **充值是否当前可用**：bundle 里有“暂停 API 服务充值”的提示文案（topUpUnavailableAlert），可能是功能开关；真机需确认页面是否显示暂停提示。
5. **支付宝 WAP 在 WebView 里的完整回跳**：未验证；依赖 alipays:// 与 DeepSeek 的 return_url。
6. **Custom Tabs 在无 Chrome 设备的 fallback**：@capacitor/browser 依赖 CustomTabsIntent，具体 fallback 行为需在目标机型验证。

## 8. 证据索引

### 8.1 DeepSeek 线上 bundle（2026-11-20 抓取）

- 充值页：https://platform.deepseek.com/top_up
- 入口脚本：https://fe-static.deepseek.com/platform/static/main.01930e1575.js
- 支付逻辑 chunk：https://fe-static.deepseek.com/platform/static/5697.092cb1c1d0.js
- 页面 commit：<code>64e05285</code>（入口 HTML meta）
- 关键字符串（可在 chunk 5697 内 grep）：
  - <code>weChatMobilePayCaveat</code>
  - <code>copyTopUpLinkButton</code>
  - <code>topUpLinkCopyOkToast</code>
  - <code>wechatPayModalIndeterminateTitle</code>
  - <code>WeixinJSBridge</code>
  - <code>/api/v1/payments</code>
  - <code>/wechat-pay</code>
- 全量扫描结论：321 个可下载 chunk 中，<code>weixin://</code>、<code>wap/pay</code>、<code>mweb_url</code>、<code>wechat_h5</code>、<code>h5_url</code> 均 0 命中。

### 8.2 微信支付官方文档

- H5 支付产品介绍（global v2）：https://pay.weixin.qq.com/doc/global/v2/zh/4013636513
- H5 支付产品介绍（global v3，含“不建议在 APP 端使用”）：https://pay.weixin.qq.com/doc/global/v3/zh/4012354168
- H5 支付开发指引：https://pay.weixin.qq.com/doc/v3/merchant/4012791831
- H5 支付常见问题（Referer/微信内/5 分钟）：https://pay.weixin.qq.com/doc/v3/merchant/4012791845
- H5 下单 API（scene_info/h5_info）：https://pay.weixin.qq.com/doc/v3/merchant/4012791834
- H5 支付后返回指定页面：https://pay.weixin.qq.com/doc/v3/merchant/4012791835
- JSAPI 支付（微信内）：https://pay.weixin.qq.com/doc/v3/merchant/4012062524
- 技术常见问题（JSAPI 必须微信浏览器内调起）：https://pay.weixin.qq.com/doc/v3/merchant/4012791869
- JSAPI 支付常见问题（微信内 JSAPI / 微信外 H5）：https://pay.weixin.qq.com/doc/global/v3/zh/4012355028

### 8.3 Android / WebView / 社区

- Android WebViewClient（shouldOverrideUrlLoading）：https://developer.android.google.cn/reference/android/webkit/WebViewClient
- Android 包可见性 use cases：https://developer.android.google.cn/training/package-visibility/use-cases
- WebView 拉起微信支付实践（ERR_UNKNOWN_URL_SCHEME、拦截 weixin://、Referer）：https://blog.soarli.top/archives/986.html
- CSDN：Android WebView 唤起 weixin://wap/pay 常见问题：https://ask.csdn.net/questions/8626610
- 微信开发者社区：WebView 接 H5 支付遇安全验证/系统繁忙（未复现）：https://developers.weixin.qq.com/community/pay/doc/000820e04acbc02b5ba46a6ba66c00
- 华为开发者论坛：WebView 拉起微信后出现“选择浏览器”中间页（未复现）：https://developer.huawei.com/consumer/cn/forum/topic/0204176058546740405

### 8.4 Capacitor

- 本机版本：@capacitor/android 8.6.2（package.json）
- <code>BridgeWebViewClient.java:27-30</code>：shouldOverrideUrlLoading → bridge.launchIntent
- <code>Bridge.java:389-427</code>：launchIntent 对非同源 URL 执行 ACTION_VIEW
- <code>Bridge.java:1464-1471</code>：public setWebViewClient
- <code>BridgeActivity.java:11-52</code>：protected bridge / onCreate
- <code>HostMask.java:92-98</code>：空 allowNavigation 返回 false
- <code>CapConfig.java:259</code>：读取 server.allowNavigation
- 官方文档：https://capacitorjs.com/docs/config
- 官方 Browser 插件：https://capacitorjs.com/docs/apis/browser （Android 使用 CustomTabsIntent；npm 8.0.x 的 Browser.java 已核对）
- 官方 InAppBrowser 插件：https://capacitorjs.com/docs/apis/inappbrowser （minSdk 26）
- 社区 WebViewClient 代理/继承：https://stackoverflow.com/questions/59152563 、https://forum.ionicframework.com/t/how-to-access-webview-errors-on-native-android/240810/2
- 原生微信 SDK 插件（不适用本场景）：https://www.npmjs.com/package/@capgo/capacitor-wechat
