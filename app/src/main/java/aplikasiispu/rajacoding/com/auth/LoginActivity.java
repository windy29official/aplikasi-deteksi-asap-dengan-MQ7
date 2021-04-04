package aplikasiispu.rajacoding.com.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import aplikasiispu.rajacoding.com.MainActivity;
import aplikasiispu.rajacoding.com.R;
import aplikasiispu.rajacoding.com.helper.Config;
import aplikasiispu.rajacoding.com.helper.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPass, etIp;
    private ImageView img_loading;
    private RelativeLayout ly00, ly11;
    public SessionManager session;
    public String iduser;
    String username, pass, ip, idadmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        session = new SessionManager(getApplicationContext());

        ly00 = findViewById(R.id.ly00);
        ly11 = findViewById(R.id.ly11);
        img_loading = findViewById(R.id.img_loading);
        Glide.with(LoginActivity.this)
                .load("https://media.giphy.com/media/3ornjOID5ncUy9xohW/giphy.gif")
                .into(img_loading);
        etUsername = findViewById(R.id.etUsername);
        etPass = findViewById(R.id.etPass);
        etIp = findViewById(R.id.etIp);
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = etUsername.getText().toString().trim();
                pass = etPass.getText().toString().trim();
                ip = etIp.getText().toString().trim();
                
                if (username.length() == 0){
                    etUsername.setError("Username tidak boleh kosong");
                } else if  (pass.length() == 0){
                    etPass.setError("Password tidak boleh kosong");
                } else {
                    LoginData(username, pass, ip);
                }
            }
        });

        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void LoginData(final String username, final String pass, final String ip) {
        ly00.setVisibility(View.VISIBLE);
        ly11.setVisibility(View.GONE);
        AndroidNetworking.post("http://" + ip + Config.HOST + "login_admin.php")
                .addBodyParameter("username", username)
                .addBodyParameter("password", pass)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.optString("response").equals("1")){
                            ly00.setVisibility(View.GONE);
                            ly11.setVisibility(View.VISIBLE);

                            idadmin = response.optString("idadmin");

                            session.createLoginSession(iduser, ip);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login anda gagal, harap coba lagi!", Toast.LENGTH_SHORT).show();
                        }

                        ly00.setVisibility(View.GONE);
                        ly11.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(ANError error) {
                        ly00.setVisibility(View.GONE);
                        ly11.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this, "Server sedang terganggu, harap coba lagi!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}