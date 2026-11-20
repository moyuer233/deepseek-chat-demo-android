(function () {
  if (window.__dsTopupScanInstalled) return;
  window.__dsTopupScanInstalled = true;
  var last = '';
  var MAX_PIXELS = 1500000;
  var MAX_NODES = 40;
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
  function visible(el) {
    var r = el.getBoundingClientRect();
    if (r.width < 100 || r.height < 100) return false;
    var vh = window.innerHeight || 0;
    var vw = window.innerWidth || 0;
    return r.bottom > -200 && r.top < vh + 200 && r.right > -200 && r.left < vw + 200;
  }
  function scanCanvas(c) {
    try {
      if (!c.width || !c.height || c.width * c.height > MAX_PIXELS) return;
      var now = Date.now();
      if (c.__dsLast && now - c.__dsLast < 2500) return;
      c.__dsLast = now;
      var ctx = c.getContext('2d');
      if (!ctx) return;
      var data = ctx.getImageData(0, 0, c.width, c.height);
      var code = jsQR(data.data, data.width, data.height);
      if (code && code.data) report(code.data);
    } catch (e) {}
  }
  function scanImg(el) {
    try {
      if (!el.complete || !el.naturalWidth) return;
      if (el.naturalWidth * el.naturalHeight > MAX_PIXELS) return;
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
      if (!visible(svg)) return;
      var now = Date.now();
      if (svg.__dsLast && now - svg.__dsLast < 2500) return;
      svg.__dsLast = now;
      var clone = svg.cloneNode(true);
      clone.setAttribute('width', '512');
      clone.setAttribute('height', '512');
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
      var nodes = document.querySelectorAll('canvas, img, svg');
      var scanned = 0;
      for (var i = 0; i < nodes.length && scanned < MAX_NODES; i++) {
        var el = nodes[i];
        if (!visible(el)) continue;
        scanned++;
        if (el.tagName === 'SVG') scanSvg(el);
        else if (el.tagName === 'CANVAS') scanCanvas(el);
        else scanImg(el);
      }
    } catch (e) {}
  }, 1500);
})();
