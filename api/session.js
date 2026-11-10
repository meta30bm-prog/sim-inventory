import { authenticated, setSession, clearSession, json } from "../lib/server.js";

export default function handler(req, res) {
  if (req.method === "GET") return json(res, 200, { authenticated: authenticated(req) });
  if (req.method === "DELETE") { clearSession(res); return json(res, 200, { ok: true }); }
  if (req.method !== "POST") return json(res, 405, { error: "Método não permitido" });
  if (!process.env.ADMIN_PASSWORD || !process.env.SESSION_SECRET) {
    return json(res, 503, { error: "Configure ADMIN_PASSWORD e SESSION_SECRET nas variáveis do Vercel." });
  }
  const password = req.body?.password;
  if (typeof password !== "string" || password.length > 200 ||
      password !== process.env.ADMIN_PASSWORD) {
    return json(res, 401, { error: "Senha incorreta." });
  }
  setSession(res);
  return json(res, 200, { authenticated: true });
}
