package com.lunime.githubcollab.archanaberry.gachadesignstudio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.RemoteViews;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationCompat;

import org.apache.commons.io.FileUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class GachaStudioMain extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int DOWNLOAD_REQUEST_CODE = 2;
    private static final String APP_FOLDER = "/";

    private String CONFIG_FILE_PATH;
    private String TEMP_DIR_PATH;
    private String ZIP_FILE_PATH;
    private String EXTRACTED_DIR_PATH;
    private String SOURCE_DIR_PATH;
    private String DEST_DIR_PATH;

    private AlertDialog progressDialog;
    private ProgressBar progressBar;

    private RemoteViews remoteViews;
	private NotificationCompat.Builder builder;
    private NotificationManagerCompat notificationManager;

    private WebView webView;
    private long backPressedTime = 0;
    private static final int PRESS_BACK_INTERVAL = 2000; // 2 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inisialisasi path
        CONFIG_FILE_PATH = getExternalFilesDir(null).getAbsolutePath()
                + APP_FOLDER + "/data/data.xml";
        TEMP_DIR_PATH = getExternalFilesDir(null).getAbsolutePath()
                + APP_FOLDER + "/.temp";
        ZIP_FILE_PATH = TEMP_DIR_PATH + "/DL.zip";
        EXTRACTED_DIR_PATH = TEMP_DIR_PATH + "/";
        SOURCE_DIR_PATH = EXTRACTED_DIR_PATH + "Gacha-Design-Studio-DL/";
        DEST_DIR_PATH = getExternalFilesDir(null).getAbsolutePath()
                + APP_FOLDER + "/";

        boolean agreementAccepted = isAgreementAccepted();
        if (agreementAccepted) {
            loadWebViewContent();
        } else {
            showWelcomeDialog();
        }
    }

    private boolean isAgreementAccepted() {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            if (!configFile.exists()) {
                return false;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("agreement");
            Element eElement = (Element) nList.item(0);
            String accepted = eElement.getElementsByTagName("accepted").item(0).getTextContent();
            return Boolean.parseBoolean(accepted);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setAgreementAccepted(boolean accepted) {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element rootElement = doc.createElement("config");
            doc.appendChild(rootElement);

            Element agreement = doc.createElement("agreement");
            rootElement.appendChild(agreement);

            Element acceptedElement = doc.createElement("accepted");
            acceptedElement.appendChild(doc.createTextNode(Boolean.toString(accepted)));
            agreement.appendChild(acceptedElement);

            // Write the content into XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new FileOutputStream(configFile));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebViewContent() {

        // Menjadikan activity fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gachastudio_main);

        webView = findViewById(R.id.web);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);

        // Autoplay untuk audio
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        // Mendapatkan path ke direktori files di external storage
        File externalFilesDir = getExternalFilesDir(null);
        if (externalFilesDir != null) {
            String htmlFilePath = externalFilesDir.getAbsolutePath() + "/mainmenu.html";
            webView.loadUrl("file:///" + htmlFilePath);

            // Menampilkan pesan alert
            webView.setWebChromeClient(new android.webkit.WebChromeClient() {
                @Override
                public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GachaStudioMain.this);
                    builder.setTitle("Gacha Design Studio Berpesan!")
                            .setMessage(message)
                            .setPositiveButton("Salin ke Papan Klip", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Pesan", message);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(GachaStudioMain.this, "Pesan disalin ke papan klip", Toast.LENGTH_SHORT).show();
                                    result.confirm(); // Ini perlu untuk mengkonfirmasi alert
                                }
                            })
                            .setNegativeButton("Oke", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    result.confirm(); // Ini perlu untuk mengkonfirmasi alert
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    return true;
                }
            });
        }

        // Sembunyikan tombol navigasi
        webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    exitApp();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exitApp() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - backPressedTime > PRESS_BACK_INTERVAL) {
            backPressedTime = currentTime;
            Toast.makeText(this, "Tekan dua kali lagi untuk keluar dari game!", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000); // Tunggu 3 detik
                        backPressedTime = 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            finish();
            // force close aplikasi
            System.exit(0);
        }
    }

    // Method untuk menampilkan dialog selamat datang
    private void showWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selamat Datang di Gacha Design Studio!");
        builder.setMessage(
                "Gacha Design Studio hadir dengan bebas berkreasi membuat karakter Gacha sesuka mu dengan dukungan kustomisasi penuh.\n\nApakah kamu ingin memainkan nya?\nKalau iya tolong pilih setuju untuk mengizinkan akses penyimpanan dan unduh sumber daya tambahan sekitar 70Mb an.\n\n! Jika tidak setuju maka game ini keluar !\n\n\n\n\nGacha Design Studio ini dibuat olah para penggemar Lunime");
        builder.setPositiveButton("Setuju", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestStoragePermission();
                } else {
                    downloadResources();
                }
            }
        });

        builder.setNegativeButton("Tidak Setuju", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    // Method untuk meminta izin akses penyimpanan
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    // Method untuk memulai unduhan sumber daya tambahan
    private void downloadResources() {
        progressDialog = new AlertDialog.Builder(this).create();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        progressBar = dialogView.findViewById(R.id.progressBar);
        progressDialog.setView(dialogView);
        progressDialog.setTitle("Sedang mengunduh sumber daya tambahan...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        new DownloadResourcesTask().execute();
    }

    // AsyncTask untuk mengunduh sumber daya tambahan
    private class DownloadResourcesTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL("https://github.com/archanaberry/Gacha-Design-Studio/archive/refs/heads/DL.zip");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();

                File tempDir = new File(TEMP_DIR_PATH);
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }

                File outputFile = new File(ZIP_FILE_PATH);
                if (outputFile.exists()) {
                    outputFile.delete();
                }

                BufferedInputStream input = new BufferedInputStream(url.openStream());
                FileOutputStream output = new FileOutputStream(outputFile);

                byte[] data = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                unzip(ZIP_FILE_PATH, EXTRACTED_DIR_PATH);
                moveExtractedFiles(SOURCE_DIR_PATH, DEST_DIR_PATH);
                deleteTempDirectory();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setTitle("Sedang mengekstrak sumber daya tambahan...");
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            progressDialog.dismiss();
            if (success) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GachaStudioMain.this);
                builder.setTitle("Unduhan Berhasil");
                builder.setMessage("Sumber daya sudah berhasil diekstrak!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setAgreementAccepted(true);
                        loadWebViewContent();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(GachaStudioMain.this);
                builder.setTitle("Unduhan Gagal");
                builder.setMessage("Maaf, unduhan gagal. Silakan coba lagi nanti.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        }
    }

    private void unzip(String zipFilePath, String destDirectory) {
        try {
            File destDir = new File(destDirectory);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractFile(ZipInputStream zipIn, String filePath) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void moveExtractedFiles(String srcDirPath, String destDirPath) {
        try {
            File srcDir = new File(srcDirPath);
            File destDir = new File(destDirPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            FileUtils.copyDirectory(srcDir, destDir);

            FileUtils.deleteDirectory(srcDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteTempDirectory() {
        try {
            File tempDir = new File(TEMP_DIR_PATH);
            FileUtils.deleteDirectory(tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadResources();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Izin Diperlukan");
                builder.setMessage(
                        "Aplikasi memerlukan izin akses penyimpanan untuk melanjutkan. Izinkan akses penyimpanan melalui Pengaturan?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, DOWNLOAD_REQUEST_CODE);
                    }
                });
                builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        }
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DOWNLOAD_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadResources();
            } else {
                finish();
            }
        }
    }
}