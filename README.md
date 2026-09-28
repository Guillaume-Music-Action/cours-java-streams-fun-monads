# Kata TDD : Talking Stick (bâton de parole)

**Le principe :** seule la personne qui tient le bâton peut parler. Le bâton circule entre des participants, et ceux qui veulent parler peuvent faire la queue.

Chaque étape est un comportement attendu. Les étudiants choisissent librement leur langage, leur modèle et leurs noms.

## Phase 1 : Possession

1. Au départ, personne ne tient le bâton.
2. Quand une personne prend le bâton libre, elle le tient.
3. La personne qui tient le bâton peut parler. Les autres ne peuvent pas.
4. Quand personne ne tient le bâton, personne ne peut parler.
5. On ne peut pas prendre le bâton s'il est déjà tenu, et celui qui le tenait le garde.
6. Quand la personne qui tient le bâton le rend, plus personne ne le tient.
7. Une personne qui ne tient pas le bâton ne peut pas le rendre.

## Phase 2 : Transmission

8. La personne qui tient le bâton peut le donner à quelqu'un d'autre, qui le tient alors.
9. Une personne qui ne tient pas le bâton ne peut pas le donner.
10. Se donner le bâton à soi-même n'a pas de sens. Le comportement exact est à décider en binôme.

## Phase 3 : Le cercle

11. Le bâton appartient à un cercle de participants défini à l'avance. Une personne hors du cercle ne peut pas le prendre.
12. On ne peut pas donner le bâton à une personne hors du cercle.
13. La personne qui tient le bâton peut le passer à son voisin, en suivant l'ordre du cercle.
14. Le dernier du cercle passe le bâton au premier.
15. Un cercle ne peut pas être vide, et une même personne ne peut pas y figurer deux fois.

## Phase 4 : Demandes de parole

16. Un participant peut demander la parole. Sa demande est alors enregistrée.
17. Demander la parole deux fois ne compte qu'une fois.
18. La personne qui tient le bâton ne peut pas demander la parole.
19. Quand le bâton est rendu, il va à la personne qui a demandé la parole en premier.
20. Quand le bâton est rendu et que personne n'a demandé la parole, plus personne ne le tient.
21. Un participant peut retirer sa demande de parole.
22. Tant que des demandes de parole sont en attente, personne ne peut prendre le bâton sans attendre son tour.

## Phase 5 : Temps de parole

23. On peut connaître depuis combien de temps la personne qui tient le bâton parle.
24. On peut fixer un temps de parole maximum. On sait alors si la personne qui tient le bâton l'a dépassé.
25. Si le temps est dépassé et que quelqu'un a demandé la parole, le bâton passe automatiquement à cette personne.
26. Si le temps est dépassé et que personne n'a demandé la parole, la personne qui tient le bâton continue de parler.

## Phase 6 : Historique (bonus)

27. On peut retrouver, dans l'ordre, qui a tenu le bâton.
28. On peut connaître le temps de parole total de chaque participant.
29. On peut savoir quel participant a le moins parlé.

## Notes pour l'animation

- **Format :** ping-pong en binôme. A écrit le test, B le fait passer et écrit le suivant. On change de rôle toutes les 5 à 7 minutes.
- **Durée :** les phases 1 à 3 prennent environ 1h30. Comptez 2 à 3h pour aller jusqu'à la phase 5. La phase 6 est réservée aux binômes rapides.
- **Règle du jeu :** on ne lit pas les étapes suivantes à l'avance. Le design doit émerger des tests, pas d'une anticipation.
- **Seule contrainte imposée :** les tests doivent être rapides et déterministes, y compris en phase 5.
- **Points de débrief :**
    - Étape 5 : comment chaque binôme a-t-il exprimé un refus ?
    - Étape 11 : quel impact l'arrivée du cercle a-t-elle eu sur les tests existants ?
    - Étape 19 : un comportement déjà testé change. Qu'avez-vous fait du test concerné ?
    - Étape 23 : comment avez-vous rendu le temps testable ?
    - En fin de session : comparer les designs des binômes. Qu'est-ce qui les rend si différents à partir des mêmes comportements ?

## Setup DevPod

Ce projet tourne dans DevPod (image `mcr.microsoft.com/devcontainers/java`, extensions Java pinnées pour compatibilité avec le langage server). Avant votre premier `devpod up` sur ce projet, exécutez une fois sur votre machine :

**macOS / Linux :**
```sh
./scripts/setup-devpod.sh
```

**Windows (PowerShell) :**
```powershell
.\scripts\setup-devpod.ps1
```

Ce script configure DevPod pour installer une version récente d'`openvscode-server` (au lieu de sa version par défaut, trop ancienne pour les extensions Java actuelles). C'est un réglage local à votre machine (`~/.devpod/config.yaml` sur macOS/Linux, `%USERPROFILE%\.devpod\config.yaml` sur Windows), pas un réglage du workspace — il doit être relancé une fois par machine, pas par workspace.
