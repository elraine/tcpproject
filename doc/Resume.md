#Projet Reseau S8


##Objectifs : 

- Faire un tracker
	- Doit garder en mémoire les fichiers mis a disposition et les seeders
	- Quand un client demande un fichier, le tracker doit renvoyer la liste des pairs et les moyens de connexions (ip:port)
	- Sauvegarder les logs dans un fichier
	- Avoir un fichier de configuration config.ini

	
	
- Faire un client
	- Sauvegarder les logs dans un fichier
	- Demander au tracker la liste des fichiers proposés par le tracker	- Demander le téléchargement d'un fichier au tracker
	- Le client peut se connecter a un autre client pour télécharger tout ou partie du fichier. 
	- Si une demande est faite au seed par un autre leecher, le seeder doit renvoyer sa BufferMap
	- Un leecher peut demander spécifiquement certaines pieces du fichiers selon le BufferMap. 
	- Echange périodique des BufferMaps entre leechers/seeders
	- Tolérer le départ d'un voisin
	- Supporter le téléchargement de fichier en parallèle
 	- Avoir un fichier de configuration config.ini
 		-	La taille des pièces doit être paramétrable et stockée dans le fichier de configuration.
 	- Message Framing

	
- Établir une conenxion entre un serveur et un client
- Établir une conenxion entre un serveur et des clients

##Management de projet 
Tracker en C, client en Java

###User Stories
Acteurs : Client={Seeder, Leecher}, tracker

- En tant que seeder, je veux pouvoir me connecter au tracker. 
- En tant que seeder, je veux pouvoir communiquer la liste des fichiers que je possède.
- En tant que seeder, je veux pouvoir spécifier mon port d'écoute.
- En tant que seeder, je veux pouvoir envoyer le BufferMap d'un fichier à un leecher.
- En tant que seeder, je veux pouvoir me connecter en même temps à plusieurs leechers pour l'envoi de fichier. 
- En tant que seeder, je veux pouvoir obtenir le hash d'un fichier afin de vérifier l'intégrité de celui-ci en fin de DL.
- En tant que seeder, je veux pouvoir envoyer une/des pièces spécifiques à un leecher qui les demande. 

//


- En tant que leecher, je veux pouvoir me connecter au tracker.
- En tant que leecher, je veux pouvoir voir la liste de fichier, filtrées selon des paramètres. 
- En tant que leecher, je veux pouvoir demander au tracker la liste de seeders d'un fichier.
- En tant que leecher, je veux pouvoir demander aux seeders quelles parties du fichier ils possèdent.
- En tant que leecher, je veux pouvoir demander le téléchargement de pièces de fichier. 
- En tant que leecher, je veux pouvoir me connecter en même temps à plusieurs seeder pour le téléchargement de fichier.
- En tant que leecher, je veux pouvoir communiquer ma BufferMap aux autres leechers/seeders afin de les tenir à jour de l'avancement de mon téléchargement.
- En tant que leecher, je veux pouvoir découvrir de nouvelles pièces téléchargeables dans mon voisinages. 
- En tant que leecher, je veux pouvoir exploiter mon fichier final à partir des pièces téléchargées. 


//


- En tant que client, je veux pouvoir sauvegarder ma configuration dans un fichier. 
- En tant que client, je veux enregistrer mes logs dans un fichier. 


- En tant que tracker, je veux pouvoir identifier les seeders.
- En tant que tracker, je veux pouvoir stocker la liste des fichiers.
- En tant que tracker, je veux avoir pour chaque fichier, la liste des seeders et leechers.
- En tant que tracker, je veux pouvoir annoncer de nouveaux seeders.
- En tant que tracker, je veux pouvoir mettre à jour les métadonnées d'un client suite à réception de la BufferMap.
