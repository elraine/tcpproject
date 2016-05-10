package com.tomatecuite.client;

import com.tomatecuite.*;
import java.util.*;
import java.io.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FileStorage{
    private static MessageDigest digest;

    private static final int PIECE_SIZE = Configuration.getInstance().getPropertyAsInt(Constants.PIECE_SIZE_KEY, 2048);

    private static final String ROOT_FOLDER_PATH = Configuration.getInstance().getProperty(Constants.LOCAL_STORAGE_KEY,
                    "downloads");

    private static FileStorage instance;
    private File rootFolder;
    private Map<String, FilePeerDescriptor> files;

    private FileStorage() {
        try{
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e){
            throw new Error("No MD5 support in this VM.");
        }

        rootFolder = new File(ROOT_FOLDER_PATH);
        if (!(rootFolder.isDirectory())) {
            throw new IllegalArgumentException();
        }
        // The synchronized (thread-safe) container
        files = Collections.synchronizedMap(new HashMap<String, FilePeerDescriptor>());
        // Create the seeded files list by browsing the local storage directory
        refreshSeededFilesList();

    }

    static FileStorage getInstance() {
        if (instance == null) {
            instance = new FileStorage();
        }
        return instance;
    }

    public void addLeechedFile(FilePeerDescriptor file) {
        if (file == null) {
            return;
        }
        try {
            FilePeerDescriptor newFile = file.clone();
            newFile.setBufferMap(new BufferMap(file.getBufferMap().getSizeBufferMap()));
            // Add the new file to the local storage
            synchronized (files) {
                files.put(newFile.getKey(), newFile);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    FilePeerDescriptor getFile(String key) {
        return files.get(key);
    }

    public List<FilePeerDescriptor> getFilesList() {
        return new ArrayList<>(getFilesMap().values());
    }

    public List<FilePeerDescriptor> getLeechList(){
        FileStorage fs = FileStorage.getInstance();

        ArrayList<FilePeerDescriptor> afpd = new ArrayList<FilePeerDescriptor>();
        List<FilePeerDescriptor> lfpd = fs.getFilesList();
        BufferMap bm;
        for (int i = 0; i < lfpd.size(); i++) {
            bm = lfpd.get(i).getBufferMap();
            BufferMap emptyBm = new BufferMap(bm.getSizeBufferMap());
            emptyBm.clear();
            bm.xor(emptyBm);
            if(bm.isEmpty()) afpd.add(lfpd.get(i));
        }
        return afpd;
    }

    private Map<String, FilePeerDescriptor> getFilesMap() {
        if (files.isEmpty()) {
            refreshSeededFilesList();
        }
        return files;
    }

    File createLeechedFile(FilePeerDescriptor file) {

        if (file.getName() == null || file.getName().isEmpty()) {
            throw new IllegalArgumentException();
        }

        // Creates the new file on the disk
        File newFile = new File(ROOT_FOLDER_PATH + File.separator
                + file.getName());
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Associates the physical file to the peer file
        file.setFile(newFile);

        return newFile;
    }

    private void refreshSeededFilesList() {

        for (File file : rootFolder.listFiles()) {

            if (!file.isFile()) {
                continue;
            }

            String key = getMD5FileDigest(file);
            FilePeerDescriptor filePeer = new FilePeerDescriptor(key, PIECE_SIZE, file);
            // Load file content by block (piece)
            try {
                InputStream reader = new FileInputStream(file);
                int i = 0;
                int len = 0;
                byte[] buffer = new byte[PIECE_SIZE];
                do {
                    // Reads the file by block of PIECE_SIZE
                    len = reader.read(buffer, 0, PIECE_SIZE);
                    if (len > 0) {
                        // Get only valid data (read data). It avoid invalid
                        // zero byte list at the end of the stream for instance.
                        ByteBuffer buff = ByteBuffer.allocate(len);
                        buff.put(buffer, 0, len);

                        // Simply create an ordered piece and add it into the
                        // peer file
                        filePeer.addPiece(new FilePiece(i++, buff.array(), len));
                    }

                    // Clear the buffer
                    buffer = new byte[PIECE_SIZE];

                } while (len != -1);

                /* Close the stream */
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            /* Put the file into the map */
            synchronized (files) {
                files.put(filePeer.getKey(), filePeer);
            }
        }
    }

    void saveFilePeerToDisk(FilePeerDescriptor file) throws Exception {

        if (file == null) {
            return;
        }
        File physFile = file.getFile();
        if (physFile == null) {
            physFile = createLeechedFile(file);
        }

        // 1. Sort pieces into the list
        List<FilePiece> pieces = file.getPieces();
        Collections.sort(pieces);

        // 2. Write data into the file
        try {
            FileOutputStream stream = new FileOutputStream(physFile);
            for (FilePiece piece : pieces) {
                if (piece == null || piece.getData() == null) {
                    continue;
                }
                stream.write(piece.getData());
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3. Check if the file is not corrupted by re-computing its MD5 hash
        String newKey = getMD5FileDigest(physFile);
        if (newKey.equals(file.getKey())) {
            System.out.println("VALID FILE !");
        } else {
            Boolean b = true;
            Scanner sc = new Scanner(System.in);
            System.out.println("The file " + file.getName() + "is corrupted. Do you want to delete it ?");
            String s = sc.nextLine();
            if(s.compareTo("no") == 0)
                b = false;
            else
                System.out.println("Incorrect answer, file delete");

            if (b) {
                files.remove(file.getKey());
                physFile.delete();
            }
            /*
            int n = JOptionPane.showConfirmDialog(new JFrame(), "The file \""
                            + file.getName()
                            + "\" is corrupted ! Would you want to keep it ? ",
                    "Warning !", JOptionPane.YES_NO_OPTION);
            // 3.1 If the file is corrupted, lets the user choose the
            // appropriate option (Keep file or delete it)
            if (JOptionPane.YES_OPTION != n) {
                synchronized (files) {
                    files.remove(file.getKey());
                }
                physFile.delete();
                TrackeirbUI.getInstance().removeFileProgressFromView(file);
                return;
                */
        }
    }

    private String getMD5FileDigest(File file) {

        byte[] b = new byte[(int) file.length()];
        try {
            InputStream stream = new FileInputStream(file);
            int bRead = stream.read(b);
            if (bRead > 0) {
                return new BigInteger(1, digest.digest(b)).toString(16);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}