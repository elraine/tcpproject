package com.tomatecuite.client;

public class FilePiece implements Comparable<FilePiece>{
    private int position;
    private byte[] data;
    private int length;

    public FilePiece(int position, byte[] data, int length){
        this.position = position;
        this.data = data;
        this.length = length;
    }

    public int getPosition(){
        return position;
    }

    public byte[] getData(){
        return data;
    }

    public int getLength(){
        return length;
    }

    public void setData(byte[] data){
        this.data = data;
    }

    @Override
    public int compareTo(FilePiece fp){
        if(this.getPosition() != fp.getPosition())
            return 1;
        else
            return 0;
    }
}