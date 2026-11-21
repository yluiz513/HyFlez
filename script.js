(() => {
  const launcher = document.getElementById('launcher');
  const panel = document.getElementById('panel');
  const backdrop = document.getElementById('backdrop');
  const closeButton = document.getElementById('closePanel');
  const tabs = [...document.querySelectorAll('.tab')];
  const pages = [...document.querySelectorAll('.tab-page')];
  const prefs = [...document.querySelectorAll('[data-pref]')];
  const defaults = { rgb: true, glow: true, compact: false, tips: true, haptics: false, remember: true };

  function openPanel(open) {
    panel.classList.toggle('open', open);
    backdrop.classList.toggle('show', open);
    launcher.setAttribute('aria-expanded', String(open));
    panel.setAttribute('aria-hidden', String(!open));
  }
  launcher.addEventListener('click', () => openPanel(!panel.classList.contains('open')));
  closeButton.addEventListener('click', () => openPanel(false));
  backdrop.addEventListener('click', () => openPanel(false));
  document.addEventListener('keydown', e => { if (e.key === 'Escape') openPanel(false); });

  tabs.forEach(tab => tab.addEventListener('click', () => {
    tabs.forEach(item => item.classList.toggle('active', item === tab));
    pages.forEach(page => page.classList.toggle('active', page.id === 'page-' + tab.dataset.tab));
  }));

  function applyPref(name, value) {
    if (name === 'rgb') document.body.classList.toggle('no-rgb', !value);
    if (name === 'glow') document.body.classList.toggle('no-glow', !value);
    if (name === 'compact') document.body.classList.toggle('compact', value);
  }
  function savePrefs() {
    const state = Object.fromEntries(prefs.map(input => [input.dataset.pref, input.checked]));
    if (state.remember) localStorage.setItem('hyflez-prefs', JSON.stringify(state));
    else localStorage.removeItem('hyflez-prefs');
  }
  let saved = {};
  try { saved = JSON.parse(localStorage.getItem('hyflez-prefs') || '{}') || {}; } catch (_) {}
  prefs.forEach(input => {
    const name = input.dataset.pref;
    if (Object.prototype.hasOwnProperty.call(saved, name)) input.checked = saved[name];
    applyPref(name, input.checked);
    input.addEventListener('change', () => {
      applyPref(name, input.checked);
      savePrefs();
      if (input.checked && document.getElementById('haptics')?.checked && navigator.vibrate) navigator.vibrate(12);
    });
  });
  document.getElementById('resetPrefs').addEventListener('click', () => {
    prefs.forEach(input => { input.checked = defaults[input.dataset.pref]; applyPref(input.dataset.pref, input.checked); });
    savePrefs();
  });
})();