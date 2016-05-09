package com.tomatecuite.client;

import java.util.BitSet;

/* Héritage de BitSet, classe qui représente un tableau de bits dont on peut adapter la taille */
public class BufferMap extends BitSet {
    private int bitLength;

    public BufferMap() {
        super();
    }

    public BufferMap(int bitLength) {
        super(bitLength);
        this.bitLength = bitLength;
    }

    public int getSizeBufferMap() {
        return bitLength;
    }

    public BufferMap stringToBufferMap(String s) {
        this.clear();

        int i = 0;
        for (char c : s.toCharArray()) {
            switch (c) {
                case '0':
                    this.set(i++, false);
                    break;
                case '1':
                    this.set(i++, true);
                    break;
            }
        }
        return this;
    }

    public String getStringForm() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < this.length(); i++) {
            boolean b = this.get(i);
            if (b) {
                s.append(1);
            } else {
                s.append(0);
            }
        }
        return s.toString();
    }
}
