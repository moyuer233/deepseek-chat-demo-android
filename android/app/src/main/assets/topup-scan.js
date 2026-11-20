(function () {
  if (window.__dsTopupScanInstalled) return;
  window.__dsTopupScanInstalled = true;
  var last = '';
  var MAX_PIXELS = 1500000;
  var MAX_NODES = 40;
  var ticks = 0;
  function report(text) {
    try { if (window.DSTopUp && window.DSTopUp.diag) window.DSTopUp.diag('report ' + String(text).slice(0, 40)); } catch (e) {}
    if (!text || text === last) return;
    last = text;
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
      try { if (window.DSTopUp && window.DSTopUp.diag) window.DSTopUp.diag('canvas ' + c.width + 'x' + c.height + ' -> ' + (code && code.data ? code.data.slice(0, 24) : 'null')); } catch (e) {}
      if (code && code.data) {
        report(code.data);
        try {
          var dataUrl = c.toDataURL('image/png');
          if (dataUrl && dataUrl.indexOf('data:image') === 0 && window.DSTopUp && window.DSTopUp.onQrImage) {
            window.DSTopUp.onQrImage(dataUrl, code.data);
          }
        } catch (e) {
          try { if (window.DSTopUp && window.DSTopUp.diag) window.DSTopUp.diag('toDataURL failed: ' + (e && e.message)); } catch (e2) {}
        }
      }
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
      ticks++;
      var nodes = document.querySelectorAll('canvas, img, svg');
      if (ticks % 4 === 1) { try { if (window.DSTopUp && window.DSTopUp.diag) window.DSTopUp.diag('tick frames=' + window.frames.length + ' iframes=' + document.querySelectorAll('iframe').length + ' nodes=' + nodes.length); } catch (e) {} }
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
