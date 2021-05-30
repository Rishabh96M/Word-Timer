package com.example.root.word_timer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileFunctions {
    ProgressDialog zipStatus, upload_status,download_status;
    private static final int BUFFER_SIZE = 8192 ;//2048;
    private static String TAG = FileFunctions.class.getName().toString();
    private static String parentPath ="";
    private String sourcePath = Environment.getExternalStorageDirectory() + "/AudioRecordings/" ;
    private String raw_file = Environment.getExternalStorageDirectory() + "/AudioRecordings/record_temp.raw" ;
    File myFile = new File(raw_file);
    private String destinationPath = Environment.getExternalStorageDirectory() + "/zip/" ;
    public String android_id, current_ts;
    private Long ts;
    public String destinationFileName;
    private boolean flag_done = false, flag_done1 = false;
    private Context mainActivity;
    private String directoryPath;
    public boolean downloading_wordlist=false;

    public void zip_files(String name){
        if(myFile.exists())
            myFile.delete();
        directoryPath = name;
        ts = System.currentTimeMillis();
        current_ts = ts.toString();
        destinationFileName = android_id + "_" + current_ts;
        zipStatus = new ProgressDialog(mainActivity);
        zipStatus.setMessage("Zipping files!\nPlease wait...\nThis might take some time");
        zipStatus.setCanceledOnTouchOutside(false);
        if(!zipStatus.isShowing())
            zipStatus.show();
        new zip_em_up().execute();
    }

    public void upload_files(){
        upload_status = new ProgressDialog(mainActivity);
        upload_status.setMessage("Uploading files!\nPlease wait...\nThis might take some time");
        upload_status.setCanceledOnTouchOutside(false);
        if(!upload_status.isShowing())
            upload_status.show();
        new ftp_transfer().execute(directoryPath);
    }

    public void download_word_list(Context conn){
        mainActivity = conn;
        download_status = new ProgressDialog(mainActivity);
        download_status.setMessage("Downloading Word List!\nPlease wait...\nThis might take some time");
        download_status.setCancelable(false);
        if(!download_status.isShowing())
            download_status.show();
        new ftp_download().execute();
    }

    private static boolean zipFile(ZipOutputStream zipOutputStream, String sourcePath) throws  IOException {
        java.io.File files = new java.io.File(sourcePath);
        java.io.File[] fileList = files.listFiles();

        if (fileList.length > 1) {
            String entryPath = "";
            BufferedInputStream input;
            for (java.io.File file : fileList) {
                if (file.isDirectory()) {
                    zipFile(zipOutputStream, file.getPath());
                } else {
                    byte data[] = new byte[BUFFER_SIZE];
                    FileInputStream fileInputStream = new FileInputStream(file.getPath());
                    input = new BufferedInputStream(fileInputStream, BUFFER_SIZE);
                    entryPath = file.getAbsolutePath().replace(parentPath, "");

                    ZipEntry entry = new ZipEntry(entryPath);
                    zipOutputStream.putNextEntry(entry);

                    int count;
                    while ((count = input.read(data, 0, BUFFER_SIZE)) != -1) {
                        zipOutputStream.write(data, 0, count);
                    }
                    input.close();
                }
            }
            return true;
        }
        else
            return false;
    }

    public class zip_em_up extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... params){
            new File(destinationPath).mkdirs();
            FileOutputStream fileOutputStream ;
            ZipOutputStream zipOutputStream =  null;
            try{
                if (!destinationPath.endsWith("/")) destinationPath+="/";
                String destination = destinationPath + destinationFileName;
                File file = new File(destination);
                if (!file.exists()) file.createNewFile();
                fileOutputStream = new FileOutputStream(file);
                zipOutputStream =  new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
                parentPath=sourcePath;
                flag_done1 = zipFile(zipOutputStream, sourcePath);
            }
            catch (IOException ioe){
                Log.d(TAG,ioe.getMessage());
            }finally {
                if(zipOutputStream!=null)
                    try {
                        zipOutputStream.close();
                    } catch(IOException e) {

                    }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String val){
            if(zipStatus.isShowing())
                zipStatus.dismiss();
            if(flag_done1)
                upload_files();
            else
                Toast.makeText(mainActivity, "No files to upload.\n Please record and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public class ftp_transfer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            FTPClient mFTPClient = new FTPClient();
            try {
                int i = 0;
                mFTPClient.setConnectTimeout(10000);
                mFTPClient.setDefaultTimeout(10000);
                mFTPClient.connect("13.232.76.89", 21);
                mFTPClient.login("ubuntu", "sirena");
                mFTPClient.setSoTimeout(30000);
                mFTPClient.setFileType(mFTPClient.BINARY_FILE_TYPE);

                mFTPClient.changeWorkingDirectory("/version_1.4");
                int returnCode = mFTPClient.getReplyCode();
                if (returnCode == 550) {
                    mFTPClient.changeWorkingDirectory("/");
                    mFTPClient.makeDirectory("Version_1.4");
                    mFTPClient.changeWorkingDirectory("/Version_1.4");
                }
                mFTPClient.changeWorkingDirectory("/Version_1.4/"+params[0]);
                int retCode = mFTPClient.getReplyCode();
                if (retCode == 550) {
                    mFTPClient.changeWorkingDirectory("/Version_1.4/");
                    mFTPClient.makeDirectory(params[0]);
                    mFTPClient.changeWorkingDirectory("/Version_1.4/"+params[0]);
                }
                File directory = new File(Environment.getExternalStorageDirectory() + "/zip/");
                File[] files_available = directory.listFiles();
                for (i = 0; i < files_available.length; i++) { // this will upload all the files
                    mFTPClient.storeFile(files_available[i].getName(), new FileInputStream(new File(Environment.getExternalStorageDirectory() + "/zip/" + String.valueOf(files_available[i].getName()))));
                }
                if(i == files_available.length)
                    flag_done = true;
                mFTPClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String val){
            if(upload_status.isShowing())
                upload_status.dismiss();

            File toDelete = new File(destinationPath);
            if (toDelete.isDirectory()) {
                String[] children = toDelete.list();
                for (int i = 0; i < children.length; i++) {
                    new File(toDelete, children[i]).delete();
                }
                toDelete.delete();
            }

            if(flag_done) {
                File toDelete1 = new File(sourcePath);
                if (toDelete1.isDirectory()) {
                    String[] children = toDelete1.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(toDelete1, children[i]).delete();
                    }
                }
                Toast.makeText(mainActivity,"Files uploaded successfully", Toast.LENGTH_SHORT);
            }
            else
                Toast.makeText(mainActivity, "Files did not get uploaded check the INTERNET connection and try again", Toast.LENGTH_SHORT).show();

            flag_done = false;
        }
    }

    public class ftp_download extends AsyncTask<String, String, String>{
        @Override
        public String doInBackground(String... params){
            downloading_wordlist=true;
            FTPClient mFTPClient = new FTPClient();
            try {
                mFTPClient.setConnectTimeout(10000);
                mFTPClient.setDefaultTimeout(10000);
                mFTPClient.connect("13.232.76.89", 21);
                mFTPClient.login("ubuntu", "sirena");
                FileOutputStream fos = new FileOutputStream( Environment.getExternalStorageDirectory()+"/wordlist.txt");
                mFTPClient.retrieveFile("/wordlist.txt",fos);
                if(fos != null){
                    fos.close();
                }
                mFTPClient.disconnect();
            }
            catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String val) {
            if(download_status.isShowing())
                download_status.dismiss();
            downloading_wordlist=false;
        }
    }
}
