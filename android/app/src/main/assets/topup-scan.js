(function () {
  if (window.__dsTopupScanInstalled) return;
  window.__dsTopupScanInstalled = true;
  var last = '';
  // 只认支付类二维码：微信登录码是 weixin://dl/…，支付码是 weixin://wxpay/bizpayurl?…
  function isPay(text) {
    var s = String(text).toLowerCase();
    if (s.indexOf('weixin://dl/') === 0) return false;
    return s.indexOf('wxpay') >= 0 || s.indexOf('alipay') >= 0 || s.indexOf('bizpayurl') >= 0;
  }
  function report(text) {
    if (!text || text === last) return;
    last = text;
    if (!isPay(text)) return;
    try { if (window.DSTopUp) window.DSTopUp.onQr(String(text)); } catch (e) {}
  }
  function scanCanvas(c) {
    try {
      var ctx = c.getContext('2d');
      if (!ctx || !c.width || !c.height) return;
      var data = ctx.getImageData(0, 0, c.width, c.height);
      var code = jsQR(data.data, data.width, data.height);
      if (code && code.data) report(code.data);
    } catch (e) {}
  }
  function scanImg(el) {
    try {
      if (!el.complete || !el.naturalWidth) return;
      var now = Date.now();
      if (el.__dsLast && now - el.__dsLast < 2500) return;
      el.__dsLast = now;
      var c = document.createElement('canvas');
      c.width = el.naturalWidth;
      c.height = el.naturalHeight;
      c.getContext('2d').drawImage(el, 0, 0);
      scanCanvas(c);
    } catch (e) {}
  }
  function scanSvg(svg) {
    try {
      var r = svg.getBoundingClientRect();
      if (r.width < 100 || r.height < 100) return;
      var now = Date.now();
      if (svg.__dsLast && now - svg.__dsLast < 2500) return;
      svg.__dsLast = now;
      var clone = svg.cloneNode(true);
      clone.setAttribute('width', String(Math.round(r.width)));
      clone.setAttribute('height', String(Math.round(r.height)));
      var xml = new XMLSerializer().serializeToString(clone);
      var url = URL.createObjectURL(new Blob([xml], { type: 'image/svg+xml;charset=utf-8' }));
      var img = new Image();
      img.onload = function () {
        try {
          var c = document.createElement('canvas');
          c.width = 512; c.height = 512;
          var ctx = c.getContext('2d');
          ctx.fillStyle = '#fff'; ctx.fillRect(0, 0, 512, 512);
          ctx.drawImage(img, 0, 0, 512, 512);
          scanCanvas(c);
        } catch (e) {}
        URL.revokeObjectURL(url);
      };
      img.onerror = function () { URL.revokeObjectURL(url); };
      img.src = url;
    } catch (e) {}
  }
  setInterval(function () {
    try {
      var nodes = document.querySelectorAll('img, canvas, svg');
      for (var i = 0; i < nodes.length && i < 80; i++) {
        var el = nodes[i];
        if (el.tagName === 'SVG') scanSvg(el);
        else if (el.tagName === 'CANVAS') scanCanvas(el);
        else scanImg(el);
      }
    } catch (e) {}
  }, 1500);
})();
