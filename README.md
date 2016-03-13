# TODO

### Fonctionnalités/améliorations à implémenter ###

* Améliorer l'IA de combat
* Optimiser le pathfinder (changements de direction)
* Implémenter la lecture des valeurs négatives dans la classe ByteArray (bug du readVarShort...)
* Optimiser l'envoi des paquets (avec création de variables locales dans les frames) -> très lourd à faire
* ~~Faire une méthode "useInteractive"~~
* ~~Faire une "dialogFrame" (traitement des messages concernant les échanges et les dialogues avec les PNJ)~~
* ~~Gérer les combats en groupe~~
* Ajouter des critères pour le choix des groupes de monstres à combattre
* Améliorer les échanges et les dialogues avec les PNJ (suppression de "sleeps" peu propres)
* Implémenter les combats en groupe
* Supprimer à terme les boucles infinies lors de l'attente d'états
* Gérer le cas où un soldat se déconnecte (reconnexion ou suppression du vecteur de soldats)
* Optimiser la régénération de la vie (beaucoup de pertes de temps actuellement)

### Bugs à résoudre ###

* ~~**GameMapNoMovementMessage (probablement à cause d'une cellule courante fausse)**~~
* **"array index out of bounds" lors de la lecture d'un paquet**
* ~~ChangeMapMessage qui ne s'envoie pas~~
* ~~"Connection reset" pendant la création du serveur d'émulation~~
* ~~Déconnexion intempestive pour une raison inconnue (en combat ou en mouvement)~~
* ~~"None possible path found" sur les maps séparées par un "mur" d'obstacles~~
* ~~Problème d'accès concurrents dans la classe RoleplayContext (et peut-être aussi dans FightContext)~~
* Chemins de maps possiblement erronés des fois, à vérifier sur le long terme
* Cellules dans le coin des maps ne permettant de changer de map que d'un seul côté (donc problème quand on veut aller vers l'autre côté)
* Exceptions en double (FatalError)
* Temps mort à la création d'un path de maps (peut-être que c'est normal)
* **Launcher qui finit par s'écrouler au bout d'un certain temps**
* Mode absent qui ne fonctionne pas pour le capitaine (probablement à cause du fait qu'il est chef de groupe)

### Échanges ###

* ~~Vérifier le lanceur de l'échange (côté mule)~~
* ~~Vérifier si l'échange a été un succès ou pas du côté combattant~~
* ~~Ajouter les kamas lors de l'échange (fighter -> mule)~~
* ~~Si la demande d'échange a échoué (cible occupée ou pas encore chargée complètement sur la map), la relancer lorsque la cible sera disponible~~

### Interface graphique ###

* "Scroll down" automatique qui s'arrête
* Ajouter la couleur dans les logs des CharacterFrames
* Padding à ajouter autour des logs des CharacterFrames
* ~~Améliorer le rafraichissement des informations dans les CharacterFrames (trop lourd actuellement)~~ 

### Facultatif ###

* ~~Envoyer le message "GameContextReadyMessage"~~
* ~~Éviter de repartir sur l'aire de combat lorsqu'on est full pods~~
* Améliorer la réflexion
* Correction de la frame nulle ajoutée dans le vecteur de frames de l'instance de la mule (pas très propre)