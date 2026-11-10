import { neon } from "@neondatabase/serverless";
import { createHmac, timingSafeEqual } from "node:crypto";

const COOKIE = "sim_inventory_session";
const DAY = 60 * 60 * 24;

function sqlClient() {
  if (!process.env.DATABASE_URL) throw new Error("DATABASE_URL não configurada");
  return neon(process.env.DATABASE_URL);
}
function sign(value) {
  return createHmac("sha256", process.env.SESSION_SECRET || "").update(value).digest("hex");
}
function cookieValue(req) {
  const raw = req.headers.cookie || "";
  const part = raw.split(";").map(x => x.trim()).find(x => x.startsWith(COOKIE + "="));
  return part ? decodeURIComponent(part.slice(COOKIE.length + 1)) : "";
}
export function authenticated(req) {
  if (!process.env.SESSION_SECRET) return false;
  const token = cookieValue(req);
  const [expires, signature] = token.split(".");
  if (!expires || !signature || Number(expires) < Math.floor(Date.now()/1000)) return false;
  const expected = sign(expires);
  try { return timingSafeEqual(Buffer.from(signature), Buffer.from(expected)); } catch { return false; }
}
export function setSession(res) {
  const expires = String(Math.floor(Date.now()/1000) + DAY);
  const token = expires + "." + sign(expires);
  res.setHeader("Set-Cookie", `${COOKIE}=${encodeURIComponent(token)}; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=${DAY}`);
}
export function clearSession(res) {
  res.setHeader("Set-Cookie", `${COOKIE}=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0`);
}
export function json(res, status, data) {
  res.statusCode = status;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Cache-Control", "no-store");
  res.end(JSON.stringify(data));
}
export async function ensureSchema(sql) {
  await sql`CREATE TABLE IF NOT EXISTS sim_inventory_state (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    payload JSONB NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
  )`;
}
export async function getState() {
  const sql = sqlClient();
  await ensureSchema(sql);
  const rows = await sql`SELECT payload FROM sim_inventory_state WHERE id = 1`;
  return rows.length ? rows[0].payload : null;
}
export async function putState(payload) {
  const sql = sqlClient();
  await ensureSchema(sql);
  await sql`INSERT INTO sim_inventory_state (id, payload, updated_at)
    VALUES (1, ${JSON.stringify(payload)}::jsonb, NOW())
    ON CONFLICT (id) DO UPDATE SET payload = EXCLUDED.payload, updated_at = NOW()`;
}
