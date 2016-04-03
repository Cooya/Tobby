# TODO

### Fonctionnalités/améliorations à implémenter ###

* Améliorer l'IA de combat
* Implémenter la lecture des valeurs négatives dans la classe ByteArray (bug du readVarShort...)
* Optimiser l'envoi des paquets (avec création de variables locales dans les frames) -> très lourd à faire
* ~~Faire une méthode "useInteractive"~~
* ~~Faire une "dialogFrame" (traitement des messages concernant les échanges et les dialogues avec les PNJ)~~
* ~~Gérer les combats en groupe~~
* ~~Ajouter des critères pour le choix des groupes de monstres à combattre~~
* Améliorer les échanges et les dialogues avec les PNJ (suppression de "sleeps" peu propres)
* Supprimer à terme les boucles infinies lors de l'attente d'états
* Ajouter, lors de la recherche d'un chemin vers une map distante, une cellule cible pour la map cible (ça évitera de se retrouver de l'autre côté d'une muraille par rapport au capitaine par exemple)
* ~~Mettre à jour l'aire de combat à chaque level up (que ce soit seul ou en groupe)~~
* Créer une API pour les "squads"
* Création automatique de personnage dans les comptes vides

### Bugs à résoudre ###

* ~~**GameMapNoMovementMessage (probablement à cause d'une cellule courante fausse)**~~
* **"array index out of bounds" lors de la lecture d'un paquet**
* ~~ChangeMapMessage qui ne s'envoie pas~~
* ~~"Connection reset" pendant la création du serveur d'émulation~~
* ~~Déconnexion intempestive pour une raison inconnue (en combat ou en mouvement)~~
* ~~"None possible path found" sur les maps séparées par un "mur" d'obstacles~~
* ~~Problème d'accès concurrents dans la classe RoleplayContext (et peut-être aussi dans FightContext)~~
* ~~Cellules dans le coin des maps ne permettant de changer de map que d'un seul côté (donc problème quand on veut aller vers l'autre côté)~~
* Exceptions en double (FatalError) -> inévitable je pense
* Temps mort à la création d'un chemin de maps vers une aire -> probablement normal (calcul de dizaines de chemins de maps)
* **Launcher qui finit par s'écrouler au bout d'un certain temps**
* ~~Mode absent qui ne fonctionne pas pour le capitaine (probablement à cause du fait qu'il est chef de groupe)~~
* ~~Chemin de maps calculé trop souvent (il prend du temps donc à optimiser)~~
* ~~**Au bout d'un moment, un message n'est pas envoyé (ou n'est pas reçu par le serveur), de ce fait, le bot est kické**~~
* ~~**Latence au lancement d'une CharacterFrame (et même des fois, plantage de la CharacterFrame)**~~
* **Erreur de frame nulle lors de certains lancements du launcher**

### Échanges ###

* ~~Vérifier le lanceur de l'échange (côté mule)~~
* ~~Vérifier si l'échange a été un succès ou pas du côté combattant~~
* ~~Ajouter les kamas lors de l'échange (fighter -> mule)~~
* ~~Si la demande d'échange a échoué (cible occupée ou pas encore chargée complètement sur la map), la relancer lorsque la cible sera disponible~~

### Groupes de combat ###

* ~~Après reconnexion lors d'un combat, l'invitation de groupe émise par le capitaine n'est pas reçue~~
* La mule a du mal à enchaîner les échanges avec plusieurs combattants à la suite
* Améliorer l'intégration d'un soldat au groupe (pas bien fait et donc trop long)
* ~~Gérer le cas où un soldat se déconnecte (reconnexion ou suppression du vecteur de soldats)~~
* Optimiser la régénération de la vie (beaucoup de pertes de temps actuellement)
* ~~Déconnecter un combattant lorsqu'il a atteint la limite de 200 combats par jour (traitement du "TextInformationMessage" reçu)~~
* Promouvoir un soldat lorsque le capitaine du groupe est déconnecté

### Interface graphique ###

* "Scroll down" automatique qui s'arrête
* Ajouter la couleur dans les logs des CharacterFrames
* Padding à ajouter autour des logs des CharacterFrames
* ~~Améliorer le rafraichissement des informations dans les CharacterFrames (trop lourd actuellement)~~
* Ajouter l'encodage "UTF-8" dans les CharacterFrames (pour afficher les accents)

### Facultatif ###

* ~~Envoyer le message "GameContextReadyMessage"~~
* ~~Éviter de repartir sur l'aire de combat lorsqu'on est full pods~~
* Optimiser le pathfinder (changements de direction)
* Améliorer la réflexion
* Correction de la frame nulle ajoutée dans le vecteur de frames de l'instance de la mule (pas très propre)
* Améliorer l'utilisation des "interactives" (récupérer le résultat et se rendre à la cellule adjacente à l'"interactive")
* ~~Gérer les "TextInformationMessages" qui peuvent donner des informations utiles pour le debuggage~~
* Traduire la classe "ParamsDecoder" pour un meilleur affichage des "TextInformationMessages"