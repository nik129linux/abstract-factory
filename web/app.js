// The page knows nothing about patterns: it sends whatever the client typed and
// shows what the program decided and built.

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

// ------------------------------------------------------------------ the thread

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
  const waiting = bubble("bot pending", "the waiter is thinking…");

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

// ------------------------------------- what the chef accepted and turned down

function renderLog(log) {
  const box = document.createElement("div");
  box.className = "msg log";
  box.innerHTML = "<b>the kitchen</b>" + log.map((row) => {
    const [verdict, role, attempt, detail] = row.split("|");
    const mark = verdict === "ok" ? "✓" : verdict === "no" ? "✗" : "!";
    return `<div class="row ${verdict}"><span>${mark}</span>
            <span class="who">${role} · try ${attempt}</span>
            <span class="what">${detail}</span></div>`;
  }).join("");
  $("thread").appendChild(box);
  $("thread").scrollTop = $("thread").scrollHeight;
}

// ------------------------------------------------------------------- the combo

function renderCombo(combo) {
  const box = document.createElement("div");
  box.className = "msg combo";

  const dishes = combo.courses.map((c) => `
    <div class="dish">
      <div class="dish-head">
        <span class="role">${c.role}</span>
        <span class="clazz">${c.clazz}</span>
        <span class="price">$${c.cost.toLocaleString("en-US")}</span>
      </div>
      <p class="name">${c.name || "—"}</p>
      <p class="ing">${c.ingredients.join(" · ")}</p>
      ${c.violations.length ? `<p class="flag">walked out of the family: ${c.violations.join(", ")}</p>` : ""}
      ${c.missing.length ? `<p class="flag">not stocked in the warehouse: ${c.missing.join(", ")}</p>` : ""}
    </div>`).join("");

  box.innerHTML = `
    <div class="combo-head">
      <div>
        <span class="tag">${combo.tradition} combo</span>
        <code>new ${combo.factory}()</code>
      </div>
      <div class="total ${combo.overBudget ? "over" : ""}">
        $${combo.costPerPerson.toLocaleString("en-US")} <small>per person</small>
        <div class="sub2">total $${combo.total.toLocaleString("en-US")}${
          combo.budget ? " · cap $" + combo.budget.toLocaleString("en-US") : ""}</div>
      </div>
    </div>
    ${dishes}
    <p class="guarantee ${combo.sameFamily ? "ok" : "bad"}">
      ${combo.sameFamily
        ? "all four came out of the same kitchen — the factory guarantees it, not a validation"
        : "families mixed: " + combo.used.join(", ")}
    </p>`;

  $("thread").appendChild(box);
  $("thread").scrollTop = $("thread").scrollHeight;
}

// ----------------------------------------------------------- counter-example

$("mixed").addEventListener("click", async () => {
  try {
    const bad = await api("mixed");
    const out = $("mixedOut");
    out.classList.remove("hidden");
    out.innerHTML = `
      <p>Built by hand with <code>new</code>, going around every factory. It compiles and runs:</p>
      <ul>${bad.classes.map((c, i) => `<li>${c} → ${bad.used[i]}</li>`).join("")}</ul>
      <p style="margin-top:.6rem">A japanese combo with pasta and panela lemonade.
         What prevents it above is not a validation: it is that without the factory
         there is no way to create a dish.</p>`;
  } catch (e) { fail(e); }
});
