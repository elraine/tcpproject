import java.util.*;
import java.io.*;
import java.nio.file.Files;


public class Peer {

	public Peer(){}
	// La fonction reception parse les ligne du fichier renseigné au chemin path_log, en fonction du mot clé du log on choisit une action correspondante
	public int recepetion(String path_log){



		try {


			BufferedReader reader = new BufferedReader(new FileReader(path_log));
			int action = 0;

			String line = null;

			while ((line = reader.readLine()) != null) {

			// Entre dans les actions
			if (line.startsWith("list")) {
				action = 1;
			} else if (line.startsWith("peers")) {
			    action = 2;
			} else if (line.startsWith("have")) {
			    action = 3;
			} else if (line.startsWith("data")) {
			
			    action = 4;
			}
			
				// decoupe le log dans un tableau de String, chaque mot est découpé quand un caractere vide est rencontré 
				String[] array = line.split( " ");
				
				switch (action) {
				case 1:

				//Retire le '[' du deuxieme mot du log grace au substring 
				array[1] = array[1].substring(1, array[1].length());
				
				//retire le ']' du dernier mot du log
				array[array.length-1] = array[array.length-1].substring(0, array[array.length-1].length()-1);
				
				// Appel fonction qui repond a la demande de look
				

				break;
				case 2:
				
				//Retire le '[' du troisième mot du log grace au substring 
				array[2] = array[2].substring(1, array[2].length());
				
				//retire le ']' du dernier mot du log
				array[array.length-1] = array[array.length-1].substring(0, array[array.length-1].length()-1);
				


				break;
				case 3:

				break;
				case 4:

				//Retire le '[' du toisième mot du log grace au substring 
				array[2] = array[2].substring(1, array[2].length());
				
				//retire le ']' du dernier mot du log
				array[array.length-1] = array[array.length-1].substring(0, array[array.length-1].length()-1);


				

				break;
				

			}

		}

	}

	catch (IOException e){

	    System.err.format("IOException : %s%n", e);
	}

	return 0;
}
}