package com.mfs.rajputvarun591.mantra_biometric_flutter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class MFS100CodeHubs extends Activity implements MFS100Event {
    private static long Threshold = 1500;
    private static long mLastClkTime = 0;
    byte[] Enroll_Template;
    byte[] Verify_Template;
    Button btnClearLog;
    Button btnExtractAnsi;
    Button btnExtractISOImage;
    Button btnExtractWSQImage;
    Button btnInit;
    Button btnMatchISOTemplate;
    Button btnStopCapture;
    Button btnSyncCapture;
    Button btnUninit;
    Button btnDone;
    CheckBox cbFastDetection;
    ImageView imgFinger;
    private boolean isCaptureRunning = false;
    private FingerData lastCapFingerData = null;
    TextView lblMessage;
    private long mLastAttTime = 0;
    long mLastDttTime = 0;
    MFS100 mfs100 = null;
    ScannerAction scannerAction = ScannerAction.Capture;
    int timeout = 10000;
    EditText txtEventLog;

    FingerData fingerData;

    /* access modifiers changed from: private */
    public enum ScannerAction {
        Capture,
        Verify
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mfs100_sample);
        FindFormControls();
        try {
            getWindow().setSoftInputMode(3);
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
        try {
            this.mfs100 = new MFS100(this);
            this.mfs100.SetApplicationContext(this);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        try {
            if (this.mfs100 == null) {
                this.mfs100 = new MFS100(this);
                this.mfs100.SetApplicationContext(this);
            } else {
                InitScanner();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        try {
            if (this.isCaptureRunning) {
                this.mfs100.StopAutoCapture();
            }
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        try {
            if (this.mfs100 != null) {
                this.mfs100.Dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void FindFormControls() {
        try {
            this.btnInit = (Button) findViewById(R.id.btnInit);
            this.btnUninit = (Button) findViewById(R.id.btnUninit);
            this.btnDone = (Button) findViewById(R.id.btnDone);
            this.btnMatchISOTemplate = (Button) findViewById(R.id.btnMatchISOTemplate);
            this.btnExtractISOImage = (Button) findViewById(R.id.btnExtractISOImage);
            this.btnExtractAnsi = (Button) findViewById(R.id.btnExtractAnsi);
            this.btnExtractWSQImage = (Button) findViewById(R.id.btnExtractWSQImage);
            this.btnClearLog = (Button) findViewById(R.id.btnClearLog);
            this.lblMessage = (TextView) findViewById(R.id.lblMessage);
            this.txtEventLog = (EditText) findViewById(R.id.txtEventLog);
            this.imgFinger = (ImageView) findViewById(R.id.imgFinger);
            this.btnSyncCapture = (Button) findViewById(R.id.btnSyncCapture);
            this.btnStopCapture = (Button) findViewById(R.id.btnStopCapture);
            this.cbFastDetection = (CheckBox) findViewById(R.id.cbFastDetection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onControlClicked(View v) {
        if (SystemClock.elapsedRealtime() - mLastClkTime >= Threshold) {
            mLastClkTime = SystemClock.elapsedRealtime();
            try {
                switch (v.getId()) {
                    case R.id.btnClearLog:
                        ClearLog();
                        return;
                    case R.id.btnExtractAnsi:
                        ExtractANSITemplate();
                        return;
                    case R.id.btnExtractISOImage:
                        ExtractISOImage();
                        return;
                    case R.id.btnExtractWSQImage:
                        ExtractWSQImage();
                        return;
                    case R.id.btnInit:
                        InitScanner();
                        return;
                    case R.id.btnMatchISOTemplate:
                        this.scannerAction = ScannerAction.Verify;
                        if (!this.isCaptureRunning) {
                            StartSyncCapture();
                            return;
                        }
                        return;
                    case R.id.btnStopCapture:
                        StopCapture();
                        return;
                    case R.id.btnSyncCapture:
                        this.scannerAction = ScannerAction.Capture;
                        if (!this.isCaptureRunning) {
                            StartSyncCapture();
                            return;
                        }
                        return;
                    case R.id.btnUninit:
                        UnInitScanner();
                        return;
                    case R.id.btnDone:
                        goBackWithData();
                    default:
                        return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void goBackWithData() {
        Intent intent = new Intent();
        String data = "No data found";
        if (fingerData != null) {
            data = Base64.encodeToString(fingerData.FingerImage(), 101);
        }
        intent.putExtra("base64FingerPrintData", data);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_mfs100_sample);
        FindFormControls();
        try {
            if (this.mfs100 == null) {
                this.mfs100 = new MFS100(this);
                this.mfs100.SetApplicationContext(this);
            }
            if (this.isCaptureRunning && this.mfs100 != null) {
                this.mfs100.StopAutoCapture();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void InitScanner() {
        try {
            int ret = this.mfs100.Init();
            if (ret != 0) {
                SetTextOnUIThread(this.mfs100.GetErrorMsg(ret));
                return;
            }
            SetTextOnUIThread("Init success");
            SetLogOnUIThread("Serial: " + this.mfs100.GetDeviceInfo().SerialNo() + " Make: " + this.mfs100.GetDeviceInfo().Make() + " Model: " + this.mfs100.GetDeviceInfo().Model() + "\nCertificate: " + this.mfs100.GetCertification());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Init failed, unhandled exception", Toast.LENGTH_LONG).show();
            SetTextOnUIThread("Init failed, unhandled exception");
        }
    }

    private void StartSyncCapture() {
        new Thread(new Runnable() {
            /* class MFS100Test.AnonymousClass1 */

            public void run() {
                MFS100CodeHubs.this.SetTextOnUIThread("");
                MFS100CodeHubs.this.isCaptureRunning = true;
                try {
                    fingerData = new FingerData();
                    int ret = MFS100CodeHubs.this.mfs100.AutoCapture(fingerData, MFS100CodeHubs.this.timeout, MFS100CodeHubs.this.cbFastDetection.isChecked());
                    Log.e("StartSyncCapture.RET", "" + ret);
                    if (ret != 0) {
                        MFS100CodeHubs.this.SetTextOnUIThread(MFS100CodeHubs.this.mfs100.GetErrorMsg(ret));
                    } else {
                        MFS100CodeHubs.this.lastCapFingerData = fingerData;
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0, fingerData.FingerImage().length);
                        MFS100CodeHubs.this.runOnUiThread(new Runnable() {
                            /* class MFS100Test.AnonymousClass1.AnonymousClass1 */

                            public void run() {
                                MFS100CodeHubs.this.imgFinger.setImageBitmap(bitmap);
                            }
                        });
                        MFS100CodeHubs.this.SetTextOnUIThread("Capture Success");
                        MFS100CodeHubs.this.SetLogOnUIThread("\nQuality: " + fingerData.Quality() + "\nNFIQ: " + fingerData.Nfiq() + "\nWSQ Compress Ratio: " + fingerData.WSQCompressRatio() + "\nImage Dimensions (inch): " + fingerData.InWidth() + "\" X " + fingerData.InHeight() + "\"\nImage Area (inch): " + fingerData.InArea() + "\"\nResolution (dpi/ppi): " + fingerData.Resolution() + "\nGray Scale: " + fingerData.GrayScale() + "\nBits Per Pixal: " + fingerData.Bpp() + "\nWSQ Info: " + fingerData.WSQInfo());
                        MFS100CodeHubs.this.SetData2(fingerData);
                    }
                } catch (Exception e) {
                    MFS100CodeHubs.this.SetTextOnUIThread("Error");
                } catch (Throwable th) {
                    MFS100CodeHubs.this.isCaptureRunning = false;
                    throw th;
                }
                MFS100CodeHubs.this.isCaptureRunning = false;
            }
        }).start();
    }

    private void StopCapture() {
        try {
            this.mfs100.StopAutoCapture();
        } catch (Exception e) {
            SetTextOnUIThread("Error");
        }
    }

    private void ExtractANSITemplate() {
        try {
            if (this.lastCapFingerData == null) {
                SetTextOnUIThread("Finger not capture");
                return;
            }
            byte[] tempData = new byte[2000];
            int dataLen = this.mfs100.ExtractANSITemplate(this.lastCapFingerData.RawData(), tempData);
            if (dataLen > 0) {
                byte[] ansiTemplate = new byte[dataLen];
                System.arraycopy(tempData, 0, ansiTemplate, 0, dataLen);
                WriteFile("ANSITemplate.ansi", ansiTemplate);
                SetTextOnUIThread("Extract ANSI Template Success");
            } else if (dataLen == 0) {
                SetTextOnUIThread("Failed to extract ANSI Template");
            } else {
                SetTextOnUIThread(this.mfs100.GetErrorMsg(dataLen));
            }
        } catch (Exception e) {
            Log.e("Error", "Extract ANSI Template Error", e);
        }
    }

    private void ExtractISOImage() {
        try {
            if (this.lastCapFingerData == null) {
                SetTextOnUIThread("Finger not capture");
                return;
            }
            byte[] tempData = new byte[((this.mfs100.GetDeviceInfo().Width() * this.mfs100.GetDeviceInfo().Height()) + 1078)];
            int dataLen = this.mfs100.ExtractISOImage(this.lastCapFingerData.RawData(), tempData, 2);
            if (dataLen > 0) {
                byte[] isoImage = new byte[dataLen];
                System.arraycopy(tempData, 0, isoImage, 0, dataLen);
                WriteFile("ISOImage.iso", isoImage);
                SetTextOnUIThread("Extract ISO Image Success");
            } else if (dataLen == 0) {
                SetTextOnUIThread("Failed to extract ISO Image");
            } else {
                SetTextOnUIThread(this.mfs100.GetErrorMsg(dataLen));
            }
        } catch (Exception e) {
            Log.e("Error", "Extract ISO Image Error", e);
        }
    }

    private void ExtractWSQImage() {
        try {
            if (this.lastCapFingerData == null) {
                SetTextOnUIThread("Finger not capture");
                return;
            }
            byte[] tempData = new byte[((this.mfs100.GetDeviceInfo().Width() * this.mfs100.GetDeviceInfo().Height()) + 1078)];
            int dataLen = this.mfs100.ExtractWSQImage(this.lastCapFingerData.RawData(), tempData);
            if (dataLen > 0) {
                byte[] wsqImage = new byte[dataLen];
                System.arraycopy(tempData, 0, wsqImage, 0, dataLen);
                WriteFile("WSQ.wsq", wsqImage);
                SetTextOnUIThread("Extract WSQ Image Success");
            } else if (dataLen == 0) {
                SetTextOnUIThread("Failed to extract WSQ Image");
            } else {
                SetTextOnUIThread(this.mfs100.GetErrorMsg(dataLen));
            }
        } catch (Exception e) {
            Log.e("Error", "Extract WSQ Image Error", e);
        }
    }

    private void UnInitScanner() {
        try {
            int ret = this.mfs100.UnInit();
            if (ret != 0) {
                SetTextOnUIThread(this.mfs100.GetErrorMsg(ret));
                return;
            }
            SetLogOnUIThread("Uninit Success");
            SetTextOnUIThread("Uninit Success");
            this.lastCapFingerData = null;
        } catch (Exception e) {
            Log.e("UnInitScanner.EX", e.toString());
        }
    }

    private void WriteFile(String filename, byte[] bytes) {
        try {
            String path = Environment.getExternalStorageDirectory() + "//FingerData";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            String path2 = path + "//" + filename;
            File file2 = new File(path2);
            if (!file2.exists()) {
                file2.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(path2);
            stream.write(bytes);
            stream.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void WriteFileString(String filename, String data) {
        try {
            String path = Environment.getExternalStorageDirectory() + "//FingerData";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            String path2 = path + "//" + filename;
            File file2 = new File(path2);
            if (!file2.exists()) {
                file2.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(path2);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(data);
            writer.flush();
            writer.close();
            stream.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void ClearLog() {
        this.txtEventLog.post(new Runnable() {
            /* class MFS100Test.AnonymousClass2 */

            public void run() {
                try {
                    MFS100CodeHubs.this.txtEventLog.setText("", TextView.BufferType.EDITABLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void SetTextOnUIThread(final String str) {
        this.lblMessage.post(new Runnable() {
            /* class MFS100Test.AnonymousClass3 */

            public void run() {
                try {
                    MFS100CodeHubs.this.lblMessage.setText(str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void SetLogOnUIThread(final String str) {
        this.txtEventLog.post(new Runnable() {
            /* class MFS100Test.AnonymousClass4 */

            public void run() {
                try {
                    EditText editText = MFS100CodeHubs.this.txtEventLog;
                    editText.append("\n" + str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void SetData2(FingerData fingerData) {
        try {
            if (this.scannerAction.equals(ScannerAction.Capture)) {
                this.Enroll_Template = new byte[fingerData.ISOTemplate().length];
                System.arraycopy(fingerData.ISOTemplate(), 0, this.Enroll_Template, 0, fingerData.ISOTemplate().length);
            } else if (this.scannerAction.equals(ScannerAction.Verify)) {
                if (this.Enroll_Template != null) {
                    this.Verify_Template = new byte[fingerData.ISOTemplate().length];
                    System.arraycopy(fingerData.ISOTemplate(), 0, this.Verify_Template, 0, fingerData.ISOTemplate().length);
                    int ret = this.mfs100.MatchISO(this.Enroll_Template, this.Verify_Template);
                    if (ret < 0) {
                        SetTextOnUIThread("Error: " + ret + "(" + this.mfs100.GetErrorMsg(ret) + ")");
                    } else if (ret >= 96) {
                        SetTextOnUIThread("Finger matched with score: " + ret);
                    } else {
                        SetTextOnUIThread("Finger not matched, score: " + ret);
                    }
                } else {
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            WriteFile("Raw.raw", fingerData.RawData());
            WriteFile("Bitmap.bmp", fingerData.FingerImage());
            WriteFile("ISOTemplate.iso", fingerData.ISOTemplate());
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    @Override // com.mantra.mfs100.MFS100Event
    public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
        if (SystemClock.elapsedRealtime() - this.mLastAttTime >= Threshold) {
            this.mLastAttTime = SystemClock.elapsedRealtime();
            if (!hasPermission) {
                SetTextOnUIThread("Permission denied");
            } else if (vid != 1204 && vid != 11279) {
            } else {
                if (pid == 34323) {
                    try {
                        int ret = this.mfs100.LoadFirmware();
                        if (ret != 0) {
                            SetTextOnUIThread(this.mfs100.GetErrorMsg(ret));
                        } else {
                            SetTextOnUIThread("Load firmware success");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (pid == 4101) {
                    int ret2 = this.mfs100.Init();
                    if (ret2 == 0) {
                        showSuccessLog("Without Key");
                    } else {
                        SetTextOnUIThread(this.mfs100.GetErrorMsg(ret2));
                    }
                }
            }
        }
    }

    private void showSuccessLog(String key) {
        try {
            SetTextOnUIThread("Init success");
            SetLogOnUIThread("\nKey: " + key + "\nSerial: " + this.mfs100.GetDeviceInfo().SerialNo() + " Make: " + this.mfs100.GetDeviceInfo().Make() + " Model: " + this.mfs100.GetDeviceInfo().Model() + "\nCertificate: " + this.mfs100.GetCertification());
        } catch (Exception e) {
        }
    }

    @Override // com.mantra.mfs100.MFS100Event
    public void OnDeviceDetached() {
        try {
            if (SystemClock.elapsedRealtime() - this.mLastDttTime >= Threshold) {
                this.mLastDttTime = SystemClock.elapsedRealtime();
                UnInitScanner();
                SetTextOnUIThread("Device removed");
            }
        } catch (Exception e) {
        }
    }

    @Override // com.mantra.mfs100.MFS100Event
    public void OnHostCheckFailed(String err) {
        try {
            SetLogOnUIThread(err);
            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
        }
    }
}
