(function () {
  if (window.__dsPayHookInstalled) return;
  window.__dsPayHookInstalled = true;
  function report(text) {
    try { if (window.DSTopUp && window.DSTopUp.diag) window.DSTopUp.diag(String(text).slice(0, 1500)); } catch (e) {}
  }
  report('[hook] installed ' + location.href);
  function interesting(url, body) {
    var s = String(body || '');
    // 只记录命中类型，不把响应体（可能含订单号/金额/二维码内容）写进 logcat
    if (/pay|charge|order|qr|wechat|weixin/i.test(url) || s.indexOf('code_url') >= 0 || s.indexOf('weixin://') >= 0) {
      report('[pay] ' + url + ' hit=' + (s.indexOf('weixin://') >= 0 ? 'weixin' : s.indexOf('code_url') >= 0 ? 'code_url' : 'url'));
    }
    // 支付接口若返回 weixin:// 链接，直接交给原生拉起微信
    var m = s.match(/weixin:\/\/[^"'\\\s]+/);
    if (m) {
      try { if (window.DSTopUp && window.DSTopUp.onQr) window.DSTopUp.onQr(m[0]); } catch (e) {}
    }
  }
  var XO = XMLHttpRequest.prototype.open;
  var XS = XMLHttpRequest.prototype.send;
  XMLHttpRequest.prototype.open = function (method, url) {
    this.__dsUrl = String(url || '');
    return XO.apply(this, arguments);
  };
  XMLHttpRequest.prototype.send = function () {
    var xhr = this;
    xhr.addEventListener('load', function () {
      try { if (xhr.__dsUrl && /pay/i.test(xhr.__dsUrl)) interesting(xhr.__dsUrl, xhr.responseText); } catch (e) {}
    });
    return XS.apply(this, arguments);
  };
  var origFetch = window.fetch;
  if (origFetch) {
    window.fetch = function () {
      var p = origFetch.apply(this, arguments);
      try {
        p.then(function (res) {
          try {
            var url = String(res && res.url || '');
            if (/pay/i.test(url) && res.clone) {
              res.clone().text().then(function (t) { interesting(url, t); }).catch(function () {});
            }
          } catch (e) {}
        }).catch(function () {});
      } catch (e) {}
      return p;
    };
  }
})();
