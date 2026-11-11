import { getState, putState, json } from "../lib/server.js";

export default async function handler(req, res) {
  try {
    if (req.method === "GET") return json(res, 200, { state: await getState() });
    if (req.method === "PUT") {
      const state = req.body;
      if (!state || !Array.isArray(state.chips) || !Array.isArray(state.moves) ||
          !Array.isArray(state.boxes) || JSON.stringify(state).length > 4_000_000) {
        return json(res, 400, { error: "Dados do inventário inválidos ou muito grandes." });
      }
      await putState(state);
      return json(res, 200, { ok: true, savedAt: new Date().toISOString() });
    }
    return json(res, 405, { error: "Método não permitido." });
  } catch (error) {
    console.error("SIM Inventory API:", error);
    return json(res, 500, { error: "Falha ao acessar o banco de dados." });
  }
}
