# Documentation des APIs du Projet Gestion de Véhicules

## Table des matières
- [Authentification](#authentification)
- [Utilisateurs](#utilisateurs)
- [Véhicules](#véhicules)
- [Départements](#départements)
- [Messagerie](#messagerie)
- [Configuration](#configuration)
- [Export](#export)
- [SMS](#sms)
- [Établissements](#établissements)

## Authentification
- `POST /login/` - Connexion des utilisateurs
- `GET /logout/` - Déconnexion
- `GET /profile/` - Profil de l'utilisateur connecté
- `POST /profile/edit/` - Modification du profil
- `POST /set-language/` - Changer la langue de l'application

## Utilisateurs
- `GET /users/` - Liste des utilisateurs
- `POST /users/create/` - Créer un utilisateur
- `POST /users/<int:pk>/edit/` - Modifier un utilisateur
- `POST /users/<int:pk>/password-reset/` - Réinitialiser le mot de passe
- `POST /users/<int:pk>/toggle-active/` - Activer/désactiver un utilisateur
- `POST /users/<int:pk>/delete/` - Supprimer un utilisateur
- `POST /users/<int:pk>/change-departement/` - Changer le département d'un utilisateur

## Véhicules
- `GET /vehicules/` - Liste des véhicules
- `POST /vehicules/create/` - Créer un véhicule
- `GET /vehicules/<int:pk>/` - Détails d'un véhicule
- `POST /vehicules/<int:pk>/edit/` - Modifier un véhicule
- `POST /vehicules/<int:pk>/delete/` - Supprimer un véhicule
- `POST /vehicule/<int:vehicule_id>/changer-etablissement/` - Changer l'établissement d'un véhicule

## Départements
- `GET /departements/` - Liste des départements
- `POST /departements/create/` - Créer un département
- `GET /departements/<int:pk>/` - Détails d'un département
- `POST /departements/<int:pk>/edit/` - Modifier un département

## Messagerie
- `POST /messagerie/send/` - Envoyer un message
- `GET /messagerie/messages/` - Récupérer les messages
- `GET /messagerie/users/` - Liste des utilisateurs pour la messagerie
- `GET /messagerie/unread_status/` - Statut des messages non lus

## Configuration
- `GET /configuration/` - Page de configuration
- `POST /application-control/password/` - Vérifier le mot de passe de contrôle d'application
- `GET /application-control/` - Page de contrôle d'application
- `GET /application-blocked/` - Page d'application bloquée
- `POST /application-control/logout/` - Déconnexion du contrôle d'application

## Export
- `GET /users/export/pdf/` - Exporter la liste des utilisateurs en PDF
- `GET /users/export/excel/` - Exporter la liste des utilisateurs en Excel
- `GET /vehicules/export/pdf/` - Exporter la liste des véhicules en PDF
- `GET /vehicules/<int:pk>/export/pdf/` - Exporter les détails d'un véhicule en PDF
- `GET /vehicules/export/excel/` - Exporter la liste des véhicules en Excel

## SMS
- `GET /send-test-sms/` - Envoyer un SMS de test
- `GET /send-test-sms-africastalking/` - Tester l'envoi via Africa's Talking

## Établissements
- `POST /etablissement/create/` - Créer un établissement
- `GET /choose-etablissement/` - Choisir un établissement

## Rapports Chauffeurs
### Rapports
- `GET /chauffeur/rapport/` - Affiche un rapport détaillé du chauffeur connecté
  - Paramètres GET optionnels :
    - `date_debut` (date): Filtrer les missions à partir de cette date
    - `date_fin` (date): Filtrer les missions jusqu'à cette date
    - `statut` (string): Filtrer par statut de mission
    - `vehicule` (int): ID du véhicule pour filtrer les missions
  - Retourne : Une page HTML avec les statistiques et la liste des missions

- `GET /chauffeur/rapport/pdf/` - Exporte le rapport du chauffeur au format PDF
  - Paramètres GET optionnels : identiques à la vue de rapport
  - Retourne : Un fichier PDF contenant le rapport

- `GET /chauffeur/rapport/excel/` - Exporte le rapport du chauffeur au format Excel
  - Paramètres GET optionnels : identiques à la vue de rapport
  - Retourne : Un fichier Excel contenant le rapport

### Rapports Demandeur
- `GET /chauffeur/rapport-demandeur/` - Affiche un rapport pour le demandeur connecté
  - Paramètres GET optionnels : identiques à la vue de rapport chauffeur
  - Retourne : Une page HTML avec les statistiques et la liste des missions

- `GET /chauffeur/rapport-demandeur/pdf/` - Exporte le rapport du demandeur au format PDF
  - Paramètres GET optionnels : identiques à la vue de rapport
  - Retourne : Un fichier PDF contenant le rapport

- `GET /chauffeur/rapport-demandeur/excel/` - Exporte le rapport du demandeur au format Excel
  - Paramètres GET optionnels : identiques à la vue de rapport
  - Retourne : Un fichier Excel contenant le rapport

### Rapports Administratifs
- `GET /chauffeur/rapport-global/` - Affiche un rapport global des missions par demandeur (admin uniquement)
  - Accès : Administrateurs uniquement
  - Retourne : Une page HTML avec les statistiques globales

## Notes
- Toutes les URLs sont préfixées par le chemin de base de l'application
- Les méthodes HTTP doivent être respectées (GET, POST, etc.)
- Les endpoints nécessitant une authentification renverront une erreur 403 si l'utilisateur n'est pas connecté.
- Les dates doivent être au format YYYY-MM-DD
- Les exports (PDF/Excel) utilisent les mêmes paramètres de filtre que la vue HTML