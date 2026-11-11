package br.com.siminventory

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val api = "https://sim-inventory-meta30bm-8124.vercel.app/api/state"
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private lateinit var list: LinearLayout
    private var state = JSONObject()
    private var chips = JSONArray()
    private var query = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        refresh()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 22, 20, 12); setBackgroundColor(0xFFF4F6FA.toInt()) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val title = TextView(this).apply { text = "SIM Inventory"; textSize = 25f; setTextColor(0xFF14213D.toInt()); setTypeface(null, Typeface.BOLD) }
        status = TextView(this).apply { text = "Conectando ao banco…"; textSize = 13f; setTextColor(0xFF53627A.toInt()); setPadding(0, 5, 0, 16) }
        val refresh = button("Atualizar dados") { refresh() }
        val add = button("+ Cadastrar chip") { showAddDialog() }
        val search = EditText(this).apply { hint = "Pesquisar número, responsável ou ICCID"; singleLine = true }
        search.addTextChangedListener(SimpleWatcher { query = it; renderList() })
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(title); content.addView(status); content.addView(refresh); content.addView(add); content.addView(search)
        content.addView(TextView(this).apply { text = "Chips cadastrados"; textSize = 19f; setTypeface(null, Typeface.BOLD); setTextColor(0xFF14213D.toInt()); setPadding(0, 18, 0, 8) })
        content.addView(list)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label; isAllCaps = false; setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 }
    }

    private fun request(method: String, body: JSONObject? = null): JSONObject {
        val conn = URL(api).openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 15000; conn.readTimeout = 20000
        conn.setRequestProperty("Accept", "application/json")
        if (body != null) {
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        val text = stream.bufferedReader().use { it.readText() }
        val code = conn.responseCode
        conn.disconnect()
        if (code !in 200..299) throw Exception("Servidor respondeu HTTP $code")
        return JSONObject(text)
    }

    private fun refresh() {
        status.text = "Sincronizando com o banco…"
        executor.execute {
            try {
                val response = request("GET")
                val cloud = response.optJSONObject("state")
                state = cloud ?: JSONObject().put("chips", JSONArray()).put("moves", JSONArray()).put("boxes", JSONArray().put(JSONObject().put("id", 1).put("name", "Caixa 01")))
                chips = state.optJSONArray("chips") ?: JSONArray()
                runOnUiThread { status.text = "Conectado • ${chips.length()} chips"; renderList() }
            } catch (e: Exception) { runOnUiThread { status.text = "Falha na conexão: ${e.message}" } }
        }
    }

    private fun renderList() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        var count = 0
        for (i in chips.length() - 1 downTo 0) {
            val chip = chips.optJSONObject(i) ?: continue
            val haystack = listOf("phone", "owner", "iccid", "operator").joinToString(" ") { chip.optString(it) }
            if (!haystack.contains(query, true)) continue
            count++
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setPadding(14, 12, 14, 12); setBackgroundColor(0xFFFFFFFF.toInt())
                val lp = LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = 9; layoutParams = lp
            }
            card.addView(TextView(this).apply { text = chip.optString("phone", "Número não informado"); textSize = 18f; setTypeface(null, Typeface.BOLD); setTextColor(0xFF14213D.toInt()) })
            card.addView(TextView(this).apply {
                text = "${chip.optString("operator", "Sem operadora")} • ${chip.optString("owner", "Sem responsável")}\n${stockName(chip.optString("stock"))} • ${statusName(chip.optString("status"))}"
                textSize = 14f; setTextColor(0xFF53627A.toInt()); setPadding(0, 5, 0, 0)
            })
            list.addView(card)
        }
        if (count == 0) list.addView(TextView(this).apply { text = "Nenhum chip encontrado."; setPadding(8, 18, 8, 18); textSize = 15f })
    }

    private fun stockName(value: String) = when(value) { "nao_ativado" -> "Não ativado"; "ativado" -> "Ativado"; "utilizado" -> "Utilizado"; else -> value }
    private fun statusName(value: String) = when(value) { "disponivel" -> "Disponível"; "em_uso" -> "Em uso"; "bloqueado" -> "Bloqueado"; "retirado" -> "Retirado"; else -> value }

    private fun showAddDialog() {
        val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18, 4, 18, 4) }
        fun field(hintText: String) = EditText(this).apply { hint = hintText; singleLine = true; panel.addView(this) }
        val phone = field("Número telefônico (obrigatório)")
        val operator = field("Operadora (Vivo, Claro, TIM…)")
        val owner = field("Responsável")
        val iccid = field("ICCID")
        val notes = field("Observações")
        val stock = Spinner(this)
        stock.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Não ativado", "Ativado", "Utilizado"))
        panel.addView(stock)
        AlertDialogBuilder.show(this, panel) {
            val number = phone.text.toString().filter { it.isDigit() }
            if (number.length !in 10..13) { Toast.makeText(this, "Informe um telefone válido com DDD.", Toast.LENGTH_LONG).show(); return@show }
            val stockCode = listOf("nao_ativado", "ativado", "utilizado")[stock.selectedItemPosition]
            executor.execute {
                try {
                    // Always fetch the latest shared state before appending a record.
                    val latest = request("GET").optJSONObject("state") ?: state
                    val latestChips = latest.optJSONArray("chips") ?: JSONArray()
                    for (i in 0 until latestChips.length()) {
                        if (latestChips.optJSONObject(i)?.optString("phone")?.filter { it.isDigit() } == number) throw Exception("Este número já está cadastrado.")
                    }
                    val chip = JSONObject()
                        .put("id", UUID.randomUUID().toString()).put("phone", number)
                        .put("operator", operator.text.toString().trim()).put("owner", owner.text.toString().trim())
                        .put("iccid", iccid.text.toString().trim()).put("stock", stockCode)
                        .put("status", "disponivel").put("boxId", JSONObject.NULL).put("compartmentId", JSONObject.NULL)
                        .put("position", JSONObject.NULL).put("notes", notes.text.toString().trim())
                        .put("createdAt", java.time.Instant.now().toString()).put("updatedAt", java.time.Instant.now().toString())
                    latestChips.put(chip)
                    latest.put("chips", latestChips)
                    if (!latest.has("moves")) latest.put("moves", JSONArray())
                    if (!latest.has("boxes")) latest.put("boxes", JSONArray().put(JSONObject().put("id", 1).put("name", "Caixa 01")))
                    request("PUT", latest)
                    state = latest; chips = latestChips
                    runOnUiThread { status.text = "Sincronizado • ${chips.length()} chips"; renderList(); Toast.makeText(this, "Chip salvo no banco compartilhado.", Toast.LENGTH_LONG).show() }
                } catch (e: Exception) { runOnUiThread { Toast.makeText(this, "Não foi possível salvar: ${e.message}", Toast.LENGTH_LONG).show() } }
            }
        }
    }
}

private class SimpleWatcher(val changed: (String) -> Unit) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { changed(s?.toString() ?: "") }
    override fun afterTextChanged(s: android.text.Editable?) {}
}

private object AlertDialogBuilder {
    fun show(activity: Activity, view: View, save: () -> Unit) {
        android.app.AlertDialog.Builder(activity).setTitle("Cadastrar chip").setView(view)
            .setNegativeButton("Cancelar", null).setPositiveButton("Salvar", null).create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(-1).setOnClickListener {
                        save()
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }
    }
}
