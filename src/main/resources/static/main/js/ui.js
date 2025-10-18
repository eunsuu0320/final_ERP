// /main/js/ui.js

(() => {
  if (window.toast) return; // 이미 정의돼 있으면 다시 안 만든다 (중복 방지)

  function ensureContainer() {
    let c = document.getElementById('__toast_container__');
    if (!c) {
      c = document.createElement('div');
      c.id = '__toast_container__';
      c.style.position = 'fixed';
      c.style.left = '50%';
      c.style.top = '24px';
      c.style.transform = 'translateX(-50%)';
      c.style.zIndex = '3000';
      c.style.display = 'flex';
      c.style.flexDirection = 'column';
      c.style.gap = '8px';
      c.style.pointerEvents = 'none'; // 클릭 방해 X
      document.body.appendChild(c);
    }
    return c;
  }

  function createToastEl(msg, type) {
    const el = document.createElement('div');
    el.textContent = msg;
    el.style.pointerEvents = 'auto';
    el.style.padding = '10px 14px';
    el.style.borderRadius = '6px';
    el.style.color = '#fff';
    el.style.boxShadow = '0 6px 16px rgba(0,0,0,.15)';
    el.style.background = (type === 'success') ? '#28a745'
                    : (type === 'error')   ? '#dc3545'
                    : '#4e73df';
    el.style.opacity = '1';
    el.style.transition = 'opacity .25s';
    return el;
  }

  function toast(msg, type = 'info', { duration = 2000 } = {}) {
    const c  = ensureContainer();
    const el = createToastEl(String(msg || '').trim() || '알림', type);
    c.appendChild(el);

    const t1 = setTimeout(() => { el.style.opacity = '0'; }, Math.max(500, duration - 300));
    const t2 = setTimeout(() => { el.remove(); }, Math.max(800, duration));

    // 메모리/타이머 정리 편의용
    el.__kill = () => { clearTimeout(t1); clearTimeout(t2); el.remove(); };
    return el;
  }

  // 전역 노출
  window.toast = toast;
  window.toastSuccess = (m, opt) => toast(m, 'success', opt);
  window.toastError   = (m, opt) => toast(m, 'error', opt);
})();
