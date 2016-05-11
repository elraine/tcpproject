package com.tomatecuite.client;

import com.tomatecuite.*;
import java.util.*;
import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class FilePeerDescriptor implements Cloneable {
    /* Metadata */
    private String fileName;
    private String fileKey;
    private int fileSize;
    private int pieceSize;

    /* Real data */
    private File file;
    private BufferMap bm;
    private List<FilePiece> pieces;

    /**
     * Constructor for real file on the disk
     *
     * @param fileName Name of the file
     * @param fileKey Key of the file
     * @param fileSize Size of the file
     * @param pieceSize Size of the pieces composing the file
     * @param f The file itself
     */
    FilePeerDescriptor(String fileName, String fileKey, int fileSize, int pieceSize, File f) {
        this.fileName = fileName;
        this.fileKey = fileKey;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        this.file = f;
        this.bm = new BufferMap(fileSize / pieceSize + 1);
        this.pieces = Collections.synchronizedList(new ArrayList<>());
        LogWriter.getInstance().fileSpecs(fileName,fileSize, fileKey);
    }

    /**
     * Constructor using the metadata of the file
     *
     * @param fileKey Key of the file
     * @param pieceSize Size of the pieces composing the file
     * @param f the file itself
     */
    FilePeerDescriptor(String fileKey, int pieceSize, File f) {
        this(f.getName(), fileKey, (int) f.length(), pieceSize, f);
        LogWriter.getInstance().fileSpecs(f.getName(),f.length(), fileKey);
    }

    /**
     * Constructor for the file with just the metadata (starting the leech)
     *
     * @param fileName Name of the file
     * @param fileKey Key of the file
     * @param fileSize Size of the file
     * @param pieceSize Size of the pieces composing the file
     */
    public FilePeerDescriptor(String fileName, String fileKey, int fileSize, int pieceSize) {
        this(fileName, fileKey, fileSize, pieceSize, null);
        LogWriter.getInstance().fileSpecs(fileName,fileSize, fileKey);
    }

    /* Get methods */
    public String getName() {
        return this.fileName;
    }

    public String getKey() {
        return fileKey;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }

    public File getFile() {
        return file;
    }

    public BufferMap getBufferMap() {
        return bm;
    }

    public List<FilePiece> getPieces() {
        return pieces;
    }

    /* Set Methods */
    public void setKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public void setFile(File f) {
        this.file = f;
        this.fileSize = (int) f.length();
        this.bm = new BufferMap(((int) f.length() / pieceSize) + 1);
    }

    public void setBufferMap(BufferMap bm) {
        this.bm = bm;
    }

    public void addPiece(FilePiece piece) {
        pieces.add(piece);
        bm.set(piece.getPosition());
    }

    /* allow clone function between two instances of this object */
    @Override
    public FilePeerDescriptor clone() throws CloneNotSupportedException {
        return (FilePeerDescriptor) super.clone();
    }
}