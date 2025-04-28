package carreiras.com.github.cryptomonitor

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import carreiras.com.github.cryptomonitor.service.MercadoBitcoinServiceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configura toolbar e botão de atualização
        // Faz chamadas à API e exibe os dados

        val toolbarMain: Toolbar = findViewById(R.id.toolbar_main)
        configureToolbar(toolbarMain)

        val btnRefresh: Button = findViewById(R.id.btn_refresh)
        btnRefresh.setOnClickListener {
            makeRestCall()
        }
    }

    private fun configureToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(getColor(R.color.white))
        supportActionBar?.setTitle(getText(R.string.app_title))
        supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primary))
    }

    private fun makeRestCall() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val service = MercadoBitcoinServiceFactory().create()
                val response = service.getTicker()

                if (response.isSuccessful) {
                    val tickerResponse = response.body()

                    val quoteInfoLayout = findViewById<View>(R.id.component_quote_information)
                    val lblValue: TextView = quoteInfoLayout.findViewById(R.id.lbl_value)
                    val lblHigh: TextView = quoteInfoLayout.findViewById(R.id.lbl_high)
                    val lblLow: TextView = quoteInfoLayout.findViewById(R.id.lbl_low)
                    val lblBuy: TextView = quoteInfoLayout.findViewById(R.id.lbl_buy)
                    val lblSell: TextView = quoteInfoLayout.findViewById(R.id.lbl_sell)
                    val lblVol: TextView = quoteInfoLayout.findViewById(R.id.lbl_vol)
                    val lblDate: TextView = quoteInfoLayout.findViewById(R.id.lbl_date)

                    val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    val btcFormat = DecimalFormat("#,##0.########")

                    tickerResponse?.ticker?.let { ticker ->
                        ticker.last?.toDoubleOrNull()?.let {
                            lblValue.text = numberFormat.format(it)
                        }

                        ticker.high?.toDoubleOrNull()?.let {
                            lblHigh.text = "Alta: ${numberFormat.format(it)}"
                        }

                        ticker.low?.toDoubleOrNull()?.let {
                            lblLow.text = "Baixa: ${numberFormat.format(it)}"
                        }

                        ticker.buy?.toDoubleOrNull()?.let {
                            lblBuy.text = "Compra: ${numberFormat.format(it)}"
                        }

                        ticker.sell?.toDoubleOrNull()?.let {
                            lblSell.text = "Venda: ${numberFormat.format(it)}"
                        }

                        ticker.vol?.toDoubleOrNull()?.let {
                            lblVol.text = "Vol: ${btcFormat.format(it)} BTC"
                        }

                        ticker.date?.let {
                            lblDate.text = sdf.format(Date(it * 1000L))
                        }
                    }

                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Bad Request"
                        401 -> "Unauthorized"
                        403 -> "Forbidden"
                        404 -> "Not Found"
                        else -> "Unknown error"
                    }
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Falha na chamada: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}