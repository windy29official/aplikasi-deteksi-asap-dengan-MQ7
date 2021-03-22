package aplikasiispu.rajacoding.com;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import aplikasiispu.rajacoding.com.auth.LoginActivity;
import aplikasiispu.rajacoding.com.helper.Config;
import aplikasiispu.rajacoding.com.helper.SessionManager;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class MainActivity extends AppCompatActivity {
    private TextView text_asap;
    private TextView text_today, text_laporan, text_keterangan;
    String asap, batas = "1";
    public ProgressDialog pDialog;
    String hariIni;
    private PieChartView pieChartView;
    Handler handler = new Handler();
    Runnable refresh;
    int jumlah_aman, jumlah_bahaya;
    String ip;
    private ImageView img_logout;
    public String idadmin;
    public SessionManager SessionManager;
    private TextView text_asap1, text_asap3;
    int intensitas_asap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SessionManager = new SessionManager(getApplicationContext());
        SessionManager.checkLogin();
        HashMap<String, String> user = SessionManager.getUserDetails();
        idadmin = user.get(SessionManager.KEY_ID);
        ip = user.get(SessionManager.KEY_IP);

        text_asap = findViewById(R.id.text_asap);
        text_today = findViewById(R.id.text_today);
        text_laporan = findViewById(R.id.text_laporan);
        text_keterangan = findViewById(R.id.text_keterangan);
        pieChartView = findViewById(R.id.chart);
        img_logout = findViewById(R.id.img_logout);
        text_asap1 = findViewById(R.id.text_asap1);
        text_asap3 = findViewById(R.id.text_asap3);
        Date dateNow = Calendar.getInstance().getTime();
        hariIni = (String) DateFormat.format("EEEE", dateNow);

        text_laporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopHandler();
                Intent i = new Intent(MainActivity.this, LaporanActivity.class);
                startActivity(i);
            }
        });
        img_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Memuat Tampilan . .");
        showDialog();
        getToday();
        doTheAutoRefresh();
    }

    private void doTheAutoRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LoadIntensitas();
                LoadData();
                getCart();
                doTheAutoRefresh();

            }
        }, 3000);
    }

    private void logoutUser() {
        SessionManager.logoutUser();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void stopHandler() {
        handler.removeMessages(0);
    }

    private void getCart() {
        List<SliceValue> pieData = new ArrayList<>();
        pieData.add(new SliceValue(jumlah_bahaya, getResources().getColor(R.color.colorRed)));
        pieData.add(new SliceValue(jumlah_aman, getResources().getColor(R.color.colorGreen)));

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(14);
        pieChartData.setHasCenterCircle(true).setCenterText1("Data Asap").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
        pieChartView.setPieChartData(pieChartData);
    }

    private void getToday() {
        Date date = Calendar.getInstance().getTime();
        String tanggal = (String) DateFormat.format("d MMMM yyyy", date);
        String formatFix = hariIni + ", " + tanggal;
        text_today.setText(formatFix);
    }

    private void LoadIntensitas() {
        AndroidNetworking.post("http://" + ip + Config.HOST  + "load_intensitas.php")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.optJSONArray("result");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject responses = jsonArray.getJSONObject(i);
                                intensitas_asap = responses.optInt("intensitas_asap");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideDialog();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        hideDialog();
                    }
                });
    }

    private void LoadData() {
        AndroidNetworking.post("http://" + ip + Config.HOST  + "list_data.php")
                .addBodyParameter("batas", batas)
                .addBodyParameter("waktu", "N")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.optJSONArray("result");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject responses = jsonArray.getJSONObject(i);
                                asap = responses.optString("asap");
                                jumlah_aman = responses.optInt("jumlah_aman");
                                jumlah_bahaya = responses.optInt("jumlah_bahaya");
                            }

                            text_asap1.setText("(" + jumlah_bahaya + ")");
                            text_asap3.setText("(" + jumlah_aman + ")");

                            text_asap.setText(asap);
                            if (Integer.parseInt(asap) > intensitas_asap) {
                          //      stopHandler();
                                text_keterangan.setText("Sensor Gas terdeteksi Bahaya\nAda asap rokok.");
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("PERINGATAN !!!")
                                        .setMessage("Terdapat asap rokok di toilet ini.")
                                        .setCancelable(false)
                                        .setNegativeButton("TUTUP", null)
                                        .setPositiveButton("LAPORKAN", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                text_keterangan.setText("Sensor Gas terdeteksi Normal\nTidak ada asap rokok.");
                            }

                            hideDialog();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideDialog();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        hideDialog();
                    }
                });
    }

    private void showDialog() {
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
    }

    private void hideDialog() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }
}