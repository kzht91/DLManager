package javaapp;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.security.*;
import java.util.concurrent.*;
import java.util.logging.*;

class Download extends Observable implements Runnable {

    public static final int DOWNLOADING = 0;
    public static final int COMPLETED = 1;
    public static final int CANCELLED = 2;
    public static final int ERROR = 3;
    public static final int STARTING = 4;
    public static final String STATUSES[] = {"Downloading", "Completed", "Cancelled", "Error", "Starting"};
    private URL url;
    private String fName;
    private int size, done, left, status;

    public Download(URL url) {
        this.url = url;
        this.fName = url.getFile().substring(url.getFile().lastIndexOf('/') + 1);
        size = 0;
        left = size;
        done = 0;
        download();
    }

    public String getUrl() {
        return url.toString();
    }

    public String getFileName() {
        return fName;
    }

    public int getSize() {
        return size;
    }

    public int getLeft() {
        return left;
    }

    public int getDone() {
        return done;
    }

    public int getStatus() {
        return status;
    }

    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }

    private void error() {
        status = ERROR;
        stateChanged();
    }

    private void download() {
        Thread thread = new Thread(this);
        thread.start();
    }

    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    @Override
    public void run() {
        status = STARTING;
        stateChanged();
        String checkSum;
        FileDialog fileDialog;
        String extension;
        try {
            extension = getFileName(url).substring(getFileName(url).lastIndexOf('.'));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

            fileDialog = new FileDialog(new JFrame(), "Сохранить", FileDialog.SAVE);
            fileDialog.setFilenameFilter((File dir, String name) -> name.endsWith(extension));
            fileDialog.setFile(getFileName(url));
            fileDialog.setVisible(true);
            MessageDigest md5Digest;
            md5Digest = MessageDigest.getInstance("MD5");
            if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
                fName = fileDialog.getFile().substring(fileDialog.getFile().lastIndexOf('/') + 1);
                stateChanged();
                File f1 = new File(fileDialog.getDirectory() + fName);
                FileOutputStream fw = new FileOutputStream(f1);
                byte[] b = new byte[1024];
                int count = 0;
                int contentLength = conn.getContentLength();
                if (contentLength < 1) {
                    error();
                }
                if (size == 0) {
                    size = contentLength;
                    stateChanged();
                }
                ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                exec.scheduleAtFixedRate(() -> this.stateChanged(), 0, 1, TimeUnit.SECONDS);
                status = DOWNLOADING;
                stateChanged();
                while ((count = bis.read(b)) != -1) {
                    if (status == CANCELLED) {
                        done = 0;
                        left = 0;
                        fw.close();
                        f1.delete();
                        break;
                    }
                    fw.write(b, 0, count);
                    md5Digest.update(b, 0, count);
                    done += count;
                    left = size - done;
                }
                fw.close();
            } else {
                status = CANCELLED;
            }
            if (status == DOWNLOADING) {
                status = COMPLETED;
                byte[] bytes = md5Digest.digest();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                    sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                }
                checkSum = sb.toString();
                fName = fName.substring(0, fName.lastIndexOf('.'));
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileDialog.getDirectory() + fName + ".check", true));
                bw.write(checkSum);
                bw.close();
                JOptionPane.showMessageDialog(new JFrame(), "File " + fName + " has been downloaded");
            }
            stateChanged();
        } catch (IOException ex) {
            status = ERROR;
            stateChanged();
            JOptionPane.showMessageDialog(new JFrame(), "Download error, connection failed");
        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(new JFrame(), "Can't create checksum file");
            Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void stateChanged() {
        setChanged();
        notifyObservers();
    }

}
