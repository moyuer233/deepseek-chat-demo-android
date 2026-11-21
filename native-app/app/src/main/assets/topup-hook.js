(function () {
  if (window.__dsPayHookInstalled) return;
  window.__dsPayHookInstalled = true;
  function report(text) {
    try { if (window.DSTopUp && window.DSTopUp.diag) window.DSTopUp.diag(String(text).slice(0, 1500)); } catch (e) {}
  }
  report('[hook] installed ' + location.href);
  function interesting(url, body) {
    var s = String(body || '');
    if (/pay|charge|order|qr|wechat|weixin/i.test(url) || s.indexOf('code_url') >= 0 || s.indexOf('weixin://') >= 0) {
      report('[pay] ' + url + '\n' + s);
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
    xhr.__dsBody = arguments[0];
    xhr.addEventListener('load', function () {
      try {
        if (xhr.__dsUrl && /pay/i.test(xhr.__dsUrl)) {
          var body = String(xhr.__dsBody || '');
          report('[payreq] ' + xhr.__dsUrl);
          // 支付渠道就在请求体里：fallback_method_type = WECHAT / ALIPAY
          var fm = /"fallback_method_type"\s*:\s*"([A-Za-z_]+)"/.exec(body);
          if (fm) {
            var v = fm[1].toUpperCase();
            var method = v === 'ALIPAY' ? 'alipay' : (v === 'WECHAT' ? 'wechat' : '');
            if (method) {
              window.__dsPayMethod = method;
              report('[paymethod] ' + method);
            } else {
              window.__dsPayMethod = '';
            }
          } else {
            window.__dsPayMethod = '';
          }
          try { if (window.__dsResetPayMethod) window.__dsResetPayMethod(); } catch (e1) {}
          interesting(xhr.__dsUrl, xhr.responseText);
        }
      } catch (e) {}
    });
    return XS.apply(this, arguments);
  };
  var origFetch = window.fetch;
  if (origFetch) {
    window.fetch = function (input, init) {
      var reqBody = '';
      try { reqBody = String((init && init.body) || ''); } catch (e0) {}
      var p = origFetch.apply(this, arguments);
      try {
        p.then(function (res) {
          try {
            var url = String(res && res.url || '');
            if (/pay/i.test(url) && res.clone) {
              report('[payreq] ' + url);
              var fm = /"fallback_method_type"\s*:\s*"([A-Za-z_]+)"/.exec(reqBody);
              if (fm) {
                var v = fm[1].toUpperCase();
                var method = v === 'ALIPAY' ? 'alipay' : (v === 'WECHAT' ? 'wechat' : '');
                if (method) {
                  window.__dsPayMethod = method;
                  report('[paymethod] ' + method);
                } else {
                  window.__dsPayMethod = '';
                }
              } else {
                window.__dsPayMethod = '';
              }
              try { if (window.__dsResetPayMethod) window.__dsResetPayMethod(); } catch (e1) {}
              res.clone().text().then(function (t) { interesting(url, t); }).catch(function () {});
            }
          } catch (e) {}
        }).catch(function () {});
      } catch (e) {}
      return p;
    };
  }
})();
