// js/ui.js
export function renderCards(container, title, rows, headers, headerLabels) {
  const html = [];
  html.push(`<div class="info-card">`);
  html.push(`<h2>${escapeHtml(title)}</h2>`);
  rows.forEach((r) => {
    headers.forEach((h, idx) => {
      const label = headerLabels?.[idx] || toTitle(h);
      html.push(`<p><strong>${escapeHtml(label)}:</strong> ${escapeHtml(r[h] || "")}</p>`);
    });
    if (headers.length && rows.length > 1) {
      html.push(`<hr style="border:none;border-top:1px solid #f0f0f0;margin:8px 0;"/>`);
    }
  });
  html.push(`</div>`);
  container.innerHTML = html.join("");
}

function toTitle(h) {
  return h.split("_").map(w => w ? (w[0].toUpperCase() + w.slice(1)) : "").join(" ").trim();
}
function escapeHtml(s) {
  return String(s)
    .replace(/&/g, "&amp;").replace(/</g, "&lt;")
    .replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}