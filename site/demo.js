const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

const question = '用一句话解释 1M 上下文意味着什么？';
const answer = '1M 上下文约等于一次读完《三体》三部曲，或者 3000 页技术文档——对话再长，也不用从头再来。';

const bubble = document.querySelector('.bubble');
const answerEl = document.getElementById('answer');
const ctxUsed = document.getElementById('ctxUsed');
const ctxPct = document.getElementById('ctxPct');
const meterFill = document.getElementById('meterFill');
const meterPct = document.getElementById('meterPct');
const meterValue = document.getElementById('meterValue');

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
let playing = false;

async function typeInto(el, text, speed) {
  el.textContent = '';
  for (const char of text) {
    el.textContent += char;
    await sleep(speed);
  }
}

async function playHero() {
  if (playing) return;
  playing = true;
  answerEl.classList.remove('done');
  bubble.textContent = '';
  answerEl.textContent = '';
  ctxUsed.textContent = '0';
  ctxPct.textContent = '0';
  await sleep(320);
  await typeInto(bubble, question, 34);
  await sleep(420);
  await typeInto(answerEl, answer, 22);
  answerEl.classList.add('done');
  // 状态行滚到 8.2K / 1%
  for (let step = 0; step <= 100; step += 4) {
    const ratio = step / 100;
    ctxUsed.textContent = ratio < 1 ? (8.2 * ratio).toFixed(1) + 'K' : '8.2K';
    ctxPct.textContent = String(Math.round(ratio));
    await sleep(12);
  }
  playing = false;
}

function setFinalHero() {
  bubble.textContent = question;
  answerEl.textContent = answer;
  answerEl.classList.add('done');
  ctxUsed.textContent = '8.2K';
  ctxPct.textContent = '1';
}

function animateMeter() {
  if (reduceMotion) {
    meterFill.style.width = '1%';
    meterPct.textContent = '1%';
    meterValue.innerHTML = '8.2K <em>/ 1M</em>';
    return;
  }
  meterFill.style.width = '1%';
  let n = 0;
  const timer = setInterval(() => {
    n += 1;
    if (n >= 1) {
      clearInterval(timer);
      meterPct.textContent = '1%';
      meterValue.innerHTML = '8.2K <em>/ 1M</em>';
    }
  }, 900);
}

if (reduceMotion) {
  setFinalHero();
  meterFill.style.width = '1%';
  meterPct.textContent = '1%';
  meterValue.innerHTML = '8.2K <em>/ 1M</em>';
} else {
  const hero = document.querySelector('.hero');
  const heroObserver = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) playHero();
      }
    },
    { threshold: 0.45 }
  );
  heroObserver.observe(hero);

  const meterCard = document.querySelector('.meter-card');
  if (meterCard) {
    const meterObserver = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            animateMeter();
            meterObserver.disconnect();
          }
        }
      },
      { threshold: 0.5 }
    );
    meterObserver.observe(meterCard);
  }
}

// 让手机上“输入框”有轻微呼吸感，但保持静态可读
const composerText = document.getElementById('composerText');
if (composerText && !reduceMotion) {
  const phrases = ['请输入消息', '试试：把这段 PDF 总结成三条', '试试：用表格对比两个方案'];
  let index = 0;
  setInterval(() => {
    index = (index + 1) % phrases.length;
    composerText.textContent = phrases[index];
  }, 4200);
}
