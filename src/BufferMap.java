
//faire une hashTable de fichier avec une BufferMap

public class BufferMap{
  int arrLength;
  int[] bm;
  BufferMap(FilePeerDescriptor fpd){
    arrLength = (int)fpd.length % fpd.pieceSize;
    bm = new int[arrLength+1];
    bm={0};
  }

  private int[] getBufferMap(){
    return bm;
  }

  private int setBufferMap(int cursor){
    bm[cursor] = 1;

  }
}
