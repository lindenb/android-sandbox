package com.github.lindenb.android.apps.downloadig;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final static int PICK_TEXT = 101;
    private static final String DEBUG_TAG = "MainActivity";
    private RetrieveImage retrieveThread = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** copy content of stream `in` to `out` */
    public static void copyTo(final InputStream in,final OutputStream out) throws IOException
    {
        final byte[] buffer =new byte[2048];
        int nRead;
        while((nRead=in.read(buffer))!=-1)
        {
            out.write(buffer,0,nRead);
        }
        out.flush();
    }

    private static boolean isURL(String s) {
        if(s==null) return false;
        s=s.trim();
        if(s.isEmpty()) return false;
        if(!(s.startsWith("https://") || s.startsWith("http://"))) return false;
        try {
            new URL(s);
            return true;
        } catch(Throwable err) {
            return false;
        }
    }

    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            final MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.reset();
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            BigInteger bigInt = new BigInteger(1,messageDigest);
            String hashtext = bigInt.toString(16);
            while(hashtext.length() < 32 ){
                hashtext = "0"+hashtext;
            }
            return hashtext;

        } catch (NoSuchAlgorithmException e) {
            Log.d(DEBUG_TAG,"md5",e);
        }
        return "md5.error";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_TEXT) {
                final Uri picked = data.getData();
                if(picked!=null) {
                    BufferedReader br = null;
                    InputStream is = null;
                    try {
                        is = getContentResolver().openInputStream(picked);
                        if (is != null) {
                            String line;
                            final Set<String> urls = new LinkedHashSet<>();
                            br = new BufferedReader(new InputStreamReader(is));
                            while ((line = br.readLine()) != null) {
                                line = line.trim();
                                if (!isURL(line)) continue;
                                urls.add(line);
                            }
                            br.close();
                            br = null;
                            is.close();
                            is = null;
                            final StringBuilder sb = new StringBuilder();
                            for (final String s : urls) sb.append(s).append("\n");
                            getTextArea().setText(sb.toString());
                        }
                    } catch (Throwable err) {
                        Log.d(DEBUG_TAG, "cannot open", err);
                    } finally {
                        if (br != null) try {
                            br.close();
                        } catch (final Throwable ignored) {
                        }
                        if (is != null) try {
                            is.close();
                        } catch (final Throwable ignored) {
                        }
                    }
                }
            }
        }
    }

    public void  openFile(final View view) {
        final Intent intent = new Intent();
        intent.setType("text/plain");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select URLs"), PICK_TEXT);
    }


    private class RetrieveImage extends Thread {
        final String textArea;
        int countSaved = 0;
        int countAlreadyExisted = 0;
        final Set<String> remains = new LinkedHashSet<>();

        RetrieveImage(String textArea) {
            this.textArea = textArea;
        }

        @Override
        public void run() {
            for(final String url:this.textArea.split("[\n\r \t]")) {
                if(url.trim().isEmpty()) continue;
                Log.d(DEBUG_TAG,"Trying URL: "+url);
                if(!isURL(url)) {
                    Log.d(DEBUG_TAG,"Not an url "+url);
                    remains.add(url);
                    continue;
                }
                final String imageUrl = getImage(url);
                if(!isURL(imageUrl)) {
                    Log.d(DEBUG_TAG,"cannot fetch from "+url);
                    remains.add(url);
                    continue;
                }


                URLConnection conn ;
                InputStream is = null;
                FileOutputStream fOut = null;
                File file = null;
                try {
                    // Assume block needs to be inside a Try/Catch block.
                    final String path = Environment.getExternalStorageDirectory().toString();
                    file = new File(path,md5(imageUrl)+(imageUrl.endsWith(".png")?".png":".jpg"));
                    if(file.exists()) {
                        countAlreadyExisted++;
                        continue;
                    }
                    Log.d(DEBUG_TAG,"file is "+file);

                    conn = new URL(imageUrl).openConnection();
                    is = conn.getInputStream();
                    fOut = new FileOutputStream(file);
                    copyTo(is,fOut);
                    fOut.flush(); // Not really required
                    fOut.close(); // do not forget to close the stream
                    fOut=null;
                    is.close();
                    is=null;
                    final File finalFile = file;
                    if(file==null) runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(),finalFile.getAbsolutePath(),finalFile.getName(),finalFile.getName());
                            } catch(IOException err2) {
                                Log.d(DEBUG_TAG,"Canot save",err2);
                            }
                        }
                    });

                    countSaved++;
                } catch(final Throwable err) {
                    Log.d(DEBUG_TAG,"Canot save",err);
                    remains.add(url);
                    if(file!=null) file.delete();
                }
                finally {
                    if(fOut!=null) try { fOut.close();} catch (IOException ignored) {}
                    if(is!=null) try {is.close();} catch(Throwable ignored) {}

                }

            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final StringBuilder sb = new StringBuilder();
                    for (final String s : remains) sb.append(s).append("\n");
                    getTextArea().setText(sb.toString());

                    if(countSaved>0 || countAlreadyExisted>0 || remains.size()>0) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Message");
                        final AlertDialog d = builder.setMessage("Saved:" + countSaved + " Existed:" + countAlreadyExisted + " Remains:" + remains.size()).create();
                        d.show();
                    }
                }
            });


            MainActivity.this.retrieveThread=null;
        }

        private String getImage(final String url) {
            if(!isURL(url)) {
                Log.d(DEBUG_TAG,"not url getImage:"+url);
                return null;
            }
            URLConnection conn;
            InputStream is = null;
            BufferedReader br = null;
            String imageURL = null;
            try
            {
                conn = new URL(url).openConnection();
                is = conn.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                final   String meta="<meta ";
                final   String prop="property=\"og:image\"";
                final   String content="content=\"";
                while ((line = br.readLine()) != null)
                {
                    int p = line.indexOf(meta);
                    if(p==-1) continue;
                    p+=meta.length();
                    p = line.indexOf(prop,p);
                    if(p==-1) continue;
                    p+=prop.length();
                    p = line.indexOf(content,p);
                    if(p==-1) continue;
                    p+=content.length();
                    int p2 = line.indexOf("\"",p);

                    if(p2==-1) continue;
                    imageURL = line.substring(p,p2);
                    if(!isURL(imageURL)) continue;
                    break;
                }

                br.close(); br= null;
                is.close(); is = null;

                return isURL(imageURL)?imageURL:null;
            }
            catch (final Exception e)
            {
                Log.d(DEBUG_TAG,"error while reading "+url,e);
                return null;
            }
            finally {
                if(br!=null) try {br.close();} catch(Throwable ignored) {}
                if(is!=null) try {is.close();} catch(Throwable ignored) {}
            }
        }


    }

    public void  saveImages(final View view) {
        Log.d(DEBUG_TAG, "Saving images");
        if(this.retrieveThread!=null) return;
        this.retrieveThread = new RetrieveImage(getTextArea().getText().toString());
        this.retrieveThread.start();
    }


    private EditText getTextArea() {
        return (EditText)findViewById(R.id.textArea);
    }

}