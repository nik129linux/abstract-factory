// La pagina no sabe nada de patrones: pide, muestra y deja que Java decida.

const $ = (id) => document.getElementById(id);

async function api(action, params = {}) {
  const url = "/api/" + action + "?" + new URLSearchParams(params);
  const res = await fetch(url);
  const data = await res.json();
  if (!res.ok || data.error) {
    throw new Error(data.error || ("HTTP " + res.status));
  }
  return data;
}

function fail(e) {
  $("err").textContent = e.message;
  $("err").classList.remove("hidden");
}

function clearFail() {
  $("err").classList.add("hidden");
}

// ------------------------------------------------------------------- arranque

api("state").then((s) => {
  $("agent").textContent = s.agent;
}).catch(fail);

// -------------------------------------------------------------- 1: el pedido

$("send").addEventListener("click", async () => {
  clearFail();
  const button = $("send");
  button.disabled = true;
  button.textContent = "el agente esta leyendo el pedido…";

  try {
    const pick = await api("choose", {
      diners: $("diners").value,
      occasion: $("occasion").value,
      restrictions: $("restrictions").value,
      notes: $("notes").value,
    });
    showChoice(pick);
    await fillAll(pick.courses);
    await showCheck();
  } catch (e) {
    fail(e);
  } finally {
    button.disabled = false;
    button.textContent = "Mandar otro pedido";
  }
});

// ------------------------------------------------------------ 2: la decision

function showChoice(pick) {
  $("tradition").textContent = "cocina " + pick.tradition;
  $("factory").textContent = "new " + pick.factory + "()";
  $("accent").textContent = pick.accent;
  $("choosePrompt").textContent = pick.prompt.trim();
  $("chooseReply").textContent = "→ " + pick.reply.trim();
  $("chooseCard").classList.remove("hidden");
}

// --------------------------------------------------------------- 3: platos

async function fillAll(slots) {
  const box = $("courses");
  box.innerHTML = "";
  $("menuCard").classList.remove("hidden");
  $("checkCard").classList.add("hidden");
  $("mixedOut").classList.add("hidden");

  for (const slot of slots) {
    const card = document.createElement("div");
    card.className = "course working";
    card.innerHTML = `
      <div class="course-head">
        <span class="role">${slot.role}</span>
        <span class="clazz">—</span>
      </div>
      <p class="rules">regla de la casa: ${slot.rules}</p>
      <p class="spin">el agente lo esta pensando…</p>`;
    box.appendChild(card);

    const dish = await api("fill", { n: slot.n });
    card.className = "course";
    card.querySelector(".clazz").textContent = dish.clazz;
    card.querySelector(".spin").remove();

    const title = document.createElement("p");
    title.className = "dish";
    title.textContent = dish.name;
    card.insertBefore(title, card.querySelector(".rules"));

    const chips = document.createElement("div");
    chips.className = "chips";
    for (const item of dish.ingredients) {
      const chip = document.createElement("span");
      chip.className = "chip";
      chip.textContent = item;
      chips.appendChild(chip);
    }
    card.appendChild(chips);

    const steps = document.createElement("ol");
    for (const step of dish.steps) {
      const li = document.createElement("li");
      li.textContent = step;
      steps.appendChild(li);
    }
    card.appendChild(steps);

    if (dish.violations.length) {
      const flag = document.createElement("p");
      flag.className = "flag";
      flag.textContent = "se salio de la familia: " + dish.violations.join(", ");
      card.appendChild(flag);
    }

    const raw = document.createElement("details");
    raw.innerHTML = `<summary>lo que contesto el modelo</summary>
      <pre class="reply">${escape(dish.reply.trim())}</pre>`;
    card.appendChild(raw);
  }
}

function escape(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

// ----------------------------------------------------------- 4: revisiones

async function showCheck() {
  const check = await api("check");
  $("checkCard").classList.remove("hidden");

  const family = $("familyResult");
  family.textContent = check.sameFamily
    ? "si — las cuatro son " + check.used.join(", ")
    : "no — " + check.used.join(", ");
  family.className = "result " + (check.sameFamily ? "ok" : "bad");

  const words = $("wordResult");
  words.textContent = check.violations.length
    ? "no — " + check.violations.join("; ")
    : "si — ninguna palabra prohibida";
  words.className = "result " + (check.violations.length ? "bad" : "ok");
}

$("mixed").addEventListener("click", async () => {
  clearFail();
  try {
    const bad = await api("mixed");
    const out = $("mixedOut");
    out.classList.remove("hidden");
    out.innerHTML = `
      <p>Armado a mano con <code>new</code>, sin pasar por ninguna fábrica.
         Compila, corre, y sale esto:</p>
      <ul>${bad.classes.map((c, i) => `<li>${c} → ${bad.used[i]}</li>`).join("")}</ul>
      <p style="margin-top:.6rem">Un menú japonés con pasta y limonada de panela.
         Lo que lo impide arriba no es una validación: es que sin la fábrica no
         hay forma de crear un plato.</p>`;
  } catch (e) {
    fail(e);
  }
});
