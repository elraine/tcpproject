package com.tomatecuite.client;

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

/*  public FilePeerDescriptor(String fileName, long fileLength, int pieceSize){
    name = fileName;
    length = fileLength;
    key = encode(name);
    this.pieceSize = pieceSize;
    this.pieceNumber = ((int)fpd.length % fpd.pieceSize) + 1;
    createBM();
  }

  private static String encode(String password) {
      byte[] uniqueKey = password.getBytes();
      byte[] hash = null;
      try {
          hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
      } catch (NoSuchAlgorithmException e) {
          throw new Error("No MD5 support in this VM.");
      }

      StringBuilder hashString = new StringBuilder();
      for (int i = 0; i < hash.length; i++) {
          String hex = Integer.toHexString(hash[i]);
          if (hex.length() == 1) {
              hashString.append('0');
              hashString.append(hex.charAt(hex.length() - 1));
          } else
              hashString.append(hex.substring(hex.length() - 2));
      }
      return hashString.toString();
  }

  private int[] getBM(){
    return this.bm;
  }

  private Bool isPieceCompleted(int t){
    return (this.bm[t] == 1);

  }
  private int setCursorBM(int t){
    if(this.bm[t] != 1){
      this.bm[t] = 1;
    }else{
      return -1;
    }
    return 0;
  }

}*/
