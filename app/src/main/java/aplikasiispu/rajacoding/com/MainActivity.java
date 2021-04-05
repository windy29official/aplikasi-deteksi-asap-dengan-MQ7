package aplikasiispu.rajacoding.com;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    String intensitas_asap2;
    private RelativeLayout rl_bad, rl_safe;
    private static final String isPlaying = "Media is Playing";
    private MediaPlayer player;
    private TextView text_default;

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
        rl_bad = findViewById(R.id.rl_bad);
        rl_safe = findViewById(R.id.rl_safe);
        text_default = findViewById(R.id.text_default);
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
        findViewById(R.id.cv_hubungi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean installed = appInstalledOrNot("com.whatsapp");
                if (installed) {
                    String receiver_number = "628978057872";
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setPackage("com.whatsapp");
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Konfirmasi.\nPemberitahuan bahwa ada asap rokok di dalam kamar mandi untuk sekarang. Untuk menjaga lingkungan Kampus IST Akpring yang bebas rokok untuk segera mengecek kamar mandi tersebut. \n\n*Powered by System Pendeteksi MQ-7 Skripsi Wirto*");
                    whatsappIntent.putExtra("jid", receiver_number + "@s.whatsapp.net");
                    startActivity(whatsappIntent);
                } else {
                    String nomor = "628978057872";
                    Intent panggil = new Intent(Intent.ACTION_DIAL);
                    panggil.setData(Uri.fromParts("tel", nomor, null));
                    startActivity(panggil);
                }
            }
        });
        findViewById(R.id.cv_bantuan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean installed = appInstalledOrNot("com.whatsapp");
                if (installed) {
                    String receiver_number = "628978057872";
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setPackage("com.whatsapp");
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Halo admin saya membutuhkan bantuan untuk penggunaan aplikasi Monitoring Bebas Rokok ini. \n\n\n*Powered by System Pendeteksi MQ-7 Skripsi Wirto*");
                    whatsappIntent.putExtra("jid", receiver_number + "@s.whatsapp.net");
                    startActivity(whatsappIntent);
                } else {
                    String nomor = "628978057872";
                    Intent panggil = new Intent(Intent.ACTION_DIAL);
                    panggil.setData(Uri.fromParts("tel", nomor, null));
                    startActivity(panggil);
                }
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
        AndroidNetworking.post("http://" + ip + Config.HOST + "load_intensitas.php")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.optJSONArray("result");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject responses = jsonArray.getJSONObject(i);
                                intensitas_asap = responses.optInt("intensitas_asap");
                            }

                            text_default.setText("Nilai Defaut MQ-7 Sekarang : " + intensitas_asap + " ppm");

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
        AndroidNetworking.post("http://" + ip + Config.HOST + "list_data.php")
                .addBodyParameter("waktu", "N")
                .addBodyParameter("menu", "menuutama")
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
                                intensitas_asap2 = responses.optString("intensitas_asap");
                                jumlah_aman = responses.optInt("jumlah_aman");
                                jumlah_bahaya = responses.optInt("jumlah_bahaya");
                            }

                            text_asap1.setText("(" + jumlah_bahaya + ")");
                            text_asap3.setText("(" + jumlah_aman + ")");

                            text_asap.setText(asap);
                            if (Integer.parseInt(asap) > intensitas_asap) {
                                text_keterangan.setText("Sensor Gas terdeteksi Bahaya\nAda asap rokok.");
                                rl_safe.setVisibility(View.GONE);
                                rl_bad.setVisibility(View.VISIBLE);

                                playSound();
                                PopUp_Laporkan();

                            } else {
                                text_keterangan.setText("Sensor Gas terdeteksi Normal\nTidak ada asap rokok.");
                                rl_safe.setVisibility(View.VISIBLE);
                                rl_bad.setVisibility(View.GONE);
                            }

                            findViewById(R.id.cv_intensitas).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Konfirmasi !!!")
                                            .setMessage("Terdapat perubahan intensitas nilai default Sensor MQ-7. Apakah mau melakukan perubahan?")
                                            .setCancelable(false)
                                            .setNegativeButton("Tidak", null)
                                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    UpdateIntensitas(asap);
                                                }
                                            })
                                            .show();
                                }
                            });

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

    private void UpdateIntensitas(String asap) {
        AndroidNetworking.get("http://" + ip + Config.HOST + "update_intensitas.php")
                .addQueryParameter("nilai_intensitas", asap)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Konfirmasi !!!")
                                .setMessage(response.optString("pesan"))
                                .setCancelable(false)
                                .setNegativeButton("Baiklah", null)
                                .show();
                    }

                    @Override
                    public void onError(ANError error) {
                        hideDialog();
                    }
                });
    }

//    private boolean appInstalledOrNot() {
//        PackageManager pm = getPackageManager();
//        boolean app_installed;
//        try {
//            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
//            app_installed = true;
//        } catch (PackageManager.NameNotFoundException e) {
//            app_installed = false;
//        }
//        return app_installed;
//    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public void openWhatsApp(String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://wa.me/+" + number + "?text=Konfirmasi.\nPemberitahuan bahwa ada asap rokok di dalam kamar mandi untuk sekarang. Untuk menjaga lingkungan Kampus IST Akpring yang bebas rokok untuk segera mengecek kamar mandi tersebut. \n\n*Powered by System Pendeteksi MQ-7 Skripsi Wierto*"));
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        pDialog.dismiss();
    }

    private void PopUp_Laporkan() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.model_popup_laporkan, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        final AlertDialog alertDialog = dialogBuilder.create();
        TextView text_tidak = dialogView.findViewById(R.id.text_tidak);
        TextView text_ya = dialogView.findViewById(R.id.text_ya);

        text_tidak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        text_ya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean installed = appInstalledOrNot("com.whatsapp");
                if (installed) {
                    String receiver_number = "628978057872";
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setPackage("com.whatsapp");
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Konfirmasi.\nPemberitahuan bahwa ada asap rokok di dalam kamar mandi untuk sekarang. Untuk menjaga lingkungan Kampus IST Akpring yang bebas rokok untuk segera mengecek kamar mandi tersebut. \n\n*Powered by System Pendeteksi MQ-7 Skripsi Wierto*");
                    whatsappIntent.putExtra("jid", receiver_number + "@s.whatsapp.net");
                    startActivity(whatsappIntent);
                } else {
                    String nomor = "628978057872";
                    Intent panggil = new Intent(Intent.ACTION_DIAL);
                    panggil.setData(Uri.fromParts("tel", nomor, null));
                    startActivity(panggil);
                }
//                boolean installed = appInstalledOrNot();
//                if (installed) {
//                    openWhatsApp("628978057872");
//                } else {
//                    String nomor = "08978057872";
//                    Intent panggil = new Intent(Intent.ACTION_DIAL);
//                    panggil.setData(Uri.fromParts("tel", nomor, null));
//                    startActivity(panggil);
//                }
            }
        });
        alertDialog.show();
    }

    private void playSound() {
        try {
            if (player.isPlaying()) {
                player.stop();
                player.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        player = MediaPlayer.create(this, R.raw.siren_alert);
        player.setLooping(false); // Set looping
        player.start();
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