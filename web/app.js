// La pagina no sabe nada de patrones: manda lo que escribe el cliente y muestra
// lo que el programa decidio y construyo.

const $ = (id) => document.getElementById(id);

async function api(action, params = {}) {
  const res = await fetch("/api/" + action + "?" + new URLSearchParams(params));
  const data = await res.json();
  if (!res.ok || data.error) throw new Error(data.error || ("HTTP " + res.status));
  return data;
}

function fail(e) {
  $("err").textContent = e.message;
  $("err").classList.remove("hidden");
}

api("state").then((s) => { $("agent").textContent = s.agent; }).catch(fail);

// ------------------------------------------------------------------- el hilo

function bubble(who, text) {
  const div = document.createElement("div");
  div.className = "msg " + who;
  div.textContent = text;
  $("thread").appendChild(div);
  $("thread").scrollTop = $("thread").scrollHeight;
  return div;
}

async function send(text) {
  if (!text.trim()) return;
  $("err").classList.add("hidden");
  $("text").value = "";
  $("send").disabled = true;

  bubble("me", text);
  const waiting = bubble("bot pending", "el mesero está pensando…");

  try {
    const turn = await api("say", { text });
    waiting.remove();
    bubble("bot", turn.say);
    markAction(turn.action);
    if (turn.log.length) renderLog(turn.log);
    if (turn.combo) renderCombo(turn.combo);
  } catch (e) {
    waiting.remove();
    fail(e);
  } finally {
    $("send").disabled = false;
    $("text").focus();
  }
}

$("send").addEventListener("click", () => send($("text").value));
$("text").addEventListener("keydown", (e) => { if (e.key === "Enter") send($("text").value); });
document.querySelectorAll(".chip-btn").forEach((b) =>
  b.addEventListener("click", () => send(b.dataset.say)));

function markAction(action) {
  document.querySelectorAll(".act").forEach((el) =>
    el.classList.toggle("on", el.dataset.act === action));
}

// ---------------------------------------------- lo que el chef acepto y rechazo

function renderLog(log) {
  const box = document.createElement("div");
  box.className = "msg log";
  box.innerHTML = "<b>la cocina</b>" + log.map((row) => {
    const [verdict, role, attempt, detail] = row.split("|");
    const mark = verdict === "ok" ? "✓" : verdict === "no" ? "✗" : "!";
    return `<div class="row ${verdict}"><span>${mark}</span>
            <span class="who">${role} · intento ${attempt}</span>
            <span class="what">${detail}</span></div>`;
  }).join("");
  $("thread").appendChild(box);
  $("thread").scrollTop = $("thread").scrollHeight;
}

// ------------------------------------------------------------------- el combo

function renderCombo(combo) {
  const box = document.createElement("div");
  box.className = "msg combo";

  const dishes = combo.courses.map((c) => `
    <div class="dish">
      <div class="dish-head">
        <span class="role">${c.role}</span>
        <span class="clazz">${c.clazz}</span>
        <span class="price">$${c.cost.toLocaleString("es-CO")}</span>
      </div>
      <p class="name">${c.name || "—"}</p>
      <p class="ing">${c.ingredients.join(" · ")}</p>
      ${c.violations.length ? `<p class="flag">se salió de la familia: ${c.violations.join(", ")}</p>` : ""}
      ${c.missing.length ? `<p class="flag">no está en la bodega: ${c.missing.join(", ")}</p>` : ""}
    </div>`).join("");

  box.innerHTML = `
    <div class="combo-head">
      <div>
        <span class="tag">combo ${combo.tradition}</span>
        <code>new ${combo.factory}()</code>
      </div>
      <div class="total ${combo.overBudget ? "over" : ""}">
        $${combo.costPerPerson.toLocaleString("es-CO")} <small>por persona</small>
        <div class="sub2">total $${combo.total.toLocaleString("es-CO")}${
          combo.budget ? " · tope $" + combo.budget.toLocaleString("es-CO") : ""}</div>
      </div>
    </div>
    ${dishes}
    <p class="guarantee ${combo.sameFamily ? "ok" : "bad"}">
      ${combo.sameFamily
        ? "los cuatro salieron de la misma cocina — lo garantiza la fábrica, no una validación"
        : "familias mezcladas: " + combo.used.join(", ")}
    </p>`;

  $("thread").appendChild(box);
  $("thread").scrollTop = $("thread").scrollHeight;
}

// ------------------------------------------------------------- contraejemplo

$("mixed").addEventListener("click", async () => {
  try {
    const bad = await api("mixed");
    const out = $("mixedOut");
    out.classList.remove("hidden");
    out.innerHTML = `
      <p>Armado a mano con <code>new</code>, sin pasar por ninguna fábrica. Compila y corre:</p>
      <ul>${bad.classes.map((c, i) => `<li>${c} → ${bad.used[i]}</li>`).join("")}</ul>
      <p style="margin-top:.6rem">Un combo japonés con pasta y limonada de panela.
         Lo que lo impide arriba no es una validación: es que sin la fábrica no hay
         forma de crear un plato.</p>`;
  } catch (e) { fail(e); }
});
